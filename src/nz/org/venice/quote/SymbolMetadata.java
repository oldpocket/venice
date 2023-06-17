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

/**
 * Data definition of symbol metadata, including if the symbol is an index.
 * May include other data in the future, such as shares on issue.
 * Venice presents index symbols slightly differently. 
 *
 * This data is currently set by the user.
 *
 * @author mhummel
 * @see Symbol
 * @see IQuoteSource
 * @see SymbolMetadataPreferencesPage
 */

package nz.org.venice.quote;



public class SymbolMetadata {
	
	public enum SymbolType {
		EQUITY("equity"),
		CRYPTO("crypto"),
		INDEX("index");
		
		private String type;
		
		SymbolType(String type) {
			this.type = type;
		}
		
		public String getType() {
			return this.type;
		}
	}

	private String symbol;
	private String prefix;
	private String posfix;
	private SymbolType type;
	private String name;
	private Boolean sync_intra_day;

	public SymbolMetadata() {
		
	}
	
	/**
	 * Construct a new index definition.
	 * 
	 * @param symbol  The symbol (e.g. 'BVSP', or 'PETR4')
	 * @param prefix  Prefix of the symbol
	 * @param posfix  Posfix of the symbol (e.g. '.SA')
	 * @param type	  The type of the symbol (equity, crypto, index)
	 * @param name    The name of the company or index related with the symbol
	 * @param sync_id True if the symbol is synchronized during intra-day
	 */
	public SymbolMetadata(Symbol symbol, String prefix, String posfix, 
			SymbolType type, String name, boolean sync_id) {
		//this.symbolObj = symbol;
		this.symbol = this.symbol.toString();
		this.prefix = prefix;
		this.posfix = posfix;
		this.type = type;
		this.name = name;
		this.sync_intra_day = sync_id;
	}
	
	/**
	 * Construct a new index definition.
	 * 
	 * @param symbolString  The symbol in a string format to search for
	 * @param prefix  		Prefix of the symbol
	 * @param posfix  		Posfix of the symbol (e.g. '.SA')
	 * @param type	  		The type of the symbol (equity, crypto, index)
	 * @param name    		The name of the company or index related with the symbol
	 * @param sync_id 		True if the symbol is synchronized during intra-day
	 */
	public SymbolMetadata(String symbolString, String prefix, String posfix, 
			SymbolType type, String name, boolean sync_id) {
		
		this.symbol = symbolString;
		this.prefix = prefix;
		this.posfix = posfix;
		this.type = type;
		this.name = name;
		this.sync_intra_day = sync_id;
		
	}

	/**
	 * Return prefix from the symbol.
	 * 
	 * @return prefix string
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * Return posfix from the symbol.
	 * 
	 * @return posfix string
	 */
	public String getPosfix() {
		return posfix;
	}
	
	/**
	 * Return the symbol.
	 * 
	 * @return the symbol
	 */
	public Symbol getSymbol() {
		Symbol symbolObj = null;
		try {
			symbolObj = Symbol.find(symbol);
		} catch (SymbolFormatException sfe) {
		} finally {

		}
		return symbolObj;
	}
	
	/**
	 * Return the name of the company or index.
	 * 
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return if this symbol is a INDEX
	 * 
	 * @return true if the symbol is an index.
	 */
	public boolean isIndex() {
		return type == SymbolType.INDEX;
	}

	/**
	 * Return the type of the symbol.
	 * 
	 * @return true if the symbol is an index.
	 */
	public SymbolType getType () {
		return type;
	}
	
	/**
	 * Return if the symbol should be synchronized during intra-day
	 * 
	 * @return true if the symbol is an index.
	 */
	public boolean syncIntraDay() {
		return sync_intra_day;
	}
	
	/**
	 * Return the full name of the symbol, with pre and posfix.
	 * 
	 * @return true if the symbol is an index.
	 */
	public String toString() {
		return prefix.toString() + symbol.toString() + posfix.toString();
	}

}