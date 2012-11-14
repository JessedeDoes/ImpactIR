package nl.namescape;

public class EntitySimilarity
{
	Entity e1;
	Entity e2;
	String type;
	double score=0;
	
	public EntitySimilarity(String type, Entity e1, Entity e2)
	{
		this.type=type;
		this.e1=e1;
		this.e2=e2;
	}
}