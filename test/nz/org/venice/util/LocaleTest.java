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

import java.util.ResourceBundle;
import java.util.Enumeration;
import java.util.HashMap;

import junit.framework.TestCase;


public class LocaleTest extends TestCase {

    private final static String UNKNOWN = "???";       
    private static boolean resourceBundlesLoaded = false;
    private static java.util.Locale locale = null;
   
    private static ResourceBundle primaryResourceBundle = null;

    //Test that no string returns "???" 
    //Very picky
    public void testCompleteness() {
	
	java.util.Locale[] locales = LocaleConstants.locales;
	//Don't test English locale
	for (int i = 1; i < locales.length; i++) {
	    locale = locales[i];	    
	    loadResourceBundles();
	
	    Enumeration keys = primaryResourceBundle.getKeys();
	    while (keys.hasMoreElements()) {
		String key = (String)keys.nextElement();
		String val = nz.org.venice.util.Locale.getString(key);
		
		if (val.equals(UNKNOWN)) {
		    System.out.println("Missing translation for: " + key + " got: " + val);
		    //fail("Missing translation for: " + key + " got: " + val);		    
		}
	    }
	}	
    }
    
    //Test that if the English locale text has parameters
    //so does the test locale 
    public void testLocaleParameters() {
	java.util.Locale[] locales = LocaleConstants.locales;

	HashMap parameterCount = new HashMap();

	for (int i = 0; i < locales.length; i++) {
	    locale = locales[i];	    
	    loadResourceBundles();
	
	    Enumeration keys = primaryResourceBundle.getKeys();
	    while (keys.hasMoreElements()) {
		String key = (String)keys.nextElement();
		String val = nz.org.venice.util.Locale.getString(key);
		int parmsCount;
		
		if (i == 0) {
		    //Will pick up % in values which are not parameters
		    //but thats ok
		    parmsCount = val.replaceAll("[^%]","").length();
		    parameterCount.put(key, new Integer(parmsCount));		
		} else {
		    
		    parmsCount = val.replaceAll("[^%]","").length();		
		    Integer tmp = (Integer)parameterCount.get(key);
		    if (tmp != null) {
			int engParmCount = tmp.intValue();
			
			if (engParmCount != parmsCount) {
			    fail("Difference parameter count for: " + key + ". Expected: " + engParmCount + " got: " + parmsCount);
			}
		    }
		    
		}
	    }
	}

    }


    private void loadResourceBundles() {
	    try {
		primaryResourceBundle = ResourceBundle.getBundle("nz.org.venice.util.locale.venice", locale);
		locale = primaryResourceBundle.getLocale();
	    
	    } catch (Exception e) {
		System.out.println("Exception in load: " + e);
	    }
    }
}
