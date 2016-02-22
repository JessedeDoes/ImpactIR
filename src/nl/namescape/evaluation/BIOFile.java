package nl.namescape.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import nl.namescape.Entity;
import nl.namescape.util.TabSeparatedFile;


public class BIOFile implements NETaggedDocument
{
	String[] fields = {"word", "tag"};
	List<String[]> lines = new ArrayList<String[]>();
	enum State { other, name };
	Map<String, Entity> entityMap = new HashMap<String,Entity>();
	List<Entity> entityList = new ArrayList<Entity>();
	
	public BIOFile(String fileName) 
	{
		 read(fileName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<Entity> getEntities() 
	{
		// TODO Auto-generated method stub
		return entityList;
	}

	@Override
	public Entity getEntityAt(String locationIdentifier) 
	{
		// TODO Auto-generated method stub
		return entityMap.get(locationIdentifier);
	}
	
	
	
	public void read(String fileName)
	{
		TabSeparatedFile f = new TabSeparatedFile(fileName,fields);
		f.setSeparator("\\s+");
		String[] line = null;
		int entityStart=0;
		Entity currentEntity=null;
		int l=0;
		while ((line  = f.getLine()) != null)
		{
			lines.add(line);
			if (line.length >= 2) try
			{
				//nl.openconvert.log.ConverterLog.defaultLog.println(line[0]);
				String[] tagParts = line[1].split("-");
				if (tagParts[0].equals("B"))
				{
					if (currentEntity != null)
						flush(currentEntity, entityStart, l-1);
					entityStart = l;
					currentEntity  = new Entity();
					currentEntity.addWord(line[0]);
					currentEntity.type = tagParts[1];
				}
				if (tagParts[0].equals("O"))
				{
					if (currentEntity != null)
						flush(currentEntity, entityStart, l-1);
					currentEntity = null;
				}
				if (tagParts[0].equals("I"))
				{
					if (currentEntity == null)
					{
						nl.openconvert.log.ConverterLog.defaultLog.println("Unexpected internal entity at: " + l + " : " + line[0]);
					}
					else
					{
						String type = tagParts[1];
						if (currentEntity.type.equals(type))
							currentEntity.addWord(line[0]);
						else
							nl.openconvert.log.ConverterLog.defaultLog.println("Inconsistent entity types for I " +l + " : "  + line[0]);
					}
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			l++;
		}
	}

	private void flush(Entity currentEntity, int entityStart, int i) 
	{
		// TODO Auto-generated method stub
		currentEntity.location = entityStart + "-" + i;
		entityList.add(currentEntity);
		entityMap.put(currentEntity.location, currentEntity);
	}
}
