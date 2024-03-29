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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import nz.org.venice.prefs.PreferencesManager;
import nz.org.venice.quote.EODQuote;
import nz.org.venice.quote.IEODQuoteFilter;
import nz.org.venice.quote.FileEODQuoteImport;
import nz.org.venice.quote.ImportExportException;
import nz.org.venice.quote.ImportQuoteModule;
import nz.org.venice.quote.QuoteFormatException;
import nz.org.venice.quote.Symbol;
import nz.org.venice.util.Find;
import nz.org.venice.util.Locale;
import nz.org.venice.util.Report;
import nz.org.venice.util.TradingDate;

/**
 * Import end-of-day quotes from a Generic Web Service into Venice.
 *
 * @author Andrew Leppard
 * @see FileEODQuoteImport
 * @see ImportQuoteModule
 */
public class GenericWSEODQuoteImport {

	// The following symbols will be replaced by the quote, date range we are after:
	private final static String SYMBOL = "_SYM_";
	private final static String START_DAY = "_SD_";
	private final static String START_MONTH = "_SM_";
	private final static String START_YEAR = "_SY_";
	private final static String END_DAY = "_ED_";
	private final static String END_MONTH = "_EM_";
	private final static String END_YEAR = "_EY_";

	// Retrieve quotes in batches of MAX_NUMBER_OF_RETRIEVAL_DAYS. This
	// limit should be safe since for the Generic Web Service provided.
	private final static int MAX_NUMBER_OF_RETRIEVAL_DAYS = 100;

	// Let's define the URL pattern that must be followed by the Generic Web
	// Service.
	private final static String URL_PATTERN = ("symbol=" + SYMBOL + "&start_month=" + START_MONTH + "&start_day=" + START_DAY + "&start_year="
			+ START_YEAR + "&end_month=" + END_MONTH + "&end_day=" + END_DAY + "&end_year=" + END_YEAR);

	private final static String GENERIC_WS_URL_PATTERN = ("http://aethiopicus.ddns.net:1414/~aethiopicus/yfinance/eod_quotes?" + URL_PATTERN);

	// This class is not instantiated.
	private GenericWSEODQuoteImport() {
		assert false;
	}

	/**
	 * Retrieve quotes from Generic Web Service. Will fire multiple request if the
	 * specified period is above the maximum number of quotes specified by class.
	 *
	 * @param report    report to log warnings and errors
	 * @param symbol    symbol to import
	 * @param suffix    optional suffix to append (e.g. ".AX"). This suffix tells
	 *                  which exchange the symbol belongs to and need to be
	 *                  supported by the Generic Web Service.
	 * @param startDate start of date range to import
	 * @param endDate   end of date range to import
	 * @return list of quotes
	 * @exception ImportExportException if there was an error retrieving the quotes
	 */
	public static List importSymbol(Report report, Symbol symbol, TradingDate startDate,
			TradingDate endDate) throws ImportExportException {

		List result = new ArrayList();

		// retrieve in parts since for better performance.
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
	 * Retrieve quotes from Generic Web Service. Do not exceed the specified
	 * MAX_NUMBER_OF_RETRIEVAL_DAYS!
	 *
	 * @param report    report to log warnings and errors
	 * @param symbol    symbol to import
	 * @param suffix    optional suffix to append (e.g. ".AX"). This suffix tells
	 *                  which exchange the symbol belongs to and need to be
	 *                  supported by the Generic Web Service.
	 * @param startDate start of date range to import
	 * @param endDate   end of date range to import
	 * @return list of quotes
	 * @exception ImportExportException if there was an error retrieving the quotes
	 */
	private static List retrieveQuotes(Report report, Symbol symbol, TradingDate startDate,
			TradingDate endDate) throws ImportExportException {

		List quotes = new ArrayList();
		String URLString = constructURL(symbol, startDate, endDate);
		IEODQuoteFilter filter = new GenericWSEODQuoteFilter(symbol);

		PreferencesManager.ProxyPreferences proxyPreferences = PreferencesManager.getProxySettings();

		try {
			URL url = new URL(URLString);

			InputStreamReader input = new InputStreamReader(url.openStream());
			BufferedReader bufferedInput = new BufferedReader(input);

			// Skip first line as it doesn't contain a quote
			String line = bufferedInput.readLine();

			while (line != null) {
				line = bufferedInput.readLine();

				if (line != null) {
					try {
						EODQuote quote = filter.toEODQuote(line);
						quotes.add(quote);
						verify(report, quote);
					} catch (QuoteFormatException e) {
						report.addError(Locale.getString("YAHOO_DISPLAY_URL") + ":" + symbol + ":"
								+ Locale.getString("ERROR") + ": " + e.getMessage());
					}
				}
			}

			bufferedInput.close();
		}

		catch (BindException e) {
			throw new ImportExportException(Locale.getString("UNABLE_TO_CONNECT_ERROR", e.getMessage()));
		}

		catch (ConnectException e) {
			throw new ImportExportException(Locale.getString("UNABLE_TO_CONNECT_ERROR", e.getMessage()));
		}

		catch (UnknownHostException e) {
			throw new ImportExportException(Locale.getString("UNKNOWN_HOST_ERROR", e.getMessage()));
		}

		catch (NoRouteToHostException e) {
			throw new ImportExportException(Locale.getString("DESTINATION_UNREACHABLE_ERROR", e.getMessage()));
		}

		catch (MalformedURLException e) {
			throw new ImportExportException(
					Locale.getString("INVALID_PROXY_ERROR", proxyPreferences.host, proxyPreferences.port));
		}

		catch (FileNotFoundException e) {
			// Don't abort the import if there are no quotes for a given symbol
			// empty list will be returned
		}

		catch (IOException e) {
			throw new ImportExportException(Locale.getString("ERROR_DOWNLOADING_QUOTES"));
		}

		return quotes;
	}

	/**
	 * Construct the URL necessary to retrieve all the quotes for the given symbol
	 * between the given dates from the Generic Web Service.
	 *
	 * @param symbol the symbol to retrieve
	 * @param suffix optional suffix to append (e.g. ".AX"). This suffix tells which
	 *               exchange the symbol belongs to and need to be supported by the
	 *               Generic Web Service.
	 * @param start  the start date to retrieve
	 * @param end    the end date to retrieve
	 * @return URL string
	 */
	private static String constructURL(Symbol symbol, TradingDate start, TradingDate end) {
		String URLString = GENERIC_WS_URL_PATTERN;
		String symbolString = symbol.getMetaData().toString();
	
		URLString = Find.replace(URLString, SYMBOL, symbolString);

		URLString = Find.replace(URLString, START_DAY, Integer.toString(start.getDay()));
		URLString = Find.replace(URLString, START_MONTH, Integer.toString(start.getMonth()));
		URLString = Find.replace(URLString, START_YEAR, Integer.toString(start.getYear()));
		URLString = Find.replace(URLString, END_DAY, Integer.toString(end.getDay()));
		URLString = Find.replace(URLString, END_MONTH, Integer.toString(end.getMonth()));
		URLString = Find.replace(URLString, END_YEAR, Integer.toString(end.getYear()));
		return URLString;
	}

	/**
	 * Verify the quote is valid. Log any problems to the report and try to clean it
	 * up the best we can.
	 *
	 * @param report the report
	 * @param quote  the quote
	 */
	private static void verify(Report report, EODQuote quote) {
		try {
			quote.verify();
		} catch (QuoteFormatException e) {
			List messages = e.getMessages();

			for (Iterator iterator = messages.iterator(); iterator.hasNext();) {
				String message = (String) iterator.next();

				report.addWarning(Locale.getString("YAHOO_DISPLAY_URL") + ":" + quote.getSymbol() + ":"
						+ quote.getDate() + ":" + Locale.getString("WARNING") + ": " + message);
			}
		}
	}
}
