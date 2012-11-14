package impact.ee.lexicon;

public class ModernHistoricalMatching 
{
	public static void main(String[] args)
	{
		LexiconUtils.prepareModernToHistoricalMatching(new NeoLexicon(args[0],false), 
				new NeoLexicon(args[1],false));
		
	}
}
