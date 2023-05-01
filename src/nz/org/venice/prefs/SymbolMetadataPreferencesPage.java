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

/**
 * Provides a preferences page for users to define symbols metadata.
 * For example, pre/posfix values, type (equity, index, etc).
 *
 * @author mhummel
 * @see IQuoteSource 
 */

package nz.org.venice.prefs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import nz.org.venice.quote.SymbolMetadata;
import nz.org.venice.ui.AbstractTableModel;
import nz.org.venice.ui.MetadataEditorDialog;
import nz.org.venice.util.Locale;

public class SymbolMetadataPreferencesPage extends JPanel implements IPreferencesPage {

	private static final long serialVersionUID = 1L;
	private JTable symbolsMetadataTable = null;
	private AbstractTableModel tableModel = null;
	private int selectedRow = -1;

	private MetadataEditorDialog editDialog;

	private List<SymbolMetadata> symbolsMetadata;

	private JButton addButton;
	private JButton deleteButton;
	private JButton editButton;

	private static final int SYMBOL_COLUMN = 0;
	private static final int PREFIX_COLUMN = 1;
	private static final int POSFIX_COLUMN = 2;
	private static final int TYPE_COLUMN = 3;
	private static final int NAME_COLUMN = 4;
	private static final int SYNC_ID_COLUMN = 5;

	final String[] names = { 
			Locale.getString("STOCK"),
			Locale.getString("PREFIX"),
			Locale.getString("POSFIX"),
			Locale.getString("TYPE"),
			Locale.getString("NAME"),
			Locale.getString("SYNC_INTRA_DAY"),
			};

	public SymbolMetadataPreferencesPage(JDesktopPane desktop) {
		initialize();
	}

	public JComponent getComponent() {
		return this;
	}

	public void save() {
		// try {
		// 	  PreferencesManager.putSymbolMetadata(indexSymbols);
		// } catch (PreferencesException e) {
		// }
	}

	public String getTitle() {
		return Locale.getString("SYMBOLS_METADATA_TITLE");
	}

	/**
	 * Initialise this object, creating panels for table and buttons.
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setLayout(new BorderLayout());
		this.setSize(300, 200);
		this.add(getTablePanel(), java.awt.BorderLayout.CENTER);
		this.add(getButtonPanel(), java.awt.BorderLayout.SOUTH);
	}

	/**
	 * This method initializes the table panel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getTablePanel() {
		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new BorderLayout());
		tablePanel.add(getJScrollPane(), BorderLayout.CENTER);
		return tablePanel;
	}

	/**
	 * This method initializes jScrollPane
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		JScrollPane jScrollPane = new JScrollPane(getMetadataTable());
		return jScrollPane;
	}

	/**
	 * This method initializes jTable
	 * 
	 * @return javax.swing.JTable
	 */
	private JTable getMetadataTable() {
		// Set up the data
		try {
			symbolsMetadata = PreferencesManager.getSymbolsMetadata();
		} catch (PreferencesException e) {

		}

		// Define the table model

		tableModel = new AbstractTableModel() {

			public int getColumnCount() {
				return names.length;
			}

			public int getRowCount() {
				return symbolsMetadata.size();
			}

			public boolean isCellEditable(int row, int col) {
				return true;
			}

			public String getColumnName(int column) {
				return names[column];
			}

			public Class getColumnClass(int c) {
				return getValueAt(0, c).getClass();
			}
			
			public Object getValueAt(int row, int col) {
				SymbolMetadata sm = (SymbolMetadata) symbolsMetadata.get(row);
				switch (col) {
				case SYMBOL_COLUMN:
					return sm.getSymbol();
				case PREFIX_COLUMN:
					return sm.getPrefix();
				case POSFIX_COLUMN:
					return sm.getPosfix();
				case TYPE_COLUMN:
					return sm.getType();
				case NAME_COLUMN:
					return sm.getName();
				case SYNC_ID_COLUMN:
					return sm.syncIntraDay();

				default:
					assert false;
				}
				return null;
			}

		};

		;

		symbolsMetadataTable = new JTable(tableModel);
		symbolsMetadataTable.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				selectedRow = symbolsMetadataTable.getSelectedRow();
				if (selectedRow != -1) {
					deleteButton.setEnabled(true);
					editButton.setEnabled(true);
				} else {
					deleteButton.setEnabled(false);
					editButton.setEnabled(true);
				}
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}
		});

		return symbolsMetadataTable;
	}

	/**
	 * This method creates the button panel
	 * 
	 * @return a panel containing the buttons.
	 */
	private JPanel getButtonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(getAddButton(), null);
		buttonPanel.add(getEditButton(), null);
		buttonPanel.add(getDeleteButton(), null);
		return buttonPanel;
	}

	/**
	 * This method creates the edit button
	 * 
	 * @return edit button
	 */
	private JButton getEditButton() {
		editButton = new JButton();
		editButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedRow = symbolsMetadataTable.getSelectedRow();
				editSymbolMetadata();
			}
		});
		editButton.setText(Locale.getString("EDIT"));
		editButton.setEnabled(false);

		return editButton;
	}

	/**
	 * This method creates the add button
	 * 
	 * @return add button
	 */
	private JButton getAddButton() {
		addButton = new JButton();
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedRow = -1;
				editSymbolMetadata();
			}
		});
		addButton.setText(Locale.getString("ADD"));
		addButton.setEnabled(true);

		return addButton;
	}

	/**
	 * This method initializes the delete button
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getDeleteButton() {
		deleteButton = new JButton();
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (selectedRow == -1) {
					return;
				}
				SymbolMetadata sm = (SymbolMetadata) symbolsMetadata.get(selectedRow);

				int response = JOptionPane.showInternalConfirmDialog(deleteButton,
						Locale.getString("SURE_DELETE_SYMBOL", sm.getSymbol().toString()),
						Locale.getString("DELETE_METADATA_TITLE"),

						JOptionPane.YES_NO_OPTION);

				if (response == JOptionPane.YES_OPTION) {
					symbolsMetadata.remove(selectedRow);
					try {
						PreferencesManager.deleteSymbolMetada(sm);
					} catch (PreferencesException prefsException) {

					}
					selectedRow = -1;
					tableModel.fireTableDataChanged();
				}
			}
		});
		deleteButton.setText(Locale.getString("DELETE"));

		deleteButton.setEnabled(false);
		return deleteButton;
	}

	private void editSymbolMetadata() {
		editDialog = (selectedRow != -1) ? new MetadataEditorDialog((SymbolMetadata) symbolsMetadata.get(selectedRow))
				: new MetadataEditorDialog();

		editDialog.addInternalFrameListener(new InternalFrameListener() {
			public void internalFrameClosed(InternalFrameEvent ife) {
				if (editDialog.okClicked()) {
					SymbolMetadata new_sm = editDialog.getSymbolMetadata();

					if (selectedRow != -1) {
						symbolsMetadata.remove(selectedRow);
						symbolsMetadata.add(new_sm);
					} else {
						symbolsMetadata.add(new_sm);
					}
					tableModel.fireTableDataChanged();
					try {
						PreferencesManager.putSymbolMetadata(new_sm);
					} catch (PreferencesException e) {

					}
				}
			}

			public void internalFrameActivated(InternalFrameEvent arg0) {
			}

			public void internalFrameClosing(InternalFrameEvent e) {
			}

			public void internalFrameDeactivated(InternalFrameEvent e) {
			}

			public void internalFrameDeiconified(InternalFrameEvent arg0) {
			}

			public void internalFrameIconified(InternalFrameEvent arg0) {
			}

			public void internalFrameOpened(InternalFrameEvent arg0) {
			}
		});
	}

}
