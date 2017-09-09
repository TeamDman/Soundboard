import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

public class FormKeys {
	private final ArrayList<BindingButton> buttons = new ArrayList<>();
	private final JFrame frame;
	private JButton btnDone;
	private JButton btnNext;
	private JButton btnPlay;
	private JButton btnPrev;
	private JButton btnRelay;
	private JButton btnStop;
	private JButton btnVolDown;
	private JButton btnVolUp;
	private JPanel panel;

	public FormKeys() {
		if (Main.windowKeys != null) {
			Main.windowKeys.close();
		}
		frame = new JFrame("Keybindings");

		new BindingButton(btnPlay, "Play", Main.EnumKeyAction.PLAY);
		new BindingButton(btnStop, "Stop", Main.EnumKeyAction.STOP);
		new BindingButton(btnVolUp, "VolUp", Main.EnumKeyAction.VOLUP);
		new BindingButton(btnVolDown, "VolDown", Main.EnumKeyAction.VOLDOWN);
		new BindingButton(btnNext, "Next", Main.EnumKeyAction.NEXT);
		new BindingButton(btnPrev, "Prev", Main.EnumKeyAction.PREV);
		new BindingButton(btnRelay, "Relay", Main.EnumKeyAction.RELAY);

		btnDone.addMouseListener(new FormMain.ClickListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				close();
			}
		});

		updateButtons();

		frame.setContentPane(panel);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);

		Point loc = Main.window.frame.getLocation();
		loc.translate(Main.window.frame.getWidth() / 2 - frame.getWidth() / 2, Main.window.frame.getHeight() / 2 - frame.getHeight() / 2);
		frame.setLocation(loc);
	}

	public void updateButtons() {
		buttons.forEach(BindingButton::update);
	}

	public void close() {
		frame.dispose();
	}

	private class BindingButton {
		final Main.EnumKeyAction action;
		final JButton btn;
		final String name;

		BindingButton(JButton btn, String name, Main.EnumKeyAction action) {
			this.btn = btn;
			this.name = name;
			this.action = action;
			buttons.add(this);
			btn.addMouseListener(new ClickBinderListener(action));
		}

		void update() {
			btn.setText(name + " [" + action.getKeyName() + "]");
		}
	}

	private class ClickBinderListener implements MouseListener {
		final Main.EnumKeyAction binding;

		ClickBinderListener(Main.EnumKeyAction action) {
			this.binding = action;
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
