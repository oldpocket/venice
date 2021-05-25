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

import nz.org.venice.quote.EODQuote;
import nz.org.venice.quote.MetaStockQuoteFilter;
import nz.org.venice.quote.Symbol;
import nz.org.venice.quote.SymbolFormatException;
import nz.org.venice.util.TradingDate;

public class MetaStock2QuoteFilterTest extends TestCase
{
    public void testConvert() {
        EODQuoteFilter filter = new MetaStock2QuoteFilter();
	EODQuote quote = null;

        try {
            quote = new EODQuote(Symbol.find("AAA"), new TradingDate(), 10000,
                                 10.00D, 20.00D, 30.00D, 40.00D);
        }
        catch(SymbolFormatException e) {
            fail("Couldn't create symbol AAA");
        }

        EODQuote filteredQuote = null;
	String filteredString;

        filteredString = filter.toString(quote);

        try {
            filteredQuote = filter.toEODQuote(filteredString);
        }
        catch(QuoteFormatException e) {
            fail("Error parsing '" + filteredString + "'");
        }

        assertEquals(filteredQuote, quote);
    }

    //Number format for German locale uses "," for decimal point   
    //This test will be identical to the above test
    // on systems with a German Default locale but we want bugs like 
    //this to be triggered for all locales. 
    public void testConvertGermanLocale() {
        EODQuoteFilter filter = new MetaStock2QuoteFilter();
	EODQuote quote = null;

	java.util.Locale prevDefault = java.util.Locale.getDefault();
	java.util.Locale.setDefault(java.util.Locale.GERMAN);

        try {
            quote = new EODQuote(Symbol.find("AAA"), new TradingDate(), 10000,
                                 10.00D, 20.00D, 30.00D, 40.00D);
        }
        catch(SymbolFormatException e) {
            fail("Couldn't create symbol AAA");
        }

        EODQuote filteredQuote = null;
	String filteredString;

        filteredString = filter.toString(quote);

        try {
            filteredQuote = filter.toEODQuote(filteredString);
        }
        catch(QuoteFormatException e) {
            fail("Error parsing '" + filteredString + "'");
        } finally {
	    java.util.Locale.setDefault(prevDefault);
	}
        assertEquals(filteredQuote, quote);
    }

}