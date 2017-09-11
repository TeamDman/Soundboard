import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class FormKeys {
	private final ArrayList<BindingButton> buttons = new ArrayList<>();
	private final JFrame  frame;
	private       JButton btnDone;
	private       JButton btnFocus;
	private       JButton btnNext;
	private       JButton btnPlay;
	private       JButton btnPrev;
	private       JButton btnRelay;
	private       JButton btnStop;
	private       JButton btnVolDown;
	private       JButton btnVolUp;
	private       JPanel  panel;
	private       JButton btnClear;

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
		new BindingButton(btnFocus, "Focus", Main.EnumKeyAction.FOCUS);
		new BindingButton(btnClear, "Clear", Main.EnumKeyAction.CLEAR);

		btnDone.addMouseListener((FormMain.ClickListener) (e) -> this.close());

		updateButtons();

		frame.setContentPane(panel);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);

		Point loc = Main.window.frame.getLocation();
		loc.translate(Main.window.frame.getWidth() / 2 - frame.getWidth() / 2, Main.window.frame.getHeight() / 2 - frame.getHeight() / 2);
		frame.setLocation(loc);
	}

	void updateButtons() {
		buttons.forEach(BindingButton::update);
	}

	void close() {
		frame.dispose();
	}

	private class BindingButton {
		final Main.EnumKeyAction action;
		final JButton            btn;
		final String             name;

		BindingButton(JButton btn, String name, Main.EnumKeyAction action) {
			this.btn = btn;
			this.name = name;
			this.action = action;
			buttons.add(this);
			btn.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON2) { // middle click
						action.setKey(0, "undefined");
					} else if (e.getButton() == MouseEvent.BUTTON1) {
						Main.setToBind(action);
					}
					updateButtons();
				}
			});
		}

		void update() {
			btn.setText(name + " [" + action.getKeyName() + "]");
		}
	}
}
