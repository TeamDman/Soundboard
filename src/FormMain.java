import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FormMain {
	private JComboBox comboCable;
	private JLabel lblStatus;
	private JButton btnPlay;
	private JPanel panel;
	private JTextField txtPath;
	private JButton btnPicker;
	private JButton btnRelay;
//	private JList<File> listSounds;
	private JCheckBox chkParallel;
	private JButton btnStop;
	private JSlider sliderVol;
	private JComboBox comboSpeakers;
	private JTree treeSounds;
	private JButton btnCfg;
	private JFrame frame;
	private SoundTreeModel treeSoundModel;

	public FormMain() {
		frame = new JFrame("Soundboard");
		comboCable.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				Main.setInfoCable((Mixer.Info) e.getItem());
			}
		});
		comboSpeakers.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				Main.setInfoSpeakers((Mixer.Info) e.getItem());
			}
		});

		btnPlay.addMouseListener(new ClickListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Main.play(getSelectedFiles());
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
				btnRelay.setText("Relay " + (Main.isRelaying() ? "stop" : "start"));
				comboCable.setEnabled(!Main.isRelaying());
				Main.toggleRelay();

			}
		});
		btnPicker.addMouseListener(new ClickListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				pick();
			}
		});
		btnCfg.addMouseListener(new ClickListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				new FormKeys();
			}
		});
		chkParallel.addActionListener(e -> Main.allowParallelAudio = chkParallel.isSelected());

		sliderVol.addChangeListener(e -> {
			Main.setGain(sliderVol.getValue() / 100.0f);
		});

		Mixer.Info[] infos = AudioSystem.getMixerInfo();
		Main.infoCable = infos[0];
		Main.infoSpeakers = infos[0];
		for (Mixer.Info v : infos) {
			comboCable.addItem(v);
			comboSpeakers.addItem(v);
		}

		Main.setGain(sliderVol.getValue() / 100.0f);
		treeSounds.setModel(treeSoundModel = new SoundTreeModel());


		frame.setContentPane(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	private void pick() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setCurrentDirectory(Main.startDir);
		if (chooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
			updateFromDir(chooser.getSelectedFile());
		}
	}

	public void updateFromDir(File dir) {
		if (dir == null || !dir.exists()) {
			setStatus("Directory contains no files!");
			return;
		}
		Main.addFiles(dir.listFiles());

		SwingUtilities.invokeLater(() -> {
			SoundTreeModel.rebuild(dir.listFiles());
			txtPath.setText(dir.getPath());
		});
	}

	private List<File> getSelectedFiles() {
//		listSounds.getSelectedValuesList()
//		return new ArrayList<>();
		List<File> rtn = new ArrayList<>();
//		treeSounds.getSelectionPaths();
		for (TreePath path : treeSounds.getSelectionModel().getSelectionPaths()){
			DefaultMutableTreeNode selected = (DefaultMutableTreeNode) path.getLastPathComponent();
			rtn.add((File) selected.getUserObject());
		}

		return rtn;
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

	private static class SoundTreeModel extends DefaultTreeModel {
		private static DefaultMutableTreeNode root = new DefaultMutableTreeNode("Sounds");
		public SoundTreeModel() {
			super(root);
		}

		public static void rebuild(File[] files) {
			root.removeAllChildren();
			for (File f : files) {
				root.add(new DefaultMutableTreeNode(f));
			}
		}
	}

}
