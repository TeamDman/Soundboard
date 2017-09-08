import org.jnativehook.keyboard.NativeKeyEvent;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

public class FormKeys {
	public JFrame frame;
	private JButton btnPlay;
	private JPanel panel;
	private JButton btnDone;
	private JButton btnStop;
	private JButton btnVolUp;
	private JButton btnVolDown;
	private JButton btnNext;
	private JButton btnPrev;
	private ArrayList<BindingButton> buttons = new ArrayList<>();

	public FormKeys() {
		frame = new JFrame("Keybindings");


		new BindingButton(btnPlay, "Play", Main.EnumKeyAction.PLAY);
		new BindingButton(btnStop, "Stop", Main.EnumKeyAction.STOP);
		new BindingButton(btnVolUp, "VolUp", Main.EnumKeyAction.VOLUP);
		new BindingButton(btnVolDown, "VolDown", Main.EnumKeyAction.VOLDOWN);
		new BindingButton(btnNext, "Next", Main.EnumKeyAction.NEXT);
		new BindingButton(btnPrev, "Prev", Main.EnumKeyAction.PREV);


		btnDone.addMouseListener(new FormMain.ClickListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				close();
			}
		});

		frame.setContentPane(panel);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		frame.setLocation(Main.window.frame.getLocation());
	}

	public void updateButtons() {
		buttons.forEach(BindingButton::update);
	}

	public void close() {
		frame.dispose();
	}

	private class BindingButton {
		JButton btn;
		String name;
		Main.EnumKeyAction action;

		public BindingButton(JButton btn, String name, Main.EnumKeyAction action) {
			this.btn = btn;
			this.name = name;
			this.action = action;
			buttons.add(this);
			btn.setText(name + " [undefined]");
			btn.addMouseListener(new ClickBinderListener(action));
		}

		public void update() {
			btn.setText(name + "[" + NativeKeyEvent.getKeyText(action.getKey()) + "]");
		}
	}

	private class ClickBinderListener implements MouseListener {
		Main.EnumKeyAction binding;

		public ClickBinderListener(Main.EnumKeyAction action) {
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
