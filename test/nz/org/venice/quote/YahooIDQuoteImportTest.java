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

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import nz.org.venice.quote.ImportExportException;
import nz.org.venice.quote.SymbolFormatException;
import nz.org.venice.quote.YahooIDQuoteImport;

/**
 * Test the YahooIDQuoteImport class by downloading some quotes.
 *
 * @author Andrew Leppard
 */
public class YahooIDQuoteImportTest extends TestCase
{
    public void testImport() {
        try {
            List symbols = new ArrayList(Symbol.toSortedSet("CSCO, GE, MSFT, IBM", false));
            List quotes = YahooIDQuoteImport.importSymbols(symbols, "");

            // Check we downloaded the right number of quotes
            assertEquals(symbols.size(), quotes.size());

            // Check all the symbols are correct
            if(quotes.size() == symbols.size()) {
                Iterator symbolIterator = symbols.iterator();
                Iterator quoteIterator = quotes.iterator();

                while(symbolIterator.hasNext()) {
                    Symbol expectedSymbol = (Symbol)symbolIterator.next();
                    IDQuote quote = (IDQuote)quoteIterator.next();
                    
                    assertEquals(expectedSymbol, quote.getSymbol());
                    System.out.println(quote);
                }
            }
        }
        catch(SymbolFormatException e) {
            fail(e.getMessage());
        }

        catch(ImportExportException e) {
            fail(e.getMessage());
        }

    }
}


