package nl.namescape.stats.colloc;
import impact.ee.util.StringUtils;

import java.util.*;

public class WordNGram 
{
	List<String> parts = new ArrayList<String>();
	double score=0;
	
	public WordNGram(String[] parts)
	{
		for (String s: parts)
		{
			this.parts.add(s);
		}
	}
	
	public String toString()
	{
		return "(" + StringUtils.join(parts, ", ") + ")";
	}
	
	public boolean equals(Object other)
	{
		WordNGram o = (WordNGram) other;
		if (o.parts.size() != parts.size())
			return false;
		for (int i=0; i < parts.size(); i++)
		{
			if (!this.parts.get(i).equals(o.parts.get(i)))
				return false;
		}
		return true;
	}
	@Override
	public int hashCode()
	{
		return parts.hashCode();
	}
}
