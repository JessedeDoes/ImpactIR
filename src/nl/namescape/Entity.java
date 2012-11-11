package nl.namescape;

import java.util.ArrayList;
import java.util.*;

public class Entity 
{
	public String type;
	public String location;
	public String text;
	private List<String> words = new ArrayList<String>();
	public String normalizedForm;
	public int frequency = 0;
	public Nym nym = null;
	List<EntitySimilarity> similarities = new ArrayList<EntitySimilarity>();
	
	public Entity(String name, String typeName) 
	{
		this.text = name;
		this.type = typeName;
		frequency=1;
		words = Arrays.asList(name.split("\\s+"));
	}

	public void addWord(String w)
	{
		words.add(w);
		text = nl.namescape.util.Util.join(words, " ");
	}
	public Entity() 
	{
		// TODO Auto-generated constructor stub
	}

	public String getText()
	{
		return text;
		//return util.Util.join(words, " ");
	}
	
	public String toString()
	{
		return "(" + type + ", " + getText() + ")"; 
	}
	
	public String getLocationKey()
	{
		return location;
	}
	
	public void addSimilarity(EntitySimilarity r)
	{
		similarities.add(r);
	}
}
