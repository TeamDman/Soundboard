import javax.sound.sampled.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
	public static Mixer.Info infoCable;
	public static Mixer.Info infoSpeakers;
	public static boolean allowParallelAudio = true;
	public static File startDir = new File("D:/Applications/SLAM/sounds");
	private static FormMain window;
	private static ArrayList<File> sounds = new ArrayList<>();
	private static ArrayList<Clip> playingClips = new ArrayList<>();
	private static MicManager.ThreadMic threadMic;
	private static float gainMod = 1.0f;
	private static ArrayList<FloatControl> gains = new ArrayList<>();

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

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
		for (FloatControl gain : gains){
			gain.setValue(((gain.getMaximum()-gain.getMinimum())*gainMod)+gain.getMinimum());
		}
	}

	public static boolean isRelaying() {
		return threadMic == null;
	}

}
