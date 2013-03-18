package nl.namescape.nelexicon.database;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.lang.reflect.*;

/**
 * This class handles mapping of one java class to a database table
 * 
 * Primary key handling?
 * @author does
 *
 */

public class ObjectRelationalMapping
{
	String tableName;
	Class javaClass;
	Field[] allFields;
	Map<String,Field> db2objectMap = new HashMap<String,Field>();
	Map<Field, String> object2dbMap = new HashMap<Field, String>();
	Field primaryKeyField = null;
	String primaryKeyDbField = null;
	
	List<ForeignKeyBinding> foreignKeyBindings = new ArrayList<ForeignKeyBinding>();
	
	public static class ForeignKeyBinding
	{
		Class javaClass;
		String tableName;
		Field foreignKeyField;
		
		public void insertObject(Object object, Object foreign)
		{
			try
			{
				if  (Collection.class.isAssignableFrom(foreignKeyField.getType()))
				{
					Object currentValue = foreignKeyField.get(object);
					if (currentValue == null)
					{
						currentValue = foreignKeyField.getType().newInstance();
					}
					Collection c = (Collection) currentValue;
					c.add(foreign);
				} else
				{
					foreignKeyField.set(object, foreign);
				}
			} catch (Exception e)
			{
				
			}
		}
	}
	
	public void addForeignKeyBinding(Class javaClass, String tableName, String keyFieldName)
	{
		try
		{
			ForeignKeyBinding fkb = new ForeignKeyBinding();
			fkb.javaClass = javaClass;
			fkb.tableName = tableName;
			fkb.foreignKeyField = this.getClass().getField(keyFieldName);
			foreignKeyBindings.add(fkb);
		} catch (Exception e)
		{

		}
	}
	
	// generated key field should not be included in inserts, but should be when retrieving

	public List<String> getPrimaryKeys(Connection connection, String tableName)
	{
		java.util.List<String> list = new java.util.ArrayList<String>();
		try
		{
			DatabaseMetaData meta = connection.getMetaData();
			ResultSet rs = meta.getPrimaryKeys(null, null, "survey");
			while (rs.next()) 
			{
				String columnName = rs.getString("COLUMN_NAME");
				list.add(columnName);
				System.err.println("getPrimaryKeys(): columnName=" + columnName);
			}
			rs.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return list;
	}
	
	public ObjectRelationalMapping(Class javaClass, String tableName)
	{
		this.tableName = tableName;
		this.javaClass = javaClass;
		this.allFields = javaClass.getFields();
	}

	public void addField(String dbField, String javaField)
	{
		try
		{
			Field f  = javaClass.getField(javaField);
			if (f != null)
			{
				db2objectMap.put(dbField,f);
				object2dbMap.put(f, javaField);
			}
		} catch (Exception e)
		{

		}
	}

	public void setPrimaryKeyField(String fieldName)
	{
		try
		{
			Field f  = javaClass.getField(fieldName);
			this.primaryKeyField = f;
		} catch (Exception e)
		{
			System.err.println(javaClass.getName() + " does not have a field "  + fieldName);
			e.printStackTrace();
		}
	}

	public void insertObject(Connection connection, 
			String tableName, Object object)
	{
		String query = "insert into " + tableName  + " (";
		Set<String> dbFields = db2objectMap.keySet();
		List<String> fieldList = new ArrayList<String>();
		for (String s: dbFields)
		{
			fieldList.add(s);
		}

		int k=0;
		for (String s: fieldList)
		{
			query += (k > 0?", ":"") + s; 
			k++;
		}
		query += ") VALUES (";
		for (int i=0; i < fieldList.size(); i++)
		{
			query += ((i>0)?", ":"") + "?";
		}
		query += ")";
		System.err.println(query);
		try
		{
			PreparedStatement pStmnt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			int i=1;
			for (String f: fieldList)
			{
				Field javaField = db2objectMap.get(f);
				Object o = javaField.get(object);
				pStmnt.setObject(i, o);
				i++;
			}
			pStmnt.execute();
			ResultSet rs = pStmnt.getGeneratedKeys();
			while (rs.next())
			{
				int primaryKey = rs.getInt(1);
				if (this.primaryKeyField != null)
					primaryKeyField.set(object, new Integer(primaryKey));
			}
			rs.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void insertObjectsInPortions(Connection connection, 
			String tableName, Collection<Object> objects, int portionSize)
	{
		List<Object> portion = new ArrayList<Object>();
		int k=0;
		for (Object object: objects)
		{
			if (k > 0 && k % portionSize ==0)
			{
				insertObjects(connection, tableName, portion);
				portion.clear();
			}
			portion.add(object);
			k++;
		}
		if (portion.size() > 0)
			insertObjects(connection, tableName, portion);
	}

	public void insertObjects(Connection connection, 
			String tableName, Collection<Object> objects)
	{
		if (objects == null || objects.size() == 0)
			return;

		StringBuffer query = new StringBuffer("insert into " + tableName  + " (");
		Set<String> dbFields = db2objectMap.keySet();
		List<String> fieldList = new ArrayList<String>();
		List<Object> objectList = new ArrayList<Object>();

		for (String s: dbFields)
		{
			fieldList.add(s);
		}

		int k=0;
		for (String s: fieldList)
		{
			query.append((k > 0?", ":"") + s); 
			k++;
		}
		query.append(") VALUES ");

		for (int j=0; j < objects.size(); j++)
		{
			//System.err.print(".");
			query.append("(");
			for (int i=0; i < fieldList.size(); i++)
			{
				query.append(((i>0)?", ":"") + "?");
			}
			query.append( ")");
			if (j < objects.size() - 1)
				query.append( ", ");
		}

		//System.err.println(".");
		//System.err.println(query);

		try
		{
			PreparedStatement statement = connection.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS);
			int i=1;
			for (Object object: objects)
			{
				for (String f: fieldList)
				{
					Field javaField = db2objectMap.get(f);
					Object o = javaField.get(object);
					statement.setObject(i, o);
					i++;
				}
				objectList.add(object);
			}
			statement.executeUpdate();
			ResultSet rs = statement.getGeneratedKeys();
			int objectNr=0;
			while (rs.next())
			{
				int primaryKey = rs.getInt(1);
				if (this.primaryKeyField != null)
					primaryKeyField.set(objectList.get(objectNr), 
							new Integer(primaryKey));
				objectNr++;
			}
			rs.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}


	public List<Object> fetchObjects(Connection connection)
	{
		List<String> primaryKeys = getPrimaryKeys(connection, this.tableName);
		if (primaryKeys.size() > 0)
		{
			primaryKeyDbField = primaryKeys.get(0);
		}
		String query = "select * from " + this.tableName;
		
		return fetchObjects(connection, query);
	}
	
	// fetching objects from the DB

	public List<Object> fetchObjects(Connection connection, String query)
	{
		List<Object> fetchedObjects = new ArrayList<Object>();
		try
		{
			PreparedStatement stmt = connection.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = stmt.executeQuery();
			Object fetched;
			while ((fetched = getNextAsObject(rs)) != null)
			{
				fetchedObjects.add(fetched);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return fetchedObjects;
	}


	public Object getNextAsObject(ResultSet rs)
	{
		try
		{
			if (rs.next())
			{
				Object o = javaClass.newInstance();
				//System.err.println("meta = " + rs.getMetaData().getColumnCount());
				for (int i=1; i < rs.getMetaData().getColumnCount(); i++)
				{
					String dbFieldName = rs.getMetaData().getColumnName(i);
					//System.err.println(i);
					Field javaField = db2objectMap.get(dbFieldName);
					if (javaField != null)
					{
						javaField.set(o, rs.getObject(i));
					} else if (dbFieldName.equals(this.primaryKeyDbField) && this.primaryKeyField != null)
					{
						this.primaryKeyField.set(o, rs.getObject(i));
					}
				}
				return o;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public void addFields(String[][] lemmaMappingData) 
	{
		// TODO Auto-generated method stub
		for (int i=0; i < lemmaMappingData.length; i++)
		{
			String[] a = lemmaMappingData[i];
			if (i==0 && a.length == 1)
			{
				this.setPrimaryKeyField(a[0]);
			} else if (a.length == 2)
			{
				this.addField(a[0], a[1]);
			}
		}
	}
}
