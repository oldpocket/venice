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

package nz.org.venice.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import javax.swing.JComboBox;

import nz.org.venice.prefs.PreferencesException;
import nz.org.venice.prefs.PreferencesManager;
import nz.org.venice.quote.EODQuoteRange;
import nz.org.venice.quote.Symbol;
import nz.org.venice.quote.SymbolFormatException;
import nz.org.venice.table.WatchScreen;
import nz.org.venice.util.Locale;

/**
 * A JComboBox which allows the user to choose from a selection of symbol lists.
 * The supported symbol lists include all ordinaries (i.e. all symbols of all
 * ordinary listed companies), all symbols or marked indices. When Venice
 * supports user stock groups or indices they can be added here.
 *
 * @author Andrew Leppard
 */
public class SymbolListComboBox extends JComboBox {

	// Drop down menu choices
	private final static String ALL_ORDINARIES = Locale.getString("ALL_ORDINARIES");
	private final static String ALL_SYMBOLS = Locale.getString("ALL_SYMBOLS");
	private final static String MARKET_INDICES = Locale.getString("MARKET_INDICES");

	/**
	 * Create a new symbol list combo box.
	 */
	public SymbolListComboBox() {
		this(new String(""));
	}

	/**
	 * Createa new symbol list combo box.
	 *
	 * @param equationText the default selection.
	 */
	public SymbolListComboBox(String equationText) {
		super();

		setEditable(true);
		updateItems();
		setSelectedItem(equationText);
	}

	/**
	 * Return the symbol list selection in terms of a {@link EODQuoteRange} object.
	 *
	 * @return quote range with selected symbol list.
	 */
	public EODQuoteRange getQuoteRange() throws SymbolFormatException {
		String text = getText();

		if (text.equals(ALL_ORDINARIES))
			return new EODQuoteRange(EODQuoteRange.ALL_ORDINARIES);
		else if (text.equals(ALL_SYMBOLS))
			return new EODQuoteRange(EODQuoteRange.ALL_SYMBOLS);
		else if (text.equals(MARKET_INDICES))
			return new EODQuoteRange(EODQuoteRange.MARKET_INDICES);
		else if (text == null)
			return new EODQuoteRange(EODQuoteRange.ALL_ORDINARIES);
		else {
			// Compare text against watch screen names first
			// and generate symbolSet from watchscreen if watchscreen
			// name matches.
			WatchScreen w = getWatchScreen(text);
			SortedSet symbolSet = null;
			if (w != null) {
				List symbolList = w.getSymbols();
				Iterator i = symbolList.iterator();
				String list = "";
				while (i.hasNext()) {
					list += i.next() + " ";
				}
				symbolSet = Symbol.toSortedSet(list, true);
			} else {
				// Otherwise,
				// Convert the text string to a sorted set of symbol
				// strings and also check to see if they exist
				symbolSet = Symbol.toSortedSet(text, true);
			}

			// If it returned empty there was an error...
			if (symbolSet.isEmpty())
				throw new SymbolFormatException(Locale.getString("MISSING_SYMBOLS"));
			else
				return new EODQuoteRange(new ArrayList(symbolSet));
		}
	}

	/**
	 * Return the current combo box selection as a text string.
	 *
	 * @return text string of current selection.
	 */
	public String getText() {
		return (String) getSelectedItem();
	}

	/**
	 * Set the current combo box selection as a text string.
	 *
	 * @param text the new text string of the current selection.
	 */
	public void setText(String text) {
		setSelectedItem(text);
	}

	// Rebuild option items in this combo box
	private void updateItems() {
		removeAllItems();
		addItem(ALL_ORDINARIES);
		addItem(ALL_SYMBOLS);
		addItem(MARKET_INDICES);
		addWatchScreens();
	}

	private void addWatchScreens() {
		List names = PreferencesManager.getWatchScreenNames();

		for (Iterator iterator = names.iterator(); iterator.hasNext();)
			addItem(iterator.next());
	}

	/**
	 * Return the watch screen with the given name or <code>null</code> if there is
	 * no watch screen with the given name.
	 *
	 * @param name name of the watch screen.
	 * @return the watch screen or <code>null</code> if there is no watch screen
	 *         with that name.
	 */
	private WatchScreen getWatchScreen(String name) {
		try {
			List names = PreferencesManager.getWatchScreenNames();

			if (names.contains(name))
				return PreferencesManager.getWatchScreen(name);
		} catch (PreferencesException e) {
			// This basic UI element should fail if there is an error parsing a
			// watch screen. So it just silently ignores it and pretends it
			// doesn't exist.
		}

		return null;
	}
}
