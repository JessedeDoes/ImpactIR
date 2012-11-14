package impact.ee.tagger.features.nonlocal;

import impact.ee.classifier.Distribution;
import impact.ee.classifier.StochasticFeature;
import impact.ee.tagger.ner.Chunk;

/*
 * This one does the context distribution for chunks?
 */
public class ContextDistributionFeature extends StochasticFeature 
{
	private static final long serialVersionUID = 1L;
	ContextVectorStore store;
	
	public ContextDistributionFeature(ContextVectorStore store)
	{
		this.store = store;
		this.name = "contextVector";
	}
	
	public Distribution getValue(Object o)
	{
		try
		{
			Chunk c = (Chunk) o;
			return store.getContextDistribution(c.getText());
		} catch (Exception e)
		{
			
		}
		return null;
	}
}
