package impact.ee.classifier;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
/**
 * 
 * TODO: ook hier pruning van weinig voorkomende kenmerken 
 * @author Gebruiker
 *
 */
public class StochasticFeature implements Serializable
{
	
	private static final long serialVersionUID = 1L;
	public String name;
	private boolean existential = false;
	
	Map<Object, Distribution> storedValueHash = new HashMap<Object,Distribution>();
	
	public void addValue(Object o, Distribution d)
	{
		storedValueHash.put(o, d);
	}
	
	public boolean isExistential()
	{
		return existential;
	}
	
	public Distribution getValue(Object o)
	{
		return null;
	}
	
	/**
	 * Distribution-valued features (e.g. conditional distributions)
	 * may need to examine some data before actual training.
	 */
	public void examine(Object o) 
	{
		
	}
	
	/**
	 * Override this if you want to remove infrequent values
	 */
	public void pruneDistribution(Distribution d)
	{
		
	}
}
