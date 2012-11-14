package impact.ee.tagger.features;

import impact.ee.classifier.Distribution;
import impact.ee.lexicon.ILexicon;
import impact.ee.lexicon.WordForm;
import impact.ee.tagger.Context;

import java.util.Set;


public class HasPoSFeature extends LexiconBasedFeature
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int k=0;

	public HasPoSFeature(int k)
	{
		this.k = k;
		this.name = "hasPoS_" + k;
		initLexicon();
	}
	
	public HasPoSFeature(int k, ILexicon lexicon)
	{
		this.lexicon = lexicon;
		this.k = k;
		this.name = "hasPoS_" + k;
	}
	
	@Override
	public Distribution getValue(Object o)
	{
		Distribution d = new Distribution();
		Context c = (Context) o;
		String w = c.getAttributeAt("word", k);
		if (w != null)
		{
			Set<WordForm> s = lexicon.findLemmata(w);
			if (s != null)
			{
				for (WordForm wf: s)
				{
					d.incrementCount(wf.lemmaPoS);
				}
			}
			d.computeProbabilities();
		}
		return d;
	}
}