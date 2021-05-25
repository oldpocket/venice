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

import nz.org.venice.quote.EODQuoteRange;
import nz.org.venice.quote.Symbol;
import nz.org.venice.quote.SymbolFormatException;
import nz.org.venice.util.TradingDate;

public class EODQuoteRangeTest extends TestCase
{
    public void testOverlapRange() {

        EODQuoteRange quoteRange, quoteRange2, clippedQuoteRange;
        Symbol CBA = null;

        try {
            CBA = Symbol.find("CBA");
        }
        catch(SymbolFormatException e) {
            fail("Couldn't create symbol CBA");
        }

        // Test Partial Overlap
        //
        // quoteRange      [----------]
        // quoteRange2  [----------]    -> [-]
        quoteRange = new EODQuoteRange(CBA, new TradingDate(2000, 1, 1),
                                       new TradingDate(2000, 12, 1));
        quoteRange2 = new EODQuoteRange(CBA, new TradingDate(1999, 1, 1),
                                        new TradingDate(2000, 6, 6));
        clippedQuoteRange = quoteRange.clip(quoteRange2);

        assertTrue(clippedQuoteRange.getFirstDate().equals(new TradingDate(1999, 1, 1)));
        assertTrue(clippedQuoteRange.getLastDate().equals(new TradingDate(1999, 12, 31)));

        // Test Partial Overlap
        //
        //
        // quoteRange   [----------]
        // quoteRange2     [----------]  ->              [-]
        quoteRange = new EODQuoteRange(CBA, new TradingDate(2000, 1, 1),
                                       new TradingDate(2000, 12, 1));
        quoteRange2 = new EODQuoteRange(CBA, new TradingDate(1999, 1, 1),
                                        new TradingDate(2000, 6, 6));
        clippedQuoteRange = quoteRange2.clip(quoteRange);

        assertTrue(clippedQuoteRange.getFirstDate().equals(new TradingDate(2000, 6, 7)));
        assertTrue(clippedQuoteRange.getLastDate().equals(new TradingDate(2000, 12, 1)));

        // Test Two Identical Ranges
        //
        // quoteRange   [----------]    -> null
        // quoteRange2  [----------]
        quoteRange = new EODQuoteRange(CBA, new TradingDate(2000, 1, 1),
                                       new TradingDate(2000, 12, 1));
        quoteRange2 = new EODQuoteRange(CBA, new TradingDate(2000, 1, 1),
                                        new TradingDate(2000, 12, 1));
        clippedQuoteRange = quoteRange2.clip(quoteRange);

        assertTrue(clippedQuoteRange.isEmpty());

        // Test Contained
        // quoteRange   [----------]    -> [----------]
        // quoteRange2      [--]
        quoteRange = new EODQuoteRange(CBA, new TradingDate(2000, 1, 1),
                                       new TradingDate(2000, 12, 1));
        quoteRange2 = new EODQuoteRange(CBA, new TradingDate(2000, 3, 1),
                                        new TradingDate(2000, 9, 1));
        clippedQuoteRange = quoteRange2.clip(quoteRange);

        assertTrue(clippedQuoteRange.getFirstDate().equals(new TradingDate(2000, 1, 1)));
        assertTrue(clippedQuoteRange.getLastDate().equals(new TradingDate(2000, 12, 1)));

        // Test Contains
        // quoteRange       [--]        -> null
        // quoteRange2  [----------]
        quoteRange = new EODQuoteRange(CBA, new TradingDate(2000, 3, 1),
                                       new TradingDate(2000, 9, 1));
        quoteRange2 = new EODQuoteRange(CBA, new TradingDate(2000, 1, 1),
                                        new TradingDate(2000, 12, 1));
        clippedQuoteRange = quoteRange2.clip(quoteRange);

        assertTrue(clippedQuoteRange.isEmpty());

   }
}



