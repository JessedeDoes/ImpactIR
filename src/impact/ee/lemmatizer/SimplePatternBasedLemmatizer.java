package impact.ee.lemmatizer;
import impact.ee.classifier.*;
import impact.ee.classifier.libsvm.*;
import impact.ee.classifier.svmlight.SVMLightClassifier;
import impact.ee.classifier.svmlight.SVMLightClassifier.TrainingMethod;
import impact.ee.classifier.weka.*;
import impact.ee.lexicon.InMemoryLexicon;
import impact.ee.lexicon.WordForm;
import impact.ee.util.Serialize;
import impact.ee.lemmatizer.reverse.*;
import impact.ee.classifier.Distribution.Outcome;
import java.io.IOException;
import java.util.*;

/**
 * Two relevant modes<br>
 * 
 * <ol>
 * <li> Lemmatize with PoS guessed by tagger
 * <li> Lemmatize only knowing the word form
 * </ol>
 * @author Gebruiker
 *
 */

public class SimplePatternBasedLemmatizer implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	Classifier classifierWithPoS = new SVMLightClassifier();
	Classifier classifierWithoutPoS = new SVMLightClassifier(); // WekaClassifier("trees.J48", false);
	Map<String, Rule> ruleID2Rule = new HashMap<String,Rule>();
	Map<Pattern, Pattern> patterns  = new HashMap<Pattern, Pattern>();
	Map<Rule, Rule> rules = new HashMap<Rule, Rule>();

	int ruleId = 1;

	private transient PatternFinder patternFinder = new SimplePatternFinder();

	FeatureSet features = new SimpleFeatureSet();

	public SimplePatternBasedLemmatizer()
	{
		if (classifierWithoutPoS.getClass().getName().contains("SVMLight"))
		{
			((SVMLightClassifier) classifierWithoutPoS).trainingMethod = TrainingMethod.ONE_VS_ALL_EXTERNAL;
		}
		if (classifierWithoutPoS.getClass().getName().contains("SuffixGuesser"))
		{
			features = new FeatureSet.Dummy();
		}
	}
	
	public void train(InMemoryLexicon lexicon, Set<WordForm> heldOutSet)
	{
		Dataset trainingSet = new Dataset("lemmatizer");
		trainingSet.features = features;
		try
		{
			for (WordForm w: lexicon) // volgorde: type lemma pos lemma_pos /// why no ID's? it is better to keep them
			{
				if (heldOutSet != null && heldOutSet.contains(w)) continue;
				Rule rule = findRule(w);
				trainingSet.addInstance(w.wordform, "rule." + rule.id);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		features.finalize(); // hm is this still needed?
		try
		{
			classifierWithoutPoS.train(trainingSet);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void test(impact.ee.lexicon.InMemoryLexicon l)
	{
		Set<WordForm> heldout = ReverseLemmatizationTest.createHeldoutSet(l, 0.1);
		train(l,heldout);
		for (WordForm wf: heldout)
		{
			testWordform(wf);
		}
	}

	private void testWordform(WordForm wf) 
	{
		String answer = classifierWithoutPoS.classifyInstance(features.makeTestInstance(wf.wordform));
		Distribution outcomes = classifierWithoutPoS.distributionForInstance(features.makeTestInstance(wf.wordform));
		Rule r = this.ruleID2Rule.get(answer);
		if (r == null)
		{
			System.err.println("HUH?: " + answer);
		} else if (!wf.wordform.equals(wf.lemma))
		{
			//System.err.println(r);
			String guessedLemma = r.pattern.apply(wf.wordform);
			if (guessedLemma != null)
				System.err.println("First choice:" + wf + " (" +  answer +  ") " + r.PoS + " : " + wf.wordform + " --> " + guessedLemma);
			if (guessedLemma == null || !guessedLemma.equals(wf.lemma))
			{
				boolean foundPoSMatch = false;
				boolean foundTagMatch = false;
				for (Outcome o: outcomes.outcomes)
				{
					Rule r1 =  this.ruleID2Rule.get(o.label);
					if (r1 == null)
					{
						System.err.println("Vreemd hoor!!!" + o.label);
						continue;
					}
					String guess = r1.pattern.apply(wf.wordform);
					if (guess == null)
						continue;
					if (r1.PoS.equals(wf.tag) && !foundTagMatch)
					{
						foundTagMatch = true;
						System.err.println("guess with complete tag information:"  + guess);
					} 
					if (r1.PoS.startsWith(wf.lemmaPoS) && !foundPoSMatch)
					{
						foundPoSMatch = true;
						System.err.println("guess with main PoS information:"  + guess + " / " + r1.PoS);
					}
				}
			}
		}
	}

	private Rule findRule(WordForm w) 
	{
		Pattern p = findPattern(w);
		Rule rule = new Rule(p, w.tag, w.lemmaPoS);
		Rule theRule = rules.get(rule);

		if (theRule == null)
		{
			rule.id = ruleId++;
			rules.put(rule,rule);
			ruleID2Rule.put("rule." + rule.id, rule);
			theRule=rule;
		} else
		{

		}
		theRule.count++;
		theRule.examples.add(new Example(w.wordform,w.lemma));

		return theRule;
	}

	private Pattern findPattern(WordForm w) 
	{
		Pattern p = null;
		Pattern r = patternFinder.findPattern(w.wordform,w.lemma);
		p = patterns.get(r);
		if (p == null)
		{
			patterns.put(r,r);
			p=r;
		}
		return p;
	}

	public void saveToFile(String fileName)
	{
		try 
		{
			new Serialize<SimplePatternBasedLemmatizer>().saveObject(this, fileName);
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	public static SimplePatternBasedLemmatizer loadFromFile(String fileName)
	{
		return new Serialize<SimplePatternBasedLemmatizer>().loadFromFile(fileName);
	}

	public static void main(String[] args)
	{
		InMemoryLexicon l = new InMemoryLexicon();
		l.readFromFile(args[0]);
		SimplePatternBasedLemmatizer spbl = new SimplePatternBasedLemmatizer();
		spbl.test(l);
	}
}
