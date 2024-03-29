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

package nz.org.venice.prefs.settings;

import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class parses ModuleFrameSettings written in XML format.
 *
 * @author Mark Hummel
 * @see nz.org.venice.main.ModuleFrame
 * @see ModuleFrameSettingsWriter
 */
public class ModuleFrameSettingsReader {

	static ISettings moduleSettings;
	static ModuleFrameSettings settings;

	/**
	 * This class cannot be instantiated.
	 */
	private ModuleFrameSettingsReader() {
		// Nothing to do
	}

	/**
	 * Read and parse the moduleframe settings in XML format from the input stream
	 * and return the moduleframe settings object.
	 *
	 * @param stream the input stream containing the watch screen in XML format
	 * @return the moduleframesettings
	 * @exception IOException                   if there was an I/O error reading
	 *                                          from the stream.
	 * @exception ModuleSettingsParserException if there was an error parsing the
	 *                                          watch screen.
	 */
	public static ModuleFrameSettings read(InputStream stream) throws IOException, ModuleSettingsParserException {

		if (settings == null) {
			settings = new ModuleFrameSettings();
		}

		try {
			XMLDecoder decoder = new XMLDecoder(stream);
			ModuleFrameSettings result = (ModuleFrameSettings) decoder.readObject();
	    	decoder.close();
	    	return (ModuleFrameSettings) result;
		} catch (Exception xe) {
			// Caused by Malformed XML, unable to instantiate object
			throw new ModuleSettingsParserException(xe.getMessage());
		}
	}

}
