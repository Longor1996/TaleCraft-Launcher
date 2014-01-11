package de.taleCraft.launcher.jobs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SpringLayout;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import de.taleCraft.launcher.AppConstants;
import de.taleCraft.launcher.AppUtil;
import de.taleCraft.launcher.LauncherFrame;
import de.taleCraft.launcher.TaleCraftLauncher;

public class STJ_UpdateTalecraft extends Job<Object> {
	Job<?> preThread;
	DownloadIndexInfo downloadIndexInfo;
	boolean forceUpdate = false;
	
	public STJ_UpdateTalecraft(Job<?> job, DownloadIndexInfo downloadIndexInfo, boolean isForceUpdate) {
		super("TaleCratUpdateJob-main");
		this.preThread = job;
		this.downloadIndexInfo = downloadIndexInfo;
		this.forceUpdate = isForceUpdate;
	}
	
	@Override
	public Object execute() {
		
		// If there is a pre-Job, wait until it finishes!
		if(this.preThread != null)
			while(!this.preThread.isFinished()) Thread.yield();
		
		// Now do stuff!
		final LauncherFrame frame = LauncherFrame.INSTANCE;
		frame.clearRootpane();
		
		{
			// setup
			JLabel l = new JLabel("Installing TaleCraft...");
			l.setOpaque(false);
			l.setBackground(AppConstants.NULL);
			l.setFont(l.getFont().deriveFont(16F));
			
			// layout
			frame.windowLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, l, 0, SpringLayout.HORIZONTAL_CENTER, frame.window.getContentPane());
			frame.windowLayout.putConstraint(SpringLayout.VERTICAL_CENTER, l, -32, SpringLayout.VERTICAL_CENTER, frame.window.getContentPane());
			
			// add
			frame.window.add(l);
		}
		
		// We need progressBar later, so we can't put it into the block.
		JProgressBar progressBar = new JProgressBar(0,100);
		
		// Initialize the Progress-Bar
		{
			// setup
			progressBar.setOpaque(false);
			progressBar.setBorder(BorderFactory.createRaisedBevelBorder());
			progressBar.setStringPainted(true);
			progressBar.setString("...?");
			progressBar.setFont(progressBar.getFont().deriveFont(16F));
			
			// layout
			frame.windowLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, progressBar, 0, SpringLayout.HORIZONTAL_CENTER, frame.window.getContentPane());
			frame.windowLayout.putConstraint(SpringLayout.NORTH, progressBar, 96, SpringLayout.VERTICAL_CENTER, frame.window.getContentPane());
			frame.windowLayout.putConstraint(SpringLayout.SOUTH, progressBar, 96+24, SpringLayout.VERTICAL_CENTER, frame.window.getContentPane());
			
			frame.windowLayout.putConstraint(SpringLayout.WEST, progressBar, 32, SpringLayout.WEST, frame.window.getContentPane());
			frame.windowLayout.putConstraint(SpringLayout.EAST, progressBar, -32, SpringLayout.EAST, frame.window.getContentPane());
			
			// add
			frame.window.add(progressBar);
		}
		
		frame.revalidateAndRedraw();
		frame.redraw();
		
		// Setup all the needed folders and files!
		File downloadFolder = new File(TaleCraftLauncher.launcher.workingDirectory, "downloads");
		
		// This operation should NOT fail!
		try {
			progressBar.setString("Touching Cache...");
			FileUtils.touch(new File(downloadFolder, "key.dat"));
			AppUtil.sleep(50);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// This operation can fail in case of the case.
		try {
			progressBar.setString("Cleaning Cache...");
			FileUtils.cleanDirectory(downloadFolder);
			AppUtil.sleep(50);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Process the download-index!
		progressBar.setString("Processing Index-File...");
		
		if(this.downloadIndexInfo == null)
		{
			progressBar.setString("ERROR: Index-File does not exist!");
			
			ActionListener action;
			
			if(this.forceUpdate)
			{
				action = new ActionListener()
				{
					@Override public void actionPerformed(ActionEvent e)
					{
						// Close Launcher! (Using some hard crashing stuff!)
						TaleCraftLauncher.launcher.frame.window.setVisible(false);
						System.exit(0);
						Runtime.getRuntime().halt(0);
					}
				};;
			}
			else
			{
				action = new ActionListener()
				{
					@Override public void actionPerformed(ActionEvent e)
					{
						// TODO: Implement link to the Main-Menu!
					}
				};;
			}
			
			frame.showErrorReport(
					"Failed to install! :(",
					"The Download-Index File could not be read.\nSomething did just go terribly wrong... Bug?",
					this.forceUpdate ? "Close launcher" : "Play using the existing Version",
					action
			);
			frame.redraw();
			
			return null;
		}
		
		////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////
		
		// Initialize Download-Threads!
		progressBar.setString("Initalizing Download Threads...");
		DownloadJob[] JOBS = new DownloadJob[16];
		
		DownloadJob dj_mc = JOBS[0] = new DownloadJob("mc", this.downloadIndexInfo.mcJar, new File(downloadFolder, "install.mc.jar"));
		DownloadJob dj_tc = JOBS[1] = new DownloadJob("tc", this.downloadIndexInfo.tcJar, new File(downloadFolder, "install.tc.jar"));
		DownloadJob dj_forge = JOBS[2] = new DownloadJob("forge", this.downloadIndexInfo.forgeJar, new File(downloadFolder, "install.forge.jar"));
		
		int jobCounter = 0;
		
		for(int i = 0; i < JOBS.length; i++)
			if(JOBS[i] != null)
				jobCounter++;
		
		progressBar.setMaximum(jobCounter-1);
		
		// Start the Download-Threads!
		for(int i = 0; i < JOBS.length; i++)
			if(JOBS[i] != null)
				new Thread(JOBS[i]).start();
		
		// Download...
		while(true)
		{
			boolean done = true;
			int countDone = 0;
			
			for(int i = 0; i < JOBS.length; i++)
				if((JOBS[i] != null))
					if(!JOBS[i].isFinished())
						done = false;
					else
						countDone++;
			
			if(done)
				break;
			
			progressBar.setString("Downloading Files... ["+countDone+" of "+jobCounter+"]");
			progressBar.setValue(countDone);
			frame.redraw();
			AppUtil.sleep(10);
		}
		
		// Check if everything wen't right!
		boolean success = true;
		
		for(int i = 0; i < JOBS.length; i++)
			if(JOBS[i] != null)
				if(!JOBS[i].isSuccess())
				{
					success = false;
					break;
				}
		success = false;
		
		// If the downloads failed, we will display an error message!
		if(!success)
		{
			StringBuffer area = new StringBuffer("The installation of TaleCraft failed. This is the error-report of the installation process.\n\n");
			
			area.append("----------------------\n");
			
			for(int i = 0; i < JOBS.length; i++)
			{
				if(JOBS[i] != null)
				{
					DownloadJob dj = JOBS[i];
					
					if(!dj.isSuccess())
					{
						area.append("Download no." + i + ", failed because of");
						
						Object obj = dj.getFailureObject();
						
						if(obj instanceof Throwable)
						{
							area.append("an exception that occurred while downloading the file.\nFollowing is the stacktrace of the exception.\n");
							String str = ExceptionUtils.getStackTrace((Throwable) obj);
							area.append(str);
							area.append("\n");
						}
						else
						{
							area.append("an system failure.\nFollowing is the string representation of the returned failure.");;
							area.append(obj.toString());
							area.append("\n");
						}
					}
					else
					{
						area.append("Download no."+i+ " did not fail.");
						area.append("\n");
					}
					area.append("\n");
					
				};
			}
			
			ActionListener action;
			
			if(this.forceUpdate)
			{
				action = new ActionListener()
				{
					@Override public void actionPerformed(ActionEvent e)
					{
						// Close Launcher! (Using some hard crashing stuff!)
						TaleCraftLauncher.launcher.frame.window.setVisible(false);
						System.exit(0);
						Runtime.getRuntime().halt(0);
					}
				};;
			}
			else
			{
				action = new ActionListener()
				{
					@Override public void actionPerformed(ActionEvent e)
					{
						// TODO: Implement link to the Main-Menu!
					}
				};;
			}
			
			frame.showErrorReport(
					"Failed to install! :(",
					area.toString(),
					this.forceUpdate ? "Close launcher" : "Play using the existing Version",
					action
			);
			frame.redraw();
			
			return null;
		}
		
		////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////
		
		// Time for the next step!
		progressBar.setString("Unpacking Files...");
		progressBar.setValue(0);
		AppUtil.sleep(100);
		
		
		
		
		
		return null;
	}
	
}
