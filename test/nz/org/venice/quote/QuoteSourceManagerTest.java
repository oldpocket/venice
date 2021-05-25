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
/*
 * Created on 2004-nov-25
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package nz.org.venice.quote;

import junit.framework.TestCase;

/**
 * @author Pontus Strömdahl
 *
 * Verifies the continued functionality of QuoteSourceManager for future
 * code changes.
 * 
 * @see QuoteSourceManager
 * @see QuoteSourceFactory
 */
public class QuoteSourceManagerTest extends TestCase {

    public void testGetSource() {
        assertTrue(QuoteSourceManager.getSource() instanceof QuoteSource);
    }

    public void testSetSource() {
        FileQuoteSource qs1 = QuoteSourceFactory.createSamplesQuoteSource();
        FileQuoteSource qs2 = QuoteSourceFactory.createSamplesQuoteSource();
        QuoteSourceManager.flush();
        QuoteSourceManager.setSource(qs1);
        assertEquals(QuoteSourceManager.getSource(),qs1);
        QuoteSourceManager.flush();
        QuoteSourceManager.setSource(qs2);
        assertEquals(QuoteSourceManager.getSource(),qs2);
    }

    public void testCreateInternalQuoteSource() {
        assertTrue(QuoteSourceFactory.createInternalQuoteSource() instanceof DatabaseQuoteSource);
    }

    public void testCreateSamplesQuoteSource() {
        assertTrue(QuoteSourceFactory.createSamplesQuoteSource() instanceof FileQuoteSource);
    }

    public void testCreateDatabaseQuoteSource() {
        assertTrue(QuoteSourceFactory.createDatabaseQuoteSource() instanceof DatabaseQuoteSource);
    }

}
