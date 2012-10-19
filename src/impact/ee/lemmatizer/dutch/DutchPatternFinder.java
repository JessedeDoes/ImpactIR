package impact.ee.lemmatizer.dutch;
import java.util.Set;

import impact.ee.lemmatizer.*;
import impact.ee.lexicon.*;

/**
 * Instead of bothering to work out a something like a "general" word frame model in Wicentowski style,
 * just implement a PatternFinder for Dutch describing "normal" inflexion.
 * <p>
 * Eventually, we will want to store the "pattern" info..<br>
 * And use the "normal" patterns to filter out the non-standard word forms belonging to standardize lemmata
 * (produkt/producten etc)..
 * @author Gebruiker
 *
 */
public class DutchPatternFinder implements PatternFinder 
{
	SimplePatternFinder fallbackFinder = new SimplePatternFinder();
	/**
	 * We should make suffixes dependent on PoS, of course...
	 */
	String[] suffixes =
		{
			"en", "d", "t", "de", "den", "te", "ten", "s", "'s", "st", "er", "ers", "end", "ende", "enden", 
			"e", "ste", "sten", "ere", "eren", "ën", "ina", "um", "a", "", "n"
		};

	String[] lemmaSuffixes =
		{
			"en", "um", ""	
		};

	String[] infixes =
		{
			"ge", ""	
		};

	Set<StemChange> stemChanges = StemChange.getAll();

	@Override
	public Pattern findPattern(String a, String b) //  a is word form, b is lemma in applications
	{
		boolean found = false;
		for (String suffixa: suffixes)
			for (String suffixb: lemmaSuffixes)
			{
				if (a.endsWith(suffixa) && b.endsWith(suffixb))
				{
					String stemA = a.substring(0, a.length() - suffixa.length());
					String stemB = b.substring(0, b.length() - suffixb.length());
					for (String infix: infixes)
					{
						//System.err.println("INFIX" + infix);
						String stemAWithoutInfix = stemA;
						if (!infix.equals("") && !stemA.contains(infix))
							continue;
						if (!infix.equals("") && stemA.contains(infix))
						{
							//System.err.println("HA!" + stemA);
							stemAWithoutInfix = stemA.replace(infix, ""); // is er een replaceOneTime? gevoltigeerd probleem
						}
						//System.err.println(stemAWithoutInfix + " " + stemB);
						for (StemChange change: stemChanges)
						{
							String x = change.transform(stemAWithoutInfix);
							if (x != null && x.equals(stemB))
							{
								found = true;
								System.err.println(stemAWithoutInfix + "-" + suffixa + " -->" + stemB + "-"  + suffixb + " : " + change.type);
							}
						}
					}
				}
			}
		if (!found)
		{
			Pattern s = this.fallbackFinder.findPattern(a, b);
			System.err.println("fallback:" + s);
		}
		return found?new SimplePattern():null;
	}

	public static void main(String[] args)
	{
		DutchPatternFinder p = new DutchPatternFinder();
		p.findPattern("gevoltigeerd", "voltigeren");
		System.exit(0);
		p.findPattern("zaken", "zaakte");
		InMemoryLexicon l = new InMemoryLexicon();
		l.readFromFile("resources/exampledata/type_lemma_pos.tab");
		int explained = 0;
		int unexplained = 0;
		for (WordForm w: l)
		{
			boolean test1 = w.lemmaPoS.equals("NOU") && w.tag.contains("pl");
			boolean test2 = w.lemmaPoS.equals("VRB") && w.tag.contains("impf");
			boolean test3 = w.lemmaPoS.equals("VRB") && w.tag.contains("pres");
			boolean test4 = w.lemmaPoS.equals("VRB") && w.tag.contains("part");
			boolean test5 = w.lemmaPoS.equals("ADJ");
			if (!w.wordform.equalsIgnoreCase(w.lemma) && test4)
			{
				if (p.findPattern(w.wordform, w.lemma) == null)
				{
					unexplained++;
					System.err.println(w);
				} else
				{
					explained++;
				}
			}

		}
		System.err.println("Explained: " + explained  + " unexplained: " + unexplained);
	}
}
