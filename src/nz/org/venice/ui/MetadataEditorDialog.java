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
 * Edit dialog for setting Symbol Metadata such as Indexes etc.
 * 
 * @author Mark Hummel
 */

package nz.org.venice.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import nz.org.venice.main.CommandManager;
import nz.org.venice.quote.SymbolMetadata;
import nz.org.venice.quote.SymbolMetadata.SymbolType;
import nz.org.venice.util.Locale;

public class MetadataEditorDialog extends JInternalFrame implements ActionListener, KeyListener {
	private static final long serialVersionUID = 1L;
	private boolean okClicked = false;
	private SymbolMetadata symbolMetadata = null;

	private JTextField symbolField;
	private JTextField prefixField;
	private JTextField posfixField;
	private JComboBox<?> typeCombo; 
	private JTextField nameField;
	private JCheckBox syncIDCheck;

	private JButton okButton;
	private JButton cancelButton;
	private JButton helpButton;

	public MetadataEditorDialog() {
		super();
		initialise();
	}

	public MetadataEditorDialog(SymbolMetadata symbolMetadataEdit) {
		super();

		symbolMetadata = symbolMetadataEdit;

		initialise();
	}

	public boolean okClicked() {
		return okClicked;
	}

	public SymbolMetadata getSymbolMetadata() {
		assert okClicked == true;
		return symbolMetadata;
	}

	private void initialise() {
		setSize(350, 249);
		setMaximizable(false);
		setResizable(true);
		setClosable(true);
		setTitle(Locale.getString("EDIT_METADATA"));

		this.setContentPane(getPanel());

		setVisible(true);
		DesktopManager.getDesktop().add(this);
	}

	private JPanel getPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(getTextPanel(), BorderLayout.CENTER);
		panel.add(getButtonPanel(), BorderLayout.SOUTH);

		return panel;
	}

	private JPanel getTextPanel() {
		JPanel panel = new JPanel();

		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		c.weightx = 1.0;
		c.ipadx = 5;
		c.anchor = GridBagConstraints.WEST;

		panel.setLayout(layout);

		String symbolText = (symbolMetadata != null) ? symbolMetadata.getSymbol().toString() : "";
		symbolField = GridBagHelper.addTextRow(panel, Locale.getString("STOCK"), symbolText, layout, c, 8);
		symbolField.addKeyListener(this);
		if (symbolMetadata != null) symbolField.setEnabled(false);
		symbolField.setToolTipText(Locale.getString("SYMBOL_FIELD_TOOLTIP"));
		
		String prefixText = (symbolMetadata != null) ? symbolMetadata.getPrefix() : "";
		prefixField = GridBagHelper.addTextRow(panel, Locale.getString("PREFIX"), prefixText, layout, c, 8);
		prefixField.addKeyListener(this);
		
		String posfixText = (symbolMetadata != null) ? symbolMetadata.getPosfix() : "";
		posfixField = GridBagHelper.addTextRow(panel, Locale.getString("POSFIX"), posfixText, layout, c, 8);
		posfixField.addKeyListener(this);

		SymbolType symbolType = (symbolMetadata != null) ? symbolMetadata.getType() : SymbolType.EQUITY;
		Vector<SymbolType> symbolTypes = new Vector<SymbolType>();
		symbolTypes.add(SymbolType.CRYPTO);
		symbolTypes.add(SymbolType.EQUITY);
		symbolTypes.add(SymbolType.INDEX);
		typeCombo = GridBagHelper.addComboBox(panel, Locale.getString("TYPE"), symbolTypes, layout, c);
		typeCombo.setSelectedItem(symbolType);
		typeCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (checkRequiredFieldsEntered()) {
					okButton.setEnabled(true);
				} else {
					okButton.setEnabled(false);
				}
			}
		});
		
		String nameText = (symbolMetadata != null) ? symbolMetadata.getName() : "";
		nameField = GridBagHelper.addTextRow(panel, Locale.getString("NAME"), nameText, layout, c, 8);
		nameField.setToolTipText(Locale.getString("METADATA_NAME_TOOLTIP"));
		nameField.addKeyListener(this);

		boolean syncCheckValue = (symbolMetadata != null) ? symbolMetadata.syncIntraDay() : false;
		syncIDCheck = GridBagHelper.addCheckBoxRow(panel, Locale.getString("SYNC_INTRA_DAY"), syncCheckValue, layout, c);
		syncIDCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (checkRequiredFieldsEntered()) {
					okButton.setEnabled(true);
				} else {
					okButton.setEnabled(false);
				}
			}
		});

		helpButton = GridBagHelper.addHelpButtonRow(panel, Locale.getString("METADATA_SYMBOLS_TITLE"), layout, c);
		helpButton.addActionListener(this);

		return panel;
	}

	private JPanel getButtonPanel() {
		JPanel panel = new JPanel();

		okButton = new JButton(Locale.getString("OK"));
		okButton.addActionListener(this);
		okButton.setEnabled(false);

		cancelButton = new JButton(Locale.getString("CANCEL"));
		cancelButton.addActionListener(this);

		panel.add(okButton);
		panel.add(cancelButton);

		return panel;
	}

	public void actionPerformed(ActionEvent e) {
		boolean closeDialog = false;
		if (e.getSource() == okButton) {
			
			symbolMetadata = new SymbolMetadata(
					symbolField.getText(), 
					prefixField.getText(), 
					posfixField.getText(),
					(SymbolType)typeCombo.getSelectedItem(), 
					nameField.getText(), 
					syncIDCheck.isSelected());

			okClicked = true;
			closeDialog = true;

		} else if (e.getSource() == cancelButton) {
			closeDialog = true;
		} else if (e.getSource() == helpButton) {
			CommandManager.getInstance().openHelp("Metadata Definition");
		} else {

		}

		if (closeDialog) {
			try {
				setClosed(true);
			} catch (java.beans.PropertyVetoException pve) {

			}
		}
	}

	public void keyTyped(KeyEvent e) {
		if (checkRequiredFieldsEntered()) {
			okButton.setEnabled(true);
		} else {
			okButton.setEnabled(false);
		}
	}

	public void keyPressed(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {

	}

	private boolean checkRequiredFieldsEntered() {
		if (symbolField.getText().equals("")) {
			return false;
		}
		return true;
	}

}