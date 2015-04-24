package com.radialpoint.word2vec;

import java.io.PrintWriter;

public class DumpAsPlainText
{
	public static void main(String[] args)
	{
		Vectors v = ConvertVectors.readVectors(args[0]);
		v.printAsText(new PrintWriter(System.out));
	}
}
