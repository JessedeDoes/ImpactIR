package impact.ee.lemmatizer.dutch;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import impact.ee.classifier.Classifier;
import impact.ee.classifier.Distribution;
import impact.ee.classifier.FeatureSet;
import impact.ee.classifier.weka.WekaClassifier;
import impact.ee.lemmatizer.ClassifierSet;
import impact.ee.lemmatizer.FoundFormHandler;
import impact.ee.lemmatizer.Rule;
import impact.ee.lemmatizer.SimpleFeatureSet;

import impact.ee.lexicon.InMemoryLexicon;
import impact.ee.lexicon.WordForm;
import impact.ee.tagger.BasicNERTagger;
import impact.ee.tagger.Context;
import impact.ee.tagger.SimpleCorpus;
import impact.ee.util.LemmaLog;

public class MultiplePatternBasedLemmatizer extends
		SimplePatternBasedLemmatizer 
{
	ClassifierSet classifiersPerTag = new ClassifierSet(features, classifierWithoutPoS.getClass().getName());
	Classifier z = new  WekaClassifier("trees.J48", false);
	FeatureSet s = new SimpleFeatureSet();
	//ClassifierSet classifiersPerTag = new ClassifierSet(s, z.getClass().getName());
	
	private Rule lastRule = null;
	
	public void train(InMemoryLexicon lexicon, Set<WordForm> heldOutSet)
	{
		for (WordForm w: lexicon)
		{
			w.tag = simplifyTag(w.tag);
			if (heldOutSet != null && heldOutSet.contains(w)) continue;
			Rule rule = findRule(w); // dit moet omgekeerd -- nee hij klopt zo niet, vereenvoudigde tag komt er niet in
			// System.err.println(w + " " + rule);
			if (rule != null)
				this.classifiersPerTag.addItem(w.tag, w.wordform, "rule." + rule.id, rule);
		};
		classifiersPerTag.buildClassifiers();
	}
	
	class theFormHandler implements FoundFormHandler
	{
		public String bestLemma;
		public List<String> allLemmata = new ArrayList<String>();
		public Rule rule;
		@Override
		public void foundForm(String wordform, String tag, String lemmaPoS,
				Rule r, double p, int rank) 
		{
			String l = r.pattern.apply(wordform);
			String l1 = l; 
			if (l == null) l1 = "null:" + r.pattern;
			allLemmata.add(l1);
			// System.err.println("Possible lemma, wordform=" + wordform + ", rule =  " + r + ", rank  = " + rank + " candidate = "  + l);
			if (bestLemma == null)
			{
				bestLemma = l;
				rule = r;
			}
		}
	}
	
	@Override
	public String simplifyTag(String tag)
	{
		if (tag.startsWith("VRB") || tag.startsWith("XXNOU")) 
		// remove last feature,  beneficial for verb, effect for NOU unclear
		{
			tag = tag.replaceAll(",[^,]*\\)",")");
		}
		return tag;
	}
	
	private String findLemmaConsistentWithTag(String wordform, String tag)
	{
		if (tag.matches(".*NOU.*sg.*")) // ahem dedoes!
		{
			//return wordform.toLowerCase();
		}
		
		theFormHandler c =  new theFormHandler();
		classifiersPerTag.callback = c;
		classifiersPerTag.classifyLemma(wordform, tag, tag, false);
		String lemma = wordform.toLowerCase();
		//System.err.println(c.allLemmata);
		if (c.bestLemma != null)
		{
			lemma = c.bestLemma;
			lastRule = c.rule;
		}
		return lemma;
	}
	
	protected void testWordform(WordForm wf, TestDutchLemmatizer t)
	{
		//if (!wf.tag.contains("part")) return;
		//String answer = classifierWithoutPoS.classifyInstance(features.makeTestInstance(wf.wordform));
		//Distribution outcomes = classifierWithoutPoS.distributionForInstance(features.makeTestInstance(wf.wordform));
		
		String lemma = this.findLemmaConsistentWithTag(wf.wordform, wf.tag);
		
		if (lemma.equals(wf.lemma))
			t.nCorrect++;
		else if (wf.tag.startsWith("NOU"))
		{
			System.err.println("Error: " + wf + " --> " + lemma + "  " + lastRule);
			//System.err.println(LemmaLog.getLastLines(5));
			//System.err.println(classifiersPerTag.allPossibleLabelsForTag(wf.tag));
		}
		t.nItems++;
		//checkResult(wf, answer, outcomes, t);
	}

	public static void main(String[] args)
	{
		InMemoryLexicon l = new InMemoryLexicon();
		l.readFromFile(args[0]);
		//System.err.println(l.findLemmata("is"));
		//System.exit(1);
		MultiplePatternBasedLemmatizer lemmatizer = new MultiplePatternBasedLemmatizer();
		lemmatizer.test(l);
	}
}
