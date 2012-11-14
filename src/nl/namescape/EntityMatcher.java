package nl.namescape;

import java.util.Collection;
import java.util.Set;

public interface EntityMatcher 
{
	public Set<Nym> findNyms(Collection<Entity> entitiesFound);	
}
