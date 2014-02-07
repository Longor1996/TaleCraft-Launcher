package de.taleCraft.launcher.platform;

import java.io.File;
import java.io.FileOutputStream;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import de.taleCraft.launcher.TaleCraftLauncher;

/** 'OS'-Class **/
public abstract class Platform {
	
	/** Utility Method **/
	public static final Platform detectPlatform()
	{
		String osName = System.getProperty("os.name").toLowerCase();
		
		if(osName.contains("windows"))
			return new PlatformWindows();
		
		if(osName.contains("linux") || osName.contains("freebsd") || osName.contains("sunos") || osName.contains("unix"))
			return new PlatformLinux();
		
		if(osName.contains("mac") || osName.contains("osx") || osName.contains("mac osx") || osName.startsWith("darwin"))
			return new PlatformMacOSX();
		
		System.out.println("Detecting Platform-Handler for: " + osName);
		
		// We don't know this OS! Return null and let the entire thing crash with a nice error!
		return null;
	}

	/** Utility Method **/
	public static final String detectWorkingDirectory()
	{
		Preferences tcPrefs = Preferences.systemNodeForPackage(TaleCraftLauncher.class);
		String workingDirectory = tcPrefs.get("workingDirectory", "<-NULL->");
		
		if(workingDirectory.equalsIgnoreCase("<-NULL->"))
			workingDirectory = acquireNewWorkingDirectory(tcPrefs);
		
		System.out.println("[Info] Working-Directory is at: " + workingDirectory);
		
		return workingDirectory;
	}

	/** Utility Method **/
	private static final String acquireNewWorkingDirectory(Preferences tcPrefs) {
		File workingDirectoryFile = null;
		
		do {
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Select TaleCraft Working-Directory! (You only have to do this once)");
			chooser.setFileHidingEnabled(false);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setMultiSelectionEnabled(false);
			chooser.setSelectedFile(new File(TaleCraftLauncher.class.getProtectionDomain().getCodeSource().getLocation().getPath()));
			int result = chooser.showOpenDialog(null);
			
			if(result == JFileChooser.CANCEL_OPTION)
			{
				int breakResult = JOptionPane.showConfirmDialog(null, "If you cancel this, you wont be able to use the Application! It will just close itself!", "?!", JOptionPane.OK_CANCEL_OPTION);
				
				if(breakResult == JOptionPane.OK_OPTION)
				{
					// Hard Crash the JVM!
					System.exit(0);
					Runtime.getRuntime().halt(0);
				}
				
				continue;
			}
			
			if(result == JFileChooser.APPROVE_OPTION)
				workingDirectoryFile = chooser.getSelectedFile();
			
			if(!workingDirectoryFile.isDirectory())
				continue;
			
		} while((workingDirectoryFile == null) || !testDirectoryForWriteAccess(workingDirectoryFile));
		
		String workingDirectory = workingDirectoryFile.getAbsolutePath();
		tcPrefs.put("workingDirectory", workingDirectory);
		return workingDirectory;
	}
	
	/** Utility Method **/
	private static final boolean testDirectoryForWriteAccess(File workingDirectoryFile) {
		File file = new File(workingDirectoryFile, "writeAccessTest.dat");
		
		try
		{
			FileOutputStream out = new FileOutputStream(file);
			out.write('T');
			out.write('E');
			out.write('S');
			out.write('T');
			out.flush();
			out.close();
			file.delete();
			
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}

	/**
	 * This is the name of the native library folder containing the native libraries for this given Platform-Handler (/Operating-System).
	 * Can be: windows, linux, macosx, null(crash!)
	 **/
	public abstract String getLWJGLLibraryName();
	
	public abstract String getSimpleName();
	
}
