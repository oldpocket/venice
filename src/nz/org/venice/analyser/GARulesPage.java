/*
 * Merchant of Venice - technical analysis software for the stock market.
 * Copyright (C) 2002 Andrew Leppard (aleppard@picknowl.com.au)
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */

/**
 *
 * @author  Alberto Nacher
 */

package nz.org.venice.analyser;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneLayout;
import javax.swing.border.TitledBorder;

import nz.org.venice.analyser.ga.GAIndividual;
import nz.org.venice.parser.IExpression;
import nz.org.venice.parser.ExpressionException;
import nz.org.venice.parser.ImplicitVariables;
import nz.org.venice.parser.Parser;
import nz.org.venice.parser.Token;
import nz.org.venice.parser.Variable;
import nz.org.venice.parser.Variables;
import nz.org.venice.prefs.PreferencesManager;
import nz.org.venice.ui.ExpressionComboBox;
import nz.org.venice.ui.GridBagHelper;
import nz.org.venice.util.Locale;

public class GARulesPage extends Page implements IAnalyserPage {

	// Swing components
	private JCheckBox ruleFamilyEnabledCheckBox;
	private ExpressionComboBox buyRuleExpressionComboBox;
	private ExpressionComboBox sellRuleExpressionComboBox;
	private JTextField parameterTextField;
	private JTextField minValueTextField;
	private JTextField maxValueTextField;
	private JButton addParameterButton;

	// Parsed input
	private IExpression buyRule;
	private IExpression sellRule;

	// Parameters Table
	private GARulesPageModule GARulesPageModule;

	/**
	 * Construct a new rules page.
	 *
	 * @param desktop the desktop
	 */
	public GARulesPage(JDesktopPane desktop, double maxHeight) {

		Dimension preferredSize = new Dimension();
		preferredSize.setSize(this.getPreferredSize().getWidth(), maxHeight / 2);

		this.desktop = desktop;
		this.GARulesPageModule = new GARulesPageModule(desktop);
		setGraphic(preferredSize);

	}

	public void load(String key) {

		String idStr = "Parameters";

		// Load last GUI settings from preferences
		HashMap settings = PreferencesManager.getAnalyserPageSettings(key + getClass().getName());

		Iterator iterator = settings.keySet().iterator();

		while (iterator.hasNext()) {
			String setting = (String) iterator.next();
			String value = (String) settings.get((Object) setting);

			if (setting.equals("buy_rule"))
				buyRuleExpressionComboBox.setExpressionText(value);
			else if (setting.equals("sell_rule"))
				sellRuleExpressionComboBox.setExpressionText(value);
		}

		HashMap settingsParam = PreferencesManager.getAnalyserPageSettings(key + idStr);

		Iterator iteratorParam = settingsParam.keySet().iterator();

		while (iteratorParam.hasNext()) {
			String settingParam = (String) iteratorParam.next();
			String valueParam = (String) settingsParam.get((Object) settingParam);

			GARulesPageModule.load(valueParam);
		}

	}

	public void save(String key) {
		String idStr = "Parameters";

		HashMap settingsParam = PreferencesManager.getAnalyserPageSettings(key + idStr);
		HashMap settings = new HashMap();

		GARulesPageModule.save(settingsParam, idStr);
		settings.put("buy_rule", buyRuleExpressionComboBox.getExpressionText());
		settings.put("sell_rule", sellRuleExpressionComboBox.getExpressionText());

		PreferencesManager.putAnalyserPageSettings(key + idStr, settingsParam);
		PreferencesManager.putAnalyserPageSettings(key + getClass().getName(), settings);
	}

	public boolean parse() {

		if (!checkNumberFormat())
			return false;

		if (!checkParameters())
			return false;

		// We need to specify the variables that are given to the buy/sell rule
		// expressions so they can be parsed properly.
		Variables variables = new Variables();

		ImplicitVariables.getInstance().setup(variables, true);

		String buyRuleString = buyRuleExpressionComboBox.getExpressionText();
		String sellRuleString = sellRuleExpressionComboBox.getExpressionText();

		// Insert all the parameters in variables.
		// We use lowestGAIndividual, but highestGAIndividual should be the same.
		// All the GAIndividual have the same parameters during all GA Algorithm,
		// they just differ one from another because of the values.
		GAIndividual lowestGAIndividual = this.getLowestIndividual();
		for (int ii = 0; ii < lowestGAIndividual.size(); ii++)
			variables.add(lowestGAIndividual.parameter(ii), lowestGAIndividual.type(ii), Variable.CONSTANT);

		if (buyRuleString.length() == 0) {
			showErrorMessage(Locale.getString("MISSING_BUY_RULE"), Locale.getString("ERROR_PARSING_RULES"));

			return false;
		}

		if (sellRuleString.length() == 0) {
			showErrorMessage(Locale.getString("MISSING_SELL_RULE"), Locale.getString("ERROR_PARSING_RULES"));

			return false;
		}

		try {
			Variables tmpVar = null;
			try {
				tmpVar = (Variables) variables.clone();
			} catch (CloneNotSupportedException e) {
			}
			buyRule = Parser.parse(tmpVar, buyRuleString);
		} catch (ExpressionException e) {
			buyRule = null;
			showErrorMessage(Locale.getString("ERROR_PARSING_BUY_RULE", e.getReason()),
					Locale.getString("ERROR_PARSING_RULES"));

			return false;
		}

		try {
			Variables tmpVar = null;
			try {
				tmpVar = (Variables) variables.clone();
			} catch (CloneNotSupportedException e) {
			}
			sellRule = Parser.parse(tmpVar, sellRuleString);
		} catch (ExpressionException e) {
			sellRule = null;
			showErrorMessage(Locale.getString("ERROR_PARSING_SELL_RULE", e.getReason()),
					Locale.getString("ERROR_PARSING_RULES"));

			return false;
		}

		return true;
	}

	public JComponent getComponent() {
		return this;
	}

	public String getTitle() {
		return Locale.getString("RULES_PAGE_TITLE");
	}

	/**
	 * Return the parsed buy rule expression.
	 *
	 * @return the buy rule
	 */
	public IExpression getBuyRule() {
		return buyRule;
	}

	/**
	 * Return the parsed sell rule expression.
	 *
	 * @return the sell rule
	 */
	public IExpression getSellRule() {
		return sellRule;
	}

	/**
	 * Return the GAIndividual with the lowest parameters.
	 *
	 * @return the individual
	 */
	public GAIndividual getLowestIndividual() {
		int sizeOfIndividual = GARulesPageModule.getRowCount();
		String[] parameters = new String[sizeOfIndividual];
		double[] values = new double[sizeOfIndividual];
		int[] types = new int[sizeOfIndividual];
		for (int ii = 0; ii < sizeOfIndividual; ii++) {
			parameters[ii] = (String) GARulesPageModule.getValueAt(ii, GARulesPageModule.PARAMETER_COLUMN);
			String value = (String) GARulesPageModule.getValueAt(ii, GARulesPageModule.MIN_PARAMETER_COLUMN);
			try {
				values[ii] = Double.valueOf(value.trim()).doubleValue();
			} catch (NumberFormatException nfe) {
				// it should never happen, because the numbers are already checked in input.
				return null;
			}
			// if there is a full stop in the number string,
			// interpret it as a FLOAT number
			if (value.indexOf('.') == -1) {
				types[ii] = IExpression.INTEGER_TYPE;
			} else {
				types[ii] = IExpression.FLOAT_TYPE;
			}
		}
		GAIndividual retValue = new GAIndividual(parameters, values, types);
		return retValue;
	}

	/**
	 * Return the GAIndividual with the highest parameters.
	 *
	 * @return the individual
	 */
	public GAIndividual getHighestIndividual() {
		int sizeOfIndividual = GARulesPageModule.getRowCount();
		String[] parameters = new String[sizeOfIndividual];
		double[] values = new double[sizeOfIndividual];
		int[] types = new int[sizeOfIndividual];
		for (int ii = 0; ii < sizeOfIndividual; ii++) {
			parameters[ii] = (String) GARulesPageModule.getValueAt(ii, GARulesPageModule.PARAMETER_COLUMN);
			String value = (String) GARulesPageModule.getValueAt(ii, GARulesPageModule.MAX_PARAMETER_COLUMN);
			try {
				values[ii] = Double.valueOf(value.trim()).doubleValue();
			} catch (NumberFormatException nfe) {
				// it should never happen, because the numbers are already checked in input.
				return null;
			}
			// if there is a full stop in the number string,
			// interpret it as a FLOAT number
			if (value.indexOf('.') == -1) {
				types[ii] = IExpression.INTEGER_TYPE;
			} else {
				types[ii] = IExpression.FLOAT_TYPE;
			}
		}
		GAIndividual retValue = new GAIndividual(parameters, values, types);
		return retValue;
	}

	private boolean checkNumberFormat() {
		int sizeOfIndividual = GARulesPageModule.getRowCount();
		String[] parameters = new String[sizeOfIndividual];
		double[] values = new double[sizeOfIndividual];
		int[] types = new int[sizeOfIndividual];
		for (int ii = 0; ii < sizeOfIndividual; ii++) {
			parameters[ii] = (String) GARulesPageModule.getValueAt(ii, GARulesPageModule.PARAMETER_COLUMN);
			String str = null;
			double dbl = 0.0D;
			try {
				// Control if right numbers (minimum and maximum parameters' bounds)
				// are inserted
				str = (String) GARulesPageModule.getValueAt(ii, GARulesPageModule.MIN_PARAMETER_COLUMN);
				dbl = Double.valueOf(str).doubleValue();
				str = (String) GARulesPageModule.getValueAt(ii, GARulesPageModule.MAX_PARAMETER_COLUMN);
				dbl = Double.valueOf(str).doubleValue();
			} catch (NumberFormatException nfe) {
				showErrorMessage(Locale.getString("ERROR_PARSING_NUMBER", str),
						Locale.getString("ERROR_PARSING_RULES"));
				return false;
			}
		}
		return true;
	}

	private boolean checkParameters() {
		// Get words of Gondola lexicon
		String[] words = Token.wordsOfGondola();

		// Get parameters defined by user in GA
		int sizeOfIndividual = GARulesPageModule.getRowCount();
		String[] parameters = new String[sizeOfIndividual];

		// Check if there is at least one parameter that is a substring or equal to
		// one of the words of Gondola lexicon.
		// This prevent syntax errors in managing the rules.
		for (int ii = 0; ii < sizeOfIndividual; ii++) {
			parameters[ii] = (String) GARulesPageModule.getValueAt(ii, GARulesPageModule.PARAMETER_COLUMN);

			// Check that the user can't add implicit variables
			if (ImplicitVariables.getInstance().contains(parameters[ii])) {
				showErrorMessage(Locale.getString("VARIABLE_DEFINED_ERROR", parameters[ii], parameters[ii]),
						Locale.getString("VARIABLE_DEFINED_ERROR"));
				return false;
			}

			for (int jj = 0; jj < words.length; jj++) {
				if (words[jj].indexOf(parameters[ii]) >= 0) {
					showErrorMessage(Locale.getString("ERROR_PARSING_PARAMETER", parameters[ii], words[jj]),
							Locale.getString("ERROR_PARSING_RULES"));
					return false;
				}
			}
		}
		return true;
	}

	private void addParameter() {
		GARulesPageModule.addParameter();
	}

	private void editParameter() {
		GARulesPageModule.editParameter();
	}

	private void deleteParameter() {
		GARulesPageModule.removeSelectedResults();
	}

	private void setGraphic(Dimension preferredSize) {

		GridBagLayout gridbag = new GridBagLayout();

		TitledBorder expressionTitled = new TitledBorder(Locale.getString("RULES_PAGE_TITLE"));
		this.setBorder(expressionTitled);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setPreferredSize(preferredSize);

		JPanel panelUp = new JPanel();
		JPanel panelDown = new JPanel();
		panelUp.setLayout(new BoxLayout(panelUp, BoxLayout.Y_AXIS));
		panelDown.setLayout(new BoxLayout(panelDown, BoxLayout.Y_AXIS));
		TitledBorder titledBorderSectionUp = new TitledBorder(Locale.getString("RULES_PAGE_TITLE"));
		TitledBorder titledBorderSectionDown = new TitledBorder(Locale.getString("PARAMETERS"));
		panelUp.setBorder(titledBorderSectionUp);
		panelDown.setBorder(titledBorderSectionDown);

		JPanel innerPanel = new JPanel();
		GridBagConstraints c = new GridBagConstraints();
		innerPanel.setLayout(gridbag);

		c.weightx = 1.0;
		c.ipadx = 5;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;

		buyRuleExpressionComboBox = GridBagHelper.addExpressionRow(innerPanel, Locale.getString("BUY_RULE"), "",
				gridbag, c);
		sellRuleExpressionComboBox = GridBagHelper.addExpressionRow(innerPanel, Locale.getString("SELL_RULE"), "",
				gridbag, c);

		// GARulesPageModule is already declared as global variable
		GARulesPageModule.setLayout(new BoxLayout(GARulesPageModule, BoxLayout.Y_AXIS));

		JScrollPane upDownScrollPane = new JScrollPane(GARulesPageModule);
		upDownScrollPane.setLayout(new ScrollPaneLayout());

		JButton addButton = new JButton(Locale.getString("ADD"));
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				addParameter();
			}
		});

		JButton editButton = new JButton(Locale.getString("EDIT"));
		editButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				editParameter();
			}
		});

		JButton deleteButton = new JButton(Locale.getString("DELETE"));
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				deleteParameter();
			}
		});

		JPanel rulesButtons = new JPanel();
		rulesButtons.add(addButton);
		rulesButtons.add(editButton);
		rulesButtons.add(deleteButton);

		panelUp.add(innerPanel);

		panelDown.add(upDownScrollPane);
		rulesButtons.setAlignmentX(CENTER_ALIGNMENT);
		panelDown.add(rulesButtons);

		this.add(panelUp);
		this.add(panelDown);
	}

}
