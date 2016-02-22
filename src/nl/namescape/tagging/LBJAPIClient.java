package nl.namescape.tagging;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import lbj.NETaggerLevel1;
import lbj.NETaggerLevel2;

import nl.namescape.tokenizer.TEITokenizer;
import nl.namescape.util.StopWatch;
import nl.namescape.util.XML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import LBJ2.classify.Classifier;
import LBJ2.nlp.Word;
import LBJ2.parse.LinkedVector;
import LbjTagger.NETester;
import LbjTagger.NEWord;
import LbjTagger.Parameters;
import LbjTagger.Reuters2003Parser;

public class LBJAPIClient 
{

	Map<String, NEWord> wordMap = new HashMap<String, NEWord>();
	NETaggerLevel1 tagger1;
	NETaggerLevel2 tagger2;

	public LBJAPIClient(boolean loadModel)
	{
		if (loadModel) init();
	}

	public void tagXMLFileUsingLBJNerTaggerAPI(String inputFile)
	{
		System.out.println("Tagging file: "+ inputFile);
		StopWatch sw = new StopWatch();
		sw.start();
		Document d = new TEITokenizer().getTokenizedDocument(inputFile, false);
		sw.stop();
		nl.openconvert.log.ConverterLog.defaultLog.println("document tokenized: " + sw.getElapsedTimeSecs());
		sw.start();
		Vector<LinkedVector> data= parseXML(d);
		sw.stop();
		
	
		nl.openconvert.log.ConverterLog.defaultLog.println("tokenized document parsed: " + sw.getElapsedTimeSecs());
	
		/*
		 * Hier starten we de eigenlijke NER
		 */
	
		sw.start();
		NETester.annotateBothLevels(data, tagger1, tagger2); 
		sw.stop();
		nl.openconvert.log.ConverterLog.defaultLog.println("NER performed: " + sw.getElapsedTimeSecs());
	
		/*
		 * De NE tagging zit in de NEWords die we in de tagger gestop hebben
		 * Hevel het hier over in de <w> tags.
		 */
		for (Element w: XML.getElementsByTagname(d.getDocumentElement(), "w", false))
		{
			NEWord z = wordMap.get(w.getAttribute("id"));
			if (z != null)
			{
				//nl.openconvert.log.ConverterLog.defaultLog.println(z);
				w.setAttribute("neLabel", z.neTypeLevel2);
			}
		}
		// ToDo revive postprocessing
		/*
		tagRanges(d);
		addNyms(d);
		for (Element w: ParseUtils.getElementsByTagname(d.getDocumentElement(), "w", false))
		{
			w.removeAttribute("neLabel");
		}
		*/
		String s = XML.documentToString(d);
		System.out.println(s);
	}

	public void init() // config file location and data and model location
	{
		tagger1 = new NETaggerLevel1();
		System.out.println("Reading model file : "+ Parameters.pathToModelFile+".level1");
		tagger1= (NETaggerLevel1) Classifier.binaryRead(Parameters.pathToModelFile+".level1");
		tagger2 = new NETaggerLevel2();
		System.out.println("Reading model file : "+ Parameters.pathToModelFile+".level2");
		tagger2= (NETaggerLevel2) Classifier.binaryRead(Parameters.pathToModelFile+".level2");
	}

	/**
	 * Apparently we DO need sentence splitting in some form or other for decent NER
	 * Bah.
	 * @param d
	 * @return
	 */
	
	public Vector<LinkedVector> parseXML(Document d)
	{
		int i=0;
		wordMap.clear();
		Vector<LinkedVector> res=new Vector<LinkedVector>();
	
		for (Element p: XML.getElementsByTagname(d.getDocumentElement(), "s", false))
		{
			LinkedVector paragraphText = new LinkedVector();
			for (Element w: XML.getElementsByTagname(p, "w", false))
			{
				String token = w.getTextContent();
				NEWord word = new NEWord(new Word(token), null, "unlabeled");
				Vector<NEWord> v = null;
				
				try
				{
					v = Reuters2003Parser.splitWord(word);
				} catch (Exception e)
				{
					nl.openconvert.log.ConverterLog.defaultLog.println("error parsing token " + token + " word: "  + word);
					e.printStackTrace();
					continue;
				}
	
				word.parts= new String[v.size()];
	
				for(int j=0;j<v.size();j++)                                       
				{
					word.parts[j] = v.elementAt(j).form;
				}
				
				paragraphText.add(word);
				wordMap.put(w.getAttribute("id"), word);
				i++;
			}
			res.add(paragraphText);
		}
		return res;  
	}

}
