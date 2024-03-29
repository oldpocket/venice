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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import nz.org.venice.chart.GraphTools;
import nz.org.venice.chart.Graphable;
import nz.org.venice.chart.GraphableQuoteFunctionSource;
import nz.org.venice.chart.source.IGraphSource;
import nz.org.venice.parser.EvaluationException;
import nz.org.venice.quote.QuoteFunctions;
import nz.org.venice.util.Locale;
import nz.org.venice.util.TradingDate;

/**
 * Bollinger Bands graph. This graph is used to show the volatility of a stock.
 * It draws two bands on the graph, they are centred around the moving average
 * of the graph. The top band is the moving average plus 2 standard deviations,
 * the lower band is the moving average minus 2 standard deviations.
 *
 * @author Andrew Leppard
 * @see PeriodGraphUI
 */
public class BollingerBandsGraph extends AbstractGraph {

	// Upper and lower band values ready to graph
	private Graphable upperBand;
	private Graphable lowerBand;

	/**
	 * Create a new bollinger bands graph.
	 *
	 * @param source the source to create a standard deviation from
	 */
	public BollingerBandsGraph(IGraphSource source) {
		super(source);
		setSettings(new HashMap());
	}

	/**
	 * Create a new bollinger bands graph.
	 *
	 * @param source the source to create a standard deviation from
	 */
	public BollingerBandsGraph(IGraphSource source, HashMap settings) {
		super(source);
		setSettings(settings);
		super.setSettings(settings);

	}

	public void render(Graphics g, Color colour, int xoffset, int yoffset, double horizontalScale, double verticalScale,
			double topLineValue, double bottomLineValue, List xRange, boolean vertOrientation) {

		// We ignore the graph colours and use our own custom colours
		g.setColor(Color.green.darker());

		GraphTools.renderLine(g, upperBand, xoffset, yoffset, horizontalScale, verticalScale, topLineValue,
				bottomLineValue, xRange, vertOrientation);
		GraphTools.renderLine(g, lowerBand, xoffset, yoffset, horizontalScale, verticalScale, topLineValue,
				bottomLineValue, xRange, vertOrientation);
	}

	public String getToolTipText(Comparable x, int y, int yoffset, double verticalScale, double bottomLineValue) {
		return null; // we never give tool tip information
	}

	// Highest Y value is in the bollinger bands graph
	public double getHighestY(List x) {
		return upperBand.getHighestY(x);
	}

	// Lowest Y value is in the bollinger bands graph
	public double getLowestY(List x) {
		return lowerBand.getLowestY(x);
	}

	/**
	 * Return the name of this graph.
	 *
	 * @return <code>Bollinger Bands</code>
	 */
	public String getName() {
		return Locale.getString("BOLLINGER_BANDS");
	}

	public boolean isPrimary() {
		return true;
	}

	/**
	 * Return the graph's user interface.
	 *
	 * @param settings the initial settings
	 * @return user interface
	 */
	public IGraphUI getUI(HashMap settings) {
		return new PeriodGraphUI(settings);
	}

	public void setSettings(HashMap settings) {
		super.setSettings(settings);

		// Retrieve period from settings hashmap
		int period = PeriodGraphUI.getPeriod(settings);

		createBollingerBands(getSource().getGraphable(), period);

	}

	private void createBollingerBands(Graphable source, int period) {

		upperBand = new Graphable();
		lowerBand = new Graphable();

		TradingDate date = (TradingDate) source.getStartX();
		GraphableQuoteFunctionSource quoteFunctionSource = new GraphableQuoteFunctionSource(source, date, period);

		for (Iterator iterator = source.iterator(); iterator.hasNext();) {
			date = (TradingDate) iterator.next();
			quoteFunctionSource.setDate(date);

			try {
				double bollingerTop = QuoteFunctions.bollingerUpper(quoteFunctionSource, period);
				upperBand.putY(date, new Double(bollingerTop));

				double bollingerBottom = QuoteFunctions.bollingerLower(quoteFunctionSource, period);
				lowerBand.putY(date, new Double(bollingerBottom));
			} catch (EvaluationException e) {
				// This can't happen since our source does not throw this exception
				assert false;
			}
		}
	}

}
