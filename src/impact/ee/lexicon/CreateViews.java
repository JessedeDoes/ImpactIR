package impact.ee.lexicon;

public class CreateViews 
{
	public static void main(String[] args)
	{
		LexiconDatabase l = new LexiconDatabase(args[0], args[1]);
		l.createViews();
	}
}
