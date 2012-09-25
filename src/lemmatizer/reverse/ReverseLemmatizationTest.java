package lemmatizer.reverse;
import java.util.*;

import classifier.FeatureSet;

import lemmatizer.ClassifierSet;
import lemmatizer.FoundFormHandler;
import lemmatizer.Rule;
import lemmatizer.SimplePatternFinder;
import lexicon.InMemoryLexicon;
import lexicon.WordForm;

import util.Options;

public class ReverseLemmatizationTest implements FoundFormHandler
{
	InMemoryLexicon referenceLexicon  = new InMemoryLexicon();
	int  itemsTested = 0;
	double correctProposals=0;
	double incorrectProposals=0;
	
	public ReverseLemmatizationTest(String referenceLexiconFilename)
	{
		referenceLexicon.readFromFile(referenceLexiconFilename);
	}
	
	/**
	 * implementation of FoundFormHandler interface
	 */
	
	public void foundForm(String lemma, String tag, String lemmaPoS, Rule r, double p, int rank)
	{
		String wf = r.pattern.applyConverse(lemma);
		if (wf != null)
		{
			WordForm w = new WordForm();
			w.lemma = lemma; w.tag=tag; w.lemmaPoS = lemmaPoS; w.wordform = wf;
			if (rank == 0)
				itemsTested++;
			else
				return;
			if (referenceLexicon.containsLemmaWordform(lemma,w.wordform))
			{
				System.err.println("+: " + wf + ": " + lemma  + ":" + tag + ":"  + r);
				correctProposals++;
			} else
			{
				incorrectProposals++;
				System.err.println("-: " + wf + ": " + lemma + ":" + tag + ":" + r	);
			}
		} else 
		{
			System.err.println("Could not apply to " + lemma + ": "  + r);
		}
	}
	
	/**
	 * Create held out set by excluding all word forms for a randomly selected set of lemmata
	 * <p>
	 * @param lexicon
	 * @param portion
	 * @return
	 */
	public static Set<WordForm> createHeldoutSet(InMemoryLexicon lexicon, double portion)
	{
		 Set<WordForm> V  = new HashSet<WordForm>(); 
		 for (String lemma: lexicon.lemma2forms.keySet())
		 {
			 if (Math.random() < portion)
				 V.addAll(lexicon.lemma2forms.get(lemma));
		 }
		 return V;
	}
	
	public void runTest(ParadigmExpander r)
	{
		r.setCallback(this);
		InMemoryLexicon all = referenceLexicon;
		Set<WordForm> heldOut = createHeldoutSet(all, 0.1);
		System.err.println("Created held-out set of size " + heldOut.size());
		r.findInflectionPatterns(all, heldOut);
		for (WordForm wf: heldOut) r.expandWordForm(wf);
		System.err.println("Total tested: " +  itemsTested + 
				" correct: " + correctProposals/itemsTested + " total wrong: " + incorrectProposals);
	}
	
	public static void main(String [] args)
	{
	  new util.Options(args);
		// String trainingData = Options.getOption("trainFile");
		// String testData =  Options.getOption("testFile");
		String referenceLexicon = Options.getOption("referenceLexicon");
		FeatureSet fs = new FeatureSet.Dummy();
		ClassifierSet cs = new ClassifierSet(fs, "lemmatizer.SuffixGuesser");
		ReverseLemmatizer rl = new ReverseLemmatizer(new SimplePatternFinder(), cs);
		new ReverseLemmatizationTest(referenceLexicon).runTest(rl);
	}
}
