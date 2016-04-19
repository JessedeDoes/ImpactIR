package nl.namescape.stats;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import nl.namescape.stats.WordList.TypeFrequency;
import nl.openconvert.filehandling.DirectoryHandling;
import nl.openconvert.filehandling.MultiThreadedFileHandler;
import nl.openconvert.util.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class TFIDFStuff implements nl.openconvert.filehandling.DoSomethingWithFile
{
	WordList global = new WordList();
	WordList documentFrequency = new WordList();
	Map<String,WordList> wordListMap = new HashMap<String,WordList>();
	Map<String,LemmaInfo> lemmaMap = new   HashMap<String,LemmaInfo>();
	int nDocuments = 0;
	private int minSize = 250;
	private int minFrequency=2;
	private int maxDF=1500;
	private int nPrint=10;

	static class LemmaInfo
	{
		String id;
		String lemma;
		String mdl;
		List<String> pos = new ArrayList<String>();

		public String toString()
		{
			return id + "_" + lemma +  "_" + pos;
		}
	}

	public static LemmaInfo getLemmaInfo(Element e)
	{
		LemmaInfo li = new LemmaInfo();
		li.id = findID(e).trim();
		List<Element> l = XML.getElementsByTagnameAndAttribute(e, "form", "type", "lemma", false);
		if (l != null && l.size() > 0)
			li.lemma = l.get(0).getTextContent().trim();
		l = XML.getElementsByTagnameAndAttribute(e, "form", "type", "mdl", false);
		if (l != null && l.size() > 0)
			li.mdl = l.get(0).getTextContent().trim();
		l  = XML.getElementsByTagname(e, "gramGrp", false);

		if (l != null && l.size() > 0)
		{
			for (Element g: l)
			{
				li.pos.add(g.getTextContent().trim().replaceAll("\\s*", " "));
			}
		}
		System.err.println(li);
		return li;
	}

	public static String findID(Element e)
	{
		return e.getAttribute("id");
	}

	private void merge(WordList l)
	{
		for (TypeFrequency tf: l.keyList(false))
		{
			global.incrementFrequency(tf.type,tf.frequency);
			documentFrequency.incrementFrequency(tf.type,1);
		}
	}

	double tfIdf(String t, String did)
	{
		int df = documentFrequency.getFrequency(t);
		int tf = wordListMap.get(did).getFrequency(t);

		return tf * Math.log(nDocuments / (double) df);
	}

	static class ScoredWord
	{
		public ScoredWord(String type, double score)
		{
			this.w = type;
			this.score = score;
		}
		String w;
		double score;
	}

	static class ScoreCompare implements Comparator<ScoredWord>
	{

		@Override
		public int compare(ScoredWord arg0, ScoredWord arg1)
		{
			// TODO Auto-generated method stub
			return Double.compare(arg1.score, arg0.score);
		}
	}

	private void wrapUp()
	{
		for (String id: this.wordListMap.keySet())
		{
			List<ScoredWord> candidates = new ArrayList<ScoredWord>();
			LemmaInfo lemma = lemmaMap.get(id);
		
			WordList l = wordListMap.get(id);
			if (l.size() > minSize )
			{
				for (TypeFrequency tf : l.keyList(false))
				{
					if (tf.frequency > minFrequency)
					{
						double score = tfIdf(tf.type,id);
						int df = documentFrequency.getFrequency(tf.type);
						if (df <= maxDF)
						candidates.add(new ScoredWord(tf.type,score));
					}
				}

				Collections.sort(candidates, new ScoreCompare());
				
				if (candidates.size() > 0)
					System.out.println(lemma.toString());
				for (int i=0; i < nPrint && i < candidates.size(); i++)
				{
					String w = candidates.get(i).w;
					int df = documentFrequency.getFrequency(w);
					int tf = wordListMap.get(id).getFrequency(w);
					System.out.println(w + "\t" + candidates.get(i).score + "\t" + tf  + "\t"  + df);
				}
			} 
		}
	}

	public void stripBibl(Element e)
	{
		List<Element> bibls = XML.getElementsByTagname(e, "bibl", false);
		for (Element b: bibls)
		{
			//System.err.println("removing"  + "\t" + b.getTextContent());
			b.getParentNode().removeChild(b);
		}
	}
	
	public void handleFile(String fileName) 
	{
		try
		{
			Document d = XML.parse(fileName);
			List<Element> entries = XML.getElementsByTagname(d.getDocumentElement(), "entry", false);
			for (Element e: entries)
			{
				stripBibl(e);
				WordList l = nl.namescape.stats.MakeFrequencyList.makeList(e);
				merge(l);
				String id = findID(e);
				wordListMap.put(id, l);
				lemmaMap.put(id, getLemmaInfo(e));
				nDocuments++;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		TFIDFStuff x = new TFIDFStuff();
		//x.handleFile(args[0]);
		//x.wrapUp();
		MultiThreadedFileHandler m = new MultiThreadedFileHandler(x,1);
		if (args.length > 0)
		{
			for (String d: args)
				DirectoryHandling.traverseDirectory(m,d);
			m.shutdown();
			x.wrapUp();
		}
	}
}
