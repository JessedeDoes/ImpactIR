package impact.ee.tagger.features.nonlocal;
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
	
	public void setWeights(ContextVector v)
	{
		
	}
}
