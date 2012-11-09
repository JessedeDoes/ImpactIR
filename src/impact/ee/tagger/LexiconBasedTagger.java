package impact.ee.tagger;

import impact.ee.classifier.libsvm.LibSVMClassifier;
import impact.ee.classifier.svmlight.SVMLightClassifier;
import impact.ee.lexicon.LexiconDatabase;
import impact.ee.lexicon.QuotationCorpus;
import impact.ee.lexicon.QuotationCorpus.inSample;
import impact.ee.lexicon.QuotationCorpus.inTraining;
import impact.ee.tagger.features.HasPoSFeature;
import impact.ee.tagger.features.TaggerFeatures;

import java.util.HashSet;
import java.util.Set;


/**
 * 
 * Try to use the attestations in an IMPACT lexicon as training data for a tagger.
 * <p>
 * @author does
 *
 *bijdrage context? Met unigram features halen we ruim 19% fout (met hele sample set 
 *in de training)
 *Met alleen de omliggende woorden: we gaan naar 17% (slechts iets beter dus)
 *met lexiconPoS van omliggende woorden: ietsje beter (16.9%) maar echt niet veel!
 *Beperkt tot "unknown words" zitten we nog op 18.5 %, wat wellicht redelijk
 *is gezien de aard van het materiaal...
 *<p>
 *Totaal training materiaal ongeveer 400.000 tokens
 *svmlight tagger heeft er veel problemen mee...
 */
public class LexiconBasedTagger extends BasicTagger
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public LexiconBasedTagger()
	{
		SVMLightClassifier svm = new SVMLightClassifier();
		svm.trainingMethod = SVMLightClassifier.TrainingMethod.ONE_VS_ALL_EXTERNAL;
		this.classifier = svm; // new LibSVMClassifier();
		features = TaggerFeatures.getBasicNERFeatures(false);
		//features.addStochasticFeature(new LexiconBasedFeature.HasPoSFeature(0));
		features.addStochasticFeature(new HasPoSFeature(-1));
		features.addStochasticFeature(new HasPoSFeature(1));
	}
	
	@Override
	protected boolean filter(Context c)
	{
		String tag = c.getAttributeAt("tag", 0);
		if (!tag.matches(".*[A-Za-z0-9].*"))
			return false;
		if (tag.contains(" "))
		{
			tag = tag.replaceAll(" ", "_");
			c.setAttributeAt("tag", tag, 0);
			//String check = c.getAttributeAt("tag", 0);
			//System.err.println(tag + " " + check);
			return (tag.equals("ADJ_ADJ") || tag.equals("ADJ_ADV"));
		} else
		return true;
	}
	
	public static void main(String[] args)
	{
		LexiconBasedTagger testje = new LexiconBasedTagger();
		testje.proportionOfTrainingToUse = 1;
		
		LexiconDatabase ee35 = new LexiconDatabase("svowim02", "EE3_5");
		boolean doTraining = true;
		if  (doTraining)
		{
			Corpus trainingCorpus = 
					ee35.getQuotationCorpus(inTraining.inTrainingSet, inSample.selectAll);
			testje.train(trainingCorpus);
			testje.saveModel("Models/lexicalTagger");
		} else
		{
			// OK dat werkt niet, want de features zijn zo allemaal onbekend...
			// de featureset moet serializable gemaakt worden hetgeen zeer bah
			testje.loadModel("Models/lexicalTagger");
		}
		
		
		Corpus testCorpus = 
				ee35.getQuotationCorpus(inTraining.inTestSet, inSample.inSampleSet);
		
		//testje = new LexiconBasedTagger();
		testje.loadModel("Models/lexicalTagger");
		testje.test(testCorpus);
	}
}
