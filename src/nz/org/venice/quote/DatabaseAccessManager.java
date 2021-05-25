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

import nz.org.venice.ui.DesktopManager;
import nz.org.venice.ui.PasswordDialog;
import nz.org.venice.prefs.PreferencesManager;
import nz.org.venice.prefs.PreferencesManager.DatabasePreferences;
import nz.org.venice.alert.AlertManager;
import nz.org.venice.util.Locale;
import nz.org.venice.util.TradingDate;

import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

/**
 * Manages database settings where the data is session based (ie not stored
 * on disk )  

 * Where the database is stored locally, a simple mask is applied. While 
 * this is trivial to break, the purpose is to slow an attacker so they 
 * don't immediately gain access to the database.  (If an attacker has access 
 * to the user account, not even a session password will help)

 * 
 * @author Mark Hummel
 * @see DatabaseManager
 * @see nz.org.venice.prefs.PreferencesManager
 */
public class DatabaseAccessManager 
{
    private static DatabaseAccessManager instance = null;
    private String enteredPassword = null;

    //Generated by UUIDGenerator
    String salt = "18cf3df0-1627-40db-9f6b-05822778e355";


    public static synchronized DatabaseAccessManager getInstance() {
	if (instance == null) {
	    instance = new DatabaseAccessManager();
	}
	return instance;
    }

    private DatabaseAccessManager() {
	
    }

    /**
     * Set the QuoteSource and AlertManager source to null, forcing the
     * Factories to recreate their database objects. This method has an effect
     * only if the user chooses to a use a session password. 
     * 
     * This is used when the connection fails. So if the password is incorrect,
     * the user has another chance to enter it correctly.
     */
    public void reset() {
	if (PreferencesManager.getDatabaseSettings().passwordPrompt) {
	    QuoteSourceManager.setSource(null);
	    AlertManager.setSourceInstance(null);
	    enteredPassword = null;
	}
    }


    /**     
     * Return a plaintext string with the mask removed.  
     * 
     * @param masked  A string which has had a mask applied by the mask method
     * @return A plaintext string.
     */
    public String unMask(String masked) {
	String unMask = applyMaskToPassword(masked);
	String unpadded = unPad(unMask);

	return unpadded;
    }

    /**
     * Return a string after the mask has been applied to a plain text string. 
     * 
     * @param unmasked  A plaintext string
     * @return an encrypted string
     */

    public String mask(String unmasked) {
	String padded = pad(unmasked);
	String masked = applyMaskToPassword(padded);

	return masked;
    }

    /**
     * Return the database password. If the password has been stored locally
     * a plaintext string is returned after having been unmasked.
     * Otherwise, if the user chooses to supply the password per session,
     * and if the user has not already entered the password, prompt the user
     * to enter it.
     */
    public String getPassword() {	
	DatabasePreferences prefs = PreferencesManager.getDatabaseSettings();

	if (!prefs.passwordPrompt) {	    
	    //If the password is empty, no mask has been applied.
	    return (prefs.password.equals("")) 
		? prefs.password
		: unMask(prefs.password);
	} else {
	    if (enteredPassword == null) {
		PasswordDialog passwordField = new PasswordDialog(DesktopManager.getDesktop(), Locale.getString("PASSWORD_PROMPT"), Locale.getString("DATABASE_PASSWORD_TITLE"));
		enteredPassword = passwordField.showDialog();
	    }
	    return enteredPassword;
	}
    }

    //Simple XOR "encryption"/decryption
    private String applyMaskToPassword(String password) {
	String key = getKey();
	
	char[] passCharList = password.toCharArray();
	char[] keyCharList = key.toCharArray();
	
	for (int i = 0; i < passCharList.length; i++) {
	    passCharList[i] ^= keyCharList[i];
	}
	      
	return new String(passCharList);
    }

    private String getKey() {
	//Generated by UUIDGenerator
	String startingKey = "cae6d9a9-e568-4835-b551-8eb924c6584a";

	int[] fibs = {1,1,2,3,5,8,13,21,34};

	char[] charList = startingKey.toCharArray();

	int j = 0;
	for (int i = 0; i < startingKey.length(); i++) {
	    int index = (i + fibs[j]) % startingKey.length();
	    j = (j+1) % fibs.length;
	    	    
	    int prevChar = charList[index];
	    int newChar = prevChar + 1 % 60;
	    charList[index] = (char)newChar;
	}
       	
	return new String(charList);
    }

    private String pad(String str) {
	String padded = "";
	
	char[] charList = str.toCharArray();
	char[] saltCharList = salt.toCharArray();
	
	int lenDiff = salt.length() - str.length();
	//No padding required
	if (lenDiff <= 0) {
	    return str;
	}

	for (int i = 0; i < lenDiff; i++) {
	    padded += saltCharList[i];
	}
	padded += str;
	return padded;
    }

    private String unPad(String str) {
	String unPadded = "";
	boolean passFound = false;

	for (int i = 0; i < str.length(); i++) {
	    if (str.charAt(i) != salt.charAt(i)) {
		passFound = true;
	    }
	    if (passFound) {
		unPadded += str.charAt(i);
	    }
	}
	return unPadded;
    }
}


