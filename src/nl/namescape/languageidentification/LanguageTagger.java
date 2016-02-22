package nl.namescape.languageidentification;
import nl.namescape.evaluation.Counter;
import nl.namescape.sentence.JVKSentenceSplitter;
import nl.namescape.sentence.TEISentenceSplitter;
import nl.namescape.tagging.ImpactTaggingClient;
import nl.namescape.tei.TEITagClasses;
import nl.namescape.tokenizer.TEITokenizer;
import nl.namescape.util.Options;
import nl.namescape.util.XML;
import nl.openconvert.filehandling.DirectoryHandling;
import nl.openconvert.filehandling.SimpleInputOutputProcess;

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
 * Now using cybozu labs language identification
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
	static String[] priorLanguages = {"nl", "en", "de", "fr", "it", "es"};
	static double[] priorProbabilities = {0.98, 0.02, 0.02, 0.02, 0.01, 0.01};
	static HashMap<String,Double> priorMap  = new HashMap<String,Double>();
	String MainLanguage = "en"; // nl
	boolean usePriors = false;
	boolean tagNTokens = true;

	private Properties properties;
	
	static
	{
		try 
		{
			DetectorFactory.loadProfileFromJar();
			List<String> langs = DetectorFactory.getLangList();
			setPriorProbabilities(langs);
		} catch (LangDetectException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	protected static void setPriorProbabilities(List<String> langs)
	{
		double sum=0;
		for (int i=0; i < priorLanguages.length; i++) 	
			sum += priorProbabilities[i];
			
		for (int i=0; i < priorLanguages.length; i++) 
		{
			String lang= priorLanguages[i];
			priorMap.put(lang,priorProbabilities[i] / sum);
		}
		for (String lang: langs)
		{
			if (priorMap.get(lang) == null)
				priorMap.put(lang,0.0);
		}
	}


	public String detectLanguage(String s)
	{
		try
		{
			Detector detector = DetectorFactory.create();
			if (usePriors)
				detector.setPriorMap(priorMap);
			detector.append(s);
			String lang = detector.detect();
			//detector.getProbabilities();
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
		int L = 0;
		int totalTokens = 0;
		for (Element  z: paragraphLike)
		{
			// nl.openconvert.log.ConverterLog.defaultLog.println(z);
			
			String s = z.getTextContent();
			L += s.length();
			
			// nl.openconvert.log.ConverterLog.defaultLog.println("Paragraph content: " + s);
			
			String lang = detectLanguage(s);
			int nTokens = TEITagClasses.getWordElements(z).size();
			totalTokens += nTokens;
			if (this.tagNTokens)
				z.setAttribute("n", new Integer(nTokens).toString());
			if (lang != null)
			{
				if (lang.equals("af"))
					lang = "nl";
				z.setAttribute("xml:lang", lang);
				c.increment(lang,s.length());
				
				if (!lang.equalsIgnoreCase(MainLanguage) && s.length() > 100)
				{
					// nl.openconvert.log.ConverterLog.defaultLog.println(lang + " IN " + s);
				}
			} else
			{	
				// nl.openconvert.log.ConverterLog.defaultLog.println("No language found for " + s);
			}
		}
		
		String mainLanguage = "unknown";
		
		for (String lang: c.keyList())
		{
			nl.openconvert.log.ConverterLog.defaultLog.println(lang + "\t"  + c.get(lang));
			if (c.get(lang) > 0.5  *L)
			{
				mainLanguage = lang;
				d.getDocumentElement().setAttribute("xml:lang", mainLanguage);
				if (!lang.equalsIgnoreCase(MainLanguage))
				{
					nl.openconvert.log.ConverterLog.defaultLog.println("Document has nondutch main lang: "  + lang);
				}
			}
		}
		
		if (mainLanguage.equals("unknown"))
		{
			nl.openconvert.log.ConverterLog.defaultLog.println("No main language found! Text length in chars: " + L);
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
			if (!main.equals(MainLanguage))
			{
				nl.openconvert.log.ConverterLog.defaultLog.println("Nondutch doc: " + main + " : "  + in);
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

	@Override
	public void setProperties(Properties properties) 
	{
		// TODO Auto-generated method stub
		this.properties = properties;
	}


	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
}
