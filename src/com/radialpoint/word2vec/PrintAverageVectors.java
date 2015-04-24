package com.radialpoint.word2vec;

public class PrintAverageVectors
{
	public static void main(String[] args)
	{
		Vectors vectors = ConvertVectors.readVectors(args[0]);
		Distance.printAverageVectorsForLabeledSentences(vectors,args[1]);
	}
}
