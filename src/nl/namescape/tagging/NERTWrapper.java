package nl.namescape.tagging;

import java.util.Properties;


import nl.namescape.filehandling.DirectoryHandling;

public class NERTWrapper
{
	public static void main(String[] args)
	{
		
		StanfordAPIClient stan = new StanfordAPIClient();

		String classifier, input, output;
		Properties p = edu.stanford.nlp.util.StringUtils.argsToProperties(args);
		input = p.getProperty("input");
		output = p.getProperty("output");
		//p.setProperty("loadClassifier",)
		stan.addNERTClassifier(p);
		//stan.addClassifier(classifier);
		DocumentTagger dt = new DocumentTagger(stan);
		dt.tokenize = true; //options.getOptionBoolean("tokenize", true);
		dt.splitSentences = true; //options.getOptionBoolean("sentences", false);
		DirectoryHandling.tagAllFilesInDirectory(stan, input, output);
	}
}
