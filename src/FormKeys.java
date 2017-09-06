import javax.swing.*;

public class FormKeys {
	private JButton playButton;
	private JPanel panel;

	private JFrame frame;

	public FormKeys() {
		frame = new JFrame("Keybindings");

		frame.setContentPane(panel);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
}
