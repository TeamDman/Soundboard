import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FormMain {
	public JFrame frame;
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
	private SoundTreeModel treeSoundModel;
	private int debounce = 3;

	public FormMain() {
		frame = new JFrame("Soundboard");
		comboCable.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED && --debounce < 0) {
				Main.setInfoCable((Mixer.Info) e.getItem());
			}
		});
		comboSpeakers.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED && --debounce < 0) {
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
				toggleRelay();
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
				if (Main.windowKeys != null) {
					Main.windowKeys.close();
				}
				Main.windowKeys = new FormKeys();
			}
		});
		chkParallel.addActionListener(e -> Main.allowParallelAudio = chkParallel.isSelected());

		sliderVol.addChangeListener(e -> {
			if (--debounce < 0)
				Main.setGain(sliderVol.getValue() / 100.0f);
		});

		Mixer.Info[] infos = AudioSystem.getMixerInfo();
		for (Mixer.Info v : infos) {
			comboCable.addItem(v);
			comboSpeakers.addItem(v);
		}
		updateCombos();

		sliderVol.setValue((int) (Main.getGain() * 100));

		treeSounds.setModel(treeSoundModel = new SoundTreeModel());
		treeSounds.updateUI();


		frame.setContentPane(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	public void toggleRelay() {
		btnRelay.setText("Relay " + (Main.isRelaying() ? "stop" : "start"));
		comboCable.setEnabled(!Main.isRelaying());
		Main.toggleRelay();
	}

	private void pick() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setCurrentDirectory(Main.startDir);
		if (chooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
			updateFromDir(chooser.getSelectedFile());
		}
		Main.startDir = chooser.getSelectedFile();
		PreferenceManager.save();
	}

	public void updateFromDir(File dir) {
		if (dir == null || !dir.exists()) {
			setStatus("Directory contains no files!");
			return;
		}
		Main.addFiles(dir.listFiles());

		SwingUtilities.invokeLater(() -> {
			SoundTreeModel.rebuild(dir.listFiles());
			if (SoundTreeModel.getRootNode().getFirstChild() != null)
				treeSounds.setSelectionPath(new TreePath(treeSoundModel.getPathToRoot(SoundTreeModel.getNodes().get(0))));
			txtPath.setText(dir.getPath());
		});
	}

	public void updateVol() {
		sliderVol.setValue((int) (Main.getGain() * 100));
	}

	public void updateCombos() {
		debounce=2;
		comboCable.setSelectedItem(Main.getInfoCable());
		comboSpeakers.setSelectedItem(Main.getInfoSpeakers());
		debounce=0;
	}

	public void updateNext() {
		if (Main.isKeyControlDown) {
			TreePath[] paths = SoundTreeModel.getNodes().stream()
					.map(e -> new TreePath(treeSoundModel.getPathToRoot(e)))
					.toArray(TreePath[]::new);
			if (treeSounds.getSelectionPaths() == null || treeSounds.getSelectionPaths().length == 0) {
				treeSounds.setSelectionPath(paths[0]);
			}
			TreePath[] selected = treeSounds.getSelectionPaths();
			TreePath[] replace = new TreePath[selected.length];
			for (int i = 0; i < selected.length; i++) {
				for (int v = 0; v < paths.length; v++) {
					if (selected[i].equals(paths[v]) && v + 1 < paths.length)
						replace[i] = paths[v + 1];
				}
			}
			treeSounds.setSelectionPaths(replace);
			SwingUtilities.invokeLater(() -> treeSounds.scrollPathToVisible(replace[0]));
		}
	}

	public void updatePrev() {
		if (Main.isKeyControlDown) {
			TreePath[] paths = SoundTreeModel.getNodes().stream()
					.map(e -> new TreePath(treeSoundModel.getPathToRoot(e)))
					.toArray(TreePath[]::new);
			if (treeSounds.getSelectionPaths() == null || treeSounds.getSelectionPaths().length == 0) {
				treeSounds.setSelectionPath(paths[0]);
			}
			TreePath[] selected = treeSounds.getSelectionPaths();
			TreePath[] replace = new TreePath[selected.length];
			for (int i = 0; i < selected.length; i++) {
				for (int v = 0; v < paths.length; v++) {
					if (selected[i].equals(paths[v]) && v > 0)
						replace[i] = paths[v - 1];
				}
			}
			treeSounds.setSelectionPaths(replace);
			SwingUtilities.invokeLater(() -> treeSounds.scrollPathToVisible(replace[0]));
		}
	}

	public List<File> getSelectedFiles() {
		List<File> rtn = new ArrayList<>();
		for (TreePath path : treeSounds.getSelectionModel().getSelectionPaths()) {
			DefaultMutableTreeNode selected = (DefaultMutableTreeNode) path.getLastPathComponent();
			rtn.add((File) selected.getUserObject());
		}

		return rtn;
	}

	public void setStatus(String txt) {
		System.out.println(txt);
		lblStatus.setText(txt);
	}

	public static abstract class ClickListener implements MouseListener {
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
		private static ArrayList<DefaultMutableTreeNode> nodes = new ArrayList<>();

		public SoundTreeModel() {
			super(root);
		}

		public static void rebuild(File[] files) {
			root.removeAllChildren();
			nodes.clear();
			for (File f : files) {
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(f);
				nodes.add(node);
				root.add(node);

			}
		}

		public static ArrayList<DefaultMutableTreeNode> getNodes() {
			return nodes;
		}

		public static DefaultMutableTreeNode getRootNode() {
			return root;
		}
	}

}
