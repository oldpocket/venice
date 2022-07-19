package nz.org.venice.main;

import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuBar;

public interface AnalyserModule {
	public String getTitle();

	public void addModuleChangeListener(PropertyChangeListener listener);

	public void removeModuleChangeListener(PropertyChangeListener listener);

	public ImageIcon getFrameIcon();

	public JComponent getComponent();

	public JMenuBar getJMenuBar();

	public JMenuBar getJToolBar();

	public boolean encloseInScrollPane();

	public void save();
}
