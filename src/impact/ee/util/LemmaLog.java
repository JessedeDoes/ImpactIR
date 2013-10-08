package impact.ee.util;
import java.util.*;

public class LemmaLog 
{
	static List<String> log = new ArrayList<String>();
	public static boolean active = false;
	public static void addToLog(String s)
	{
		if (active)
			log.add(s);
	}
	
	public static String getLastLines(int i)
	{
		String r= "";
		int k=0;
		for (int j=log.size()-1; j > 0 && k < i; j--)
		{
			r += ">" + log.get(j) +  "\n";
			k++;
		}
		return r;
	}
}
