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

package nz.org.venice.util;

import junit.framework.TestCase;

import nz.org.venice.util.TradingDate;
import nz.org.venice.util.TradingDateFormatException;

public class TradingDateTest extends TestCase {
    public void testParser() {
        TradingDate date = null;

        try {
            // YYMMDD
            date = new TradingDate("071231", TradingDate.BRITISH);
            assertTrue(date.getYear() == 2007 &&
                       date.getMonth() == 12 &&
                       date.getDay() == 31);

            // YYYYMMDD
            date = new TradingDate("20071231", TradingDate.BRITISH);
            assertTrue(date.getYear() == 2007 &&
                       date.getMonth() == 12 &&
                       date.getDay() == 31);

            // YYYY-MM-DD
            date = new TradingDate("2007-12-31", TradingDate.BRITISH);
            assertTrue(date.getYear() == 2007 &&
                       date.getMonth() == 12 &&
                       date.getDay() == 31);

            // YYYY/MM/DD
            date = new TradingDate("2007/12/31", TradingDate.BRITISH);
            assertTrue(date.getYear() == 2007 &&
                       date.getMonth() == 12 &&
                       date.getDay() == 31);

            // DD/MM/YY
            date = new TradingDate("31/12/07", TradingDate.BRITISH);
            assertTrue(date.getYear() == 2007 &&
                       date.getMonth() == 12 &&
                       date.getDay() == 31);

            // DD/MM/YYYY
            date = new TradingDate("31/12/2007", TradingDate.BRITISH);
            assertTrue(date.getYear() == 2007 &&
                       date.getMonth() == 12 &&
                       date.getDay() == 31);

            // MM/DD/YY
            date = new TradingDate("12/31/07", TradingDate.US);
            assertTrue(date.getYear() == 2007 &&
                       date.getMonth() == 12 &&
                       date.getDay() == 31);

            // MM/DD/YYYY
            date = new TradingDate("12/31/2007", TradingDate.US);
            assertTrue(date.getYear() == 2007 &&
                       date.getMonth() == 12 &&
                       date.getDay() == 31);

            // DD-MM-YY
            date = new TradingDate("31-12-07", TradingDate.BRITISH);
            assertTrue(date.getYear() == 2007 &&
                       date.getMonth() == 12 &&
                       date.getDay() == 31);

            // DD-MM-YYYY
            date = new TradingDate("31-12-2007", TradingDate.BRITISH);
            assertTrue(date.getYear() == 2007 &&
                       date.getMonth() == 12 &&
                       date.getDay() == 31);

            // MM-DD-YY
            date = new TradingDate("12-31-07", TradingDate.US);
            assertTrue(date.getYear() == 2007 &&
                       date.getMonth() == 12 &&
                       date.getDay() == 31);

            // MM-DD-YYYY
            date = new TradingDate("12-31-2007", TradingDate.US);
            assertTrue(date.getYear() == 2007 &&
                       date.getMonth() == 12 &&
                       date.getDay() == 31);

            // DD/MON/YY
            date = new TradingDate("31/Dec/07", TradingDate.BRITISH);
            assertTrue(date.getYear() == 2007 &&
                       date.getMonth() == 12 &&
                       date.getDay() == 31);

            // DD/MON/YYYY
            date = new TradingDate("31/Dec/2007", TradingDate.BRITISH);
            assertTrue(date.getYear() == 2007 &&
                       date.getMonth() == 12 &&
                       date.getDay() == 31);

            // DD/MONTH/YY
            date = new TradingDate("31/December/07", TradingDate.BRITISH);
            assertTrue(date.getYear() == 2007 &&
                       date.getMonth() == 12 &&
                       date.getDay() == 31);

            // DD/MONTH/YYYY
            date = new TradingDate("31/December/2007", TradingDate.BRITISH);
            assertTrue(date.getYear() == 2007 &&
                       date.getMonth() == 12 &&
                       date.getDay() == 31);
        }
        catch(TradingDateFormatException e) {
            fail(e.toString());
        }
    }
}
