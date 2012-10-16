package impact.ee.lemmatizer;
import impact.ee.classifier.*;
import impact.ee.classifier.libsvm.LibSVMClassifier;
import impact.ee.lexicon.InMemoryLexicon;
import impact.ee.lexicon.WordForm;
import impact.ee.util.Serialize;
import impact.ee.lemmatizer.reverse.*;
import java.io.IOException;
import java.util.*;

/**
 * Two relevant modes<br>
 * 
 * <ol>
 * <li> Lemmatize with PoS guessed by tagger
 * 
 * <li> Lemmatize only knowing the word form
 * </ol>
 * @author Gebruiker
 *
 */
public class SimplePatternBasedLemmatizer implements java.io.Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Classifier classifierWithPoS = new LibSVMClassifier();
	Classifier classifierWithoutPoS = new LibSVMClassifier();
	Map<String, Rule> ruleID2Rule = new HashMap<String,Rule>();
	Map<Pattern, Pattern> patterns  = new HashMap<Pattern, Pattern>();
	Map<Rule, Rule> rules = new HashMap<Rule, Rule>();
	private Map<String,ArrayList<WordForm>> lemmataSeenInTrainingData = new HashMap<String,ArrayList<WordForm>>();

	private ArrayList<Rule> allRules;
	int ruleId = 1;

	private transient PatternFinder patternFinder = new SimplePatternFinder();

	FeatureSet features = new SimpleFeatureSet();

	public void train(InMemoryLexicon lexicon, Set<WordForm> heldOutSet)
	{
		Dataset trainingSet = new Dataset("lemmatizer");
		trainingSet.features = features;
		try
		{

			for (WordForm w: lexicon) // volgorde: type lemma pos lemma_pos /// why no ID's? it is better to keep them
			{
				if (heldOutSet != null && heldOutSet.contains(w))
				{
					continue;
				} 
				ArrayList<WordForm> l = lemmataSeenInTrainingData.get(w.lemma + ":" + w.lemmaPoS);
				if (l == null)
				{
					lemmataSeenInTrainingData.put(w.lemma + ":" + w.lemmaPoS, (l= new ArrayList<WordForm>()));
				}
				l.add(w);

				Pattern p = findPattern(w);
				Rule rule = findRule(w, p);
				trainingSet.addInstance(w.wordform, "rule." + rule.id);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		// allRules = new ArrayList<Rule>(rules.keySet());
		// Collections.sort(allRules, new RuleFrequencyComparator());
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
		Set<WordForm> heldout = ReverseLemmatizationTest.createHeldoutSet(l, 0.3);
		train(l,heldout);
		for (WordForm wf: heldout)
		{
			String answer = classifierWithoutPoS.classifyInstance(features.makeTestInstance(wf.wordform));
			Rule r = this.ruleID2Rule.get(answer);
			if (r == null)
			{
				System.err.println("HUH?" + answer);
			} else if (!wf.wordform.equals(wf.lemma))
			{
				String guessedLemma = r.pattern.apply(wf.wordform);
				System.err.println(wf + " /  " +  answer +  " / " + r.PoS + " /  " + guessedLemma);
			}
		}
	}

	private Rule findRule(WordForm w, Pattern p) 
	{
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
			// TODO Auto-generated catch block
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
