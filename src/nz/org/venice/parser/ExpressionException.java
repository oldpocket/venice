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
 * An exception which is raised when there is a problem parsing or executing an
 * expression.
 */
public class ExpressionException extends Throwable {

	private String reason = null;

	/**
	 * Create a new expression exception with the given error reason.
	 *
	 * @param reason the reason the exception was thrown
	 */
	public ExpressionException(String reason) {
		this.reason = reason;
	}

	/**
	 * Return the reason this exception was raised.
	 *
	 * @return the reason the exception was raised
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * Convert the exception to a string
	 *
	 * @return string version of the exception
	 */
	public String toString() {
		return getReason();
	}
}
