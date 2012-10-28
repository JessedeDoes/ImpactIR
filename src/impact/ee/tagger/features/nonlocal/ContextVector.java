package impact.ee.tagger.features.nonlocal;
import impact.ee.tagger.Context;
import impact.ee.util.WeightMap;

import java.util.*;

public class ContextVector 
{
	String focusWord;
	WeightMap<String> termFrequencies = new WeightMap<String>();
	
	public ContextVector(String s)
	{
		focusWord = s;
	}
	public void addContext(Context c)
	{
		
	}
	
	public double getMaxTermFrequency()
	{
		double max=0;
		for (String s: this.termFrequencies.keySet())
		{
			double f = termFrequencies.get(s);
			if (f > max) max = f;
		}
		return max;
	}
}
