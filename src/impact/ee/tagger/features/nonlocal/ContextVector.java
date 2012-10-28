package impact.ee.tagger.features.nonlocal;
import impact.ee.classifier.Distribution;
import impact.ee.tagger.Context;
import impact.ee.util.WeightMap;

import java.util.*;

public class ContextVector 
{
	String focusWord;
	WeightMap<String> termFrequencies = new WeightMap<String>();
	double maxTermFrequency = -1;
	private Distribution distribution = null;

	public ContextVector(String s)
	{
		focusWord = s;
	}
	
	public void addContext(Context c)
	{
		
	}
	
	public double getMaxTermFrequency()
	{
		if (maxTermFrequency >= 0)
			return maxTermFrequency;
		double max=0;
		for (String s: this.termFrequencies.keySet())
		{
			double f = termFrequencies.get(s);
			if (f > max) max = f;
		}
		return maxTermFrequency= max;
	}
	
	public Distribution getDistribution() // this is rather awful
	{
		if (distribution == null)
		{
			distribution = new Distribution();
			for (String s: termFrequencies.keySet())
			{
				distribution.addOutcome(s, termFrequencies.get(s));
			}
		}
		return distribution;
	}
}
