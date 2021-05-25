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

package nz.org.venice.portfolio;

import junit.framework.TestCase;

import nz.org.venice.quote.Symbol;
import nz.org.venice.quote.SymbolFormatException;

import nz.org.venice.util.Currency;
import nz.org.venice.util.Money;
import nz.org.venice.util.TradingDate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Tests the portfolio, accounts and transaction classes.
 *
 * @author Andrew Leppard
 */
public class PortfolioTest extends TestCase {

    private final static String PORTFOLIO_NAME = "TestPortfolioName";
    private final static String SHARE_ACCOUNT_NAME = "ShareAccountName";
    private final static String CASH_ACCOUNT_NAME = "CashAccountName";
    private final static String CASH_ACCOUNT_NAME2 = "CashAccountName2";

    /**
     * General portfolio test.
     */
    public void testPortfolio() {

        //
        // Test constructing a portfolio
        //

        TradingDate today = new TradingDate();
        TradingDate yesterday = today.previous(1);
        Symbol CBA = null;

        try {
            CBA = Symbol.find("CBA");
        }
        catch(SymbolFormatException e) {
            fail(e.getMessage());
        }

        Portfolio portfolio = new Portfolio(PORTFOLIO_NAME,
                                            Currency.getDefaultCurrency());
        assertEquals(portfolio.getName(), PORTFOLIO_NAME);
        assertFalse(portfolio.isTransient());

        CashAccount cashAccount = new CashAccount(CASH_ACCOUNT_NAME);
        CashAccount cashAccount2 = new CashAccount(CASH_ACCOUNT_NAME2);
        assertEquals(cashAccount.getName(), CASH_ACCOUNT_NAME);

        ShareAccount shareAccount = new ShareAccount(SHARE_ACCOUNT_NAME);
        assertEquals(shareAccount.getName(), SHARE_ACCOUNT_NAME);

        portfolio.addAccount(cashAccount);
        portfolio.addAccount(cashAccount2);
        portfolio.addAccount(shareAccount);
        assertEquals(portfolio.getAccounts().size(), 3);
        assertEquals(portfolio.countAccounts(Account.CASH_ACCOUNT), 2);
        assertEquals(portfolio.countAccounts(Account.SHARE_ACCOUNT), 1);
        assertEquals(portfolio.findAccountByName(CASH_ACCOUNT_NAME), cashAccount);
        assertEquals(portfolio.findAccountByName(CASH_ACCOUNT_NAME2), cashAccount2);
        assertEquals(portfolio.findAccountByName(SHARE_ACCOUNT_NAME), shareAccount);
        
        // Construct a portfolio using every transaction type
        Transaction transaction = Transaction.newDeposit(yesterday,
                                                         new Money(10000),
                                                         cashAccount);
        portfolio.addTransaction(transaction);

        transaction = Transaction.newWithdrawal(yesterday,
                                                new Money(500),
                                                cashAccount);
        portfolio.addTransaction(transaction);

        transaction = Transaction.newInterest(yesterday,
                                              new Money(50),
                                              cashAccount);
        portfolio.addTransaction(transaction);

        transaction = Transaction.newFee(yesterday,
                                         new Money(25),
                                         cashAccount);
        portfolio.addTransaction(transaction);

        transaction = Transaction.newAccumulate(yesterday,
                                                new Money(1000),
                                                CBA,
                                                1000,
                                                new Money (25),
                                                cashAccount,
                                                shareAccount);
        portfolio.addTransaction(transaction);
                
        transaction = Transaction.newReduce(yesterday,
                                            new Money(1000),
                                            CBA,
                                            500,
                                            new Money (25),
                                            cashAccount,
                                            shareAccount);
        portfolio.addTransaction(transaction);

        transaction = Transaction.newDividend(yesterday,
                                              new Money(100),
                                              CBA,
                                              cashAccount,
                                              shareAccount);
        portfolio.addTransaction(transaction);

        transaction = Transaction.newDividendDRP(today,
                                                 CBA,
                                                 100,
                                                 shareAccount);
        portfolio.addTransaction(transaction);

        transaction = Transaction.newTransfer(yesterday,
                                              new Money(100),
                                              cashAccount,
                                              cashAccount2);
        portfolio.addTransaction(transaction);
        assertEquals(portfolio.getStartDate(), yesterday);
        assertEquals(portfolio.getLastDate(), today);
        assertEquals(portfolio.getSymbolsTraded().size(), 1);
        assertEquals(portfolio.getStocksHeld().size(), 1);
        assertEquals(portfolio.countTransactions(), 9);
        assertEquals(portfolio.countTransactions(Transaction.WITHDRAWAL), 1);
        assertEquals(portfolio.getTransactions().size(), 9);
        assertEquals(new Money(9575),
                     portfolio.getCashValue(portfolio.getLastDate()));

        //
        // Test exporting and then importing the portfolio.
        //

        Portfolio importedPortfolio = new Portfolio(PORTFOLIO_NAME,
                                                    Currency.getDefaultCurrency());
        try {
            File tempFile = File.createTempFile("venice_test",  null);

            // Write Portfolio...
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            PortfolioWriter.write(portfolio, outputStream);
            outputStream.close();

            // ... and read it back in again.
            FileInputStream inputStream = new FileInputStream(tempFile);
            importedPortfolio = PortfolioReader.read(inputStream);
            inputStream.close();
        }
        catch(IOException e) {
            fail(e.getMessage());
        }
        catch(PortfolioParserException e) {
            fail(e.getMessage());
        }
        catch(SecurityException e) {
            fail(e.getMessage());
        }

        assertEquals(portfolio, importedPortfolio);

        //
        // Test cloned portfolio.
        //

        Portfolio clonedPortfolio = (Portfolio)portfolio.clone();
        assertEquals(portfolio, clonedPortfolio);

        //
        // Test removing all transactions
        //
        
        portfolio.removeAllTransactions();
        assertEquals(portfolio.getStartDate(), null);
        assertEquals(portfolio.getLastDate(), null);
        assertEquals(portfolio.getSymbolsTraded().size(), 0);
        assertEquals(portfolio.getStocksHeld().size(), 0);
        assertEquals(portfolio.countTransactions(), 0);
        assertEquals(portfolio.countTransactions(Transaction.WITHDRAWAL), 0);
        assertEquals(portfolio.getTransactions().size(), 0);        
    }
}
