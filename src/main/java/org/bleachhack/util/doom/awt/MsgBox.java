package org.bleachhack.util.doom.awt;

import java.awt.*;

import java.awt.event.*;

import javax.swing.JLabel;

/** A convenient message box class to pop up here and there.
 *  
 * @author zyklon
 *
 */

public class MsgBox extends Dialog implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -872019680203708495L;
	private Button ok, can;
	private boolean isOk = false;

	/*  
	 * * @param frame parent frame
	 * 
	 * @param msg message to be displayed
	 * 
	 * @param okcan true : ok cancel buttons, false : ok button only
	 */

	public boolean isOk() {
		return isOk;
	}

	public MsgBox(Frame frame, String title, String msg, boolean okcan) {
		super(frame, title, true);
		setLayout(new BorderLayout());
		add("Center", new JLabel(msg));
		addOKCancelPanel(okcan);
		createFrame();
		pack();
		setVisible(true);
		this.can.requestFocus();
	}

	public MsgBox(Frame frame, String msg) {
		this(frame, "Message", msg, false);
	}

	private void addOKCancelPanel(boolean okcan) {
		Panel p = new Panel();
		p.setLayout(new FlowLayout());
		createOKButton(p);
		if (okcan == true)
			createCancelButton(p);
		add("South", p);
	}

	private void createOKButton(Panel p) {
		p.add(ok = new Button("OK"));
		ok.addActionListener(this);
	}

	private void createCancelButton(Panel p) {
		p.add(can = new Button("Cancel"));
		can.addActionListener(this);
	}

	private void createFrame() {
		Dimension d = getToolkit().getScreenSize();
		setLocation(d.width / 3, d.height / 3);		
	}

	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == ok) {
			isOk = true;
			setVisible(false);
		} else if (ae.getSource() == can) {
			setVisible(false);
		}
	}

	/*
	 * public static void main(String args[]) { //Frame f = new Frame();
	 * //f.setSize(200,200); //f.setVisible(true); MsgBox message = new MsgBox
	 * (null , "Hey you user, are you sure ?", true); if (message.isOk)
	 * System.out.println("Ok pressed"); if (!message.isOk)
	 * System.out.println("Cancel pressed"); message.dispose(); }
	 */
}