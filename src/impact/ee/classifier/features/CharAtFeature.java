package impact.ee.classifier.features;

import impact.ee.classifier.Feature;

public class CharAtFeature extends Feature
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int k;

	public CharAtFeature(int x)
	{
		k=x;
		name = "charAt_" + k;
	}
	
	public String getValue(Object o)
	{
		String s = (String) o;
		if (k >= 0)
		{
			if (s.length() > k)
			{
				return s.charAt(k)+"";
			}
		} else
		{
			if (s.length() > -k)
			{
				return s.charAt(s.length()+k)+""; 
			}
		}
		return "";
	}
}