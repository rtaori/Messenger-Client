import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.jhlabs.image.PosterizeFilter;

public class ClientGUI extends JFrame {

	private static final long serialVersionUID = 1L;

	private JTextField userText;
	private JTextArea chatWindow;
	private JButton fileLoad;
	private JPanel controlPanel;
	private ActionListener attachUpdate;

	public ClientGUI(final ActionListener textUpdate, final ActionListener attachUpdate) {
		super("Messaging App (Client)!");

		userText = new JTextField();
		userText.setEditable(false);
		userText.addActionListener(textUpdate);
		userText.setText("");
		userText.setCaretPosition(userText.getDocument().getLength());

		chatWindow = new JTextArea();
		chatWindow.setEditable(false);

		fileLoad = new JButton("Attach");
		fileLoad.addActionListener(e -> this.chooseFile());

		this.attachUpdate = attachUpdate;

		controlPanel = new JPanel(new BorderLayout());
		controlPanel.add(fileLoad, BorderLayout.WEST);
		controlPanel.add(userText, BorderLayout.CENTER);

		this.add(controlPanel, BorderLayout.SOUTH);
		this.add(new JScrollPane(chatWindow), BorderLayout.CENTER);
		this.setSize(350, 500);
		this.setLocation(500, 100);
	}

	private void chooseFile() {
		final String[] choices = { "Audio", "Picture", "Take a Selfie!" };
		final String choice = (String) JOptionPane.showInputDialog(null,
				"What do you wanna send?", "", JOptionPane.PLAIN_MESSAGE, null,
				choices, null);

		Object source = null;
		switch (choice) {
		case "Audio":
			source = this.sendAudio();
			break;
		case "Picture":
			source = this.sendPicture();
			break;
		case "Take a Selfie!":
			source = this.sendSelfie();
			break;
		default:
			return;
		}

		attachUpdate.actionPerformed(new ActionEvent(source, 0, null));
	}

	private ImageIcon sendPicture() {
		final JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new FileNameExtensionFilter(null, "jpg", "gif"));
		chooser.showOpenDialog(null);
		return new ImageIcon(chooser.getSelectedFile().toString());
	}

	private ImageIcon sendSelfie() {
		final Webcam webcam = Webcam.getDefault();
		webcam.setViewSize(WebcamResolution.VGA.getSize());

		final WebcamPanel panel = new WebcamPanel(webcam);
		panel.setMirrored(true);

		final JFrame window = new JFrame("WEBCAM!!!");
		window.add(panel);
		window.setResizable(true);
		window.pack();
		window.setVisible(true);

		int response = -23423;
		while (response != JOptionPane.OK_OPTION)
			response = JOptionPane.showConfirmDialog(null,
					"Click OK to take selfie!", "SELFIE!",
					JOptionPane.OK_CANCEL_OPTION);

		window.setVisible(false);
		webcam.open();
		BufferedImage image = webcam.getImage();

		response = JOptionPane.showConfirmDialog(null,
				"Picture taken!\nApply filter to picture?", "FILTER!",
				JOptionPane.YES_NO_OPTION);
		if (response == JOptionPane.YES_OPTION) {
			final BufferedImageOp filter = new PosterizeFilter();
			image = filter.filter(image, null);
		}

		final ImageIcon selfie = new ImageIcon(image);
		final JFrame frame = new JFrame();
		frame.add(new JLabel(selfie));
		frame.pack();
		frame.setVisible(true);

		JOptionPane.showMessageDialog(null, "Selfie sent!");
		return selfie;
	}

	private byte[] sendAudio() {
		final JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new FileNameExtensionFilter(null, "wav"));
		chooser.showOpenDialog(null);
		try {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			final BufferedInputStream in = new BufferedInputStream(
					new FileInputStream(chooser.getSelectedFile()));

			int read;
			final byte[] buff = new byte[50];
			while ((read = in.read(buff)) > 0)
				out.write(buff, 0, read);

			out.flush();
			in.close();
			return out.toByteArray();
		} catch (final IOException e) {
			e.printStackTrace();
			return new byte[1];
		}
	}

	public void makeVisible() {
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void setTypable(final boolean x) {
		SwingUtilities.invokeLater(() -> userText.setEditable(x));
	}

	public void showMessage(final String message) {
		SwingUtilities.invokeLater(() -> chatWindow.append(message));
	}

	public void resetText() {
		userText.setText("");
	}

}
