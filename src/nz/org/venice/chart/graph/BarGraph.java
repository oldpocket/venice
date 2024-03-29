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

package nz.org.venice.chart.graph;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

import nz.org.venice.chart.GraphTools;
import nz.org.venice.chart.source.IGraphSource;

/**
 * Horizontal bar graph. This graph is most commonly used to draw the volume
 * graph.
 *
 * @author Andrew Leppard
 */
public class BarGraph extends AbstractGraph {

	// The name of the graph, e.g. "Volume"
	private String name;

	// See Graph.java
	private boolean isPrimary;

	/**
	 * Create a new horizontal bar graph.
	 *
	 * @param source    the source to render
	 * @param name      the graph name
	 * @param isPrimary is this a primary graph?
	 */
	public BarGraph(IGraphSource source, String name, boolean isPrimary) {
		super(source);
		this.name = name;
		this.isPrimary = isPrimary;
	}

	// See Graph.java
	public void render(Graphics g, Color colour, int xoffset, int yoffset, double horizontalScale, double verticalScale,
			double topLineValue, double bottomLineValue, List xRange, boolean vertOrientation) {

		g.setColor(colour);
		GraphTools.renderBar(g, getSource().getGraphable(), xoffset, yoffset, horizontalScale, verticalScale,
				topLineValue, bottomLineValue, xRange, vertOrientation);
	}

	/**
	 * Return the name of this graph.
	 *
	 * @return the name given to the constructor
	 */
	public String getName() {
		return name;
	}

	public boolean isPrimary() {
		return isPrimary;
	}
}
