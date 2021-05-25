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

import junit.framework.TestCase;

import java.util.Vector;


import nz.org.venice.parser.expression.NumberExpression;
import nz.org.venice.parser.expression.AddExpression;
import nz.org.venice.parser.expression.MultiplyExpression;
import nz.org.venice.parser.expression.GreaterThanExpression;
import nz.org.venice.parser.expression.AndExpression;
import nz.org.venice.parser.expression.OrExpression;
import nz.org.venice.parser.expression.EqualThanExpression;
import nz.org.venice.parser.expression.NotEqualExpression;
import nz.org.venice.parser.expression.DefineVariableExpression;
import nz.org.venice.parser.expression.SetVariableExpression;
import nz.org.venice.parser.expression.FunctionExpression;
import nz.org.venice.parser.expression.DefineParameterExpression;
import nz.org.venice.parser.expression.ClauseExpression;



public class ExpressionHashCodeTest extends TestCase {
    
    public void testNumber() {
	//Number fields are type and value
	Expression num1 = new NumberExpression(3.1415926);
	Expression num2 = new NumberExpression(3);
	Expression num3 = new NumberExpression(4);
	Expression num4 = new NumberExpression(3.141592);
	Expression num5 = new NumberExpression(true);
	Expression num6 = new NumberExpression(false);

	Expression num7 = new NumberExpression(4);
	
	assertTrue(num1.hashCode() != num2.hashCode());	
	assertTrue(num2.hashCode() != num3.hashCode());	
	assertTrue(num1.hashCode() != num4.hashCode());	
	assertTrue(num5.hashCode() != num6.hashCode());	

	assertTrue(num7.hashCode() == num3.hashCode());	
	
    }

    public void testBinaryExpressions() {
	Expression num1 = new NumberExpression(2);
	Expression num2 = new NumberExpression(3.14159);
	Expression num3 = new NumberExpression(1.5);
		
	Expression multExpression1 = new MultiplyExpression(num1, num2);
	Expression multExpression2 = new MultiplyExpression(num2, num1);
	Expression multExpression3 = new MultiplyExpression(num2, num3);

	Expression addExpression1 = new AddExpression(num1, num2);
	Expression addExpression2 = new AddExpression(num2, num1);
	Expression addExpression3 = new AddExpression(num2, num3);

	

	checkContract(multExpression1,
		      multExpression2,
		      true);
	
	checkContract(multExpression1,
		      multExpression3,
		      false);

	checkContract(addExpression1,
		      addExpression2,
		      true);

	checkContract(addExpression1,
		      addExpression3,
		      false);
	
	Expression gt1 = new GreaterThanExpression(num1, num2);
	Expression gt2 = new GreaterThanExpression(num2, num1);
	Expression or1 = new OrExpression(gt1, gt2);
	Expression or2 = new OrExpression(gt2, gt1);
	Expression and1 = new AndExpression(gt1, gt2);
	Expression and2 = new AndExpression(gt2, gt1);
	
	Expression eq1 = new NotEqualExpression(num1, num3);
	Expression eq2 = new NotEqualExpression(num3, num1);
	Expression eq3 = new NotEqualExpression(num1, num2);

	Expression not1 = new NotEqualExpression(num1, num3);
	Expression not2 = new NotEqualExpression(num3, num1);
	Expression not3 = new NotEqualExpression(num1, num2);

	
	checkContract(gt1,
		      gt2,
		      false);
	

	checkContract(or1,
		      or2,
		      true);
	
	checkContract(and1,
		      and2,
		      true);

	checkContract(eq1,
		      eq2,
		      true);

	checkContract(eq1,
		      eq3,
		      false);

	checkContract(not1,
		      not2,
		      true);

	checkContract(not1,
		      not3,
		      false);
	
    }

    public void testVariableExpression() {
	Expression num1 = new NumberExpression(3);
	Expression num2 = new NumberExpression(3.14);

	Expression dfe1 = new DefineVariableExpression("foo", Expression.INTEGER_TYPE, true, num1);

	Expression dfe11 = new DefineVariableExpression("foo", Expression.INTEGER_TYPE, true, num1);

	Expression dfe2 = new DefineVariableExpression("foo", Expression.INTEGER_TYPE, false, num1);

	Expression dfe3 = new DefineVariableExpression("foo", Expression.FLOAT_TYPE, true, num2);

	Expression dfe4 = new DefineVariableExpression("bar", Expression.FLOAT_TYPE, true, num2);

	Expression dfe5 = new DefineVariableExpression("foo", Expression.INTEGER_TYPE, true, num1);

	Expression dfe6 = new DefineVariableExpression("foo", Expression.FLOAT_TYPE, true, num2);

	checkContract(dfe1, dfe2, false);
	checkContract(dfe3, dfe4, false);		
	checkContract(dfe5, dfe6, false);
	checkContract(dfe1, dfe11, true);

	Expression sfe1 = new SetVariableExpression("foo", Expression.INTEGER_TYPE, num1);

	Expression sfe2 = new SetVariableExpression("foo", Expression.FLOAT_TYPE, num2);
	
	Expression sfe3 = new SetVariableExpression("foo", Expression.FLOAT_TYPE, num2);

	Expression sfe4 = new SetVariableExpression("bar", Expression.FLOAT_TYPE, num2);

	Expression sfe5 = new SetVariableExpression("bar", Expression.FLOAT_TYPE, num2);

	checkContract(sfe1, sfe2, false);
	checkContract(sfe3, sfe4, false);
	checkContract(sfe4, sfe5, true);


    }

    public void testClauseExpression() {
	String test1 = "int n = 0\nfor (int i = 0; i < 10; i = i + 1) { n = n + 1} n";
	String test2 = "int n = 0\nfor (int i = 0; i < 10; i = i + 1) { n = n + 1} n";

	Expression exp1 = parse(test1, Expression.INTEGER_TYPE);
	Expression exp2 = parse(test2, Expression.INTEGER_TYPE);
		
    }

    public void testFunctionExpression() {
	String bodyStr = "for (int k = 0; k < 10; k = k + 1) { k = k + 1}";
	String bodyStr2 = "if (close > open) { true } else {false}";
	Expression body = parse(bodyStr, Expression.INTEGER_TYPE);
	Expression body2 = parse(bodyStr2, Expression.INTEGER_TYPE);
	
	Expression parm1 = new DefineParameterExpression("n", 
							 Expression.INTEGER_TYPE);

	Expression parm2 = new DefineParameterExpression("n", 
							 Expression.INTEGER_TYPE);
	
	Expression parm3 = new DefineParameterExpression("n", 
							 Expression.BOOLEAN_TYPE);
	Expression parm4 = new DefineParameterExpression("i", 
							 Expression.INTEGER_TYPE);
	
	Expression parameters = createParameterExpressions(parm1);
	Expression parameters2 = createParameterExpressions(parm4);


	Expression fe1 = new FunctionExpression("foo", 
						Expression.INTEGER_TYPE, 
						parameters,
						body);

	Expression fe2 = new FunctionExpression("bar", 
						Expression.INTEGER_TYPE, 
						parameters,
						body);

	Expression fe3 = new FunctionExpression("foo", 
						Expression.INTEGER_TYPE, 
						parameters,
						body);

	Expression fe4 = new FunctionExpression("foo", 
						Expression.INTEGER_TYPE, 
						parameters2,
						body);

	Expression fe5 = new FunctionExpression("foo", 
						Expression.INTEGER_TYPE, 
						parameters,
						body2);
		
	checkContract(parm1, parm2, true);
	checkContract(parm1, parm3, false);
	checkContract(fe1, fe2, false);
	checkContract(fe1, fe3, true);
	checkContract(fe1, fe4, false);
	checkContract(fe1, fe5, false);

	
    }

    private Expression createParameterExpressions(Expression parameter) {
	Vector parmList = new Vector();
	parmList.add(parameter);
	Expression parameters = new ClauseExpression(parmList);

	return parameters;
    }

    private void checkContract(Expression e1, Expression e2, boolean equal) {
	if (equal) {
	    assertTrue(e1.equals(e2));
	    assertTrue(e2.equals(e1));
	    assertTrue(e1.hashCode() == e2.hashCode());
	} else {
	    assertTrue(!e1.equals(e2));
	    assertTrue(!e2.equals(e1));
	    assertTrue(e1.hashCode() != e2.hashCode());
	}
    }
    
    private Expression parse(String string) {
	return parse(string, Expression.INTEGER_TYPE);	
    }

    private Expression parse(String string, int type) {
        try {
            Variables variables = new Variables();
            variables.add("x", type, false);
            variables.add("y", type, false);
            variables.add("a", Expression.BOOLEAN_TYPE, false);
            variables.add("b", Expression.BOOLEAN_TYPE, false);
            variables.add("c", Expression.BOOLEAN_TYPE, false);

            return Parser.parse(variables, string);
        }
        catch(ExpressionException e) {
            System.out.println(e);
            assert false;
            return null;
        }
    }

    private boolean withinEpsilon(double val, double testVal) {
	double epsilon = 0.000005;
	
	if (val - epsilon <= testVal &&
	    val + epsilon >= testVal) {
	    return true;
	}
	return false;
    } 

}
