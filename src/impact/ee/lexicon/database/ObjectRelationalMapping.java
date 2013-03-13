package impact.ee.lexicon.database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.lang.reflect.*;

/**
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
			PreparedStatement pStmnt = connection.prepareStatement(query);
			int i=1;
			for (String f: fieldList)
			{
				Field javaField = db2objectMap.get(f);
				Object o = javaField.get(object);
				pStmnt.setObject(i, o);
				i++;
			}
			pStmnt.execute();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}


	public void insertObjects(Connection connection, 
			String tableName, Collection<Object> objects)
	{
		String query = "insert into " + tableName  + " (";
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
			query += (k > 0?", ":"") + s; 
			k++;
		}
		query += ") VALUES ";

		for (int j=0; j < objects.size(); j++)
		{
			query += "(";
			for (int i=0; i < fieldList.size(); i++)
			{
				query += ((i>0)?", ":"") + "?";
			}
			query += ")";
			if (j < objects.size() - 1)
				query += ", ";
		}

		System.err.println(query);
		
		try
		{
			PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
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
}
