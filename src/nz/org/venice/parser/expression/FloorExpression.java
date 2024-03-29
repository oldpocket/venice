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
import nz.org.venice.quote.IQuoteBundle;
import nz.org.venice.quote.Symbol;

/**
 * An expression which returns the floor value.
 */
public class FloorExpression extends UnaryExpression {

	/**
	 * An expression which performs exponential function <code>floor</code> on the
	 * sub-expressions.
	 */
	public FloorExpression(IExpression arg) {
		super(arg);
	}

	public double evaluate(Variables variables, IQuoteBundle quoteBundle, Symbol symbol, int day)
			throws EvaluationException {

		return Math.floor(getChild(0).evaluate(variables, quoteBundle, symbol, day));
	}

	public IExpression simplify() {
		// First simplify child argument
		IExpression simplified = super.simplify();

		// If the child argument is a constant we can precompute.
		if (simplified.getChild(0) instanceof NumberExpression) {
			try {
				return new NumberExpression(simplified.evaluate(null, null, null, 0), simplified.getType());
			} catch (EvaluationException e) {
				// Shouldn't happen.
				return simplified;
			}
		} else {
			return simplified;
		}
	}

	public String toString() {
		String childString = (getChild(0) != null) ? getChild(0).toString() : "(null)";
		return new String("floor(" + childString + ")");
	}

	/**
	 * Check the input argument to the expression. It can only be
	 * {@link #INTEGER_TYPE} or {@link #FLOAT_TYPE}.
	 *
	 * @return the type of the expression
	 */
	public int checkType() throws TypeMismatchException {
		int type = getChild(0).checkType();

		if (type == FLOAT_TYPE || type == INTEGER_TYPE)
			return getType();
		else {
			String types = "" + type;
			String expectedTypes = "" + FLOAT_TYPE;
			throw new TypeMismatchException(this, types, expectedTypes);
		}
	}

	/**
	 * Get the type of the expression.
	 *
	 * @return {@link #FLOAT_TYPE}.
	 */
	public int getType() {
		return INTEGER_TYPE;
	}

	public Object clone() {
		return new FloorExpression((IExpression) getChild(0).clone());
	}
}
