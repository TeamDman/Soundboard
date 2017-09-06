import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Main {
	public static Mixer.Info outputInfo;
	public static boolean allowParallelAudio = true;
	public static File startDir = new File("D:/Applications/SLAM/sounds");
	private static FormMain window;
	private static File playing;
	private static ArrayList<File> sounds = new ArrayList<>();
	private static ArrayList<Clip> playingClips = new ArrayList<>();
	private static MicManager.ThreadMic threadMic;

	public static void main(String[] args) {
		window = new FormMain();
		window.updateFromDir(startDir);
	}

	public static void play() {
		if (playing == null || !playing.exists()) {
			window.setStatus("No sound selected");
			return;
		}
		if (!allowParallelAudio)
			stop();
		try {
			if (playing.getName().replaceAll("^.*\\.(.*)$", "$1").equals("wav")) {
				playClip(AudioSystem.getAudioInputStream(playing));
			} else {
				System.out.println("Not WAV, converting and playing");
//				new SoundThread(playing).start();
				AudioInputStream stream = AudioSystem.getAudioInputStream(playing);
				AudioInputStream decodedStream;
				AudioFormat formatBase = stream.getFormat();
				AudioFormat format = new AudioFormat(44100,16,2,true,true);
//								AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
//										formatBase.getSampleRate(),
//										16,
//										formatBase.getChannels(),
//										formatBase.getChannels() * 2,
//										formatBase.getSampleRate(),
//										false);

				decodedStream = AudioSystem.getAudioInputStream(format, stream);
				playClip(decodedStream);
			}
		} catch (Exception e) {
			System.out.println("Exception occurred");
			e.printStackTrace();
			window.setStatus(e.getLocalizedMessage());
		}
	}

	public static void stop() {
		for (Clip clip : playingClips) {
			clip.stop();
		}
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

	private static void playClip(AudioInputStream stream) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
		Clip clip = AudioSystem.getClip(outputInfo);
		clip.addLineListener(e -> {
			LineEvent.Type t = e.getType();
			if (t == LineEvent.Type.START) {
				window.setStatus("Playing");
			} else if (t == LineEvent.Type.STOP) {
				playingClips.remove(clip);
				if (playingClips.size() == 0)
					window.setStatus("Stopped");
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

	public static void setPlaying(File playing) {
		Main.playing = playing;
	}

	public static boolean isRelaying() {
		return threadMic == null;
	}

}
