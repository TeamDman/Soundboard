import javax.sound.sampled.*;
import javax.swing.*;
import java.io.File;

public class Main {
	public static Mixer.Info outputInfo;
	private static FormMain window;
	private static File picked;

	public static void main(String[] args) {
		window = new FormMain();
	}

	public static void play() {
		try {
			Clip clip = AudioSystem.getClip(outputInfo);
			clip.addLineListener(e -> {
				LineEvent.Type t = e.getType();
				if (t == LineEvent.Type.START) {
					window.setStatus("Playing");
				} else if (t == LineEvent.Type.STOP) {
					window.setStatus("Stopped");
				}
			});

			AudioInputStream stream = AudioSystem.getAudioInputStream(picked);
			clip.open(stream);
			clip.start();
		} catch (Exception e) {
			window.setStatus(e.getLocalizedMessage());
		}
	}

	public static void setPicked(File f) {
		picked = f;
	}

	public static void setOutput(Mixer.Info info) {
		outputInfo = info;
	}

	public static void pick() {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("D:/Applications/SLAM/csgo"));
		chooser.showOpenDialog(null);
		picked = chooser.getSelectedFile();
		if (picked.exists())
			window.setPath(picked.getPath());
	}
}
