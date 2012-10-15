package impact.ee.classifier.features;

import impact.ee.classifier.Feature;

public class SuffixFeature extends Feature
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int k;

	public SuffixFeature(int x)
	{
		k=x;
		name = "s_" + k;
	}
	public String getValue(Object o)
	{
		String s = (String) o;
		if (s.length() >= k)
		{
			return s.substring(s.length()-k);
		}
		return "";
	}
}