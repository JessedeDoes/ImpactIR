package impact.ee.lemmatizer.dutch;

import java.util.Set;

import impact.ee.lemmatizer.ClassifierSet;
import impact.ee.lemmatizer.FoundFormHandler;
import impact.ee.lemmatizer.Rule;

import impact.ee.lexicon.InMemoryLexicon;
import impact.ee.lexicon.WordForm;
import impact.ee.tagger.BasicNERTagger;
import impact.ee.tagger.Context;
import impact.ee.tagger.SimpleCorpus;

public class MultiplePatternBasedLemmatizer extends
		SimplePatternBasedLemmatizer 
{
	ClassifierSet classifiersPerTag = new ClassifierSet(features, classifierWithoutPoS.getClass().getName());
	
	public void train(InMemoryLexicon lexicon, Set<WordForm> heldOutSet)
	{
		for (WordForm w: lexicon)
		{
			if (heldOutSet != null && heldOutSet.contains(w)) continue;
			Rule rule = findRule(w);
			System.err.println(w + " " + rule);

			this.classifiersPerTag.addItem(w.tag, w.wordform, "rule." + rule.id, rule);
		};
		classifiersPerTag.buildClassifiers();
	}
	
	class theFormHandler implements FoundFormHandler
	{
		public String bestLemma;
		@Override
		public void foundForm(String wordform, String tag, String lemmaPoS,
				Rule r, double p, int rank) 
		{
			
			String l = r.pattern.apply(wordform);
			bestLemma = l;
		}
	}
	
	private String findLemmaConsistentWithTag(String wordform, String tag)
	{
		
		theFormHandler c =  new theFormHandler();
		classifiersPerTag.callback = c;
		classifiersPerTag.classifyLemma(wordform, simplifyTag(tag), tag);
		return c.bestLemma;
	}
	
	public static void main(String[] args)
	{
		InMemoryLexicon l = new InMemoryLexicon();
		l.readFromFile(args[0]);
		//System.err.println(l.findLemmata("is"));
		//System.exit(1);
		MultiplePatternBasedLemmatizer lemmatizer = new MultiplePatternBasedLemmatizer();
		lemmatizer.train(l);
		SimpleCorpus testCorpus = new SimpleCorpus(args[1], BasicNERTagger.defaultAttributeNames);

		for (Context c: lemmatizer.tag(testCorpus).enumerate())
		{
			System.out.println(
					c.getAttributeAt("word", 0) 
					+ "\t" + c.getAttributeAt("tag", 0)
					+ "\t" + c.getAttributeAt("lemma", 0));
		}
		// spbl.test(l);
	}
}
