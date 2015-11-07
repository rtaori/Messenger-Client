package src;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Client extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final int PORT_NUMBER = 6789;

	private String name;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private String serverIP;
	private Socket connection;
	private ClientGUI graphics;

	public Client(final String host, final String name) {
		super("Messaging App (Client)!");

		serverIP = host;
		this.name = name + " - ";

		graphics = new ClientGUI(e -> this.sendMessage(e.getActionCommand()),
				e -> this.sendMessage(e.getSource()));
		graphics.makeVisible();
	}

	public void sendMessage(final Object message) {
		try {
			output.writeObject(message);
			output.flush();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void sendMessage(final String message) {
		try {
			output.writeObject(name + message);
			output.flush();
			graphics.resetText();
		} catch (final IOException e) {
			graphics.showMessage("\nError: cannot send message.");
		}
	}

	public void run() {
		try {
			this.connectToServer();
			this.setupStreams();
			this.whileChatting();
		} catch (final EOFException e) {
			this.sendMessage("Connection ended");
		} catch (final IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			this.closeStreams();
		}
	}

	private void closeStreams() {
		graphics.showMessage("\nClosing connection...");
		graphics.setTypable(false);
		try {
			output.close();
			input.close();
			connection.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void whileChatting() throws IOException, ClassNotFoundException {
		graphics.setTypable(true);
		final String message = "";
		do {
			final Object in = input.readObject();
			if (in instanceof String)
				this.readString(in);
			else if (in instanceof ImageIcon)
				this.readImage(in);
			else if (in instanceof byte[])
				this.readAudio(in);
		} while (!message.equals("SERVER - END"));
	}

	private void readString(final Object in) {
		final String message = (String) in;
		graphics.showMessage("\n" + message);
	}

	private void readImage(final Object in) {
		final JLabel img = new JLabel((ImageIcon) in);
		final JFrame frame = new JFrame();
		frame.add(img);
		frame.pack();
		frame.setVisible(true);
	}

	private void readAudio(final Object in) {
		final byte[] audioData = (byte[]) in;
		final InputStream byteArray = new ByteArrayInputStream(audioData);
		final AudioFormat format = new AudioFormat(44100.0F, 16, 2, true, false);

		try {
			final Clip sound = AudioSystem.getClip();
			sound.open(new AudioInputStream(byteArray, format, audioData.length
					/ format.getFrameSize()));
			sound.start();
		} catch (final LineUnavailableException | IOException e1) {
			e1.printStackTrace();
		}
	}

	private void setupStreams() throws IOException {
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connection.getInputStream());
		graphics.showMessage("\nStreams are now set up!");
	}

	private void connectToServer() throws IOException {
		graphics.showMessage("\nAttempting connection...");
		connection = new Socket(InetAddress.getByName(serverIP), PORT_NUMBER);
		graphics.showMessage("\nConnected to: "
				+ connection.getInetAddress().getHostName());
	}

}
