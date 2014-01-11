package de.taleCraft.launcher.jobs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SpringLayout;

import org.apache.commons.io.FileUtils;

import de.taleCraft.launcher.AppConstants;
import de.taleCraft.launcher.AppUtil;
import de.taleCraft.launcher.LauncherFrame;
import de.taleCraft.launcher.TaleCraftLauncher;
import de.taleCraft.launcher.TransparentJButton;

public class STJ_Initialize extends Job<EnumUpdateCheckResult> {
	
	public STJ_Initialize()
	{
		super("core:initialize");
	}
	
	@Override
	public Object execute() {
		
		LauncherFrame frame = LauncherFrame.INSTANCE;
		
		{
			frame.clearRootpane();
			JLabel l = new JLabel("Trying to download version-info from server ...");
			l.setOpaque(false);
			l.setBackground(AppConstants.NULL);
			l.setFont(l.getFont().deriveFont(16F));
			
			frame.windowLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, l, 0, SpringLayout.HORIZONTAL_CENTER, frame.window.getContentPane());
			frame.windowLayout.putConstraint(SpringLayout.VERTICAL_CENTER, l, 0, SpringLayout.VERTICAL_CENTER, frame.window.getContentPane());
			frame.window.add(l);
			frame.revalidateAndRedraw();
		}
		
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
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
		
		System.out.println("[Initialize] Updatecheck-Result: " + result);
		
		// Rebuild GUI
		{
			frame.clearRootpane();
			
			switch(result)
			{
				case DOWNLOAD_IS_NEWER: this.showUpdatePrompt(false, downloadIndexInfo); break;
				case NO_LOCAL_ONLINE_AVAIBLE: this.showUpdatePrompt(true, downloadIndexInfo); break;
				case CURRENT_IS_NEWER: this.showPlayScreen(); break;
				case LOCAL_AVAIBLE_ONLINE_NOT: this.showPlayScreen(); break;
				case SAME_VERSION: this.showPlayScreen(); break;
				case NO_VERSION_AVAIBLE_LOCAL_NOR_ONLINE: frame.showFatalErrorScreen("TaleCraft is not installed. Failed to contact server.", false); break;
				default:
					break;
			}
			
			frame.revalidateAndRedraw();
		}
		
		this.setResult(result);
		return null;
	}
	
	private void showPlayScreen() {
		// TODO: Implement 'Main-Menu'-Screen.
	}

	private void showUpdatePrompt(boolean forceUpdate, final DownloadIndexInfo downloadIndexInfo) {
		final LauncherFrame frame = LauncherFrame.INSTANCE;
		
		JLabel l = new JLabel(forceUpdate ? "TaleCraft is not yet installed. Install?" : "An update is avaible, download and install?");
		l.setOpaque(false);
		l.setBackground(AppConstants.NULL);
		l.setFont(l.getFont().deriveFont(16F));
		
		frame.windowLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, l, 0, SpringLayout.HORIZONTAL_CENTER, frame.window.getContentPane());
		frame.windowLayout.putConstraint(SpringLayout.VERTICAL_CENTER, l, -32, SpringLayout.VERTICAL_CENTER, frame.window.getContentPane());
		frame.window.add(l);
		
		JButton button_updateInstall = new TransparentJButton(forceUpdate ? "Install" : "Update");
		JButton button_cancelUpdate = new TransparentJButton(forceUpdate ? "Close Launcher" : "Don't Update");
		
		if(forceUpdate)
		{
			button_updateInstall.addActionListener(new ActionListener()
			{
				@Override public void actionPerformed(ActionEvent e)
				{
					System.out.println("FORCE_UPDATE");
					new Thread(new STJ_UpdateTalecraft(STJ_Initialize.this, downloadIndexInfo, true)).start();
				}
			});
			button_cancelUpdate.addActionListener(new ActionListener()
			{
				@Override public void actionPerformed(ActionEvent e)
				{
					if(JOptionPane.showConfirmDialog(
							frame.window,
							"Are you sure you wan't to close the Launcher?",
							"?",
							JOptionPane.YES_NO_OPTION)
							== JOptionPane.YES_OPTION
					){
						// Close Launcher! (Using some hard crashing stuff!)
						TaleCraftLauncher.launcher.frame.window.setVisible(false);
						System.exit(0);
						Runtime.getRuntime().halt(0);
					}
				}
			});
		}
		else
		{
			button_updateInstall.addActionListener(new ActionListener()
			{
				@Override public void actionPerformed(ActionEvent e)
				{
					System.out.println("UNFORCED_UPDATE");
				}
			});
			button_cancelUpdate.addActionListener(new ActionListener()
			{
				@Override public void actionPerformed(ActionEvent e)
				{
					System.out.println("GOTO_MAINSCREEN");
				}
			});
		}
		
		frame.windowLayout.putConstraint(SpringLayout.VERTICAL_CENTER, button_updateInstall, 32, SpringLayout.VERTICAL_CENTER, frame.window.getContentPane());
		frame.windowLayout.putConstraint(SpringLayout.VERTICAL_CENTER, button_cancelUpdate, 32, SpringLayout.VERTICAL_CENTER, frame.window.getContentPane());
		
		frame.windowLayout.putConstraint(SpringLayout.EAST, button_updateInstall, -8, SpringLayout.HORIZONTAL_CENTER, frame.window.getContentPane());
		frame.windowLayout.putConstraint(SpringLayout.WEST, button_cancelUpdate, +8, SpringLayout.HORIZONTAL_CENTER, frame.window.getContentPane());
		
		frame.window.add(button_updateInstall);
		frame.window.add(button_cancelUpdate);
		
		
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
