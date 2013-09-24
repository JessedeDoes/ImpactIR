package impact.ee.lemmatizer;
import impact.ee.classifier.Classifier;
import impact.ee.classifier.Dataset;
import impact.ee.classifier.Distribution;
import impact.ee.classifier.FeatureSet;
import impact.ee.classifier.Instance;
import impact.ee.lemmatizer.reverse.ParadigmExpander;
import impact.ee.lemmatizer.reverse.ReverseLemmatizationTest;
import impact.ee.lemmatizer.reverse.ReverseLemmatizer;
import impact.ee.lexicon.InMemoryLexicon;
import impact.ee.lexicon.WordForm;
import impact.ee.util.Options;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.io.*;



class Pair<X,Y>
{
	X first;
	Y second;

	public boolean equals(Object other)
	{
		try
		{
			@SuppressWarnings("unchecked")
			Pair<X,Y> p = (Pair<X,Y>) other;
			return p.first.equals(first) && p.second.equals(second);
		} catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public int hashCode()
	{
		return first.hashCode() +257 *  second.hashCode();
	}
}

class ScoredRule
{
	Rule rule;
	double p;
	int rank;
	ScoredRule(Rule r, double p, int rank)
	{
		rule=r;
		this.p=p;
		this.rank=rank;
	}
}
/**
 * Try to improve things like "ge"-insertion at the right position in Dutch or German, by running independent classifiers for
 * what happens at  the start of the word and at the end of the word.
 * <p>
 * @author jesse
 *
 */
public class PrefixSuffixGuesser implements ParadigmExpander, FoundFormHandler, Classifier
{
	// and the classifier sets????
	ReverseLemmatizer initialExpander = null;
	ReverseLemmatizer finalExpander = null;
	SimplePatternFinder simple = new SimplePatternFinder();
	FoundFormHandler callback = null;
	Set<Pair<Pattern,Pattern>> compatibilities = new HashSet<Pair<Pattern,Pattern>>();

	private HashMap<String,ArrayList<WordForm>> lemmataSeenInTrainingData = new HashMap<String,ArrayList<WordForm>>();

	private boolean seenLemmaInTrainingData(String lemma, String lemmaPoS)
	{
		return lemmataSeenInTrainingData.containsKey(lemma + ":" + lemmaPoS);
	}	
	public PrefixSuffixGuesser()
	{
		FeatureSet fs1 = new FeatureSet.ReversedDummy();
		FeatureSet fs2 = new FeatureSet.Dummy();

		ClassifierSet cs1 = new ClassifierSet(fs1, "impact.ee.lemmatizer.SuffixGuesser");
		ClassifierSet cs2 = new ClassifierSet(fs2, "impact.ee.lemmatizer.SuffixGuesser");

		initialExpander = new ReverseLemmatizer(new InitialPatternFinder(), cs1);
		finalExpander = new ReverseLemmatizer(new FinalPatternFinder(), cs2);

		this.callback =  this;
		//new ReverseLemmatizationTest(referenceLexicon).runTest(rl)
	}

	class Knutselaar implements FoundFormHandler
	{
		List<ScoredRule> initialBag = new ArrayList<ScoredRule>(); // should be rule and score
		List<ScoredRule> finalBag = new ArrayList<ScoredRule>();
		List<ScoredRule> currentBag = null;

		public void foundForm(String lemma, String tag, String lemmaPoS, Rule r,
				double p, int rank)
		{
			// TODO Auto-generated method stub
			//if (tag.matches("part.*past"))
			//	System.err.println(r + " for " + lemma + ":" + tag);
			currentBag.add(new ScoredRule(r,p,rank));
		}

		String reverse(String s)
		{
			return new StringBuffer(s).reverse().toString();
		}

		public ScoredRule knutsel() // what about the scores...
		{
			SimplePattern pat = new SimplePattern();
			try
			{
				ScoredRule initialTop = initialBag.get(0);
				ScoredRule finalTop = finalBag.get(0);
				Rule r = new Rule();
				r.pattern = pat;
				SimplePattern f = (SimplePattern) finalTop.rule.pattern;
				SimplePattern i = (SimplePattern) initialTop.rule.pattern;
				if (f.leftSuffix.startsWith("ge") && i.leftPrefix.endsWith("ge")) // Very.Ugly.
				{
					pat.leftPrefix=pat.rightPrefix="";
				} else
				{
					pat.leftPrefix  = ((SimplePattern) initialTop.rule.pattern).leftPrefix;
					pat.rightPrefix  = ((SimplePattern) initialTop.rule.pattern).rightPrefix;
				}
				pat.leftSuffix  = (((SimplePattern) finalTop.rule.pattern).leftSuffix);
				pat.rightSuffix  = (((SimplePattern) finalTop.rule.pattern).rightSuffix);
				r.id = (1000000 * initialTop.rule.id) + finalTop.rule.id;
				//System.err.println("Concocted: " + r);
				ScoredRule concocted = new ScoredRule(r,initialTop.p * finalTop.p, 0);

				return concocted;
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			return null;
		}	
	}

	Knutselaar knutselaar = new Knutselaar();

	public void findInflectionPatterns(InMemoryLexicon lexicon, Set<WordForm> heldOutSet)
	{
		initialExpander.findInflectionPatterns(lexicon, heldOutSet);
		finalExpander.findInflectionPatterns(lexicon, heldOutSet);
	}

	public void findInflectionPatterns(String fileName)
	{
		InMemoryLexicon l = new InMemoryLexicon();
		l.readFromFile(fileName);
		findInflectionPatterns(l, new HashSet<WordForm>());
	}


	public void expandLemmaList(String filename)
	{
		try
		{
			BufferedReader b = new BufferedReader(new FileReader(filename)) ; // stop - encoding UTF8
			String s;
			while ( (s = b.readLine()) != null)
			{
				String[] fields = s.split("\\t");
				if (fields.length < 2) continue;
				String lemma = fields[0];
				String lemmaPoS = fields[1];
				WordForm w = new WordForm();
				w.lemma=lemma; w.lemmaPoS=lemmaPoS;
				// to do: just add the forms from the example material when the lemma/pos combi is known
				if (!seenLemmaInTrainingData(lemma,lemmaPoS))
					expandAllTagsForWordform(w);
				else // just echo later on what we already know
				{
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		for (String lp: lemmataSeenInTrainingData.keySet())
		{
			for (WordForm s: lemmataSeenInTrainingData.get(lp))
			{
				System.out.println(s.toStringTabSeparated());
			}
		}
	}

	public void expandAllTagsForWordform(WordForm w)
	{
	  for (String tag: initialExpander.allTags())
	  {
	  	if (tag.startsWith(w.lemmaPoS))
	  	{
	  		w.tag = tag;
	  		expandWordForm(w);
	  	}
	  }
	}

	public void expandWordForm(WordForm w)
	{
		//System.err.println("start expanding: " + w);
		knutselaar.initialBag.clear();
		knutselaar.finalBag.clear();

		initialExpander.callback = knutselaar;
		finalExpander.callback = knutselaar;
		knutselaar.currentBag = knutselaar.initialBag;
		initialExpander.expandWordForm(w);
		knutselaar.currentBag = knutselaar.finalBag;
		finalExpander.expandWordForm(w);
		ScoredRule r = knutselaar.knutsel();
		if (r !=  null)
		{
			callback.foundForm(w.lemma, w.tag, w.lemmaPoS, r.rule, r.p, r.rank);
		} 
	}

	class FinalPatternFinder implements PatternFinder
	{
		@Override
		public Pattern findPattern(String a, String b)
		{
			SimplePattern sp = (SimplePattern) simple.findPattern(a, b);
			sp.leftPrefix = (sp.rightPrefix="");
			return sp;
		}
		@Override
		public Pattern findPattern(String a, String b, String PoS)
		{
			return null;
		}
	}

	class InitialPatternFinder implements PatternFinder
	{
		@Override
		public Pattern findPattern(String a, String b)
		{
			SimplePattern sp = (SimplePattern) simple.findPattern(a, b);
			sp.leftSuffix = (sp.rightSuffix="");
			return sp;
		}
		@Override
		public Pattern findPattern(String a, String b, String PoS)
		{
			return null;
		}
	}

	@Override
	public void setCallback(FoundFormHandler callback)
	{
		// TODO Auto-generated method stub
		this.callback = callback;
	}

	public void foundForm(String lemma, String tag, String lemmaPoS, Rule r, double p, int rank)
	{
		// TODO Auto-generated method stub
		String wf = r.pattern.applyConverse(lemma);
		if (wf != null)
		{
			System.out.println(String.format("%s\t%s\t%s\t%s\t%f\t[%d]\t%s=%s", wf,
					lemma,tag,lemmaPoS, p, rank, r.id, r.toString()));
		}
	}	
	
	public static void main(String[] args)
	{
		new impact.ee.util.Options(args);
		String referenceLexicon = Options.getOption("referenceLexicon");
		ParadigmExpander pe = new PrefixSuffixGuesser();
		if (Options.getOption("command") != null && Options.getOption("command").equals("test"))
		{
			ReverseLemmatizationTest test = new ReverseLemmatizationTest(referenceLexicon);
			test.runTest(pe);
		} else
		{
			pe.findInflectionPatterns(Options.getOption("trainFile"));
			pe.expandLemmaList(Options.getOption("testFile"));

		}
	}
	@Override
	public String classifyInstance(Instance i) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Distribution distributionForInstance(Instance i) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void train(Dataset d) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void train(Dataset d, int MAX_ITEMS_USED) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void save(String filename) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void load(String filename) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setType(String classifierType) {
		// TODO Auto-generated method stub
		
	}
}
