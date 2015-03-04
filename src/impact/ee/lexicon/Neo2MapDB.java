package impact.ee.lexicon;
public class Neo2MapDB
{
	public static void main(String[] args)
	{

		NeoLexicon l = new NeoLexicon(args[0], false);
		MapDBLexicon ml = new MapDBLexicon(args[1]);
		for (WordForm w: l)
		{
			ml.addWordform(w);
		}
		ml.close();
	}
}
