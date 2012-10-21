package impact.ee.lemmatizer.dutch;

import impact.ee.lemmatizer.Pattern;
import impact.ee.lemmatizer.dutch.StemChange.RegularStemChange;

public class DutchPattern implements Pattern
{
	StemChange.RegularStemChange stemChange;
	String infix="";
	String inflectionSuffix;
	String lemmaSuffix;
	
	public DutchPattern(String suffixa, String suffixb, RegularStemChange type) 
	{
		lemmaSuffix = suffixb;
		inflectionSuffix = suffixa;
		stemChange = type;
	}
	
	public String toString()
	{
		return infix + "-" + stemChange + "-[" + inflectionSuffix + "," + lemmaSuffix + "]";
	}

	@Override
	public String apply(String s)  // trouble: which possibility for infix removal?
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String applyConverse(String s) 
	{
		// TODO Auto-generated method stub
		return null;
	}
}
