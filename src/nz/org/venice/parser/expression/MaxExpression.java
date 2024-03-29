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
import nz.org.venice.parser.IExpression;
import nz.org.venice.parser.TypeMismatchException;
import nz.org.venice.parser.Variables;
import nz.org.venice.quote.MissingQuoteException;
import nz.org.venice.quote.IQuoteBundle;
import nz.org.venice.quote.Symbol;

/**
 * An expression which finds the maximum quote over a given trading period.
 *
 * @author Andrew Leppard
 */
public class MaxExpression extends TernaryExpression {

	/**
	 * Create a new maximum expression for the given <code>quote</code> kind, for
	 * the given number of <code>days</code> starting with <code>lag</code> days
	 * away.
	 *
	 * @param quote the quote kind to find the maximum
	 * @param days  the number of days to search
	 * @param lag   the offset from the current day
	 */
	public MaxExpression(IExpression quote, IExpression days, IExpression lag) {
		super(quote, days, lag);
	}

	public double evaluate(Variables variables, IQuoteBundle quoteBundle, Symbol symbol, int day)
			throws EvaluationException {

		IQuoteSymbol quoteChild = (IQuoteSymbol) getChild(0);
		Symbol explicitSymbol = (quoteChild.getSymbol() != null) ? quoteChild.getSymbol() : symbol;

		int days = (int) getChild(1).evaluate(variables, quoteBundle, explicitSymbol, day);
		int quoteKind = quoteChild.getQuoteKind();

		if (days <= 0) {
			EvaluationException e = EvaluationException.MAX_RANGE_EXCEPTION;
			e.setMessage(this, "", days);
			throw e;
		}
		int offset = (int) getChild(2).evaluate(variables, quoteBundle, explicitSymbol, day);

		if (offset > 0) {
			EvaluationException e = EvaluationException.MAX_OFFSET_EXCEPTION;
			e.setMessage(this, "", offset);
			throw e;
		}

		return max(quoteBundle, explicitSymbol, quoteKind, days, day, offset);
	}

	public String toString() {
		String c1 = (getChild(0) != null) ? getChild(0).toString() : "(null)";
		String c2 = (getChild(1) != null) ? getChild(1).toString() : "(null)";
		String c3 = (getChild(2) != null) ? getChild(2).toString() : "(null)";

		return new String("max(" + c1 + ", " + c2 + ", " + c3 + ")");
	}

	public int checkType() throws TypeMismatchException {
		// First type must be quote, second and third types must be value
		if ((getChild(0).checkType() == FLOAT_QUOTE_TYPE || getChild(0).checkType() == INTEGER_QUOTE_TYPE)
				&& getChild(1).checkType() == INTEGER_TYPE && getChild(2).checkType() == INTEGER_TYPE)
			return getType();
		else {
			String types = getChild(0).getType() + "," + getChild(1).getType() + "," + getChild(2).getType();

			String expectedTypes = FLOAT_QUOTE_TYPE + " , " + INTEGER_TYPE + " ," + INTEGER_TYPE;

			throw new TypeMismatchException(this, types, expectedTypes);
		}
	}

	public int getType() {
		if (getChild(0).getType() == FLOAT_QUOTE_TYPE)
			return FLOAT_TYPE;
		else {
			assert getChild(0).getType() == INTEGER_QUOTE_TYPE;
			return INTEGER_TYPE;
		}
	}

	private double max(IQuoteBundle quoteBundle, Symbol symbol, int quote, int days, int day, int offset)
			throws EvaluationException {

		// double max = 0.0D;
		double max = Double.MIN_VALUE;
		boolean setValue = false;
		boolean missingQuotes = false;

		for (int i = offset - days + 1; i <= offset; i++) {
			try {
				double value = quoteBundle.getQuote(symbol, quote, day, i);
				if (value > max) {
					max = value;
				}

				setValue = true;
			} catch (MissingQuoteException e) {
				missingQuotes = true;
				// nothing to do
			}
		}
		/*
		 * Returning Double.MIN_VALUE here causes offset to overflow when the parent is
		 * a LagExpression. Consequently, offset becomes a positive value which triggers
		 * assertions which halt analyser processes.
		 */

		if (!setValue && missingQuotes) {
			throw EvaluationException.UNDEFINED_RESULT_EXCEPTION;
		}
		return max;
	}

	public Object clone() {
		return new MaxExpression((IExpression) getChild(0).clone(), (IExpression) getChild(1).clone(),
				(IExpression) getChild(2).clone());
	}
}
