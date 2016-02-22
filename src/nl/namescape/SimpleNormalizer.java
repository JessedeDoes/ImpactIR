package nl.namescape;

import java.util.Collection;
import java.util.*;

public class SimpleNormalizer implements EntityNormalizer
{
	Map<String, Map<String, Entity>> entityMap = new HashMap<String, Map<String, Entity>> ();
	
	private  Entity storeEntity(Entity e)
	{
		Map<String, Entity> m = entityMap.get(e.type);
		if (m == null)
		{
			entityMap.put(e.type, m = new HashMap<String,Entity>());
		}
		Entity e1 = m.get(e.getText());
		if (e1 == null)
			m.put(e.getText(), e1 = e);
		return e1;
	}
	
	
	public String simpleNormalization(String n) // weten we zeker dat de interpunctie we is
	{
		n = n.trim();
		n = n.toUpperCase();
		n = n.replaceAll("'S$", "");
		if (n.endsWith("'S")) 
		{
			nl.openconvert.log.ConverterLog.defaultLog.println("CRASH BOEM " + n);
			//System.exit(1);
		}
		return n;
	}
	
	/**
	 * Niet goed; de hash moet op normalized form gaan..
	 */
	@Override
	public void findNormalizedForms(Collection<Entity> entitySet)
	{
		for (Entity e: entitySet)
		{
			storeEntity(e);
		}
		for (String type: entityMap.keySet())
		{
			Map<String, Entity> m = entityMap.get(type);
			Map<String, Entity> m1 = new HashMap<String, Entity>();
			for (Entity e: m.values())
			{
				e.normalizedForm = simpleNormalization(e.getText());
				m1.put(e.normalizedForm, e);
			}
			for (Entity e: m.values())
			{
				String withoutS = e.normalizedForm.replaceAll("[Ss]$", "");
				Entity other = m1.get(withoutS);
				if (other != null)
				{
					e.normalizedForm = withoutS;
				}
			}
		}
	}
}
