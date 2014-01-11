package de.taleCraft.launcher;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class LauncherFrame {
	public static final LauncherFrame INSTANCE = new LauncherFrame();
	
	public JFrame window;
	public SpringLayout windowLayout;
	
	private LauncherFrame()
	{
		this.window = new JFrame("TaleCraft - Launcher");
		
		// Custom JPanel as ContentPane allows us to draw anything as the Main-Frame background.
		this.window.setContentPane(new JPanel()
		{
			{
				this.setOpaque(false);
			}
			
			@Override public void paint(Graphics g)
			{
				Graphics2D g2d = (Graphics2D) g;
				g2d.clearRect(0, 0, this.getWidth(), this.getHeight());
				g2d.setPaint(new GradientPaint(this.getWidth()/2, 0, Color.LIGHT_GRAY, this.getWidth()/2, this.getHeight(), Color.GRAY));
				g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
				
				super.paint(g);
			}
		});
		this.windowLayout = new SpringLayout();
		
		// ---- Window Config
		
		// Size
		final int initialWidth = 768;
		final int initialHeight = 560;
		this.window.setLayout(this.windowLayout);
		this.window.setPreferredSize(new Dimension(initialWidth,initialHeight));
		this.window.setSize(new Dimension(initialWidth,initialHeight));
		this.window.setMinimumSize(new Dimension(256,96+64));
		this.window.setResizable(true);
		
		// Miscellaneous
		this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.window.setAutoRequestFocus(true);
		this.window.setLocationRelativeTo(null);
		
		// ---- End!
		
	}
	
	public void clearRootpane()
	{
		this.window.getContentPane().removeAll();
		//this.window.setLayout(new SpringLayout());
		this.revalidateAndRedraw();
	}
	
	public void revalidateAndRedraw() {
		SwingUtilities.invokeLater(new Runnable(){
			@Override public void run()
			{
				LauncherFrame.this.window.revalidate();
				LauncherFrame.this.window.repaint();
				AppUtil.sleep(100);
				LauncherFrame.this.window.repaint();
			}
		});
	}

	public void showFatalErrorScreen(String string, boolean backToMainScreen) {
		this.clearRootpane();
		
		JLabel l = new JLabel(string);
		l.setOpaque(false);
		l.setBackground(AppConstants.NULL);
		l.setFont(l.getFont().deriveFont(16F));
		
		this.windowLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, l, 0, SpringLayout.HORIZONTAL_CENTER, this.window.getContentPane());
		this.windowLayout.putConstraint(SpringLayout.VERTICAL_CENTER, l, -32, SpringLayout.VERTICAL_CENTER, this.window.getContentPane());
		this.window.add(l);
		
		JButton button = new TransparentJButton(backToMainScreen ? "Back to Main-Menu" : "Close Launcher");
		
		if(backToMainScreen)
		{
			button.addActionListener(new ActionListener()
			{
				@Override public void actionPerformed(ActionEvent e)
				{
					// TODO: Implement BackTomainMenu for the Error-Screen.
				}
			});;
		}
		else
		{
			button.addActionListener(new ActionListener()
			{
				@Override public void actionPerformed(ActionEvent e)
				{
					// Close Launcher! (Using some hard crashing stuff!)
					TaleCraftLauncher.launcher.frame.window.setVisible(false);
					System.exit(0);
					Runtime.getRuntime().halt(0);
				}
			});;
		}
		
		this.windowLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, button, 0, SpringLayout.HORIZONTAL_CENTER, this.window.getContentPane());
		this.windowLayout.putConstraint(SpringLayout.VERTICAL_CENTER, button, +16, SpringLayout.VERTICAL_CENTER, this.window.getContentPane());
		
		this.window.add(button);
		
		this.revalidateAndRedraw();
	}
	
}
