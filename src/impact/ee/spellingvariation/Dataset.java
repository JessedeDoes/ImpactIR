package impact.ee.spellingvariation;
import impact.ee.spellingvariation.AlignmentSegmenter.SegmentationGraph;

import java.io.*;
import java.util.Iterator;
import java.util.Vector;





class Candidate
{
	String wordform = null;
	double lambda;
	double frequency;
	Dataitem item = null;
	Alphabet.CodedString coded_wordform;
	Alignment.AlignmentGraph alignmentGraph;
	AlignmentSegmenter.SegmentationGraph segmentationGraph;
}

class Dataitem
{
	public String lemma;
	public Candidate best_match;
	public Vector<Candidate> candidates;
	public String target;
	public Alphabet.CodedString  coded_target; 
  boolean heldOut = false;
	public Dataitem() 
	{
		candidates = new Vector<Candidate>();
	};
};


/** A Dataset is a set consisting of items, where an
 * item consists of a lemma, a list of known wordforms and a target wordform
 * to be matched to the know ones.
 * The tab-separated representation is lemma&lt;TAB>space-separated-wordforms&lt;TAB>target wordform
 */

public class Dataset implements Iterable<Dataitem>
{
	public Alphabet input_alphabet;
	public Alphabet output_alphabet;
	protected Vector<Dataitem> items= new Vector<Dataitem>();
	boolean has_frequency = false;
	double  lambda(int i, int j) { return items.get(i).candidates.get(j).lambda;};
	double  frequency(int i, int j) { return items.get(i).candidates.get(j).frequency; };
	String wordform(int i, int j) { return items.get(i).candidates.get(j).wordform; } ;
	Dataset() { has_frequency = false; items = new Vector<Dataitem>(); };
	public int size() 
	{ return items.size(); }
	public boolean addWordBoundaries = false;

	//lemma <TAB>  wordforms <TAB> target

	public java.util.Iterator<Dataitem> iterator() 
	{
		return items.iterator();
	}
	
	protected Dataitem get(int i)
	{
		return items.get(i);
	}

	public int read_from_file(String filename)
	{
		try
		{
			BufferedReader f = new BufferedReader(new FileReader(filename));
			input_alphabet = new Alphabet("");
			output_alphabet = new Alphabet("");
			String s;
			while ((s = f.readLine()) != null) 
			{
				String[] t1 = s.split("\t");
				if (t1.length < 3)
				{
					nl.openconvert.log.ConverterLog.defaultLog.println("DATA ERROR: " + s);
					continue;
				}
				Dataitem item  = new Dataitem();
				item.lemma  = t1[0];
				

				
				item.target = t1[2];
				if (addWordBoundaries)
				{
					item.target = Alphabet.initialBoundary + item.target + Alphabet.finalBoundary;
				}
				
				// fwprintf(stderr, L"%ls\n", item.target);
				
				output_alphabet.addSymbolsFrom(item.target);
				item.coded_target = output_alphabet.encode(item.target);
				String[] t2 = t1[1].split(" ");

				for (int i=0; i < t2.length; i++) 
				{
					Candidate c = new Candidate();
					item.candidates.add(c);
					String wf = t2[i];
					if (has_frequency)
					{
						String[] t3 = wf.split(":");
						if (t3.length > 1)
						{
							wf = t3[0];
							double d = Double.parseDouble(t3[1]);
							c.frequency = d;
						}
					}
					if (addWordBoundaries)
					{
						wf = Alphabet.initialBoundary + wf + Alphabet.finalBoundary;
					}
					c.wordform = wf;
					input_alphabet.addSymbolsFrom(wf);
					c.coded_wordform = input_alphabet.encode(wf);
					double d = t2.length; d = 1/d;
					c.lambda = d;
					c.item = item;
				}
				addItem(item);
			}
			return 0;
		} catch (Exception e)
		{
			String currentDirectory = System.getProperty("user.dir");
			nl.openconvert.log.ConverterLog.defaultLog.println("Unable to read file " + filename);
			nl.openconvert.log.ConverterLog.defaultLog.println("Current directory: " + currentDirectory);
			e.printStackTrace();
			return 1;
		}
	}
	public void addItem(Dataitem item) {
		items.addElement(item);
	}
	
	class SegmentationGraphIterator implements Iterator<SegmentationGraph> 
	{
		int itemNumber=0;
		int candidateNumber=0;
		
		public boolean hasNext()
		{
			return (itemNumber < items.size()) && 
			  (itemNumber < items.size() -1 || 
			  		candidateNumber < get(itemNumber).candidates.size());
		}

		public SegmentationGraph next()
		{
			
			Dataitem item = get(itemNumber);
			if (candidateNumber < item.candidates.size())
			{
				return item.candidates.get(candidateNumber++).segmentationGraph;
			}
			itemNumber++;
			if (itemNumber < size())
			{
				candidateNumber=0;
				return get(itemNumber).candidates.get(candidateNumber++).segmentationGraph;
			}
			return null;
		}

		public void remove()
		{
			nl.openconvert.log.ConverterLog.defaultLog.println("remove for this class not implemented");
			// does nothing!
		}
	}
	
	public Iterator<SegmentationGraph> getGraphIterator()
	{
		return new SegmentationGraphIterator();
	}
}
