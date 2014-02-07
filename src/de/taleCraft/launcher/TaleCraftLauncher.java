package de.taleCraft.launcher;

import java.io.File;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.mojang.authlib.Agent;
import com.mojang.authlib.UserAuthentication;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

import de.taleCraft.launcher.jobs.STJ_ConfirmUserInfo;
import de.taleCraft.launcher.jobs.STJ_Initialize;
import de.taleCraft.launcher.platform.Platform;

public class TaleCraftLauncher {
	public static TaleCraftLauncher launcher;
	public static UserAuthentication auth =
			new YggdrasilUserAuthentication(new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString()), Agent.MINECRAFT);
	
	public static void main(String[] args)
	{
		
		System.out.println("Hello, World!  Starting up...");
		
		try{UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}catch(Exception e){}
		
		launcher = new TaleCraftLauncher();
		
		// genLibList();
		
		
		if(!launcher.run())
			System.exit(0);
		
	}
	
	/**
	 * @deprecated UNUSED! This method is not to be used. Only for usage in TaleCraft distribution.
	 **/
	public static void genLibList() {
		String strPath = launcher.workingDirectory.getAbsolutePath() + "\\serverside\\all_libs_to_download_and_add_to_classpath.txt";
		System.out.println("> TRY-DIR == " + strPath);
		System.out.println("");
		System.out.println("");
		
		ArrayList<String> paths = new ArrayList<String>();
		
		try
		{
			Scanner sc = new Scanner(new File(strPath));
			
			while(sc.hasNextLine())
			{
				String line = sc.nextLine().trim();
				
				if(line.isEmpty())
				{
					paths.add("");
					continue;
				}
				
				if(line.startsWith("//"))
				{
					paths.add(line);
					continue;
				}
				
				if(line.startsWith("#"))
					continue;
				
				if(line.contains("[STOP]"))
					break;
				
				System.out.println("[:lib:] " + line);
				
				String[] pieces = line.split(":");
				pieces[0].replace(".", "/");
				
				String fullPath = "https://libraries.minecraft.net/";
				
				fullPath += pieces[0];
				fullPath += "/";
				fullPath += pieces[1];
				fullPath += "/";
				fullPath += pieces[2];
				fullPath += "/";
				fullPath += pieces[1];
				fullPath += "-";
				fullPath += pieces[2];
				fullPath += ".jar";
				
				paths.add(fullPath);
			}
			
			sc.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		System.out.println("");
		
		for(String str : paths)
			System.out.println("\t\t{url:\"" + str + "\"},");
		
		System.out.println("");
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
		
		new Thread(new STJ_ConfirmUserInfo()).start();
		new Thread(new STJ_Initialize()).start();
		
		return true;
	}
	
}
