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

import nz.org.venice.parser.TypeMismatchException;
import nz.org.venice.parser.Variables;
import nz.org.venice.quote.IQuoteBundle;
import nz.org.venice.quote.Symbol;
import nz.org.venice.util.TradingDate;

/**
 * A function that returns the current day of month. The first day of the month
 * will be 1.
 */
public class DayExpression extends TerminalExpression {

	public DayExpression() {
		// nothing to do
	}

	public double evaluate(Variables variables, IQuoteBundle quoteBundle, Symbol symbol, int day) {
		TradingDate date = quoteBundle.offsetToDate(day);
		return date.getDay();
	}

	public String toString() {
		return "day()";
	}

	public int checkType() throws TypeMismatchException {
		return getType();
	}

	/**
	 * Get the type of the expression.
	 *
	 * @return returns {@link #INTEGER_TYPE}.
	 */
	public int getType() {
		return INTEGER_TYPE;
	}

	public Object clone() {
		return new DayExpression();
	}

}
