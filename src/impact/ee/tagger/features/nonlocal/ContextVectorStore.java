package impact.ee.tagger.features.nonlocal;
import impact.ee.tagger.Context;
import impact.ee.tagger.Corpus;
import impact.ee.util.WeightMap;

import java.util.*;

public class ContextVectorStore 
{
	private int contextSize = 0;
	Map<String, ContextVector> contextMap = new HashMap<String, ContextVector>();
	WeightMap<String> globalTermFrequencies = new WeightMap<String>();
	private int nItems = 0;

	public ContextVectorStore(int contextSize)
	{
		this.contextSize = contextSize;
	}

	public void fillContextStore(Corpus c)
	{
		for (Context context: c.enumerate())
		{
			addContext(context);
		}
		for (ContextVector v: contextMap.values())
			setTFIDFWeights(v);
	}
	
	public void addContext(Context c)
	{
		String s = c.getAttributeAt("word", 0);
		ContextVector v = contextMap.get(s);
		globalTermFrequencies.increment(s,1);
		
		if (v == null)
		{
			contextMap.put(s,v = new ContextVector(s));
		}
		
		for (int i=-contextSize; i <= contextSize; i++)
		{
			if (i==0) continue;
			String w = c.getAttributeAt("word", i);
			v.termFrequencies.increment(w,positionWeight(i));
		}
	}

	public double positionWeight(int i)
	{
		return 1;
	}
	/**
	 * idf(d,s) should be length(d) / number of documents countaining s
	 * @param v
	 * @param s
	 */
	public double TFIDFWeight(ContextVector v, String s) // apply tfidf weighting
	{
		double fs = globalTermFrequencies.get(s);
		if (fs == 0)
			return 0;
		double idf = Math.log(globalTermFrequencies.get(v.focusWord) / fs); // nee dit levert niks op, is altijd hetzelfde...
		double tf = v.termFrequencies.get(s) / v.getMaxTermFrequency() ; // globalTermFrequencies.get(v.focusWord);
		return tf * idf;
	}
	
	public void setTFIDFWeights(ContextVector v)
	{
		v.getMaxTermFrequency();
		for (String s: v.termFrequencies.keySet())
		{
			double d = TFIDFWeight(v,s);
			v.termFrequencies.setWeight(s, d);
		}
	}
}
