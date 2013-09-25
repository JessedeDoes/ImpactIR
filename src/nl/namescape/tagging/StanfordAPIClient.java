package nl.namescape.tagging;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import nl.inl.impact.ner.stanfordplus.ImpactCRFClassifier;
import nl.namescape.filehandling.DirectoryHandling;
import nl.namescape.tei.TEITagClasses;
import nl.namescape.util.Options;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;

public class StanfordAPIClient extends DocumentTagger implements SentenceTagger 
{
	boolean english = false;
	public StanfordAPIClient(SentenceTagger st) 
	{
		super(st);
		// TODO Auto-generated constructor stub
	}

	public StanfordAPIClient()
	{
		super();
		this.sentenceTagger = this;
	}
	
	boolean useTags = false; // should be true... (?)
	List<AbstractSequenceClassifier> 
		listOfTaggers = new ArrayList<AbstractSequenceClassifier>();
	
	public void addClassifier(String fileName)
	{
		try 
		{
			if (fileName.contains("english"))
				english = true;
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
			//if (!"O".equalsIgnoreCase(parts[1]))
			//	System.err.println(parts[0] + " " + parts[1]);
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
		if (english)
		{
			List<Element> sentences = TEITagClasses.getSentenceElements(d);
			for (Element s: sentences)
			{
				List<Element> tokens = TEITagClasses.getTokenElements(s);
				String previousLabel="";
				for (Element t: tokens)
				{
					String neLabel = t.getAttribute("neLabel").toLowerCase();
					if (neLabel.equalsIgnoreCase("organization"))
						neLabel = "organisation";
					if (neLabel == null || neLabel.equals(""))
						neLabel = "O";
					if (!neLabel.equalsIgnoreCase("O"))
					{
						System.err.println(neLabel);
						if (previousLabel.equals(neLabel))
						{
							t.setAttribute("neLabel", "I-" + neLabel);
						} else
						{
							t.setAttribute("neLabel", "B-" + neLabel);
						}
					}
					previousLabel = neLabel;
				}
			}
		}
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
	
	public void setProperties(Properties p)
	{
		this.properties = p;
		String model = properties.getProperty("model");
		
		
		this.tokenize = !("false".equalsIgnoreCase(properties.getProperty("tokenize")));
		this.splitSentences = ("true".equalsIgnoreCase(properties.getProperty("splitSentences")));
		addClassifier(model);
	}
	
	public static void main(String[] args)
	{
		nl.namescape.util.Options options = 
				new nl.namescape.util.Options(args);
		args = options.commandLine.getArgs();
		StanfordAPIClient stan = new StanfordAPIClient();

		String classifier, input, output;
		if (args.length == 2)
		{
			classifier = "N:/Taalbank/Namescape/Tools/stanford-ner-2012-07-09/classifiers/english.all.3class.distsim.crf.ser.gz";
			input = args[0];
			output = args[1];
		} else
		{
			classifier = args[0];
			input = args[1];
			output = args[2];
		}
		stan.addClassifier(classifier);
		DocumentTagger dt = new DocumentTagger(stan);
		dt.tokenize = options.getOptionBoolean("tokenize", true);
		dt.splitSentences = options.getOptionBoolean("sentences", false);
		DirectoryHandling.tagAllFilesInDirectory(stan, input, output);
	}
}
