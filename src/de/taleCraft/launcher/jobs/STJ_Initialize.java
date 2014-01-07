package de.taleCraft.launcher.jobs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JLabel;
import javax.swing.SpringLayout;

import de.taleCraft.launcher.AppConstants;
import de.taleCraft.launcher.AppUtil;
import de.taleCraft.launcher.LauncherFrame;

public class STJ_Initialize extends SingleThreadedJob {
	
	public STJ_Initialize()
	{
		super("core:initialize");
	}
	
	@Override
	public void execute() {
		
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
		
		try {
			URL url = new URL(AppConstants.NET_VersionInfoURL);
			URLConnection connection = url.openConnection();
			
			
			
			if(AppUtil.isAvaible(connection))
			{
				InputStream inputStream = connection.getInputStream();
				System.err.println("Success: " + connection + " ::> " + inputStream);
				
				byte[] a = (byte[]) AppUtil.getFieldContent(ByteArrayInputStream.class, inputStream, "buf");
				
				System.out.println("["+a.length+"] " + new String(a));
				
				;
			}
			else
			{
				System.err.println("Error X: " + connection);;
			}
			// connection.
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
