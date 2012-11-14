package impact.ee.lexicon;
public class Lookup
{


 	public static void main(String[] args)
	{
		 
                       NeoLexicon l = new NeoLexicon(args[0], false);
                       l.lookupWord(args[1]);
	}
}
