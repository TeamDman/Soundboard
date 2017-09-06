import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
	public static Mixer.Info outputInfo;
	public static boolean allowParallelAudio = true;
	public static File startDir = new File("D:/Applications/SLAM/sounds");
	private static FormMain window;
	private static ArrayList<File> sounds = new ArrayList<>();
	private static ArrayList<Clip> playingClips = new ArrayList<>();
	private static MicManager.ThreadMic threadMic;

	public static void main(String[] args) {
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
						playClip(AudioSystem.getAudioInputStream(file), file.getName());
					} else {
						System.out.println("Not WAV, converting and playing");
						AudioInputStream stream = AudioSystem.getAudioInputStream(file);
						AudioInputStream decodedStream;
						AudioFormat format = new AudioFormat(44100, 16, 2, true, true);
						//				AudioFormat formatBase = stream.getFormat();
						//				AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
						//										formatBase.getSampleRate(),
						//										16,
						//										formatBase.getChannels(),
						//										formatBase.getChannels() * 2,
						//										formatBase.getSampleRate(),
						//										false);
						decodedStream = AudioSystem.getAudioInputStream(format, stream);
						playClip(decodedStream, file.getName());
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
		//		new Thread(() -> {
		for (Clip clip : playingClips) {
			clip.stop();
		}
		//		}).start();
		playingClips.clear();
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

	private static void playClip(AudioInputStream stream, String name) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
		Clip clip = AudioSystem.getClip(outputInfo);
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
		clip.start();
		playingClips.add(clip);
	}

	public static void addFiles(File[] f) {
		for (File file : f) {
			sounds.add(file);
		}
	}

	public static void setOutput(Mixer.Info info) {
		outputInfo = info;
	}

	public static boolean isRelaying() {
		return threadMic == null;
	}

}
