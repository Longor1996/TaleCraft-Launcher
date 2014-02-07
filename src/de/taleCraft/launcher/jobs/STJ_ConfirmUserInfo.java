package de.taleCraft.launcher.jobs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.apache.commons.io.FileUtils;

import de.taleCraft.launcher.AppConstants;
import de.taleCraft.launcher.LauncherFrame;
import de.taleCraft.launcher.TaleCraftLauncher;

public class STJ_ConfirmUserInfo extends Job<Object> {

	public STJ_ConfirmUserInfo() {
		super("AuthCheck-Job");
	}

	@Override
	public Object execute() throws Throwable {
		{
			LauncherFrame frame = LauncherFrame.INSTANCE;
			
			frame.clearRootpane();
			JLabel l = new JLabel("Checking previous Login...");
			l.setOpaque(false);
			l.setBackground(AppConstants.NULL);
			l.setFont(l.getFont().deriveFont(16F));
			
			frame.windowLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, l, 0, SpringLayout.HORIZONTAL_CENTER, frame.window.getContentPane());
			frame.windowLayout.putConstraint(SpringLayout.VERTICAL_CENTER, l, 0, SpringLayout.VERTICAL_CENTER, frame.window.getContentPane());
			frame.window.add(l);
			frame.revalidateAndRedraw();
		}
		
		boolean refreshNeeded = !this.checkOldAccessToken();
		
		if(refreshNeeded)
		{
			this.buildLoginGui();;
		}
		else
		{
			// Implement link to the Main-Menu!
		}
		
		return null;
	}

	private void buildLoginGui() {
		LauncherFrame frame = LauncherFrame.INSTANCE;
		
		frame.clearRootpane();
		
		JLabel l = new JLabel("Please Login with your Minecraft-Account");
		l.setOpaque(false);
		l.setBackground(AppConstants.NULL);
		l.setFont(l.getFont().deriveFont(Font.BOLD, 16F));
		
		final JEditorPane hiddenMessage = new JEditorPane();
		hiddenMessage.setOpaque(false);
		hiddenMessage.setText("-null-");
		hiddenMessage.setEditable(false);
		hiddenMessage.setPreferredSize(new Dimension(256, 16 * 3));
		hiddenMessage.setForeground(Color.RED);
		hiddenMessage.setVisible(false);
		
		JTextField fieldUSERNAME = new JTextField();
		fieldUSERNAME.setBackground(Color.LIGHT_GRAY.brighter());
		fieldUSERNAME.setFont(fieldUSERNAME.getFont().deriveFont(Font.BOLD,12F));
		fieldUSERNAME.setColumns(18);
		
		JPasswordField fieldUSERPASS = new JPasswordField();
		fieldUSERPASS.setBackground(Color.LIGHT_GRAY.brighter());
		fieldUSERPASS.setFont(fieldUSERPASS.getFont().deriveFont(12F));
		fieldUSERPASS.setColumns(18);
		
		JLabel lN = new JLabel("Username");
		lN.setOpaque(false);
		lN.setBackground(AppConstants.NULL);
		lN.setFont(l.getFont().deriveFont(12F));
		
		JLabel lP = new JLabel("Password");
		lN.setOpaque(false);
		lP.setBackground(AppConstants.NULL);
		lP.setFont(l.getFont().deriveFont(12F));
		
		@SuppressWarnings("serial")
		JButton login = new JButton(new AbstractAction("Login")
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				
				// Do stuff!
				
				// If something goes wrong during Login, we can simply 'post-update' the spring-layout and blend in a error-message.
				// That will look way nicer than dumb dialog!
				hiddenMessage.setText("Unable to Login:\nFunctionality not yet implemented.\nderp");
				hiddenMessage.revalidate();
				hiddenMessage.setVisible(true);
				
			}
		});
		login.setOpaque(false);
		login.setFont(login.getFont().deriveFont(14F));
		
		frame.windowLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, l, 0, SpringLayout.HORIZONTAL_CENTER, frame.window.getContentPane());
		frame.windowLayout.putConstraint(SpringLayout.VERTICAL_CENTER, l, -96, SpringLayout.VERTICAL_CENTER, frame.window.getContentPane());
		
		frame.windowLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, fieldUSERNAME, 0, SpringLayout.HORIZONTAL_CENTER, frame.window.getContentPane());
		frame.windowLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, fieldUSERPASS, 0, SpringLayout.HORIZONTAL_CENTER, frame.window.getContentPane());
		frame.windowLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, login, 0, SpringLayout.HORIZONTAL_CENTER, frame.window.getContentPane());
		frame.windowLayout.putConstraint(SpringLayout.WEST, hiddenMessage, 8, SpringLayout.WEST, l);
		
		frame.windowLayout.putConstraint(SpringLayout.NORTH, hiddenMessage, 2, SpringLayout.SOUTH, l);
		frame.windowLayout.putConstraint(SpringLayout.NORTH, fieldUSERNAME, +2, SpringLayout.SOUTH, hiddenMessage);
		frame.windowLayout.putConstraint(SpringLayout.NORTH, fieldUSERPASS, +12, SpringLayout.SOUTH, fieldUSERNAME);
		frame.windowLayout.putConstraint(SpringLayout.NORTH, login, +12, SpringLayout.SOUTH, fieldUSERPASS);
		
		frame.windowLayout.putConstraint(SpringLayout.EAST, lN, -12, SpringLayout.WEST, fieldUSERNAME);
		frame.windowLayout.putConstraint(SpringLayout.EAST, lP, -12, SpringLayout.WEST, fieldUSERPASS);
		
		frame.windowLayout.putConstraint(SpringLayout.VERTICAL_CENTER, lN, 0, SpringLayout.VERTICAL_CENTER, fieldUSERNAME);
		frame.windowLayout.putConstraint(SpringLayout.VERTICAL_CENTER, lP, 0, SpringLayout.VERTICAL_CENTER, fieldUSERPASS);
		
		frame.window.add(l);
		frame.window.add(hiddenMessage);
		frame.window.add(fieldUSERNAME);
		frame.window.add(fieldUSERPASS);
		frame.window.add(login);
		frame.window.add(lN);
		frame.window.add(lP);
		
		frame.revalidateAndRedraw();
		fieldUSERNAME.requestFocus();
		
		
	}

	private boolean checkOldAccessToken() {
		File accessTokenFile = new File(TaleCraftLauncher.launcher.workingDirectory, "accessToken.auth");
		
		// If the token-file does NOT exist, we will HAVE to force a refresh!
		if(!accessTokenFile.exists())
			return false;
		
		String accessToken = null;
		try {
			accessToken = FileUtils.readFileToString(accessTokenFile);
		} catch (IOException e) {
			e.printStackTrace();
			accessToken = null;
		}
		
		// If we were unable to read the File, force refresh.
		if(accessToken == null)
		{
			JOptionPane.showMessageDialog(TaleCraftLauncher.launcher.frame.window,
					"The Authentication-system was unable to read the Acces-Token file.\n" +
					"You will now be forced to Login again.\n\n" +
					"If this message keeps appearing, there might be something wrong with the TaleCraft directory!\n" +
					"Go to the official Forum-Thread of TaleCraft and ask for help. Don't forget to be nice.\n"
			);
			return false;
		}
		
		// TODO: Implement a small code to validate the AccessToken on the official Minecraft-Server!
		
		// ???: com.mojang.authlib.yggdrasil.request.XXX
		// ???: new RefreshRequest(new YggdrasilUserAuthentication(new YggdrasilAuthenticationService(Proxy.NO_PROXY, accessToken), Agent.MINECRAFT));
		
		
		// For now, just return true!
		return (new Random().nextInt(12)) < accessToken.length();
	}
	
	
}
