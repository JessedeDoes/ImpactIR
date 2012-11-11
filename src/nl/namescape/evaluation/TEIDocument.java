package nl.namescape.evaluation;

import java.util.*;

import nl.namescape.Entity;
import nl.namescape.tei.TEITagClasses;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class TEIDocument implements NETaggedDocument 
{
	Document document=null;
	Map<String,Entity> entityMap = new HashMap<String,Entity>();
	List<Entity> entities = new ArrayList<Entity>();
	private boolean entitiesListed = false;
	
	public TEIDocument(Document d)
	{
		document=d;
		getEntities();
	}
	
	public List<Entity> getEntities()
	{
		if (entitiesListed)
			return entities;
		
		List<Element> nameElements = nl.namescape.tei.TEITagClasses.getNameElements(document);
		for (Element n: nameElements)
		{
			Entity e = getEntity(n);
				
			entityMap.put(e.location, e);
			entities.add(e);
		}
		entitiesListed = true;
		return entities;
	}

	public static Entity getEntity(Element n) 
	{
		List<Element> tokens = TEITagClasses.getTokenElements(n);
		String location = tokens.get(0).getAttribute("id") 
				+ "-" + tokens.get(tokens.size()-1).getAttribute("id");
		
		Entity e = new Entity();
			e.type = n.getAttribute("type");
			e.location = location;
			e.text = n.getTextContent();
		return e;
	}
	
	public Entity getEntityAt(String locationIdentifier)
	{
		return entityMap.get(locationIdentifier);
	}
}
