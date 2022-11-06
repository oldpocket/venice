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

package nz.org.venice.parser.expression;

import nz.org.venice.parser.EvaluationException;
import nz.org.venice.parser.Variables;
import nz.org.venice.quote.MissingQuoteException;
import nz.org.venice.quote.IQuote;
import nz.org.venice.quote.IQuoteBundle;
import nz.org.venice.quote.Symbol;
import nz.org.venice.util.Locale;

/**
 * Class that represents a quote kind, e.g. day open, day close, etc. Originally
 * there was a separate class for each quote kind but this was deemed a little
 * excessive, so it was all folded into a single class.
 */
public class QuoteExpression extends TerminalExpression implements IQuoteSymbol {

	// Quote kind - Quote.DAY_OPEN, Quote.DAY_CLOSE, Quote.DAY_LOW, etc...
	private int quoteKind;

	/**
	 * Create a new quote expression.
	 *
	 * @param quoteKind Kind of quote. One of {@link IQuote#DAY_OPEN},
	 *                  {@link IQuote#DAY_CLOSE}, {@link IQuote#DAY_LOW},
	 *                  {@link IQuote#DAY_HIGH} or {@link IQuote#DAY_VOLUME}
	 */

	public QuoteExpression(int quoteKind) {
		assert (quoteKind == IQuote.DAY_OPEN || quoteKind == IQuote.DAY_CLOSE || quoteKind == IQuote.DAY_LOW
				|| quoteKind == IQuote.DAY_HIGH || quoteKind == IQuote.DAY_VOLUME);

		this.quoteKind = quoteKind;
	}

	/**
	 * Get the quote kind.
	 *
	 * @return the quote kind, one of: {@link IQuote#DAY_OPEN},
	 *         {@link IQuote#DAY_CLOSE}, {@link IQuote#DAY_HIGH},
	 *         {@link IQuote#DAY_LOW} or {@link IQuote#DAY_VOLUME}.
	 */
	public int getQuoteKind() {
		return quoteKind;
	}

	public Symbol getSymbol() {
		return null;
	}

	/**
	 * Get the type of the expression.
	 *
	 * @return {@link #FLOAT_QUOTE_TYPE} or {@link #INTEGER_QUOTE_TYPE}.
	 */
	public int getType() {
		if (getQuoteKind() == IQuote.DAY_VOLUME)
			return INTEGER_QUOTE_TYPE;
		else
			return FLOAT_QUOTE_TYPE;
	}

	public double evaluate(Variables variables, IQuoteBundle quoteBundle, Symbol symbol, int day)
			throws EvaluationException {

		try {
			return quoteBundle.getQuote(symbol, getQuoteKind(), day, 0);
		} catch (MissingQuoteException e) {
			// What should I do in this case?
			String message = symbol + " : "
					+ Locale.getString("NO_QUOTES_DATE", quoteBundle.offsetToDate(day).toString());

			throw new EvaluationException(message);
			// return 0.0D;
		}
	}

	public String toString() {
		switch (quoteKind) {
		case IQuote.DAY_OPEN:
			return "open";
		case IQuote.DAY_CLOSE:
			return "close";
		case IQuote.DAY_HIGH:
			return "high";
		case IQuote.DAY_LOW:
			return "low";
		default:
			assert quoteKind == IQuote.DAY_VOLUME;
			return "volume";
		}
	}

	public Object clone() {
		return new QuoteExpression(quoteKind);
	}

	public int checkType() {
		return getType();
	}

}
