package impact.ee.lemmatizer;
//import spellingvariation.MemorylessMatcher;
//import trie.Trie;

import impact.ee.lexicon.ILexicon;
import impact.ee.lexicon.LexiconDatabase;
import impact.ee.lexicon.MapDBLexicon;
import impact.ee.lexicon.NeoLexicon;
import impact.ee.lexicon.WordForm;
import impact.ee.spellingvariation.DatrieMatcher;
import impact.ee.trie.DoubleArrayTrie;
import impact.ee.trie.ITrie;
import impact.ee.util.Options;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;



public class Lemmatizer
{
	DatrieMatcher  matcher;
	public ILexicon historicalLexicon = null;
	public ILexicon modernLexicon = null;
	ITrie<Object> lexiconTrie = null;
	private boolean useMatcher = true;
	boolean modernWordformAsLemma = false;
	boolean simplify = true;
	boolean believeExactMatches = true;
	boolean preferHistorical = true;
	boolean toLowerCaseBeforeLookup = true;
	
	static enum StorageType { NEO, MAPDB, IN_MEMORY, JDBC };
	
	static StorageType defaultStorageBackend = StorageType.MAPDB;
	
	Map<String, List<WordMatch>> cache = new HashMap<String, List<WordMatch>>();
	
	public void close()
	{
		if (historicalLexicon instanceof NeoLexicon)
			((NeoLexicon) historicalLexicon).close();
		if (modernLexicon instanceof NeoLexicon)
			((NeoLexicon) modernLexicon).close();
	}
	
	public Lemmatizer(String patternFilename, ILexicon m, ILexicon h, ITrie<Object> trie)
	{
		this.historicalLexicon = h;
		this.modernLexicon = m;
		this.matcher = new DatrieMatcher(patternFilename);
		
		//matcher.setMaxPenaltyIncrement(10000);
		matcher.setMaxSuggestions(3);
		matcher.setMaximumPenalty(1000);
		//matcher.
		this.lexiconTrie = trie;
	}

	public Lemmatizer(String patternFilename, 
			String modernLexiconFilename,
			String historicalLexiconFilename,
			String trieFilename)
	{
		this(patternFilename, modernLexiconFilename, historicalLexiconFilename, trieFilename, defaultStorageBackend);
	}
	
	public Lemmatizer(String patternFilename, 
			String modernLexiconFilename,
			String historicalLexiconFilename,
			String trieFilename, StorageType storageType)
	{
		this.modernWordformAsLemma = Options.getOptionBoolean("modernWordformAsLemma", 
				false);
		
		switch (storageType)
		{
			case NEO: loadLexicaNeo(modernLexiconFilename, historicalLexiconFilename); break;
			case MAPDB: loadLexicaMapDB(modernLexiconFilename, historicalLexiconFilename); break;
		}

		System.err.println("finished reading lexicon text files");
		this.matcher = new DatrieMatcher(patternFilename);
		// this.lexiconTrie =modernLexicon.createTrie(matcher.addWordBoundaries);
		try 
  		{
			this.lexiconTrie = DoubleArrayTrie.loadTrie(trieFilename);
			System.err.println("Loaded lexicon from " + trieFilename);
		} catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.err.println("created trie from modern lexicon content");
		//this.matcher ..
	}

	private void loadLexicaNeo(String modernLexiconFilename,
			String historicalLexiconFilename) 
	{
		try
		{
			this.modernLexicon = new NeoLexicon(modernLexiconFilename, false);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			File f = new File(historicalLexiconFilename);
			if (f.exists())
			{
				this.historicalLexicon =
						new NeoLexicon(historicalLexiconFilename, false);
			} else
			{
				this.historicalLexicon = new LexiconDatabase("svowim02", "EE3_5");
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void loadLexicaMapDB(String modernLexiconFilename,
			String historicalLexiconFilename) 
	{
		try
		{
			this.modernLexicon = new MapDBLexicon(modernLexiconFilename);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			File f = new File(historicalLexiconFilename);
			if (f.exists())
			{
				this.historicalLexicon =
						new MapDBLexicon(historicalLexiconFilename);
			} else
			{
				this.historicalLexicon = new LexiconDatabase("svowim02", "EE3_5");
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
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
	
	public List<WordMatch> lookupWordform(String w0)
	{
		String w = impact.ee.spellingvariation.Ligatures.replaceLigatures(w0);
		if (this.toLowerCaseBeforeLookup) 
			w = w.toLowerCase();
		List<WordMatch> cached = cache.get(w);
		if (cached != null)
			return cached;
		Set<WordForm> exactMatches = (historicalLexicon == null)? new HashSet<WordForm>() :historicalLexicon.findLemmata(w);
		Set<WordForm> modernMatches = new HashSet<WordForm>();
		if (!this.preferHistorical || exactMatches.size() == 0)
			modernMatches = (modernLexicon == null)? 
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
		
		//System.err.println("for:  " + w0 + ", found before matching: " + matches.size());
		
		if (isUseMatcher() && !(believeExactMatches && matches.size() > 0))
		{	
			// System.err.println(" use matcher for  " + w0 + " found before matching: " + matches.size());
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
		if (simplify)
			matches = WordMatch.simplify(matches, true);
		Collections.sort(matches, new WordMatchComparator()); // OK this was missing. No more kemmen now??
		cache.put(w, matches);
		return matches;
	}

	public void destroy()
	{
		destroyLexicon(this.modernLexicon);
		destroyLexicon(this.historicalLexicon);
	}
	
	public void destroyLexicon(ILexicon l)
	{
		if (l == null)
			return;
		try 
		{
			Class c = l.getClass();
			System.err.println("CLASS: "  + c);
			Method m = l.getClass().getDeclaredMethod("destroy");
			if (m != null)
				m.invoke(l,null);
		} catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public static void main(String []args)
	{
		new Options(args);
		String c;
		boolean simplify = true;
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
					if (simplify)
					{
						s = WordMatch.simplify(s, false);
					}
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

	boolean isUseMatcher() {
		return useMatcher;
	}

	public void setUseMatcher(boolean useMatcher) {
		this.useMatcher = useMatcher;
	}
}
