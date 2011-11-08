package spellingvariation;

import java.util.List;

import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultWeightedEdge;

import spellingvariation.Alignment.Position;
import util.Matrix;

class Util
{
	public static double drandom()
	{
		double x = Math.random();
		long M0 = 1<<31;
		long M = M0 -1;
		double D = M;
		double d = x / D;
		return d;
	}
}

/** 
 * <p>
 * This class implements parameter estimation for 0-1/0-1 joint multigrams, without dependency 
 * on context.
 * It is just Ristad & Yianilos' method for the parameter estimation
 *  of a weighted string edit distance.
 *  <p>
 *  The resulting substitution, insertion and deletion weights are not used directly.
 *  The alignments obtained by viterbi-optimal alignment of the data according to these weights 
 *  are used in the next steps by 
 *  the MultigramTransducer class
 */
public class UnigramTransducer implements java.io.Serializable, CodeToStringPairMapping
{
	private static final long serialVersionUID = 5544593995127888964L;

	class CodePair implements java.io.Serializable
	{
		private static final long serialVersionUID = 2063150781763259557L;
		public int  lhs;
		public int  rhs;
		public int  id;
		public CodePair() {};
		public CodePair(int l, int r, int id) { lhs=l; rhs = r ; this.id = id; }
	};

	public UnigramTransducer() {};

	/**
	 * Not used. alphabet is obtained from the example data
	 * @param A
	 * @param B
	 */
	public UnigramTransducer(String A, String B)
	{
		inputAlphabet = new Alphabet(A);
		outputAlphabet = new Alphabet(B);
		init();
	}
	/**
	 * the integer value used to represent empty left- or right hand sides in insertion or deletion
	 */

	protected static final int 空 = Alphabet.空; //
	private static final double LOG_2 = 0.693147180559945;
	private static boolean debug = false;
	protected int MAX_ITERATIONS = 30;
	protected Alphabet inputAlphabet;
	protected Alphabet outputAlphabet;
	protected CodePair symbolPairs[];
	//private double R;

	protected int nSymbolPairs;
	protected double [][]delta; 
	protected int CodePair2SymbolId[][];

	//private double [][]gamma;

	private double stoppingProbability;
	private transient Dataset dataset;

	public double stochasticEditDistance(String _x, String _y)
	{
		Alphabet.CodedString x = inputAlphabet.encode(_x);
		Alphabet.CodedString y = outputAlphabet.encode(_y);
		double d =  stochasticEditDistance(x,y);
		return d;
	}

	public double stochasticEditDistance(Alphabet.CodedString x, Alphabet.CodedString y)
	{
		double [][]alpha = forwardEvaluate(x, y);
		double AlphaMN = alpha[x.size][y.size];
		return -Math.log(AlphaMN) / LOG_2;
	}

	public double jointProbability(Alphabet.CodedString x, Alphabet.CodedString y)
	{
		double[][] alpha = forwardEvaluate(x, y);
		double AlphaMN = alpha[x.size][y.size]; // Kan dit negatief worden?
		if (Double.isNaN(AlphaMN))
		{
			System.err.println("Fatal error: Joint probability undefined!");
			System.exit(1);
		}
		if (AlphaMN < 0)
		{
			System.err.println("Warning: Joint probability < 0 (" + x + ", " + y + ")");
		}
		return AlphaMN;
	}

	public double logLikelihood(String [] X, String[] Y, int m)
	{
		double ll = 0.0;
		for (int i=0; i < m; i++)
			ll += this.stochasticEditDistance(X[i],Y[i]);
		return ll;
	}

	/*
  private double getNewAlpha(int s, int t, double α)
  {
	 double r = delta[s][t];
	 if (Double.isNaN(r))
	 {
		System.err.println("Error: Bad delta value for:" + s + ","  + t + 
				input_alphabet.decode(s) + " " + output_alphabet.decode(t));
	 }
	 return r * α;
  }
	 */

	/**
	 * Basic parameter estimation procedure
	 * @param d: example pairs
	 */
	public void estimateParameters(Dataset d)
	{
		inputAlphabet = d.input_alphabet;
		outputAlphabet = d.output_alphabet;
		init();
		for(int i=0; i < this.MAX_ITERATIONS; i++)
		{
			System.err.println("Iteration: round " + i);
			expectationMaximization(d);
		}
                //dumpAlignments(d);
		//dumpParameterTable("lsed_mixture.params");
	}

	public void dumpAlignments(Dataset d)
	{
		ZeroOrderAligner aligner = new ZeroOrderAligner(this);
		for (int i=0; i < d.size(); i++)
		{ 
			if (true)
			{
				String a = aligner.alignment(d.items.get(i).best_match.wordform, d.items.get(i).target);
				System.out.printf("%s\t%s\t%s\t%s\n", d.items.get(i).lemma, d.items.get(i).best_match.wordform, d.items.get(i).target, a);
			}
		}
	}
	/**
	 * One step in the iteration
	 * @param d
	 */
	private void expectationMaximization(Dataset d)
	{
		this.dataset = d;
		int m = d.size();
		//System.err.println("piep " + m);
		double [][]Γ = util.Matrix.newMatrix(256,256); //HOHO: alphabet sizes meenemen

		for (int i=0; i < nSymbolPairs; i++) Γ[symbolPairs[i].lhs][symbolPairs[i].rhs] = 0;
		double Γ_stop = 0.0;


		System.err.println("Expectation steps");

		for (int i=0; i < m; i++)
		{
			Dataitem item = dataset.get(i);
			for (Candidate cand: dataset.items.get(i).candidates)
			{
				Γ_stop = 
					expectationStep(cand.coded_wordform, item.coded_target, Γ, Γ_stop, cand.lambda, 1.0);
			}
		}

		maximizationStep(Γ,Γ_stop); // i.e. just dump normalized Γ into delta

		// Set the lambdas and pick best matches

		for (int i=0; i < m; i++)
		{
			double norm = 0;
			Dataitem item = dataset.get(i);
			Alphabet.CodedString ct =  dataset.items.get(i).coded_target;
			for (int j =0; j < item.candidates.size(); j++)
			{
				Candidate cand = item.candidates.get(j);
				Alphabet.CodedString cwf = cand.coded_wordform;
				cand.lambda =  jointProbability(cwf, ct);
				//WordGraph wg = new WordGraph(this,cwf,ct,100);
				if (dataset.has_frequency)
				{
					cand.lambda *= cand.frequency;
				}
				norm += dataset.lambda(i,j);
			}
			double lambda_max = -1;
			boolean pickedBestMatch = false;
			for (int j=0; j < item.candidates.size(); j++)
			{
				Candidate cand = item.candidates.get(j);
				cand.lambda /= norm;
				if (cand.lambda > lambda_max)
				{
					item.best_match = cand;
					lambda_max = dataset.lambda(i,j);
					pickedBestMatch = true;
				}
				//fwprintf(stderr,L"%ls %f\n",dataset.items[i].wordforms[j], dataset.lambda(i,j));
			}
			if (!pickedBestMatch)
			{
				System.err.println("no best match picked for:" + dataset.items.get(i).target);
			}
		}
	}

	/**
	 * compute forward and backward probabilities and update gamma
	 * @param x
	 * @param y
	 * @param Γ
	 * @param Γ_stop
	 * @param scale
	 * @return
	 */

	private double expectationStep(Alphabet.CodedString x, Alphabet.CodedString y, 
			double [][] Γ, double Γ_stop, double lambda, double scale) // stop_d was reference!
	{
		int T = x.size;
		int V = y.size;

		double [][] alpha = forwardEvaluate(x, y);

		/**
		 * Forward evaluation may result in too small probability for numerical stability
		 * TODO: Needs better handling
		 */

		if (alpha[T][V] == 0.0 || Double.isInfinite(1/alpha[T][V])) 
		{
			//System.err.println("alpha[T][V] zero after forward evaluation!!\n");
			return Γ_stop;
		}

		double [][] beta = backwardEvaluate(x, y);

		Γ_stop += scale * lambda;

		for (int t=0; t <= T; t++)
		{
			for (int v=0; v <= V; v++)
			{
				int z1, z2;
				double norm = scale * lambda * beta[t][v] / alpha[T][V]; // dit kan mis gaan
				if (Double.isNaN(norm) || Double.isInfinite(norm))
				{
					System.err.println("norm boem " + norm +  " "  + alpha[T][V]);
					System.exit(1);
				}
				// moet som over alphatjes van alternatieven zijn? scale = zo ongeveer alpha[T][V] / som(alpha), dit is gek
				// lambda = (alphatv / somalpha) dus die zitter al in?
				if (t > 0)
				{
					z1 = x.get(t-1); z2 =  空;
					Γ[z1][z2] += alpha[t-1][v] * delta[z1][z2] * norm;
				}
				if (v > 0)
				{
					z1 = 空; z2 = y.get(v-1);
					Γ[z1][z2] += alpha[t][v-1] * delta[z1][z2] * norm;
				}
				if (t > 0 && v > 0)
				{
					z1 = x.get(t-1);  z2 = y.get(v-1);
					Γ[z1][z2] += alpha[t-1][v-1] * delta[z1][z2] * norm;
				}
			}
		}  
		if (debug)
		{
			System.err.println("Dump gamma:\n");
			// dump_params(Γ,safestrlen( (String ) A),safestrlen( (unsigned String ) B));
		}
		return Γ_stop;
	}

	/*
     Performs a single EM iteration
	 */

	private void maximizationStep(double[][] Γ, double Γ_stop) // stop_d was reference
	{
		System.err.println("Maximization step");

		
		double N = Γ_stop; // now this is nonsense

		for (int i=0; i < nSymbolPairs; i++)
			N += Γ[symbolPairs[i].lhs][symbolPairs[i].rhs];

		for (int i=0; i < nSymbolPairs; i++)
		{
			delta[symbolPairs[i].lhs][symbolPairs[i].rhs] = 
				Γ[symbolPairs[i].lhs][symbolPairs[i].rhs]/N; // ??
			if (Double.isNaN(delta[symbolPairs[i].lhs][symbolPairs[i].rhs]))
			{
				System.err.println("boem " + N);
				System.exit(1);
			}
		}

		stoppingProbability = Γ_stop / N;
	}

	protected double[][] forwardEvaluate(Alphabet.CodedString x, Alphabet.CodedString y)
	{ 
		int T =  x.size;
		int V =  y.size;

		double[][] α = Matrix.newMatrix(T+1,V+1); 
		for (int t=0; t < T+1; t++)
			for (int v=0; v < V+1; v++)
				α[t][v] = 0;
		α[0][0] = 1;

		for (int t=0; t < T+1; t++)
		{
			for (int v=0; v < V+1; v++)
			{
				if (v > 0 && t > 0) α[t][v] = 0;
				if (v > 0) // insert
					α[t][v] += delta[空][y.get(v-1)] * α[t][v-1];
				if (t > 0) // delete
					α[t][v] += delta[x.get(t-1)][空] * α[t-1][v];
				if (v > 0 && t > 0) //replace
					α[t][v] += delta[x.get(t-1)][y.get(v-1)] * α[t-1][v-1];
				if (Double.isNaN(α[t][v])) 
				{
					System.err.printf("Error: bad alpha value computed:... %d %d %s = %s\n", t, v, 
							x, y);
					System.exit(1);
				}
			}
		}
		α[T][V] *= stoppingProbability; 
		if (Double.isNaN(α[T][V]))
		{
			System.err.println("Fatal error: unstable forward evaluation; dump α matrix\n");
			dumpParameters(α,T+1,V+1);
			System.exit(1);
		}
		return α;
	}

	protected double[][]  backwardEvaluate(Alphabet.CodedString x, Alphabet.CodedString y)
	{
		int T =  x.size;
		int V =  y.size;

		double[][] β = Matrix.newMatrix(T+1, V+1);

		β[T][V] = this.stoppingProbability;

		for (int t=T; t >=0; t--)
		{
			for (int v=V; v >=0; v--)
			{
				if (v < V && t < T)
				{
					β[t][v] = 0;
				}
				if (v < V)
					β[t][v] += delta[空][y.get(v)] * β[t][v+1];
				if (t < T)
					β[t][v] += delta[x.get(t)][空] * β[t+1][v];
				if (v < V && t < T)
					β[t][v] += delta[x.get(t)][y.get(v)] * β[t+1][v+1];
			}
		}
		if (debug)
		{
			//fprintf(stderr,"Dump beta\n");
			dumpParameters(β,T+1,V+1);
		}
		return β;
	}

	private void init()
	{
		symbolPairs = new UnigramTransducer.CodePair[(inputAlphabet.size +1) * (outputAlphabet.size + 1)];
		Alphabet.CodedString I = inputAlphabet.encode(inputAlphabet.alphabet);
		Alphabet.CodedString O = outputAlphabet.encode(outputAlphabet.alphabet);

		int k=0;

		for (int i=0; i < I.size; i++)
		{
			for (int j=0; j < O.size; j++)
			{
				if (i == 0) { symbolPairs[k] = new CodePair(空,O.get(j),k); k++; }
				symbolPairs[k] = new CodePair(I.get(i), O.get(j),k); k++;
			}
			symbolPairs[k] = new CodePair(I.get(i), 空,k); k++;
		}

		this.nSymbolPairs = k;

		this.delta = Matrix.newMatrix(inputAlphabet.size+1,outputAlphabet.size+1);
		this.CodePair2SymbolId = new int[inputAlphabet.size+1][outputAlphabet.size+1];

		for (int i=0; i < nSymbolPairs; i++)
		{
			double d = Util.drandom();
			CodePair e = symbolPairs[i];
			CodePair2SymbolId[e.lhs][e.rhs] = i;
			//System.err.printf("%d: d('%d','%d')=%f\n",i,E[i].a,E[i].b,d);
			delta[e.lhs][e.rhs] = d;

			if (e.lhs == e.rhs)
				delta[e.lhs][e.rhs] = 1 - 0.1 * d;
			else
				delta[e.lhs][e.rhs] = 0.1 * d;
		}
		stoppingProbability = Util.drandom(); // HM?? why?
		normalize();
	}

	protected int getCodeForSymbolPair(int i, int j)
	{
		int r = CodePair2SymbolId[i][j];
		int i1 = symbolPairs[r].lhs;
		int j1 = symbolPairs[r].rhs;
		if (i != i1 || j != j1)
		{
			System.err.println("inconsistency!");
			System.exit(1);
		}
		return CodePair2SymbolId[i][j];
	}

	private void normalize() // make sure probabilities add up to 1
	{
		double N;
		N = stoppingProbability; // Hallo?

		for (int i=0; i < nSymbolPairs; i++)
		{
			N += delta[symbolPairs[i].lhs][symbolPairs[i].rhs];
		}

		//fprintf(stderr,"N=%f\n",N);

		for (int i=0; i < nSymbolPairs; i++)
		{
			delta[symbolPairs[i].lhs][symbolPairs[i].rhs] /= N;
		}

		//fprintf(stderr,"Dumping delta after Normalization\n");
		//dump_params(delta,safestrlen( (char *) A),safestrlen( (char *)B));
	}

	private void dumpParameterTable(String filename)
	{
		double [][] x = delta;
		int m = inputAlphabet.size-1;
		int n = outputAlphabet.size-1;
		Alphabet.CodedString I = inputAlphabet.encode(inputAlphabet.alphabet);
		Alphabet.CodedString O = outputAlphabet.encode(outputAlphabet.alphabet);
		// check summation
		double N=0;
		for (int i=0; i < nSymbolPairs; i++)
		{
			N += delta[this.symbolPairs[i].lhs][this.symbolPairs[i].rhs];
		}
		System.err.println("symbolpairs: " + nSymbolPairs + " N= "  + N);
		try
		{
			java.io.PrintStream f = new java.io.PrintStream(filename);
			for (int j=0; j < n; j++)
			{
				f.printf("_eps_\t%c\t%4.16f\n", outputAlphabet.decode(O.get(j)), x[Alphabet.空][O.get(j)]);
			}
			for (int i=0; i < m; i++)
			{
				f.printf("%c\t_eps_\t%4.16f\n", inputAlphabet.decode(I.get(i)),  x[I.get(i)][Alphabet.空]);
				for (int j=0; j < n; j++)
				{
					f.printf("%c\t%c\t%4.16f\n", 
							inputAlphabet.decode(I.get(i)),  outputAlphabet.decode(O.get(j)),x[I.get(i)][O.get(j)]);
				}
			}
		} catch (Exception e)
		{
		}
	}

	private void dumpParameters(double [][]x, int m, int n)
	{
		for (int i=0; i < m; i++)
		{
			for (int j=0; j < n; j++)
			{
				System.err.print(" " +  x[i][j] + " ");
			}
			System.err.print("\n");
		}
	}


	public static void usage()
	{
	}

	/*
	 * TODO: ensure everything is in UTF-8 when default system settings are not UTF-8
	 */

	public static void main(String [] argv)
	{
		int argc = argv.length; 
		if (argc < 1)
		{
			usage();
			System.exit(1);
		}

		Dataset d = new Dataset();
		d.read_from_file(argv[0]);
		System.err.println("Data read: " + d.size() + " items");

		UnigramTransducer t = new UnigramTransducer();
		t.estimateParameters(d);
		t.dumpAlignments(d);
	}

	// interface CodeToStringPairMapping
	public String getLHS(int i)
	{
		if (i == Alphabet.空)
			return "";
		return "" + (this.inputAlphabet.decode(this.symbolPairs[i].lhs));
	}


	public String getRHS(int i)
	{
		if (i == Alphabet.空)
			return "";
		return "" + (this.outputAlphabet.decode(this.symbolPairs[i].rhs));
	}
}
