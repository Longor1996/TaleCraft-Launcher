package de.taleCraft.launcher;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.SpringLayout;

public class LauncherFrame {
	public static final LauncherFrame INSTANCE = new LauncherFrame();
	
	public JFrame window;
	public SpringLayout windowLayout;
	
	private LauncherFrame()
	{
		this.window = new JFrame("TaleCraft - Launcher");
		this.windowLayout = new SpringLayout();
		
		// ---- Window Config
		// Size
		final int initialWidth = 768;
		final int initialHeight = 560;
		this.window.setLayout(this.windowLayout);
		this.window.setPreferredSize(new Dimension(initialWidth,initialHeight));
		this.window.setSize(new Dimension(initialWidth,initialHeight));
		this.window.setMinimumSize(new Dimension(128,96));
		this.window.setResizable(true);
		// Miscellaneous
		this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.window.setAutoRequestFocus(true);
		this.window.setLocationRelativeTo(null);
		
		// ---- End!
		
	}
	
}
