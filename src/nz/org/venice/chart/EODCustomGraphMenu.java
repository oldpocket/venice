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
 * Menu for adding and removing multiple graphs of Gondola expressions.
 *
 * @author Mark Hummel
 * @see EODQuoteChartManu
 */

package nz.org.venice.chart;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JOptionPane;

import nz.org.venice.quote.Symbol;
import nz.org.venice.quote.EODQuoteBundle;
import nz.org.venice.chart.graph.Graph;
import nz.org.venice.chart.graph.GraphUI;
import nz.org.venice.chart.graph.CustomGraph;
import nz.org.venice.chart.GraphFactory;
import nz.org.venice.util.Locale;

public class EODCustomGraphMenu extends JMenu implements ActionListener {
    
    private ChartModule listener;
    private EODQuoteBundle quoteBundle;
    private Symbol symbol;
    private boolean index;
    private HashMap settings, graphItemMap;

    private final JMenu deleteMenu = new JMenu(Locale.getString("DELETE"));

    /**
     * Create a new Custom sub-menu allowing the user to add or remove custom 
     * graphs. There's no option to edit a custom graph as custom graphs 
     * dont accept parameters.
     *
     * @param listener The parent ChartModule which manages graphs
     * @param bundle The quote data
     * @param symbol The symbol being graphed
     * @param index  A flag specifiying if the chart is an indexChart
     */

    public EODCustomGraphMenu(ChartModule listener, EODQuoteBundle bundle, Symbol symbol, boolean index) {
	super(Locale.getString("CUSTOM"));
	this.listener = listener;
	quoteBundle = bundle;
	this.symbol = symbol;
	this.index = index;
	settings = new HashMap();
	graphItemMap = new HashMap();
	
	buildMenu();
    }

    private void buildMenu() {
	JMenuItem addItem = new JMenuItem(Locale.getString("ADD"));
	
	final EODCustomGraphMenu parent = this;
	addItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    final Graph graph = GraphFactory.newGraph(Locale.getString("CUSTOM"), 
							index,
							quoteBundle,
							symbol);
						
		    final CustomGraph customGraph = (CustomGraph)graph;
		    final GraphUI graphUI = graph.getUI(settings);
		    
		    final GraphSettingsDialog dialog =
			new GraphSettingsDialog(graphUI, graph.getName(), true);

		    Thread thread = new Thread() {
			    public void run() {
				int buttonPressed = dialog.showDialog();
				
				if (buttonPressed == GraphSettingsDialog.ADD) {
				    graph.setSettings(dialog.getSettings());
				    if (graph.isPrimary()) {
					listener.append(graph, 0);
				    } else {
					listener.append(graph);
				    }
				    listener.redraw();

				    final String label = customGraph.getExpressionLabel();
				    JMenuItem newGraphItem = new JMenuItem(label);
				    newGraphItem.addActionListener(parent);
				    deleteMenu.add(newGraphItem);
				    graphItemMap.put(newGraphItem, graph);
				}
			    }
			};
		    thread.start();
		}
	    });
	this.add(addItem);
	this.add(deleteMenu);
    }

    public void actionPerformed(ActionEvent e) {
	JMenuItem deletedItem = (JMenuItem)e.getSource();
	Graph graph = (Graph)graphItemMap.get(deletedItem);
	graphItemMap.remove(deletedItem);
	deleteMenu.remove(deletedItem);
	listener.remove(graph);
	listener.redraw();
    }
}