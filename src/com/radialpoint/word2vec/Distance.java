/*
 * Copyright 2014 Radialpoint SafeCare Inc. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.radialpoint.word2vec;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.io.*;

/**
 * this is much faster in Mikolov's code. Difference?
 */
public class Distance 
{

	public static class ScoredTerm 
	{
		private String term;
		private float score;

		public ScoredTerm(String term, float score) 
		{
			super();
			this.term = term;
			this.score = score;
		}

		public String getTerm() {
			return term;
		}

		public float getScore() {
			return score;
		}
	}

	public static List<ScoredTerm> getClosestTerms(Vectors vectors, int wordsToReturn, float[] vec)
	{
		float[][]allVec = vectors.getVectors();
		double distance = 0;

		int size = vectors.vectorSize();

		float[] bestDistance = new float[wordsToReturn];
		String[] bestWords = new String[wordsToReturn];

		for (int c = 0; c < vectors.wordCount(); c++) 
		{    
			distance = 0;
			for (int i = 0; i < size; i++)
				distance += vec[i] * allVec[c][i];
			for (int i = 0; i < wordsToReturn; i++)  // this is slow
			{
				if (distance > bestDistance[i]) 
				{
					for (int d = wordsToReturn - 1; d > i; d--) 
					{
						bestDistance[d] = bestDistance[d - 1];
						bestWords[d] = bestWords[d - 1];
					}
					bestDistance[i] = (float) distance;
					bestWords[i] = vectors.getTerm(c);
					break;
				}
			}
		}
		List<ScoredTerm> result = new ArrayList<ScoredTerm>(wordsToReturn);
		for (int i = 0; i < wordsToReturn; i++)
			result.add(new ScoredTerm(bestWords[i], bestDistance[i]));
		return result;
	}

	public static float[] getAverageVector(Vectors vectors, String[] tokens, float alpha)
	{
		int size = vectors.vectorSize();
		float[] vec = new float[size];
		Arrays.fill(vec, 0.0f);
		int tokenCount = tokens.length;
		float[][]allVec = vectors.getVectors();
		for (int i = 0; i < tokenCount; i++) 
		{
			Integer idx = vectors.getIndexOrNull(tokens[i]);
			if (idx == null) 
			{	
				continue;
			}
			//wordIdx.add(idx);
			float[] vect1 = allVec[idx];
			for (int j = 0; j < size; j++)
				vec[j] += vect1[j];
		}
		normalize(vec);
		return vec;
	}
	
	
	public static void printAverageVectorsForLabeledSentences(Vectors vectors, String filename)
	{
		String line;
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(filename));
			while ((line = in.readLine()) != null)
			{
				String[] tokens = line.split("\t");
				String lemma = tokens[0];
				String sense = tokens[1];
				String example = tokens[2];
				String[] words = example.split("\\s+");
				float [] vec = getAverageVector(vectors, words, 0);
				System.out.print(sense + "\t" + lemma + "\t");
				for (int i=0; i < vec.length; i++)
				{
					System.out.print(i==0?"":" " + vec[i]);
				}
				System.out.println();
			}
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static List<ScoredTerm> measure(Vectors vectors, int wordsToReturn, String[] tokens) throws OutOfVocabularyException 
	{
		float distance;
		float length;
		float[] bestDistance = new float[wordsToReturn];
		String[] bestWords = new String[wordsToReturn];
		int d;
		int size = vectors.vectorSize();
		float[] vec = new float[size]; // average vector of input tokens
		float[][]allVec = vectors.getVectors();
		long startTime = System.currentTimeMillis();
		// nl.openconvert.log.ConverterLog.defaultLog.println("1:" + startTime);
		Set<Integer> wordIdx = new TreeSet<Integer>();

		int tokenCount = tokens.length;
		boolean outOfDict = false;
		String outOfDictWord = null;
		Arrays.fill(vec, 0.0f);
		wordIdx.clear();
		
		for (int i = 0; i < tokenCount; i++) 
		{
			Integer idx = vectors.getIndexOrNull(tokens[i]);
			if (idx == null) 
			{
				outOfDictWord = tokens[i];
				outOfDict = true;
				break;
			}
			wordIdx.add(idx);
			float[] vect1 = allVec[idx];
			for (int j = 0; j < size; j++)
				vec[j] += vect1[j];
		}
		if (outOfDict)
			throw new OutOfVocabularyException(outOfDictWord);

		normalize(vec);

		for (int i = 0; i < wordsToReturn; i++) 
		{
			bestDistance[i] = Float.MIN_VALUE;
			bestWords[i] = "";
		}

		//nl.openconvert.log.ConverterLog.defaultLog.println("2:" +  ( System.currentTimeMillis() - startTime));
		int wc = vectors.wordCount();
		
		for (int c = 0; c < wc; c++) 
		{
			if (wordIdx.contains(c)) continue;
			distance = 0;
			float[] vc = allVec[c];
			
			for (int i = 0; i < size; i++) // this is simply much slower than C..
			{
				distance += vec[i] * vc[i];
			}
			
			 for (int i = 0; i < wordsToReturn; i++) 
			{
				if (distance > bestDistance[i])  
				{
					for (d = wordsToReturn - 1; d > i; d--) // schuif op (dit zou sneller moeten kunnen)
					{
						bestDistance[d] = bestDistance[d - 1];
						bestWords[d] = bestWords[d - 1];
					}
					bestDistance[i] = (float) distance;
					bestWords[i] = vectors.getTerm(c);
					break;
				}
			}
		}
		//nl.openconvert.log.ConverterLog.defaultLog.println("best:" + bestDistance[0]);
		//nl.openconvert.log.ConverterLog.defaultLog.println("3:" +  ( System.currentTimeMillis() - startTime));
		List<ScoredTerm> result = new ArrayList<ScoredTerm>(wordsToReturn);
		for (int i = 0; i < wordsToReturn; i++)
			result.add(new ScoredTerm(bestWords[i], bestDistance[i]));
		return result;
	}


	public static void normalize( float[] vec)
	{
		int size = vec.length;
		double length;
		length = 0;
		for (int i = 0; i < size; i++)
			length += vec[i] * vec[i];
		length = (float) Math.sqrt(length);
		for (int i = 0; i < size; i++)
			vec[i] /= length;
	}


	public static double cosineSimilarity(float[] a, float[] b) 
	{
		double dotProduct = 0.0;
		double aMagnitude = 0.0;
		double bMagnitude = 0.0;
		for (int i = 0; i < b.length ; i++) 
		{
			double aValue = a[i];
			double bValue = b[i];
			aMagnitude += aValue * aValue;
			bMagnitude += bValue * bValue;
			dotProduct += aValue * bValue;
		}
		aMagnitude = Math.sqrt(aMagnitude);
		bMagnitude = Math.sqrt(bMagnitude);
		return (aMagnitude == 0 || bMagnitude == 0)
				? 0: dotProduct / (aMagnitude * bMagnitude);
	}

	public static void main(String[] args)
	{
		Vectors v = ConvertVectors.readVectors(args[0]);
		int nof = Integer.parseInt(args[1]);
		String line;
		try
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			while ((line = in.readLine()) != null)
			{
				String[] tokens = line.split("\\s+");
				try
				{
					List<ScoredTerm> l = Distance.measure(v, nof, tokens);
					for (ScoredTerm t: l)
					{
						System.out.println(t.getTerm() + "\t" + t.getScore());
					}
				} catch (Exception e)
				{

				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
