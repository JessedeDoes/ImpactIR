package spellingvariation;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;
import spellingvariation.AlignmentSegmenter.SegmentationGraph;
import spellingvariation.Alignment.Position;
import spellingvariation.MatcherWithMemory.ScoredState;

/**
 * This class does the score keeping for histories and implements the state model interface.
 * Would be more efficient with a different Trie datastructure. (datrie)
 * @author jesse
 *
 */

public class History implements Iterable<History.State>,  
			StateModel<History.State>, java.io.Serializable
{
	private static final long serialVersionUID = -7483091014679179374L;
	public State startState;
	protected int MODEL_ORDER = 3;
	private Vector<State> finalStates = new Vector<State>(); // states ending an n-gram
	protected Vector<State> allStates = new Vector<State>();
	protected Vector<State> wordFinalStates = new Vector<State>(); 
	protected History lowerOrderHistory = null;
	protected History higherOrderHistory = null;
	protected double minP = 1e-10;
	
	/**
	 * A silly way to make states nicely printable
	 */
	
	protected CodeInterpreter codeInterpreter = null; 
	private boolean closed = false;
	// states ending an n-gram that occurs word-finally
	int size;

	private void readObject(ObjectInputStream in) throws java.io.IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		for (State s: allStates)
		{
			if (s.prolongations != null) for (Transition t: s.prolongations)
			{
				t.target = allStates.get(t.targetIndex);
			}
			if (s.stateTransitions != null) for (Transition t: s.stateTransitions)
			{
				t.target = allStates.get(t.targetIndex);
			}
		}
	}
	
	private void writeObject(ObjectOutputStream out) throws java.io.IOException
	{
		this.cleanupProlongations();
	  out.defaultWriteObject();
	}
	
	public History()
	{
		startState = new State();
		startState.setFinal(true); // the empty history is OK
	}

	/**
	 * State model transition class
	 * @author jesse
	 *
	 */
	public class Transition implements java.io.Serializable, Comparable<Transition>
	{
		private static final long serialVersionUID = 3108670837891207778L;
		/**
		 * Integer code for a multigram
		 */
		int symbol;
		double evidence = 0;
		double p = 0;
		transient State target;
		int targetIndex;
		
		/**
		 * Evidence on the non-held-out part of the data
		 */

		double partialEvidence = 0;
		
		public Transition(int c, State n)
		{
			symbol = c; target = n;
		}
		
		/**
		 * Override default serialization in order to avoid the serializability and cycles problem: replace object pointers
		 * by indexes (and reset them after reading the notes, cf. History.readObject above.
		 * @param out object output stream.
		 * @throws java.io.IOException
		 */
		
		private void writeObject(ObjectOutputStream out) throws java.io.IOException
		{
			targetIndex = target.index;
		  out.defaultWriteObject();
		}

		private void readObject(ObjectInputStream in) throws java.io.IOException, ClassNotFoundException
		{
			in.defaultReadObject();
			//target = allStates.get(targetIndex); // TODO this does NOT work
		}

		public int compareTo(Transition arg0)
		{
			// TODO Auto-generated method stub
			return symbol - arg0.symbol;
		}
	} // end of class transition

	protected void makeStateModel()
	{
		makeBackwardLinks();
		for (State s: allStates) // of toch allStates? maar dan moet je uitkijken
		{
			s.makeStateTransitions();
			//s.addBackwardBackoffTransitions(); // niet echt nodig
		}
	}
	
	private void makeBackwardLinks()
	{
		for (Transition t: startState.prolongations)
		{
			State n0 = t.target;
			n0.rightParentState = startState;
			int s0 = t.symbol;
			for (Transition t1: n0.prolongations)
			{
				State n1 = t1.target;
				State n2 = startState.prolongation(t1.symbol);
				if (n2 != null)
				{
					linkBackward(n1,n2,s0);
				}
			}
		}
	}

	/**
	 * Recursively link all states back to their right parents
	 * @param x
	 * @param y
	 * @param s0
	 */
	private void linkBackward(State x, State y, int s0)
	{
		//y.stateTransitions.add(new Transition(s0,x));
		x.rightParentState = y;
		for (Transition t: x.prolongations)
		{
			State x1 = t.target;
			State y1 = y.prolongation(t.symbol);
			if (y1 != null)
			{
				linkBackward(x1,y1,s0);
			}
		}
	}
	
	/*
	 * Inserting histories from segmentation graphs 
	 */
	
	private int[] symbolHistory = new int[1000];

	/**
	 * Store histories from this graph
	 * @param g a segmentation graph
	 */
	
	public void insertHistoriesFromGraphs(java.util.Iterator<SegmentationGraph> g)
	{
		while (g.hasNext())
		{
			this.insertHistoriesFromGraph(g.next());
		}
		System.err.println("histories inserted, start polishing .. ");
		makeStateModel(); // moet dit niet NA het opschonen van de begintoestanden?
		int index=0;
		Vector<Transition> fromStart = new Vector<Transition>();
		
		// TODO dit klopt weer niet
		

		
		// TODO: only keep final states
		for (State s: allStates) // TODO: check: transitions should go from final to final
		{
			s.index = index++;
		}
		size = allStates.size();
		closed = true;
	}

	public void cleanupProlongations()
	{
		Vector<Transition> fromStart = new Vector<Transition>();
		for (int i=0; i < startState.prolongations.size(); i++)
		{
			Transition t = startState.prolongations.get(i);
			if (!t.target.isFinal)
			{
				//System.err.println("Neen:" + t.target);
				//startState.transitions.remove(t);
			} else
			{
				fromStart.add(t);
			}
		}
		startState.prolongations = fromStart;
	}
	
	/**
	 *  Store histories from graph g 
	 * @param g segmentation graph 
	 */
	
	private void insertHistoriesFromGraph(SegmentationGraph g)
	{
		for (Alignment.Position from: g.getForwardVector())
		  insertHistoriesFromVertex(g,from);
	}

	/**
	 *  Store histories starting from a node 
	 * @param g segmentation graph 
	 * @param from node where current history starts
	 */
	
	private void insertHistoriesFromVertex(SegmentationGraph g, Alignment.Position from)
	{
		insertHistoriesFromVertexWalk(g, from, from, 0);
	}

	/**
	 * Storing a history, walk along a path between nodes.
	 * 
	 * @param g segmentation graph 
	 * @param from node where current history starts
	 * @param at node where current history ends
	 * @param p length of current history fragment
	 * 
	 * This whole prcedure is wrong...
	 * At least too much transitions for start state
	 */

	private void insertHistoriesFromVertexWalk(SegmentationGraph g, Position from, Position at, int p) 
	{
		if (p > MODEL_ORDER)
		{
			return;
		}
		for (AlignmentSegmenter.Transition t: g.outgoingEdgesOf(at))
		{
			symbolHistory[p] = t.multigramId;
			Position target = g.getEdgeTarget(t);
			if (p >= 0 && 
					(p == MODEL_ORDER  || from == g.startPosition ||
					(target == g.endPosition && p == MODEL_ORDER-1)))
			// TODO: short words: if word shorter than model order, wordfinal final states can be short
			{
				int nofBefore = allStates.size(); // ugly hack to check whether we get a new state
				State s = startState.insertHistory(symbolHistory, 0, p+1);
				//System.err.println(s);
				if  (from == g.startPosition || p == MODEL_ORDER)
				{
					State sp = s.leftParentState;
					if (sp != null)
					{
						sp.setFinal(true);
					}
				}
				if (target == g.endPosition && p == MODEL_ORDER-1)
				{
					s.setFinal(true);
				}
				if (allStates.size() > nofBefore)
				{
					if (from == g.startPosition)
					{
					}
					if (target == g.endPosition)
					{
						//System.err.println("word final " + s);
						s.setWordFinal(true); 
					}
				}
			}
			insertHistoriesFromVertexWalk(g, from, target, p+1);
		}
	}
	
/**
 * A state in the history state model.
 * States correspond to a sequence of multigrams before the current multigram.
 *
 */
	
	protected class State implements java.io.Serializable
	{
		private static final long serialVersionUID = -6312326824602648497L;

		public boolean isFinal;
		public boolean isWordFinal;
		public int index; // e/0 0/e and 0/e e/0 are different
		protected double evidence; // needed for smoothing
		protected double lambda; // needed for smoothing
		/**
		 * left parent of "abc" = "ab"
		 */
		State leftParentState = null; // left parent "abc" = "ab"
		/**
		 * right parent of "abc" = "bc"
		 */
		State rightParentState = null; // right parent "abc" = "bc"

		/**
		 * The symbol on the transition from the left parent to this state
		 */
		
		int lastSymbol;

		int order=0;
		
		/**
		 * Why not use a linked or unspecified list for the transitions?
		 */
		private java.util.Vector<Transition> prolongations = null; // prolong history
		private java.util.Vector<Transition> stateTransitions = null; // shift history

		Double nodeData;

		protected int uplink=-1;

		protected int downlink=-1;
		
		private void pruneTransitions(Vector<Transition> transitions)
		{
			java.util.ListIterator<Transition> li = transitions.listIterator();
			double less=0;
		  while (li.hasNext())
		  {
		  	Transition t = li.next();
		  	if (t.p < minP)
		  	{
		  		li.remove();
		  		less += minP;
		  		// System.err.println("wappp");
		  	}
		  }
		  double mult = 1/(1-less);
		  for (Transition t: transitions)
		  {
		  	t.p  *= mult;
		  }
		}
		
		private void writeObject(ObjectOutputStream out) throws java.io.IOException
		{
			java.util.Collections.sort(prolongations);
			java.util.Collections.sort(stateTransitions);
			pruneTransitions(prolongations);
			pruneTransitions(stateTransitions);
		  out.defaultWriteObject();
		}
		/**
		 * Important: call this instead of accessing the stateTransitions directly.
		 * For "short states" (word initial) 
		 * @return
		 */
		protected java.util.Vector<Transition> getStateTransitions()
		{
			if (order < MODEL_ORDER) // word initial states
				return prolongations;
			return stateTransitions;
		}
		
		public State()
		{   
			this.index = -1;
			this.prolongations = new Vector<Transition>();
			this.stateTransitions = new Vector<Transition>();
			allStates.add(this);
		}

		public void setFinal(boolean b)
		{
			// TODO Auto-generated method stub
			if (!b)
			{
				if (!isFinal)
					return;
				isFinal = false;
				finalStates.remove(this);
			}
			if (!isFinal)
			{
				finalStates.add(this);
			}
			isFinal = true;
		}
		
		public void setWordFinal(boolean b)
		{
			// TODO Auto-generated method stub
			if (!b)
			{
				if (!isWordFinal)
					return;
				isWordFinal = false;
				wordFinalStates.remove(this);
			}
			if (!isWordFinal)
			{
				wordFinalStates.add(this);
			}
			isWordFinal = true;
		}

		public State stateTransition(int c)
		{
			Vector<Transition> v =  getStateTransitions();
			for (Transition t: getStateTransitions()) // depends on model order!!
			{
				if (t.symbol == c) return t.target;
			}
			return null;
		}

		public State prolongation(int c)
		{
			for (Transition t: prolongations)
			{
				if (t.symbol == c) return t.target;
			}
			return null;
		}

		public State insertHistory(int[] history, int startPosition)
		{
			return insertHistory(history, startPosition, startPosition + MODEL_ORDER+1, null);
		}
		
		public State insertHistory(int[] history, int startPosition, int endPosition)
		{
			return insertHistory(history, startPosition, endPosition, null);
		}

		/**
		 * @param history a particular instance of a multigram as a sequence of 0-1 units
		 * @param currentPosition
		 * @param endPosition
		 * @param data
		 * @return
		 */

		public State insertHistory(int[] history, int currentPosition, int endPosition, Double data)
		{
			if (currentPosition >= endPosition)
			{
				if (data != null) 
					nodeData = data;
				return this;
			}
			for (Transition t: prolongations)
			{
				if (t.symbol == history[currentPosition])
					return t.target.insertHistory(history, currentPosition+1, endPosition, data);
			}
			State n = new State();
			{
				/*
				n.isFinal = (currentPosition == endPosition -2);  // two because we store one longer
				if (n.isFinal)
				{
					finalStates.add(n);
				}
				*/
				n.leftParentState = this;
				n.order = n.getOrder();
				n.lastSymbol = history[currentPosition];
			}
			prolongations.add(new Transition(history[currentPosition], n));
			return n.insertHistory(history, currentPosition+1, endPosition, data);
		}

		/*
		 * TODO: these functions (withdraw, advance) are wrong for varying length history sets 
		 */


		public State advance(int c) // advance ("abc",d) = "bcd"
		{
			State f = stateTransition(c);
			return f;
		}

		public int getOrder()
		{
			if (this == startState)
			{
				return 0;
			}
			return leftParentState.getOrder() + 1;
		}
		
		/**
		 * This happens too often.<br>
		 * It only makes sense to add the transition from <i>(w<sub>1</sub>..w<sub>n</sub>)</i> on symbol c
		 * to  <i>(w<sub>2</sub>..w<sub>n</sub>, δ(w<sub>2</sub>..w<sub>n</sub>, c))</i> if
		 * <i>(w<sub>1</sub>..w<sub>n</sub>, δ(w<sub>2</sub>..w<sub>n</sub>, c))</i> occurs in the data.
		 * <p>
		 * Which means that we have store to histories 1 symbol longer than the state model order.
		 */
		
		private void makeStateTransitions()
		{
			if (rightParentState == null)
			{
				//System.err.println("OUCH " + this);
			  	stateTransitions = prolongations;
				return;
			}
			/*
			if (getOrder() < MODEL_ORDER) // TODO: cannot do lower order models in this way
			{
				stateTransitions = trieTransitions;
				return;
			}
			*/
			if (this == startState)
			{
				System.err.println("impossible!");
				System.exit(1);
			}
			Vector<Transition> vNew = new Vector<Transition>();
			for (Transition t: rightParentState.prolongations)
			{
				Transition t1 = new History.Transition(t.symbol, t.target);
				vNew.add(t1);
			}
			stateTransitions = vNew;
		}
		
		
		public void addEvidence(int c, double e)
		{
			for (Transition t: getStateTransitions()) // oopsie
			{
				if (t.symbol == c)
				{
					t.evidence += e; // TODO is this ever reset??
				}
			}
		}

		@Override
		public String toString()
		{
			if (codeInterpreter != null)
			{
				return toString(codeInterpreter);
			}
			Vector<Integer> v = new Vector<Integer>();
			for (State n=this; n!= null && n != startState; n=n.leftParentState)
			{
				v.add(n.lastSymbol);
			}
			String s = "";
			for (int i=v.size()-1; i >=0; i--)
			{
				if (i > 0)
					s += v.get(i) + ", ";
				else
					s += v.get(i);
			}
			return "[(" + isFinal + ") "+ s + "]"; 
		}
		
		public String toString(CodeInterpreter interp)
		{
			Vector<Integer> v = new Vector<Integer>();
			for (State n=this; n!= null && n != startState; n=n.leftParentState)
			{
				v.add(n.lastSymbol);
			}
			String s = "";
			for (int i=v.size()-1; i >=0; i--)
			{
				if (i > 0)
					s += interp.toString(v.get(i)) + ", ";
				else
					s += interp.toString(v.get(i));
			}
			if (this == startState)
			{
				s = "!start  " + s;
			}
			return "[(" + isFinal + ") " + s + "]"; 
		}
		
		public double conditionalProbability(int c)
		{
			for (Transition t: getStateTransitions())
			{
				if (t.symbol == c)
				{
					return t.p;
				}
			}
			return 0;
		}
		
		public State getUplink()
		{
			if (uplink >=0 && History.this.higherOrderHistory != null)
			{
				return History.this.higherOrderHistory.allStates.get(this.uplink);
			}
			return null;
		}
		
		public State getDownlink()
		{
			if (downlink >= 0 && History.this.lowerOrderHistory != null)
			{
				State downlink =  History.this.lowerOrderHistory.allStates.get(this.downlink);
				//System.err.println("Downlink: " + this.getEnclosingHistory().MODEL_ORDER + "  --> " + lowerOrderHistory.MODEL_ORDER);
				return History.this.lowerOrderHistory.allStates.get(this.downlink);
			}
			return null;
		}
		
		public History getEnclosingHistory()
		{
			return History.this;
		}
		
		public Transition getTransition(int c)
		{
			Vector<Transition> v = getStateTransitions();
			int l=0; int r=v.size()-1;
			while (l <= r)
			{
				int m = l + (r - l) / 2;
				int vm = v.get(m).symbol;
				if (c < vm)
					 r = m-1;
				else if (c > vm)
					l  = m+1;
				else
					return v.get(m);
			}
			return null;
		}
	}

	public Iterator<State> iterator()
	{
		return finalStates.iterator();
	} 


	public void printModel(java.io.PrintStream out, MultigramSet set)
	{
		for (State s: this.finalStates)
		{
			out.println(s.toString(set));
			for (Transition t: s.getStateTransitions())
			{
				out.printf("\t%s\t%e\t%s\n", set.getMultigramById(t.symbol), t.p, t.target);
			}
		}
	}

	// interface StateModel<History.State>
	
	/**
	 * Binary search for transition
	 * Can only be used after serialization and deserialization!
	 * @param s
	 * @param c
	 * @return
	 */

	
	public double conditionalProbability(State s, int c)
	{
		return s.conditionalProbability(c);
	}

	
	public State delta(State state, int c)
	{
		return state.stateTransition(c);
	}

	public State getStartState()
	{
		return this.startState;
	}

	public State getStateById(int id)
	{
		//TODO: decide whether nonFinal states get saved or not.
		// They should be for backoff etc!
		return this.allStates.get(id);
	}

	public int getStateId(State s)
	{
		return s.index;
	}
	
	public int size()
	{
		return size;
	}
	
	protected void resetEvidence()
	{
		for (State s: allStates)
		{
			s.evidence = 0;
			for (Transition t: s.stateTransitions)
			{
				t.evidence = 0;
			}
			for (Transition t: s.prolongations)
			{
				t.evidence = 0;
			}
		}
	}
	



	/**
	 * Connect corresponding states from lower and higher order models for smoothing purposes
	 * @param lowerOrder
	 * @param higherOrder
	 */
	
	public static void connectModels(History lowerOrder, History higherOrder)
	{
		lowerOrder.higherOrderHistory = higherOrder;
		higherOrder.lowerOrderHistory = lowerOrder;
		//System.err.println("connecting!! " + lowerOrder.MODEL_ORDER + " " + higherOrder.MODEL_ORDER);
		connectStates(lowerOrder.startState, higherOrder.startState);
	}
	
	public static void connectStates(State lowerOrder, State higherOrder)
	{
		//System.err.println("connect: " + lowerOrder + "/" + lowerOrder.getEnclosingHistory() + " -- " + 
		//		                                                 higherOrder +  "/" + higherOrder.getEnclosingHistory());
				
		lowerOrder.uplink = higherOrder.index;
		higherOrder.downlink = lowerOrder.index;
		
		for (Transition t: lowerOrder.prolongations)
		{
			State higherTarget = higherOrder.prolongation(t.symbol);
			if (higherTarget != null)
			{
				connectStates(t.target, higherTarget);
			}
		}
	}
	
	public static void main(String[] args)
	{
		History h = new History();
		h.MODEL_ORDER =3;
		int [] example = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
		for (int i=0; i < example.length; i++)
		{
			if (i < example.length-3)
			{
				h.startState.insertHistory(example, i);
			}
		}

		h.makeBackwardLinks();

		for (State s: h.allStates)
		{
			System.err.println("State: " + s);
			for (Transition t: s.getStateTransitions())
			{
				System.err.println("state transition: "+ t.symbol +  " -> " + t.target);
			}
			for (int i=1; i <= 20; i++)
			{
				State n = s.advance(i);
				if (n != null)
				{
					System.err.println("\tadvance on: "+ i +  " -> " + n);
				}
			}
		}
	}
}
