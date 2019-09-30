import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class FormMain {
	public final  JFrame                frame;
	private final SoundTreeModel        treeSoundModel;
	private       JButton               btnCfg;
	private       JButton               btnClear;
	private       JButton               btnPicker;
	private       JButton               btnPlay;
	private       JButton               btnRelay;
	private       JButton               btnStop;
	private       JCheckBox             chkAutoRelay;
	private       JCheckBox             chkOnTop;
	private       JCheckBox             chkParallel;
	private       JComboBox<Mixer.Info> comboCable;
	private       JComboBox<Mixer.Info> comboMic;
	private       JComboBox<Mixer.Info> comboSpeakers;
	private boolean hasFocus = false;
	private JLabel               lblStatus;
	private JList<Main.ClipData> listPlaying;
	private JPanel               panel;
	private JSlider              sliderVol;
	private JSplitPane           splitPane;
	private JTree                treeSounds;
	private JTextField           txtFilter;
	private JTextField           txtPath;

	public FormMain() {
		frame = new JFrame("Soundboard");

		for (Mixer.Info v : AudioSystem.getMixerInfo()) {
			comboCable.addItem(v);
			comboSpeakers.addItem(v);
			comboMic.addItem(v);
		}

		sliderVol.setValue((int) (Main.getGain() * 100));
		treeSounds.setModel(treeSoundModel = new SoundTreeModel());
		treeSounds.updateUI();
		chkOnTop.setSelected(PreferenceManager.alwaysOnTop);
		chkAutoRelay.setSelected(PreferenceManager.autoRelay);

		if (PreferenceManager.autoRelay)
			Main.toggleRelay();
		updateCombos();
		updateRelay();
		updateFromDir(Main.currentDir);

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
		comboMic.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				Main.setInfoMic((Mixer.Info) e.getItem());
			}
		});

		btnPlay.addMouseListener((ClickListener) (e) -> Main.EnumKeyAction.PLAY.getAction().run());
		btnStop.addMouseListener((ClickListener) (e) -> Main.EnumKeyAction.STOP.getAction().run());
		btnClear.addMouseListener((ClickListener) (e) -> Main.EnumKeyAction.CLEAR.getAction().run());
		btnRelay.addMouseListener((ClickListener) (e) -> Main.EnumKeyAction.RELAY.getAction().run());
		btnPicker.addMouseListener((ClickListener) (e) -> pick());
		btnCfg.addMouseListener((ClickListener) (e) -> Main.windowKeys = new FormKeys());

		chkParallel.addActionListener(e -> Main.allowParallelAudio = chkParallel.isSelected());
		chkOnTop.addActionListener(e -> frame.setAlwaysOnTop(chkOnTop.isSelected()));
		chkAutoRelay.addActionListener(e -> {
			PreferenceManager.autoRelay = chkAutoRelay.isSelected();
			PreferenceManager.save();
		});
		sliderVol.addChangeListener(e -> {
			Main.updateGains(sliderVol.getValue() / 100.0f);
			PreferenceManager.save();

		});
		treeSounds.addMouseListener((ClickListener) this::checkTreeRightClick);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				PreferenceManager.save();
			}
		});
		frame.addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowGainedFocus(WindowEvent e) {
				hasFocus = true;
			}

			@Override
			public void windowLostFocus(WindowEvent e) {
				hasFocus = false;
			}
		});

		txtFilter.addActionListener((e) -> updateFilter());
		txtFilter.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateFilter();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateFilter();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateFilter();
			}
		});
		listPlaying.addMouseListener((ClickListener) this::checkListRightClick);
		splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, (e) -> PreferenceManager.save());

		//noinspection MagicConstant
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(panel);
		frame.setLocation(PreferenceManager.windowX, PreferenceManager.windowY);
		frame.pack();
		if (PreferenceManager.windowW != -1 && PreferenceManager.windowW != -1) {
			frame.setSize(PreferenceManager.windowW, PreferenceManager.windowH);
		}
		frame.setVisible(true);
		if (PreferenceManager.dividerX == -1)
			splitPane.setDividerLocation(0.5);
		else
			splitPane.setDividerLocation(PreferenceManager.dividerX);
	}

	private void pick() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setCurrentDirectory(Main.currentDir);
		if (chooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
			Main.currentDir = chooser.getSelectedFile();
			updateFromDir(Main.currentDir);
		}
		PreferenceManager.save();
	}

	private void checkTreeRightClick(MouseEvent e) {
		btnPlay.setText("Play [" + getSelectedFiles().size() + "]");
		if (e.getButton() == MouseEvent.BUTTON3) {
			TreePath path = treeSounds.getPathForLocation(e.getPoint().x, e.getPoint().y);
			if (path != null) {
				File f = (File) ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
				if (getSelectedFiles().contains(f))
					new FormRename(f);
			}
		}
	}

	List<File> getSelectedFiles() {
		List<File> rtn = new ArrayList<>();
		for (TreePath path : treeSounds.getSelectionModel().getSelectionPaths()) {
			DefaultMutableTreeNode selected = (DefaultMutableTreeNode) path.getLastPathComponent();
			rtn.add((File) selected.getUserObject());
		}
		return rtn;
	}

	private void checkListRightClick(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			Main.stop(listPlaying.getSelectedValuesList());
			updatePlaying();
			//Cleanup handled by closelistener
		}
	}

	void updatePlaying() {
		EventQueue.invokeLater(() ->
				//				listPlaying.setListData(Main.getPlaying().stream()
				//						.filter(e -> e.info == Main.getInfoCable())
				//						.toArray(Main.ClipData[]::new)
				//				)
				listPlaying.setListData(Main.getPlaying().toArray(new Main.ClipData[Main.getPlaying().size()]))
		);
	}

	private void updateFromDir(File dir) {
		if (dir == null || !dir.exists()) {
			updateStatus("Directory contains no files!");
			return;
		}
		updateFromFiles(dir.listFiles());
		EventQueue.invokeLater(() -> txtPath.setText(dir.getPath()));
	}

	private void updateFromFiles(File[] files) {
		EventQueue.invokeLater(() -> {
			SoundTreeModel.rebuild(files);
			if (SoundTreeModel.getRootNode().children().hasMoreElements())
				treeSounds.setSelectionPath(new TreePath(treeSoundModel.getPathToRoot(SoundTreeModel.getNodes().get(0))));
			treeSounds.updateUI();
		});
	}

	void updateStatus(String txt) {
		System.out.println(txt);
		EventQueue.invokeLater(() -> lblStatus.setText(txt));
	}

	void updateCombos() {
		comboCable.setSelectedItem(Main.getInfoCable());
		comboSpeakers.setSelectedItem(Main.getInfoSpeakers());
		comboMic.setSelectedItem(Main.getInfoMic());
	}

	void updateRelay() {
		comboSpeakers.setEnabled(!Main.isRelaying());
		comboCable.setEnabled(!Main.isRelaying());
		comboMic.setEnabled(!Main.isRelaying());
		updateButtons();
	}

	void updateButtons() {
		EventQueue.invokeLater(() -> {
			btnStop.setText(Main.allowResume ? "Resume" : "Stop");
			btnRelay.setText("Relay " + (Main.isRelaying() ? "stop" : "start"));
		});
	}

	void updateFilter() {
		if (txtFilter.getText().length() == 0) {
			updateFromDir(Main.currentDir);
		} else {
			File[] files = SoundTreeModel.getFiles();
			if (SoundTreeModel.files.length == 0)
				files = Main.currentDir.listFiles();

			Pattern p = Pattern.compile(txtFilter.getText(), Pattern.CASE_INSENSITIVE);

			ArrayList<File> refined = new ArrayList<>();

			for (File f : files) {
				Matcher m = p.matcher(f.getName());
				if (m.find())
					refined.add(f);
			}
			updateFromFiles(refined.toArray(new File[0]));
		}

	}

	void focus() {
		System.out.println(hasFocus);
		if (hasFocus) {
			txtFilter.requestFocus();
		} else {
			Main.receivingFilterInput = true;
		}
	}

	void addInput(String c) {
		if (c.equals("Backspace"))
			txtFilter.setText(txtFilter.getText().substring(0, txtFilter.getText().length() - 1));
		else if (c.equals("Space"))
			txtFilter.setText(txtFilter.getText() + " ");
		else if (c.length() > 1)
			System.out.println("Fake focus typing attempted to add long string '" + c + "'");
		else
			txtFilter.setText(txtFilter.getText() + c);
		txtFilter.setCaretPosition(txtFilter.getText().length());
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

	int getDividerX() {
		return splitPane.getDividerLocation();
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

	interface ClickListener extends MouseListener {
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

	static class SoundTreeModel extends DefaultTreeModel {
		private static final ArrayList<DefaultMutableTreeNode> nodes = new ArrayList<>();
		private static final DefaultMutableTreeNode            root  = new DefaultMutableTreeNode("Sounds");
		private static File[] files;

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
			SoundTreeModel.files = files;
			root.removeAllChildren();
			nodes.clear();

			for (File f : files) {
				add(f, root);
			}
		}

		private static void add(File file, DefaultMutableTreeNode node) {
			DefaultMutableTreeNode nodeNew = new DefaultMutableTreeNode(file);
			nodes.add(nodeNew);
			if (file.isDirectory()) {
				for (File f : file.listFiles())
					add(f, nodeNew);
			}
			node.add(nodeNew);
		}

		static File[] getFiles() {
			return files;
		}
	}

}
