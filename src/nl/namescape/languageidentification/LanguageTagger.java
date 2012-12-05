package nl.namescape.languageidentification;
import nl.namescape.filehandling.DirectoryHandling;
import nl.namescape.filehandling.SimpleInputOutputProcess;
import nl.namescape.sentence.JVKSentenceSplitter;
import nl.namescape.sentence.TEISentenceSplitter;
import nl.namescape.tagging.ImpactTaggingClient;
import nl.namescape.tei.TEITagClasses;
import nl.namescape.tokenizer.TEITokenizer;
import nl.namescape.util.Options;
import nl.namescape.util.XML;

import org.w3c.dom.*;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

import impact.ee.tagger.BasicTagger;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;

public class LanguageTagger implements SimpleInputOutputProcess
{
	static
	{
		try 
		{
			DetectorFactory.loadProfileFromJar();
		} catch (LangDetectException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public String detectLanguage(String s)
	{
		try
		{
			Detector detector = DetectorFactory.create();
			detector.append(s);
			String lang = detector.detect();
			return lang;
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public void tagLanguages(Document d)
	{
		Set<Element> paragraphLike = TEITagClasses.getSentenceSplittingElements(d);
		for (Element  z: paragraphLike)
		{
			String s = z.getTextContent();
			System.err.println("paragraph content: " + s);
			String lang = detectLanguage(s);
			if (lang != null)
				z.setAttribute("lang", lang);
		}
	}
	
	@Override
	public void handleFile(String in, String out) 
	{

		Document d = null;
		
			try 
			{
				d = XML.parse(in);
				tagLanguages(d);
			} catch (Exception e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			} 
		
		try 
		{
			PrintStream pout = new PrintStream(new FileOutputStream(out));
			
			
			pout.print(XML.documentToString(d));
			pout.close();
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		
		LanguageTagger xmlTagger = new LanguageTagger();
		DirectoryHandling.tagAllFilesInDirectory(xmlTagger, args[0], args[1]);
	}
}
