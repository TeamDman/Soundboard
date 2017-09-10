import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FormMain {
	public final  JFrame                frame;
	private final SoundTreeModel        treeSoundModel;
	private       JButton               btnCfg;
	private       JButton               btnPicker;
	private       JButton               btnPlay;
	private       JButton               btnRelay;
	private       JButton               btnStop;
	private       JCheckBox             chkAutoRelay;
	private       JCheckBox             chkOnTop;
	private       JCheckBox             chkParallel;
	private       JComboBox<Mixer.Info> comboCable;
	private       JComboBox<Mixer.Info> comboSpeakers;
	private int debounce = 3;
	private JLabel     lblStatus;
	private JPanel     panel;
	private JSlider    sliderVol;
	private JTree      treeSounds;
	private JTextField txtPath;

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

		btnPlay.addMouseListener((ClickListener) (e) -> Main.play(getSelectedFiles()));
		btnStop.addMouseListener((ClickListener) (e) -> Main.stop());
		btnRelay.addMouseListener((ClickListener) (e) -> updateRelay());
		btnPicker.addMouseListener((ClickListener) (e) -> pick());
		btnCfg.addMouseListener((ClickListener) (e) -> Main.windowKeys = new FormKeys());

		chkParallel.addActionListener(e -> Main.allowParallelAudio = chkParallel.isSelected());
		chkOnTop.addActionListener(e -> frame.setAlwaysOnTop(chkOnTop.isSelected()));
		chkAutoRelay.addActionListener(e -> {
			PreferenceManager.autoRelay = chkAutoRelay.isSelected();
			PreferenceManager.save();
		});
		sliderVol.addChangeListener(e -> {
			if (--debounce < 0)
				Main.setGain(sliderVol.getValue() / 100.0f);
		});
		treeSounds.addMouseListener((ClickListener)(e) -> checkRightClick(e));
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				PreferenceManager.save();
			}
		});

		for (Mixer.Info v : AudioSystem.getMixerInfo()) {
			comboCable.addItem(v);
			comboSpeakers.addItem(v);
		}
		updateCombos();

		sliderVol.setValue((int) (Main.getGain() * 100));
		treeSounds.setModel(treeSoundModel = new SoundTreeModel());
		treeSounds.updateUI();
		chkOnTop.setSelected(PreferenceManager.alwaysOnTop);
		chkAutoRelay.setSelected(PreferenceManager.autoRelay);

		if (PreferenceManager.autoRelay) {
			updateRelay();
		}

		//noinspection MagicConstant
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(panel);
		frame.setLocation(PreferenceManager.windowX, PreferenceManager.windowY);
		frame.pack();
		if (PreferenceManager.windowW != -1 && PreferenceManager.windowW != -1)
			frame.setSize(PreferenceManager.windowW, PreferenceManager.windowH);
		frame.setVisible(true);
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

	private void checkRightClick(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			TreePath path = treeSounds.getPathForLocation(e.getPoint().x, e.getPoint().y);
			if (path != null) {
				File f = (File) ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
				if (getSelectedFiles().contains(f))
					new FormRename(f);
			}
		}
	}

	void updateFromDir(File dir) {
		if (dir == null || !dir.exists()) {
			updateStatus("Directory contains no files!");
			return;
		}
		Main.addFiles(dir.listFiles());

		SwingUtilities.invokeLater(() -> {
			SoundTreeModel.rebuild(dir.listFiles());
			if (SoundTreeModel.getRootNode().getFirstChild() != null)
				treeSounds.setSelectionPath(new TreePath(treeSoundModel.getPathToRoot(SoundTreeModel.getNodes().get(0))));
			txtPath.setText(dir.getPath());
			treeSounds.updateUI();
		});
	}

	void updateStatus(String txt) {
		System.out.println(txt);
		lblStatus.setText(txt);
	}

	void updateCombos() {
		debounce = 2;
		comboCable.setSelectedItem(Main.getInfoCable());
		comboSpeakers.setSelectedItem(Main.getInfoSpeakers());
		debounce = 0;
	}

	void updateRelay() {
		Main.toggleRelay();
		btnRelay.setText("Relay " + (Main.isRelaying() ? "stop" : "start"));
		comboCable.setEnabled(!Main.isRelaying());
	}

	List<File> getSelectedFiles() {
		List<File> rtn = new ArrayList<>();
		for (TreePath path : treeSounds.getSelectionModel().getSelectionPaths()) {
			DefaultMutableTreeNode selected = (DefaultMutableTreeNode) path.getLastPathComponent();
			rtn.add((File) selected.getUserObject());
		}
		return rtn;
	}

	void focus() {
		EventQueue.invokeLater(() -> {
			frame.setExtendedState(JFrame.NORMAL);
			treeSounds.requestFocus();
		});
	}

	int getHeight() {
		return frame.getHeight();
	}

	int getWidth() {
		return frame.getWidth();
	}

	int getX() {
		return frame.getX();
	}

	int getY() {
		return frame.getY();
	}

	void updateNext() {
		TreePath[] paths = SoundTreeModel.getNodes().stream()
				.map(e -> new TreePath(treeSoundModel.getPathToRoot(e)))
				.toArray(TreePath[]::new);
		if (treeSounds.getSelectionPaths() == null || treeSounds.getSelectionPaths().length == 0) {
			treeSounds.setSelectionPath(paths[0]);
		}
		TreePath[] selected = treeSounds.getSelectionPaths();
		TreePath[] replace  = new TreePath[selected.length];
		for (int i = 0; i < selected.length; i++) {
			for (int v = 0; v < paths.length; v++) {
				if (selected[i].equals(paths[v]) && v + 1 < paths.length)
					replace[i] = paths[v + 1];
			}
		}
		treeSounds.setSelectionPaths(replace);
		SwingUtilities.invokeLater(() -> treeSounds.scrollPathToVisible(replace[0]));

	}

	void updatePrev() {
		TreePath[] paths = SoundTreeModel.getNodes().stream()
				.map(e -> new TreePath(treeSoundModel.getPathToRoot(e)))
				.toArray(TreePath[]::new);
		if (treeSounds.getSelectionPaths() == null || treeSounds.getSelectionPaths().length == 0) {
			treeSounds.setSelectionPath(paths[0]);
		}
		TreePath[] selected = treeSounds.getSelectionPaths();
		TreePath[] replace  = new TreePath[selected.length];
		for (int i = 0; i < selected.length; i++) {
			for (int v = 0; v < paths.length; v++) {
				if (selected[i].equals(paths[v]) && v > 0)
					replace[i] = paths[v - 1];
			}
		}
		treeSounds.setSelectionPaths(replace);
		SwingUtilities.invokeLater(() -> treeSounds.scrollPathToVisible(replace[0]));
	}

	void updateVol() {
		sliderVol.setValue((int) (Main.getGain() * 100));
	}

	public interface ClickListener extends MouseListener {
		@Override
		default void mousePressed(MouseEvent e) {
		}

		@Override
		default void mouseReleased(MouseEvent e) {
		}

		@Override
		default void mouseEntered(MouseEvent e) {
		}

		@Override
		default void mouseExited(MouseEvent e) {
		}
	}

	private static class SoundTreeModel extends DefaultTreeModel {
		private static final ArrayList<DefaultMutableTreeNode> nodes = new ArrayList<>();
		private static final DefaultMutableTreeNode            root  = new DefaultMutableTreeNode("Sounds");

		SoundTreeModel() {
			super(root);
		}

		static ArrayList<DefaultMutableTreeNode> getNodes() {
			return nodes;
		}

		static DefaultMutableTreeNode getRootNode() {
			return root;
		}

		static void rebuild(File[] files) {
			root.removeAllChildren();
			nodes.clear();
			for (File f : files) {
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(f);
				nodes.add(node);
				root.add(node);
			}
		}
	}

}
