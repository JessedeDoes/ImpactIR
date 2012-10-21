package impact.ee.lemmatizer;
import java.util.ArrayList;

/**
 * The main difference between Rule and Pattern is that Rule includes PoS information
 * @author Gebruiker
 *
 */
public class Rule
{
	public Pattern pattern;
	public String lemmaPoS;
	String PoS;
	public int count=0;
	public int id;
	public ArrayList<Example> examples = new ArrayList<Example>();

	public Rule()
	{
		lemmaPoS = "";
		PoS = "";
	}

	public Rule(Pattern pat, String pos, String lpos)
	{
		pattern=pat; PoS = pos; lemmaPoS = lpos;
	}

	public boolean equals(Object o1)
	{
		if (o1 instanceof Rule)
		{
			Rule o = (Rule) o1;
			return (pattern.equals(o.pattern) && lemmaPoS.equals(o.lemmaPoS) && PoS.equals(o.PoS));
		} else
		{
			return false;
		}
	}

	public String toString()
	{
		return pattern + ": " + lemmaPoS + "->" + PoS;
	}

	public int hashCode()
	{
		return pattern.hashCode()+ PoS.hashCode() + lemmaPoS.hashCode();
	}
}
