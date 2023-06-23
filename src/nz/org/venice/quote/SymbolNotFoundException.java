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

package nz.org.venice.quote;

/**
* An exception which is raised when a symbol is not found
*
* @author Andrew Leppard
*/
public class SymbolNotFoundException extends Throwable {

	private static final long serialVersionUID = 1L;

	/**
	 * Create a new symbol not found exception with the given error reason.
	 *
	 * @param message the reason why the symbol was not found
	 */
	public SymbolNotFoundException(String message) {
		super(message);
	}
}
