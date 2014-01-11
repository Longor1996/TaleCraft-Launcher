package de.taleCraft.launcher;

import javax.swing.JButton;

public class TransparentJButton extends JButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6404780581903355592L;

	public TransparentJButton(String string) {
		super(string);
		this.setOpaque(false);
	}
	
	
	
}
