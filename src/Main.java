import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
	public static Mixer.Info infoCable;
	public static Mixer.Info infoSpeakers;
	public static boolean allowParallelAudio = true;
	public static File startDir = new File("D:/Applications/SLAM/sounds");
	public static FormMain window;
	public static FormKeys windowKeys;
	private static ArrayList<File> sounds = new ArrayList<>();
	private static ArrayList<Clip> playingClips = new ArrayList<>();
	private static MicManager.ThreadMic threadMic;
	private static float gainMod = 1.0f;
	private static ArrayList<FloatControl> gains = new ArrayList<>();
	private static java.util.HashMap<Integer, EnumKeyAction> keybindings = new HashMap<>();
	private static EnumKeyAction toBind;

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			GlobalScreen.registerNativeHook();
		} catch (Exception e) {
			e.printStackTrace();
		}

		GlobalScreen.addNativeKeyListener(new GlobalKeyListener());
		Logger.getLogger(GlobalScreen.class.getPackage().getName()).setLevel(Level.OFF);

		window = new FormMain();
		window.updateFromDir(startDir);

	}

	public static void play(List<File> listFiles) {
		new Thread(() -> {
			for (File file : listFiles) {
				if (file == null || !file.exists()) {
					window.setStatus("No sound selected");
					return;
				}
				if (!allowParallelAudio) {
					stop();
				}
				try {
					if (file.getName().replaceAll("^.*\\.(.*)$", "$1").equals("wav")) {
						playClip(AudioSystem.getAudioInputStream(file), infoCable, file.getName());
						playClip(AudioSystem.getAudioInputStream(file), infoSpeakers, file.getName());
					} else {
						System.out.println("Not WAV, converting and playing");
						AudioInputStream stream = AudioSystem.getAudioInputStream(file);
						AudioFormat format = new AudioFormat(44100, 16, 2, true, true);
						playClip(AudioSystem.getAudioInputStream(format, stream), infoSpeakers, file.getName());

						AudioInputStream streama = AudioSystem.getAudioInputStream(file);
						AudioFormat formata = new AudioFormat(44100, 16, 2, true, true);
						playClip(AudioSystem.getAudioInputStream(formata, streama), infoCable, file.getName());

						//lol why do I need to copypaste this for it to work instead of sharing the original audioinputstream ;-;


						//				AudioFormat formatBase = stream.getFormat();
						//				AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
						//										formatBase.getSampleRate(),
						//										16,
						//										formatBase.getChannels(),
						//										formatBase.getChannels() * 2,
						//										formatBase.getSampleRate(),
						//										false);
					}
				} catch (Exception e) {
					System.out.println("Exception occurred");
					e.printStackTrace();
					window.setStatus(e.getLocalizedMessage());
				}
			}
		}).start();
	}

	public static void stop() {
		for (Clip clip : playingClips) {
			clip.stop();
		}
		playingClips.clear();
		gains.clear();
	}

	public static void toggleRelay() {
		if (threadMic != null) {
			try {
				threadMic.running = false;
				threadMic.join();
				threadMic = null;
			} catch (Exception e) {
				System.out.println("Exception relaying mic");
				e.printStackTrace();
				window.setStatus(e.getLocalizedMessage());
			}
		} else {
			threadMic = new MicManager.ThreadMic();
			threadMic.start();
		}
	}

	private static void playClip(AudioInputStream stream, Mixer.Info info, String name) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
		Clip clip = AudioSystem.getClip(info);
		clip.addLineListener(e -> {
			LineEvent.Type t = e.getType();
			if (t == LineEvent.Type.START) {
				window.setStatus("Playing: " + name);
			} else if (t == LineEvent.Type.STOP) {
				//				playingClips.remove(clip);
				//				if (playingClips.size() == 0)
				//					window.setStatus("Stopped");
			}
		});
		clip.open(stream);

		FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		gains.add(gain);
		setGain(gainMod);
		clip.start();
		playingClips.add(clip);
	}

	public static void addFiles(File[] f) {
		for (File file : f) {
			sounds.add(file);
		}
	}

	public static void setInfoCable(Mixer.Info info) {
		infoCable = info;
	}

	public static void setInfoSpeakers(Mixer.Info info) {
		infoSpeakers = info;
	}

	public static void setGain(float v) {
		gainMod = v;
		for (FloatControl gain : gains) {
			gain.setValue(((gain.getMaximum() - gain.getMinimum()) * gainMod) + gain.getMinimum());
		}
	}

	public static void setBinding(EnumKeyAction action) {
		toBind=action;
	}

	public static boolean isRelaying() {
		return threadMic == null;
	}

	private static class GlobalKeyListener implements NativeKeyListener {
		public void nativeKeyPressed(NativeKeyEvent e) {
			System.out.println("Keydown: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
			if (toBind != null) {
				System.out.println("BINDING KEY");
				keybindings.put(e.getRawCode(), toBind);
				toBind.setKey(e.getKeyCode());
				windowKeys.updateButtons();
				toBind = null;
			} else {
				EnumKeyAction exec;
				if ((exec = keybindings.get(e.getRawCode())) != null )
					exec.getAction().run();
			}
		}

		@Override
		public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

		}

		@Override
		public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

		}
	}

	public static enum EnumKeyAction {
		PLAY,
		STOP;

		private Runnable action;
		private int key;
		public void setAction(Runnable action) {
			this.action = action;
		}
		public Runnable getAction() {
			return action;
		}

		public void setKey(int key) {
			this.key = key;
		}

		public int getKey() {
			return key;
		}
	}

	static {
		EnumKeyAction.PLAY.setAction(()->Main.play(Main.window.getSelectedFiles()));
		EnumKeyAction.STOP.setAction(Main::stop);
	}
}
