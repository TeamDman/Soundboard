import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
		btnRelay.addMouseListener(new ClickListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				MicManager.init();
			}
		});
		btnPicker.addMouseListener(new ClickListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Main.pick();
			}
		});
		txtPath.getDocument().addDocumentListener(new DocChangedListener() {
			public void changed(DocumentEvent e) {
				Main.setPicked(new File(txtPath.getText()));
			}
		});

		frame.setContentPane(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	public void setStatus(String txt) {
		lblStatus.setText(txt);
	}

	public void setPath(String p) {
		txtPath.setText(p);
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

	private static abstract class DocChangedListener implements DocumentListener {
		@Override
		public void insertUpdate(DocumentEvent e) {
			changed(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			changed(e);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			changed(e);
		}

		public abstract void changed(DocumentEvent e);

	}
}
