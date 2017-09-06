import org.jnativehook.keyboard.NativeKeyEvent;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class FormKeys {
	private JButton btnPlay;
	private JPanel panel;
	private JButton doneButton;

	private JFrame frame;

	public FormKeys() {
		frame = new JFrame("Keybindings");

		btnPlay.addMouseListener(new ClickBinderListener(Main.EnumKeyAction.PLAY));

		frame.setContentPane(panel);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	public void updateButtons() {
		btnPlay.setText("Play ["+ NativeKeyEvent.getKeyText(Main.EnumKeyAction.PLAY.getKey())+"]");
	}

	public void close() {
		frame.dispose();
	}

	private class ClickBinderListener implements MouseListener {
		Main.EnumKeyAction binding;
		public ClickBinderListener(Main.EnumKeyAction action) {
			this.binding=action;
		}

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

		@Override
		public void mouseClicked(MouseEvent e) {
			Main.setBinding(binding);
		}
	}


}
