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

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import nz.org.venice.alert.AlertManager;
import nz.org.venice.prefs.PreferencesManager;
import nz.org.venice.prefs.PreferencesManager.DatabasePreferences;
import nz.org.venice.ui.DesktopManager;
import nz.org.venice.ui.PasswordDialog;
import nz.org.venice.util.Locale;

/**
 * Manages database settings where the data is session based (ie not stored on
 * disk )
 * 
 * Where the database is stored locally, a simple mask is applied. While this is
 * trivial to break, the purpose is to slow an attacker so they don't
 * immediately gain access to the database. (If an attacker has access to the
 * user account, not even a session password will help)
 * 
 * 
 * @author Mark Hummel
 * @see DatabaseManager
 * @see nz.org.venice.prefs.PreferencesManager
 */
public class DatabaseAccessManager {
	private static DatabaseAccessManager instance = null;
	private String enteredPassword = null;

	String k = "Bar12345Bar12345";
	SecretKey key = new SecretKeySpec(k.getBytes(), "AES");

	public static synchronized DatabaseAccessManager getInstance() {
		if (instance == null) {
			instance = new DatabaseAccessManager();
		}
		return instance;
	}

	private DatabaseAccessManager() {

	}

	/**
	 * Set the QuoteSource and AlertManager source to null, forcing the Factories to
	 * recreate their database objects. This method has an effect only if the user
	 * chooses to a use a session password.
	 * 
	 * This is used when the connection fails. So if the password is incorrect, the
	 * user has another chance to enter it correctly.
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
	 * @param masked A string which has had a mask applied by the mask method
	 * @return A plaintext string.
	 */
	public String unMask(String masked) {
		String unmasked = "";
		try {
			unmasked = decrypt(masked);
		} catch (Exception e) {
			System.out.println("Error unMask");
		}
		return unmasked;
	}

	/**
	 * Return a string after the mask has been applied to a plain text string.
	 * 
	 * @param unmasked A plaintext string
	 * @return an encrypted string
	 */
	public String mask(String unmasked) {
		String masked = "";
		try {
			masked = encrypt(unmasked);
		} catch (Exception e) {
			System.out.println("Error mask");
		}
		return masked;
	}

	/**
	 * Return the database password. If the password has been stored locally a
	 * plaintext string is returned after having been unmasked. Otherwise, if the
	 * user chooses to supply the password per session, and if the user has not
	 * already entered the password, prompt the user to enter it.
	 */
	public String getPassword() {
		DatabasePreferences prefs = PreferencesManager.getDatabaseSettings();

		if (!prefs.passwordPrompt) {
			// If the password is empty, no mask has been applied.
			return (prefs.password.equals("")) ? prefs.password : unMask(prefs.password);
		} else {
			if (enteredPassword == null) {
				PasswordDialog passwordField = new PasswordDialog(DesktopManager.getDesktop(),
						Locale.getString("PASSWORD_PROMPT"), Locale.getString("DATABASE_PASSWORD_TITLE"));
				enteredPassword = passwordField.showDialog();
			}
			return enteredPassword;
		}
	}

	private String encrypt(String str) throws Exception {
		// Encode the string into bytes using utf-8
		Cipher ecipher;
		ecipher = Cipher.getInstance("AES");
		ecipher.init(Cipher.ENCRYPT_MODE, key);

		byte[] utf8 = str.getBytes("UTF8");

		// Encrypt
		byte[] enc = ecipher.doFinal(utf8);

		// Encode bytes to base64 to get a string
		return Base64.getEncoder().encodeToString(enc);
		// return new sun.misc.BASE64Encoder().encode(enc);
	}

	private String decrypt(String str) throws Exception {
		Cipher dcipher;
		dcipher = Cipher.getInstance("AES");
		dcipher.init(Cipher.DECRYPT_MODE, key);
		// Decode base64 to get bytes
		byte[] dec = Base64.getDecoder().decode(str);

		byte[] utf8 = dcipher.doFinal(dec);

		// Decode using utf-8
		return new String(utf8, "UTF8");
	}

}
