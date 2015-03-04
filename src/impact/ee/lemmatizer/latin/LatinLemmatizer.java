package impact.ee.lemmatizer.latin;

import impact.ee.classifier.FeatureSet;
import impact.ee.classifier.svmlight.SVMLightClassifier;
import impact.ee.classifier.svmlight.SVMLightClassifier.TrainingMethod;
import impact.ee.lemmatizer.SuffixGuesser;
import impact.ee.lemmatizer.dutch.MultiplePatternBasedLemmatizer;
import impact.ee.lemmatizer.dutch.SimplePatternBasedLemmatizer;
import impact.ee.lemmatizer.tagset.PerseusTagset;
import impact.ee.lemmatizer.tagset.TrivialRelation;
import impact.ee.lexicon.InMemoryLexicon;

public class LatinLemmatizer extends MultiplePatternBasedLemmatizer 
{
	public LatinLemmatizer()
	{
		super();
		this.tagRelation = new TrivialRelation();
		this.corpusTagset = new PerseusTagset();
	}
	
	public static void main(String[] args)
	{
		LatinLemmatizer ll = new LatinLemmatizer();
		InMemoryLexicon l = new InMemoryLexicon();
		l.readFromFile(args[0]);
		ll.test(l);
	}
}
