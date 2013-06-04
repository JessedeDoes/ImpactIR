package nl.namescape;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import java.util.*;

import nl.namescape.tokenizer.SimpleTokenizer;
public class SimpleMatcher implements EntityMatcher
{
	SimpleTokenizer tokenizer = new SimpleTokenizer();
	Set<Nym> nymSet = new HashSet<Nym>();
	
	Map<String, Nym> nymMap = new HashMap<String, Nym>();
	
	private int MAX_DEPTH=10;
	@Override
	
	public Set<Nym> findNyms(Collection<Entity> entitiesFound) 
	{
		// determine possible similarities
		for (Entity e1: entitiesFound)
			for (Entity e2: entitiesFound)
			{
				if (e1 != e2)
				{
					EntitySimilarity s = findSimilarity(e1,e2);
					if (s != null)
					{
						e1.addSimilarity(s);
					}
				}
			}
		// now create Nyms for the entities that are not related to any other
		for (Entity e: entitiesFound)
		{
			if (e.similarities.size() == 0)
			{
				createNym(e);
			}
		}
		// 
		for (Entity e: entitiesFound)
		{
			if (e.nym == null)
			{
				Entity e1 = findDominatingEntity(e,e,0);
				if (e1 == null)
				{ 
					createNym(e);
				} else
				{
					e.nym = e1.nym;
					e.nym.instances.add(e);
				}
			}
		}
		return nymSet;
	}

	public void createNym(Entity e)
	{
		String key = e.type + ":" + e.normalizedForm;
		Nym n = nymMap.get(key);
		if (n == null)
		{
			n = new Nym();
			n.type = e.type;
			n.nymForm = e.normalizedForm;
			this.nymSet.add(n);
			n.id = "nym." + e.type + "." +  nymSet.size(); 
			nymMap.put(key, n);
		}
		n.instances.add(e);
		e.nym = n;
	}
	
	// Beware of possible cycles...
	
	private Entity findDominatingEntity(Entity e0, Entity e, int depth)
	{
		if (e.similarities.size() == 0)
		{
			return e;
		}
		else
		{
			Entity e2 = e.similarities.get(0).e2;
			if (e2 == e0 || depth > MAX_DEPTH)
			{
				System.err.println("possible cycle!!.... " + depth + " " + e0);
				return null;
			} else return (findDominatingEntity(e0, e2, depth+1));
		}
	}
	
	private EntitySimilarity findSimilarity(Entity e1, Entity e2)
	{
		// TODO Auto-generated method stub
		String n1 = e1.normalizedForm;
		String n2 = e2.normalizedForm;
		if (n1 == null || n2 == null)
			return null; // maar dit zou niet moeten kunnen....
		if (n1.equalsIgnoreCase(n2))
		{
			return null;
		}
		String[] n1Parts = n1.split("\\s+");
		String[] n2Parts = n2.split("\\s+");
		
		if (n2.startsWith(n1 + " "))
		{
			EntitySimilarity s = new EntitySimilarity("prefixOf", e1, e2);
			return s;
		}
		if (n2.endsWith(" " + n1))
		{
			EntitySimilarity s = new EntitySimilarity("suffixOf", e1, e2);
			return s;
		}
		
		if (n1Parts.length > 1 && (n1Parts.length == n2Parts.length))
		{
			boolean abbreviation = true;
			for (int i=0; i < n1Parts.length; i++)
			{
				String p1 = n1Parts[i];
				
				tokenizer.tokenize(p1);
				p1 = tokenizer.trimmedToken;
				if (p1.length() > 2)
				{
					abbreviation = false;
					continue;
				}
				if (!n2Parts[i].startsWith(p1))
				{
					abbreviation = false;
					break;
				}
			}
			if (abbreviation)
			{
				EntitySimilarity s = new EntitySimilarity("abbreviationOf", e1, e2);
				return s;
			}
		}
		return null;
	}
}
