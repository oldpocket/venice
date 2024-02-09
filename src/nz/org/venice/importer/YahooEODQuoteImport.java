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

package nz.org.venice.importer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.MalformedURLException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.net.URL;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

import nz.org.venice.prefs.PreferencesManager;
import nz.org.venice.quote.EODQuote;
import nz.org.venice.quote.IEODQuoteFilter;
import nz.org.venice.quote.ImportExportException;
import nz.org.venice.quote.QuoteFormatException;
import nz.org.venice.quote.Symbol;
import nz.org.venice.util.Find;
import nz.org.venice.util.Locale;
import nz.org.venice.util.Report;
import nz.org.venice.util.TradingDate;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

/**
 * Import end-of-day quotes from finance.yahoo.com into Venice.
 *
 * @author Andrew Leppard
 * @see FileEODQuoteImport
 * @see ImportQuoteModule
 */
public class YahooEODQuoteImport {

	// Retrieve quotes in batches of MAX_NUMBER_OF_RETRIEVAL_DAYS. This
	// limit should be safe since for the Generic Web Service provided.
	private final static int MAX_NUMBER_OF_RETRIEVAL_DAYS = 100;
	
    // This class is not instantiated.
    private YahooEODQuoteImport() {
        assert false;
    }

    /**
     * Retrieve quotes from Yahoo. Will fire multiple request
     * if the specified period is above the maximum number of
     * quotes yahoo supports.
     *
     * @param report report to log warnings and errors
     * @param symbol symbol to import
     * @param suffix optional suffix to append (e.g. ".AX"). This suffix tells
     *               Yahoo which exchange the symbol belongs to.
     * @param startDate start of date range to import
     * @param endDate end of date range to import
     * @return list of quotes
     * @exception ImportExportException if there was an error retrieving the quotes
     */
	public static List importSymbol(Report report, Symbol symbol, TradingDate startDate,
			TradingDate endDate) throws ImportExportException {

		List result = new ArrayList();

        // retrieve in parts since Yahoo only provides quotes for a limited time period.
        TradingDate retrievalStartDate;
        TradingDate retrievalEndDate = endDate;

		do {
			// determine startDate for retrieval
			retrievalStartDate = retrievalEndDate.previous(MAX_NUMBER_OF_RETRIEVAL_DAYS);
			if (retrievalStartDate.before(startDate)) {
				retrievalStartDate = startDate;
			}
			// retrieve quotes and add to result
			List quotes = retrieveQuotes(report, symbol, retrievalStartDate, retrievalEndDate);
			result.addAll(quotes);

			// determine endDate for next retrieval
			retrievalEndDate = retrievalStartDate.previous(1);
		} while (!retrievalEndDate.before(startDate));

		if (result.size() == 0) {
			report.addError(Locale.getString("YAHOO_DISPLAY_URL") + ":" + symbol + ":" + Locale.getString("ERROR")
					+ ": " + Locale.getString("NO_QUOTES_FOUND"));
		}
		return result;
    }

    /**
     * Retrieve quotes from Yahoo.
     * Do not exceed the specified MAX_NUMBER_OF_RETRIEVAL_DAYS!
     *
     * @param report report to log warnings and errors
     * @param symbol symbol to import
     * @param startDate start of date range to import
     * @param endDate end of date range to import
     * @return list of quotes
     * @exception ImportExportException if there was an error retrieving the quotes
     */
    private static List retrieveQuotes(Report report, Symbol symbol,
                                       TradingDate startDate, TradingDate endDate)
    	throws ImportExportException {

        List<EODQuote> quotes = new ArrayList<EODQuote>();
        IEODQuoteFilter filter = new YahooEODQuoteFilter(symbol);

        List<HistoricalQuote> history;
		try {
			
			Stock stock = YahooFinance.get(symbol.toString(), 
						startDate.toCalendar(), 
						endDate.toCalendar(), 
						Interval.DAILY);
			
			history = stock.getHistory();
			
			for(HistoricalQuote historicalQuote : history) {
				try {
				    EODQuote quote = filter.toEODQuote(historicalQuote.toString());
				    quotes.add(quote);
				    verify(report, quote);
				}
				catch(QuoteFormatException e) {
				    report.addError(Locale.getString("YAHOO_DISPLAY_URL") + ":" +
				                    symbol + ":" +
				                    Locale.getString("ERROR") + ": " +
				                    e.getMessage());
				}
			}
			
		} catch (IOException e) {
			throw new ImportExportException(e.getMessage());
		}
      
        return quotes;
    }
  

    /**
     * Verify the quote is valid. Log any problems to the report and try to clean
     * it up the best we can.
     *
     * @param report the report
     * @param quote the quote
     */
    private static void verify(Report report, EODQuote quote) {
        try {
            quote.verify();
        }
        catch(QuoteFormatException e) {
            List messages = e.getMessages();

            for(Iterator iterator = messages.iterator(); iterator.hasNext();) {
                String message = (String)iterator.next();

                report.addWarning(Locale.getString("YAHOO_DISPLAY_URL") + ":" +
                                  quote.getSymbol() + ":" +
                                  quote.getDate() + ":" +
                                  Locale.getString("WARNING") + ": " +
                                  message);
            }
        }
    }
}
