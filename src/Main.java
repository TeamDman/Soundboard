import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
	private static final ArrayList<File> sounds = new ArrayList<>();
	private static final ArrayList<Clip> playingClips = new ArrayList<>();
	private static final ArrayList<FloatControl> gains = new ArrayList<>();
	private static final java.util.HashMap<Integer, EnumKeyAction> keybindings = new HashMap<>();
	public static Mixer.Info infoCable;
	public static boolean allowParallelAudio = true;
	public static File startDir;
	public static FormMain window;
	public static FormKeys windowKeys;
	private static Mixer.Info infoSpeakers;
	private static MicManager.ThreadMic threadMic;
	private static float gainMod = 1.0f;
	private static EnumKeyAction toBind;

	static {
		EnumKeyAction.PLAY.setAction(() -> Main.play(Main.window.getSelectedFiles()));
		EnumKeyAction.STOP.setAction(Main::stop);
		EnumKeyAction.VOLUP.setAction(Main::increaseGain);
		EnumKeyAction.VOLDOWN.setAction(Main::decreaseGain);
		EnumKeyAction.NEXT.setAction(Main::soundNext);
		EnumKeyAction.PREV.setAction(Main::soundPrev);
		EnumKeyAction.RELAY.setAction(() -> window.toggleRelay());
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			GlobalScreen.registerNativeHook();
		} catch (Exception e) {
			e.printStackTrace();
		}

		GlobalScreen.addNativeKeyListener(new GlobalKeyListener());
		Logger.getLogger(GlobalScreen.class.getPackage().getName()).setLevel(Level.OFF);
		PreferenceManager.init();

		EventQueue.invokeLater(() -> {
			window = new FormMain();
			window.updateFromDir(startDir);
		});

	}

	static void play(List<File> listFiles) {
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

	private static void playClip(AudioInputStream stream, Mixer.Info info, String name) throws LineUnavailableException, IOException {
		Clip clip = AudioSystem.getClip(info);
		clip.addLineListener(e -> {
			LineEvent.Type t = e.getType();
			if (t == LineEvent.Type.START) {
				window.setStatus("Playing: " + name);
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
		Collections.addAll(sounds, f);
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


	public static Mixer.Info getInfoCable() {
		return infoCable;
	}

	public static void setInfoCable(Mixer.Info info) {
		boolean update = infoCable != null;
		infoCable = info;

		if (update)
			PreferenceManager.save();
		if (window != null)
			window.updateCombos();
	}

	public static Mixer.Info getInfoSpeakers() {
		return infoSpeakers;
	}

	public static void setInfoSpeakers(Mixer.Info info) {
		boolean update = infoCable != null;
		infoSpeakers = info;

		if (update)
			PreferenceManager.save();
		if (window != null)
			window.updateCombos();
	}

	public static float getGain() {
		return gainMod;
	}

	public static void setGain(float v) {
		gainMod = v;
		PreferenceManager.save();
		for (FloatControl gain : gains) {
			gain.setValue(((gain.getMaximum() - gain.getMinimum()) * gainMod) + gain.getMinimum());
		}
	}

	public static void setGain(float v, boolean b) {
		gainMod = v;
	}


	private static void increaseGain() {
		gainMod = Math.min(gainMod + 0.02f, 1);
		window.updateVol();
	}

	private static void decreaseGain() {
		gainMod = Math.max(gainMod - 0.02f, 0);
		window.updateVol();
	}

	private static void soundNext() {
		window.updateNext();
	}

	private static void soundPrev() {
		window.updatePrev();
	}


	public static void setBinding(EnumKeyAction action) {
		toBind = action;
	}

	public static void setBinding(EnumKeyAction action, int key, String keyName) {
		keybindings.put(key, action);
		action.setKey(key, keyName);
		toBind = null;
	}

	public static boolean isRelaying() {
		return threadMic != null;
	}


	public enum EnumKeyAction {
		PLAY,
		STOP,
		VOLUP,
		VOLDOWN,
		NEXT,
		PREV,
		RELAY;

		private Runnable action;
		private int key = 0;
		private String keyName = "undefined";

		Runnable getAction() {
			return action;
		}

		void setAction(Runnable action) {
			this.action = action;
		}

		void setKey(int key, String keyName) {
			this.key = key;
			this.keyName = keyName;
		}

		public int getKey() {
			return key;
		}

		public String getKeyName() {
			return keyName;
		}
	}

	private static class GlobalKeyListener implements NativeKeyListener {
		@Override
		public void nativeKeyTyped(NativeKeyEvent e) {
		}

		public void nativeKeyPressed(NativeKeyEvent e) {
			if (toBind != null) {
				System.out.println("BINDING KEY");
				setBinding(toBind, e.getRawCode(), NativeKeyEvent.getKeyText(e.getKeyCode()));
				PreferenceManager.save();
				windowKeys.updateButtons();
			} else {
				EnumKeyAction exec;
				if ((exec = keybindings.get(e.getRawCode())) != null)
					exec.getAction().run();
			}
		}

		@Override
		public void nativeKeyReleased(NativeKeyEvent e) {
		}
	}
}
