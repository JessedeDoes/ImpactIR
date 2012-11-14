package impact.ee.lemmatizer;

import impact.ee.util.Options;

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
	boolean plainTextOutput = false;
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
			String inputFile = Options.getOption("lemmatizerInput");
			Reader reader = new InputStreamReader(new FileInputStream(inputFile), "UTF-8");
			BufferedReader input = new BufferedReader(reader);
			String w; String line;
	
			boolean englishBehaviour = false;
			
			while ((line = input.readLine()) != null)
			{
				String[] parts = line.split("\\t");
				w = parts[0];
				if (impact.ee.util.SimpleTokenizer.isPunctuationOrWhite(w)) // skip this
					continue;
				String correctLemmata = "";
				Set<String> possibleLemmata = new HashSet<String>();
				boolean unfinishedLetter = false;
				boolean lemmaInModernLexicon = false;
				boolean lemmaInHistoricalLexicon = false;
				if (parts.length > 1)
				{
					for (String l: parts[1].split("\\|"))
					{
						if (l.toLowerCase().matches("^[efgijkrutw].*"))
							unfinishedLetter = true;
						possibleLemmata.add(l.toLowerCase());
						if (simpleLemmatizer.historicalLexiconHasLemma(l))
							lemmaInHistoricalLexicon=true;
						if (l.contains("*****") || simpleLemmatizer.modernLexiconHasLemma(l)) // german wildcard again
							lemmaInModernLexicon=true;
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
				
				// case sensitive or not?
				IRLexiconEvaluation.Item item = report.addItem(w, possibleLemmata);
				
				item.lemmaInHistoricalLexicon = lemmaInHistoricalLexicon;
				item.lemmaInModernLexicon = lemmaInModernLexicon;
				
				List<WordMatch> matches = simpleLemmatizer.lookupWordform(w.toLowerCase());
				item.matches = matches;
				report.matchItem(item, matches);
				
				/*
				if (matches==null || matches.size()==0)
				{
					if (this.plainTextOutput)
						out.write(w + "  --> "  + "NoMatch,  reference: " + correctLemmata + "\n");
					System.err.println("no match for item " + item.n);
					incrementCount(MatchType.None);
				}
				
				else
				{     
					//System.out.println(""  + w + " ");
					report.matchItem(item, matches);
					
					if (plainTextOutput)
					{
						out.write(w + " := " + item.matches.get(0) + " reference "  + 
								item.lemma + " foundACorrectLemma + " + item.hasCorrectMatch + "\n");
						out.write("\t" + item.matchesAsString() + "\n");
					}
				
				}
				*/
			}
			report.print(System.err);
			if (!plainTextOutput)
			{
				report.marshal();
			}
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
