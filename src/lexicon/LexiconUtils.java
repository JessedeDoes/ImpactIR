package lexicon;

import java.io.FileNotFoundException;
import java.io.IOException;

import trie.DoubleArrayTrie;
import util.Options;

/*
 * create a new directory (per language) containing
 * The pattern files (patterns.txt)
 * Subdirectories for modern lexicon and historical lexicon database
 * Trie for modern lexicon
 */

public class LexiconUtils 
{
	public static boolean prepareLexiconData(String targetDirectory, String patternFilename, 
			String modernLexiconFilename,
			String historicalLexiconFilename)
	{
		NeoLexicon modernLexicon = new NeoLexicon(targetDirectory + "/ModernLexicon", true);
		modernLexicon.readWordsFromFile(modernLexiconFilename);
		
		NeoLexicon historicalLexicon = new NeoLexicon(targetDirectory + "/HistoricalLexicon", true);
		historicalLexicon.readWordsFromFile(historicalLexiconFilename);
		
		DoubleArrayTrie dat = new DoubleArrayTrie();
		for (WordForm w: modernLexicon)
		{
			dat.add(w.wordform);
		}
		//dat.readWordsFromFile(modernWordList); // neen...
		try 
		{
			dat.saveToFile(targetDirectory + "/modernWords.datrie");
		} catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//dat.r
		return true;
	}
	
	public static WordForm getWordformFromLine(String s) 
	{
		String[] parts = s.split("\t");
		
		WordForm w = new WordForm();
		w.wordform = parts[0]; 
		w.lemma = parts[1];
		w.tag = parts[2];
	
		if (parts.length > 3)
		{
			w.lemmaPoS = parts[3];
		} else
		{
			w.lemmaPoS = w.tag;
		}	
		try
		{
			if (parts.length > 4)
			{
				w.lemmaFrequency = Integer.parseInt(parts[4]);
				if (w.lemmaFrequency > 10000)
				{
					System.err.println(s);
					System.exit(1);
				}
			}
			if (parts.length > 5)
				w.wordformFrequency = Integer.parseInt(parts[5]);
		} catch (Exception e)
		{
			//e.printStackTrace();
		}
		return w;
	}

	public static void main(String [] args)
	{
		Options options = new Options(args);
		String spanishDir = "c:/IREval/Data/Spanish";
		LexiconUtils.prepareLexiconData(
				Options.getOption("targetDirectory", "C:/Temp/SpanishLexicon"), 
				Options.getOption("patternInput", spanishDir + "/patterns.txt"), 
				Options.getOption("modernLexicon", spanishDir + "/ModernLexicon.txt"), 
				Options.getOption("historicalLexicon", spanishDir + "/HistoricalLexicon.txt"));
		
	}
}
