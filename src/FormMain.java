import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

public class FormMain {
	private JComboBox comboAudio;
	private JLabel lblStatus;
	private JButton btnPlay;
	private JPanel panel;
	private JTextField txtPath;
	private JButton btnPicker;
	private JButton btnRelay;
	private JList<File> listSounds;
	private JCheckBox chkParallel;
	private JButton btnStop;
	private JFrame frame;

	public FormMain() {
		frame = new JFrame("Soundboard");

		Mixer.Info[] infos = AudioSystem.getMixerInfo();
		Main.outputInfo = infos[0];
		for (Mixer.Info v : infos)
			comboAudio.addItem(v);

		comboAudio.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				Main.setOutput((Mixer.Info) e.getItem());
			}
		});

		btnPlay.addMouseListener(new ClickListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Main.play();
			}
		});
		btnStop.addMouseListener(new ClickListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Main.stop();
			}
		});
		btnRelay.addMouseListener(new ClickListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				MicManager.init();
			}
		});
		btnPicker.addMouseListener(new ClickListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				pick();
			}
		});
		chkParallel.addActionListener(e -> Main.allowParallelAudio = chkParallel.isSelected());
		listSounds.addListSelectionListener(e -> {
			if (e.getValueIsAdjusting()) {
				Main.setPlaying(listSounds.getSelectedValue());
			}
		});

		frame.setContentPane(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	private void pick() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setCurrentDirectory(new File("D:/Applications/SLAM/csgo"));
		if (chooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
			updateFromDir(chooser.getSelectedFile());
		}
	}

	private void updateFromDir(File dir) {
		if (dir == null || !dir.exists()) {
			setStatus("Directory contains no files!");
			return;
		}
		Main.addFiles(dir.listFiles());

		SwingUtilities.invokeLater(() -> {
			listSounds.setListData(dir.listFiles());
			txtPath.setText(dir.getPath());
		});
	}

	public void setStatus(String txt) {
		System.out.println(txt);
		lblStatus.setText(txt);
	}

	private static abstract class ClickListener implements MouseListener {
		@Override
		public void mousePressed(MouseEvent e) {

		}

		@Override
		public void mouseReleased(MouseEvent e) {

		}

		@Override
		public void mouseEntered(MouseEvent e) {

		}

		@Override
		public void mouseExited(MouseEvent e) {

		}
	}

}
