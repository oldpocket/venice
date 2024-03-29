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

package nz.org.venice.parser;

/**
 * A representation of a variable in the <i>Gondola</i> language.
 */
public class Variable {

	// Name of variable
	private String name;

	// Value of variable
	private double value;

	// Type of variable
	private int type;

	// Constant or variable
	private boolean isConstant;

	// Function
	private boolean isFunction;

	/** Indicates that the variable is constant and the value cannot be changed. */
	public final static boolean CONSTANT = true;

	/** Indicates that the variable can be changed. */
	public final static boolean VARIABLE = false;

	/**
	 * Create a new variable.
	 *
	 * @param name       the name of the variable.
	 * @param type       the type of the variable, one of
	 *                   {@link IExpression#BOOLEAN_TYPE},
	 *                   {@link IExpression#FLOAT_TYPE} or
	 *                   {@link IExpression#INTEGER_TYPE}.
	 * @param isConstant whether the variable is constant. Either @{link CONSTANT}
	 *                   or {@link Variable#VARIABLE}.
	 * @param value      the initial value of the variable.
	 */
	public Variable(String name, int type, boolean isConstant, double value) {
		assert (type == IExpression.BOOLEAN_TYPE || type == IExpression.FLOAT_TYPE || type == IExpression.INTEGER_TYPE);

		this.name = name;
		this.type = type;
		this.value = value;
		this.isConstant = isConstant;
		isFunction = false;
	}

	/**
	 * Create a new variable.
	 *
	 * @param name       the name of the variable.
	 * @param type       the type of the variable, one of
	 *                   {@link IExpression#BOOLEAN_TYPE},
	 *                   {@link IExpression#FLOAT_TYPE} or
	 *                   {@link IExpression#INTEGER_TYPE}.
	 * @param isConstant whether the variable is constant. Either @{link CONSTANT}
	 *                   or {@link Variable#VARIABLE}.
	 * @param value      the initial value of the variable.
	 */
	public Variable(String name, int type, boolean isConstant, boolean isFunction, double value) {
		assert (type == IExpression.BOOLEAN_TYPE || type == IExpression.FLOAT_TYPE || type == IExpression.INTEGER_TYPE);

		this.name = name;
		this.type = type;
		this.value = value;
		this.isConstant = isConstant;
		this.isFunction = isFunction;

		assert !(isConstant && isFunction);
	}

	/**
	 * Return the name of the variable.
	 *
	 * @return the name of the variable.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return the type of the variable.
	 *
	 * @return the type of the variable.
	 */

	public int getType() {
		return type;
	}

	/**
	 * Return whether the variable is a constant.
	 *
	 * @return <code>TRUE</code> iff the variable is a constant.
	 */
	public boolean isConstant() {
		return isConstant;
	}

	/**
	 * Return whether the variable is a function.
	 *
	 * @return <code>TRUE</code> iff the variable is function constant.
	 */
	public boolean isFunction() {
		return isFunction;
	}

	/**
	 * Return the value of the variable.
	 *
	 * @return the value of the variable.
	 */
	public double getValue() {
		return value;
	}

	/**
	 * Set the value of the variable. You can legally change the value of constant
	 * variables using this method. Constant variables are only constant in the
	 * gondola language.
	 *
	 * @param value the new value.
	 */
	public void setValue(double value) {
		this.value = value;
	}

	/**
	 * Set the value of the variable. You can legally change the value of constant
	 * variables using this method. Constant variables are only constant in the
	 * gondola language.
	 *
	 * @param value the new value.
	 */
	public void setValue(int value) {
		this.value = (double) value;
	}

	public Object clone() throws CloneNotSupportedException {
		Variable newVariable = new Variable(name, type, isConstant, isFunction, value);

		return newVariable;
	}

}
