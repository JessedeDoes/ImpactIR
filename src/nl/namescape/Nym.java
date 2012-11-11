package nl.namescape;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Nym 
{
	public String type;
	public String nymForm;
	public Collection<Entity> instances = new HashSet<Entity>();
	public String id;
	
	public boolean equals(Object other)
	{
		try
		{
			Nym n = (Nym) other;
			return (n.type.equals(type) && n.nymForm.equals(nymForm));
		} catch (Exception e)
		{
			return false;
		}
	}
}
