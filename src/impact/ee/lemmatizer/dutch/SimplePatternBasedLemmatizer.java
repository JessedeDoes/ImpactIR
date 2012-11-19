package impact.ee.lemmatizer.dutch;
import impact.ee.classifier.*;
import impact.ee.classifier.libsvm.*;
import impact.ee.classifier.svmlight.SVMLightClassifier;
import impact.ee.classifier.svmlight.SVMLightClassifier.TrainingMethod;
import impact.ee.classifier.weka.*;
import impact.ee.lexicon.InMemoryLexicon;
import impact.ee.lexicon.WordForm;
import impact.ee.tagger.Context;
import impact.ee.tagger.Corpus;
import impact.ee.tagger.Tagger;
import impact.ee.util.Serialize;
import impact.ee.lemmatizer.Example;
import impact.ee.lemmatizer.Pattern;
import impact.ee.lemmatizer.PatternFinder;
import impact.ee.lemmatizer.Rule;
import impact.ee.lemmatizer.SimpleFeatureSet;
import impact.ee.lemmatizer.SuffixGuesser;
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
 *HM... can we use svmstruct for joint lemma/pos guessing.... ????
 *
 *Lemmatization suitable for database construction is different...
 *disabling smoothing means that we basically get only the first guess in many cases!
 *(so if PoS guess is wrong (gevarengeld --> gevarengelen etc), we do not see the other options!)
 */

public class SimplePatternBasedLemmatizer implements java.io.Serializable, Tagger
{
	private static final long serialVersionUID = 1L;

	Classifier classifierWithPoS = new SVMLightClassifier(); // not used
	Classifier classifierWithoutPoS = new SuffixGuesser(); // WekaClassifier("trees.J48", false);
	Map<String, Rule> ruleID2Rule = new HashMap<String,Rule>();
	Map<Pattern, Pattern> patterns  = new HashMap<Pattern, Pattern>();
	Map<Rule, Rule> rules = new HashMap<Rule, Rule>();

	int ruleId = 1;

	private transient PatternFinder patternFinder = new DutchPatternFinder();

	FeatureSet features = new SimpleFeatureSet();

	public SimplePatternBasedLemmatizer()
	{
		if (classifierWithoutPoS.getClass().getName().contains("SVMLight"))
		{
			((SVMLightClassifier) classifierWithoutPoS).trainingMethod = TrainingMethod.ONE_VS_ALL_EXTERNAL;
		}
		if (classifierWithoutPoS.getClass().getName().contains("SuffixGuesser"))
		{
			SuffixGuesser sfg = (SuffixGuesser) classifierWithoutPoS;
			sfg.applySmoothing = false;
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
				System.err.println(w + " " + rule);
			
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
		// System.exit(1);
	}

	public void test(impact.ee.lexicon.InMemoryLexicon l)
	{
		Set<WordForm> heldout = ReverseLemmatizationTest.createHeldoutSet(l, 0.05);
		train(l,heldout);
		TestDutchLemmatizer testResults = new TestDutchLemmatizer();
		for (WordForm wf: heldout)
		{
			testWordform(wf, testResults);
		}
		System.err.println(testResults);
	}

	private void testWordform(WordForm wf, TestDutchLemmatizer t) 
	{
		//if (!wf.tag.contains("part")) return;
		String answer = classifierWithoutPoS.classifyInstance(features.makeTestInstance(wf.wordform));
		Distribution outcomes = classifierWithoutPoS.distributionForInstance(features.makeTestInstance(wf.wordform));
		checkResult(wf, answer, outcomes, t);
	}

	private void checkResult(WordForm wf, String answer, Distribution outcomes, TestDutchLemmatizer t) 
	{
		t.nItems++;
		Rule r = this.ruleID2Rule.get(answer);
		if (r == null)
		{
			System.err.println("HUH?: " + answer);
		} else if (true || !wf.wordform.equals(wf.lemma))
		{
			//System.err.println(r);
			String guessedLemma = r.pattern.apply(wf.wordform);
			if (guessedLemma == null)
			{
				System.err.println("Dit kan dus niet:.... " + r);
				return;
			}
			boolean isOK = guessedLemma.equalsIgnoreCase(wf.lemma);
			if (isOK) t.nCorrect++;
		    System.err.println("First choice: (" + isOK + ") " + wf + " (" +  r +  ") " + r.PoS + " : " + wf.wordform + " --> " + guessedLemma);
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
					boolean okNOW = guess.equalsIgnoreCase(wf.lemma);
					if (r1.PoS.equals(wf.tag) && !foundTagMatch)
					{
						foundTagMatch = true;
						System.err.println("\tguess with complete tag information: ("  + okNOW + ") " + guess +  " " +  r1);
						if (okNOW)
							t.nCorrectGivenTag++;
					} 
					if (r1.PoS.startsWith(wf.lemmaPoS) && !foundPoSMatch)
					{
						foundPoSMatch = true;
						System.err.println("\tguess with main PoS information: ("  + okNOW + ") " + guess +  " " + r1);
						if (okNOW)
							t.nCorrectGivenPoS++;
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
		Pattern r = patternFinder.findPattern(w.wordform,w.lemma,w.lemmaPoS);
		System.err.println(r);
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

	@Override
	public HashMap<String, String> apply(Context c) 
	{
		// TODO Auto-generated method stub
		HashMap<String,String> m = new HashMap<String,String>();
		//m.put("word", c.getAttributeAt("word", 0));
		
		for (String key: c.getAttributes())
		{
			m.put(key, c.getAttributeAt(key, 0));
		}
		
		String word = c.getAttributeAt("word", 0);
		String tag = c.getAttributeAt("tag", 0);
		String lemma = this.findLemmaConsistentWithTag(word,tag);
		m.put("lemma", lemma);
		return m;
	}

	private String findLemmaConsistentWithTag(String word, String lemma) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Corpus tag(Corpus inputCorpus) 
	{
		// TODO Auto-generated method stub
		return null;
	}
}
