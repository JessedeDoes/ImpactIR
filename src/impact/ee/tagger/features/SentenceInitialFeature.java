package impact.ee.tagger.features;

import impact.ee.classifier.Feature;
import impact.ee.tagger.Context;

/**
 * Does not appear to contribute anything!
 */
class SentenceInitialFeature extends Feature
{
	
	private static final long serialVersionUID = 1L;
	int k;

	public SentenceInitialFeature(int x)
	{
		k=x;
		name = "sentenceInitial_" + k;
	}

	public String getValue(Object o)
	{
		String s = ((Context) o).getAttributeAt("word", k);
		if (s != null)
		{
			
				String previous = ((Context) o).getAttributeAt("word", k-1);
				boolean b1 =  previous.matches(".*[!?\\.].*");
				//nl.openconvert.log.ConverterLog.defaultLog.println(previous + " " + b1);
				return new Boolean(b1).toString();
			
		}
		return "false";
	}
}