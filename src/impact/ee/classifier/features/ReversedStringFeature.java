package impact.ee.classifier.features;

import impact.ee.classifier.Feature;

public class  ReversedStringFeature extends Feature
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ReversedStringFeature()
	{
		THRESHOLD=0;
	}

	public String getValue(Object o) // never called???
	{
		//System.err.println(s);
		String s = (String) o;
		return new StringBuffer(s).reverse().toString();
	}
}