package spellingvariation;
/**
 * Compute alignments for a weighted transducer: 
 * Aligner for he joint unigram case ("learning string edit distance")
 * This is just the usual string edit distance procedure
 */


public class ZeroOrderAligner
{

	static final int REPLACE = 0;
	static final int INSERT = 1;
	static final int DELETE = 2;

	UnigramTransducer transducer;

	private double[][] costMatrix;
	int n,m;
	private int[][] operationMatrix;

	public ZeroOrderAligner(UnigramTransducer transducer) 
	{
		this.transducer = transducer;
	}

	String alignment(String s, String t)
	{
		Alphabet.CodedString cs = transducer.inputAlphabet.encode(s);
		Alphabet.CodedString ct = transducer.outputAlphabet.encode(t);
		String x = alignment(cs,ct);
		return x;
	}

	/*
	 * Does Math.max handle negative infinity correctly?
	 */

	double best(double x, double y) 
	{ 
		double r =  Math.max(x,y);
		if (r == Double.NaN)
		{
			System.err.printf("Huh\n");
			return Double.NEGATIVE_INFINITY;
		}
		return r;
	}



	double getCost(int a, int b)
	{
		return costMatrix[a][b];
	}

	void dumpCostMatrix(Alphabet.CodedString s, Alphabet.CodedString t)
	{
		System.out.print("<table border>");
		System.out.print("<tr><td></td>");
		for (int j=0; j < n; j++)
		{
			System.out.printf("<td>%c</td>", transducer.inputAlphabet.decode(s.get(j)));
		}
		for (int i=0; i < m; i++)
		{
			System.out.print("<tr>");
			System.out.printf("<td>%c</td>", transducer.outputAlphabet.decode(t.get(i)));
			for (int j=0; j < n; j++)
			{
				System.out.printf("<td>%f:%d</td>", getCost(i,j),getOperation(i,j));
			}
		}
		System.out.print("</table>");
	}

	double newCost(double oldCost, double delta)
	{
		if (Double.isInfinite(oldCost)) return Double.NEGATIVE_INFINITY;
		if (delta == 0.0)
		{
			// System.err.println("this should not happen " + Math.log(delta)); // je moet oneindig terug geven
			return Double.NEGATIVE_INFINITY;
		}
		double r =  oldCost + 1.0 * Math.log(delta);
		if (Double.isNaN(r))
		{
			return Double.NEGATIVE_INFINITY;
		}
		return r;
	}

	int getOperation(int a, int b)
	{
		return operationMatrix[a][b];
	}

	void setCost(int a, int b, double d)
	{
		if (Double.isNaN(d))
		{
			System.err.printf("NaN at %d %d\n", a, b);
		}
		costMatrix[a][b] = d;
	}

	void setOperation(int a, int b, int o)
	{
		operationMatrix[a][b] = o; 
	}

	public String addPair(String alignment, int x, int y)
	{
		if (x == y && y == 0) return alignment;
		char u = transducer.inputAlphabet.decode(x); 
		char v = transducer.outputAlphabet.decode(y); 
		if (u == v)
			return u + " " + alignment;
		else if (u == 0)
			return "_eps_->" + v + " " + alignment;
		else if (v == 0)
			return u + "->_eps_" + " " + alignment;
		else 
			return u + "->" + v + " " + alignment;
	}
	/*
	 * Compute alignment between encoded words
	 * Synchronized because of nonlocal variables
	 */
	public synchronized String alignment(Alphabet.CodedString s, Alphabet.CodedString t)
	{
		n = s.size;
		m = t.size;

		costMatrix = new double[m+1][n+1];
		operationMatrix = new int[m+1][n+1];

		int a,b;
		double p1,p2,p3;

		int maxnm = (n>m ? n : m);

		// Fill first row and column of costMatrix

		setCost(0,0,0);

		for (a=1; a<=n; a++) // s horizontal
		{
			setCost(0,a,newCost(getCost(0,a-1),transducer.delta[s.get(a-1)][Alphabet.空]));
			operationMatrix[0][a] = DELETE;
		}

		for (a=1; a<=m; a++) // t vertical
		{
			setCost(a,0,newCost(getCost(a-1,0), transducer.delta[Alphabet.空][t.get(a-1)]));
			setOperation(a,0, INSERT);
		}

		// Fill the rest of the costMatrix
		// Order of filling is in an L-shape: One row, then one column, then one row etc.


		for(a=1; a<=maxnm; a++)
		{
			// fill all (remaining) entries in row 'a'. (corresponding to s)

			for (b=a; b<=n; b++)
			{
				if (a>m) continue;

				p1 = newCost(getCost(a-1,b-1), transducer.delta[s.get(b-1)][t.get(a-1)]);
				p2 = newCost(getCost(a-1,b), transducer.delta[Alphabet.空][t.get(a-1)]);
				p3 = newCost(getCost(a,b-1), transducer.delta[s.get(b-1)][Alphabet.空]);

				if (p1 >= p2 && p1 >= p3) 
				{ setOperation(a,b,REPLACE); }
				else if (p2 > p3) 
				{ setOperation(a,b,INSERT); } 
				else 
				{ setOperation(a,b,DELETE); };

				setCost(a, b, best(best(p1,p2),p3));
			}

			// fill all (remaining) entries in column 'a'.

			for(b=a; b<=m; b++) 
			{
				if(a>n) continue;

				p1 = newCost(getCost(b-1,a-1),transducer.delta[s.get(a-1)][t.get(b-1)]);
				p2 = newCost(getCost(b-1,a),transducer.delta[Alphabet.空][t.get(b-1)]);
				p3 = newCost(getCost(b,a-1),transducer.delta[s.get(a-1)][Alphabet.空]);

				if (p1 >= p2 && p1 >= p3) 
				{ setOperation(b,a,REPLACE); } 
				else if (p2 > p3) 
				{ setOperation(b,a,INSERT); } 
				else 
				{ setOperation(b,a,DELETE); };

				setCost(b,a,best(best(p1,p2),p3));
			}
		}



		//System.out.printf("Cost: %f\n",getCost(m,n););
		//dumpCostMatrix(s,t);
		//System.exit(1);

		b = m;
		a = n;

		int [] changes = new int [2 * maxnm];
		int k=0;
		String theAlignment="";

		while (a >= 0 && b >= 0 && (a >= 1 || b >= 1))
		{
			switch (operationMatrix[b][a])
			{
			case REPLACE:
				changes[k++] = REPLACE; a--; b--;
				if (a >= 0 && b >= 0) 
					theAlignment = addPair(theAlignment, s.get(a),t.get(b));
				break;
			case INSERT:
				changes[k++] = INSERT; b--;
				if (b >= 0) 
					theAlignment = addPair(theAlignment,0,t.get(b));
				break;
			case DELETE:
				changes[k++] = DELETE; a--;
				if (a >= 0) 
					theAlignment = addPair(theAlignment, s.get(a),0);
				break;
			default:
				//fwprintf(stderr,L"ERROR AT %d,%d (%s,%s,%s)\n",a,b,s,t,the_alignment);
				return theAlignment;
			}
		}

		//fprintf(stderr,"k=%d\n",k);

		return theAlignment;
	}


	public void alignPairsFromFile(java.io.BufferedReader f)
	{
		String s;
		try
		{
			while ((s =  f.readLine()) != null)
			{
				String[] t = s.split("\t");  
				String x =  t[0];
				String y =  t[1];
				String z =  alignment(x,y);
				System.out.printf("%s\t%s\t%s\n",x,y,z);
			} 
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
