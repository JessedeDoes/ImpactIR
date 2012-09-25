package tagger.features;
import tagger.Context;
import classifier.*;

public class TaggerFeatures
{
	public static class PrefixFeature extends Feature
	{
		int k;

		public PrefixFeature(int x)
		{
			k=x;
			name = "p_" + k;
		}

		public String getValue(Object o)
		{
			String s = ((Context) o).getAttributeAt("word", 0);
			if (s.length() >= k)
			{
				return s.substring(0,k);
			}
			return "";
		}
	}

	static class SuffixFeature extends Feature
	{
		int k;

		public SuffixFeature(int x)
		{
			k=x;
			name = "s_" + k;
		}

		public String getValue(Object o)
		{
			String s = ((Context) o).getAttributeAt("word", 0);
			if (s.length() >= k)
			{
				return s.substring(s.length()-k);
			}
			return "";
		}
	}
	
	static class WordAtFeature extends Feature
	{
		int k;

		public WordAtFeature(int x)
		{
			k=x;
			name = "word_" + k;
		}

		public String getValue(Object o)
		{
			String s = ((Context) o).getAttributeAt("word", k);
			return s;
		}
	}
	
	static class TagAtFeature extends Feature
	{
		int k;

		public TagAtFeature(int x)
		{
			k=x;
			name = "tag_" + k;
		}

		public String getValue(Object o)
		{
			String s = ((Context) o).getAttributeAt("tag", k);
			return s;
		}
	}
	static class PoSAtFeature extends Feature
	{
		int k;

		public PoSAtFeature(int x)
		{
			k=x;
			name = "pos_" + k;
		}

		public String getValue(Object o)
		{
			String s = ((Context) o).getAttributeAt("tag", k);
			s = extractPoS(s);
			return s;
		}
	}

	
	public static String extractPoS(String tag)
	{
		if (tag == null) return null;
		return tag.replaceAll("\\\u0028.*", "");
	}
	
	static class CapitalFirstFeature extends Feature
	{
		int k;

		public CapitalFirstFeature(int x)
		{
			k=x;
			name = "capitalFirst_" + k;
		}

		public String getValue(Object o)
		{
			String s = ((Context) o).getAttributeAt("word", k);
			if (s != null)
				return new Boolean(s.matches("^[A-Z][a-z]*$")).toString();
			return "false";
		}
	}
	
	static class AllCapitalFeature extends Feature
	{
		int k;

		public AllCapitalFeature(int x)
		{
			k=x;
			name = "AllCapital_" + k;
		}

		public String getValue(Object o)
		{
			String s = ((Context) o).getAttributeAt("word", k);
			if (s != null)
				return new Boolean(s.matches("^[A-Z][A-Z]*$")).toString();
			return "false";
		}
	}
	
	static class InitialFeature extends Feature
	{
		int k;

		public InitialFeature(int x)
		{
			k=x;
			name = "initial_" + k;
		}

		public String getValue(Object o)
		{
			String s = ((Context) o).getAttributeAt("word", k);
			if (s != null)
			{
				boolean b = s.matches("^[A-Z][a-z]\\.?$") || s.matches("[A-Z][a-z]?\\.[A-Z][a-z]\\.?");
				return new Boolean(s.matches("^[A-Z][A-Z]*$")).toString();
			} 
			return "false";
		}
	}
	
	static class SentenceInternalCapitalFeature extends Feature
	{
		int k;

		public SentenceInternalCapitalFeature(int x)
		{
			k=x;
			name = "swimmingCapital_" + k;
		}

		public String getValue(Object o)
		{
			String s = ((Context) o).getAttributeAt("word", k);
			if (s != null)
			{
			boolean b = s.matches("^[A-Z][a-z]*$");
			if (b)
			{
				String previous = ((Context) o).getAttributeAt("tag", k-1);
				boolean b1 =  previous.matches("#");
				return new Boolean(!b1).toString();
			} else
				return "false";
			}
			return "false";
		}
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
	
	public static FeatureSet getMoreFeatures(boolean useFeedback)
	{
		FeatureSet f = getBasicFeatures(useFeedback);
		f.addStochasticFeature(getPoSDistributionAt(1));
		f.addStochasticFeature(getPoSDistributionAt(-1));
		//f.addStochasticFeature(getPoSDistributionAt(2));
		//f.addStochasticFeature(getPoSDistributionAt(-2));
		return f;
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
		for (int i=-1; i <=1 ; i++)
		{
			fs.addFeature(new CapitalFirstFeature(i));
			
			//fs.addFeature(new InitialFeature(i));
		}
		if (useFeedback)
		{
			fs.addFeature(new TagAtFeature(-1));
			fs.addFeature(new TagAtFeature(-2));
		}
		fs.addFeature(new AllCapitalFeature(0));
		// fs.addFeature(new SwimmingCapitalFeature(0));
		return fs;
	}
}
