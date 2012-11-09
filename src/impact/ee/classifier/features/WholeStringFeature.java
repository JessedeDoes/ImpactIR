package impact.ee.classifier.features;

import impact.ee.classifier.Feature;

public class  WholeStringFeature extends Feature
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public WholeStringFeature()
	{
		THRESHOLD=0;
	}

	public String getValue(Object o) // never called???
	{
		String s = (String) o;
		//System.err.println(s);
		return s;
	}
}