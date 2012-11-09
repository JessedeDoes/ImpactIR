package impact.ee.classifier.features;

import impact.ee.classifier.Feature;

public class PrefixFeature extends Feature
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int k;

	public PrefixFeature(int x)
	{
		k=x;
		name = "p_" + k;
	}

	public String getValue(Object o)
	{
		String s = (String) o;
		if (s.length() >= k)
		{
			return s.substring(0,k);
		}
		return "";
	}
}