/* Merchant of Venice - technical analysis software for the stock market.
   Copyright (C) 2003 Andrew Leppard (aleppard@picknowl.com.au)

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
import nz.org.venice.parser.Variable;
import nz.org.venice.parser.Variables;
import nz.org.venice.quote.IQuoteBundle;
import nz.org.venice.quote.Symbol;
import nz.org.venice.util.Locale;

/**
 * A representation of an expression to set and return the value of a variable.
 */
public class SetVariableExpression extends UnaryExpression {

	// The variable's name and type
	private String name;
	private int type;

	public SetVariableExpression(String name, int type, IExpression value) {
		super(value);

		assert name != null && name.length() > 0;

		this.name = name;
		this.type = type;
	}

	public double evaluate(Variables variables, IQuoteBundle quoteBundle, Symbol symbol, int day)
			throws EvaluationException {

		Variable variable = variables.get(name);

		if (variable != null) {
			if (!variable.isConstant()) {
				assert variable.getType() == type;
				double value = getChild(0).evaluate(variables, quoteBundle, symbol, day);
				variable.setValue(value);
				return value;
			} else
				throw new EvaluationException(Locale.getString("VARIABLE_IS_CONSTANT_ERROR", name));
		} else
			throw new EvaluationException(Locale.getString("VARIABLE_NOT_DEFINED_ERROR", name));
	}

	public String toString() {
		return new String(name + " = " + getChild(0));
	}

	public String getName() {
		return name;
	}

	public int getType() {
		return type;
	}

	public int checkType() throws TypeMismatchException {
		if (getType() == getChild(0).checkType())
			return getType();
		else {
			throw new TypeMismatchException(this, getChild(0).checkType(), type);
		}
	}

	public boolean equals(Object object) {
		if (object instanceof SetVariableExpression) {
			SetVariableExpression expression = (SetVariableExpression) object;

			if (expression.getName().equals(getName()) && expression.getType() == getType()
					&& expression.getChild(0).equals(getChild(0)))
				return true;
		}

		return false;
	}

	public int hashCode() {
		IExpression child1 = getChild(0);
		assert child1 != null;

		return (child1.hashCode() ^ getName().hashCode() ^ (37 * getType()));
	}

	public Object clone() {
		return new SetVariableExpression(name, type, (IExpression) getChild(0).clone());
	}

}
