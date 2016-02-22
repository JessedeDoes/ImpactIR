package nl.namescape.stats;

import com.radialpoint.word2vec.ConvertVectors;
import com.radialpoint.word2vec.Distance;
import com.radialpoint.word2vec.Distance.ScoredTerm;
import com.radialpoint.word2vec.Vectors;

import ml.options.Options;
import ml.regression.LASSO;
import ml.regression.Regression;
import la.matrix.*;
import la.vector.DenseVector;
import ml.utils.Printer;

import java.util.*;
/**
 * 
 * Test of met lineare relatie tussen twee ruimtes goede woordparen kunnen vinden.
 * Wellicht beter te maken door per woord / groepje woorden 
 * andere lineaire afbeelding te kiezen, meer bepaald door de woorden waarmee het te mappen woord een relatie heeft
 * 
 * 
 * LASSO is a Java implementation of LASSO, which solves the following convex optimization problem:
min_W 2\1 || Y - X * W ||_F^2 + lambda * || W ||_1
where X is an n-by-p data matrix with each row bing a p dimensional data vector and Y is an n-by-ny dependent variable matrix.
 * @author does
 *
 */
public class testLAML
{
	double p = 0.01; // is dit slim?
	int max = 8000;
	boolean useP = false;
	boolean useMax = true;
	int N_TERMS=10;
	
	public Regression getLasso()
	{
		Options options = new Options(); 
		options.maxIter = 30; 
		options.lambda = 0.01;  // regularization
		options.verbose = !true; 
		options.epsilon = 1e-5; 

		Regression lasso = new LASSO(options); 
		return lasso;
	}

	public static void test()
	{
		double[][] data = 
			{
				{1, 2, 3, 2}, 
				{4, 2, 3, 6}, 
				{5, 1, 2, 1}
			};

		double[][] depVars = {{3, 2}, {2, 3}, {1, 4}}; 

		Options options = new Options(); 
		options.maxIter = 600; 
		options.lambda = 0.05; 
		options.verbose = !true; 
		options.epsilon = 1e-5; 
		Regression LASSO = new LASSO(options); 
		LASSO.feedData(data); 
		LASSO.feedDependentVariables(depVars);
		LASSO.train(); 
		System.out.printf("Projection matrix:\n"); 
		Printer.display(LASSO.W); 
		Matrix Yt =
				LASSO.predict(data); 
		// 
		System.out.printf("Predicted dependent variables:\n"); 
		Printer.display(Yt);
	}


	public float[] doubleToFloat(double[] x)
	{
		float[] f = new float[x.length];
		for (int i=0; i < x.length; i++)
			f[i] = (float) x[i];
		return f;
	}

	public double[] floatToDouble(float[] x)
	{
		double[] f = new double[x.length];
		for (int i=0; i < x.length; i++)
			f[i] = x[i];
		return f;
	}

	public String[] getCommonVocabulary(Vectors v1, Vectors v2)
	{
		List<String> common = new ArrayList<String>();
		for (int i=0; i < v1.wordCount(); i++)
		{
			String w = v1.getTerm(i);
			if (v2.hasTerm(w))
			{
				common.add(w);
			}
		}
		String[] x = (String[]) common.toArray();
		return x;
	}
	
	public void testMapping(Vectors v1, Vectors v2)
	{
		Matrix W = getLinearMappingBetweenSpaces(v1,v2);
		//String[] common = getCommonVocabulary(v1,v2);
		Matrix Wt = W.transpose();
		for (int i=0; i < v1.wordCount(); i++)
		{
			String w = v1.getTerm(i);
			if (v2.hasTerm(w))
			{
				try
				{
					float[] vec1 = v1.getVector(i);
					float[] vec2 = v2.getVector(w);
					double[][] d1 = new double[1][];
					d1[0] = floatToDouble(vec1);
					DenseVector v = new DenseVector( floatToDouble(vec1));
					
			
					DenseVector img = (DenseVector) Wt.operate(v);
					
					float[] y1 = doubleToFloat(img.getPr());
					Distance.normalize(y1);
					
					// now measure distance between y1 and vec2. waarom zit dat verdarrie niet in de interface??
					
					double d = Distance.cosineSimilarity(y1,vec2);
					
					System.out.print(w + " selfdist:"  + d + "  ");
					
					List<ScoredTerm> close = Distance.getClosestTerms(v2, N_TERMS, y1);
					
					int k=0;
					int matchAt = -1;
					
					boolean selfMatch = false;
					
					String neighbours = "";
					
					for (ScoredTerm st: close)
					{
						if (st.getTerm().equals(w))
						{
							matchAt = k;
						}
						neighbours += st.getTerm() + "/" + st.getScore() + " ";
						k++;
					}
					
					System.out.println( "   selfMatch: " + matchAt + " " + neighbours);
					
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}


	public Matrix getLinearMappingBetweenSpaces(Vectors v1, Vectors v2)
	{
		long s = System.currentTimeMillis();
		List<String> words = new ArrayList<String>();
		List<float[]> vectors1 = new ArrayList<float[]>();
		List<float[]> vectors2 = new ArrayList<float[]>();
		int k=0;
		for (int i=0 ; i < v1.wordCount(); i++)
		{
			if ((!useP  ||  Math.random() < p) && (!useMax ||  k < max))
			{
				String w = v1.getTerm(i);
				float[] vec1 = v1.getVector(i);
				if (v2.hasTerm(w)) try
				{
					// nl.openconvert.log.ConverterLog.defaultLog.println("selected " + w);
					float[] vec2 = v2.getVector(w);
					if (vec2 != null)
					{
						words.add(v1.getTerm(i));
						vectors1.add(vec1);
						vectors2.add(vec2);
						k++;
					}
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		
		double[][] X  = new double[vectors1.size()][];
		double[][] Y  = new double[vectors1.size()][];
		
		nl.openconvert.log.ConverterLog.defaultLog.println("Selected + " + vectors1.size());
		
		for (int i=0; i < vectors1.size(); i++)
		{
			float[] vec1 = vectors1.get(i);
			float[] vec2 = vectors2.get(i);
			int nx = vec1.length;
			int ny = vec2.length;

			X[i] = new double[nx];
			Y[i] = new double[ny];
			for (int j=0; j < nx; j++)
			{
				X[i][j]= vec1[j];
			}
			for (int j=0; j < ny; j++)
			{
				Y[i][j]= vec2[j];
			}
		}
		Regression lasso = getLasso();
		lasso.feedData(X);
		lasso.feedDependentVariables(Y);
		lasso.train();
		//Printer.display(lasso.W);a
		long f = System.currentTimeMillis();	
		nl.openconvert.log.ConverterLog.defaultLog.println("Computed linear mapping in " +(f-s) + " milliseconds ");
		return lasso.W;
	}

	public static void main(String[] args)
	{
		//test();
		testLAML l = new testLAML();
		Vectors v1 = ConvertVectors.readVectors(args[0]);
		Vectors v2 = ConvertVectors.readVectors(args[1]);
		l.testMapping(v1, v2);               
	}
}
