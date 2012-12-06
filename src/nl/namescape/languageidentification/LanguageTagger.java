package nl.namescape.languageidentification;
import nl.namescape.evaluation.Counter;
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

/**
 * Simple paragraph-level language identification<br>
 * Now using cybozu labs lamguage identification
 * <p>
 * Problems:
 *<p>
 * Not very good at short paragraphs<br>
 * Uppercase-only text should be lowercased (else almost always recognized as german)
 * 
 * @author does
 *
 */
public class LanguageTagger implements SimpleInputOutputProcess
{
	
	static
	{
		try 
		{
			DetectorFactory.loadProfileFromJar();
			List<String> langs = DetectorFactory.getLangList();
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
			// e.printStackTrace();
			return null;
		}
	}

	public String tagLanguages(Document d)
	{
		Counter<String> c = new Counter<String>();
		Set<Element> paragraphLike = TEITagClasses.getSentenceSplittingElements(d);
		int L=0;
		for (Element  z: paragraphLike)
		{
			// System.err.println(z);
			
			String s = z.getTextContent();
			L += s.length();
			
			// System.err.println("paragraph content: " + s);
			
			String lang = detectLanguage(s);
			
			if (lang != null)
			{
				if (lang.equals("af"))
					lang = "nl";
				z.setAttribute("xml:lang", lang);
				c.increment(lang,s.length());
				
				if (!lang.equalsIgnoreCase("nl") && s.length() > 100)
				{
					// System.err.println(lang + " IN " + s);
				}
			} else
			{	
				// System.err.println("No language found for " + s);
			}
		}
		
		String mainLanguage = "unknown";
		
		for (String lang: c.keyList())
		{
			System.err.println(lang + "\t"  + c.get(lang));
			if (c.get(lang) > 0.5  *L)
			{
				mainLanguage = lang;
				d.getDocumentElement().setAttribute("xml:lang", mainLanguage);
				if (!lang.equalsIgnoreCase("nl"))
				{
					System.err.println("Document has nondutch main lang: "  + lang);
				}
			}
		}
		
		if (mainLanguage.equals("unknown"))
		{
			System.err.println("No main language found! Text length in chars: " + L);
		}
		return mainLanguage;
	}
	
	@Override
	public void handleFile(String in, String out) 
	{
		
		Document d = null;
		try 
		{
			d = XML.parse(in);
			String main = tagLanguages(d);
			if (!main.equals("nl"))
			{
				System.err.println("Nondutch doc: " + main + " : "  + in);
			}
		} catch (Exception e) 
		{
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
