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
import nz.org.venice.parser.Variables;
import nz.org.venice.quote.IQuoteBundle;
import nz.org.venice.quote.Symbol;

/**
 * An expression which compares whether the first expression is greater than or
 * equal to the second expression.
 */
public class GreaterThanEqualExpression extends ComparisionExpression {

	public GreaterThanEqualExpression(IExpression left, IExpression right) {
		super(left, right);
	}

	public double evaluate(Variables variables, IQuoteBundle quoteBundle, Symbol symbol, int day)
			throws EvaluationException {

		if (getChild(0).evaluate(variables, quoteBundle, symbol, day) >= getChild(1).evaluate(variables, quoteBundle,
				symbol, day))
			return TRUE;
		else
			return FALSE;
	}

	public IExpression simplify() {
		// First perform comparision simplifications
		IExpression simplified = super.simplify();

		// If we haven't simplified the whole expression away and
		// the left and right arguments are the same expression
		// then the comparision must be true.
		if (simplified.equals(this) && simplified.getChild(0).equals(simplified.getChild(1)))
			return new NumberExpression(true);
		else
			return simplified;
	}

	public String toString() {
		return super.toString(">=");
	}

	public Object clone() {
		return new GreaterThanEqualExpression((IExpression) getChild(0).clone(), (IExpression) getChild(1).clone());
	}
}
