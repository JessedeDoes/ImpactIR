package impact.ee.lemmatizer.dutch;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import impact.ee.lemmatizer.*;
import impact.ee.lemmatizer.dutch.StemChange.RegularStemChange;
import impact.ee.lexicon.*;
import impact.ee.util.StringUtils;

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
	
	boolean useFallback =  true;
	
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
			"ge"	
	};

	Set<StemChange> stemChanges = StemChange.getAll();

	public Pattern findPattern(String a, String b)
	{
		return null;
	}
	@Override
	public Pattern findPattern(String a, String b, String PoS) //  a is word form, b is lemma in applications
	{
		boolean found = false;
		Comparator<DutchPattern> comparator = new Comparator<DutchPattern>() 
			     { public int compare(DutchPattern a, DutchPattern b) { return b.inflectionSuffix.length() - a.inflectionSuffix.length();} };
		SortedSet<DutchPattern> P = new TreeSet<DutchPattern>(comparator);
		for (String suffixa: suffixes)
		{
			for (String suffixb: lemmaSuffixes)
			{
				if (a.endsWith(suffixa) && b.endsWith(suffixb))
				{
					String stemA = a.substring(0, a.length() - suffixa.length());
					String stemB = b.substring(0, b.length() - suffixb.length());
					//nl.openconvert.log.ConverterLog.defaultLog.println(stemA + " " + stemB);
					for (String infix: infixes)
					{
						for (String stripped: StringUtils.removeInfix(stemA, infix))
						{
							for (StemChange change: stemChanges)
							{
								//nl.openconvert.log.ConverterLog.defaultLog.println(stemA + " " + stemB + "?"+ change);
								if (!change.appliesToPoS(PoS))
									continue;
								String x = change.transform(stripped);
								if (x != null && x.equals(stemB))
								{
									found = true;
									DutchPattern p = new DutchPattern(suffixa, suffixb, change.type);
									p.infix = stripped.equals(stemA)?"":infix;
									P.add(p);
									//nl.openconvert.log.ConverterLog.defaultLog.println(p);
									//nl.openconvert.log.ConverterLog.defaultLog.println(stripped + "-" + suffixa + " -->" + stemB + "-"  + suffixb + " : " + change.type);
								}
							}
						}
					}
				}
			}
		}
		Pattern foundPattern = null;
		if (!found)
		{
			if (this.useFallback)
				foundPattern = this.fallbackFinder.findPattern(a, b);
			//nl.openconvert.log.ConverterLog.defaultLog.println("Fallback to default for " + a + "~" + b +   " : " + foundPattern);
			//foundPattern = null; // OeHoeps..
		} else
		{
			foundPattern = P.iterator().next();
		}
		
	    if (false && P.size() > 1)
	    {
	    	int k=0;
	    	nl.openconvert.log.ConverterLog.defaultLog.println("multiple for " + a + "~" + b);
	    	for (DutchPattern p: P)
	    	{
	    		nl.openconvert.log.ConverterLog.defaultLog.println(k++ + ":" + p);
	    	}
	    }
		return foundPattern;
	}

	public static void main(String[] args)
	{
		DutchPatternFinder p = new DutchPatternFinder();
		//p.findPattern("gevoltigeerd", "voltigeren", "VRB");
		//System.exit(0);
		p.findPattern("langsliep", "langslopen", "VRB");
		System.exit(0);
		InMemoryLexicon l = new InMemoryLexicon();
		//l.readFromFile("resources/exampledata/type_lemma_pos.tab");
		l.readFromFile(args[0]);
		int explained = 0;
		int unexplained = 0;
		for (WordForm w: l)
		{
			boolean test1 = w.lemmaPoS.equals("NOU") && w.tag.contains("pl");
			boolean test2 = w.lemmaPoS.equals("VRB") && w.tag.contains("impf");
			boolean test3 = w.lemmaPoS.equals("VRB") && w.tag.contains("pres");
			boolean test4 = w.lemmaPoS.equals("VRB") && w.tag.contains("part");
			boolean test5 = w.lemmaPoS.equals("ADJ");
			
			if (!w.wordform.equalsIgnoreCase(w.lemma) && test2)
			{
				Pattern pat=null;
				if ((pat=p.findPattern(w.wordform, w.lemma, w.lemmaPoS)) == null)
				{
					unexplained++;
					//nl.openconvert.log.ConverterLog.defaultLog.println(w);
				} else
				{
					if (!pat.getClass().getName().contains("Dutch"))
					{
						unexplained++;
					} else
					{
						DutchPattern d = (DutchPattern) pat;
						if (d.stemChange.equals(RegularStemChange.IRREGULAR_STEM_CHANGE))
							nl.openconvert.log.ConverterLog.defaultLog.println(d);
						explained++;
					}
				}
			}

		}
		nl.openconvert.log.ConverterLog.defaultLog.println("Explained: " + explained  + " unexplained: " + unexplained);
	}
}
