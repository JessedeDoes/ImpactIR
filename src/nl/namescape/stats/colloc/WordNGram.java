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
	
	public WordNGram(List<String>  parts)
	{
		for (String s: parts)
		{
			this.parts.add(s);
		}
	}
	
	
	public WordNGram(List<String>  parts, int k)
	{
		for (int i=0; i < k; i++)
		{
			this.parts.add(parts.get(i));
		}
	}
	
	public WordNGram(List<String>  parts, int startAt, int endBefore)
	{
		for (int i=startAt; i < endBefore; i++)
		{
			this.parts.add(parts.get(i));
		}
	}
	
	
	public WordNGram(String s1, String s2)
	{
		this.parts.add(s1);
		this.parts.add(s2);
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
	
	int size()
	{
		return parts.size();
	}
	
	public WordNGram span(int begin, int endBefore)
	{
		List<String> l = new ArrayList<String>();
		for (int i=begin; i < endBefore; i++)
		{
			l.add(parts.get(i));
		}
		return new WordNGram(l);
	}
}
