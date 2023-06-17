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
import nz.org.venice.quote.IDQuote;
import nz.org.venice.quote.IIDQuoteFilter;
import nz.org.venice.quote.IQuote;
import nz.org.venice.quote.ImportExportException;
import nz.org.venice.quote.QuoteFormatException;
import nz.org.venice.quote.Symbol;
import nz.org.venice.util.Find;
import nz.org.venice.util.Locale;

/**
 * Import intra-day quotes from Web Service into Venice.
 *
 * @author Andrew Leppard
 */
public class GenericWSIDQuoteImport {

	// The following symbols will be replaced by the quote, date range we are after:
	private final static String SYMBOLS = "_SYM_";

	// Let's define the URL pattern that must be followed by the Generic Web
	// Service.
	private final static String URL_PATTERN = ("s=" + SYMBOLS);

	private final static String GENERIC_WS_URL_PATTERN = ("http://aethiopicus.ddns.net:1414/~aethiopicus/cgi-bin/hello.py?" + URL_PATTERN);

	// This class is not instantiated.
	private GenericWSIDQuoteImport() {
		assert false;
	}

	/**
	 * Retrieve intra-day quotes from Web Service.
	 *
	 * @param symbols the symbols to import.
	 * @param suffix  optional suffix to append (e.g. ".AX"). This suffix tells
	 *                which exchange the symbol belongs to and need to be supported
	 *                by the Generic Web Service.
	 * @exception ImportExportException if there was an error retrieving the quotes
	 */
	public static List<IQuote> importSymbols(List<Symbol> symbols) throws ImportExportException {

		List<IQuote> quotes = new ArrayList<IQuote>();
		String URLString = constructURL(symbols);
		IIDQuoteFilter filter = new GenericWSIDQuoteFilter();

		PreferencesManager.ProxyPreferences proxyPreferences = PreferencesManager.getProxySettings();

		try {
			URL url = new URL(URLString);

			InputStreamReader input = new InputStreamReader(url.openStream());
			BufferedReader bufferedInput = new BufferedReader(input);

			// Skip first line as it doesn't contain a quote
			String line = bufferedInput.readLine();

			do {
				line = bufferedInput.readLine();

				if (line != null) {
					try {
						IDQuote quote = filter.toIDQuote(line);
						quote.verify();
						quotes.add(quote);
					} catch (QuoteFormatException e) {
						// Ignore
					}
				}
			} while (line != null);

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
			throw new ImportExportException(Locale.getString("ERROR_DOWNLOADING_QUOTES"));
		}

		catch (IOException e) {
			throw new ImportExportException(Locale.getString("ERROR_DOWNLOADING_QUOTES"));
		}

		return quotes;
	}

	/**
	 * Construct the URL necessary to retrieve all the quotes for the given symbol
	 * between the given dates from Web Service.
	 *
	 * @param symbols the symbos to import.
	 * @param suffix  optional suffix to append (e.g. ".AX"). This suffix tells
	 *                which exchange the symbol belongs to and need to be supported
	 *                by the Generic Web Service.
	 * @return URL string
	 */
	private static String constructURL(List<Symbol> symbols) {
		String URLString = GENERIC_WS_URL_PATTERN;
		String symbolStringList = "";

		// Construct a plus separated list of symbols, e.g. IBM+MSFT+...
		for (Iterator<Symbol> iterator = symbols.iterator(); iterator.hasNext();) {
			Symbol symbol = iterator.next();
			
			String symbolString = symbol.getMetaData().toString();

			symbolStringList += symbolString;

			if (iterator.hasNext())
				symbolStringList += "%2B"; // character '+' url encoded
		}

		URLString = Find.replace(URLString, SYMBOLS, symbolStringList);
		return URLString;
	}
}
