package impact.ee.lemmatizer.dutch;

import impact.ee.lemmatizer.dutch.StemChange.RegularStemChange;

public class DutchPattern 
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
}
