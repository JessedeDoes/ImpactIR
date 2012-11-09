package impact.ee.lexicon;

public class CreateSimpleAnalyzedWordforms 
{

		public static void main(String[] args)
		{
			LexiconDatabase l = new LexiconDatabase(args[0], args[1]);
			l.createSimpleAnalyzedWordformTable();
		}
}
