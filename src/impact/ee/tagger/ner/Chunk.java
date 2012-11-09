package impact.ee.tagger.ner;

import impact.ee.tagger.Context;
import impact.ee.util.StringUtils;

import java.util.*;
public class Chunk 
{
	public final static  int MAX_LENGTH=10;
	public int length=0;
	String label="";
	Context context = null;
	List<String> tags = new ArrayList<String>();
	
	public String toString()
	{
		return label + ": " + getText();
	}
	
	public String getText() // pas op werk alleen als je erop staat!
	{
		List<String> tokens = new ArrayList<String>();
		for (int i=0; i < length; i++)
		{
			tokens.add(context.getAttributeAt("word", i));
		}
		return StringUtils.join(tokens, " "); 
	}
}
