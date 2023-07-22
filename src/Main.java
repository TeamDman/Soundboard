import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
	private static final int                  allowResumeTimeout = 5;
	private static final ArrayList<ClipData>  playingClips       = new ArrayList<>();
	private static final ArrayList<TimerTask> tasksResumers      = new ArrayList<>();
	static               boolean              allowParallelAudio = true;
	static               boolean              allowResume        = false;
	static File currentDir;
	static boolean receivingFilterInput = false;
	static FormMain   window;
	static FormKeys   windowKeys;
	static FormRename windowRename;
	private static float gainMod = 1.0f;
	private static Mixer.Info           infoCable;
	private static Mixer.Info           infoMic;
	private static Mixer.Info           infoSpeakers;
	private static MicManager.ThreadMic threadMic;
	private static EnumKeyAction        toBind;
	static boolean ctrlDown = false;

	static {
		EnumKeyAction.PLAY.setAction(() -> Main.play(Main.window.getSelectedFiles()));
		EnumKeyAction.STOP.setAction(Main::stop);
		EnumKeyAction.VOLUP.setAction(Main::increaseGain);
		EnumKeyAction.VOLDOWN.setAction(Main::decreaseGain);
		EnumKeyAction.NEXT.setAction(Main::soundNext);
		EnumKeyAction.PREV.setAction(Main::soundPrev);
		EnumKeyAction.RELAY.setAction(Main::toggleRelay);
		EnumKeyAction.FOCUS.setAction(() -> window.focus());
		EnumKeyAction.CLEAR.setAction(() -> stop(playingClips));
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
		EventQueue.invokeLater(() -> window = new FormMain());

	}

	private static void play(List<File> listFiles) {
		new Thread(() -> {
			for (File file : listFiles) {
				if (file == null || !file.exists()) {
					window.updateStatus("No sound selected");
					return;
				}
				if (!allowParallelAudio) {
					allowResume = false;
					stop();
				}
				try {
					if (file.getName().replaceAll("^.*\\.(.*)$", "$1").equals("wav")) {
						playClip(AudioSystem.getAudioInputStream(file), infoCable, file.getName());
						playClip(AudioSystem.getAudioInputStream(file), infoSpeakers, file.getName());
					} else {
						System.out.println("Not WAV, converting and playing");
						AudioInputStream stream = AudioSystem.getAudioInputStream(file);
						AudioFormat      format = new AudioFormat(44100, 16, 2, true, true);
						playClip(AudioSystem.getAudioInputStream(format, stream), infoSpeakers, file.getName());
//
//						AudioInputStream streama = AudioSystem.getAudioInputStream(file);
//						AudioFormat      formata = new AudioFormat(44100, 16, 2, true, true);
//						playClip(AudioSystem.getAudioInputStream(formata, streama), infoCable, file.getName());

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
					System.out.println("Exception occurred playing clip");
					e.printStackTrace();
					window.updateStatus(e.getLocalizedMessage());
				}
			}
		}).start();
	}

	private static void stop() {
		for (ClipData data : playingClips) {
			if (allowResume) {
				data.clip.setMicrosecondPosition(data.time);
				data.clip.start();
			} else {
				data.time = data.clip.getMicrosecondPosition();
				data.clip.stop();
			}
		}
		if (allowResume) {
			allowResume = false;
			window.updateButtons();
		} else {
			allowResume = true;
			window.updateButtons();
			tasksResumers.forEach(TimerTask::cancel);
			tasksResumers.clear();
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					allowResume = false;
					window.updateButtons();
				}
			};
			tasksResumers.add(task);
			new Timer().schedule(task, allowResumeTimeout * 1000);
		}
	}

	private static void playClip(AudioInputStream stream, Mixer.Info info, String name) throws LineUnavailableException, IOException {
		Clip     clip = AudioSystem.getClip(info);
		ClipData data = new ClipData(clip, name, info);
		clip.addLineListener(e -> {
			LineEvent.Type t = e.getType();
			if (t == LineEvent.Type.START) {
				window.updateStatus("Playing: " + name);
			} else if (t == LineEvent.Type.STOP) {
				if (clip.getMicrosecondLength() == clip.getMicrosecondPosition())
					clip.close();
			} else if (t == LineEvent.Type.CLOSE) {
				playingClips.remove(data);
				window.updatePlaying();
				System.out.println("Removed clip from queue");
			}
		});
		clip.open(stream);
		playingClips.add(data);
		updateGains(gainMod);
		clip.start();
		window.updatePlaying();
	}

	static void updateGains(float v) {
		gainMod = v;
		for (ClipData data : playingClips) {
			FloatControl gain = (FloatControl) data.clip.getControl(FloatControl.Type.MASTER_GAIN);
			gain.setValue(((gain.getMaximum() - gain.getMinimum()) * gainMod) + gain.getMinimum());
		}
	}

	static void stop(List<ClipData> toStop) {
		ArrayList<Main.ClipData> toRemove = new ArrayList<>();
		toRemove.addAll(toStop);
		toRemove.forEach(data -> data.clip.close());
		Main.getPlaying().removeAll(toRemove);
	}

	static ArrayList<ClipData> getPlaying() {
		return playingClips;
	}

	static boolean isRelaying() {
		return threadMic != null;
	}

	static Mixer.Info getInfoCable() {
		return infoCable;
	}

	static void setInfoCable(Mixer.Info info) {
		boolean update = infoCable != null;
		infoCable = info;

		if (update)
			PreferenceManager.save();
		if (window != null)
			window.updateCombos();
	}

	static Mixer.Info getInfoSpeakers() {
		return infoSpeakers;
	}

	static void setInfoSpeakers(Mixer.Info info) {
		boolean update = infoCable != null;
		infoSpeakers = info;

		if (update)
			PreferenceManager.save();
		if (window != null)
			window.updateCombos();
	}

	static Mixer.Info getInfoMic() {
		return infoMic;
	}

	static void setInfoMic(Mixer.Info info) {
		boolean update = infoMic != null;
		infoMic = info;
		if (update)
			PreferenceManager.save();
		if (window != null)
			window.updateCombos();

	}

	static void setToBind(EnumKeyAction action) {
		toBind = action;
	}

	static float getGain() {
		return gainMod;
	}

	static void toggleRelay() {
		if (threadMic != null) {
			try {
				threadMic.running = false;
				threadMic.join();
				threadMic = null;
			} catch (Exception e) {
				System.out.println("Exception relaying mic");
				e.printStackTrace();
				window.updateStatus(e.getLocalizedMessage());
			}
		} else {
			threadMic = new MicManager.ThreadMic();
			threadMic.start();
		}
		if (window != null)
			EventQueue.invokeLater(window::updateRelay);
	}

	private static void soundNext() {
		window.updateNext();
	}

	private static void soundPrev() {
		window.updatePrev();
	}

	private static void increaseGain() {
		gainMod = Math.min(gainMod + 0.02f, 1);
		window.updateVol();
	}

	private static void decreaseGain() {
		gainMod = Math.max(gainMod - 0.02f, 0);
		window.updateVol();
	}


	enum EnumKeyAction {
		PLAY,
		STOP,
		VOLUP,
		VOLDOWN,
		NEXT,
		PREV,
		RELAY,
		FOCUS,
		CLEAR;

		private Runnable action;
		private int    key     = 0;
		private String keyName = "undefined";

		Runnable getAction() {
			return action;
		}

		void setAction(Runnable action) {
			this.action = action;
		}

		public int getKey() {
			return key;
		}

		public String getKeyName() {
			return keyName;
		}

		void setKey(int key, String keyName) {
			this.key = key;
			this.keyName = keyName;
		}
	}

	static class ClipData {
		final Clip       clip;
		final Mixer.Info info;
		final String     name;
		Long time = -1L;

		ClipData(Clip clip, String name, Mixer.Info info) {
			this.clip = clip;
			this.name = name;
			this.info = info;
		}

		@Override
		public String toString() {
			return (info.equals(Main.getInfoCable()) ? "[Speakers] " : "[Cable] ") + name;
		}
	}

	private static class GlobalKeyListener implements NativeKeyListener {
		@Override
		public void nativeKeyTyped(NativeKeyEvent e) {
		}

		public void nativeKeyPressed(NativeKeyEvent e) {
			if (e.getKeyCode() == NativeKeyEvent.VC_CONTROL)
				ctrlDown = true;
			if (toBind != null) {
				System.out.println("BINDING KEY");
				toBind.setKey(e.getRawCode(), NativeKeyEvent.getKeyText(e.getKeyCode()));
				toBind = null;
				PreferenceManager.save();
				windowKeys.updateButtons();
			} else {
				for (EnumKeyAction action : EnumKeyAction.values()) {
					if (action.getKey() == e.getRawCode()) {
						receivingFilterInput = false;
						action.getAction().run();
						return;
					}
				}
				if (e.getKeyCode() == NativeKeyEvent.VC_ENTER) {
					receivingFilterInput = false;
					window.updateFilter();
				}
				if (receivingFilterInput)
					window.addInput(NativeKeyEvent.getKeyText(e.getKeyCode()));
			}
		}

		@Override
		public void nativeKeyReleased(NativeKeyEvent e) {
			if (e.getKeyCode() == NativeKeyEvent.VC_CONTROL)
				ctrlDown = false;
		}
	}
}
