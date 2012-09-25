package classifier;

import java.util.*;
public class ConditionalFeature extends StochasticFeature 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Feature conditionedOn = null;
	Feature conditioned = null;
	
	
	public ConditionalFeature(Feature conditionedOn, Feature conditioned)
	{
		this.conditioned = conditioned;
		this.conditionedOn = conditionedOn;
		this.name = conditioned.name + "|" + conditionedOn.name;
	}
	
	public void examine(Object o) 
	{
		String v1 = conditionedOn.getValue(o);
		String v2 = conditioned.getValue(o);
		Distribution d = storedValueHash.get(v1);
		if (d == null)
		{
			storedValueHash.put(v1, d = new Distribution());
		}
		d.incrementCount(v2);
	}
	
	public Distribution getValue(Object o)
	{
		String v1 = conditionedOn.getValue(o);
		return storedValueHash.get(v1);
	}
}
