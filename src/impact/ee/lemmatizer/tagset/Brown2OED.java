package impact.ee.lemmatizer.tagset;

import java.io.BufferedReader;

import impact.ee.util.Resource;
import java.util.*;

/**
 * t1 is brown tag, t2 is oed tag
 */
public class Brown2OED implements TagRelation 
{
	Set<String> compatible = new HashSet<String>();

	public Brown2OED()
	{
		try
		{
			BufferedReader r = new BufferedReader(Resource.openResourceFile("tagsets/brown_oed.txt"));
			String l;
			while ((l = r.readLine()) != null)
			{
				String[] fields = l.split("\t");
				String brownTags = fields[0].trim().toLowerCase();
				String oedTags = fields[1].trim().toLowerCase();
				for (String oedTag: oedTags.split("\\|"))
				{
					//System.err.println(brownTag + "~" + oedTag);
					for (String brownTag: brownTags.split("\\|"))
					{
						//System.err.println(brownTag + "~" + oedTag);
						compatible.add(brownTag + "~" + oedTag);
					}
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public boolean compatible(Tag t1, Tag t2)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	/**
	 * t1 is brown tag, t2 is oed tag
	 */
	public boolean corpusTagCompatibleWithLexiconTag(String t1, String t2, boolean allowConversion) 
	{
		// TODO Auto-generated method stub
		return compatible.contains(t1.trim().toLowerCase() + "~" + t2.trim().toLowerCase());
	}

	public static void main(String[] args)
	{
		new Brown2OED();
	}
}
