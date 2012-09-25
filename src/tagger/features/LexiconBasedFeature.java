package tagger.features;
import lexicon.*;
import classifier.*;

import java.io.ObjectInputStream;
import java.util.*;


import tagger.Context;

public class LexiconBasedFeature extends StochasticFeature
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	transient ILexicon lexicon;
	String databaseHost = "svowim02";
	String database = "EE3_5";
	
	public void initLexicon()
	{
		lexicon = new LexiconDatabase(databaseHost, database);
	}

	private void readObject(ObjectInputStream in) throws java.io.IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		initLexicon();
	}
	
	public static class HasPoSFeature extends LexiconBasedFeature
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
}
