package impact.ee.tagger.features;

import impact.ee.classifier.Distribution;
import impact.ee.lexicon.ILexicon;
import impact.ee.lexicon.WordForm;
import impact.ee.tagger.Context;

import java.util.Set;


public class HasTagFeature extends LexiconBasedFeature
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int k=0;

	public HasTagFeature(int k)
	{
		this.k = k;
		this.name = "hasTag_" + k;
		initLexicon();
	}
	
	public HasTagFeature(int k, ILexicon lexicon)
	{
		this.lexicon = lexicon;
		this.k = k;
		this.name = "hasTag" + "_" + k;
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
					d.incrementCount(wf.tag);
				}
			}
			d.computeProbabilities();
		}
		return d;
	}
}