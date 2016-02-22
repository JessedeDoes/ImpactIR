package impact.ee.lexicon;
import java.util.*;

public class TestMapDB 
{
	public static void main(String[] args)
	{
		MapDBLexicon l1 = new MapDBLexicon("/tmp/lex1");
		l1.readFromFile("resources/exampledata/molexDump.txt");
		l1.close();
		InMemoryLexicon iml = new InMemoryLexicon();
		iml.readFromFile("resources/exampledata/molexDump.txt");
		
		
		MapDBLexicon l2 = new MapDBLexicon("/tmp/lex1");
		for (WordForm w: iml)
		{
			Set<WordForm> V = l2.findLemmata(w.wordform);
			if (!V.contains(w))
			{
				nl.openconvert.log.ConverterLog.defaultLog.println("Missing: " + w);
			}
		}
	}
}
