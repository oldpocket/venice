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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import nz.org.venice.prefs.PreferencesManager;
import nz.org.venice.util.DatabaseAccessManager;
import nz.org.venice.util.DatabaseHelper;
import nz.org.venice.util.DatabaseManager;

/**
 * Contains static methods for creating File-, Sample- and DatabaseQuoteSources
 * based on the user preference.
 * 
 * @author Pontus Strömdahl
 * @see FileQuoteSource
 * @see DatabaseQuoteSource
 * @see nz.org.venice.prefs.PreferencesManager
 */
public class QuoteSourceFactory {

	/**
	 * Create an internal database quote source.
	 *
	 * @return the database quote source
	 */
	public static DatabaseQuoteSource createInternalQuoteSource() {

		return new DatabaseQuoteSource(DatabaseHelper.getDatabaseManager());
	}

	/**
	 * Create A file quote source directly using the in-built sample files as the
	 * quotes.
	 *
	 * @return the file quote source
	 */
	public static FileQuoteSource createSamplesQuoteSource() {
		String[] fileNames = { "nz/org/venice/quote/samples/01-12-86.txt", "nz/org/venice/quote/samples/02-12-86.txt",
				"nz/org/venice/quote/samples/03-11-86.txt", "nz/org/venice/quote/samples/03-12-86.txt",
				"nz/org/venice/quote/samples/04-11-86.txt", "nz/org/venice/quote/samples/04-12-86.txt",
				"nz/org/venice/quote/samples/05-11-86.txt", "nz/org/venice/quote/samples/05-12-86.txt",
				"nz/org/venice/quote/samples/06-11-86.txt", "nz/org/venice/quote/samples/07-11-86.txt",
				"nz/org/venice/quote/samples/08-12-86.txt", "nz/org/venice/quote/samples/09-12-86.txt",
				"nz/org/venice/quote/samples/10-11-86.txt", "nz/org/venice/quote/samples/10-12-86.txt",
				"nz/org/venice/quote/samples/11-11-86.txt", "nz/org/venice/quote/samples/11-12-86.txt",
				"nz/org/venice/quote/samples/12-11-86.txt", "nz/org/venice/quote/samples/12-12-86.txt",
				"nz/org/venice/quote/samples/13-11-86.txt", "nz/org/venice/quote/samples/14-11-86.txt",
				"nz/org/venice/quote/samples/15-12-86.txt", "nz/org/venice/quote/samples/16-12-86.txt",
				"nz/org/venice/quote/samples/17-11-86.txt", "nz/org/venice/quote/samples/17-12-86.txt",
				"nz/org/venice/quote/samples/18-11-86.txt", "nz/org/venice/quote/samples/18-12-86.txt",
				"nz/org/venice/quote/samples/19-11-86.txt", "nz/org/venice/quote/samples/19-12-86.txt",
				"nz/org/venice/quote/samples/20-11-86.txt", "nz/org/venice/quote/samples/21-11-86.txt",
				"nz/org/venice/quote/samples/22-12-86.txt", "nz/org/venice/quote/samples/23-12-86.txt",
				"nz/org/venice/quote/samples/24-11-86.txt", "nz/org/venice/quote/samples/24-12-86.txt",
				"nz/org/venice/quote/samples/25-11-86.txt", "nz/org/venice/quote/samples/25-12-86.txt",
				"nz/org/venice/quote/samples/26-11-86.txt", "nz/org/venice/quote/samples/26-12-86.txt",
				"nz/org/venice/quote/samples/27-11-86.txt", "nz/org/venice/quote/samples/28-11-86.txt",
				"nz/org/venice/quote/samples/29-12-86.txt", "nz/org/venice/quote/samples/30-12-86.txt",
				"nz/org/venice/quote/samples/31-12-86.txt" };

		List fileURLs = new ArrayList();
		for (int i = 0; i < fileNames.length; i++) {
			URL fileURL = ClassLoader.getSystemResource(fileNames[i]);

			if (fileURL != null)
				fileURLs.add(fileURL);
			else
				assert false;
		}

		return new FileQuoteSource("EzyChart", fileURLs);
	}

	/**
	 * Create a database quote source directly using the user preferences.
	 *
	 * @return the database quote source
	 */
	public static DatabaseQuoteSource createDatabaseQuoteSource() {
		
		return new DatabaseQuoteSource(DatabaseHelper.getDatabaseManager());
	}
}
