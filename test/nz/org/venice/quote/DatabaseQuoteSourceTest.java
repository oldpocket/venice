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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import nz.org.venice.quote.DatabaseManager;
import nz.org.venice.quote.DatabaseQuoteSource;
import nz.org.venice.quote.EODQuote;
import nz.org.venice.quote.Symbol;
import nz.org.venice.quote.SymbolFormatException;
import nz.org.venice.util.TradingDate;

import nz.org.venice.quote.MissingQuoteException;

public class DatabaseQuoteSourceTest extends TestCase
{
    public void testAll() {
        //
        // Setup environment
        //

        // Delete old database
        try {
            File oldDatabase = new File(".database.log");
            oldDatabase.delete();
            oldDatabase = new File(".database.properties");
            oldDatabase.delete();
            oldDatabase = new File(".database.script");
            oldDatabase.delete();
        }
        catch(SecurityException e) {
            // Don't care
        }

        String databaseFile = null;
        File tempFile;

        try {
            tempFile = new File(".database");
            databaseFile = tempFile.getCanonicalPath();
        }
        catch(IOException e) {
            fail(e.getMessage());
        }

        Symbol CBA = null;
        Symbol WBC = null;
        Symbol ANZ = null;

        try {
            CBA = Symbol.find("CBA");
            WBC = Symbol.find("WBC");
            ANZ = Symbol.find("ANZ");
        }
        catch(SymbolFormatException e) {
            fail(e.getMessage());
        }

	DatabaseManager databaseManager = new DatabaseManager(databaseFile);
        DatabaseQuoteSource database = new DatabaseQuoteSource(databaseManager);

        //
        // Test import
        //

        // The database is empty so these symbols should not be found
        assertFalse(database.symbolExists(CBA));
        assertFalse(database.symbolExists(WBC));
        assertFalse(database.symbolExists(ANZ));

        List importedQuotes = new ArrayList();
        importedQuotes.add(new EODQuote(CBA, new TradingDate(2005, 9, 16),
                                        1000, 12.0D, 12.0D, 12.0D, 12.0D));
        importedQuotes.add(new EODQuote(CBA, new TradingDate(2005, 9, 15),
                                        1000, 12.0D, 12.0D, 12.0D, 12.0D));
        importedQuotes.add(new EODQuote(ANZ, new TradingDate(2005, 9, 16),
                                        1000, 12.0D, 12.0D, 12.0D, 12.0D));
        importedQuotes.add(new EODQuote(WBC, new TradingDate(2005, 9, 16),
                                        1000, 12.0D, 12.0D, 12.0D, 12.0D));
        importedQuotes.add(new EODQuote(CBA, new TradingDate(2005, 9, 14),
                                        1000, 12.0D, 12.0D, 12.0D, 12.0D));

        int importCount = database.importQuotes(importedQuotes);
        assertEquals(importCount, 5);

        assertTrue(database.symbolExists(CBA));
        assertTrue(database.symbolExists(WBC));
        assertTrue(database.symbolExists(ANZ));

        // Re-import existing quotes with one new quote
        importedQuotes.add(new EODQuote(CBA, new TradingDate(2005, 9, 13),
                                        1000, 12.0D, 12.0D, 12.0D, 12.0D));
        importCount = database.importQuotes(importedQuotes);
        assertEquals(importCount, 1);

        //
        // Test date ranges
        //

        assertEquals(database.getFirstDate(), new TradingDate(2005, 9, 13));
        assertEquals(database.getLastDate(), new TradingDate(2005, 9, 16));
        assertFalse(database.containsDate(new TradingDate(2005, 9, 12)));
        assertTrue(database.containsDate(new TradingDate(2005, 9, 13)));
        assertTrue(database.containsDate(new TradingDate(2005, 9, 14)));
        assertTrue(database.containsDate(new TradingDate(2005, 9, 15)));
        assertTrue(database.containsDate(new TradingDate(2005, 9, 16)));
        assertFalse(database.containsDate(new TradingDate(2005, 9, 17)));

        List dates = database.getDates();
        assertEquals(dates.size(), 4);
        Collections.sort(dates);
        assertEquals(dates.get(0), new TradingDate(2005, 9, 13));
        assertEquals(dates.get(1), new TradingDate(2005, 9, 14));
        assertEquals(dates.get(2), new TradingDate(2005, 9, 15));
        assertEquals(dates.get(3), new TradingDate(2005, 9, 16));
    }

    //Test that bulk date AdvanceDecline returns the same data
    //as cumulating individual dates
    
    //This test is sensitive to bad data 
    //(ie if you've got data for non trading days you will get different results
    // for cumulutive and calculating one day at a time)
    public void testAdvanceDecline() {
	TradingDate firstDate = QuoteSourceManager.getSource().getFirstDate();
        TradingDate lastDate = QuoteSourceManager.getSource().getLastDate();
	
	//Don't want the whole database for this test.
	//So we move firstDate until it's less than maxDaysdBetween lastdate
	int maxDaysBetween = 50;
       
	TradingDate prevDate = lastDate.previous(maxDaysBetween);	
	if (prevDate.before(firstDate)) {
	} else {
	    firstDate = prevDate;
	}
	       
	//Get advance decline by individual dates
	int cumTotalByDates = 0;
	int cumTotalSingle = 0;

	List dates = TradingDate.dateRangeToList(firstDate, lastDate);
	Iterator iterator = dates.iterator();

	try {
	    int value = 0;
	    while (iterator.hasNext()) {
		TradingDate date = (TradingDate)iterator.next();		
		value = QuoteSourceManager.getSource().getAdvanceDecline(date);					
		cumTotalByDates += value; 

	    }

	    //Test against generating adv/dec using a pair of SQL queries	    
	    HashMap advanceDeclines = QuoteSourceManager.getSource().getAdvanceDecline(firstDate, lastDate);	    	    
	    SortedSet sortedAdvDeclines = new TreeSet(advanceDeclines.keySet());

	    iterator =  sortedAdvDeclines.iterator();
	    while (iterator.hasNext()) {
		TradingDate date = (TradingDate)iterator.next();
		Integer advanceDeclineValue = 
		    (Integer)advanceDeclines.get(date);
		
		cumTotalSingle += advanceDeclineValue.intValue();
	    }

	    if (cumTotalByDates != cumTotalSingle) {
		fail("Expected: " + cumTotalByDates + " got: " + cumTotalSingle);
	    }	
	} catch (MissingQuoteException e) {
	    
	}
    }
}
