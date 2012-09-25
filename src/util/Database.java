package util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

public class Database
{
	public Connection connection = null;
	static boolean PREPEND_COLUMN_NAMES = false;
	public String mysqlhost = "impactdb.inl.loc";
	public String mysqlport = "3306";
	public String mysqldbname = "SpanishLexiconV3";
	public String mysqltablename = "lemmata";
	//String article_stylesheet_location="/home/jesse/Projects/WntConversie/Linkdb/NieuweOpzet/ewn.xsl";
	public String mysqlurl = "jdbc:mysql://" + mysqlhost + ":" + mysqlport + "/" + mysqldbname;
	//+ "?useUnicode=true&characterEncoding=utf8&autoReconnect=true";
	public String mysqluser = "impact";
	public String mysqlpasswd = "impact";

	public Database(Properties props)
	{
		mysqlhost = props.getProperty("mysqlhost");
		mysqlport = props.getProperty("mysqlport");
		mysqldbname = props.getProperty("mysqldbname");
		mysqluser = props.getProperty("mysqluser");
		mysqlpasswd = props.getProperty("mysqlpasswd");
		mysqltablename = props.getProperty("mysqltablename");
		mysqlurl = "jdbc:mysql://" + mysqlhost + ":" + mysqlport + "/" + mysqldbname;
		init();
	}

	public Database(String databaseName)
	{
		this.mysqldbname = databaseName;
		mysqlurl = "jdbc:mysql://" + mysqlhost + ":" + mysqlport + "/" + mysqldbname;
		System.err.println(mysqlurl);
		init();
	}

	public Database()
	{
		init();
	}

	public void init()
	{
		System.err.println("connecting.....");
		try 
		{
			this.connection = (new ConnectorSimple()).connect(mysqlurl, mysqluser, mysqlpasswd);
			System.err.println("Connected: "  + this.connection);
		} catch (Exception e)
		{
			e.printStackTrace();
			//System.exit(1);
		}
	}

	public void setHostPort(String host, int port)
	{
		this.mysqlhost = host;
		if (port <= 0)
		{  
			this.mysqlport = "3306";
		} else
		{
			this.mysqlport = new Integer(port).toString();
		}
	}

	public void setDatabase(String dbname)
	{
		mysqldbname = dbname;
	}

	public void setTablename(String tablename)
	{
		mysqltablename = tablename;
	}


	public boolean tableExists(String tablename)
	{
		try
		{
		 DatabaseMetaData meta = connection.getMetaData();
	      ResultSet res = meta.getTables(null, null, null, 
	         new String[] {"TABLE"});
	      boolean found = false;
	      
	      while (res.next()) 
	      {
	    	 String name = res.getString("TABLE_NAME");
	    	 if (name.equalsIgnoreCase(tablename))
	    		 found = true;
	    	 /*
	         System.err.println(
	            "   "+res.getString("TABLE_CAT") 
	           + ", "+res.getString("TABLE_SCHEM")
	           + ", "+res.getString("TABLE_NAME")
	           + ", "+res.getString("TABLE_TYPE")
	           + ", "+res.getString("REMARKS")); 
	         */
	      }
	      res.close();
	      return found;
		} catch (Exception e)
		{
			
		}
		return false;
	}

	public Vector<Vector<String>> SimpleSearch(String tableName,  Vector<String> whereclauses, int start, int nof) throws Exception
	{
		PreparedStatement stmt = null;
		String where = "";
		Vector<String> values = new  Vector<String>();
		boolean got_some = false;

		//int start=0; int nof=250;

		for (int i=0; i < whereclauses.size(); i+=3)
		{
			String val = whereclauses.get(i+2);
			String operator =  whereclauses.get(i+1);
			if (val == null || val.length() == 0) continue;

			if (got_some) where += " and ";
			where += whereclauses.get(i);
			if (operator.equals("=") || operator.equals("like") || operator.equals("regexp")) where += " " + operator + "? ";
			values.addElement(val);
			got_some = true;
		} 

		String query = "SELECT * from " + tableName + " where " + where + " limit " + start + "," + nof;
		stmt = this.connection.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

		for (int i=0; i < values.size(); i++)
		{
			stmt.setBytes(i+1, values.get(i).getBytes("UTF-8"));
		}
		System.err.println(stmt);
		ResultSet rs = stmt.executeQuery();
		Vector<Vector<String>> types = new Vector<Vector<String>>();
		int nofcolumns = rs.getMetaData().getColumnCount();

		Vector<String> fields = new Vector<String>(); 
		for (int i=1; i <= nofcolumns; i++)
		{
			fields.addElement(rs.getMetaData().getColumnName(i));
		}
		if (PREPEND_COLUMN_NAMES)
		{
			types.addElement(fields);
		} 

		while (rs.next()) // mis je nu de eerste??
		{
			Vector<String> row = new Vector<String>();
			for (int i=1; i <= nofcolumns; i++)
			{
				try
				{
					String s = new String(rs.getBytes(i), "UTF-8");
					row.addElement(s);
				} catch (Exception e)
				{
					row.addElement("NULL");
				}
			}
			types.addElement(row);
		}
		return types;
	}

	public Vector<Vector<String>> SimpleSelect(String query) 
	{
		PreparedStatement stmt = null;
		Vector<Vector<String>> types = new Vector<Vector<String>>();
		try
		{
			stmt = this.connection.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = stmt.executeQuery();
			int nofcolumns = rs.getMetaData().getColumnCount();
			while (rs.next()) // mis je nu de eerste??
			{
				Vector<String> row = new Vector<String>();
				for (int i=1; i <= nofcolumns; i++)
				{
					try
					{
						String s = new String(rs.getBytes(i), "UTF-8");
						row.addElement(s);
					} catch (Exception e)
					{
						row.addElement("NULL");
					}
				}
				types.addElement(row);
			}
		} catch (Exception e)
		{
			System.err.println(query);
			e.printStackTrace();
		}
		return types;
	}

	public static class MapFetcher
	{
		ResultSet rs;
		int nofcolumns;
		Vector<String> fields;
		
		public MapFetcher(Connection connection, String query)
		{
			try
			{
				PreparedStatement stmt = connection.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				ResultSet rs = stmt.executeQuery();
				init(rs);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		public void init(ResultSet rs)
		{
			this.rs=rs;
			//System.err.println(rs);
			try
			{
				nofcolumns = rs.getMetaData().getColumnCount();

				fields = new Vector<String>();
				for (int i=1; i <= nofcolumns; i++)
				{
					fields.addElement(rs.getMetaData().getColumnName(i));
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		public MapFetcher(ResultSet rs)
		{
			init(rs);
		}
		
		public Map<String,String> fetchMap()
		{
			try 
			{
				if (rs.next()) // mis je nu de eerste??
				{
					Map<String,String> m = new HashMap<String,String>();
					
					for (int i=1; i <= nofcolumns; i++)
					{
						try
						{
							String s = new String(rs.getBytes(i), "UTF-8");
							m.put(fields.get(i-1), s);
						} catch (Exception e)
						{
							
						}
					}
					return m;
				}
			} catch (SQLException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}
	
	
	public Vector<Vector<String>> SimpleSelect(String tableName, Vector<String> selected_fields, Vector<String> whereclauses, int start, int nof) throws Exception
	{
		PreparedStatement stmt = null;
		String where = "";
		Vector<String> values = new  Vector<String>();
		boolean got_some = false;

		//int start=0; int nof=250;
		String selected = "";
		for (int i=0; i < selected_fields.size(); i++)
		{
			if (i < selected_fields.size() -1)
			{ 
				selected += selected_fields.get(i) + ", ";  
			} else
			{
				selected += selected_fields.get(i);
			}
		}
		for (int i=0; i < whereclauses.size(); i+=3)
		{
			String val = whereclauses.get(i+2);
			String operator =  whereclauses.get(i+1);
			if (val == null || val.length() == 0) continue;

			if (got_some) where += " and ";
			where += whereclauses.get(i);
			if (operator.equals("=") || operator.equals("like") || operator.equals("regexp")) where += " " + operator + "? ";
			values.addElement(val);
			got_some = true;
		}

		String query = "SELECT " + selected + " from " + tableName + " where " + where + " limit " + start + "," + nof;
		stmt = this.connection.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

		for (int i=0; i < values.size(); i++)
		{
			stmt.setBytes(i+1, values.get(i).getBytes("UTF-8"));
		}
		System.err.println(stmt);
		ResultSet rs = stmt.executeQuery();
		Vector<Vector<String>> types = new Vector<Vector<String>>();
		int nofcolumns = rs.getMetaData().getColumnCount();

		Vector<String> fields = new Vector<String>();
		for (int i=1; i <= nofcolumns; i++)
		{
			fields.addElement(rs.getMetaData().getColumnName(i));
		}
		if (PREPEND_COLUMN_NAMES)
		{
			types.addElement(fields);
		}

		while (rs.next()) // mis je nu de eerste??
		{
			Vector<String> row = new Vector<String>();
			for (int i=1; i <= nofcolumns; i++)
			{
				try
				{
					String s = new String(rs.getBytes(i), "UTF-8");
					row.addElement(s);
				} catch (Exception e)
				{
					row.addElement("NULL");
				}
			}
			types.addElement(row);
		}
		return types;
	}


	public Vector<Vector<String>> getTableContent(String table_name, int lb, int ub) throws Exception
	{
		PreparedStatement stmt = null;

		stmt = this.connection.prepareStatement(
				"select * from " +  table_name + " limit " + lb + "," + ub,
				ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

		System.err.println(stmt);
		ResultSet rs = stmt.executeQuery();
		Vector<Vector<String>> types = new Vector<Vector<String>>();
		int nofcolumns = rs.getMetaData().getColumnCount();

		Vector<String> fields = new Vector<String>(); 
		for (int i=1; i <= nofcolumns; i++)
		{
			fields.addElement(rs.getMetaData().getColumnName(i));
		}
		if (PREPEND_COLUMN_NAMES)
		{
			types.addElement(fields);
		} 

		while (rs.next()) // mis je nu de eerste??
		{
			Vector<String> row = new Vector<String>();
			for (int i=1; i <= nofcolumns; i++)
			{
				try
				{
					String s = new String(rs.getBytes(i), "UTF-8");
					row.addElement(s);
				} catch (Exception e)
				{
					row.addElement("NULL");
				}
			}
			types.addElement(row);
		}
		return types;
	}


	public Vector<String> getColumn(Vector<Vector<String>> matrix, int k)
	{
		Vector<String> column = new Vector<String>();
		for (int i=0; i < matrix.size(); i++)
			column.addElement( (matrix.get(i)).get(k));
		return column; 
	}


	public String setValues(String table_name, String key_fieldname, String key_fieldvalue, String field_name, String field_value)
	{
		try
		{
			String q = String.format("update %s set %s = ? where %s=?",table_name, field_name, key_fieldname);
			PreparedStatement stmt = null;
			stmt = this.connection.prepareStatement(q);
			stmt.setBytes(1, field_value.getBytes("UTF-8"));
			stmt.setBytes(2, key_fieldvalue.getBytes("UTF-8"));
			System.err.println(stmt);
			int u = stmt.executeUpdate();
			System.err.println("hihi " + u);
			stmt.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public void runQuery(String sql)
	{
		try
		{
			PreparedStatement stmt = null;
			stmt = this.connection.prepareStatement(sql);
			//System.err.println(stmt);
			boolean u = stmt.execute();
			//System.err.println("run query " + u);
			stmt.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void runQueries(String sql)
	{
		for (String s:sql.split(";"))
		{
			s = s.trim();
			if (s.length() > 0)
				runQuery(s);
		}
	}

	public int nofRows (String table_name) throws Exception
	{
		try
		{
			PreparedStatement stmt = null;
			stmt = this.connection.prepareStatement("select count(*) from " + table_name);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
			{
				int  n = Integer.parseInt(new String(rs.getBytes(1), "UTF-8"));
				return n;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return -1;
	}

	public static class ConnectorSimple
	{
		//static private Logger logger = Logger.getLogger(DatabaseConnectorSimple.class);

		public Connection connect(String url, String user, String password)
		{
			try
			{
				// Register the JDBC driver for MySQL.
				Class.forName("com.mysql.jdbc.Driver");

				// Get a connection to the database
				Connection connection = DriverManager.getConnection(url, user, password);

				// Allow large packets

				//Statement stmt = connection.createStatement();
				//stmt.execute("SET SESSION `max_allowed_packet`= 1000000000;");
				//stmt.close();

				return connection;
			}
			catch (ClassNotFoundException e)
			{
				//logger.error("Database driver niet gevonden", e);
				System.err.println("ramp (DRIVER) !!!!!!!!!!!!!!");
				return null;
			}
			catch (SQLException e)
			{
				//logger.error("Kan geen verbinding met database maken", e);
				e.printStackTrace();
				System.err.println("ramp!!!!!!!!!!!!!!");
				return null;
			}
		}
	}
}
