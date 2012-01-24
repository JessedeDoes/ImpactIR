package lemmatizer;
import spellingvariation.DatrieMatcher;
//import spellingvariation.MemorylessMatcher;
import trie.DoubleArrayTrie;
import trie.ITrie;
//import trie.Trie;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import lexicon.ILexicon;
//import lexicon.InMemoryLexicon;
import lexicon.NeoLexicon;
import lexicon.WordForm;
import util.Options;
import java.util.Collections;
import java.util.ArrayList;



public class Lemmatizer
{
	DatrieMatcher  matcher;
	ILexicon historicalLexicon = null;
	ILexicon modernLexicon = null;
	ITrie<Object> lexiconTrie = null;
	boolean useMatcher = true;
	boolean modernWordformAsLemma = false;
	
	public Lemmatizer(String patternFilename, ILexicon m, ILexicon h, ITrie<Object> trie)
	{
		this.historicalLexicon = h;
		this.modernLexicon = m;
		this.matcher = new DatrieMatcher(patternFilename);
		matcher.setMaxPenaltyIncrement(10000);
		matcher.setMaxSuggestions(5);
		this.lexiconTrie = trie;
	}

	public Lemmatizer(String patternFilename, 
			String modernLexiconFilename,
			String historicalLexiconFilename,
			String trieFilename)
	{
		this.modernWordformAsLemma = Options.getOptionBoolean("modernWordformAsLemma", 
				false);
		try
		{
			this.modernLexicon = new NeoLexicon(modernLexiconFilename, false);
		} catch (Exception e)
		{
			
		}
		try
		{
			this.historicalLexicon = new NeoLexicon(historicalLexiconFilename, false);
		} catch (Exception e)
		{
		}

		System.err.println("finished reading lexicon text files");
		this.matcher = new DatrieMatcher(patternFilename);
		//this.lexiconTrie =modernLexicon.createTrie(matcher.addWordBoundaries);
		try 
  		{
			this.lexiconTrie = DoubleArrayTrie.loadTrie(trieFilename);
			System.err.println("loaded lexicon from " + trieFilename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.err.println("created trie from modern lexicon content");
		//this.matcher..
	}

	class candidateCollector extends DatrieMatcher.Callback
	{
		List<WordMatch> candidates;
		
		public candidateCollector()
		{
			candidates = new ArrayList<WordMatch>();
		}
		public candidateCollector(List<WordMatch> extendMePlease)
		{
			candidates = extendMePlease;
		}

		@Override
		public void handleMatch(String targetWord, String matchedWord,
				String matchInfo, int cost, double p)
		{
			Set<WordForm> extraCandidates = (modernLexicon==null)? new HashSet<WordForm>(): modernLexicon.findLemmata(matchedWord);
			if (extraCandidates == null)
			{
				System.err.println("Huh? cannot find matched word in lexicon: " + matchedWord);
			} else
			{
				for (WordForm w: extraCandidates)
				{
					WordMatch x = new WordMatch();
					x.wordform = w;
					x.alignment = matchInfo;
					x.matchScore = p;
					x.wordformFrequency = w.wordformFrequency;
					x.lemmaFrequency = w.lemmaFrequency;
					
					if (p==1.0)
						x.type = MatchType.ModernExact;
					else
						x.type = MatchType.ModernWithPatterns; 
					x.target = targetWord;
					candidates.add(x);
				}
			}
		}
	};

	public boolean modernLexiconHasLemma(String lemma)
	{
		if (lemma.contains("*"))
			return false;
		boolean wordformAsLemma = Options.getOptionBoolean("modernWordformAsLemma", false);	
		Set<WordForm> w = wordformAsLemma? 
				modernLexicon.findLemmata(lemma) : modernLexicon.findForms(lemma, "*");
		return (w != null && w.size() > 0);
	}
	
	public boolean historicalLexiconHasLemma(String lemma)
	{
		if (lemma.contains("*"))
			return false;
		boolean wordformAsLemma = Options.getOptionBoolean("modernWordformAsLemma", false);	
		Set<WordForm> w = wordformAsLemma? 
				historicalLexicon.searchByModernWordform(lemma) : historicalLexicon.findForms(lemma, "*");
				
		return (w != null && w.size() > 0);
	}
	
	List<WordMatch> lookupWordform(String w0)
	{
		String w = spellingvariation.Ligatures.replaceLigatures(w0);
		Set<WordForm> exactMatches = (historicalLexicon == null)? new HashSet<WordForm>() :historicalLexicon.findLemmata(w);
		Set<WordForm> modernMatches = (modernLexicon == null)? 
				new HashSet<WordForm>(): modernLexicon.findLemmata(w);
		if (exactMatches == null)
			exactMatches = new HashSet<WordForm>();
		if (modernMatches == null)
			modernMatches = new HashSet<WordForm>();
		List<WordMatch> matches = new ArrayList<WordMatch>();
		
		for (WordForm wf: modernMatches)
		{
			WordMatch x = new WordMatch();
			x.wordform = wf;
			x.target = w;
			x.type = MatchType.ModernExact;
			x.lemmaFrequency = wf.lemmaFrequency;
			x.wordformFrequency = wf.wordformFrequency;
			matches.add(x);
		}
		for (WordForm wf: exactMatches)
		{
			WordMatch x = new WordMatch();
			x.wordform = wf;
			x.target = w;
			x.type = MatchType.HistoricalExact;
			x.lemmaFrequency = wf.lemmaFrequency;
			x.wordformFrequency = wf.wordformFrequency;
			matches.add(x);
		}
		if (useMatcher)
		{
			matcher.setCallback(new candidateCollector(matches));
			matcher.matchWordToLexicon(lexiconTrie, w.toLowerCase());
		}
		if (modernWordformAsLemma) // the German situation: only modern word form annotated
		{
			for (WordMatch wm: matches)
			{
				if (wm.type == MatchType.ModernExact || wm.type == MatchType.ModernWithPatterns)
				{
					wm.wordform.lemma = wm.wordform.wordform;
				} else
				{
					wm.wordform.lemma = wm.wordform.modernWordform;
				}
			}
		}
		return matches;
	}

	public static void main(String []args)
	{
		new Options(args);
		String c;
		if ((c = Options.getOption("command"))  != null && c. equals("test"))
		{
			(new LemmatizationTest()).runTest();;
			System.exit(0);
		}
		Lemmatizer sll = new Lemmatizer(
				Options.getOption("patternInput"),
				Options.getOption("modernLexicon"), 
				Options.getOption("historicalLexicon"), 
				Options.getOption("lexiconTrie"));
		try
		{
			LemmatizationTest test = new LemmatizationTest();
			BufferedReader input = new BufferedReader(new FileReader(Options.getOption("lemmatizerInput")));
			String w; String line;
			while ((line = input.readLine()) != null)
			{
				String[] parts = line.split("\\t");
				w = parts[0];
				List<WordMatch> s = sll.lookupWordform(w);
				if (s==null || s.size()==0)
				{
					System.out.println(w + "  --> "  + "NoMatch");
					test.incrementCount(MatchType.None);
				}
				else
				{     
					//System.out.println(""  + w + " ");
					ArrayList<WordMatch> asList = new ArrayList<WordMatch>(s);
					Collections.sort(asList, new WordMatchComparator());
					WordMatch bestMatch = asList.get(0);
					test.incrementCount(bestMatch.type);
					System.out.println(w  + " --> " + bestMatch);
					for (WordMatch wf: asList)
					{
						System.out.println("\t" + wf);
					}
				}
			}
			test.matchTypeStatistics();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
