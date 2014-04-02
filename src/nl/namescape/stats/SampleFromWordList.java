package nl.namescape.stats;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.stat.regression.RegressionResults;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import scala.Math;

import nl.namescape.stats.WordList.TypeFrequency;

public class SampleFromWordList
{
	WordList baseList;
	BitSet sample = null;
	List<TypeFrequency> tl;
	List<Integer> sampleSizes = new ArrayList<Integer>();
	List<Integer>  vocabularySizes = new ArrayList<Integer>();
	SimpleRegression regression;

	public SampleFromWordList(WordList base)
	{
		baseList = base;
		System.err.println("read base list");
		tl = baseList.getTypeFrequencyList();
	}

	public void sample(int size)
	{
		Random generator = new Random(); 
		//int max = WordList.
		int max = 379 * 1000000;
		sample = new BitSet(max+1);

		int portionSize = max / size;

		boolean useRandom = true;
		if (useRandom)
		{
			for (int i=0; i < size; i++)
			{
				int randomInt = 	generator.nextInt(max);
				sample.set(randomInt,true);
			}
		} else
		{
			for (int i=0; i < max; i+= portionSize)
			{
				sample.set(i,true);
			}
		}

		System.err.println("sample taken");
		int indexInFrequencyList=0; 

		WordList sampleList = new WordList();


		int cumul = tl.get(0).frequency;
		int K=0;
		int TL=tl.size();
		TypeFrequency tf = tl.get(0);

		for (int i=0; i < max; i++)
		{
			if (indexInFrequencyList >= TL-1) 
				break;
			if (i > cumul)
			{
				indexInFrequencyList++;
				tf =   tl.get(indexInFrequencyList);
				cumul += tf.frequency;
			}
			if (sample.get(i) )
			{
				sampleList.incrementFrequency(tf.type, 1);
			}
		}

		System.err.printf("frequency list from sample constructed, %d words\n", sampleList.size());

		List<TypeFrequency> sl = sampleList.getTypeFrequencyList();

		int V =  sampleList.size();
		double TTR =  V / (double) size;

		this.vocabularySizes.add(V);
		this.sampleSizes.add(size);
		

		System.err.printf("Distinct words in sample of size %d: %d, Ratio=%f\n", size, V, TTR);

		int P=0;

		if (sampleSizes.size() >= 3)
			regression();
	}
	void printTF(List<TypeFrequency> sl, int max)
	{
		//for (TypeFrequency tf1: sl)
		//{
		//if (P++ >= max) break;
		//System.err.println(tf1.type + "\t" + tf1.frequency);
		//}
	}
	public void regression()
	{
		regression = new SimpleRegression();
		for (int i=0; i < this.vocabularySizes.size(); i++)
		{
			double X = StrictMath.log((double) this.sampleSizes.get(i));
			double logY = StrictMath.log((double) this.vocabularySizes.get(i));
			regression.addData(X,logY);
		}

		RegressionResults result = regression.regress();

		double logK = regression.getIntercept();
		double B = regression.getSlope();
		System.err.println("K=" + StrictMath.exp(logK) + "  Beta= "  + B);
	}

	public static void main(String[] args)
	{
		SampleFromWordList s = new SampleFromWordList(new WordList("s://jesse/FrequentieLijsten/chnWord.txt"));
		int[] sampleSize = {10000,100000,1000000,10000000,100000000};

		for (int S: sampleSize )
		{
			s.sample(S);
		}

		s.sampleSizes.add(3700000);
		s.sampleSizes.add(378000000);
		s.regression();
	}
}
