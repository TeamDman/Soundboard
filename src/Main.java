import javax.sound.sampled.*;
import java.io.File;
import java.util.ArrayList;

public class Main {
	public static Mixer.Info outputInfo;
	public static boolean allowParallelAudio = true;

	private static FormMain window;
	private static File playing;

	private static ArrayList<File> sounds = new ArrayList<>();
	private static ArrayList<Clip> playingClips = new ArrayList<>();

	public static void main(String[] args) {
		window = new FormMain();
	}

	public static void play() {
		if (playing == null || !playing.exists()){
			window.setStatus("No sound selected");
			return;
		}
		if (!allowParallelAudio)
			stop();
		try {
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

			AudioInputStream stream = AudioSystem.getAudioInputStream(playing);


			clip.open(stream);
			clip.start();
			playingClips.add(clip);
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
}
