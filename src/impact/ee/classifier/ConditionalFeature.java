package impact.ee.classifier;

import impact.ee.classifier.Distribution.Outcome;

import java.util.*;

public class ConditionalFeature extends StochasticFeature 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Feature conditionedOn = null;
	Feature conditioned = null;
	public int absoluteThreshold=0;
	
	public ConditionalFeature(Feature conditionedOn, Feature conditioned)
	{
		this.conditioned = conditioned;
		this.conditionedOn = conditionedOn;
		this.name = conditioned.name + "|" + conditionedOn.name;
	}
	
	public boolean applicable(String v1)
	{
		return true;
	}
	  
	public void examine(Object o) 
	{
		String v1 = conditionedOn.getValue(o);
		if (!applicable(v1)) return;
		String v2 = conditioned.getValue(o);
		Distribution d = storedValueHash.get(v1);
		if (d == null)
		{
			storedValueHash.put(v1, d = new Distribution());
		}
		d.incrementCount(v2);
	}
	
	/**
	 * This does not do anything useful for unknown Objects!
	 * Hence the word vectors will not work in this way...
	 */
	public Distribution getValue(Object o)
	{
		String v1 = conditionedOn.getValue(o);
		return storedValueHash.get(v1);
	}
	
	public void pruneDistribution(Distribution d)
	{
		ArrayList<Outcome> newOutcomes = new ArrayList<Outcome>();
		if (absoluteThreshold > 1)
		{
			for (Outcome o: d.outcomes)
			{
				if (o.count < absoluteThreshold)
				{
					o.count=0;
				} else
				{
				   // System.err.println("keep " + o);
				   newOutcomes.add(o);
				}
			}
			d.outcomes = newOutcomes;
		}
		d.computeProbabilities();
	}
}
