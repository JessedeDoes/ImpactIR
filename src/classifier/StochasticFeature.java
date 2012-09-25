package classifier;

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
	
	public void examine(Object o) 
	{
		
	}
}
