/* Merchant of Venice - technical analysis software for the stock market.
   Copyright (C) 2002 Andrew Leppard (aleppard@picknowl.com.au)

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package nz.org.venice.analyser;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import nz.org.venice.main.CommandManager;
import nz.org.venice.main.IModule;
import nz.org.venice.main.ModuleFrame;
import nz.org.venice.prefs.settings.AnalyserResultSettings;
import nz.org.venice.prefs.settings.ISettings;
import nz.org.venice.ui.AbstractTable;
import nz.org.venice.ui.AbstractTableModel;
import nz.org.venice.ui.ChangeFormat;
import nz.org.venice.ui.Column;
import nz.org.venice.ui.EditorDialog;
import nz.org.venice.ui.MenuHelper;
import nz.org.venice.util.Locale;
import nz.org.venice.util.Money;
import nz.org.venice.util.TradingDate;

public class ANNResultModule extends AbstractTable implements IModule {
	private PropertyChangeSupport propertySupport;

	private static final int START_DATE_COLUMN = 0;
	private static final int END_DATE_COLUMN = 1;
	private static final int SYMBOLS_COLUMN = 2;
	private static final int TRADE_COST_COLUMN = 3;
	private static final int NUMBER_OF_TRADES_COLUMN = 4;
	private static final int INITIAL_CAPITAL_COLUMN = 5;
	private static final int FINAL_CAPITAL_COLUMN = 6;
	private static final int PERCENT_RETURN_COLUMN = 7;

	private Model model;
	private AnalyserResultSettings settings;

	// Menus
	private JMenuBar menuBar;
	private JMenuItem openMenuItem;
	private JMenuItem graphMenuItem;
	private JMenuItem transactionsMenuItem;
	private JMenuItem getTipMenuItem;
	private JMenuItem removeMenuItem;
	private JMenuItem removeAllMenuItem;

	private JDesktopPane desktop;

	private class Model extends AbstractTableModel {
		private List results;

		public Model(List columns) {
			super(columns);
			results = new ArrayList();
		}

		public ANNResult getResult(int row) {
			return (ANNResult) results.get(row);
		}

		public void removeAllResults() {
			results.clear();

			// Notify table that the whole data has changed
			fireTableDataChanged();
		}

		public List getResults() {
			return results;
		}

		public void setResults(List results) {
			this.results = results;

			// Notify table that the whole data has changed
			fireTableDataChanged();
		}

		public void addResults(List results) {
			this.results.addAll(results);

			// Notify table that the whole data has changed
			fireTableDataChanged();
		}

		public int getRowCount() {
			return results.size();
		}

		public Object getValueAt(int row, int column) {
			if (row >= getRowCount())
				return "";

			ANNResult result = (ANNResult) results.get(row);

			if (column == START_DATE_COLUMN)
				return result.getStartDate();

			else if (column == END_DATE_COLUMN)
				return result.getEndDate();

			else if (column == SYMBOLS_COLUMN)
				return result.getSymbols();

			else if (column == TRADE_COST_COLUMN)
				return result.getTradeCost();

			else if (column == NUMBER_OF_TRADES_COLUMN)
				return new Integer(result.getNumberTrades());

			else if (column == FINAL_CAPITAL_COLUMN)
				return result.getFinalCapital();

			else if (column == INITIAL_CAPITAL_COLUMN)
				return result.getInitialCapital();

			else if (column == PERCENT_RETURN_COLUMN)
				return new ChangeFormat(result.getInitialCapital(), result.getFinalCapital());
			else {
				assert false;
				return "";
			}
		}
	}

	public ANNResultModule() {
		model = new Model(createColumns());
		setModel(model);

		model.addTableModelListener(this);

		propertySupport = new PropertyChangeSupport(this);

		addMenu();

		// If the user clicks on the table trap it.
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				handleMouseClicked(evt);
			}
		});

		showColumns(model);
	}

	public ANNResultModule(AnalyserResultSettings settings) {
		this.settings = settings;

		model = new Model(createColumns());
		model.setResults(settings.getResults());
		setModel(model);

		model.addTableModelListener(this);

		propertySupport = new PropertyChangeSupport(this);

		addMenu();

		// If the user clicks on the table trap it.
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				handleMouseClicked(evt);
			}
		});

		showColumns(model);
	}

	private List createColumns() {

		List columns = new ArrayList();
		columns.add(new Column(START_DATE_COLUMN, Locale.getString("START_DATE"),
				Locale.getString("START_DATE_COLUMN_HEADER"), TradingDate.class, Column.VISIBLE));
		columns.add(new Column(END_DATE_COLUMN, Locale.getString("END_DATE"),
				Locale.getString("END_DATE_COLUMN_HEADER"), TradingDate.class, Column.VISIBLE));
		columns.add(new Column(SYMBOLS_COLUMN, Locale.getString("SYMBOLS"), Locale.getString("SYMBOLS_COLUMN_HEADER"),
				String.class, Column.VISIBLE));
		columns.add(new Column(TRADE_COST_COLUMN, Locale.getString("TRADE_COST"),
				Locale.getString("TRADE_COST_COLUMN_HEADER"), Money.class, Column.HIDDEN));
		columns.add(new Column(NUMBER_OF_TRADES_COLUMN, Locale.getString("NUMBER_TRADES"),
				Locale.getString("NUMBER_TRADES_COLUMN_HEADER"), Integer.class, Column.HIDDEN));
		columns.add(new Column(INITIAL_CAPITAL_COLUMN, Locale.getString("INITIAL_CAPITAL"),
				Locale.getString("INITIAL_CAPITAL_COLUMN_HEADER"), Money.class, Column.VISIBLE));
		columns.add(new Column(FINAL_CAPITAL_COLUMN, Locale.getString("FINAL_CAPITAL"),
				Locale.getString("FINAL_CAPITAL_COLUMN_HEADER"), Money.class, Column.HIDDEN));
		columns.add(new Column(PERCENT_RETURN_COLUMN, Locale.getString("PERCENT_RETURN"),
				Locale.getString("PERCENT_RETURN_COLUMN_HEADER"), ChangeFormat.class, Column.VISIBLE));

		return columns;
	}

	public void setDesktop(JDesktopPane desktop) {
		this.desktop = desktop;
	}

	// If the user double clicks on a result with the LMB, graph the portfolio.
	// If the user right clicks over the table, open up a popup menu.
	private void handleMouseClicked(MouseEvent event) {
		Point point = event.getPoint();

		// Right click on the table - raise menu
		if (event.getButton() == MouseEvent.BUTTON3) {
			JPopupMenu menu = new JPopupMenu();

			JMenuItem popupOpenMenuItem = new JMenuItem(Locale.getString("OPEN"));
			popupOpenMenuItem.setEnabled(getSelectedRowCount() == 1);
			popupOpenMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					openSelectedResult();
				}
			});
			menu.add(popupOpenMenuItem);

			JMenuItem popupGraphMenuItem = new JMenuItem(Locale.getString("GRAPH"));
			popupGraphMenuItem.setEnabled(getSelectedRowCount() == 1);
			popupGraphMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					graphSelectedResult();
				}
			});
			menu.add(popupGraphMenuItem);

			JMenuItem popupTransactionsMenuItem = new JMenuItem(Locale.getString("TRANSACTIONS"));
			popupTransactionsMenuItem.setEnabled(getSelectedRowCount() == 1);
			popupTransactionsMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					transactionsSelectedResult();
				}
			});
			menu.add(popupTransactionsMenuItem);

			menu.addSeparator();

			JMenuItem popupGetTipMenuItem = new JMenuItem(Locale.getString("GET_TIP"));
			popupGetTipMenuItem.setEnabled(getSelectedRowCount() == 1);
			popupGetTipMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					showTip();
				}
			});
			menu.add(popupGetTipMenuItem);

			menu.addSeparator();

			JMenuItem popupRemoveMenuItem = new JMenuItem(Locale.getString("REMOVE"));
			popupRemoveMenuItem.setEnabled(getSelectedRowCount() >= 1);
			popupRemoveMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					removeSelectedResults();
					checkMenuDisabledStatus();
				}
			});
			menu.add(popupRemoveMenuItem);

			menu.show(this, point.x, point.y);
		}

		// Left double click on the table - graph portfolio
		else if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 2) {
			graphSelectedResult();
		}
	}

	// Graphs first selected result
	private void graphSelectedResult() {
		// Get result at row
		int row = getSelectedRow();

		// Don't do anything if we couldn't retrieve the selected row
		if (row != -1) {
			ANNResult result = model.getResult(row);

			CommandManager.getInstance().graphPortfolio(result.getPortfolio(), result.getQuoteBundle(),
					result.getStartDate(), result.getEndDate());
		}
	}

	// Transactions first selected result
	private void transactionsSelectedResult() {
		// Get result at row
		int row = getSelectedRow();

		// Don't do anything if we couldn't retrieve the selected row
		if (row != -1) {
			ANNResult result = model.getResult(row);

			CommandManager.getInstance().tableTransactions(result.getPortfolio(), result.getQuoteBundle());
		}
	}

	// Displays the tip for next day trading, in an alert window
	private void showTip() {
		// Get result at row
		int row = getSelectedRow();

		// Don't do anything if we couldn't retrieve the selected row
		if (row != -1) {
			final ANNResult result = model.getResult(row);

			Thread thread = new Thread(new Runnable() {
				public void run() {
					showTipWindow();
				}
			});

			thread.start();
		}
	}

	private void showTipWindow() {
		// Get result at row
		int row = getSelectedRow();

		// Don't do anything if we couldn't retrieve the selected row
		if (row != -1) {
			ANNResult result = model.getResult(row);

			EditorDialog.showViewDialog(Locale.getString("GET_TIP"), Locale.getString("SHORT_GET_TIP"),
					new String(result.getTip()));
		}
	}

	// Opens first selected result
	private void openSelectedResult() {
		// Get result at row
		int row = getSelectedRow();

		// Don't do anything if we couldn't retrieve the selected row
		if (row != -1) {
			ANNResult result = model.getResult(row);

			CommandManager.getInstance().openPortfolio(result.getPortfolio(), result.getQuoteBundle());
		}
	}

	// Removes all the selected results from the table
	private void removeSelectedResults() {

		// Get selected rows and put them in order from highest to lowest
		int[] rows = getSelectedRows();
		List rowIntegers = new ArrayList();
		for (int i = 0; i < rows.length; i++)
			rowIntegers.add(new Integer(rows[i]));

		List sortedRows = new ArrayList(rowIntegers);
		Collections.sort(sortedRows);
		Collections.reverse(sortedRows);

		// Now remove them from the results list starting from the highest row
		// to the lowest
		List results = model.getResults();
		Iterator iterator = sortedRows.iterator();

		while (iterator.hasNext()) {
			Integer rowToRemove = (Integer) iterator.next();

			results.remove(rowToRemove.intValue());
		}

		model.setResults(results);
	}

	// Some menu items are only enabled/disabled depending on what is
	// selected in the table or by the size of the table
	private void checkMenuDisabledStatus() {
		int numberOfSelectedRows = getSelectedRowCount();

		openMenuItem.setEnabled(numberOfSelectedRows == 1);
		graphMenuItem.setEnabled(numberOfSelectedRows == 1);
		transactionsMenuItem.setEnabled(numberOfSelectedRows == 1);
		getTipMenuItem.setEnabled(numberOfSelectedRows == 1);
		removeMenuItem.setEnabled(numberOfSelectedRows >= 1);
		removeAllMenuItem.setEnabled(model.getRowCount() > 0);
	}

	// Add a menu
	private void addMenu() {
		menuBar = new JMenuBar();

		JMenu resultMenu = MenuHelper.addMenu(menuBar, Locale.getString("RESULT"));

		openMenuItem = new JMenuItem(Locale.getString("OPEN"));
		openMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openSelectedResult();
			}
		});
		resultMenu.add(openMenuItem);

		graphMenuItem = new JMenuItem(Locale.getString("GRAPH"));
		graphMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				graphSelectedResult();
			}
		});
		resultMenu.add(graphMenuItem);

		transactionsMenuItem = new JMenuItem(Locale.getString("TRANSACTIONS"));
		transactionsMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				transactionsSelectedResult();
			}
		});
		resultMenu.add(transactionsMenuItem);

		resultMenu.addSeparator();

		getTipMenuItem = new JMenuItem(Locale.getString("GET_TIP"));
		getTipMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showTip();
			}
		});
		resultMenu.add(getTipMenuItem);

		resultMenu.addSeparator();

		removeMenuItem = new JMenuItem(Locale.getString("REMOVE"));
		removeMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeSelectedResults();
				checkMenuDisabledStatus();
			}
		});
		resultMenu.add(removeMenuItem);

		removeAllMenuItem = new JMenuItem(Locale.getString("REMOVE_ALL"));
		removeAllMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				model.removeAllResults();
				checkMenuDisabledStatus();
			}
		});
		resultMenu.add(removeAllMenuItem);

		resultMenu.addSeparator();

		JMenu columnMenu = createShowColumnMenu(model);
		resultMenu.add(columnMenu);

		resultMenu.addSeparator();

		JMenuItem resultCloseMenuItem = new JMenuItem(Locale.getString("CLOSE"));
		openMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// When we close, free all the results to reduce memory
				model.removeAllResults();

				propertySupport.firePropertyChange(ModuleFrame.WINDOW_CLOSE_PROPERTY, 0, 1);
			}
		});
		resultMenu.add(resultCloseMenuItem);

		// Listen for changes in selection so we can update the menus
		getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				checkMenuDisabledStatus();
			}

		});

		checkMenuDisabledStatus();
	}

	public void addResults(List results) {
		model.addResults(results);
		checkMenuDisabledStatus();
		validate();
		repaint();
	}

	public void save() {
		// Free up precious memory
		model.removeAllResults();
	}

	public String getTitle() {
		return Locale.getString("ARTIFICIAL_NEURAL_NETWORK_RESULTS_TITLE");
	}

	public void addModuleChangeListener(PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(listener);
	}

	public void removeModuleChangeListener(PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(listener);
	}

	public ImageIcon getFrameIcon() {
		return null;
	}

	public JComponent getComponent() {
		return this;
	}

	public JMenuBar getJMenuBar() {
		return menuBar;
	}

	public boolean encloseInScrollPane() {
		return true;
	}

	public ISettings getSettings() {
		return settings;
	}
}
