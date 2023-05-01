/* Merchant of Venice - technical analysis software for the stock market.
   Copyright (C) 2002 Andrew Leppard (aleppard@picknowl.com.au)
   This portion of code Copyright (C) 2004 Dan Makovec (venice@makovec.net)

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

package nz.org.venice.prefs;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import nz.org.venice.macro.StoredMacro;
import nz.org.venice.main.Main;
import nz.org.venice.main.ModuleFrame;
import nz.org.venice.portfolio.Portfolio;
import nz.org.venice.portfolio.PortfolioParserException;
import nz.org.venice.portfolio.PortfolioReader;
import nz.org.venice.portfolio.PortfolioWriter;
import nz.org.venice.prefs.settings.ModuleFrameSettingsWriter;
import nz.org.venice.prefs.settings.ModuleSettingsParserException;
import nz.org.venice.quote.Symbol;
import nz.org.venice.quote.SymbolMetadata;
import nz.org.venice.table.WatchScreen;
import nz.org.venice.table.WatchScreenParserException;
import nz.org.venice.table.WatchScreenReader;
import nz.org.venice.table.WatchScreenWriter;
import nz.org.venice.util.DatabaseManager;
import nz.org.venice.util.Locale;
import nz.org.venice.util.DatabaseHelper;
import nz.org.venice.util.TradingTime;
import nz.org.venice.util.TradingTimeFormatException;

/**
 * The Preferences Manager contains a set of routines for loading and saving all
 * preferences data for the application. Consolidating these routines in a
 * single place allows us to maintain preferences namespace convention and also
 * allows us to easily change the method of storage at a later date if desired.
 * <p>
 * If a save method first clears all preferences data from a node, it is
 * imperative that both the save and the equivalent load methods are
 * <code>synchronized</code>. Otherwise there is the possibility the load call
 * is called just after the delete call which would nuke all the values. Perhaps
 * all the methods should be synchronized.
 *
 * @author Daniel Makovec
 */
public class PreferencesManager {
	// The base in the prefs tree where all Venice settings are stored
	private final static String base = "nz.org.venice";

	// The user root from Venice's point of view
	private static Preferences userRoot = Preferences.userRoot().node(base);

	// This class cannot be instantiated
	private PreferencesManager() {
		// nothing to do
	}

	/** Indicates the quote source is using the inbuilt sample quotes. */
	public static int SAMPLES = 0;

	/** @deprecated Indicates the quote source is accessing quotes in files. */
	public static int FILES = 1;

	/** Indicates the quote source is accessing quotes in a database. */
	public static int DATABASE = 2;

	/** Indicates the quote source is accessing the internal database. */
	public static int INTERNAL = 3;

	/** Web proxy preferences fields. */
	public class ProxyPreferences {

		/** Web proxy host address. */
		public String host;

		/** Web proxy port. */
		public String port;

		/** Whether we are using the web proxy. */
		public boolean isEnabled;

		/** Is authentication enabled? */
		public boolean authEnabled;

		/** Authentication user name. */
		public String user;

		/** Authentication password. */
		public String password;
	}

	/** Database preferences fields. */
	public class DatabasePreferences {

		/** Database software (e.g. "mysql"). */
		public String software;

		/** Database driver class. */
		public String driver;

		/** Database host. */
		public String host;

		/** Database port. */
		public String port;

		/** Database name (e.g. "shares") */
		public String database;

		/** Database user name. */
		public String username;

		/** Database password. */
		public String password;

		/** Prompt user to enter password instead of saving it to disk. */
		public boolean passwordPrompt;
	}

	/** Display preferences fields. */
	public class DisplayPreferences {
		/** X location of main window. */
		public int x;

		/** Y location of main window. */
		public int y;

		/** Width of main window. */
		public int width;

		/** Height of main window. */
		public int height;
	}

	/** Intra-day Quote Sync preferences fields. */
	public class IDQuoteSyncPreferences {

		/** Is syncing enabled? */
		public boolean isEnabled;

		/** Symbols to automatically sync. */
		public String symbols;

		/** Optional suffix to append (e.g. ".AX") */
		public String suffix;

		/** Time exchange opens. */
		public TradingTime openTime;

		/** Time exchange closes. */
		public TradingTime closeTime;

		/** Polling period in seconds. */
		public int period;
	}

	/**
	 * Forces the preferences data to be saved to the backend store (e.g. disk).
	 */
	public static void flush() {
		try {
			userRoot.flush();
		} catch (BackingStoreException e) {
			// ignore
		}
	}

	/**
	 * Fetches the desired user node, based at the <code>base</code> branch.
	 * 
	 * @param node the path to the node to be fetched.
	 */
	public static Preferences getUserNode(String node) {
		if (node.charAt(0) == '/')
			node = node.substring(1);
		return userRoot.node(node);
	}

	/**
	 * Return whether we require the user to explicitly accept the GPL license.
	 * Currently the license must be explicilty accepted by the user for each
	 * version.
	 *
	 * @return <code>true</code> if the user needs to explicitly accept the GPL.
	 */
	public static boolean getHasGPLAcceptance() {
		Preferences node = getUserNode("/license");
		String acceptedVersion = node.get("accepted_version", "not_accepted");
		return !acceptedVersion.equals(Main.SHORT_VERSION);
	}

	/**
	 * Set that the user has been shown the GPL and has accepted it. The user
	 * will not be bothered again until the next version.
	 */
	public static void putHasGPLAcceptance() {
		Preferences node = getUserNode("/license");
		node.put("accepted_version", Main.SHORT_VERSION);
	}

	/**
	 * Load the last directory used when importing quote files.
	 *
	 * @param dirtype the directory type (e.g. macros, importer, etc).
	 * @return the directory.
	 */
	public static String getDirectoryLocation(String dirtype) {
		Preferences prefs = getUserNode("/" + dirtype);
		String directory = prefs.get("directory", "");

		if (directory.length() != 0)
			return directory;
		else
			return null;
	}

	/**
	 * Save the directory used to import quote files.
	 *
	 * @param dirtype   the directory type (e.g. macros, importer, etc)
	 * @param directory the directory.
	 */
	public static void putDirectoryLocation(String dirtype, String directory) {
		Preferences prefs = getUserNode("/" + dirtype);
		prefs.put("directory", directory);
	}

	/**
	 * Load the list of all stored expressions.
	 *
	 * @return the list of stored expressions.
	 * @see StoredExpression
	 */
	public static synchronized List getStoredExpressions() {
		List storedExpressions = new ArrayList();
		Preferences prefs = getUserNode("/equations");

		try {
			String[] keys = prefs.keys();
			for (int i = 0; i < keys.length; i++)
				storedExpressions.add(new StoredExpression(keys[i], 
						prefs.get(keys[i], "")));
		} catch (BackingStoreException e) {
			// ignore
		}

		return storedExpressions;
	}

	/**
	 * Save the list of all stored expressions.
	 *
	 * @param storedExpressions the stored expressions.
	 * @see StoredExpression
	 */
	public static synchronized void putStoredExpressions(List storedExpressions) {
		try {
			// Remove old expressions
			Preferences prefs = getUserNode("/equations");
			prefs.removeNode();
			prefs = getUserNode("/equations");

			for (Iterator iterator = storedExpressions.iterator(); iterator.hasNext();) {
				StoredExpression storedExpression = (StoredExpression) iterator.next();
				prefs.put(storedExpression.name, storedExpression.expression);
			}
		} catch (BackingStoreException e) {
			// ignore
		}
	}

	/**
	 * Load the list of all registered macros.
	 *
	 * @return the list of registered macros.
	 * @see StoredMacro
	 */
	public static synchronized List getStoredMacros() {
		List stored_macros = new ArrayList();

		String dirname = PreferencesManager.getMacroHome().getAbsolutePath();
		if (dirname == null)
			return stored_macros;
		File directory = new File(dirname);
		if (!directory.isDirectory())
			return stored_macros;

		String[] list = directory.list(new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return (dir.getAbsolutePath().equals(dirname) && filename.indexOf(".py") == filename.length() - 3);
			}
		});

		for (int i = 0; i < list.length; i++) {
			String name = list[i].substring(0, list[i].length() - 3);
			Preferences macro_node = getUserNode("/macros_info/" + list[i]);
			stored_macros.add(
					new StoredMacro(macro_node.get("name", name), list[i], macro_node.getBoolean("on_startup", false),
							macro_node.getInt("start_sequence", 0), macro_node.getBoolean("in_menu", false)));
		}
		return stored_macros;
	}

	/**
	 * Save the list of all registered macros.
	 *
	 * @param stored_macros the registered macros.
	 * @see StoredMacro
	 */
	public static synchronized void putStoredMacros(List stored_macros) {
		try {
			// Remove old macro definitions
			Preferences prefs = getUserNode("/macros_info");
			prefs.removeNode();
			prefs = getUserNode("/macros_info");

			for (Iterator iterator = stored_macros.iterator(); iterator.hasNext();) {
				StoredMacro stored_macro = (StoredMacro) iterator.next();
				Preferences macro_node = getUserNode("/macros_info/" + stored_macro.getFilename());
				macro_node.put("name", stored_macro.getName());
				macro_node.putBoolean("on_startup", stored_macro.isOn_startup());
				macro_node.putInt("start_sequence", stored_macro.getStart_sequence());
				macro_node.putBoolean("in_menu", stored_macro.isIn_menu());
			}
		} catch (BackingStoreException e) {
			// ignore
		}
	}
	
	/**
	 * Get the list of Symbols Metadata saved in the database
	 *
	 * @return List of SymbolMetadata
	 * @see nz.org.venice.quotes.SymbolMetadata 
	 */
	public static List<SymbolMetadata> getSymbolsMetadata() 
			throws PreferencesException {

		List<SymbolMetadata> symbolsMetadata = new ArrayList<>();

		DatabaseManager dbm = DatabaseHelper.getDatabaseManager();

		if (!dbm.getConnection()) {
			return new ArrayList<>();
		}
		
		final String queryLabel = "selectAllMetadata";
		List<String> queries = dbm.getQueries(queryLabel);
		
		try {
			List results = dbm.executeQueryTransaction(queryLabel, queries);
			
			Iterator<ResultSet> iterator = results.iterator();
			while (iterator.hasNext()) {
				ResultSet RS = (ResultSet) iterator.next();
				List <SymbolMetadata> sm = DatabaseHelper.convertSQLResultSetToObject(RS, SymbolMetadata.class);
				symbolsMetadata.addAll(sm);
			}
		} catch (Exception e) {
			throw new PreferencesException(e.getMessage());
		}
		return symbolsMetadata;
	}

	/**
	 * Insert or update (if already exist) a Metadata for a symbol
	 *
	 * @param symbolMetadata Metadata for a given symbol
	 * @see nz.org.venice.quotes.SymbolMetadata 
	 */
	public static void putSymbolMetadata(SymbolMetadata symbolMetadata) 
			throws PreferencesException {

		DatabaseManager dbm = DatabaseHelper.getDatabaseManager();
		
		if (!dbm.getConnection()) {
			return;
		}
		
		final String queryLabel = "insertUpdateShareMetadata";
		List<String> queries = dbm.getQueries(queryLabel);
		ArrayList<String> newQueryList = new ArrayList<String>();
		
		Iterator iterator = queries.iterator();
		while (iterator.hasNext()) {
			String query = (String) iterator.next();
			query = dbm.replaceParameter(query, "symbol", symbolMetadata.getSymbol().toString());
			query = dbm.replaceParameter(query, "prefix", symbolMetadata.getPrefix());
			query = dbm.replaceParameter(query, "posfix", symbolMetadata.getPosfix());
			query = dbm.replaceParameter(query, "type", symbolMetadata.getType().name());
			query = dbm.replaceParameter(query, "name", symbolMetadata.getName());
			query = dbm.replaceParameter(query, "sync_intra_day", symbolMetadata.syncIntraDay() ? "1" : "0" );
			
			newQueryList.add(query);
		}
		
		try {
			dbm.executeUpdateTransaction(queryLabel, newQueryList);
		} catch (Exception e) {
			throw new PreferencesException(e.getMessage());
		} finally {
			try {
				dbm.queryCleanup(queryLabel);
			} catch (SQLException e) {

			}
		} 
	}
	
	/**
	 * Delete a Metadata for a symbol
	 *
	 * @param symbolMetadata Metadata for a given symbol
	 * @see nz.org.venice.quotes.SymbolMetadata 
	 */
	public static void deleteSymbolMetada(SymbolMetadata symbolMetadata) 
			throws PreferencesException {

		DatabaseManager dbm = DatabaseHelper.getDatabaseManager();
		
		if (!dbm.getConnection()) {
			return;
		}
		
		final String queryLabel = "deleteShareMetada";
		List<String> queries = dbm.getQueries(queryLabel);
		ArrayList<String> newQueryList = new ArrayList<String>();
		
		Iterator iterator = queries.iterator();
		while (iterator.hasNext()) {
			String query = (String) iterator.next();
			query = dbm.replaceParameter(query, "symbol", symbolMetadata.getSymbol().toString());
			newQueryList.add(query);
		}
		
		try {
			dbm.executeUpdateTransaction(queryLabel, newQueryList);
		} catch (Exception e) {
			throw new PreferencesException(e.getMessage());
		} finally {
			try {
				dbm.queryCleanup(queryLabel);
			} catch (SQLException e) {

			}
		} 
	}

	/**
	 * Check if a given symbol is Market Index or not
	 *
	 * @param symbol The symbol to be checked
	 * @return true if is market index, false otherwise
	 * @see nz.org.venice.quotes.SymbolMetadata 
	 */
	public static boolean isMarketIndex(Symbol symbol) {
		
		try {
			List<SymbolMetadata> symbolMetadata = getSymbolsMetadata();
			Iterator<SymbolMetadata> iterator = symbolMetadata.iterator();
			while (iterator.hasNext()) {
				SymbolMetadata data = (SymbolMetadata) iterator.next();
				if (data.getSymbol().equals(symbol) && data.isIndex()) {
					return true;
				}
			}
			return false;
		} catch (PreferencesException e) {
			return false;
		}
	}

	// Store the users text made for this symbol
	public static void putUserNotes(String symbol, String text) {
		String xpath = "/userNotes/" + symbol;
		Preferences prefs = getUserNode("/userNotes");

		prefs = getUserNode("/table/userNotes/" + symbol);
		prefs.put(symbol, text);
	}

	// Retrieve the notes made for this symbol.
	public static String getUserNotes(String symbol) {
		String text, xpath;
		Preferences prefs;

		text = "";
		xpath = "/userNotes" + symbol;

		// Pre 0.724b version saved userNotes in /userNotes
		// Check in old location if no notes are found in the new location.
		prefs = getUserNode("/table/userNotes/" + symbol);
		if (prefs != null) {
			text = prefs.get(symbol, "");
			if (text.equals("")) {
				prefs = getUserNode("/userNotes");
				return prefs.get(symbol, text);
			}
		}

		return text;

	}

	/**
	 * Load all saved user input in an Analyser Page.
	 *
	 * @param key a key which identifies which page settings to load.
	 * @return mapping of settings.
	 * @see nz.org.venice.analyser.IAnalyserPage
	 */
	public static HashMap getAnalyserPageSettings(String key) {

		HashMap settings = new HashMap();
		Preferences p = getUserNode("/analyser/" + key);
		String[] settingList = null;

		// Get all the settings that we've saved
		try {
			settingList = p.keys();
		} catch (BackingStoreException e) {
			// ignore
		}

		// Now populate settings into a hash
		for (int i = 0; i < settingList.length; i++) {
			String value = p.get(settingList[i], "");
			settings.put((Object) settingList[i], (Object) value);
		}

		return settings;
	}

	/**
	 * Save all user input in an Analyser Page.
	 *
	 * @param key      a key which identifies which page settings to save.
	 * @param settings the settings to save.
	 * @see nz.org.venice.analyser.IAnalyserPage
	 */
	public static void putAnalyserPageSettings(String key, HashMap settings) {
		Preferences p = getUserNode("/analyser/" + key);
		Iterator iterator = settings.keySet().iterator();

		while (iterator.hasNext()) {
			String setting = (String) iterator.next();
			String value = (String) settings.get((Object) setting);

			p.put(setting, value);
		}
	}

	/**
	 * Load the last preferences page visited.
	 *
	 * @return index of the last preferences page visited.
	 */
	public static int getLastPreferencesPage() {
		Preferences prefs = getUserNode("/prefs");
		return prefs.getInt("page", 0);
	}

	/**
	 * Save last preferences page visited.
	 *
	 * @param page index of the last preferences page visited.
	 */
	public static void putLastPreferencesPage(int page) {
		Preferences prefs = getUserNode("/prefs");
		prefs.putInt("page", page);
	}

	/**
	 * Load the cache's maximum number of quotes.
	 *
	 * @return the maximum number of quotes.
	 */
	public static int getMaximumCachedQuotes() {
		Preferences prefs = getUserNode("/cache");
		return prefs.getInt("maximumQuotes", 100000);
	}

	/**
	 * Save the cache's maximum number of quotes.
	 *
	 * @param maximumCachedQuotes the maximum number of quotes.
	 */
	public static void putMaximumCachedQuotes(int maximumCachedQuotes) {
		Preferences prefs = getUserNode("/cache");
		prefs.putInt("maximumQuotes", maximumCachedQuotes);
	}

	/**
	 * Load the users preference for whether the quotes in the cache expire.
	 * 
	 * @return True if cacheExpiry is enabled.
	 */
	public static boolean getCacheExpiryEnabled() {
		Preferences prefs = getUserNode("/cache");
		return prefs.getBoolean("expiryEnabled", false);
	}

	/**
	 * Save the users preferences for whether the quotes in the cache expire.
	 *
	 * @param expiry If true, the quotes in the cache will expire.
	 */
	public static void putCacheExpiryEnabled(boolean expiry) {
		Preferences prefs = getUserNode("/cache");
		prefs.putBoolean("expiryEnabled", expiry);
	}

	/**
	 * Save the users preference for when the quotes in the cache expire.
	 * 
	 * @param lifespan the number of minutes that must elapse before refreshing the
	 *                 cache.
	 */
	public static void putCacheExpiryTime(int lifespan) {
		Preferences prefs = getUserNode("/cache");
		prefs.putInt("expiryTime", lifespan);
	}

	/**
	 * Load the users preference for when the quotes in the cache expire.
	 * 
	 * @return The number of minutes that must elapse before refreshing the cache.
	 */
	public static int getCacheExpiryTime() {
		int defaultLifetime = 60 * 8; // 8 hours
		Preferences prefs = getUserNode("/cache");
		return prefs.getInt("expiryTime", defaultLifetime);
	}

	/**
	 * Return a list of the names of all the watch screens.
	 *
	 * @return the list of watch screen names.
	 */
	public static List getWatchScreenNames() {
		List watchScreenNames = new ArrayList();

		// First retrieve all the watch screens stored in ~/Venice/WatchScreen/ (0.8 and
		// up)
		// Watch screens are now stored as files, as opposed to Java prefences, because
		// this
		// improves read and write times (especially on Mac OS X) and makes watch screen
		// management easier for the user.
		String[] watchScreenFileNames = getWatchScreenHome().list();
		String suffix = ".xml";

		for (int i = 0; i < watchScreenFileNames.length; i++) {
			String watchScreenFileName = watchScreenFileNames[i];

			// Ignore files without trailing suffix
			if (watchScreenFileName.endsWith(suffix)) {
				// Remove trailing suffix
				String watchScreenName = watchScreenFileName.substring(0,
						watchScreenFileName.length() - suffix.length());
				watchScreenNames.add(watchScreenName);
			}
		}

		// Make sure list is in alphabetical order
		Collections.sort(watchScreenNames);

		return watchScreenNames;
	}

	/**
	 * Read the watch screen contained the given file. Venice stores watch screens
	 * in files from 0.8 and up.
	 *
	 * @param watchScreenFile the file containing the watch screen.
	 * @return the watch screen contained in the file.
	 * @exception PreferencesException if there was an error loading the watch
	 *                                 screen.
	 */
	private static WatchScreen getWatchScreenFromFile(File watchScreenFile) throws PreferencesException {
		try {
			FileInputStream inputStream = new FileInputStream(watchScreenFile);
			WatchScreen watchScreen = WatchScreenReader.read(inputStream);
			inputStream.close();
			return watchScreen;
		} catch (IOException e) {
			throw new PreferencesException(e.getMessage());
		} catch (WatchScreenParserException e) {
			throw new PreferencesException(e.getMessage());
		} catch (SecurityException e) {
			throw new PreferencesException(e.getMessage());
		}
	}

	/**
	 * Load the watch screen with the given name.
	 *
	 * @param watchScreenName the name of the watch screen to load.
	 * @return the watch screen.
	 * @exception PreferencesException if there was an error loading the watch
	 *                                 screen.
	 */
	public static synchronized WatchScreen getWatchScreen(String watchScreenName) throws PreferencesException {
		File watchScreenFile = new File(getWatchScreenHome(), watchScreenName.concat(".xml"));

		// Load the watchScreen from ~/Venice/WatchScreen/ (0.8 and up)
		if (watchScreenFile.exists())
			return getWatchScreenFromFile(watchScreenFile);

		else
			return null;
	}

	/**
	 * Save the watch screen.
	 *
	 * @param watchScreen the watch screen.
	 * @exception PreferencesException if there was an error saving the watch
	 *                                 screen.
	 */
	public static synchronized void putWatchScreen(WatchScreen watchScreen) throws PreferencesException {

		try {
			File watchScreenFile = new File(getWatchScreenHome(), watchScreen.getName() + ".xml");
			FileOutputStream outputStream = new FileOutputStream(watchScreenFile);
			WatchScreenWriter.write(watchScreen, outputStream);
			outputStream.close();
		} catch (IOException e) {
			throw new PreferencesException(e.getMessage());
		} catch (SecurityException e) {
			throw new PreferencesException(e.getMessage());
		}
	
	}

	/**
	 * Delete the watch screen.
	 *
	 * @param name the watch screen name.
	 */
	public static synchronized void deleteWatchScreen(String name) {
		// Delete the watch screen from ~/Venice/WatchScreen/ (0.8 and up)
		File watchScreenFile = new File(getWatchScreenHome(), name.concat(".xml"));
		watchScreenFile.delete();
	}

	/**
	 * Return a list of the names of all the portfolios.
	 *
	 * @return the list of portfolio names.
	 */
	public static synchronized List getPortfolioNames() {
		List portfolioNames = new ArrayList();

		// First retrieve all the portfolios stored in ~/Venice/Portfolio/ (0.7b and up)
		// Portfolios are now stored as files, as opposed to Java prefences, because
		// this
		// improves read and write times (especially on Mac OS X) and makes portfolio
		// management easier for the user.
		String[] portfolioFileNames = getPortfolioHome().list();
		String suffix = ".xml";

		for (int i = 0; i < portfolioFileNames.length; i++) {
			String portfolioFileName = portfolioFileNames[i];

			// Ignore files without trailing suffix
			if (portfolioFileName.endsWith(suffix)) {
				// Remove trailing suffix
				String portfolioName = portfolioFileName.substring(0, portfolioFileName.length() - suffix.length());
				portfolioNames.add(portfolioName);
			}
		}

		// Make sure list is in alphabetical order
		Collections.sort(portfolioNames);

		return portfolioNames;
	}

	/**
	 * Delete the portfolio.
	 *
	 * @param name the portfolio name.
	 */
	public static synchronized void deletePortfolio(String name) {
		// Delete the portfolio from ~/Venice/Portfolio/ (0.7b and up)
		File portfolioFile = new File(getPortfolioHome(), name.concat(".xml"));
		portfolioFile.delete();
	
	}

	/**
	 * Read the portfolio contained the given file. Venice stores portfolios in
	 * files from 0.7b and up.
	 *
	 * @param portfolioFile the file containing the portfolio.
	 * @return the Portfolio contained in the file.
	 * @exception PreferencesException if there was an error loading the portfolio.
	 */
	private static Portfolio getPortfolioFromFile(File portfolioFile) throws PreferencesException {
		try {
			FileInputStream inputStream = new FileInputStream(portfolioFile);
			Portfolio portfolio = PortfolioReader.read(inputStream);
			inputStream.close();
			return portfolio;
		} catch (IOException e) {
			throw new PreferencesException(e.getMessage());
		} catch (PortfolioParserException e) {
			throw new PreferencesException(e.getMessage());
		} catch (SecurityException e) {
			throw new PreferencesException(e.getMessage());
		}
	}

	/**
	 * Load the portfolio with the given name.
	 *
	 * @param portfolioName the name of the portfolio to load.
	 * @return the portfolio.
	 * @exception PreferencesException if there was an error loading the portfolio.
	 */
	public static synchronized Portfolio getPortfolio(String portfolioName) throws PreferencesException {
		File portfolioFile = new File(getPortfolioHome(), portfolioName.concat(".xml"));

		// Load the portfolio from ~/Venice/Portfolio/ (0.7b and up)
		if (portfolioFile.exists())
			return getPortfolioFromFile(portfolioFile);

		else
			return null;
	}

	/**
	 * Return the directory which contains Venice's HSQLDB database.
	 *
	 * @return Database directory.
	 */
	private static File getDatabaseHome() {
		File veniceHome = getVeniceHome();
		File databaseHome = new File(veniceHome, "Database");
		if (!databaseHome.exists())
			databaseHome.mkdir();
		return databaseHome;
	}

	/**
	 * Return the directory which contains Venice's macros.
	 *
	 * @return Macro directory.
	 */
	public static File getMacroHome() {
		File veniceHome = getVeniceHome();
		File macroHome = new File(veniceHome, "Macro");
		if (!macroHome.exists())
			macroHome.mkdir();
		return macroHome;
	}

	/**
	 * Return the directory which contains Venice's portfolios.
	 *
	 * @return Portfolio directory.
	 */
	private static File getPortfolioHome() {
		File veniceHome = getVeniceHome();
		File portfolioHome = new File(veniceHome, "Portfolio");
		if (!portfolioHome.exists())
			portfolioHome.mkdir();
		return portfolioHome;
	}

	/**
	 * Return the directory which contains Venice's watch screens.
	 *
	 * @return Watch screen directory.
	 */
	private static File getWatchScreenHome() {
		File veniceHome = getVeniceHome();
		File watchScreenHome = new File(veniceHome, "WatchScreen");
		if (!watchScreenHome.exists())
			watchScreenHome.mkdir();
		return watchScreenHome;
	}

	/**
	 * Return Venice's home directory. Venice uses this directory to store important
	 * files such as portfolios. If this directory does not exist it will be
	 * created.
	 *
	 * @return Home directory.
	 */
	private static File getVeniceHome() {
		File veniceHome = new File(System.getProperty("user.home"), "Venice");
		if (!veniceHome.exists())
			veniceHome.mkdir();
		return veniceHome;
	}

	/**
	 * Save the portfolio.
	 *
	 * @param portfolio the portfolio.
	 * @exception PreferencesException if there was an error saving the portfolio.
	 */
	public static synchronized void putPortfolio(Portfolio portfolio) throws PreferencesException {
		try {
			File portfolioFile = new File(getPortfolioHome(), portfolio.getName() + ".xml");
			FileOutputStream outputStream = new FileOutputStream(portfolioFile);
			PortfolioWriter.write(portfolio, outputStream);
			outputStream.close();
		} catch (IOException e) {
			throw new PreferencesException(e.getMessage());
		} catch (SecurityException e) {
			throw new PreferencesException(e.getMessage());
		}

		// Clear old portfolio from preferences if present (up to 0.6b).
		try {
			Preferences p = getUserNode("/portfolio/" + portfolio.getName());
			p.removeNode();
		} catch (BackingStoreException e) {
			throw new PreferencesException(e.getMessage());
		}
	}

	/**
	 * Load proxy settings.
	 *
	 * @return proxy preferences.
	 */
	public static ProxyPreferences getProxySettings() {
		Preferences prefs = getUserNode("/proxy");
		PreferencesManager preferencesManager = new PreferencesManager();
		ProxyPreferences proxyPreferences = preferencesManager.new ProxyPreferences();
		proxyPreferences.host = prefs.get("host", "proxy");
		proxyPreferences.port = prefs.get("port", "8080");
		proxyPreferences.isEnabled = prefs.getBoolean("enabled", false);

		proxyPreferences.user = prefs.get("user", "");
		proxyPreferences.password = prefs.get("password", "");
		proxyPreferences.authEnabled = prefs.getBoolean("authEnabled", false);

		return proxyPreferences;
	}

	/**
	 * Save proxy settings.
	 *
	 * @param proxyPreferences the new proxy preferences.
	 */
	public static void putProxySettings(ProxyPreferences proxyPreferences) {
		Preferences prefs = getUserNode("/proxy");
		prefs.put("host", proxyPreferences.host);
		prefs.put("port", proxyPreferences.port);
		prefs.putBoolean("enabled", proxyPreferences.isEnabled);

		prefs.put("user", proxyPreferences.user);
		prefs.put("password", proxyPreferences.password);
		prefs.putBoolean("authEnabled", proxyPreferences.authEnabled);
	}

	/**
	 * Load language setting. Returns <code>null</code> if there is no language
	 * setting saved in preferences.
	 *
	 * @return ISO Language Code
	 */
	public static String getLanguageCode() {
		Preferences prefs = getUserNode("/language");
		return prefs.get("locale", null);
	}

	/**
	 * Save language setting.
	 *
	 * @param languageCode ISO Language Code.
	 */
	public static void putLanguageCode(String languageCode) {
		Preferences prefs = getUserNode("/language");
		prefs.put("locale", languageCode);
	}

	/**
	 * Load user interface setting.
	 *
	 * @return the minimum decimal digits to be displayed.
	 */
	public static int getMinDecimalDigits() {
		// 3 is the default, if anything goes wrong
		int retValue = 3;
		Preferences prefs = getUserNode("/min_user_interface");
		String str = prefs.get("min_decimal_digits", "3");
		try {
			retValue = Integer.parseInt(str);
		} catch (Exception ex) {
			retValue = 3;
		}
		return retValue;
	}

	/**
	 * Save user interface setting.
	 *
	 * @param minDecimalDigits the minimum decimal digits to be displayed.
	 */
	public static void putMinDecimalDigits(String minDecimalDigits) {
		Preferences prefs = getUserNode("/min_user_interface");
		prefs.put("min_decimal_digits", minDecimalDigits);
	}

	/**
	 * Load user interface setting.
	 *
	 * @return the maximum decimal digits to be displayed.
	 */
	public static int getMaxDecimalDigits() {
		// 3 is the default, if anything goes wrong
		int retValue = 3;
		Preferences prefs = getUserNode("/max_user_interface");
		String str = prefs.get("max_decimal_digits", "3");
		try {
			retValue = Integer.parseInt(str);
		} catch (Exception ex) {
			retValue = 3;
		}
		return retValue;
	}

	/**
	 * Save user interface setting.
	 *
	 * @param maxDecimalDigits the maximum decimal digits to be displayed.
	 */
	public static void putMaxDecimalDigits(String maxDecimalDigits) {
		Preferences prefs = getUserNode("/max_user_interface");
		prefs.put("max_decimal_digits", maxDecimalDigits);
	}

	/**
	 * Load user interface setting.
	 *
	 * @return the tab size to enter in a text dialog.
	 */
	public static int getEditTabSize() {
		// 8 is the default, if anything goes wrong
		int defaultValue = 8;
		int retValue = defaultValue;
		Preferences prefs = getUserNode("/max_user_interface");
		String str = prefs.get("edit_tab_size", "8");
		try {
			retValue = Integer.parseInt(str);
		} catch (Exception ex) {
			retValue = defaultValue;
		}
		return retValue;
	}

	/**
	 * Save user interface setting.
	 *
	 * @param tabSize the number of characters to insert when the tab key is
	 *                pressed.
	 */
	public static void putEditTabSize(String tabSize) {
		Preferences prefs = getUserNode("/max_user_interface");
		prefs.put("edit_tab_size", tabSize);
	}

	/**
	 * Load default chart setting.
	 *
	 * @return the default chart to be displayed.
	 */
	public static String getDefaultChart() {
		Preferences prefs = getUserNode("/default_chart_defaults");
		String str = prefs.get("default_chart", "Line Graph");
		return str;
	}

	/**
	 * Load default chart setting.
	 *
	 * @return the default chart background colour.
	 */
	public static Color getDefaultChartBackgroundColour() {

		Preferences prefs = getUserNode("/default_chart_defaults/default_chart_background_colour");
		int red = prefs.getInt("red", 0);
		int green = prefs.getInt("green", 0);
		int blue = prefs.getInt("blue", 0);
		int alpha = prefs.getInt("alpha", 0);

		Color bgColour = new Color(red, green, blue, alpha);
		return bgColour;
	}

	/**
	 * Load default chart scroll bar position.
	 *
	 * @return whether to move the scroll bar to the end of the pane when creating a
	 *         new chart.
	 */
	public static boolean getDefaultChartScrollToEnd() {
		Preferences prefs = getUserNode("/default_chart_defaults");
		String str = prefs.get("scroll_to_end", "Line Graph");
		return new Boolean(str).booleanValue();
	}

	/**
	 * Load default table scroll bar position.
	 *
	 * @return whether to move the scroll bar to the end of the pane when creating a
	 *         new table.
	 */
	public static boolean getDefaultTableScrollToEnd() {
		Preferences prefs = getUserNode("/default_table_defaults");
		String str = prefs.get("scroll_to_end", "false");
		return new Boolean(str).booleanValue();
	}

	/**
	 * Save wether to restore windows on restart.
	 *
	 * @param state a boolean flag which when true causes Venice to reconstruct
	 *              previously open windows.
	 **/

	public static void putRestoreSavedWindowsSetting(boolean state) {
		Preferences prefs = getUserNode("/restore_windows_user_interface");
		prefs.put("state", String.valueOf(state));
	}

	/**
	 * Return wether to restore windows on restart
	 *
	 * @return wether Venice should to reconstruct previously open windows.
	 **/

	public static boolean getRestoreSavedWindowsSetting() {
		Preferences prefs = getUserNode("/restore_windows_user_interface");
		String state = prefs.get("state", "false");

		return state.equals("true") ? true : false;
	}

	/**
	 * Save whether Venice should confirm exit.
	 *
	 * @param state Flag when true causes venice to prompt the user to confirm their
	 *              exit.
	 *
	 **/
	public static void putConfirmExitSetting(boolean state) {
		Preferences prefs = getUserNode("/confirm_exit_to_venice");
		prefs.put("state", String.valueOf(state));
	}

	/**
	 * Return true if Venice should confirm exit.
	 *
	 * @return true if Venice should prompt the user to confirm their exit.
	 *
	 **/

	public static boolean getConfirmExitSetting() {
		Preferences prefs = getUserNode("/confirm_exit_to_venice");
		String state = prefs.get("state", "false");

		return state.equals("true") ? true : false;
	}

	/**
	 * Save default chart setting.
	 *
	 * @param defaultChart the chart to be displayed.
	 */
	public static void putDefaultChart(String defaultChart) {

		Preferences prefs = getUserNode("/default_chart_defaults");
		prefs.put("default_chart", defaultChart);
	}

	/**
	 * Save default chart background colour.
	 *
	 * @param backColour the default background colour.
	 */
	public static void putDefaultChartBackgroundColour(Color backColour) {

		Preferences prefs = getUserNode("/default_chart_defaults/default_chart_background_colour");

		int red = backColour.getRed();
		int green = backColour.getGreen();
		int blue = backColour.getBlue();
		int alpha = backColour.getAlpha();

		prefs.putInt("red", red);
		prefs.putInt("green", green);
		prefs.putInt("blue", blue);
		prefs.putInt("alpha", alpha);
	}

	/**
	 * Save default chart scroll bar setting.
	 *
	 * @param isSelected if the scroll bar on a chart should be moved to the end.
	 */
	public static void putDefaultChartScrollToEnd(boolean isSelected) {

		Preferences prefs = getUserNode("/default_chart_defaults");
		prefs.put("scroll_to_end", String.valueOf(isSelected));
	}

	/**
	 * Save default table scroll bar setting.
	 *
	 * @param isSelected if the scroll bar on a table should be moved to the end.
	 */
	public static void putDefaultTableScrollToEnd(boolean isSelected) {

		Preferences prefs = getUserNode("/default_table_defaults");
		prefs.put("scroll_to_end", String.valueOf(isSelected));
	}

	/**
	 * Get quote source setting.
	 *
	 * @return quote source, one of {@link #DATABASE}, {@link #FILES} or
	 *         {@link #SAMPLES}.
	 */
	public static int getQuoteSource() {
		Preferences prefs = getUserNode("/quote_source");
		String quoteSource = prefs.get("source", "samples");

		if (quoteSource.equals("samples"))
			return SAMPLES;
		else if (quoteSource.equals("files"))
			// File quote source is deprecated. Switch to internal quote source.
			return INTERNAL;
		else if (quoteSource.equals("database"))
			return DATABASE;
		else
			return INTERNAL;
	}

	/**
	 * Set quote source setting.
	 *
	 * @param quoteSource the quote source, one of {@link #DATABASE},
	 *                    {@link #INTERNAL} or {@link #SAMPLES}.
	 */
	public static void putQuoteSource(int quoteSource) {
		assert (quoteSource == DATABASE || quoteSource == SAMPLES || quoteSource == INTERNAL);

		Preferences prefs = getUserNode("/quote_source");
		String source;

		if (quoteSource == SAMPLES)
			source = "samples";
		else if (quoteSource == DATABASE)
			source = "database";
		else
			source = "internal";

		prefs.put("source", source);
	}

	/**
	 * Load database settings.
	 *
	 * @return database preferences.
	 */
	public static DatabasePreferences getDatabaseSettings() {
		Preferences prefs = getUserNode("/quote_source/database");
		PreferencesManager preferencesManager = new PreferencesManager();
		DatabasePreferences databasePreferences = preferencesManager.new DatabasePreferences();
		databasePreferences.software = prefs.get("software", "mysql");
		databasePreferences.driver = prefs.get("driver", "com.mysql.jdbc.Driver");
		databasePreferences.host = prefs.get("host", "db");
		databasePreferences.port = prefs.get("port", "3306");
		databasePreferences.database = prefs.get("dbname", "shares");
		databasePreferences.username = prefs.get("username", "");
		databasePreferences.password = prefs.get("password", "3306");
		databasePreferences.passwordPrompt = prefs.getBoolean("passwordPrompt", false);
		return databasePreferences;
	}

	/**
	 * Save database settings.
	 *
	 * @param databasePreferences the new database preferences.
	 */
	public static void putDatabaseSettings(DatabasePreferences databasePreferences) {
		Preferences prefs = getUserNode("/quote_source/database");
		prefs.put("software", databasePreferences.software);
		prefs.put("driver", databasePreferences.driver);
		prefs.put("host", databasePreferences.host);
		prefs.put("port", databasePreferences.port);
		prefs.put("dbname", databasePreferences.database);
		prefs.put("username", databasePreferences.username);
		prefs.put("password", databasePreferences.password);
		prefs.putBoolean("passwordPrompt", databasePreferences.passwordPrompt);
	}

	/**
	 * Load the file name to store the internal database.
	 *
	 * @return internal database file name
	 */
	public static String getInternalFileName() {
		File databaseFile = new File(getDatabaseHome(), "Database");
		String databaseFileName = "Database";

		try {
			databaseFileName = databaseFile.getCanonicalPath();
		} catch (IOException e) {
			// don't care
		} catch (SecurityException e) {
			// don't care
		}

		return databaseFileName;
	}

	/**
	 * Load display settings.
	 *
	 * @return display preferences.
	 */
	public static DisplayPreferences getDisplaySettings() {
		Preferences prefs = getUserNode("/display");
		PreferencesManager preferencesManager = new PreferencesManager();
		DisplayPreferences displayPreferences = preferencesManager.new DisplayPreferences();
		displayPreferences.x = prefs.getInt("default_x", 0);
		displayPreferences.y = prefs.getInt("default_y", 0);
		displayPreferences.width = prefs.getInt("default_width", 400);
		displayPreferences.height = prefs.getInt("default_height", 400);
		return displayPreferences;
	}

	/**
	 * Save display settings.
	 *
	 * @param displayPreferences the new display preferences.
	 */
	public static void putDisplaySettings(DisplayPreferences displayPreferences) {
		Preferences prefs = getUserNode("/display");
		prefs.putInt("default_x", displayPreferences.x);
		prefs.putInt("default_y", displayPreferences.y);
		prefs.putInt("default_width", displayPreferences.width);
		prefs.putInt("default_height", displayPreferences.height);
	}

	/**
	 * Load intra-day quote sync module preferences.
	 *
	 * @return the preferences.
	 * @see nz.org.venice.quote.IDQuoteSyncModule
	 */
	public static IDQuoteSyncPreferences getIDQuoteSyncPreferences() {
		Preferences prefs = getUserNode("/id_quote_sync");
		PreferencesManager preferencesManager = new PreferencesManager();
		IDQuoteSyncPreferences idQuoteSyncPreferences = preferencesManager.new IDQuoteSyncPreferences();

		idQuoteSyncPreferences.isEnabled = prefs.getBoolean("isEnabled", false);
		idQuoteSyncPreferences.symbols = prefs.get("symbols", "");
		idQuoteSyncPreferences.suffix = prefs.get("suffix", "");

		try {
			idQuoteSyncPreferences.openTime = new TradingTime(prefs.get("openTime", "9:00:00"));
			idQuoteSyncPreferences.closeTime = new TradingTime(prefs.get("closeTime", "16:00:00"));
		} catch (TradingTimeFormatException e) {
			// This should never happen - but deal with the possibility gracefully.
			idQuoteSyncPreferences.openTime = new TradingTime(9, 0, 0);
			idQuoteSyncPreferences.closeTime = new TradingTime(16, 0, 0);
		}

		idQuoteSyncPreferences.period = prefs.getInt("period", 60);
		return idQuoteSyncPreferences;
	}

	/**
	 * Save intra-day quote sync module preferences.
	 *
	 * @param idQuoteSyncPreferences the preferences.
	 * @see nz.org.venice.quote.IDQuoteSyncModule
	 */
	public static void putIDQuoteSyncPreferences(IDQuoteSyncPreferences idQuoteSyncPreferences) {
		Preferences prefs = getUserNode("/id_quote_sync");
		prefs.putBoolean("isEnabled", idQuoteSyncPreferences.isEnabled);
		prefs.put("symbols", idQuoteSyncPreferences.symbols);
		prefs.put("suffix", idQuoteSyncPreferences.suffix);
		prefs.put("openTime", idQuoteSyncPreferences.openTime.toString());
		prefs.put("closeTime", idQuoteSyncPreferences.closeTime.toString());
		prefs.putInt("period", idQuoteSyncPreferences.period);

	}

	/**
	 * Return the location of the saved window data. The directory is created if it
	 * does not exist.
	 * 
	 */

	private static File getFrameSettingsHome() {
		File veniceHome = getVeniceHome();
		File frameSettingsHome = new File(veniceHome, "SavedWindows");
		if (!frameSettingsHome.exists())
			frameSettingsHome.mkdir();

		return frameSettingsHome;
	}

	/**
	 * Save open frame settings.
	 *
	 * @param frame A ModuleFrame representing an open window on the Venice desktop.
	 * @see ModuleFrame
	 */

	/**
	 * Return a list of frames saved on the filesystem.
	 *
	 * @return A vector where the elements are File objects containing saved
	 *         ModuleFrame data.
	 *
	 *         The location of the saved frames is ~/Venice/SavedWindows. As the
	 *         restore saved windows feature is new, there is no old preferences
	 *         mechanism.
	 */
	public static Vector getSavedFrames() {
		String[] savedFrameFileNames = PreferencesManager.getFrameSettingsHome().list();

		Vector savedFrames = new Vector();
		String suffix = ".xml";

		for (int i = 0; i < savedFrameFileNames.length; i++) {
			String savedFrameFileName = savedFrameFileNames[i];
			// Ignore files which are not XML
			if (!savedFrameFileName.endsWith(suffix)) {
				continue;
			}
			// Interested in the ModuleFrames, not the modules at this stage
			if (!savedFrameFileName.startsWith("FrameData")) {
				continue;
			}

			File savedFrameFile = new File(PreferencesManager.getFrameSettingsHome(), savedFrameFileName);

			savedFrames.add(savedFrameFile);
		}
		return savedFrames;
	}

	public static void putModuleFrameSettings(ModuleFrame frame) throws PreferencesException {

		// Don't want to spam the filesystem with saved frames
		// unless the user is interested in restoring them.
		if (!PreferencesManager.getRestoreSavedWindowsSetting()) {
			return;
		}

		try {

			// Don't save an empty file
			if (frame.getModule().getSettings() == null) {
				return;
			}

			File frameSettingsFile = new File(getFrameSettingsHome(),
					("FrameDataFor" + frame.getTitle()).replaceAll(" ", "") + "_" + frame.hashCode() + ".xml");

			FileOutputStream outputStream = new FileOutputStream(frameSettingsFile);

			ModuleFrameSettingsWriter settingsWriter = new ModuleFrameSettingsWriter();

			settingsWriter.write(frame, outputStream);
			outputStream.close();
		} catch (IOException e) {
			throw new PreferencesException(e.getMessage());
		} catch (ModuleSettingsParserException e) {
			throw new PreferencesException(e.getMessage());
		} catch (SecurityException e) {
			throw new PreferencesException(e.getMessage());
		}
	}

	/**
	 *
	 * Remove all the saved frames.
	 */
	public static void removeSavedFrames() {
		Vector list = getSavedFrames();
		Iterator iterator = list.iterator();

		while (iterator.hasNext()) {
			File f = (File) iterator.next();
			f.delete();
		}
	}

	/**
	 * Retrieve where the users is storing alerts
	 */
	public static String getAlertDestination() {
		Preferences prefs = getUserNode("/alert_destination");

		return prefs.get("destination", "disabled");
	}

	/**
	 * Save alert destination
	 *
	 * @param destination Where the alerts should be stored
	 */
	public static void putAlertDestination(String destination) {
		if (!destination.equals(Locale.getString("ALERT_DISABLE_ALL")) && !destination.equals(Locale.getString("FILE"))
				&& !destination.equals(Locale.getString("DATABASE"))) {
			assert false;
		}

		Preferences prefs = getUserNode("/alert_destination");
		prefs.put("destination", destination);

	}
}
