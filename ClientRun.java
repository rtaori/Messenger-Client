import javax.swing.JOptionPane;

public class ClientRun {

	public static void main(final String[] args) {
		final String userip = JOptionPane
				.showInputDialog("Type in IPAddress to connect to.");
		final String name = JOptionPane.showInputDialog("What is your name?");
		final Client billie = new Client(userip, name);
		billie.run();
	}

}