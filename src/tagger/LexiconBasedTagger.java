package tagger;

import tagger.features.LexiconBasedFeature;
import tagger.features.TaggerFeatures;
import lexicon.LexiconDatabase;
import lexicon.QuotationCorpus;
import lexicon.QuotationCorpus.inSample;
import lexicon.QuotationCorpus.inTraining;

public class LexiconBasedTagger extends BasicTagger
{
	
	public LexiconBasedTagger()
	{
		features = TaggerFeatures.getBasicFeatures(false);
		features.addStochasticFeature(new LexiconBasedFeature.HasPoSFeature(0));
		features.addStochasticFeature(new LexiconBasedFeature.HasPoSFeature(-1));
		features.addStochasticFeature(new LexiconBasedFeature.HasPoSFeature(1));
	}
	
	public static void main(String[] args)
	{
		LexiconBasedTagger testje = new LexiconBasedTagger();
		testje.proportionOfTrainingToUse = 0.1;
		
		LexiconDatabase ee35 = new LexiconDatabase("svowim02", "EE3_5");
		boolean doTraining = true;
		if  (doTraining)
		{
			Corpus trainingCorpus = ee35.getQuotationCorpus(inTraining.inTrainingSet, inSample.inSampleSet);
			testje.train(trainingCorpus);
			testje.saveModel("/tmp/lexicalTagger");
		} else
		{
			// ok dat werkt niet, want de features zijn zo allemaal onbekend...
			// de featureset moet serializable gemaakt worden hetgeen zeer bah
			testje.loadModel("Models/lexicalTagger");
		}
		
		
		Corpus testCorpus = ee35.getQuotationCorpus(inTraining.inTestSet, inSample.inSampleSet);
		
		testje = new LexiconBasedTagger();
		testje.loadModel("/tmp/lexicalTagger");
		testje.test(testCorpus);
	}
}
