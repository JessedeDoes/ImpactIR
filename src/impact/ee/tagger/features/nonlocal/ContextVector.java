package impact.ee.tagger.features.nonlocal;
import impact.ee.classifier.Distribution;
import impact.ee.tagger.Context;
import impact.ee.util.StringUtils;
import impact.ee.util.WeightMap;

import java.util.*;

public class ContextVector 
// look at sspace for this kind of stuff - more feasible with SVD
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
	
	public String toString()
	{
		List<String> z = new ArrayList<String>();
		for (String s: this.termFrequencies.keySet())
		{
			double x = this.termFrequencies.get(s);
			z.add(s + " " + x);
		}
		return this.focusWord + "\t" + StringUtils.join(z, "\t");
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
				double d = termFrequencies.get(s);
				if (d > 0)
					distribution.addOutcome(s, termFrequencies.get(s));
			}
		}
		return distribution;
	}
}
