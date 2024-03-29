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

package nz.org.venice.analyser.gp;

import java.util.Iterator;
import java.util.Random;

import nz.org.venice.analyser.GPGondolaSelection;
import nz.org.venice.parser.IExpression;
import nz.org.venice.parser.ExpressionFactory;
import nz.org.venice.parser.expression.AbsExpression;
import nz.org.venice.parser.expression.AddExpression;
import nz.org.venice.parser.expression.AndExpression;
import nz.org.venice.parser.expression.AvgExpression;
import nz.org.venice.parser.expression.BBLExpression;
import nz.org.venice.parser.expression.BBUExpression;
import nz.org.venice.parser.expression.CeilExpression;
import nz.org.venice.parser.expression.CosineExpression;
import nz.org.venice.parser.expression.DayExpression;
import nz.org.venice.parser.expression.DayOfWeekExpression;
import nz.org.venice.parser.expression.DayOfYearExpression;
import nz.org.venice.parser.expression.DivideExpression;
import nz.org.venice.parser.expression.EMAExpression;
import nz.org.venice.parser.expression.EqualThanExpression;
import nz.org.venice.parser.expression.ExponentialExpression;
import nz.org.venice.parser.expression.FloorExpression;
import nz.org.venice.parser.expression.GetVariableExpression;
import nz.org.venice.parser.expression.GreaterThanEqualExpression;
import nz.org.venice.parser.expression.GreaterThanExpression;
import nz.org.venice.parser.expression.IfExpression;
import nz.org.venice.parser.expression.LagExpression;
import nz.org.venice.parser.expression.LessThanEqualExpression;
import nz.org.venice.parser.expression.LessThanExpression;
import nz.org.venice.parser.expression.LogarithmExpression;
import nz.org.venice.parser.expression.MACDExpression;
import nz.org.venice.parser.expression.MaxExpression;
import nz.org.venice.parser.expression.MinExpression;
import nz.org.venice.parser.expression.MomentumExpression;
import nz.org.venice.parser.expression.MonthExpression;
import nz.org.venice.parser.expression.MultiplyExpression;
import nz.org.venice.parser.expression.NotEqualExpression;
import nz.org.venice.parser.expression.NotExpression;
import nz.org.venice.parser.expression.NumberExpression;
import nz.org.venice.parser.expression.OBVExpression;
import nz.org.venice.parser.expression.OrExpression;
import nz.org.venice.parser.expression.PercentExpression;
import nz.org.venice.parser.expression.QuoteExpression;
import nz.org.venice.parser.expression.RSIExpression;
import nz.org.venice.parser.expression.SineExpression;
import nz.org.venice.parser.expression.SqrtExpression;
import nz.org.venice.parser.expression.StandardDeviationExpression;
import nz.org.venice.parser.expression.SubtractExpression;
import nz.org.venice.parser.expression.SumExpression;
import nz.org.venice.quote.IQuote;
import nz.org.venice.util.VeniceLog;

/**
 * The mutator can build random expressions and randomly mutate existing
 * expressions. This class is at the heart of the GP as it creates the random
 * buy and sell rules and combines the rules during "breeding".
 *
 * @author Andrew Leppard
 * @see Individual
 * @see GeneticProgramme
 */
public class Mutator {

	// The branch factor is a number which defines the likelihood of
	// us choosing a non-terminal expression over a terminal expression.
	// The likelihood will be diminished as the expression tree grows
	// in depth.
	private final static int BRANCH_FACTOR = 80;

	// When mutating a numeric value (e.g. 10), this is the percent
	// chance that we favour applying a percent change to the number
	// (e.g. +20%) rather than replacing it with an entirely random number
	private final static int FAVOUR_NUMBER_PERCENT = 85;

	// This is the chance that a mutation occurs
	private final static int MUTATION_PERCENT = 10;

	// This is the chance that an additional mutation occurs. And an
	// additional mutation after that, and after that etc.
	private final static int EXTRA_MUTATION_PERCENT = 10;

	// Given a mutation, this is the chance of it being an insertion mutation
	// (i.e. we insert an expression tree at the mutation point).
	private final static int INSERTION_MUTATION_PERCENT = 10;

	// Given a mutation, this is the chance of it being a deletion mutation
	// (i.e. we delete the expression tree at the mutation point).
	private final static int DELETION_MUTATION_PERCENT = 20;

	// Given a mutation, this is the chance of it being a modification mutation
	// (i.e. we modify the expression tree at the mutation point).
	private final static int MODIFICATION_MUTATION_PERCENT = 70;

	// The subtype concept is introduced in Mutator class to pilot the GP process,
	// so that less time is wasted to find the best fitting expressions.
	// The type concept is present in Expression class and it is widely used in
	// Gondola,
	// the subtype concept is a weaker tool, that only suggests the GP what to find
	// for the best fit in an expression.
	// For example in lag(close,xxx), we would like xxx be a INTEGER_TYPE
	// expression,
	// by the point of view of the type, but an integer is not enough,
	// we also need that this integer be small and negative,
	// because xxx is the number of days of delay.
	// So we pilot the GP to find a small negative integer using
	// the subtype NEGATIVE_SHORT_INTEGER_SUBTYPE.
	private final static int NO_SUBTYPE = 0;
	private final static int POSITIVE_SHORT_INTEGER_SUBTYPE = 1;
	private final static int NEGATIVE_SHORT_INTEGER_SUBTYPE = 2;
	private final static int SMOOTHING_CONSTANT_SUBTYPE = 3;

	// Random number generator
	private Random random;

	// UI Panel containing user's selection of percent chance of generating
	// each expression type
	private GPGondolaSelection GPGondolaSelection;

	// Is this mutator allowed to generate the "held" variable? Typically
	// buy mutators cannot, and sell mutators can.
	private boolean allowHeld;

	// Is this mutator allowed to generate the "order" variable? Typically
	// only if the user has ordered the stocks will this variable bew
	// available.
	private boolean allowOrder;

	/**
	 * Create a new mutator.
	 *
	 * @param random             use this random number generator
	 * @param GPGondolaSelection UI containing user's desired expression
	 *                           probabilities
	 * @param allowHeld          allow the creation of the <code>held</code>
	 *                           variable
	 * @param allowOrder         allow the creation of the <code>order</code>
	 *                           variable
	 */
	public Mutator(Random random, GPGondolaSelection GPGondolaSelection, boolean allowHeld, boolean allowOrder) {
		this.random = random;
		this.GPGondolaSelection = GPGondolaSelection;
		this.allowHeld = allowHeld;
		this.allowOrder = allowOrder;
	}

	/**
	 * Create a new random expression of the given type.
	 *
	 * @param type the type of the expression, e.g. {@link IExpression#BOOLEAN_TYPE}
	 * @return a randomly generated expression
	 */
	public IExpression createRandom(int type) {
		return createRandom(null, type, this.NO_SUBTYPE, 1);
	}

	/**
	 * Create a new random expression of the given type and subType.
	 *
	 * @param type    the type of the expression, e.g.
	 *                {@link IExpression#BOOLEAN_TYPE}
	 * @param subType the subType of the expression.
	 * @return a randomly generated expression
	 */
	public IExpression createRandom(int type, int subType) {
		return createRandom(null, type, subType, 1);
	}

	/**
	 * Create a new random expression of the given type,subType at the given level.
	 * The level parameter is used to vary the probability of the expression being a
	 * non-terminal or a terminal expression. As the level of the expression tree
	 * gets larger, the probability of creating a non-terminal child expression
	 * decreases.
	 *
	 * @param type    the type of the expression, e.g.
	 *                {@link IExpression#BOOLEAN_TYPE}
	 * @param subType the subType of the expression.
	 * @param level   the level in the tree
	 * @return a randomly generated expression
	 */
	public IExpression createRandom(int type, int subType, int level) {
		return createRandom(null, type, subType, level);
	}

	/**
	 * Create a new random expression based on mutating the given expression. If
	 * <code>level < 1</code> then the top node of the created expression will not
	 * be terminal.
	 *
	 * @param model   initial expression to work with
	 * @param type    the type of the expression, e.g.
	 *                {@link IExpression#BOOLEAN_TYPE}
	 * @param subType the subType of the expression.
	 * @param level   the level in the tree
	 * @return a randomly generated expression
	 * @see #createRandom(int type, int subType, int level)
	 */
	public IExpression createRandom(IExpression model, int type, int subType, int level) {
		boolean terminal = true;

		if (level < 1) {
			terminal = false;
		} else {
			// Work out percent chance of non-terminate symbol
			double branchPercent = (double) BRANCH_FACTOR / (double) level;
			double percent = random.nextDouble() * 100;

			if (branchPercent > percent) {
				terminal = false;
			}
		}

		// If the type is a boolean then there isn't much point generating
		// the boolean terminal expressions TRUE or FALSE because our
		// simplification code will just simplify it out of existence,
		// e.g. "and or a" would just become "a".
		if (type == IExpression.BOOLEAN_TYPE || !terminal) {
			return createRandomNonTerminal(model, type, subType, level + 1);
		} else {
			return createRandomTerminal(type, subType);
		}
	}

	/**
	 * Create a new random non-terminal expression of the given type. A terminal
	 * expression is one that has children, e.g. an operator such as plus. (Thus
	 * plus operator would have two children, e.g. 1 and 1).
	 *
	 * @param type the type of the expression, e.g. {@link IExpression#BOOLEAN_TYPE}
	 * @return a randomly generated non-terminal expression
	 */
	public IExpression createRandomNonTerminal(int type) {
		return createRandomNonTerminal(null, type, this.NO_SUBTYPE, 1);
	}

	/**
	 * Create a new random non-terminal expression of the given type and subType. A
	 * terminal expression is one that has children, e.g. an operator such as plus.
	 * (Thus plus operator would have two children, e.g. 1 and 1).
	 *
	 * @param type    the type of the expression, e.g.
	 *                {@link IExpression#BOOLEAN_TYPE}
	 * @param subType the subType of the expression.
	 * @return a randomly generated non-terminal expression
	 */
	public IExpression createRandomNonTerminal(int type, int subType) {
		return createRandomNonTerminal(null, type, subType, 1);
	}

	/**
	 * Create a new random non-terminal expression of the given type and subType at
	 * the given level.
	 *
	 * @param type    the type of the expression, e.g.
	 *                {@link IExpression#BOOLEAN_TYPE}
	 * @param subType the subType of the expression.
	 * @param level   the level in the tree
	 * @return a randomly generated non-terminal expression
	 */
	public IExpression createRandomNonTerminal(int type, int subType, int level) {
		return createRandomNonTerminal(null, type, subType, level);
	}

	/**
	 * Create a new random non-terminal expression based on mutating the given
	 * expression.
	 *
	 * @param model   initial expression to work with
	 * @param type    the type of the expression, e.g.
	 *                {@link IExpression#BOOLEAN_TYPE}
	 * @param subType the subType of the expression.
	 * @param level   the level in the tree
	 * @return a randomly generated non-terminal expression
	 * @see #createRandom(int type, int subType, int level)
	 */
	public IExpression createRandomNonTerminal(IExpression model, int type, int subType, int level) {

		IExpression rv = null;

		if (type == IExpression.BOOLEAN_TYPE) {
			rv = createRandomNonTerminalBoolean(model, level);
		} else if (type == IExpression.FLOAT_TYPE) {
			rv = createRandomNonTerminalFloat(model, level);
		} else if ((type == IExpression.INTEGER_TYPE) && (subType == this.POSITIVE_SHORT_INTEGER_SUBTYPE)) {
			rv = createRandomNonTerminalPositiveShortInteger(model, level);
		} else if ((type == IExpression.INTEGER_TYPE) && (subType == this.NEGATIVE_SHORT_INTEGER_SUBTYPE)) {
			rv = createRandomNonTerminalNegativeShortInteger(model, level);
			// At the end of all integer type, we put the integer type with no subType.
		} else if (type == IExpression.INTEGER_TYPE) {
			rv = createRandomNonTerminalInteger(model, level);
		} else if (type == IExpression.NUMERIC_TYPE) {
			rv = createRandomNonTerminalNumeric(model, level);
		} else {
			// Quote types are all terminal!
			assert (type == IExpression.FLOAT_QUOTE_TYPE || type == IExpression.INTEGER_QUOTE_TYPE);
			rv = createRandomTerminal(type);
		}

		return rv;
	}

	/**
	 * Creates a random terminal expression of the given type. A terminal expression
	 * is one that does not have any children, e.g. a number or a variable
	 * expression.
	 *
	 * @param type the type of the expression, e.g. {@link IExpression#BOOLEAN_TYPE}
	 * @return a randomly generated terminal expression
	 */
	public IExpression createRandomTerminal(int type) {
		return createRandomTerminal(type, this.NO_SUBTYPE);
	}

	/**
	 * Creates a random terminal expression of the given type and subType. A
	 * terminal expression is one that does not have any children, e.g. a number or
	 * a variable expression.
	 *
	 * @param type    the type of the expression, e.g.
	 *                {@link IExpression#BOOLEAN_TYPE}
	 * @param subType the subType of the expression.
	 * @return a randomly generated terminal expression
	 */
	public IExpression createRandomTerminal(int type, int subType) {

		int randomNumber = 0;

		switch (type) {

		case IExpression.BOOLEAN_TYPE:
			randomNumber = random.nextInt(2);

			if (randomNumber == 0) {
				return new NumberExpression(true);
			} else {
				assert randomNumber == 1;
				return new NumberExpression(false);
			}

		case IExpression.FLOAT_TYPE:
			if (subType == this.SMOOTHING_CONSTANT_SUBTYPE) {
				// Generate an ordinary number that fit for the smoothing constant.
				return new NumberExpression(0.01D + random.nextDouble() * (1.0D - 0.01D));
			}

			randomNumber = GPGondolaSelection.getRandomToGenerateTerminalFloat(allowHeld);

			if (randomNumber == 0) {
				// Generate an ordinary number
				return new NumberExpression(50 - random.nextDouble() * 100);
			} else if (randomNumber == 1) {
				return new GetVariableExpression("capital", IExpression.FLOAT_TYPE);
			} else if (randomNumber == 2) {
				assert allowHeld;
				return new GetVariableExpression("stockcapital", IExpression.FLOAT_TYPE);
			}

		case IExpression.INTEGER_TYPE:

			// Generate an ordinary number small and negative.
			if (subType == this.NEGATIVE_SHORT_INTEGER_SUBTYPE) {
				return new NumberExpression(0 - random.nextInt(50));
			}

			randomNumber = GPGondolaSelection.getRandomToGenerateTerminalInteger(allowHeld, allowOrder);

			if (randomNumber == 0) {

				// Generate an ordinary number small and positive.
				if (subType == this.POSITIVE_SHORT_INTEGER_SUBTYPE) {
					return new NumberExpression(50 - random.nextInt(50));
				}

				// Generate an ordinary number
				return new NumberExpression(50 - random.nextInt(100));

			} else if (randomNumber == 1) {
				return new DayOfYearExpression();
			} else if (randomNumber == 2) {
				return new MonthExpression();
			} else if (randomNumber == 3) {
				return new DayExpression();
			} else if (randomNumber == 4) {
				return new DayOfWeekExpression();
			} else if (randomNumber == 5) {
				return new GetVariableExpression("daysfromstart", IExpression.INTEGER_TYPE);
			} else if (randomNumber == 6) {
				return new GetVariableExpression("transactions", IExpression.INTEGER_TYPE);
			} else {
				if (allowOrder && allowHeld) {
					if (randomNumber == 7) {
						return new GetVariableExpression("held", IExpression.INTEGER_TYPE);
					} else {
						return new GetVariableExpression("order", IExpression.INTEGER_TYPE);
					}
				} else if (allowHeld) {
					return new GetVariableExpression("held", IExpression.INTEGER_TYPE);
				} else {
					assert allowOrder;
					return new GetVariableExpression("order", IExpression.INTEGER_TYPE);
				}
			}

		case IExpression.FLOAT_QUOTE_TYPE:

			randomNumber = GPGondolaSelection.getRandomToGenerateFloatQuote();

			if (randomNumber == 0) {
				return new QuoteExpression(IQuote.DAY_OPEN);
			} else if (randomNumber == 1) {
				return new QuoteExpression(IQuote.DAY_HIGH);
			} else if (randomNumber == 2) {
				return new QuoteExpression(IQuote.DAY_LOW);
			} else {
				assert randomNumber == 3;
				return new QuoteExpression(IQuote.DAY_CLOSE);
			}

		case IExpression.INTEGER_QUOTE_TYPE:
			return new QuoteExpression(IQuote.DAY_VOLUME);

		case IExpression.NUMERIC_TYPE:
			randomNumber = GPGondolaSelection.getRandomToGenerateTerminalInteger(allowHeld, allowOrder);

			if (randomNumber % 2 == 0) {

				randomNumber = GPGondolaSelection.getRandomToGenerateTerminalInteger(allowHeld, allowOrder);

				if (randomNumber % 2 != 0) {
					// Generate an ordinary number small and negative.
					return new NumberExpression(0 - random.nextInt(50));
				} else {
					// Generate an ordinary number
					return new NumberExpression(50 - random.nextInt(100));
				}
			} else {
				if (subType == this.SMOOTHING_CONSTANT_SUBTYPE) {
					// Generate an ordinary number that fit for the smoothing constant.
					return new NumberExpression(0.01D + random.nextDouble() * (1.0D - 0.01D));
				}

				randomNumber = GPGondolaSelection.getRandomToGenerateTerminalFloat(allowHeld);

				if (randomNumber == 0) {
					// Generate an ordinary number
					return new NumberExpression(50 - random.nextDouble() * 100);
				} else if (randomNumber == 1) {
					return new GetVariableExpression("capital", IExpression.FLOAT_TYPE);
				} else if (randomNumber == 2) {
					assert allowHeld;
					return new GetVariableExpression("stockcapital", IExpression.FLOAT_TYPE);
				}
			}

		default:
			// Shouldn't get here - would be a sign of a serious bug
			assert false;
			return null;
		}
	}

	/**
	 * Create a random non-terminal {@link IExpression#BOOLEAN_TYPE} expression.
	 *
	 * @param model model expression
	 * @param level tree level
	 * @return randomly generated non-terminal boolean expression
	 */
	private IExpression createRandomNonTerminalBoolean(IExpression model, int level) {
		int randomNumber = GPGondolaSelection.getRandomToGenerateBoolean();

		if (randomNumber == 0) {
			return new NotExpression(getChild(model, level, 0, IExpression.BOOLEAN_TYPE));

		} else if (randomNumber == 1) {
			IExpression first = getChild(model, level, 0, IExpression.NUMERIC_TYPE);
			return new EqualThanExpression(first, getChild(model, level, 1, first.getType()));

		} else if (randomNumber == 2) {
			IExpression first = getChild(model, level, 0, IExpression.NUMERIC_TYPE);
			return new GreaterThanEqualExpression(first, getChild(model, level, 1, first.getType()));

		} else if (randomNumber == 3) {
			IExpression first = getChild(model, level, 0, IExpression.NUMERIC_TYPE);
			IExpression next = getChild(model, level, 1, first.getType());

			IExpression retExp = new GreaterThanExpression(first, next);

			return retExp;

		} else if (randomNumber == 4) {
			IExpression first = getChild(model, level, 0, IExpression.NUMERIC_TYPE);
			IExpression next = getChild(model, level, 1, first.getType());

			IExpression retExp = new LessThanEqualExpression(first, next);

			return retExp;

		} else if (randomNumber == 5) {
			IExpression first = getChild(model, level, 0, IExpression.NUMERIC_TYPE);
			return new LessThanExpression(first, getChild(model, level, 1, first.getType()));

		} else if (randomNumber == 6) {
			IExpression first = getChild(model, level, 0, IExpression.NUMERIC_TYPE);
			return new NotEqualExpression(first, getChild(model, level, 1, first.getType()));

		} else if (randomNumber == 7) {
			return new AndExpression(getChild(model, level, 0, IExpression.BOOLEAN_TYPE),
					getChild(model, level, 1, IExpression.BOOLEAN_TYPE));

		} else {
			assert randomNumber == 8;

			IExpression first = getChild(model, level, 0, IExpression.BOOLEAN_TYPE);
			IExpression next = getChild(model, level, 1, IExpression.BOOLEAN_TYPE);

			IExpression retExp = new OrExpression(first, next);

			return retExp;
		}
	}

	/**
	 * Create a random non-terminal {@link IExpression#FLOAT_TYPE} expression.
	 *
	 * @param model model expression
	 * @param level tree level
	 * @return randomly generated non-terminal float expression
	 */
	private IExpression createRandomNonTerminalFloat(IExpression model, int level) {

		// If we are mutating an existing number expression then favour
		// just modifying the number's value rather than replacing it
		// with a random expressions. This helps keep the equation size down and
		// favours trying different values.
		if (model != null && model instanceof NumberExpression && FAVOUR_NUMBER_PERCENT > random.nextInt(100)) {

			NumberExpression numberExpression = (NumberExpression) model;
			double step = random.nextDouble() * 6.0D;
			double value = Math.pow(10.0D, step);

			if (random.nextBoolean())
				value = -value;

			numberExpression.setValue(numberExpression.getValue() + value);
			return numberExpression;
		}

		int randomNumber = GPGondolaSelection.getRandomToGenerateFloat();

		if (randomNumber == 0) {
			return createRandomTerminal(IExpression.FLOAT_TYPE);

		} else if (randomNumber == 1) {
			return new AddExpression(getChild(model, level, 0, IExpression.FLOAT_TYPE),
					getChild(model, level, 1, IExpression.NUMERIC_TYPE));

		} else if (randomNumber == 2) {
			return new SubtractExpression(getChild(model, level, 0, IExpression.FLOAT_TYPE),
					getChild(model, level, 1, IExpression.NUMERIC_TYPE));

		} else if (randomNumber == 3) {
			return new MultiplyExpression(getChild(model, level, 0, IExpression.FLOAT_TYPE),
					getChild(model, level, 1, IExpression.NUMERIC_TYPE));

		} else if (randomNumber == 4) {
			return new DivideExpression(getChild(model, level, 0, IExpression.FLOAT_TYPE),
					getChild(model, level, 1, IExpression.NUMERIC_TYPE));

		} else if (randomNumber == 5) {
			return new PercentExpression(getChild(model, level, 0, IExpression.FLOAT_TYPE),
					getChild(model, level, 1, IExpression.NUMERIC_TYPE));

		} else if (randomNumber == 6) {
			return new IfExpression(getChild(model, level, 0, IExpression.BOOLEAN_TYPE),
					getChild(model, level, 1, IExpression.FLOAT_TYPE), getChild(model, level, 2, IExpression.FLOAT_TYPE));

		} else if (randomNumber == 7) {
			return new LagExpression(createRandomTerminal(IExpression.FLOAT_QUOTE_TYPE),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE));

		} else if (randomNumber == 8) {
			return new MinExpression(createRandomTerminal(IExpression.FLOAT_QUOTE_TYPE),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE),
					getChild(model, level, 2, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE));

		} else if (randomNumber == 9) {
			return new MaxExpression(createRandomTerminal(IExpression.FLOAT_QUOTE_TYPE),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE),
					getChild(model, level, 2, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE));

		} else if (randomNumber == 10) {
			return new SumExpression(createRandomTerminal(IExpression.FLOAT_QUOTE_TYPE),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE),
					getChild(model, level, 2, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE));

		} else if (randomNumber == 11) {
			return new SqrtExpression(getChild(model, level, 0, IExpression.FLOAT_TYPE));

		} else if (randomNumber == 12) {
			return new AbsExpression(getChild(model, level, 0, IExpression.FLOAT_TYPE));

		} else if (randomNumber == 13) {
			return new CosineExpression(getChild(model, level, 0, IExpression.NUMERIC_TYPE));

		} else if (randomNumber == 14) {
			return new SineExpression(getChild(model, level, 0, IExpression.NUMERIC_TYPE));

		} else if (randomNumber == 15) {
			return new LogarithmExpression(getChild(model, level, 0, IExpression.NUMERIC_TYPE));

		} else if (randomNumber == 16) {
			// Either or - don't care
			return new ExponentialExpression(getChild(model, level, 0, IExpression.NUMERIC_TYPE));

		} else if (randomNumber == 17) {
			return new AvgExpression(createRandomTerminal(IExpression.FLOAT_QUOTE_TYPE),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE),
					getChild(model, level, 2, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE));

		} else if (randomNumber == 18) {
			return new EMAExpression(createRandomTerminal(IExpression.FLOAT_QUOTE_TYPE),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE),
					getChild(model, level, 2, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE),
					createRandomTerminal(IExpression.FLOAT_TYPE, this.SMOOTHING_CONSTANT_SUBTYPE));

		} else if (randomNumber == 19) {
			return new MACDExpression(createRandomTerminal(IExpression.FLOAT_QUOTE_TYPE),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE));

		} else if (randomNumber == 20) {
			return new MomentumExpression(createRandomTerminal(IExpression.FLOAT_QUOTE_TYPE),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE),
					getChild(model, level, 2, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE));

		} else if (randomNumber == 21) {
			return new RSIExpression(
					getChild(model, level, 0, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE),
					new NumberExpression(false));

		} else if (randomNumber == 22) {
			return new StandardDeviationExpression(createRandomTerminal(IExpression.FLOAT_QUOTE_TYPE),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE),
					getChild(model, level, 2, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE));

		} else if (randomNumber == 23) {
			return new BBLExpression(createRandomTerminal(IExpression.FLOAT_QUOTE_TYPE),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE),
					getChild(model, level, 2, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE));

		} else {
			assert randomNumber == 24;
			return new BBUExpression(createRandomTerminal(IExpression.FLOAT_QUOTE_TYPE),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE),
					getChild(model, level, 2, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE));
		}
	}

	private IExpression createRandomNonTerminalNumeric(IExpression model, int level) {
		int randomNumber = GPGondolaSelection.getRandomToGenerateInteger();

		if (randomNumber % 2 == 0) {
			return createRandomNonTerminalInteger(model, level);
		} else {
			return createRandomNonTerminalFloat(model, level);
		}

	}

	/**
	 * Create a random non-terminal {@link IExpression#INTEGER_TYPE} expression.
	 *
	 * @param model model expression
	 * @param level tree level
	 * @return randomly generated non-terminal integer expression
	 */
	private IExpression createRandomNonTerminalInteger(IExpression model, int level) {

		IExpression rv = null;

		// If we are mutating an existing number expression then favour
		// just modifying the number's value rather than replacing it
		// with a random expressions. This helps keep the equation size down and
		// favours trying different values.
		if (model != null && model instanceof NumberExpression && FAVOUR_NUMBER_PERCENT > random.nextInt(100)) {

			NumberExpression numberExpression = (NumberExpression) model;
			double step = random.nextDouble() * 6.0D;
			double value = Math.pow(10.0D, step);

			if (random.nextBoolean())
				value = -value;

			numberExpression.setValue(numberExpression.getValue() + value);
			return numberExpression;
		}

		int randomNumber = GPGondolaSelection.getRandomToGenerateInteger();

		if (randomNumber == 0) {
			rv = createRandomTerminal(IExpression.INTEGER_TYPE);

		} else if (randomNumber == 1) {
			rv = new AddExpression(getChild(model, level, 0, IExpression.INTEGER_TYPE),
					getChild(model, level, 1, IExpression.INTEGER_TYPE));

		} else if (randomNumber == 2) {
			rv = new SubtractExpression(getChild(model, level, 0, IExpression.INTEGER_TYPE),
					getChild(model, level, 1, IExpression.INTEGER_TYPE));

		} else if (randomNumber == 3) {
			rv = new MultiplyExpression(getChild(model, level, 0, IExpression.INTEGER_TYPE),
					getChild(model, level, 1, IExpression.INTEGER_TYPE));

		} else if (randomNumber == 4) {
			rv = new DivideExpression(getChild(model, level, 0, IExpression.INTEGER_TYPE),
					getChild(model, level, 1, IExpression.INTEGER_TYPE));

		} else if (randomNumber == 5) {
			rv = new PercentExpression(getChild(model, level, 0, IExpression.INTEGER_TYPE),
					getChild(model, level, 1, IExpression.INTEGER_TYPE));

		} else if (randomNumber == 6) {
			rv = new IfExpression(getChild(model, level, 0, IExpression.BOOLEAN_TYPE),
					getChild(model, level, 1, IExpression.INTEGER_TYPE),
					getChild(model, level, 2, IExpression.INTEGER_TYPE));

		} else if (randomNumber == 7) {
			rv = new LagExpression(new QuoteExpression(IQuote.DAY_VOLUME),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE));

		} else if (randomNumber == 8) {
			rv = new MinExpression(new QuoteExpression(IQuote.DAY_VOLUME),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE),
					getChild(model, level, 2, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE));

		} else if (randomNumber == 9) {
			rv = new MaxExpression(new QuoteExpression(IQuote.DAY_VOLUME),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE),
					getChild(model, level, 2, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE));

		} else if (randomNumber == 10) {
			rv = new SumExpression(new QuoteExpression(IQuote.DAY_VOLUME),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE),
					getChild(model, level, 2, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE));

		} else if (randomNumber == 11) {
			rv = new SqrtExpression(getChild(model, level, 0, IExpression.INTEGER_TYPE));

		} else if (randomNumber == 12) {
			rv = new AbsExpression(getChild(model, level, 0, IExpression.INTEGER_TYPE));

		} else if (randomNumber == 13) {
			rv = new AvgExpression(new QuoteExpression(IQuote.DAY_VOLUME),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE),
					getChild(model, level, 2, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE));

		} else if (randomNumber == 14) {

			rv = new EMAExpression(new QuoteExpression(IQuote.DAY_VOLUME),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE),
					getChild(model, level, 2, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE),
					createRandomTerminal(IExpression.FLOAT_TYPE, this.SMOOTHING_CONSTANT_SUBTYPE));

		} else if (randomNumber == 15) {
			rv = new MACDExpression(new QuoteExpression(IQuote.DAY_VOLUME),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE));

		} else if (randomNumber == 16) {
			rv = new MomentumExpression(new QuoteExpression(IQuote.DAY_VOLUME),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE),
					getChild(model, level, 2, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE));

		} else if (randomNumber == 17) {
			rv = new StandardDeviationExpression(new QuoteExpression(IQuote.DAY_VOLUME),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE),
					getChild(model, level, 2, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE));

		} else if (randomNumber == 18) {
			rv = new BBLExpression(new QuoteExpression(IQuote.DAY_VOLUME),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE),
					getChild(model, level, 2, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE));

		} else if (randomNumber == 19) {
			rv = new BBUExpression(new QuoteExpression(IQuote.DAY_VOLUME),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE),
					getChild(model, level, 2, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE));

		} else {
			assert randomNumber == 20;

			IExpression child1 = getChild(model, level, 0, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE);

			IExpression child2 = getChild(model, level, 0, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE);

			IExpression child3 = getChild(model, level, 0, IExpression.INTEGER_TYPE);

			rv = new OBVExpression(child1, child2, child3);

		}

		return rv;
	}

	/**
	 * Create a random non-terminal {@link IExpression#INTEGER_TYPE} expression. The
	 * number should be a positive short integer
	 * {@link Mutator#POSITIVE_SHORT_INTEGER_SUBTYPE}.
	 *
	 * @param model model expression
	 * @param level tree level
	 * @return randomly generated non-terminal integer expression
	 */
	private IExpression createRandomNonTerminalPositiveShortInteger(IExpression model, int level) {

		// If we are mutating an existing number expression then favour
		// just modifying the number's value rather than replacing it
		// with a random expressions. This helps keep the equation size down and
		// favours trying different values.
		if (model != null && model instanceof NumberExpression && FAVOUR_NUMBER_PERCENT > random.nextInt(100)) {

			NumberExpression numberExpression = (NumberExpression) model;
			double step = random.nextDouble() * 4.0D;
			double value = Math.pow(2.0D, step);

			numberExpression.setValue(numberExpression.getValue() + value);
			return numberExpression;
		}

		int randomNumber = GPGondolaSelection.getRandomToGeneratePositiveShortInteger();

		// MH: I have a feeling that the generator never returns 11 at all
		// which might be a bug.
		if (randomNumber == 11) {
			VeniceLog.getInstance().log("In genShortPosInt, random number = 11");
		}

		if (randomNumber == 0) {
			return createRandomTerminal(IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE);

		} else if (randomNumber == 1) {
			return new AddExpression(
					getChild(model, level, 0, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE));

		} else if (randomNumber == 2) {
			return new SubtractExpression(
					getChild(model, level, 0, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE));

		} else if (randomNumber == 3) {
			return new MultiplyExpression(
					getChild(model, level, 0, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE));

		} else if (randomNumber == 4) {
			return new DivideExpression(
					getChild(model, level, 0, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE));

		} else if (randomNumber == 5) {
			return new PercentExpression(
					getChild(model, level, 0, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE));

		} else if (randomNumber == 6) {
			return new IfExpression(getChild(model, level, 0, IExpression.BOOLEAN_TYPE),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE),
					getChild(model, level, 2, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE));

		} else if (randomNumber == 7) {
			return new SqrtExpression(
					getChild(model, level, 0, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE));

		} else if (randomNumber == 8) {
			return new AbsExpression(
					getChild(model, level, 0, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE));

		} else if (randomNumber == 9) {
			/*
			 * We put also the following model: (+1)*(generic float expression). The generic
			 * float can be got from any econometric function. This permits to use all the
			 * functions in a positive small integer number, the conversion to integer is
			 * obtained with a multiply expression, that is the simplest method to got the
			 * conversion in Gondola.
			 * 
			 * The above is no longer true, and therefore must be done with either ceil() or
			 * floor(). (The conversion by multiply by one caused bugs when large floats get
			 * cast to int)
			 * 
			 */
			return new FloorExpression(getChild(model, level, 1, IExpression.FLOAT_TYPE));
		} else if (randomNumber == 10) {
			return new CeilExpression(getChild(model, level, 1, IExpression.FLOAT_TYPE));
		} else {
			assert randomNumber == 11;
			/*
			 * We put also the following model: (generic integer expression). The generic
			 * integer can be got from any econometric function. This permits to use all the
			 * functions in a positive small integer number.
			 */
			return createRandomNonTerminal(model, IExpression.INTEGER_TYPE, this.NO_SUBTYPE, level);

		}
	}

	/**
	 * Create a random non-terminal {@link IExpression#INTEGER_TYPE} expression. The
	 * number should be a negative small integer
	 * {@link Mutator#NEGATIVE_SHORT_INTEGER_SUBTYPE}.
	 *
	 * @param model model expression
	 * @param level tree level
	 * @return randomly generated non-terminal integer expression
	 */
	private IExpression createRandomNonTerminalNegativeShortInteger(IExpression model, int level) {

		// If we are mutating an existing number expression then favour
		// just modifying the number's value rather than replacing it
		// with a random expressions. This helps keep the equation size down and
		// favours trying different values.
		if (model != null && model instanceof NumberExpression && FAVOUR_NUMBER_PERCENT > random.nextInt(100)) {

			NumberExpression numberExpression = (NumberExpression) model;
			double step = random.nextDouble() * 4.0D;
			double value = Math.pow(2.0D, step);

			numberExpression.setValue(numberExpression.getValue() - value);
			return numberExpression;
		}

		int randomNumber = GPGondolaSelection.getRandomToGenerateNegativeShortInteger();

		// MH: I have a feeling that the generator never returns 7 at all
		// which might be a bug.
		if (randomNumber == 7) {
			VeniceLog.getInstance().log("In genShortNegInt, random number = 7");
		}

		if (randomNumber == 0) {
			return createRandomTerminal(IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE);

		} else if (randomNumber == 1) {
			return new AddExpression(
					getChild(model, level, 0, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE));

		} else if (randomNumber == 2) {
			return new SubtractExpression(
					getChild(model, level, 0, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE));

		} else if (randomNumber == 3) {
			return new MultiplyExpression(
					getChild(model, level, 0, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE));

		} else if (randomNumber == 4) {
			return new DivideExpression(
					getChild(model, level, 0, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE));

		} else if (randomNumber == 5) {
			return new PercentExpression(
					getChild(model, level, 0, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.POSITIVE_SHORT_INTEGER_SUBTYPE));

		} else if (randomNumber == 6) {
			return new IfExpression(getChild(model, level, 0, IExpression.BOOLEAN_TYPE),
					getChild(model, level, 1, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE),
					getChild(model, level, 2, IExpression.INTEGER_TYPE, this.NEGATIVE_SHORT_INTEGER_SUBTYPE));
		} else {
			assert randomNumber == 7;
			/*
			 * We put also the following model: (-1)*(generic integer expression). The
			 * generic integer can be got from any econometric function. This permits to use
			 * all the functions in a negative small integer number.
			 */
			return new MultiplyExpression(new NumberExpression(-1), getChild(model, level, 1, IExpression.INTEGER_TYPE));

		}
	}

	// creates a float or integer type
	private IExpression getChild(IExpression model, int level, int arg) {

		// Case 1: The expression doesn't have this many children or
		// it has a child here but it is a different type. So create
		// a new argument.
		if (model == null || arg >= model.getChildCount() || (model.getChild(arg).getType() != IExpression.FLOAT_TYPE
				&& model.getChild(arg).getType() != IExpression.INTEGER_TYPE)) {

			int randomNumber = GPGondolaSelection.getRandomToGenerateFloatInteger();

			if (randomNumber == 0) {
				return createRandom(null, IExpression.FLOAT_TYPE, this.NO_SUBTYPE, level);
			} else {
				assert randomNumber == 1;
				return createRandom(null, IExpression.INTEGER_TYPE, this.NO_SUBTYPE, level);
			}
		}

		// Case 2: It has an argument of the right type
		else {
			return model.getChild(arg);
		}
	}

	// create a child with no subType defined
	private IExpression getChild(IExpression model, int level, int arg, int type) {
		return getChild(model, level, arg, type, this.NO_SUBTYPE);
	}

	private IExpression getChild(IExpression model, int level, int arg, int type, int subType) {

		IExpression rv;

		// Case 1: The expression doesn't have this many children or
		// it has a child here but it is a different type. So create
		// a new argument.

		// getChild allows index to be equal to the childcount
		// Should arg count test be arg > model.getcc or should
		// the getChild assertion be fixed?
		if (model == null || arg >= model.getChildCount() || model.getChild(arg).getType() != type) {

			// FIXME: Code cleanup - tidy or simplify
			if (model != null && arg < model.getChildCount() && type == IExpression.NUMERIC_TYPE
					&& (model.getChild(arg).getType() == IExpression.INTEGER_TYPE
							|| model.getChild(arg).getType() == IExpression.FLOAT_TYPE)) {

				// Turns out that the argument is of the right type
				// (generic numeric type)
				rv = model.getChild(arg);
			}

			rv = createRandom(null, type, subType, level);
		}

		// Case 2: It has an argument of the right type
		else {
			rv = model.getChild(arg);
		}

		return rv;
	}

	/**
	 * Randomly pick a node in the given expression.
	 *
	 * @param expression the expression to search
	 * @return expression node
	 */
	public IExpression findRandomSite(IExpression expression) {
		int randomNumber = random.nextInt(expression.size());
		IExpression randomSite = null;

		for (Iterator iterator = expression.iterator(); iterator.hasNext();) {

			randomSite = (IExpression) iterator.next();

			// Return if this is the xth random element
			if (randomNumber-- <= 0)
				break;
		}

		assert randomSite != null;

		return randomSite;
	}

	/**
	 * Randomly pick a node of the given type in the given expression.
	 *
	 * @param expression the expression node
	 * @param type       the type of the node, e.g. {@link IExpression#BOOLEAN_TYPE}
	 * @return expression node or <code>null</code> if one could not be found
	 */
	public IExpression findRandomSite(IExpression expression, int type) {
		IExpression randomSite = null;
		int possibleSites = expression.size(type);

		if (possibleSites > 0) {
			int randomNumber = random.nextInt(possibleSites);

			for (Iterator iterator = expression.iterator(); iterator.hasNext();) {

				randomSite = (IExpression) iterator.next();

				// Return if this is the xth random element of the
				// given type
				if (randomSite.getType() == type)
					if (randomNumber-- <= 0)
						break;
			}
			assert randomSite != null;
		}

		return randomSite;
	}

	/**
	 * Perform a deletion mutation on the given expression. Since the mutation might
	 * chance the root of the expression, the updated root is returned. The returned
	 * root may be the same as the one passed in.
	 *
	 * @param root        the root of the expression being mutated
	 * @param destination the destination site for the deletion
	 * @return the new root of the expression
	 */
	public IExpression delete(IExpression root, IExpression destination) {
		return insert(root, destination, createRandomTerminal(destination.getType()));
	}

	/**
	 * Perform an insertion mutation on the given expression.
	 *
	 * @param root        the root of the expression being mutated
	 * @param destination the destination site for the insertion
	 * @param source      the expression to insert
	 * @return the new root of the expression
	 * @see #delete(IExpression root, IExpression destination)
	 */
	public IExpression insert(IExpression root, IExpression destination, IExpression source) {

		IExpression parent = (IExpression) destination.getParent();

		if (parent == null) {
			// If the destination has no parent it must be the root of the tree.
			assert root == destination;
			return source;
		} else {
			int childNumber = parent.getIndex(destination);
			parent = ExpressionFactory.setChild(parent, source, childNumber);
			// parent.setChild(source, childNumber);
			return root;
		}
	}

	/**
	 * Perform a modification mutation on the given expression.
	 *
	 * @param root        the root of the expression being mutated
	 * @param destination the destination site for the modification
	 * @return the new root of the expression
	 * @see #delete(IExpression root, IExpression destination)
	 */
	public IExpression modify(IExpression root, IExpression destination) {
		if (destination == root) {
			IExpression newExpression = createRandom(destination.getType(), this.NO_SUBTYPE, 1);
			newExpression = newExpression.simplify();
			return newExpression;
		} else {

			IExpression newExpression = createRandom(destination, destination.getType(), this.NO_SUBTYPE, 1);

			newExpression = newExpression.simplify();
			return insert(root, destination, newExpression);
		}
	}

	/**
	 * Possibly mutate the given expression.
	 *
	 * @param expression the root of the expression to modify
	 * @return the new root of the expression
	 */
	public IExpression mutate(IExpression expression) {
		return mutate(expression, MUTATION_PERCENT);
	}

	/**
	 * Possibly mutate the given expression
	 *
	 * @param expression the root of the expression to modify
	 * @param percent    percent change of mutation
	 * @return the new root of the expression
	 */
	public IExpression mutate(IExpression expression, int percent) {
		// Mutations do not always occur. Use the given percent to work
		// out whether one should occur.
		int randomPercent = random.nextInt(100);
		if (percent < randomPercent) {
			return expression;
		}

		// Mutate
		int randomTypePercent = random.nextInt(100);
		if (INSERTION_MUTATION_PERCENT > randomTypePercent) {
			expression = mutateByInsertion(expression);
		} else {
			randomTypePercent -= INSERTION_MUTATION_PERCENT;

			if (DELETION_MUTATION_PERCENT > randomTypePercent) {
				expression = mutateByDeletion(expression);
			} else {
				randomTypePercent -= DELETION_MUTATION_PERCENT;

				expression = mutateByModification(expression);
			}
		}

		// There's always the possibility of a 2nd, 3rd, etc mutation. This
		// can be useful if the gene pool is stagnant.
		return mutate(expression, EXTRA_MUTATION_PERCENT);
	}

	/**
	 * Mutate the given expression by modification.
	 *
	 * @param expression the root of the expression to modify
	 * @return the new root of the expression
	 */
	private IExpression mutateByModification(IExpression expression) {
		IExpression destination = findRandomSite(expression);

		return modify(expression, destination);
	}

	/**
	 * Mutate the given expression by insertion.
	 *
	 * @param expression the root of the expression to modify
	 * @return the new root of the expression
	 */
	private IExpression mutateByInsertion(IExpression expression) {
		IExpression destination = findRandomSite(expression);
		IExpression insertSubTree = createRandom(destination.getType());

		return insert(expression, destination, insertSubTree);
	}

	/**
	 * Mutate the given expression by deletion.
	 *
	 * @param expression the root of the expression to modify
	 * @return the new root of the expression
	 */
	private IExpression mutateByDeletion(IExpression expression) {
		IExpression destination = findRandomSite(expression);

		// There's no point in replacing the root node with a terminal
		// expression, and replacing a terminal expression with
		// a random expression is closer to an insertion mutation than
		// deletion. So just skip the whole deletion idea and try a random
		// mutation somewhere.
		if (destination.isRoot() || destination.getChildCount() == 0) {
			return mutateByModification(expression);
		} else {
			return delete(expression, destination);
		}
	}
}
