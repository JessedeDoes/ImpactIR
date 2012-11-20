package nl.namescape.util.database;
//Connect to a database via JDBC-ODBC - Real's Java How-to ... <- More
       

/*
Connect to a database via JDBC-ODBC
You have to keep in mind that the bridge JDBC-ODBC is only useful in an 
Application, you can't use it with JAVA Applet because ODBC requires some DLL on 
the client machine (forbidden for security reason). 
*/

import java.net.URL;
import java.sql.*;
import java.io.*;

class AccessDatabase
{
  static Connection theConn;

  public static void main (String args[]) 
  {
    try
    {
      String database = args[1];
      String action = args[0];
      if (action.equals("dumpApestaarten"))
      {
        String sFrom = args[2];
        String sTo = args[3];

        int iFrom = Integer.parseInt(sFrom);
        int iTo = Integer.parseInt(sTo);
        dumpApestaarten(database, sFrom, sTo);
      } else if (action.equals("dumpQuery"))
      {
        String sql = args[2];
        dumpQuery(database,sql);
      } else if (action.equals("runUpdate"))
      {
        String sql = args[2];
        runUpdate(database,sql);
      }
    } catch (Exception e)
    {
      e.printStackTrace();
    }
  }

   
  public static void dumpApestaarten(String database, String sFrom, String sTo)
  {
    String sql = sql = "select * from apestaarten where id >= " + sFrom + " and id < " + sTo;
    dumpQuery(database, sql);
  }

  
  public static void dumpQuery(String database, String sql)
  {
    try 
    {
      // connection to an ACCESS MDB
      theConn = OdbcConnection.getConnection(database);

      ResultSet rs;
      Statement stmt;

      PrintStream ps = null;

     try 
     {
       ps = new PrintStream(System.out, true, "UTF-8");
     } catch (UnsupportedEncodingException error) 
     {
       System.err.println(error);
       System.exit(0);
     }

      stmt = theConn.createStatement();
      rs = stmt.executeQuery(sql);

      ResultSetMetaData rsmd = rs.getMetaData();
      int numCols = rsmd.getColumnCount();
      
      while (rs.next()) 
      {
         for (int i=1; i <= numCols; i++) 
         {
           String z = "";
           try 
           {
             z = rs.getString(i);
           } catch (Exception e) {}; 

           ps.print(z);
           if (i < numCols) ps.print("\t"); else ps.print("\n");
         }
      }
      rs.close();
      stmt.close();
    }
    catch (Exception e) 
    {
        e.printStackTrace();
    }
    finally 
    {
      try 
      {
        if (theConn != null) theConn.close();
      }
      catch (Exception e) { }
    }
  }

  public static void runUpdate(String database, String sql)
  {
    try 
    {
      // connection to an ACCESS MDB
      theConn = AccessConnection.getConnection(database);

      Statement stmt;


      stmt = theConn.createStatement();
      stmt.executeUpdate(sql);
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    finally 
    {
      try 
      {
        if (theConn != null) theConn.close();
      }
      catch (Exception e) 
      {
      }
    }
  }
}

class AccessConnection 
{
   public static Connection getConnection(String dbFileName) throws Exception 
   {
      Driver d = (Driver) Class.forName("sun.jdbc.odbc.JdbcOdbcDriver").newInstance(); // not used?
      Connection c = DriverManager.getConnection("jdbc:odbc:Driver={Microsoft Access Driver (*.mdb, *.accdb)};DBQ=" + dbFileName);
      return c;
      /*
        To use an already defined ODBC Datasource :    
        String URL = "jdbc:odbc:myDSN";
        Connection c = DriverManager.getConnection(URL, "user", "pwd"); 
      */     
    }
}

class OdbcConnection
{
  public static Connection getConnection(String dataSourceName) throws Exception
  {
     Driver d = (Driver) Class.forName("sun.jdbc.odbc.JdbcOdbcDriver").newInstance();
     String URL = "jdbc:odbc:" + dataSourceName;
     Connection c = DriverManager.getConnection(URL, "jesse", "d2d4d7d5");
     return c;
  }
}

