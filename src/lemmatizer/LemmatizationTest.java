package lemmatizer;

import java.io.BufferedReader;
import java.io.FileInputStream;
//import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import util.Options;

/**
 * Test lexicon coverage for the combination of historical and modern with patterns
 * Also test accuracy in some way.
 * Recall only fttb: OK if the right lemma comes up in the suggestion list
 * Instead of precision proper: keep a list of average position of best possible lemma
 * Precision proper is not possible as there is currently no disambiguation!
 * <p>
 * Reference file should contain:
 * 1) type 
 * 2) list of possible lemmata, separated by (|)
 * 3) frequency [optional]
 * <p>
 * @author jesse
 *
 */
public class LemmatizationTest
{
	Lemmatizer lemmatizer;
	Map<MatchType, Integer> typeHash = new HashMap<MatchType, Integer> ();
	
	public void incrementCount(MatchType m)
	{
		Integer z = typeHash.get(m);
		if (z == null)
			typeHash.put(m,1);
		else
			typeHash.put(m,z+1);
	}
	
	public void matchTypeStatistics()
	{
		for (MatchType m: typeHash.keySet())
		{
			System.err.println(m + ": " + typeHash.get(m));
		}
	}
	
	// TODO: 
	// store all; compute precision-recall profile with ranked candidates
	
	public void runTest()
	{
		//new Options(args);
		Lemmatizer simpleLemmatizer = new Lemmatizer(
				Options.getOption("patternInput"),
				Options.getOption("modernLexicon"), 
				Options.getOption("historicalLexicon"),
				Options.getOption("lexiconTrie"));
		IRLexiconEvaluation report = new IRLexiconEvaluation();
		try
		{
			OutputStreamWriter out = new OutputStreamWriter(System.out,"UTF-8");
			// format for the reference file: word, lemmata.
			// there is no part of speech?? never mind....
			Reader reader = new InputStreamReader(new FileInputStream(Options.getOption("lemmatizerInput")), "UTF-8");
			BufferedReader input = new BufferedReader(reader);
			String w; String line;
	
			boolean englishBehaviour = false;
			
			while ((line = input.readLine()) != null)
			{
				String[] parts = line.split("\\t");
				w = parts[0];
				if (util.SimpleTokenizer.isPunctuationOrWhite(w)) // skip this
					continue;
				String correctLemmata = "";
				Set<String> possibleLemmata = new HashSet<String>();
				boolean unfinishedLetter = false;
				if (parts.length > 1)
				{
					for (String l: parts[1].split("\\|"))
					{
						if (l.toLowerCase().matches("^[efgijkrutw].*"))
							unfinishedLetter = true;
						possibleLemmata.add(l.toLowerCase());
						//System.err.println("<" + l + ">");
					}
					correctLemmata = parts[1];
				}
				if (possibleLemmata.size() == 0) // skip unlemmatized tokens
					continue;
				if (englishBehaviour && unfinishedLetter)
				{
					System.err.println("skipping " + line);
					continue;
				}
				@SuppressWarnings("unused")
				/*
				int frequency=-1;
				
				if (parts.length >2)
				{
					try
					{
						frequency = Integer.parseInt(parts[2]); // this is nonsense
					} catch (Exception e)
					{
						//e.printStackTrace();
					}
				}
				*/
				// case sensitive or not?
				IRLexiconEvaluation.Item item = report.addItem(w, possibleLemmata);
				List<WordMatch> matches = simpleLemmatizer.lookupWordform(w.toLowerCase());
				item.matches = matches;
				if (matches==null || matches.size()==0)
				{
					out.write(w + "  --> "  + "NoMatch,  reference: " + correctLemmata + "\n");
					incrementCount(MatchType.None);
				}
				else
				{     
					//System.out.println(""  + w + " ");
					report.matchItem(item, matches);
					
					out.write(w + " --> " + item.matches.get(0) + " reference "  + 
								item.lemma + " foundACorrectLemma + " + item.hasCorrectMatch + "\n");
					out.write(item.matchesAsString());
				
				}
			}
			report.calculate();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		new Options(args);	
		(new LemmatizationTest()).runTest();	
	}
}
