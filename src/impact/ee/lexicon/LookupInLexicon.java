package impact.ee.lexicon;

public class LookupInLexicon 
{
	public static void main(String[] args)
	{
		MapDBLexicon m = new MapDBLexicon(args[0]);
		System.out.println(m.findLemmata(args[1]));
	}
}
