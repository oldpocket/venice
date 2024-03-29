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

package nz.org.venice.importer;

import nz.org.venice.quote.IDQuote;
import nz.org.venice.quote.IIDQuoteFilter;
import nz.org.venice.quote.QuoteFormatException;
import nz.org.venice.quote.Symbol;
import nz.org.venice.quote.SymbolFormatException;
import nz.org.venice.quote.SymbolNotFoundException;
import nz.org.venice.util.Locale;
import nz.org.venice.util.TradingDate;
import nz.org.venice.util.TradingDateFormatException;
import nz.org.venice.util.TradingTime;
import nz.org.venice.util.TradingTimeFormatException;

/**
 * Provides a filter to parse the Generic intra-day stock quote format. This
 * format uses a date with the month name, prices are in dollars. The first
 * column is the symbol, then the last quote, the time, change, open, high, low
 * and then the volume.
 *
 * Example:
 * 
 * <pre>
 * IBM,76.39,6/17/2005,4:02pm,-0.66,77.70,77.73,76.38,8594900
 * </pre>
 *
 * @author Andrew Leppard
 */

public class GenericWSIDQuoteFilter implements IIDQuoteFilter {

	/**
	 * Creates an instance of the filter.
	 */
	public GenericWSIDQuoteFilter() {
		// nothing to do
	}

	/**
	 * Return the name of the filter.
	 *
	 * @return the name of the filter.
	 */
	public String getName() {
		return "Generic Web Service";
	}

	/**
	 * Parse the given text string and returns the stock quote or null if it did not
	 * contain a valid quote.
	 *
	 * @param quoteLine a single line of text containing a quote.
	 * @return the stock quote
	 * @exception QuoteFormatException if the quote could not be parsed
	 */
	public IDQuote toIDQuote(String quoteLine) throws QuoteFormatException {
		IDQuote quote = null;

		if (quoteLine != null) {
			String[] quoteParts = quoteLine.split(",");
			int i = 0;

			/* Remove quotation marks. */
			for (i = 0; i < quoteParts.length; i++) {
				quoteParts[i] = quoteParts[i].replace('"', ' ');
				quoteParts[i] = quoteParts[i].trim();
			}

			i = 0;

			if (quoteParts.length == 9) {
				try {
					Symbol symbol = Symbol.find(quoteParts[i++]);
					double last = Double.parseDouble(quoteParts[i++]);
					TradingDate date = new TradingDate(quoteParts[i++], TradingDate.US);
					TradingTime time = new TradingTime(quoteParts[i++]);

					// Skip current change
					i++;

					double day_open = Double.parseDouble(quoteParts[i++]);
					double current_high = Double.parseDouble(quoteParts[i++]);
					double current_low = Double.parseDouble(quoteParts[i++]);
					long current_volume = Long.parseLong(quoteParts[i++]);

					// Yahoo unfortunately does not provide bid or ask prices
					// in its downloadable format.
					quote = new IDQuote(symbol, date, time, current_volume, current_low, current_high, day_open, last,
							0.0D, 0.0D);
				} catch (NumberFormatException e) {
					throw new QuoteFormatException(Locale.getString("ERROR_PARSING_NUMBER", quoteParts[i - 1]));
				} catch (SymbolFormatException e) {
					throw new QuoteFormatException(e.getMessage());
				} catch (TradingDateFormatException e) {
					throw new QuoteFormatException(e.getMessage());
				} catch (TradingTimeFormatException e) {
					throw new QuoteFormatException(e.getMessage());
				}
			} else
				throw new QuoteFormatException(Locale.getString("WRONG_FIELD_COUNT"));
		}

		return quote;
	}

	/**
	 * Convert the given stock quote to a string line.
	 *
	 * @param quote a stock quote
	 * @return string version of the quote
	 */
	public String toString(IDQuote quote) {
		throw new UnsupportedOperationException();
	}
}