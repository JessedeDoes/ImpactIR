package nl.namescape.stats;
import java.util.*;

public class WordNGram 
{
	List<String> parts = new ArrayList<String>();
	public WordNGram(String[] parts)
	{
		for (String s: parts)
		{
			this.parts.add(s);
		}
	}
}
