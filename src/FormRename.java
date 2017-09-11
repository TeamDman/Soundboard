import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Files;

class FormRename {
	private final JFrame     frame;
	private final File       renaming;
	private       JButton    btnSubmit;
	private       JPanel     panel;
	private       JTextField txtName;

	public FormRename(File f) {
		if (Main.windowRename != null) {
			Main.windowRename.close();
		}
		this.renaming = f;
		Main.window.frame.setEnabled(false);

		frame = new JFrame("Rename");

		txtName.setText(f.getName());
		EventQueue.invokeLater(() -> txtName.setCaretPosition(f.getName().lastIndexOf(".")));

		btnSubmit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				finishRenaming();
			}
		});
		txtName.addActionListener((e) -> finishRenaming());
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				Main.window.frame.setEnabled(true);
			}
		});

		frame.setContentPane(panel);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);

		Point loc = Main.window.frame.getLocation();
		loc.translate(Main.window.frame.getWidth() / 2 - frame.getWidth() / 2, Main.window.frame.getHeight() / 2 - frame.getHeight() / 2);
		frame.setLocation(loc);
	}

	private void finishRenaming() {
		try {
			if (JOptionPane.showConfirmDialog(frame,"Are you sure you want to rename this file?","Confirm Rename", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				File newFile = new File(renaming.getParent(),txtName.getText());
				Files.move(renaming.toPath(),newFile.toPath());
			}
			close();
		} catch (Exception e) {
			Main.window.updateStatus(e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	private void close() {
		frame.dispose();
	}
}
