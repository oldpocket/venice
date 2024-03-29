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

package nz.org.venice.portfolio;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import nz.org.venice.main.IModule;
import nz.org.venice.main.ModuleFrame;
import nz.org.venice.prefs.settings.ISettings;
import nz.org.venice.prefs.settings.TransactionModuleSettings;
import nz.org.venice.ui.AbstractTable;
import nz.org.venice.ui.AbstractTableModel;
import nz.org.venice.ui.Column;
import nz.org.venice.ui.ConfirmDialog;
import nz.org.venice.ui.MenuHelper;
import nz.org.venice.util.Locale;
import nz.org.venice.util.Money;
import nz.org.venice.util.TradingDate;

/**
 * Venice module for displaying a portfolio's transaction history to the user.
 */
public class TransactionModule extends AbstractTable implements IModule, ActionListener {

	// Column ennumeration
	private static final int DATE_COLUMN = 0;
	private static final int TRANSACTION_COLUMN = 1;
	private static final int CREDIT_COLUMN = 2;
	private static final int DEBIT_COLUMN = 3;

	// Menu items
	private JMenuBar menuBar;

	private JMenuItem transactionNew;
	private JMenuItem transactionEdit;
	private JMenuItem transactionDelete;
	private JMenuItem transactionClose;

	// Popup menu items
	private JMenuItem popupTransactionNew;
	private JMenuItem popupTransactionEdit;
	private JMenuItem popupTransactionDelete;

	private PropertyChangeSupport propertySupport;
	private PortfolioModule portfolioModule;
	private Portfolio portfolio;
	private Model model;
	private TransactionModuleSettings settings;

	class Model extends AbstractTableModel {

		private List transactions;

		public Model(List columns, List transactions) {
			super(columns);
			this.transactions = transactions;
		}

		public void setTransactions(List transactions) {
			this.transactions = transactions;
			fireTableDataChanged();
		}

		public Transaction getTransactionAtRow(int row) {
			return (Transaction) transactions.get(row);
		}

		public List getTransactions() {
			return transactions;
		}

		public int getRowCount() {
			return transactions.size();
		}

		public Object getValueAt(int row, int column) {

			if (row >= getRowCount())
				return "";

			Transaction transaction = (Transaction) transactions.get(row);
			int type = transaction.getType();

			switch (column) {

			case (DATE_COLUMN):
				return transaction.getDate();

			case (TRANSACTION_COLUMN):
				return getTransactionString(transaction);

			case (CREDIT_COLUMN):
				// Portfolio gains money
				switch (type) {
				case (Transaction.DEPOSIT):
				case (Transaction.DIVIDEND):
				case (Transaction.INTEREST):
				case (Transaction.TRANSFER):
					return transaction.getAmount();
				}

				return Money.ZERO;

			case (DEBIT_COLUMN):
				// Portfolio loses money
				switch (type) {
				case (Transaction.WITHDRAWAL):
				case (Transaction.FEE):
				case (Transaction.TRANSFER):
					return transaction.getAmount();
				case (Transaction.ACCUMULATE):
				case (Transaction.REDUCE):
					return transaction.getTradeCost();
				}

				return Money.ZERO;
			}

			return "";
		}
	}

	// Get the string to display in the transaction column
	private String getTransactionString(Transaction transaction) {
		int type = transaction.getType();

		String transactionString = Transaction.typeToString(type);

		// Add additional information here
		switch (type) {
		case (Transaction.ACCUMULATE):
		case (Transaction.REDUCE):
			Money pricePerShare = transaction.getAmount().divide(transaction.getShares());

			transactionString = transactionString
					.concat(" " + transaction.getShares() + " " + transaction.getSymbol() + " @ " + pricePerShare);
			break;
		case (Transaction.DIVIDEND):
			transactionString = transactionString.concat(" " + transaction.getSymbol());
			break;

		case (Transaction.DIVIDEND_DRP):
			transactionString = transactionString.concat(" " + transaction.getShares() + " " + transaction.getSymbol());
			break;

		case (Transaction.TRANSFER):
			transactionString = transactionString
					.concat(" " + Locale.getString("FROM") + " " + transaction.getCashAccount().getName() + " "
							+ Locale.getString("TO") + " " + transaction.getCashAccount2().getName());

			break;
		}

		return transactionString;
	}

	/**
	 * Create a new transaction module from the given portfolio.
	 *
	 * @param portfolio portfolio to display transaction history
	 */
	public TransactionModule(final PortfolioModule portfolioModule, final Portfolio portfolio) {

		List columns = new ArrayList();
		columns.add(new Column(DATE_COLUMN, Locale.getString("DATE"), Locale.getString("DATE"), TradingDate.class,
				Column.VISIBLE));
		columns.add(new Column(TRANSACTION_COLUMN, Locale.getString("TRANSACTION"), Locale.getString("TRANSACTION"),
				String.class, Column.VISIBLE));
		columns.add(new Column(CREDIT_COLUMN, Locale.getString("CREDIT"), Locale.getString("CREDIT"), Money.class,
				Column.VISIBLE));
		columns.add(new Column(CREDIT_COLUMN, Locale.getString("DEBIT"), Locale.getString("DEBIT"), Money.class,
				Column.VISIBLE));

		this.portfolioModule = portfolioModule;
		this.portfolio = portfolio;

		model = new Model(columns, portfolio.getTransactions());
		setModel(model);

		propertySupport = new PropertyChangeSupport(this);

		// If the user double clicks on a row then edit that transaction
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				handleMouseClicked(evt);
			}
		});

		// Listen for changes in selection so we can update the menus
		getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				checkMenuDisabledStatus();
			}

		});

		createMenu();
	}

	// If the user double clicks on a transaction with the LMB, edit the
	// transaction.
	// If the user right clicks over the table, open up a popup menu.
	private void handleMouseClicked(MouseEvent event) {

		Point point = event.getPoint();

		// Right click on the table - raise menu
		if (event.getButton() == MouseEvent.BUTTON3) {
			JPopupMenu menu = new JPopupMenu();

			popupTransactionNew = MenuHelper.addMenuItem(this, menu, Locale.getString("NEW"));
			popupTransactionEdit = MenuHelper.addMenuItem(this, menu, Locale.getString("EDIT"));
			popupTransactionDelete = MenuHelper.addMenuItem(this, menu, Locale.getString("DELETE"));

			int numberOfSelectedRows = getSelectedRowCount();

			popupTransactionEdit.setEnabled(numberOfSelectedRows == 1 ? true : false);
			popupTransactionDelete.setEnabled(numberOfSelectedRows > 0 ? true : false);

			menu.show(this, point.x, point.y);
		}

		// Left double click on the table - edit transaction
		else if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 2) {
			int row = getUnsortedRow(rowAtPoint(point));

			// Get transaction at row
			Transaction transaction = model.getTransactionAtRow(row);

			editTransaction(transaction);
		}
	}

	// Create new menu for this module
	private void createMenu() {
		menuBar = new JMenuBar();

		JMenu transactionMenu = MenuHelper.addMenu(menuBar, Locale.getString("TRANSACTION"), 'T');
		{
			transactionNew = MenuHelper.addMenuItem(this, transactionMenu, Locale.getString("NEW"));

			transactionEdit = MenuHelper.addMenuItem(this, transactionMenu, Locale.getString("EDIT"));

			transactionDelete = MenuHelper.addMenuItem(this, transactionMenu, Locale.getString("DELETE"));

			transactionMenu.addSeparator();

			transactionClose = MenuHelper.addMenuItem(this, transactionMenu, Locale.getString("CLOSE"));
		}

		checkMenuDisabledStatus();
	}

	// Edit & Delete menu items are only enabled when items are selected in the
	// table.
	private void checkMenuDisabledStatus() {
		int numberOfSelectedRows = getSelectedRowCount();

		transactionEdit.setEnabled(numberOfSelectedRows == 1 ? true : false);
		transactionDelete.setEnabled(numberOfSelectedRows > 0 ? true : false);
	}

	/**
	 * Redraw and redisplay table
	 */
	public void redraw() {
		resort();
		revalidate();
		repaint();
	}

	public void save() {
		settings = new TransactionModuleSettings(portfolio.getName());
	}

	public String getTitle() {
		return Locale.getString("TRANSACTION_HISTORY_TITLE", portfolio.getName());
	}

	/**
	 * Add a property change listener for module change events.
	 *
	 * @param listener listener
	 */
	public void addModuleChangeListener(PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(listener);
	}

	/**
	 * Remove a property change listener for module change events.
	 *
	 * @param listener listener
	 */
	public void removeModuleChangeListener(PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(listener);
	}

	/**
	 * Return frame icon for table module.
	 *
	 * @return the frame icon.
	 */
	public ImageIcon getFrameIcon() {
		return null;
	}

	/**
	 * Return displayed component for this module.
	 *
	 * @return the component to display.
	 */
	public JComponent getComponent() {
		return this;
	}

	/**
	 * Return menu bar for chart module.
	 *
	 * @return the menu bar.
	 */
	public JMenuBar getJMenuBar() {
		return menuBar;
	}

	/**
	 * Return whether the module should be enclosed in a scroll pane.
	 *
	 * @return enclose module in scroll bar
	 */
	public boolean encloseInScrollPane() {
		return true;
	}

	// Edit the given transaction
	private void editTransaction(final Transaction transaction) {

		// Handle action in a separate thread so we dont
		// hold up the dispatch thread. See O'Reilley Swing pg 1138-9.
		Thread showEditDialog = new Thread() {

			public void run() {
				JDesktopPane desktop = nz.org.venice.ui.DesktopManager.getDesktop();
				TransactionDialog dialog = new TransactionDialog(desktop, portfolio);

				if (dialog.editTransaction(transaction)) {

					SwingUtilities.invokeLater(new Runnable() {
						public void run() {

							// If the transaction was changed then redraw the table
							model.setTransactions(portfolio.getTransactions());

							// Update the table and the portfolio window
							redraw();

							portfolioModule.redraw();
						}
					});
				}
			}
		};

		showEditDialog.start();
	}

	// Delete the given transaction(s)
	private void deleteTransactions(final List deleteTransactions) {
//	JDesktopPane desktop =
//	    nz.org.venice.ui.DesktopManager.getDesktop();

//	int option = 
//	    JOptionPane.showInternalConfirmDialog(desktop,
//						  Locale.getString("SURE_DELETE_TRANSACTIONS"),
//						  Locale.getString("DELETE_TRANSACTIONS"),
//						  JOptionPane.YES_NO_OPTION);
//	if(option == JOptionPane.YES_OPTION) {
		JDesktopPane desktop = nz.org.venice.ui.DesktopManager.getDesktop();

		ConfirmDialog dialog = new ConfirmDialog(desktop, Locale.getString("SURE_DELETE_TRANSACTIONS"),
				Locale.getString("DELETE_TRANSACTIONS"));
		boolean option = dialog.showDialog();
		if (option) {

			// Update display
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {

					// Delete transactions
					List transactions = new ArrayList(portfolio.getTransactions());

					// Remove deleted transactions from the list
					Iterator iterator = transactions.iterator();
					while (iterator.hasNext()) {
						Transaction traverseTransaction = (Transaction) iterator.next();

						if (deleteTransactions.contains(traverseTransaction))
							iterator.remove();
					}

					// Remove and re-add transactions to portfolio
					portfolio.removeAllTransactions();
					portfolio.addTransactions(transactions);

					// If the transaction was changed then redraw the table
					model.setTransactions(portfolio.getTransactions());

					// Update the table and the portfolio window
					redraw();
					portfolioModule.redraw();

					// Selected elements no longer exist so remove them
					clearSelection();
				}
			});
		}
	}

	// Create a new transaction
	private void newTransaction() {

		// Call PortfolioModule's routine to create a new transaction.
		portfolioModule.newTransaction();
	}

	/**
	 * Handle widget events.
	 *
	 * @param e action event
	 */
	public void actionPerformed(final ActionEvent e) {

		// Handle all menu actions in a separate thread so we dont
		// hold up the dispatch thread. See O'Reilley Swing pg 1138-9.
		Thread menuAction = new Thread() {

			public void run() {
				if (e.getSource() == transactionNew
						|| (popupTransactionNew != null && e.getSource() == popupTransactionNew)) {

					newTransaction();
				}

				else if (e.getSource() == transactionEdit
						|| (popupTransactionEdit != null && e.getSource() == popupTransactionEdit)) {

					Transaction transaction = model.getTransactionAtRow(getSelectedRow());

					editTransaction(transaction);
				}

				else if (e.getSource() == transactionDelete
						|| (popupTransactionDelete != null && e.getSource() == popupTransactionDelete)) {

					int[] selectedRows = getSelectedRows();
					List transactions = new ArrayList();

					for (int i = 0; i < selectedRows.length; i++) {
						transactions.add(model.getTransactionAtRow(selectedRows[i]));
					}

					deleteTransactions(transactions);
				}

				else if (e.getSource() == transactionClose) {
					propertySupport.firePropertyChange(ModuleFrame.WINDOW_CLOSE_PROPERTY, 0, 1);
				}

				else
					assert false;
			}
		};

		menuAction.start();
	}

	public ISettings getSettings() {
		return settings;
	}
}
