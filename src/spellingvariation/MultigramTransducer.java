package spellingvariation;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import spellingvariation.Alignment.AlignmentGraph;
import spellingvariation.Alignment.Position;
import spellingvariation.AlignmentSegmenter.SegmentationGraph;
import util.Options;

/**
 * Implementation of the joint multigram model following <i>Deligne & Bimbot 1997</i>.
 * <br>
 * There is no conditioning on previous history.
 * <br>
 * This class is really only the estimation part.
 * 
 * <p style="white-space: pre">
 * Problem: this implementation will naturally prefer long segments and stamp out the short ones.
 * How to smooth this?
 * </p>
 * @author jesse
 *
 */
public class MultigramTransducer implements java.io.Serializable
{
	private static final long serialVersionUID = 7505314874185654306L;

	static final int MAXIMUM_HISTORY_LENGTH = 10000;
	protected int MAX_ALIGNMENTS = 6;
	int MAX_ITERATIONS = 5;
	double HELDOUT_PART = 0.0; 
	boolean printSubstitutionsOnly = false;
	
	protected int MODEL_ORDER = 4;
	//protected int nMultigrams;
	protected transient Dataset dataset;
	protected transient UnigramTransducer baseTransducer;
	//private transient Semiring semiring = new Semiring.Reals(); // not used
	protected MultigramSet multigramSet = new MultigramSet();
	protected transient MultigramPruner multigramPruner = null;
	double[] delta;
	double[][] deltaAtStage;
	double[] gamma;
	double stoppingProbability;
	transient private double[][] current_alpha;
	transient private double[][] current_beta;
	protected boolean useConditionalProbabilities = false;  // does not work
	
	private double EVIDENCE_THRESHOLD = 0.0;

  int nMultigrams()
  {
  	return multigramSet.size();
  }

	/**
	 * Initialize from the single-character case
	 */

	public MultigramTransducer() { };

	public synchronized void estimateParameters(Dataset d)
	{
		this.dataset = d;
		// 1: bootstrap from the singleton case
		this.baseTransducer = new UnigramTransducer();
		this.baseTransducer.estimateParameters(d);
		// this.baseTransducer.dumpAlignments(d);
		// 2: set initial multigram scores from the singleton transducer
		// also produces pruned alignment graphs for all candidate pairs
		multigramSet.order = MODEL_ORDER; 
		// System.err.println("order=" + multigramSet.order);
	
		
		this.initializeFromSingletonTransducer(this.baseTransducer, dataset);
		// 3: initial (language-specific) pruning of the multigram set (if needed)
		
		if (this.multigramPruner != null)
		{
			multigramSet.prune(this.multigramPruner);
		}
		// 4: segment all candidate pairs

		AlignmentSegmenter segmenter = new AlignmentSegmenter(multigramSet);
		
		// als je omhoog gaat met smoothing moet dit anders
		
		for (int i=0; i < this.dataset.size(); i++)
		{
			Dataitem item = dataset.get(i);
			for (Candidate cand: item.candidates)
			{
				// hm: getSegmentation is rather SLOW (slightly better now)
				cand.segmentationGraph = segmenter.getSegmentation(cand.alignmentGraph);
				//System.err.println("Item " + i + ": " + cand.wordform + ": segmentation graph has  " 	+ cand.segmentationGraph.edgeSet().size() +  " egdes");
			}
		}

		
		// let 'abstract' patterns overrule their instances
		this.delta = new double[this.nMultigrams()+50];
		applyAbstractions();
		// set held out part of data
		
		int heldOutFrom = (int) ((1.0 - this.HELDOUT_PART) * dataset.size()); 
		//System.err.println("Held out from " + heldOutFrom);
		for (int i = heldOutFrom; i < dataset.size(); i++)
		{
			dataset.get(i).heldOut = true;
		}
		// 5: initialize delta

		this.delta = new double[this.nMultigrams()+50];

		this.stoppingProbability = 0.1;

		double N = this.stoppingProbability;
		for (int i=0; i < nMultigrams(); i++)
			N+= multigramSet.getScore(i);
		for (int i=0; i < nMultigrams(); i++)
			delta[i] = multigramSet.getScore(i) / N;

		// printMultigrams("/tmp/multigrams.before");

		// this.setUniformInitialDistribution(); // this is absurd. lets skip it!
		
		// 6: carry out expectation maximization
		
		this.gamma = new double[this.nMultigrams()];
		for (int i=0; i < MAX_ITERATIONS; i++)
		{
			System.err.println("Iteration " + i);
			double ll = this.expectationMaximization(this.dataset);
			System.err.println("Log likelihood now: " + ll);
			//pruneMultigrams(3000 - 10*i); // HM .. dit gaat niet werken....
		}
		// mop up
		// print alignments
		//this.printAlignments();
		multigramSet.checkUp();
		for (int i=0; i < nMultigrams(); i++)
		{
			multigramSet.setScore(i, delta[i]);
		}
		printMultigrams(Options.getOption("patternOutput"));
		System.out.println("stopping prob=" + this.stoppingProbability);
	}

	/**
	 * replace instances of an abstraction by their abstraction 
	 */
	
	public void applyAbstractions() 
	{
		for (Dataitem item: dataset.items)
		{
			Candidate cand = item.best_match;
			SegmentationGraph g = cand.segmentationGraph;
			for (AlignmentSegmenter.Transition t: g.edgeSet())
			{
				JointMultigram m = multigramSet.getMultigramById(t.multigramId);
				if (m.groupId >= 0)
				{
					// JointMultigram a = multigramSet.getMultigramById(m.groupId);
					// System.err.println("abstracting away: " + m + " for " + a + " :  " + a.id);
					t.multigramId = m.groupId;
					m.active = false;
					
					multigramSet.setScore(t.multigramId, multigramSet.getScore(t.multigramId)+ multigramSet.getScore(m.id));
					multigramSet.setScore(m.id, 0.0);
					// System.err.println(multigramSet.getScore(a.id));
				}
			};
		}
	}
	
	/**
	 * Can only be called after estimateParameters
	 * @param f stream to print to
	 */
	
	@Deprecated
	private void printMultigramsOld(java.io.PrintStream f)
	{
		// System.err.println("Printing multigrams:");
		computeConditionalProbabilities();
		Iterator<JointMultigram> i = multigramSet.sort();
	  while (i.hasNext())
	  {
		   JointMultigram p = i.next();
		   if (printSubstitutionsOnly && !p.isSubstitution()) // no pure insertions or deletions 
		   {
		  	 continue;
		   }
		   if (delta[p.id] > 0 &&  (false || !(p.lhs.equals(p.rhs))))
		   {
		  	 f.println(p + "\t" + delta[p.id] + "\t" + deltaConditional[p.id] + "\t" + deltaConditionalInverse[p.id]);
		   }
	   }
	}
	
	public void printMultigrams(String filename)
	{
		try
		{
			java.io.PrintStream f = new java.io.PrintStream(filename);
			if (Options.getOptionBoolean("useOldPatternOutputMode",false))
			{
				printMultigramsOld(f);
                	} else
				bestMatchStatistics(f);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void pruneMultigrams(int max)
	{
		for (int i=0; i < this.nMultigrams(); i++)
		{
			multigramSet.setScore(i,delta[i]);
		}
		Iterator<JointMultigram> i = multigramSet.sort();
		int k=0;
	  while (i.hasNext())
	  {
		   JointMultigram p = i.next();
		   if (p.isSingleton()) continue; // keep all singletons
		   if (k > max)
		  	 delta[p.id] = 0;
	     k++;
	  }
	}
	
	public void printAlignments(String fileName)
	{
		try
		{
		   FileWriter f = new FileWriter(fileName);
		   printAlignments(new PrintWriter(f));
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public void printAlignments(PrintWriter out) 
	{
		for (Dataitem item: dataset.items)
		{
			Candidate cand = item.best_match;
			SegmentationGraph g = cand.segmentationGraph;
			String segmentation = "";
			for (AlignmentSegmenter.Transition t: g.edgeSet())
			{
				//JointMultigram m = multigramSet.getMultigramById(t.multigramId);
				// double d = Math.exp(m.lhs.length() + m.rhs.length());
				double d = weightOf(t.multigramId);
				//System.err.println("weight for "  + m +  " = " + d);
				g.setEdgeWeight(t,d); // weightOf(t.multigramId)); // what about infinity
			}
			List<AlignmentSegmenter.Transition> bestPath = AlignmentSegmenter.getShortestPath(g);
			for (AlignmentSegmenter.Transition t1: bestPath)
			{
				JointMultigram m = multigramSet.getMultigramById(t1.multigramId);
				segmentation += "[" + m  + "]";
			}
			// System.out.println(item.lemma + "\t" + segmentation);
			out.println(item.lemma + "\t" + cand.wordform + "\t" + item.target + "\t" + segmentation);
		}
		out.close();
	}
	
	public void bestMatchStatistics(PrintStream out)
	{
		//HashMap<JointMultigram, List<AlignmentSegmenter.Transition>> occurrences = new HashMap<JointMultigram, List<AlignmentSegmenter.Transition>>();
		final HashMap<JointMultigram, Integer> counts =
			 new HashMap<JointMultigram, Integer>();
		MultigramSet.Statistics stats= multigramSet.statistics(dataset,true);

		
		for (Dataitem item: dataset.items)
		{
			Candidate cand = item.best_match;
			SegmentationGraph g = cand.segmentationGraph;
			
			for (AlignmentSegmenter.Transition t: g.edgeSet())
			{
				// JointMultigram m = multigramSet.getMultigramById(t.multigramId);
				// double d = Math.exp(m.lhs.length() + m.rhs.length());
				double d = weightOf(t.multigramId);
				//System.err.println("weight for "  + m +  " = " + d);
				g.setEdgeWeight(t,d);  // weightOf(t.multigramId)); // what about infinity
			}
			List<AlignmentSegmenter.Transition> bestPath = AlignmentSegmenter.getShortestPath(g);
			for (AlignmentSegmenter.Transition t1: bestPath)
			{
				JointMultigram m = multigramSet.getMultigramById(t1.multigramId);			
				if (counts.get(m) != null)
					counts.put(m, counts.get(m)+1);
				else
					counts.put(m,1);
			}
		}
		@SuppressWarnings("unused")
		double N = 0;
		for (JointMultigram m: counts.keySet())
		{
			N += counts.get(m);
		}
		Comparator<JointMultigram> c = new Comparator<JointMultigram>()
		{
			public int compare(JointMultigram m1,  JointMultigram m2)
			{
				return (counts.get(m2) - counts.get(m1));
			}
		};
		List<JointMultigram> l = new ArrayList<JointMultigram>(counts.keySet());
		Collections.sort(l,c);
		for (JointMultigram m: l)
		{
			double p_cond_lhs = counts.get(m) / stats.LHSCount(m);
			double p_cond_rhs = counts.get(m) / stats.RHSCount(m);
			double p_joint = counts.get(m) / new Double(stats.totalLengthLHS); // of RHS?
			boolean printIt = !m.isSymmetric();
			if (Options.getOptionBoolean("forbidInsertsAndDeletes",false))
                          printIt &= !(m.isInsertion() || m.isDeletion());
			if (printIt)
						out.println(m + "\t" + delta[m.id] + "\t" + p_cond_lhs + "\t" + p_cond_rhs + "\t" + p_joint + "\t" + counts.get(m));
		}
	}
	
	
/**
 * Take over alphabet and initial parameters from the 0-1/0-1 case
 * @param t unigram transducer
 * @param dataset dataset to estimate parameters from
 */
	
	private void initializeFromSingletonTransducer(UnigramTransducer t, Dataset dataset)
	{
		this.dataset = dataset;
		baseTransducer = t;
		multigramSet = new MultigramSet(baseTransducer);
		multigramSet.order = MODEL_ORDER;
		for (int i=0; i < dataset.size(); i++)
		{
			Dataitem item = dataset.get(i);
			for (Candidate c: item.candidates)
			{
				// niet netjes om dit hier te maken
				Alignment w = new Alignment(this.baseTransducer, 
						c.coded_wordform, item.coded_target, MAX_ALIGNMENTS );
				c.alignmentGraph = w.restrictedGraph;
				// System.err.println("Item " + i + ": " + c.wordform + ": alignment graph has  " 	+ c.alignmentGraph.edgeSet().size() +  " egdes");
				this.getInitialMultigramCounts(c.coded_wordform, item.coded_target, 
						c.alignmentGraph, c.lambda);
			}		
		}
	}
	
/**
 * Initialize from 0-1/0-1 alignments  as obtained from stochastic edit distance
 * @param x
 * @param y
 * @param gxy
 * @param lambda 
 */
	private void getInitialMultigramCounts(Alphabet.CodedString x, Alphabet.CodedString y, 
			AlignmentGraph gxy, double lambda)
	{
		current_alpha = baseTransducer.forwardEvaluate(x,y);
		current_beta = baseTransducer.backwardEvaluate(x,y);
		for (Alignment.Position p: gxy.vertexSet())
		{
			countMultigramsFrom(gxy,p,lambda);
		}
		//this.nMultigrams = multigramSet.size();
	}

	transient Alignment.Transition[] history = new Alignment.Transition[MAXIMUM_HISTORY_LENGTH];
	transient private int[] symbolHistory  = new int[MAXIMUM_HISTORY_LENGTH];

	private double[] deltaConditional;

	private double[] deltaConditionalInverse;

	/**
	 * Initial multigram counting from singleton transducer: count multigrams starting a node
	 * @param g
	 * @param from
	 */

	private void countMultigramsFrom(Alignment.AlignmentGraph g, Alignment.Position from, double lambda)
	{
		countMultigramsFromTo(g, from, from, 0, lambda);
	}

	/**
	 * Initial multigram counting from singleton transducer: count multigrams along a path between nodes
	 * @param g alignment graph (cf. Alignment.class)
	 * @param from Node where current sequence starts
	 * @param to Node where current sequence ends
	 * @param p current sequence length
	 * @param lambda weight of current match candidate
	 */

	private void countMultigramsFromTo(AlignmentGraph g, Position from, Position to, int p, double lambda) 
	{
		if (p >= MODEL_ORDER)
		{
			return;
		}
		for (Alignment.Transition t: g.outgoingEdgesOf(to))
		{
			history[p] = t;
			symbolHistory[p] = t.symbol;
			// now count the stuff
			Position target = g.getEdgeTarget(t);
			if (p >= 0)
			{
				double w=0;
				// MultigramSet<Double>.Node n = multigramCounter.startNode.insert(symbolHistory, 0, p+1);
				//System.err.println("at Position " + to + "; at node: " + n);
				for (int i=0; i <= p; i++) 
					w += g.getEdgeWeight(history[i]);

				double alpha = current_alpha[from.lpos][from.rpos];
				double beta = current_beta[target.lpos][target.rpos]; // to: at target
				double alphaEnd = current_alpha[g.endPosition.lpos][g.endPosition.rpos];
				double delt = Math.exp(-1 *w);

				//System.err.printf("alpha=%e beta=%e alphaEnd=%e delt=%e\n", alpha,beta,alphaEnd,delt);
				w = alpha * beta  * delt;
				w = lambda * w / alphaEnd;
				//System.err.println("total added weight: " + w + "\n");
				if (!Double.isNaN(w)) // wrong: leaves unscored multigrams
				{
					MultigramSet.Node n = 
						multigramSet.startNode.insert(symbolHistory, 0, p+1);
				  // TODO: scoring alleen als niet in de development set
					multigramSet.addToScore(n.multigramId, w);
				}
			}
			countMultigramsFromTo(g, from, g.getEdgeTarget(t), p+1, lambda);
		}
	}

	/**
	 * Forward and backward evaluation functions.<br>
	 * This could be nicely wrapped in some more general folding functional, but never mind!
	 * <p>
	 * @param gxy the segmentation graph for the current word pair
	 * @param backward true iff evaluation direction is backward
	 * @return The forward parameters α[] or β[], indexed by position in the segmentation graph
	 * <p>
	 * 
	 */

	private double[] TopologicalOrderEvaluation(SegmentationGraph gxy, boolean backward)
	{
		// first obtain a graph with transitions according to the current multigram set
		double [] alpha = new double[gxy.vertexSet().size()];
		alpha[gxy.startPosition.index] = 1.0;
		if (backward)
			alpha[gxy.endPosition.index] = 1; // this.stoppingProbability;
		Iterator<Position> i = 
			backward ? gxy.getBackwardIterator() : gxy.getForwardIterator(); 
			while (i.hasNext())
			{
				Position p = i.next();
				for (AlignmentSegmenter.Transition t: 
					backward? gxy.outgoingEdgesOf(p): gxy.incomingEdgesOf(p))
				{
					Position prevNode = backward? gxy.getEdgeTarget(t) : gxy.getEdgeSource(t);
					double prevWeight = alpha[prevNode.index];
					alpha[p.index] += prevWeight * delta[t.multigramId];
					//double x= alpha[p.index];
					//double y=1.0;
					// now where to store the alpha?
				}
				// now check all incoming egdes
			}
			return alpha;
	}

	private double[] forwardEvaluation(SegmentationGraph gxy)
	{
		return TopologicalOrderEvaluation(gxy,false);
	}

	private double jointProbability(SegmentationGraph gxy)
	{
		double[] alpha = forwardEvaluation(gxy);
		double alphaEnd = alpha[gxy.endPosition.index];
		if (Double.isNaN(alphaEnd))
		{
			System.err.println("Fatal error: Joint probabilty undefined!");
			System.exit(1);
		}
		if (alphaEnd < 0)
		{
			System.err.println("Warning: Joint probability < 0");
		}
		return alphaEnd;
	}

	private double expectationMaximization(Dataset d)
	{
		this.dataset = d;
		int m = d.size();
		//System.err.println("beep " + m);
		if (useConditionalProbabilities)
			this.computeConditionalProbabilities();
		
		double [] Γ = new double[nMultigrams()]; //HOHO: alphabet sizes meenemen

		for (int i=0; i < nMultigrams(); i++) Γ[i] = 0;
		double Γ_stop = 0.0;
		double scale = 1.0;
		
		System.err.println("Expectation steps...");

		for (int i=0; i < m; i++)
		{
			Dataitem item = dataset.get(i);
			if (item.heldOut)
				continue;
			for (Candidate c: item.candidates)
			{
				Γ_stop = expectationStep(c.segmentationGraph, Γ, Γ_stop, c.lambda, scale);
				/*
	        stop_d = expectationStep(dataset.items.get(i).coded_wordforms.get(j), 
	        		 dataset.items.get(i).coded_target, 
	                 Γ,stop_d, dataset.lambda(i,j));
				 */
			}
		}

		maximizationStep(Γ, Γ_stop); // i.e. just dump normalized Γ into delta
		double logLikelihood = 0;
		@SuppressWarnings("unused")
		double heldoutLogLikelihood = 0;
		
		// Set the lambdas and pick best matches..
		for (Dataitem item: dataset)
		{
			double norm = 0;
			//Alphabet.CodedString ct =  item.coded_target;
			for (Candidate cand: item.candidates)
			{
				//Alphabet.CodedString cwf = cand.coded_wordform;
				cand.lambda = jointProbability(cand.segmentationGraph); // dit doe je dus eigenlijk dubbel
				if (dataset.has_frequency)
					cand.lambda = cand.lambda * cand.frequency;
				norm += cand.lambda;			
				//System.err.println("lambda = " + cand.lambda);
			}
			//System.err.println("norm = " + norm);
			if (item.heldOut)
				heldoutLogLikelihood += Math.log(norm);
			else 
				logLikelihood += Math.log(norm);
			double lambda_max = -1;
			boolean pickedBestMatch = false;
			for (Candidate cand: item.candidates)
			{
				cand.lambda /= norm;
				if (cand.lambda > lambda_max)
				{
					lambda_max = cand.lambda;
					item.best_match = cand;
					pickedBestMatch = true;
				}
			}
			if (!pickedBestMatch)
			{
				System.err.println("no best match picked for:" + item.target);
			}
		}
		//System.err.println("Held out likelihood " + heldoutLogLikelihood);
		return logLikelihood;
	}

	/**
	 * Problems:
	 * <ol>
	 * <li>what if there is no segmentation
	 * <li>what if α[gxy.endPosition.index] is zero or too small
	 * </ol>
	 * @param gxy current segmentation graph
	 * @param Γ evidence accumulation array
	 * @param Γ_stop stopping probability
	 * @param scale always  1
	 * @param lambda 
	 * @return new value for Γ_stop
	 */

	private double expectationStep(SegmentationGraph gxy, double[] Γ, double Γ_stop, 
			double lambda, double scale)
	{
		double[] alpha = TopologicalOrderEvaluation(gxy,false);
		double[] beta = TopologicalOrderEvaluation(gxy,true);
		double alphaEnd = alpha[gxy.endPosition.index];
		//double betaStart = beta[gxy.startPosition.index];

		if (alphaEnd == 0.0 || Double.isInfinite(1/alphaEnd))
		{
			System.err.println("alpha ends on zero after forward evaluation !! " + alphaEnd);
			return Γ_stop;
		}

		Γ_stop += scale * lambda;

		Iterator<Position> i = gxy.getForwardIterator();
		while (i.hasNext())
		{
			Position p = i.next();
			double norm = scale * lambda * beta[p.index] / alphaEnd;
			for (AlignmentSegmenter.Transition t: gxy.incomingEdgesOf(p))
			{
				Position prevNode = gxy.getEdgeSource(t);
				Γ[t.multigramId] += alpha[prevNode.index] * delta[t.multigramId] * norm;
				if (Double.isNaN(Γ[t.multigramId]))
				{
					//System.err.println("whoopsie");
				}
			}
		}
		return Γ_stop;
	}
	
	/**
	 * Dus hoe zat het ook alweer:
	 * "Links" is het moderne woord, "rechts" het historische?
	 * dus "links" gaat over de 'kandidaten' en rechts over de 'target'?
	 * En in de datafile
	 * <br> lemma |  kandidaten | target<br>
	 * en we printen:<br> multigram |  delta(M) | fr. (M) / fr l(M) |  fr(M) / fr r(M)
	 */
	private void computeConditionalProbabilities() // this is stupid and needs to be fixed immediately!
	{
		// java.util.HashMap<String,Double> lhsMap = new java.util.HashMap<String,Double> (); 
		// java.util.HashMap<String,Double> rhsMap = new java.util.HashMap<String,Double> (); 
		MultigramSet.Statistics stats = multigramSet.statistics(dataset, false);

		this.deltaConditional = new double[delta.length];
		this.deltaConditionalInverse = new double[delta.length];
		for (JointMultigram m: multigramSet)
		{
			deltaConditional[m.id] =  delta[m.id] / stats.LHSRelativeFrequency(m);
			deltaConditionalInverse[m.id] = delta[m.id] / stats.RHSRelativeFrequency(m);
		}
	}

	/**
	 * sets probabilities delta according to counts accumulated in expe
	 * @param Γ
	 * @param stop_d
	 * @return
	 */
	
	private void maximizationStep(double[] Γ, double Γ_stop) // stop_d was reference
	{
		System.err.println("Maximization step");
		double N =  Γ_stop;

		for (int i=0; i < nMultigrams(); i++)
		{
			if (delta[i] == 0 && ! (Γ[i] == 0))
			{
				System.err.println("this cannot be happening " + i  + " " +multigramSet.getMultigramById(i) );
				System.exit(1);
			}
			if (Double.isNaN(Γ[i]) || Double.isInfinite(Γ[i]))
			{
				System.err.println("Oh nee ..." + Γ[i] +  " in " + multigramSet.getMultigramById(i));
			}
			N += Γ[i];
		}

		for (int i=0; i < nMultigrams(); i++)
		{
			if (Γ[i] < this.EVIDENCE_THRESHOLD)
			{
				delta[i] = 0.0; // fade out this multigram
				continue;
			}
			double deltaNew = Γ[i] / N;
			if (delta[i] == 0 && ! (deltaNew == 0))
			{
				System.err.println("this cannot be happening " + i  + " " +multigramSet.getMultigramById(i) );
				System.exit(1);
			}
			delta[i]= Γ[i] / N; // wat te doen als beiden nul (dan hele ding op nul zetten?)
			if (Double.isNaN(delta[i]))
			{
				System.err.println("Fatal: cannot set delta[" + i + "]: N = " + N + " Γ = " + Γ[i]);
				System.exit(1);
			}
		}
		stoppingProbability = Γ_stop / N;
	}


	private double weightOf(int multigramId) 
	{
		double w = -Math.log(delta[multigramId]);
		if (Double.isNaN(w)) 
		{
			w = Double.POSITIVE_INFINITY;
			w = 1e50;
		}
		return w;
	}
	
	public void setUniformInitialDistribution()
	{
		// p(k,l) = 1 / N(k,l) = 1 / [in]^k [out]*l
		double Ntotaal = 0;
		for (int k=0; k <= MODEL_ORDER; k++)
		{
			for (int l=0; l <= MODEL_ORDER; l++)
			{
			   double Nkl = Math.pow(1 / baseTransducer.inputAlphabet.size ,k) * 
			              Math.pow(1/ baseTransducer.outputAlphabet.size, l);
			   Ntotaal += Nkl;
			}
		}
		for (int i=0; i < this.nMultigrams(); i++)
		{
			delta[i] = 1 / Ntotaal;
		}
	}
	
	public static void usage()
	{
	}

	public void FrenchG2PTest(Dataset d)
	{	
	  	this.MAX_ALIGNMENTS = 5;
		this.MODEL_ORDER = 4;
		this.multigramPruner = new FrenchG2PMultigramPruner();
		this.estimateParameters(d);
		this.printAlignments(Options.getOption("alignmentOutput"));
	}
	
	public void FrenchTest(Dataset d)
	{
		//patternsets/frenchG2P.graphemes
	  	this.MAX_ALIGNMENTS = 5;
		this.MODEL_ORDER = 4;
		this.multigramPruner = new LHSConstrainedMultigramPruner("patternsets/frenchG2P.graphemes");
		this.estimateParameters(d);
		this.printAlignments(Options.getOption("alignmentOutput"));
	}

	public void CzechTest(Dataset d)
	{
		// patternsets/frenchG2P.graphemes
		this.MAX_ALIGNMENTS = 5;
		this.MODEL_ORDER = 4;
		this.multigramPruner = new LHSConstrainedMultigramPruner(
				"patternsets/czech.graphemes");
		this.estimateParameters(d);
		this.printAlignments(Options.getOption("alignmentOutput"));
	}
	
	public void DutchTest(Dataset d)
	{	
	  this.MAX_ALIGNMENTS = 5;
		this.MODEL_ORDER = 3;
		this.multigramPruner = new DutchMultigramPruner();
		this.estimateParameters(d);
		this.printAlignments(Options.getOption("alignmentOutput"));
		//this.bestMatchStatistics();
	}
	
	public void doJob(Dataset d)
	{
		this.MAX_ALIGNMENTS = 5;
		this.MODEL_ORDER = 3;
		try
		{
			MODEL_ORDER = Options.getOptionInt("multigramLength", MODEL_ORDER);
			String prunerClass = Options.getOption("pruner", "spellingvariation.DutchMultigramPruner");
			if (!prunerClass.equals("none"))
			  this.multigramPruner = (MultigramPruner) Class.forName(prunerClass).newInstance();
			this.estimateParameters(d);
			this.printAlignments(Options.getOption("alignmentOutput"));
		} catch (Exception e)
		{
       e.printStackTrace();
		}
	}
	
	public static void main(String [] argv)
	{
		int argc = argv.length; 
		new Options(argv);
		Options.list();
		if (argc < 1)
		{
			usage();
			System.exit(1);
		}

		Dataset d = new Dataset();
		d.addWordBoundaries = Options.getOptionBoolean("addWordBoundaries", true);
		d.read_from_file(Options.getOption("trainFile"));

		System.err.println("MultigramTransducer --  data read: " + d.size() + " items");

		MultigramTransducer t = new MultigramTransducer();
	  t.doJob(d);
		//t.DutchTest(d);
	}
}
