package impact.ee.lexicon;
public class Dump
{
 	public static void main(String[] args)
	{
		 
                       NeoLexicon l = new NeoLexicon(args[0], false);
                       l.dumpDB();
	}
}
