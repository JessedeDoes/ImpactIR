package impact.ee.tagger.features;

import impact.ee.classifier.Feature;
import impact.ee.tagger.Context;

class WordsAtFeature extends Feature
{
	int[] k;

	public WordsAtFeature(int[] x)
	{
		k=x;
		String nm = "";
		for (int i=0; i < x.length; i++)
			nm = nm + "_" + x[i];
		name = "words" + nm;
	}

	public String getValue(Object o)
	{
		String val="seq";
		for (int i=0; i < k.length; i++)
		{
			String s = ((Context) o).getAttributeAt("word", k[i]);
			val += "_" + s;
		}
		return val;
	}
}