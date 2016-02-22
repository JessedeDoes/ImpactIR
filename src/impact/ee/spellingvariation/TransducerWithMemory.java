package impact.ee.spellingvariation;


import impact.ee.spellingvariation.Alignment.Position;
import impact.ee.spellingvariation.AlignmentSegmenter.SegmentationGraph;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;



/**
 * This follows Bisani and Ney 2008
 * 
 * EM procedure to estimate parameters for an N-gram model over joint multigrams.
 * Actually, it seems N-gram models with high N over joint unigrams perform best.
 * @author jesse
 *
 */

public class TransducerWithMemory implements java.io.Serializable
{
	private static final long serialVersionUID = 7844159849339402087L;
	protected  int MODEL_ORDER = 4;
	private MultigramTransducer memorylessTransducer;
	private transient Dataset dataset;
	private transient ScoredState[] scoredStateMap;
	private transient ScoredState[] scoredStateMap1; // = new StateAndProbability[histories.size()];
	History histories;
	//JavaSparseArray transitionProbabilities; // = new JavaSparseArray();
	protected int MAX_ITERATIONS = 10;

	public TransducerWithMemory(MultigramTransducer mt)
	{
		memorylessTransducer = mt;
		dataset = mt.dataset;
	}

	public MultigramSet getMultigramSet()
	{
		return this.memorylessTransducer.multigramSet;
	}
	
	public History getHistory()
	{
		return this.histories;
	}
	/**
	 * HM why not simply limit attention to histories 
	 * actually occurring in the current item?
	 * (which means building the history set per candidate-item,
	 * and no sparse graph stuff needed to evaluate the function)
	 * @param g
	 */

	static class ScoredState extends impact.ee.util.ShortestPath.BasicState
	{
		History.State state;
		double score = 0;
		boolean isFinal =false;
		Vector<Link> links = new Vector<Link>();
		
		static class Link extends impact.ee.util.ShortestPath.BasicEdge
		{
			public Link(int m, ScoredState n)
			{
				multigramId = m;
				next = n;
			}
			public String toString()
			{
			  return multigramId + " -> " + next.state + " : " + next.isFinal;
			}
			int multigramId;
			ScoredState next;
		}

		public ScoredState(History.State s)
		{
			state = s;
		}
		
		public ScoredState(History.State s, double w)
		{
			state = s;
			score = w;
		}
		
		public void addLink(int multigramId, ScoredState nextScoredState)
		{
			links.add(new Link(multigramId,nextScoredState));
		}

		@Override
		public boolean isFinal()
		{
			// TODO Auto-generated method stub
			return isFinal;
		}

		
		public void relaxEdges(impact.ee.util.ShortestPath sp, impact.ee.util.ShortestPath.MatchState source)
		{
			ScoredState ss = (ScoredState) source.base;
			for (Link l: ss.links)
			{
				double delta = ss.state.conditionalProbability(l.multigramId);
				sp.relaxEgde(l, source, l.next, (int) (-100 * Math.log(delta)));
			}
		}
	}

	/**
	 * the values of p(m | s) are stored in the History objects
	 * @param s
	 * @param multigramId
	 * @return
	 */
	protected double conditionalP(History.State s, int multigramId)
	{
		return s.conditionalProbability(multigramId);
	}

	/**
	 * This builds a rather elaborate representation and is likely to be very slow.
	 * Can´t we keep this representation during iterations??
	 * Seems unavoidable, otherwise much too slow. 
	 * alternative would be huge (sparse?) matrix
	 * @param g
	 * @return
	 */

	private Vector<Vector<ScoredState>> forwardEvaluation(SegmentationGraph g)
	{
		Vector<Alignment.Position> forwardVector =  g.getForwardVector();
		Vector<Vector<ScoredState>> stateLists = new Vector<Vector<ScoredState>>();

		//nl.openconvert.log.ConverterLog.defaultLog.println("bloep");
		@SuppressWarnings("unused")
		int graphSize = 0;
		// make next variable global

		// ToDo initialization..
		// history class should take care of starting up histories initially etc.
		
		for (int i=0; i < forwardVector.size(); i++)
		{
			// recall we set index on positions according to a topological order
			Vector<ScoredState> stateList = new Vector<ScoredState>();
			stateLists.add(stateList);

			if (i == 0) // start state
			{
				ScoredState sNs = new ScoredState(histories.startState);
				sNs.score = 1;
				stateList.add(sNs);
				continue;
			}
			
			Position p = forwardVector.get(i);
			boolean wordFinal = (p == g.endPosition);
			for (AlignmentSegmenter.Transition t: g.incomingEdgesOf(p))
			{
				Position prevNode = g.getEdgeSource(t);
				Vector<ScoredState> previousStates = stateLists.get(prevNode.index);
				for (ScoredState sNs: previousStates)
				{
					History.State previousState = sNs.state;
					History.State nextState = previousState.advance(t.multigramId);
					//nl.openconvert.log.ConverterLog.defaultLog.println(previousState +  "->" + nextState + " (" + t.multigramId + ")");
					if (nextState != null)
					{
						double w = conditionalP(previousState, t.multigramId);
						//nl.openconvert.log.ConverterLog.defaultLog.println("forward(" + i + "): " + previousState +  "->" + nextState + " (" + t.multigramId + "," + w + ")");
						double deltaP = 0;
						if ( (deltaP = w * sNs.score) > 0)
						{
							ScoredState nextScoredState;
							if (scoredStateMap[nextState.index]==null)
							{
								nextScoredState = new ScoredState(nextState);
								nextScoredState.isFinal = wordFinal;
								scoredStateMap[nextState.index] = nextScoredState;
								graphSize++;
								//nl.openconvert.log.ConverterLog.defaultLog.println("add state at " + i + ": " + nextState);
								stateList.add(nextScoredState);
							} else
							{
								nextScoredState = scoredStateMap[nextState.index];
							}
							sNs.addLink(t.multigramId, nextScoredState);
							nextScoredState.score += deltaP;
						} else
						{
							//nl.openconvert.log.ConverterLog.defaultLog.println("no fun for " + sNs.state + ": " + sNs.score);
						}
					}
				}
			}
			for (ScoredState sNs: stateList)
			{
				scoredStateMap[sNs.state.index] = null;
			}
		}
		//nl.openconvert.log.ConverterLog.defaultLog.println("graph size="  + graphSize);
		return stateLists;
	}

	/**
	 * In computing the beta values, we restrict to states with nonzero alpha value.
	 * This does not have any impact on the collected evidence values (or so i hope)
	 * @param g
	 * @param alphas
	 * @return
	 */
	private Vector<Vector<ScoredState>> backwardEvaluation(SegmentationGraph g, 
																						Vector<Vector<ScoredState>> alphas)
	{
		Vector<Alignment.Position> forwardVector =  g.getForwardVector();
		Vector<Vector<ScoredState>> stateLists = new Vector<Vector<ScoredState>>();
		stateLists.setSize(forwardVector.size());

		// make next variable global

		// ToDo initialization..
		// history class should take care of starting up histories initially etc.
		// nl.openconvert.log.ConverterLog.defaultLog.println(forwardVector.size());
		
		for (int i = forwardVector.size()-1; i >= 0; i--)
		{
			Vector<ScoredState> stateList = new Vector<ScoredState>();
			stateLists.setElementAt(stateList,i);
			if (i == forwardVector.size()-1) // final state.. how to initialize?
			{
				for (History.State s: histories.wordFinalStates)
				{
					ScoredState sNs = new ScoredState(s); // ??
					sNs.score = 1;
					stateList.add(sNs);
				}
				continue;
			}
			
			Vector<ScoredState> alphaStates = alphas.get(i);

			Position p = forwardVector.get(i);
			
			for (AlignmentSegmenter.Transition t: g.outgoingEdgesOf(p))
			{
				Position nextNode = g.getEdgeTarget(t);
				//nl.openconvert.log.ConverterLog.defaultLog.println(i + "->" + nextNode.index); // of course...
				Vector<ScoredState> nextStates = stateLists.get(nextNode.index);
				
				for (ScoredState sNs: nextStates)
				{
					 scoredStateMap1[sNs.state.index] = sNs;
				}
				
				for (ScoredState sNs: alphaStates)
				{
					History.State nextState = sNs.state.stateTransition(t.multigramId);
					if (nextState != null)
					{
						ScoredState nextScoredState = scoredStateMap1[nextState.index];
						if (nextScoredState != null)
						{
							double w = conditionalP(sNs.state, t.multigramId);
							//nl.openconvert.log.ConverterLog.defaultLog.println("backward:(" + i + ") "+ 
									//sNs.state +  "->" + nextState + " (" + t.multigramId + "," + w + ")");
							double deltaP = w *  nextScoredState.score;
							if (deltaP > 0)
							{
								ScoredState prevScoredState = scoredStateMap[sNs.state.index];
								if (prevScoredState == null)
								{
									prevScoredState = new ScoredState(sNs.state);
									scoredStateMap[sNs.state.index] = prevScoredState;
									stateList.add(prevScoredState);
								}
								prevScoredState.score += deltaP;
							} 
						}
					}
				}
				// cleanup Map1
				for (ScoredState sNs: nextStates)
				{
					 scoredStateMap1[sNs.state.index] = null;
				}
			}
			// cleanup Map
			for (ScoredState sNs: stateList)
			{
				scoredStateMap[sNs.state.index] = null;
			}
		}
		return stateLists;
	}

	/**
	 * make the link pointers in alpha point to the scored states in beta instead of alpha..
	 */

	private void connectAlphaBeta(SegmentationGraph g, 
			Vector<Vector<ScoredState>> alpha, 
			Vector<Vector<ScoredState>> beta)
	{
		Vector<History.State> used = new Vector<History.State>();
		for (Vector<ScoredState> b: beta)
		{
			for (ScoredState s: b)
			{
				if (scoredStateMap[s.state.index] == null)
				{
					scoredStateMap[s.state.index] = s;
					used.addElement(s.state);
				}
			}
		}

		for (Vector<ScoredState> a: alpha)
		{
			for (ScoredState s: a)
			{
				for (ScoredState.Link l: s.links)
				{
					// PAS OP dit kan l.next gelijk aan NULL maken!
					l.next = scoredStateMap[l.next.state.index];
					if (l.next != null)
					{
						//nl.openconvert.log.ConverterLog.defaultLog.println("beep...");
					} else
					{
						nl.openconvert.log.ConverterLog.defaultLog.println("more or less killing state "  + s.state);
					}
				}
			}
		}

		for (History.State s: used)
		{
			scoredStateMap[s.index] = null;
		}
	}

	private void expectationStep(SegmentationGraph g, double lambda)
	{
		Vector<Vector<ScoredState>> alphas = forwardEvaluation(g);
		Vector<Vector<ScoredState>> betas = backwardEvaluation(g,alphas);
		//Vector<Alignment.Position> forwardVector = g.getForwardVector();

		connectAlphaBeta(g, alphas, betas);

		double alphaEnd = 0; 
		for (ScoredState ss: alphas.get(g.endPosition.index))
			alphaEnd += ss.score;


		for (int i=0; i < g.getForwardVector().size(); i++)
		{
			//Position p = forwardVector.get(i);
			Vector<ScoredState> alpha_i = alphas.get(i);
			for (ScoredState sNs: alpha_i)
			{
				double alpha = sNs.score; 
				for (ScoredState.Link l: sNs.links)
				{
					if (l.next != null)
					{
						double beta = l.next.score;
						double e = lambda * alpha * beta * conditionalP(sNs.state, l.multigramId) 
												/ alphaEnd;
						addToEvidence(sNs.state, l.multigramId, e);
					}
				}
			}
		}
	}

	protected double jointProbability(SegmentationGraph g)
	{
		Vector<Vector<ScoredState>> alphas = forwardEvaluation(g);
	
		double alphaEnd = 0; 
		for (ScoredState ss: alphas.get(g.endPosition.index))
			alphaEnd += ss.score;
		return alphaEnd;
	}
	
	protected void printBestAlignment(SegmentationGraph g)
	{
		try
		{
			Vector<Vector<ScoredState>> alphas = forwardEvaluation(g);
			impact.ee.util.ShortestPath<ScoredState, ScoredState.Link> sp = new impact.ee.util.ShortestPath<ScoredState, ScoredState.Link>();
			java.util.List <ScoredState.Link> path =  sp.bestFirstSearch(alphas.get(0).get(0));
			if (path != null)
			{
				for (ScoredState.Link l: path)
				{
					System.out.print("[" + this.getMultigramSet().getMultigramById(l.multigramId) + "]");
				}
				System.out.println("");
			} else
			{
				nl.openconvert.log.ConverterLog.defaultLog.println("Could not compute shortest path!");
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void addToEvidence(History.State state, int multigramId, double e)
	{
		// this could be faster with other data representation
		//nl.openconvert.log.ConverterLog.defaultLog.println("there is evidence "  + e + "  at " + state + "  for " + 
	  	 //this.memorylessTransducer.multigramSet.getMultigramById(multigramId));
		if (Double.isNaN(e) || Double.isInfinite(e) || e == 0)
		{
			// nl.openconvert.log.ConverterLog.defaultLog.println("Bah in addToEvidence" + e  + " state:" +  state);
		}
		state.addEvidence(multigramId, e);
	}

	public void estimateParameters()
	{
		// prepare the histories

		histories = new History();
		histories.codeInterpreter = this.getMultigramSet();
		histories.MODEL_ORDER = this.MODEL_ORDER;
		nl.openconvert.log.ConverterLog.defaultLog.println("Start building history transition diagram .. ");
		histories.insertHistoriesFromGraphs(dataset.getGraphIterator());
		scoredStateMap = new ScoredState[histories.size()];
		scoredStateMap1 = new ScoredState[histories.size()];
		// initialize conditional probabilities from memoryless model
		// ok this does not work!
		nl.openconvert.log.ConverterLog.defaultLog.println("Initialize parameters from memoryless model ..");
		
		for (History.State s: histories) // this should include the start state!
		{
			double denominator = 0;

			for (History.Transition t: s.getStateTransitions()) // hoho gevaarlijk: alleen voor, etc.
			{
				denominator += this.memorylessTransducer.delta[t.symbol];
			}

			for (History.Transition t: s.getStateTransitions())
			{
				t.p =  this.memorylessTransducer.delta[t.symbol] / denominator;
			  // nl.openconvert.log.ConverterLog.defaultLog.println(s +  "->" + t.target + " (" + t.symbol + "="  + t.p + ")");
			}
		}
		
		nl.openconvert.log.ConverterLog.defaultLog.println("Start expectation mazimization .. ");
		for (int i=0; i < MAX_ITERATIONS; i++)
		{
			nl.openconvert.log.ConverterLog.defaultLog.println(this.getClass().getName() + ": iteration " + i);
			expectationMaximization();
		}
	}

	private void collectPartialEvidence()
	{
		for (History.State s: histories)
		{
			for (History.Transition t: s.getStateTransitions()) t.partialEvidence  = t.evidence;
		}
	}
	
	private void expectationMaximization()
	{
		// Expectation steps: accumulate evidence according to current mode
		int k=0;
		this.histories.resetEvidence(); // reset all state and transition evidence to 0
		for (Dataitem item: dataset)
		{
			if (k % 500 == 0)
				nl.openconvert.log.ConverterLog.defaultLog.println(this.getClass().getName() + ": Start expectation steps for items from " + k);
			for (Candidate c: item.candidates) expectationStep(c.segmentationGraph, c.lambda);
			if (k == (9 * dataset.size()) / 10)
			{
				nl.openconvert.log.ConverterLog.defaultLog.println("start collecting partial evidence " + k);
				collectPartialEvidence();
			}
			k++;
		}
		
		// maximization step: update parameters according to accumulated evidence
		
		double totalHeldoutEvidence=0;
		double unseenEvidence=0;
		
		for (History.State s: histories)
		{
			double denominator = 0;

			for (History.Transition t: s.getStateTransitions())
			{
				denominator += t.evidence;
				if  (t.partialEvidence == 0)
				{
					//nl.openconvert.log.ConverterLog.defaultLog.println("never seen before:" + s + this.getMultigramSet().getMultigramById(t.symbol));
					unseenEvidence += t.evidence;
				} else
				{
					//nl.openconvert.log.ConverterLog.defaultLog.println("Yep seen before:" + s + this.getMultigramSet().getMultigramById(t.symbol));
				}
				totalHeldoutEvidence += t.evidence - t.partialEvidence;
				t.partialEvidence=0; // reset  for next time.
			}
	
			if (Double.isNaN(denominator) || Double.isInfinite(denominator) || denominator == 0)
			{
				// nl.openconvert.log.ConverterLog.defaultLog.println("Bah in maximization step: denominator = " + denominator + " in state " + s);
				continue;
			}
			
			for (History.Transition t: s.getStateTransitions())
				t.p = t.evidence / denominator;
				// nl.openconvert.log.ConverterLog.defaultLog.println("set probability: " + s +  "->" + t.target + " (" + t.symbol + "="  + t.p + ")");
			s.evidence = denominator;
		
			// TODO: recompute the lambda's!!
		}
		
		double logLikelihood = 0;
		@SuppressWarnings("unused")
		double heldoutLogLikelihood = 0;
		
		nl.openconvert.log.ConverterLog.defaultLog.println("Smoothing info: % unseen evidence: " + (unseenEvidence / totalHeldoutEvidence) + " .. " + totalHeldoutEvidence );
		
		// Set the lambdas and pick best matches..
		
		for (Dataitem item: dataset)
		{
			double norm = 0;
		
			for (Candidate cand: item.candidates)
			{
				cand.lambda = jointProbability(cand.segmentationGraph); // dit doe je dus eigenlijk dubbel
				printBestAlignment(cand.segmentationGraph);
				if (dataset.has_frequency)
					cand.lambda = cand.lambda * cand.frequency;
				norm += cand.lambda;			
				//nl.openconvert.log.ConverterLog.defaultLog.println("lambda = " + cand.lambda);
			}
			//nl.openconvert.log.ConverterLog.defaultLog.println("norm = " + norm);
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
				nl.openconvert.log.ConverterLog.defaultLog.println("no best match picked for:" + item.target);
			}
		}
		nl.openconvert.log.ConverterLog.defaultLog.println(this.getClass().getName() + ": Log likelihood now:" + logLikelihood);
	}

	public void saveToFile(String filename)
	{
		try
		{
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
			out.writeObject(this);
			out.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static TransducerWithMemory readFromFile(String filename)
	{
		try
		{
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
			TransducerWithMemory t = (TransducerWithMemory) in.readObject();
			in.close();
			return t;
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static void usage()
	{
		System.out.println("One argument: training-dataset-filename\nModel is saved to /tmp/model.out");
	}

	public static void main(String[] args)
	{
		int argc = args.length; 
		if (argc < 1)
		{
			usage();
			System.exit(1);
		}

		Dataset d = new Dataset();
		d.addWordBoundaries = true;
		d.read_from_file(args[0]);

		nl.openconvert.log.ConverterLog.defaultLog.println("Transducer with Memory: data read: " + d.size() + " items");

		MultigramTransducer t = new MultigramTransducer();
		t.MAX_ITERATIONS = 3;
		t.MODEL_ORDER = 3; // three did not work very well
		t.multigramPruner = new FrenchG2PMultigramPruner();
		t.MAX_ALIGNMENTS = 10; // for very small datasets, the first step (LSED) may go quite wrong . 
		t.printSubstitutionsOnly = false;
		t.estimateParameters(d);

		TransducerWithMemory mt = new TransducerWithMemory(t);
		mt.MODEL_ORDER = 2;
		mt.MAX_ITERATIONS = 3;
		mt.estimateParameters();
		mt.saveToFile("/tmp/model.out");
		//mt.histories.printModel(System.out, mt.getMultigramSet());
		//t.dumpAlignments(d);
	}
}

