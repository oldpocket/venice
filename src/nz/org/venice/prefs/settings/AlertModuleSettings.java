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

import java.util.List;

/**
 * This class  represents Alert Module data  which can restore modules upon restart. 
 * 
 * @author Mark Hummel
 * @see AlertModule
 * @see ModuleSettingsWriter
 * @see ModuleSettingReader 
*/

import javax.swing.JDesktopPane;

import nz.org.venice.alert.AlertException;
import nz.org.venice.alert.AlertManager;
import nz.org.venice.alert.AlertModule;
import nz.org.venice.alert.IAlertReader;
import nz.org.venice.alert.IAlertWriter;
import nz.org.venice.main.IModule;

public class AlertModuleSettings extends AbstractSettings {

	List symbols;

	public AlertModuleSettings() {
		super(ISettings.ALERTS, ISettings.MODULE);
	}

	public AlertModuleSettings(List symbols) {
		super(ISettings.ALERTS, ISettings.MODULE);
		this.symbols = symbols;
	}

	public void setSymbols(List symbols) {
		this.symbols = symbols;
	}

	public IModule getModule(JDesktopPane desktop) {
		IAlertReader reader = AlertManager.getReader();
		IAlertWriter writer = AlertManager.getWriter();

		IModule alertModule = null;

		try {
			if (symbols == null) {
				alertModule = new AlertModule(desktop, reader, writer);
			} else {
				alertModule = new AlertModule(desktop, symbols, reader, writer);
			}
		} catch (AlertException e) {
			// Ignore here
		} finally {
			return alertModule;
		}
	}
}