package impact.ee.tagger.features;
import impact.ee.classifier.*;

public class TaggerFeatures
{
	public static String extractPoS(String tag)
	{
		if (tag == null) return null;
		return tag.replaceAll("\\\u0028.*", "");
	}

	// doe dit alleen voor uppercase tokens?
	
	public static StochasticFeature getPreviousWordDistributionAt(int k)
	{
		ConditionalFeature f1 = new ConditionalFeature(new WordAtFeature(k), 
				new WordAtFeature(k-1)) 
		{
			public boolean applicable(String v1)
			{
				return v1.matches("^[A-Z]");
			}
		};
		f1.absoluteThreshold=3;
		return f1;
	}
	
	public static StochasticFeature getTagDistributionAt(int k)
	{
		StochasticFeature f1 = new ConditionalFeature(new WordAtFeature(k), new TagAtFeature(k));
		return f1;
	}

	public static StochasticFeature getPoSDistributionAt(int k)
	{
		StochasticFeature f1 = new ConditionalFeature(new WordAtFeature(k), new PoSAtFeature(k));
		return f1;
	}

	/**
	 * Het gedoe met de context vectoren moet anders.
	 * Verdeel de features in initiele groepjes.
	 * Die kunnen dan instuderen op diverse corpora
	 * (al dan niet met bepaalde tagging)
	 * Vervolgens alles in een groep zetten 
	 * en aan de classifier voeren.
	 * @param useFeedback
	 * @return
	 */
	public static FeatureSet getMoreFeatures(boolean useFeedback)
	{
		FeatureSet f = getBasicNERFeatures(useFeedback);
		f.addStochasticFeature(getPoSDistributionAt(1));
		f.addStochasticFeature(getPoSDistributionAt(-1));
		// f.addStochasticFeature(getPreviousWordDistributionAt(0)); // silly... only makes sense if pruned...
		//f.addStochasticFeature(getPoSDistributionAt(2));
		//f.addStochasticFeature(getPoSDistributionAt(-2));ls -
		return f;
	}

	public static FeatureSet getBasicNERFeatures(boolean useFeedback)
	{
		FeatureSet fs = new FeatureSet();
		for (int i=1; i < 10; i++)
		{
			fs.addFeature(new SuffixFeature(i));
			fs.addFeature(new PrefixFeature(i));
		}
		
		for (int i=-2; i <= 2; i++)
		{
			fs.addFeature(new WordAtFeature(i));
		}
		
		int[] p01 = {0,1};
		int[] p12 = {1,2};
		int[] p_10 = {-1,0};
		
		fs.addFeature(new WordsAtFeature(p01));
		fs.addFeature(new WordsAtFeature(p12));
		fs.addFeature(new WordsAtFeature(p_10));
		
		fs.addFeature(new 
				PhraseShapeFeature(WordShapeClassifier.WORDSHAPECHRIS1, -2, 2));
		
		for (int i=-1; i <=1 ; i++)
		{
			fs.addFeature(new CapitalFirstFeature(i));
			fs.addFeature(new SentenceInternalCapitalFeature(i));
			fs.addFeature(new ClusterFeature(ClusterFeature.SandersClusterFile,4,i));
			fs.addFeature(new ClusterFeature(ClusterFeature.SandersClusterFile,8,i));
			//fs.addFeature(new InitialFeature(i));
		}
		if (useFeedback)
		{
			fs.addFeature(new TagAtFeature(-1));
			fs.addFeature(new TagAtFeature(-2));
		}
		fs.addFeature(new AllCapitalFeature(0));
		fs.addFeature(new SentenceInternalCapitalFeature(0));
		fs.addStochasticFeature(new CaseProfileFeature(CaseProfileFeature.profilesFromCorpusSanders));
		//fs.addFeature(new SentenceInitialFeature(0));
		//fs.addFeature(new SentenceInitialFeature(-1));
		return fs;
	}
	
	public static FeatureSet getBasicFeatures(boolean useFeedback)
	{
		FeatureSet fs = new FeatureSet();
		for (int i=1; i < 10; i++)
		{
			fs.addFeature(new SuffixFeature(i));
			fs.addFeature(new PrefixFeature(i));
		}
		for (int i=-2; i <= 2; i++)
		{
			fs.addFeature(new WordAtFeature(i));
		}
		
		int[] p01 = {0,1};
		int[] p12 = {1,2};
		int[] p_10 = {-1,0};
		
		fs.addFeature(new WordsAtFeature(p01));
		fs.addFeature(new WordsAtFeature(p12));
		fs.addFeature(new WordsAtFeature(p_10));
		
		//fs.addFeature(new PhraseShapeFeature(WordShapeClassifier.WORDSHAPECHRIS1, -2, 2));
		
		for (int i=-1; i <=1 ; i++)
		{
			fs.addFeature(new CapitalFirstFeature(i));
			fs.addFeature(new SentenceInternalCapitalFeature(i));
			// fs.addFeature(new InitialFeature(i));
		}
		
		if (useFeedback)
		{
			fs.addFeature(new TagAtFeature(-1));
			fs.addFeature(new TagAtFeature(-2));
		}
		
		fs.addFeature(new AllCapitalFeature(0));
		fs.addFeature(new SentenceInternalCapitalFeature(0));
		return fs;
	}

	public static FeatureSet getUnigramFeatures()
	{
		FeatureSet fs = new FeatureSet();
		for (int i=1; i < 10; i++)
		{
			fs.addFeature(new SuffixFeature(i));
			fs.addFeature(new PrefixFeature(i));
		}

		fs.addFeature(new WordAtFeature(0));


		fs.addFeature(new CapitalFirstFeature(0));



		fs.addFeature(new AllCapitalFeature(0));
		// fs.addFeature(new SwimmingCapitalFeature(0));
		return fs;
	}
}
