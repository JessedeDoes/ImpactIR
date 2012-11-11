package nl.namescape.evaluation;

import java.util.List;

import nl.namescape.Entity;

public interface NETaggedDocument 
{
	public List<Entity> getEntities();
	public Entity getEntityAt(String locationIdentifier);
}
