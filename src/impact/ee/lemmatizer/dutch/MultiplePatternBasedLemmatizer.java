package impact.ee.lemmatizer.dutch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import impact.ee.lemmatizer.dutch.LemmaMatch.MatchType;
import impact.ee.lemmatizer.tagset.*;

import impact.ee.classifier.Classifier;
import impact.ee.classifier.Distribution;
import impact.ee.classifier.FeatureSet;
import impact.ee.classifier.weka.WekaClassifier;
import impact.ee.lemmatizer.ClassifierSet;
import impact.ee.lemmatizer.FoundFormHandler;
import impact.ee.lemmatizer.Rule;
import impact.ee.lemmatizer.SimpleFeatureSet;
import impact.ee.lemmatizer.tagset.GiGaNTCorpusLexiconRelation;
import impact.ee.lemmatizer.tagset.GiGaNTTagSet;

import impact.ee.lexicon.InMemoryLexicon;
import impact.ee.lexicon.WordForm;
import impact.ee.tagger.BasicNERTagger;
import impact.ee.tagger.BasicTagger;
import impact.ee.tagger.ChainOfTaggers;
import impact.ee.tagger.Context;
import impact.ee.tagger.SimpleCorpus;
import impact.ee.tagger.Tagger;
import impact.ee.tagger.features.TaggerFeatures;
import impact.ee.util.LemmaLog;

public class MultiplePatternBasedLemmatizer extends SimplePatternBasedLemmatizer 
{
	ClassifierSet classifiersPerTag = new ClassifierSet(features, classifierWithoutPoS.getClass().getName());
	Classifier z = new  WekaClassifier("trees.J48", false);
	FeatureSet s = new SimpleFeatureSet();
	private Map<String,Set<String>> matchInformation = new HashMap<String,Set<String>>();
	LemmaMatchLog lemmaLog = new LemmaMatchLog();

	boolean noHeuristics = false;
	boolean reduceNounGender = true; 

	// taking gender into account is better in a lexicon leave out test, but not practically possible in corpus tagging

	private Rule lastRule = null;

	public MultiplePatternBasedLemmatizer()
	{
		this.tagRelation = new GiGaNTCorpusLexiconRelation();
		this.corpusTagset = new GiGaNTTagSet();
	}

	public void train(InMemoryLexicon lexicon, Set<WordForm> heldOutSet)
	{
		int nFallbacks = 0;
		int nWords = 0;
		this.lexicon = lexicon; 
		boolean sampling = false;
		addPresentParticleToLexicon(lexicon); // and other hacks as well....
		for (WordForm w: lexicon)
		{
			w.tag = simplifyTag(w.tag); 

			if (heldOutSet != null && heldOutSet.contains(w)) 
				continue;
			if (sampling && !(nWords % 10 == 0))
				continue;
			if (noHeuristics) continue;

			Rule rule = findRule(w); 

			if (rule != null)
				this.classifiersPerTag.addItem(w.tag, w.wordform, "rule." + rule.id, rule);
			if (!rule.pattern.getClass().getName().contains("DutchPattern" ))
				nFallbacks++;
			nWords ++;
		};

		System.err.println("Words " + nWords + "  fallbacks " + nFallbacks);
		classifiersPerTag.buildClassifiers();
	}
/**
 * Generate some extra forms
 * Also: s-genitives
 * @param lexicon
 */
	private void addPresentParticleToLexicon(InMemoryLexicon lexicon)
	{	
		Set<WordForm> additions = new HashSet<WordForm>();

		for (WordForm w: lexicon) // temporary hack: add present participles etc
		{
			w.tag = w.tag.replaceAll("NA=", "number=");
			w.tag = w.tag.replaceAll("PA=", "person=");
			w.tag = w.tag.replaceAll("/","|");

			if (w.tag.matches("VRB.*[23].*") && !w.tag.contains("1"))
			{
				if (w.wordform.endsWith("t") && !w.lemma.endsWith("ten")) // ahem moet; omvat; .....
				{
					w.tag = w.tag.replaceAll("\\)", ",formal=infl-t)");
					//System.err.println(w);
				}
			}

			if (w.tag.matches("AA.*") && w.wordform.endsWith("e") && !w.lemma.endsWith("e"))
			{
				w.tag = w.tag.replaceAll("\\)", ",formal=infl-e)");
				//System.err.println(w);
			}

			if (w.tag.matches("AA.*") && w.wordform.endsWith("s") && !w.lemma.endsWith("s"))
			{
				w.tag = w.tag.replaceAll("\\)", ",formal=infl-s)");
				//System.err.println(w);
			}

			if (w.tag.matches("VRB.*inf.*"))
			{
				WordForm w1 = new WordForm();
				w1.wordform = w.wordform + "d";
				w1.lemma = w.lemma;
				w1.lemmaPoS =w.lemmaPoS;
				w1.tag="VRB(finiteness=part,tense=pres)";

				additions.add(w1);

				WordForm w2 = new WordForm();
				w2.wordform = w.wordform + "de";
				w2.lemma = w.lemma;
				w2.lemmaPoS =w.lemmaPoS;
				w2.tag="VRB(finiteness=part,tense=pres,formal=infl-e)";
				additions.add(w2);

				WordForm w3 = new WordForm(); // not needed anymore??
				w3.wordform = w.wordform;
				w3.lemma = w.lemma;
				w3.lemmaPoS =w.lemmaPoS;
				w3.tag="VRB(mood=ind,tense=pres,number=pl,finiteness=fin)";
				additions.add(w3);
			}
			if (w.tag.matches("VRB.*part.*past.*") && w.lemma.matches(".*en$") && !w.wordform.matches(".*e$"))
			{
				WordForm w1 = new WordForm();
				w1.wordform = w.wordform + "e"; // niet altijd!
				w1.lemma = w.wordform;
				w1.lemmaPoS =w.lemmaPoS;
				w1.tag="AA(degree=pos,formal=infl-e)";
				additions.add(w1);
			}
		}

		for (WordForm w:additions)
			lexicon.addWordform(w);
	}

	public void train(InMemoryLexicon lexicon)
	{
		train(lexicon, null);
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

			if (bestLemma == null)
			{
				bestLemma = l;
				rule = r;
			}
		}
	}

	@Override
	public String simplifyTag(String tag) // aHem... zo sloop je al die formals er snel weer af ....
	{
		String[] coreF = { "pos", "tense", "mood", "person", "number", "formal", "finiteness" };
		Set<String> coreFeatures = new HashSet<String>(Arrays.asList(coreF));

		if (tag.startsWith("VRB"))
		{
			Tag t = corpusTagset.parseTag(tag);
			//tag = tag.replaceAll(",[^,]*\\)",")");
			Set<String> fnames = t.keySet();
			for (String fname: fnames)
			{
				if (!coreFeatures.contains(fname))
				{
					t.remove(fname);
				}
			}
			return t.toString();
		}

		if (reduceNounGender && tag.startsWith("NOU-C"))
		{
			Tag t = corpusTagset.parseTag(tag);
			//tag = tag.replaceAll(",[^,]*\\)",")");
			Set<String> fnames = t.keySet();
			for (String fname: fnames)
			{
				if (!coreFeatures.contains(fname))
				{
					t.remove(fname);
				}
			}
			return t.toString();
		}
		return tag;
	}

	@Override
	protected String findLemmaConsistentWithTag(String wordform, String corpusTag) 
	{
		if (corpusTag == null)
			return wordform.toLowerCase();

		String bestGuess = checkLexiconAndCache(wordform, corpusTag);

		if (bestGuess != null)
			return bestGuess;


		bestGuess = wordform.toLowerCase();

		if (!corpusTagset.isInflectingPoS(corpusTag))
		{
			lemmaLog.addToLog(wordform, wordform.toLowerCase(), "_none_", corpusTag, MatchType.Unknown);
			return wordform.toLowerCase();
		}


		String lemma = wordform.toLowerCase();

		boolean foundHeuristicMatch = false;
		if (!noHeuristics) for (String lexiconTag: classifiersPerTag.tagsSorted)
		{			
			if (this.tagRelation.corpusTagCompatibleWithLexiconTag(corpusTag,lexiconTag,false)) // problem: there may be multiple possibilities...
			{
				System.err.println("Try classifier for " + lexiconTag + " for " + wordform +  ", corpus tag="  + corpusTag);

				theFormHandler c =  new theFormHandler();
				classifiersPerTag.callback = c;
				classifiersPerTag.classifyLemma(wordform, lexiconTag, lexiconTag, false);
				//System.err.println(c.allLemmata);
				if (c.bestLemma != null)
				{
					lemma = c.bestLemma;
					lastRule = c.rule;

					lemmaLog.addToLog(wordform, lemma, lexiconTag, corpusTag, MatchType.Guesser);
					foundHeuristicMatch = true;
				}
			}  else
			{
				// System.err.println("Not compatible: " + corpusTag + tag);
			}
		}
		if (!foundHeuristicMatch)
		{
			lemmaLog.addToLog(wordform, wordform.toLowerCase(), "_none_", corpusTag, MatchType.Unknown);
		}
		lemmaCache.put(wordform, corpusTag, lemma);
		return lemma;
	}

	private String findLemmaWithTag(String wordform, String tag)
	{

		String bestGuess = wordform.toLowerCase();

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

	private String checkLexiconAndCache(String wordform, String corpusTag) 
	{
		String lemma = null;
		if ((lemma = lemmaCache.get(wordform,corpusTag)) != null)
		{
			return lemma;
		} else
		{
			boolean foundMatchInLexicon = false;
			Set<WordForm> lemmata = lexicon.findLemmata(wordform);
			if (lemmata == null)
				lemmata = lexicon.findLemmata(wordform.toLowerCase());

			if (lemmata != null)
			{
				// try without conversion first...
				for (WordForm w: lemmata)
				{
					if (tagRelation.corpusTagCompatibleWithLexiconTag(corpusTag, w.tag, false))
					{
						lemma = w.lemma;
						lemmaCache.put(wordform, corpusTag, w.lemma);
						lemmaLog.addToLog(wordform, w.lemma, w.tag, corpusTag, MatchType.Lexicon);
						foundMatchInLexicon = true;
						//break;
					}
				}

				if (!foundMatchInLexicon)
				{
					for (WordForm w: lemmata)
					{
						if (tagRelation.corpusTagCompatibleWithLexiconTag(corpusTag, w.tag, true))
						{
							lemma = w.lemma;
							lemmaCache.put(wordform, corpusTag, w.lemma);
							lemmaLog.addToLog(wordform, w.lemma, w.tag, corpusTag, MatchType.LexiconWithConversion);
							foundMatchInLexicon = true;
						}
					}
				}

				if (!foundMatchInLexicon && !corpusTag.contains("NOU-P"))
				{
					System.err.println("Word in lexicon, maar niet met passende tag.. " + wordform + ":" + corpusTag + " " + lemmata);
				}
			} 
		}
		return lemma;
	}

	protected void testWordform(WordForm wf, TestDutchLemmatizer t)
	{
		lastRule = null;
		String lemma = this.findLemmaWithTag(wf.wordform, wf.tag);

		if (lemma.equals(wf.lemma))
			t.nCorrect++;
		else if (true || wf.tag.startsWith("NOU"))
		{
			System.err.println("Error: " + wf + " --> " + lemma + "  " + lastRule);
		}
		t.nItems++;
		//checkResult(wf, answer, outcomes, t);
	}

	public static Tagger getTaggerLemmatizer(String taggingModel, String lexiconPath)
	{

		BasicTagger tagger = new BasicTagger();

		tagger.loadModel(taggingModel);

		InMemoryLexicon l = null;
		try
		{
			l = (InMemoryLexicon) TaggerFeatures.getNamedObject("tagLexicon");
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		if (l == null)
		{
			l = new InMemoryLexicon();
			l.readFromFile(lexiconPath);
		}

		// l should be obtained from the tagger....

		MultiplePatternBasedLemmatizer lemmatizer = 
				new MultiplePatternBasedLemmatizer();
		lemmatizer.train(l);


		ChainOfTaggers t = new ChainOfTaggers();
		t.addTagger(tagger);
		t.addTagger(lemmatizer);
		return t;
	}

	public void testOnLemmatizedCorpus(String corpusFileName)
	{
		String[] testCorpusAttributes = {"word", "cgnTag", "tag", "lemma"};
		SimpleCorpus testCorpus = new SimpleCorpus(corpusFileName, testCorpusAttributes);

		for (Context c: testCorpus.enumerate())
		{
			Map<String,String> m = this.apply(c);
			String wordform = c.getAttributeAt("word", 0);
			String corpusTag = c.getAttributeAt("tag", 0);
			String assignedLemma = m.get("lemma");

			String trueLemma = c.getAttributeAt("lemma", 0);
			boolean teltMee = trueLemma != null && trueLemma.length() > 0 && !trueLemma.equals("_");
			if (trueLemma == null)
				trueLemma = "null";

			boolean mismatch = teltMee && !trueLemma.equalsIgnoreCase(assignedLemma);
			String pm = trueLemma.equalsIgnoreCase(assignedLemma)?"+=":"!=";
			if (!teltMee)
				pm = "~";

			String mismatchType = "";
			if (mismatch)
			{
				mismatchType="OTHER";

				String cgnTag = c.getAttributeAt("cgnTag", 0);

				if (cgnTag.startsWith("WW"))
				{
					if (corpusTag.startsWith("NOU"))
					{
						mismatchType = "probableConversion-VRB-NOU";
					}
					if (corpusTag.startsWith("AA"))
					{
						mismatchType = "probableConversion-VRB-ADJ";
					}
				}

				if (cgnTag.startsWith("ADJ"))
				{
					if (corpusTag.startsWith("NOU"))
					{
						mismatchType = "probableConversion-ADJ-NOU";
					}
				}

				if  (!corpusTag.matches("^(NOU|AA|VRB|NUM).*"))
				{
					//System.err.println(corpusTag);
					//System.exit(1);
					mismatchType = "probably-functionword-mismatch";
				}

				if (cgnTag.contains("dim"))
				{
					mismatchType = "probably-dim-issue";
				}
			}

			String extra = "";
			if (teltMee || mismatch)
			{
				extra = mismatchType +  " " + lemmaLog.getLoggedMatches(wordform, corpusTag) + "";
				if (extra.equals("null"))
				{
					System.err.println("No log information for " + wordform + ":" + corpusTag);
					//System.exit(1);
				}
			}

			System.out.println(
					c.getAttributeAt("word", 0) 
					+ "\t" + c.getAttributeAt("cgnTag", 0)
					+ "\t" + c.getAttributeAt("tag", 0)
					+ "\t" + assignedLemma + pm + trueLemma + "\t" + extra);
		}
	}

	public static void main(String[] args)
	{
		InMemoryLexicon l = new InMemoryLexicon();
		l.readFromFile(args[0]);

		boolean lexiconTest = true;
		if (args.length > 1)
			lexiconTest = false;
		MultiplePatternBasedLemmatizer lemmatizer = new MultiplePatternBasedLemmatizer();
		if (lexiconTest)
			lemmatizer.test(l);
		else
		{
			lemmatizer.train(l);
			lemmatizer.testOnLemmatizedCorpus(args[1]);
		}
	}
}
