package impact.ee.lexicon;

import impact.ee.util.Options;

public class PrepareLexicon 
{
	public static void main(String [] args)
	{
		new Options(args);
		String spanishDir = "c:/IREval/Data/Spanish";
		LexiconUtils.prepareLexiconData(
				Options.getOption("targetDirectory", "C:/Temp/SpanishLexicon"), 
				Options.getOption("patternInput", spanishDir + "/patterns.txt"), 
				Options.getOption("modernLexicon", spanishDir + "/ModernLexicon.txt"), 
				Options.getOption("historicalLexicon", spanishDir + "/HistoricalLexicon.txt"));

	}
}
