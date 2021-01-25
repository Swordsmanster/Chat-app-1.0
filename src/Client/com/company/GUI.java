package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.EOFException;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GUI implements ActionListener {

	public static JTextPane ta;
	public String svIP;
	Client soc;
	LocalDateTime timeObj;
	String formattedDate;
	DateTimeFormatter timeF;
	String nickname;
	JFrame frame;
	JMenuBar mb;
	JScrollPane sc;
	JTextField tf;
	BackgroundThread t;

	public static boolean validate(final String ip) {
		String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";

		return ip != null && ip.matches(PATTERN);
	}

	public void init() {

		timeObj = LocalDateTime.now();
		timeF = DateTimeFormatter.ofPattern("HH:mm");
		formattedDate = "";

		// Set up main window

		frame = new JFrame("Chat app");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setSize(600, 300);
		Dimension minSize = new Dimension(600, 300);
		frame.setMinimumSize(minSize);

		Image img = Toolkit.getDefaultToolkit().getImage("beach.jpeg");


		// Make window turn to icon on close

		long width;

		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				frame.setExtendedState(JFrame.ICONIFIED);
			}
		});


		// Set up menu bar
		//TODO: add more menu buttons

		mb = new JMenuBar();

		JMenu m1 = new JMenu("File");
		JMenu m2 = new JMenu("Help");

		JMenuItem m11 = new JMenuItem("Exit");
		JMenuItem m12 = new JMenuItem("Connect");
		JMenuItem m13 = new JMenuItem("Disconnect");

		m12.setActionCommand("connect");
		m13.setActionCommand("disconnect");
		m11.setActionCommand("exit");

		m11.addActionListener(this);
		m12.addActionListener(this);
		m13.addActionListener(this);

		m1.add(m12);
		m1.add(m13);
		m1.add(m11);

		mb.add(m1);
		mb.add(m2);


		// Set up bottom panel

		JPanel wp1 = new JPanel();
		JLabel lb1 = new JLabel("Say:");
		tf = new JTextField(10);

		tf.setActionCommand("sb");
		tf.addActionListener(this);

		wp1.add(lb1);
		wp1.add(tf);


		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(wp1, BorderLayout.WEST);

		// Set up middle panel

		ta = new JTextPane();


		ta.setEditable(false);
		ta.setText("Connect to a server to start chatting!");

		sc = new JScrollPane(ta);

		// Put everything together

		frame.getContentPane().add(mb, BorderLayout.NORTH);
		frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		frame.getContentPane().add(sc, BorderLayout.CENTER);

		frame.setVisible(true);

		t = new BackgroundThread();
		t.run();

	}

	public void updateTA(String msg) {
		ta.setText(ta.getText() + msg);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		// Update left text area

		if (e.getActionCommand().equals("sb")) {
			if (tf != null && ta != null) {

				// Take input from text field and clear it

				String message = tf.getText();
				tf.setText("");
				tf.grabFocus();
				if (message.compareTo("") != 0) {

					// Update text area
					if (soc != null) {
						soc.send("[" + nickname + "]" + ": " + message + "\n");
					}

					ta.setText(ta.getText() + "You: " + message + "\n");
				}
			}
		}


		if (e.getActionCommand().equals("exit")) {
			frame.dispose();
			disconnect();
			System.exit(0);
		}

		if (e.getActionCommand().equals("connect")) {
			String ip = JOptionPane.showInputDialog(frame, "Input server IP", null);
			if (validate(ip)) {
				nickname = JOptionPane.showInputDialog(frame, "Enter your nickname", null);
				if (nickname != null) {
					svIP = ip;
					soc = new Client(svIP, nickname);
					if (soc != null && soc.socket.isConnected()) {
						ta.setText("Connected\n");
					}

				}
			}
		}

		if (e.getActionCommand().equals("disconnect")) {
			disconnect();
		}
	}

	public void disconnect() {
		if (soc != null) {
			soc.disconnect();
			ta.setText("Disconnected. Connect to a server to start chatting!\n");
		}


	}

	class BackgroundThread implements Runnable {

		@Override
		public void run() {
			while (true) {
				if (soc != null && soc.in != null && !soc.socket.isClosed()) {
					try {
						String line = soc.in.readUTF();
						if (line != null) {
							updateTA(line);
						}
					} catch (SocketException e) {
						disconnect();
					} catch (EOFException e) {
						disconnect();
					} catch (Exception e) {
						e.printStackTrace();
					}

				} else {
					System.out.print("");
				}
				if (!frame.isEnabled()) return;
			}
		}
	}

}