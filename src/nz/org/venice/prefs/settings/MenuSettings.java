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

package nz.org.venice.prefs.settings;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.JDesktopPane;
import javax.swing.JMenuItem;

import nz.org.venice.chart.graph.Graph;
import nz.org.venice.main.Module;
import nz.org.venice.quote.Symbol;

/**
 * This class represents Graph data which can be saved for the purposes of
 * restoring the modules upon restart.
 * 
 * @author Mark Hummel
 * @see nz.org.venice.prefs.PreferencesManager
 */

public class MenuSettings extends AbstractSettings {

	String title;
	Symbol symbol;
	HashMap map;

	/**
	 *
	 * Create new MenuSettings.
	 * 
	 * @param key    The Menu Settings Identifier
	 * @param parent The graph settings identifier
	 * @param title  The title of the graph.
	 * 
	 */

	public MenuSettings(String key, String parent, String title) {
		super(Settings.GRAPHS, Settings.MENUS, key);
		this.title = title;
		map = new HashMap();
	}

	/**
	 *
	 * Create new GraphSettings.
	 * 
	 */

	public MenuSettings() {
		super(Settings.GRAPHS, Settings.MENUS);
		map = new HashMap();

	}

	/**
	 * Return the graph title
	 * 
	 * @return The graph title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Set the graph title
	 * 
	 * @param title The graph title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 *
	 * Set the symbol of the graph
	 * 
	 * @param item
	 */

	public void setMenu(JMenuItem item) {
		this.symbol = symbol;
	}

	/**
	 *
	 * Return the symbol assigned to this graph
	 * 
	 * @return The quote symbol
	 */

	public Symbol getMenuItem() {
		return null;
	}

	/**
	 *
	 * Set the map of the menu
	 * 
	 * @param graphList A list of graphs
	 */

	public void setMap(List graphList) {
		Iterator iterator = graphList.iterator();

		map = new HashMap();
		while (iterator.hasNext()) {
			Graph graph = (Graph) iterator.next();
			String identifier = graph.getName();
			map.put(identifier, graph);
		}
	}

	public void setMap(HashMap map) {
		this.map = map;
	}

	public void addGraph(Graph graph) {
		map.put(graph.getName(), graph);
	}

	/**
	 *
	 * get the map of the menu
	 * 
	 * @return map A map representing the selected menu items
	 */

	public HashMap getMap() {
		return map;
	}

	// Menu settings are data of the graph settings, so nothing is returned here
	public Module getModule(JDesktopPane desktop) {
		return null;
	}

}
