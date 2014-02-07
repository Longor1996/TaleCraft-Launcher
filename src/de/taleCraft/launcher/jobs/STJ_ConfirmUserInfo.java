package de.taleCraft.launcher.jobs;

import javax.swing.JLabel;
import javax.swing.SpringLayout;

import de.taleCraft.launcher.AppConstants;
import de.taleCraft.launcher.LauncherFrame;

public class STJ_ConfirmUserInfo extends Job<Object> {

	public STJ_ConfirmUserInfo() {
		super("AuthCheck-Job");
	}

	@Override
	public Object execute() throws Throwable {
		
		LauncherFrame frame = LauncherFrame.INSTANCE;
		
		{
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
		
		
		
		
		
		return null;
	}
	
	
}
