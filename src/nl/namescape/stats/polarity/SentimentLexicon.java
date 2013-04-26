package nl.namescape.stats.polarity;

import nl.namescape.util.TabSeparatedFile;
import java.util.*;
public class SentimentLexicon 
{
	enum Polarity { PLUS, MINUS};
	String[] fields = {"word", "assessment1", "assessment2"};
	static String douman = "/mnt/Projecten/Taalbank/CL-SE-Data/Projecten/Biland/duoman_sent_lexicon_20090401/subj_assessments.txt";
	
	Map<String,Polarity> map = new HashMap<String,Polarity>();
	
	public SentimentLexicon(String fileName) 
	{
		// TODO Auto-generated constructor stub
		readDuomanFile(fileName);
	}

	public Polarity getPolarity(String word)
	{
		return map.get(word);
	}
	
	public void readDuomanFile(String fileName)
	{
		TabSeparatedFile tsf = new TabSeparatedFile(fileName, fields);
		while ((tsf.getLine() != null))
		{
			String word = tsf.getField("word");
			String[] parts = word.split("\\s+");
			word = parts[0];
			String assessment = tsf.getField("assessment1");
			if (assessment.contains("+"))
				map.put(word, Polarity.PLUS);
			if (assessment.contains("-"))
				map.put(word, Polarity.MINUS);
		}
	}
}
