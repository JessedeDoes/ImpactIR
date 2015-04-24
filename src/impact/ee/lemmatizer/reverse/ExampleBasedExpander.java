package impact.ee.lemmatizer.reverse;
import java.io.*;
import java.util.*;

import impact.ee.lemmatizer.PrefixSuffixGuesser;
import impact.ee.lexicon.*;

/*
 * This is nonsense... 
 */
public class ExampleBasedExpander
{
	public static  void expandFromFile(String exampleFile, String toExpand, ParadigmExpander pe)
	{
		InMemoryLexicon allExamples = new InMemoryLexicon();
		allExamples.readFromFile(exampleFile);
		WordForm currentWordForm = null;
		try
		{
			BufferedReader b = new BufferedReader(new FileReader(toExpand));
			String line;

		
			while (( line= b.readLine())!=null)
			{
				String[] fields = line.split("\t");
				InMemoryLexicon rl = new InMemoryLexicon();
				WordForm w = new WordForm();
				w.lemma = fields[1];
				w .wordform= null;
				w.lemmaPoS = fields[3];
				w.tag = fields[2];

				Set<WordForm> examples = allExamples.findForms(fields[0], null);
				if (examples != null)
				{
					for (WordForm e: examples)
					{	
						//System.err.println(e);
						rl.addWordform(e);
					}
					pe = new PrefixSuffixGuesser();
					pe.findInflectionPatterns(rl, null);
					for (WordForm e: examples)
					{
						w.tag = e.tag;
						currentWordForm = w;
						pe.expandWordForm(w);
					}
				} else
				{
					System.err.println("Nee, niks gevonden voor voorbeeldlemma " + fields[0]);
				}
			}
		} catch (Exception e)
		{
			System.err.println("Error expanding " + currentWordForm);
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		ExampleBasedExpander.expandFromFile(args[0], args[1], new PrefixSuffixGuesser());
	}
}
