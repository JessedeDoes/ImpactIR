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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * This program takes vectors are produced by the C program word2vec and transforms them into a Java binary file to be
 * read by the Vectors class
 * 
 * (Slightly optimized by me, notably by simply using a BufferedInputStream)
 * Also (probably) reading the word2vec file is  faster than reading the java serialized object?
 */
public class ConvertVectors {

	/**
	 * @param args
	 *            the input C vectors file, output Java vectors file
	 */


	public static void main(String[] args) throws VectorsException, IOException 
	{
		//File outputFile = new File(vectorFile);
		Vectors v = readVectors(args[0]);
		File outputFile = new File(args[1]);
		FileOutputStream fos = new FileOutputStream(outputFile);
		v.writeTo(fos);
	}

	/**
	 * @param vectorFileName
	 * @return
	 */
	
	public static Vectors readVectors(String vectorFileName)
	{
		float[][] vectors;
		String[] vocabVects;
		int words;
		int size;
		
		try
		{
			double len;
			File vectorFile = new File(vectorFileName);
			if (!vectorFile.exists())
				throw new VectorsException("Vectors file not found");

			BufferedInputStream fis = new BufferedInputStream(new FileInputStream(vectorFile));

			StringBuilder sb = new StringBuilder();
			char ch = (char) fis.read();
			while (ch != '\n') {
				sb.append(ch);
				ch = (char) fis.read();
			}

			String line = sb.toString();
			String[] parts = line.split("\\s+");
			words = (int) Long.parseLong(parts[0]);
			size = (int) Long.parseLong(parts[1]);
			vectors = new float[words][];
			vocabVects = new String[words];

			nl.openconvert.log.ConverterLog.defaultLog.println("Read " + words + " words with size " + size + " per vector from "  + vectorFileName);

			byte[] orig = new byte[4];
			byte[] buf = new byte[4];
			for (int w = 0; w < words; w++) {
				if (w % (words / 10) == 0) {
					nl.openconvert.log.ConverterLog.defaultLog.println("Read " + w + " words");
				}

				sb.setLength(0);
				ch = (char) fis.read();
				while (!Character.isWhitespace(ch) && ch >= 0 && ch <= 256) {
					sb.append((char) ch);
					ch = (char) fis.read();
				}
				ch = (char) fis.read();
				String st = sb.toString();

				vocabVects[w] = st;
				float[] m = new float[size];
				for (int i = 0; i < size; i++) {
					// read a little endian floating point number and interpret it as a big endian one, see
					// http://stackoverflow.com/questions/2782725/converting-float-values-from-big-endian-to-little-endian/2782742#2782742
					// NB: this code assumes amd64 architecture
					for (int j = 0; j < 4; j++)
						orig[j] = (byte) fis.read(); // silly: no buffering....
					buf[2] = orig[0];
					buf[1] = orig[1];
					buf[0] = orig[2];
					buf[3] = orig[3];
					// this code can be made more efficient by reusing the ByteArrayInputStream
					//DataInputStream dis = new DataInputStream(new ByteArrayInputStream(buf));
					//float f1 = dis.readFloat();
					float f2 =  ByteBuffer.wrap(buf).getFloat();
					//if (f1 != f2)
					//{
						//nl.openconvert.log.ConverterLog.defaultLog.println("Niet gelijk: "+ f1 + " != " + f2);
					//}
					m[i] = f2;
					//dis.close();
				}
				len = 0;
				for (int i = 0; i < size; i++)
					len += m[i] * m[i];
				len = (float) Math.sqrt(len);
				for (int i = 0; i < size; i++)
					m[i] /= len;
				vectors[w] = m;
			}
			fis.close();
			Vectors instance = new Vectors(vectors, vocabVects);
			return instance;
		}  catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
