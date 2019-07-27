package net.termat.gsi;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.termat.geo.xmap.MapPanel2d;
import net.termat.geo.xmap.MultiTileFactoryInfo;

public class GSIBrower {
	private JFrame frame;
	private  MapPanel2d map;
	private JComboBox<String> combo;

	public GSIBrower(){
		frame=new JFrame();
		frame.setTitle("Test");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
			SwingUtilities.updateComponentTreeUI(frame);
		}catch(Exception e){
			try {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
				SwingUtilities.updateComponentTreeUI(frame);
			}catch(Exception ee){
				ee.printStackTrace();
			}
		}
		WindowAdapter wa=new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e) {
				close();
			}
		};
		frame.addWindowListener(wa);
		frame.getContentPane().setLayout(new BorderLayout());
		 map=new MapPanel2d(5,35,135);
		 frame.getContentPane().add(map,BorderLayout.CENTER);
		 frame.getContentPane().add(createJToolBar(),BorderLayout.NORTH);
	}

	private JToolBar createJToolBar(){
		JToolBar tool=new JToolBar();
		tool.setBorder(BorderFactory.createEtchedBorder());
		tool.setFloatable(false);
		tool.addSeparator();
		combo=new JComboBox<String>(MultiTileFactoryInfo.infos);
		combo.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				map.getTileInfo().setSelected(combo.getSelectedIndex());
			}
		});
		tool.add(combo);
		tool.addSeparator();
		return tool;
	}

	private void close(){
		int id=JOptionPane.showConfirmDialog(frame, "Exit?", "Info", JOptionPane.YES_NO_OPTION,JOptionPane.INFORMATION_MESSAGE);
		if(id==JOptionPane.YES_OPTION){
			frame.setVisible(false);
			System.exit(0);
		}
	}

	private void show(){
		frame.setSize(1280, 900);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);
	}

	public static void main(String[] args){
		GSIBrower app=new GSIBrower();
		app.show();
	}

}
