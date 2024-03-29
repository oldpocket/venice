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

import java.util.HashMap;
import java.util.Iterator;

import nz.org.venice.quote.IQuoteBundle;
import nz.org.venice.quote.Symbol;

/**
 * Representation of a composite executable parse tree. Any expression in the
 * <i>Gondola</i> language is parsed into a composite structure built upon this
 * class. This class therefore represents an executable expression.
 * <p>
 * Any single object of this type could represent a <b>terminal expression</b>
 * that is a number such as "<code>5</code>", a <b>unary expression</b> such as
 * "<code>not(X)</code>", a <b>binary expression</b> such as
 * "<code>X and Y</code>" or a <b>ternary expression</b>. The arguments labelled
 * above as <code>X</code> and <code>Y</code> would be represented by separate
 * <code>Expression</code> classes. Those classes would however be contained by
 * this class.
 */

public interface IExpression extends Cloneable {

	/**
	 * Type is undefined. Initial or default value only. Shouldn't be returned
	 * unless there's a bug.
	 */
	public static final int UNDEFINED_TYPE = -1;

	/** A boolean type that can be either {@link #TRUE} or {@link #FALSE}. */
	public static final int BOOLEAN_TYPE = 0;

	/** A float type that can contain any number. */
	public static final int FLOAT_TYPE = 1;

	/** An integer type that can contain any integer number. */
	public static final int INTEGER_TYPE = 2;

	/** Represents a stock float quote <b>type</b>: open, close, low, high */
	public static final int FLOAT_QUOTE_TYPE = 3;

	/** Represents a stock integer quote <b>type</b>: volume */
	public static final int INTEGER_QUOTE_TYPE = 4;

	/** Represents a string type that can contain any string. */
	public static final int STRING_TYPE = 5;

	/** Threshold level where a number is registered as <code>TRUE</code> */
	public static final double TRUE_LEVEL = 0.1D;

	/** Represents a type than can contain any short number. */
	public static final int INTEGER_SHORT_TYPE = 6;

	/** Represents either a float or an integer. */
	public static final int NUMERIC_TYPE = 7;

	/** Value of <code>TRUE</code> */
	public final static double TRUE = 1.0D;

	/** Value of <code>FALSE</code> */
	public final static double FALSE = 0.0D;

	/**
	 * Evaluates the given expression and returns the result.
	 *
	 * @param variables   variable storage area for expression
	 * @param quoteBundle the quote bundle containing quote data to use
	 * @param symbol      the current symbol
	 * @param day         current date in cache fast access format
	 * @return the result of the expression
	 * @throws EvaluationException if the expression performs an illegal operation
	 *                             such as divide by zero.
	 */
	public double evaluate(Variables variables, IQuoteBundle quoteBundle, Symbol symbol, int day)
			throws EvaluationException;

	/**
	 * Convert the given expression to a string.
	 * 
	 * @return the string representation of the expression
	 */
	public String toString();

	/**
	 * Perform type checking on the expression.
	 *
	 * @return the return type of the expression
	 * @throws TypeMismatchException if the expression has incorrect types
	 */
	public int checkType() throws TypeMismatchException;

	/**
	 * Get the type of the expression.
	 *
	 * @return one of {@link #BOOLEAN_TYPE}, {@link #FLOAT_TYPE},
	 *         {@link #INTEGER_TYPE}, {@link #FLOAT_QUOTE_TYPE} or
	 *         {@link #INTEGER_QUOTE_TYPE}.
	 */
	public int getType();

	/**
	 * Return the number of arguments of this expression.
	 *
	 * @return the number of arguments
	 */
	public int getChildCount();

	/**
	 * Return the parent of this node.
	 *
	 * @return the parent.
	 */
	public IExpression getParent();

	/**
	 * Set the parent of this node.
	 *
	 * @param parent the parent.
	 */
	public void setParent(IExpression parent);

	public Object clone();

	/**
	 * Return whether this node is the root node.
	 *
	 * @return <code>TRUE</code> if this node has no parent.
	 */
	public boolean isRoot();

	/**
	 * Return an iterator to iterate over all the nodes in this expression's tree.
	 *
	 * @return iterator.
	 */
	public Iterator iterator();

	/**
	 * Return the given argument.
	 *
	 * @param index the argument index
	 * @return the argument
	 */
	public IExpression getChild(int index);

	/**
	 * Set the argument.
	 *
	 * @param child new argument expression
	 * @param index index of the argument expression
	 */
	public IExpression setChild(IExpression child, int index);

	/**
	 * Perform simplifications and optimisations on the expression tree. For
	 * example, if the expression tree was <code>a and true</code> then the
	 * expression tree would be simplified to <code>a</code>.
	 */
	public IExpression simplify();

	/**
	 * Return the index of the given argument in the expression. We override this
	 * method because we use "==" to denote equality, not "equals" as the former
	 * would return the first argument with the same expression not necessarily the
	 * actual expression instance desired.
	 *
	 * @param expression the child expression to locate
	 * @return index of the child expression or <code>-1</code> if it could not be
	 *         found
	 */
	public int getIndex(IExpression expression);

	/**
	 * Returns whether this expression tree and the given expression tree are
	 * equivalent.
	 *
	 * @param object the other expression
	 */
	public boolean equals(Object object);

	/**
	 * If you override the {@link #equals} method then you should override this
	 * method. It provides a very basic hash code function.
	 *
	 * @return a poor hash code of the tree
	 */
	public int hashCode();

	/**
	 * Count the number of nodes in the tree.
	 *
	 * @return number of nodes or 1 if this is a terminal node
	 */
	public int size();

	/**
	 * Count the number of nodes in the tree with the given type.
	 *
	 * @return number of nodes in the tree with the given type.
	 */
	public int size(int type);

	/**
	 *
	 * @return true if none of the children of the expression are null (slightly
	 *         deprecated since 7.21 - shouldn't be anything but true because
	 *         children are now immutable and consequently not allowed to be null)
	 */
	public boolean validTree();

	/**
	 * Store the metadata about how the expressing parsing.
	 * 
	 * @param parseTree    A map which relates a constructed expression with it's
	 *                     associated token.
	 * @param tokenLineMap A map which associates a token to which line number of
	 *                     the rule text it appears.
	 */
	public void setParseMetadata(HashMap parseTree, HashMap tokenLineMap);

	/**
	 * @return metadata information about the parse. (e.g. line numbers)
	 */
	public ParseMetadata getParseMetadata();

	/**
	 * @return the unique id of the expression. (Not all expressions implement a
	 *         proper hashcode())
	 */
	public String getId();

}
