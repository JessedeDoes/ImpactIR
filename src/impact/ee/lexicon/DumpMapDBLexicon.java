package impact.ee.lexicon;

public class DumpMapDBLexicon
{
	public static void main(String[] args)
	{
		MapDBLexicon l = new MapDBLexicon(args[0]);
		for (WordForm w: l.getAllWords())
		{
			System.out.println(w);
		}
	}
}
