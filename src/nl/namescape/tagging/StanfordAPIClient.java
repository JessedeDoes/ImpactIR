package nl.namescape.tagging;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.inl.impact.ner.stanfordplus.ImpactCRFClassifier;
import nl.namescape.util.Options;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;

public class StanfordAPIClient implements SentenceTagger 
{
	boolean useTags = false; // should be true... (?)
	List<AbstractSequenceClassifier> listOfTaggers = new ArrayList<AbstractSequenceClassifier>();
	
	public void addClassifier(String fileName)
	{
		try 
		{
			listOfTaggers.add(edu.stanford.nlp.ie.crf.CRFClassifier.getClassifier(new File(fileName)));
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public String tagString(String in) 
	{
		return StanfordSentenceTagging.tagSentence(in, listOfTaggers);
	}

	@Override
	public void tagWordElement(Element w, String line) 
	{
		try
		{
			String[] parts = line.split("\\s+");
			w.setAttribute("neLabel", parts[1]);
			if (!"O".equalsIgnoreCase(parts[1]))
				System.err.println(parts[0] + " " + parts[1]);
			if (parts.length > 2)
			{
				w.setAttribute("nePartLabel", parts[2]);
			}
		} catch (Exception e)
		{
			System.err.println(line);
			e.printStackTrace();
		}
	}

	@Override
	public void postProcessDocument(Document d) 
	{
		(new nl.namescape.tei.TEINameTagging()).realizeNameTaggingInTEI(d);
	}

	@Override
	public String tokenToString(Element t) 
	{
		String w = t.getTextContent();
		if (useTags)
		{
			String tag = t.getAttribute("neLabel");
			if (tag == null || tag=="")
				tag="O";
			return w + "\t" + tag;
		}
		return w;
	}
	
	public static void main(String[] args)
	{
		nl.namescape.util.Options options = 
				new nl.namescape.util.Options(args);
		args = options.commandLine.getArgs();
		
		StanfordAPIClient stan = new StanfordAPIClient();
		//stan.addClassifier("N:/Taalbank/Namescape/Tools/stanford-ner-2012-07-09/classifiers/english.conll.4class.distsim.crf.ser.gz");/
		stan.addClassifier("/mnt/Projecten/Taalbank/Namescape/Corpus-KB/Training/models/kranten.ser.gz");
		DocumentTagger dt = new DocumentTagger(stan);
		dt.tokenize = Options.getOptionBoolean("tokenize", true);
		dt.splitSentences = Options.getOptionBoolean("sentences", false);
		File f = new File(args[0]);
		if (f.isDirectory())
			dt.tagXMLFilesInDirectory(args[0], args[1]);
		else
			dt.tagXMLFile(args[0], args[1]);
	}
}
