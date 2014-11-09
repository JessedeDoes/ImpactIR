package impact.ee.lexicon;
public class ListLookup
{


 	public static void main(String[] args)
	{
		 
                       NeoLexicon l = new NeoLexicon(args[0], false);
                       l.lookupLemmataFromFile(args[1]);
	}
}
