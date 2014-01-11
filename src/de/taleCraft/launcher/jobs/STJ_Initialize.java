package de.taleCraft.launcher.jobs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JLabel;
import javax.swing.SpringLayout;

import org.apache.commons.io.FileUtils;

import de.taleCraft.launcher.AppConstants;
import de.taleCraft.launcher.AppUtil;
import de.taleCraft.launcher.LauncherFrame;
import de.taleCraft.launcher.TaleCraftLauncher;

public class STJ_Initialize extends Job<EnumUpdateCheckResult> {
	
	public STJ_Initialize()
	{
		super("core:initialize");
	}
	
	@Override
	public Object execute() {
		
		LauncherFrame frame = LauncherFrame.INSTANCE;
		
		JLabel l = new JLabel("Trying to download version-info from server ...");
		l.setOpaque(false);
		l.setBackground(AppConstants.NULL);
		l.setFont(l.getFont().deriveFont(16F));
		
		frame.windowLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, l, 0, SpringLayout.HORIZONTAL_CENTER, frame.window.getContentPane());
		frame.windowLayout.putConstraint(SpringLayout.VERTICAL_CENTER, l, 0, SpringLayout.VERTICAL_CENTER, frame.window.getContentPane());
		frame.window.add(l);
		
		frame.window.repaint();
		
		AppUtil.sleep(500L);
		
		frame.window.repaint();
		
		boolean indexFileDownloadSuccess = false;
		File dowloadIndexLocalFile = new File(TaleCraftLauncher.launcher.workingDirectory, AppConstants.IO_DownloadIndexFileName);
		File localVersionFile = new File(TaleCraftLauncher.launcher.workingDirectory, AppConstants.IO_VersionFileName);
		FileUtils.deleteQuietly(dowloadIndexLocalFile);
		AppUtil.sleep(100L);
		
		try {
			URL url = new URL(AppConstants.NET_VersionInfoURL);
			URLConnection connection = url.openConnection();
			
			if(AppUtil.isAvaible(connection))
			{
				InputStream inputStream = connection.getInputStream();
				FileUtils.copyInputStreamToFile(inputStream, dowloadIndexLocalFile);
				inputStream.close();
				indexFileDownloadSuccess = true;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			indexFileDownloadSuccess = false;
		} catch (IOException e) {
			e.printStackTrace();
			indexFileDownloadSuccess = false;
		}
		
		// Load the current Version Information!
		String currentVersion = null;
		try {
			currentVersion = FileUtils.readFileToString(localVersionFile);
		} catch (IOException e) {
			System.out.println("[Error] Failed to read local Version-File: " + e.getLocalizedMessage());
			currentVersion = null;
		}
		
		// Load the downloadIndex!
		DownloadIndexInfo downloadIndexInfo = new DownloadIndexInfo(dowloadIndexLocalFile);
		
		EnumUpdateCheckResult result = this.compareVersion(currentVersion, downloadIndexInfo.versionString);
		
		// Here comes the true nightmare!
		// Do stuff depending on the EnumUpdateCheckResult!
		
		System.out.println("result: " + result);
		
		this.setResult(result);
		return null;
	}
	
	public EnumUpdateCheckResult compareVersion(String current, String downloadIndexVersion)
	{
		if((current == null) && (downloadIndexVersion != null))
			return EnumUpdateCheckResult.NO_LOCAL_ONLINE_AVAIBLE;
		
		if((current != null) && (downloadIndexVersion == null))
			return EnumUpdateCheckResult.LOCAL_AVAIBLE_ONLINE_NOT;
		
		if((current == null) && (downloadIndexVersion == null))
			return EnumUpdateCheckResult.NO_VERSION_AVAIBLE_LOCAL_NOR_ONLINE;
		
		if((current != null) && (downloadIndexVersion != null))
			; // continue to compare
		
		System.out.println("compareVersion C = " + current);
		System.out.println("compareVersion D = " + downloadIndexVersion);
		
		// Convert strings into binary representations
		// split step
		String[] strC = current.split(".");
		String[] strD = downloadIndexVersion.split(".");
		
		// malloc step
		int[] intC = new int[strC.length];
		int[] intD = new int[strD.length];
		
		// convert step
		for(int i = 0; i < strC.length; i++)
			intC[i] = Integer.valueOf(strC[i]);
		for(int i = 0; i < strD.length; i++)
			intD[i] = Integer.valueOf(strD[i]);
		
		// Compare the two int arrays, and check which one is bigger.
		for(int i = 0; i < strC.length; i++)
		{
			// fetch step
			int C = intC[i];
			int D = intD[i];
			
			// comparison step
			if(C > D)
			{
				; // C is newer.
				return EnumUpdateCheckResult.CURRENT_IS_NEWER;
			}
			else if(D > C)
			{
				; // D is newer.
				return EnumUpdateCheckResult.DOWNLOAD_IS_NEWER;
			}
			else
			{
				; // Same number, check if this is the last index!
				if(i == (strC.length-1))
				{
					; // The versions are completely the same.
					return EnumUpdateCheckResult.SAME_VERSION;
				}
				else
				{
					; // Same number, check the next.
					continue;
				}
			}
		}
		
		// This should never be reached!
		// error return step
		return EnumUpdateCheckResult.NO_VERSION_AVAIBLE_LOCAL_NOR_ONLINE;
	}
	
}
