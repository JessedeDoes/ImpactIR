package impact.ee.classifier;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class StochasticFeature implements Serializable
{
	/**
	 * TODO: ook hier pruning van weinig voorkomende kenmerken
	 */
	private static final long serialVersionUID = 1L;
	public String name;
	Map<Object,Distribution> 
		storedValueHash = new HashMap<Object,Distribution>();
	
	// ooops hier staat niks in!
	
	public void addValue(Object o, Distribution d)
	{
		storedValueHash.put(o, d);
	}
	
	public Distribution getValue(Object o)
	{
		return null;
	}
	
	/*
	 * Distribution-valued features (e.g. conditional distributions)
	 * may need to examine some data before actual training
	 */
	public void examine(Object o) 
	{
		
	}
	
	/*
	 * Override this if you want to remove rare values
	 */
	public void pruneDistribution(Distribution d)
	{
		
	}
}
