package de.taleCraft.launcher;

import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import de.taleCraft.launcher.jobs.STJ_Initialize;
import de.taleCraft.launcher.platform.Platform;

public class TaleCraftLauncher {
	public static TaleCraftLauncher launcher;
	public static void main(String[] args)
	{
		
		System.out.println("Hello, World!  Starting up...");
		
		try{UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}catch(Exception e){}
		
		launcher = new TaleCraftLauncher();
		
		if(!launcher.run())
			System.exit(0);
		
	}
	
	public final LauncherFrame frame;
	public final Platform platform;
	public final File workingDirectory;
	public final boolean constructSuccess;
	
	private TaleCraftLauncher()
	{
		this.frame = LauncherFrame.INSTANCE;
		this.workingDirectory = new File(Platform.detectWorkingDirectory());
		this.platform = Platform.detectPlatform();
		
		if(this.platform == null)
		{
			;JOptionPane.showMessageDialog(null,
					"The Launcher was unable to provide a Platform-Handler for your Operating-System!\n" +
					"This is a serios bug, and as such should be reported immediately to the developer.\n\n" +
					"Operating-System Name: " + System.getProperty("os.name") + "\n" +
					"Operating-System Version: " + System.getProperty("os.version") + "\n" +
					"\nThe Application will now be terminated, as it cannot work without a Platform-Handler.\n"
					, "TaleCraft-Launcher: Fatal Error Occurred", JOptionPane.ERROR_MESSAGE
			);
			this.constructSuccess = false;
			return;
		}
		
		this.constructSuccess = true;
	}
	
	private boolean run()
	{
		if(!this.constructSuccess)
			return false;
		
		this.frame.window.setVisible(true);
		
		new Thread(new STJ_Initialize()).start();
		
		return true;
	}
	
}
