Merchant of Venice, 0.752 beta 26/December/2018
=========================================

Copyright (C) 2003-19, Andrew Leppard (andrew venice org nz)
See COPYING.txt for license terms.

1 Introduction

Thank you for trying Merchant of Venice (Venice).

Venice is a stock market trading programme that supports portfolio
management, charting, technical analysis, paper trading and
experimental methods like genetic programming. Venice runs in a
graphical user interface with online help and has full documentation.
Venice runs on UNIX, including Mac OS X and Windows.

2 Running

To run Venice you will need Java J2SE JRE 1.4 (recommended 1.4.2_19) 
or higher, available from the following location:

        http://java.sun.com/j2se/1.4.2/download.html

If you want to connect to a MySQL database you will need the database
software which is available here:

   MySQL
        http://www.mysql.com

Venice includes a copy of a MySQL JDBC driver for your convenience.

If you want to connect to a PostgreSQL databse you will need the
database software and driver which are available here:

   PostgreSQL
        http://www.postgresql.org/

   PostgreSQL Java Driver
        http://jdbc.postgresql.org/

Venice allows you to access stock quotes through an internal
database (hsqldb) which should require no configuration on your part.

Once they are set up, you can run Venice by typing from the command prompt:

   sh venice

Or in Windows by clicking on the "venice.jar" file.
Or in Mac OS X by clicking on the "Venice.app" file.

3 Contained technology

Venice contains the following technologies

   HSQLDB V1.8.0.4           from http://hsqldb.sourceforge.net
   Jython V2.1               from http://www.jython.org
   MySQL Connector/J V3.1.12 from http://www.mysql.com
   Joone V1.2.1              from http://www.jooneworld.com
   XStream V1.1.2            from http://xstream.codehaus.org
   Java Uuid Generator (JUG) from http://wiki.fasterxml.com/JugHome

4 Bug Reports and Enhancements

Please report any bugs that you encounter via the Sourceforge bug
tracking system at:

   https://sourceforge.net/tracker/?func=add&group_id=53631&atid=471025

If you have any ideas for enhancements, please document them via the
Sourceforge enhancement tracking system at:

   https://sourceforge.net/tracker/?func=add&group_id=53631&atid=471028

Or if you have any comments, please don't hesitate to email me,
Andrew Leppard (andrew venice org nz).

5 Building

To build Venice you will need to download a copy of the source. The
source is no longer included in the binary distribution. You can
download the source release which contains the source from the latest
release. Alternatively, you can download the current development
version from Subversion by running the following command:

svn co https://mov.svn.sourceforge.net/svnroot/mov/trunk/ venice

To build Venice you will need the programmes listed in the "Running"
section and the following:

    Ant 1.5 or higher
        http://jakarta.apache.org/builds/jakarta-ant/release/v1.5/

To run the unit tests you will need:

    JUnit 3.8.1 or higher
        http://www.junit.org/

To create the manual you wll need:

    Xalan-Java 2.7.1 or higher
        http://xml.apache.org/xalan-j/
   
To build Venice type the following:

ant build

You can then run Venice by either:

ant run

Or by creating a jar (ant jar) and then running Venice as described above.

The build file (build.xml) provides other functions for developers (some
of these will only work from source checked out from CVS):

api     Generate a javadoc API of the code
app     Generate a Mac OS X Venice application
backup  Pulls a backup copy of the CVS tree from Sourceforge and stores it
        in the backup directory
clean   Removes all built and temporary files
doc     Builds the documentation
jar     Create a java archive file for Venice
locale  Check locale files for consistency
release Packages Venice into a file ready for release
test    Runs the automated test suite
web     Packages the web files ready for deployment (Not included in
        the source release).

You can also use an IDE, Borland JBuilder 2005 to build and run Venice. 
The project file is in ide/borland.

6 Licenses

Venice is Copyright (C) 2003-2012, Andrew Leppard.
HSQLDB is Copyright (C) 1995-2000, The Hypersonic SQL Group.
Joone is Copyright (C) 2004, Paolo Marrone and the Joone team.
Jython is Copyright (C) 2000, Jython Developers.
XStream is Copyright (C) 2003-2005, Joe Walnes.
JUG is Copyright (C) 2010, Tatu Saloranta.

7 Thanks

Thanks to the following people for providing additional code, patches or
bugfixes:

Daniel Makovec, Quentin Bossard, Peter Fradley, Mark Hummel, Bryan Lin,
Alberto Nacher, Matthias Stöckel, Dennis van den Berg, Andrew Goh, Christian Brom, Guillermo Bonhevi.

And the following for providing translations:

Benedict P. Barszcz (Polish), Quentin Bossard (French), Bryan Lin (Chinese),
Alberto Nacher (Italian), Jordi Pujol (Catalan), Pontus Strömdahl (Swedish)
& Christian Brom (German)

The winged lion image is:

Copyright (C) Nicholas Aodhagan Webb (naw1 cec wustl edu)
