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

import java.beans.ExceptionListener;
import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JScrollPane;
import javax.xml.parsers.DocumentBuilderFactory;

import nz.org.venice.main.ModuleFrame;
import nz.org.venice.util.ExchangeRateCache;

/**
 * This class writes settings in XML format.
 *
 * @author Mark Hummel
 * @see nz.org.venice.prefs.PreferencesManager
 * @see ISettingsWriter
 */
public class ModuleFrameSettingsWriter {

	public ModuleFrameSettingsWriter() {
		// Nothing to do
	}

	/**
	 * Write the module settings to the output stream in XML format.
	 *
	 * @param frame  the module frame data to write
	 * @param stream the output stream to write the window settings.
	 */
	public void write(ModuleFrame frame, OutputStream stream) throws IOException, ModuleSettingsParserException {

		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

		ModuleFrameSettings settings = new ModuleFrameSettings();
		ISettings moduleSettings = frame.getModule().getSettings();

		if (moduleSettings != null) {

			settings.setModuleSettings(moduleSettings);
			settings.setBounds(frame.getBounds());

			/*
			 * ModuleFrames which are not enclosed have components which manage their own
			 * scrollPane. That is why the scroll bar settings appears twice.
			 */
			if (frame.getModule().encloseInScrollPane()) {
				JScrollPane scrollPane;
				scrollPane = frame.getScrollPane();
				settings.setScrollBarValues(scrollPane);
				moduleSettings.setScrollBarValues(scrollPane);
			}

			XMLEncoder encoder = new XMLEncoder(stream);
		    encoder.setExceptionListener(new ExceptionListener() {
		            public void exceptionThrown(Exception e) {
		              System.out.println("Exception! :"+e.toString());
		            }
		    });
		    encoder.writeObject(settings);
		    encoder.close();
		} else {
			throw new ModuleSettingsParserException("No Settings to save");
		}
	}

}