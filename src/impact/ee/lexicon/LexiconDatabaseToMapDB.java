package impact.ee.lexicon;

public class LexiconDatabaseToMapDB 
{
	public static void main(String[] args)
	{
		MapDBLexicon l1 = new MapDBLexicon(args[0]);
		LexiconDatabase ldb = new LexiconDatabase(args[1],args[2]); // host, db
		ldb.useSimpleWordformsOnly = true;
		for (WordForm w: ldb)
		{
			l1.addWordform(w);
		}
		l1.close();
	}
}
