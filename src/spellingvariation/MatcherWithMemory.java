package spellingvariation;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.alg.*;

import util.ShortestPath;
import util.Trie;
import util.ShortestPath.BasicState;
import util.ShortestPath.MatchState;
import util.Trie.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedList;

import spellingvariation.History.State;
import spellingvariation.TransducerWithMemory.ScoredState;

import java.util.Vector;

interface StateMap<B,S>
{
	public S getState(B index);
	public void setState(B index, S s);
	public void clearState(B index);
}

class HashStateMap<B,S> implements StateMap<B,S>
{
	HashMap<B,S> h = new HashMap<B,S>();
	public S getState(B x)
	{
		return h.get(x);
	}
	
	public void clearState(B x)
	{
		h.remove(x);
		// TODO Auto-generated method stub
		
	}
	public void setState(B b, S s)
	{
		// TODO Auto-generated method stub
		h.put(b,s);
	}
}

class HugeUglyMap implements
		StateMap<History.State,MatcherWithMemory.ScoredState<History.State>>
{
	MatcherWithMemory.ScoredState<History.State>[] stateMap;

	public HugeUglyMap(int N)
	{
		setSize(N);
	}

	public void clearState(History.State s)
	{
		stateMap[s.index] = null;
	}

	public MatcherWithMemory.ScoredState<History.State> getState(History.State s)
	{
		return stateMap[s.index];
	}

	public void setSize(int N)
	{
		stateMap = new MatcherWithMemory.ScoredState[N];
	}

	public void setState(History.State b, MatcherWithMemory.ScoredState<History.State> s)
	{
		stateMap[b.index] = s;
	}
}

public class MatcherWithMemory<State> extends MemorylessMatcher
{
	private MultigramSet multigramSet;
	private StateModel<State> stateModel;

	protected StateMap<State, ScoredState<State>> stateMap = null;

	public MatcherWithMemory(MultigramSet multigramSet,
			StateModel<State> stateModel)
	{
		this.multigramSet = multigramSet;
		// TODO: build rule trie
		this.stateModel = stateModel;
		getRulesFromMultigramSet(multigramSet);
		// stateMap = (ScoredState[]) new Object[stateModel.size()];
	}

	static class ScoredState<S>
	{
		S state;
		double score;
		ScoredState<S> parentState;
		int multigramId;

		public ScoredState(S s, double score)
		{
			state = s;
			this.score = score;
		}

		public ScoredState()
		{

		}
	}

	/**
	 * This should not be forced to be static by the silly ShortestPath class!
	 * @author jesse
	 *
	 */
	
	static class SearchState extends util.ShortestPath.BasicState
	{
		int position;
		boolean isWordFinal=false;
		History.State state;
		Segmentation segmentation;
		StateModel model;
		
		public SearchState(int p, History.State base)
		{
			position = p;
			state = base;
		}
		
		static class Link extends ShortestPath.BasicEdge
		{
			int multigramId;
			public Link(int m)
			{
				multigramId = m;
			}
		}
		
		public boolean equals(Object other)
		{
			try
			{
				SearchState os = (SearchState) other;
				return state.equals(os.state) && position == os.position;
			} catch (Exception e)
			{
				return false;
			}
		}
		@Override
		public boolean isFinal()
		{
			// TODO Auto-generated method stub
			return isWordFinal;
		}

		@Override
		public void relaxEdges(ShortestPath sp, MatchState<BasicState> source)
		{
			// TODO Auto-generated method stub
			for (Segment segment: segmentation.segments[position])
			{
				if (segment.multigrams == null)
					continue;
				for (JointMultigram m: segment.multigrams)
				{
					History.State next = (History.State) model.delta(this.state,m.id);
					//System.err.println(position + " " + this.state + "->" + next + " " + m);
					if (next != null)
					{
						double p = model.conditionalProbability(this.state,m.id);
						SearchState target = new SearchState(segment.to, next);
						target.model = this.model;
						target.segmentation = this.segmentation;
						target.isWordFinal = (segment.to == this.segmentation.length -1);
						sp.relaxEgde(new Link(m.id), source, target,  (int) (-100 * Math.log(p)));
					}
				}
			}
		}
	}
	
	public void bestFirstSearch(String s)
	{
		Segmentation segmentation = new Segmentation(this,s, false);
		SearchState start = new SearchState(0, (History.State) stateModel.getStartState());
		start.model = stateModel;
		start.segmentation = segmentation;
		ShortestPath sp = new ShortestPath();
		java.util.List<SearchState.Link> bestPath = sp.bestFirstSearch(start);
		if (bestPath != null)
		{
			for (SearchState.Link l: bestPath)
			{
				System.out.print("[" + this.multigramSet.getMultigramById(l.multigramId) + "]");
			}
			System.out.println("");
		}
	}
	
	static class Segment
	{
		int from;
		int to;
		Vector<JointMultigram> multigrams;

		Segment(int f, int t, Vector<JointMultigram> v)
		{
			from = f;
			to = t;
			multigrams = v;
		}
	}

	static class Segmentation
	{
		boolean[] positionVisited;
		LinkedList<Segment>[] segments;
		MatcherWithMemory matcher;
		boolean backward;
		int length;
		
		public Segmentation(MatcherWithMemory matcher, String s, boolean backward)
		{
			this.matcher = matcher;
			this.backward = backward;
			positionVisited = new boolean[s.length()+1];
			segments = new LinkedList[s.length()+1];
			length = s.length()+1;
			
			for (int i = 0; i <= s.length(); i++)
			{
				positionVisited[i] = false;
				segments[i] = new LinkedList<Segment>();
			}
			segment(s, 0, 0, matcher.ruletrie.root);
		}

		private void segment(String s, int startPosition, int endPosition,
				TrieNode rhsNode)
		{
			if (rhsNode.isFinal)
			{
				// System.err.println("segment:"
				// +s.substring(startPosition,endPosition));
		
					addSegment(startPosition, endPosition, matcher.multigramSet
							.getMultigramsbyRHS(s.substring(startPosition, endPosition)));
			
				if (!positionVisited[endPosition])
				{
					positionVisited[endPosition] = true;
					segment(s, endPosition, endPosition, matcher.ruletrie.root);
				}
				if (endPosition < s.length())
				{
					TrieNode next = rhsNode.delta(s.charAt(endPosition));
					if (next != null)
					{
						segment(s, startPosition, endPosition + 1, next);
					}
				}
			}
		}

		private void addSegment(int startPosition, int endPosition,
				Vector<JointMultigram> v)
		{
			if (backward)
			{
				segments[endPosition].add(new Segment(startPosition, endPosition, v));
			} else
			{
				segments[startPosition].add(new Segment(startPosition, endPosition, v));
			}
		}
		
		private void printWalk()
		{
			
		}
		public void printAll()
		{
			for (int i=0; i < length; i++)
			{
				this.positionVisited[i] = false;	
			}
		}
	} // end of enclosed class Segmentation

	public void printState(ScoredState<State> ss)
	{
		StringBuilder sb = new StringBuilder();
		// Send all output to the Appendable object sb
		Formatter formatter = new Formatter(sb);
		formatter.format("%e", ss.score);
		String s = ":  " + sb;
		
		String match = "";
		while (ss != null)
		{
			JointMultigram m = this.multigramSet.getMultigramById(ss.multigramId);
			//History.State xx = (History.State) (ss.state);
			if (!(ss.state == stateModel.getStartState()))
			{
				int score = -  (int) (Math.round(Math.log(ss.score)));
				s =  "<" + m + "  ->"   + ss.state  + " (" + score + ")>    "
				+ s;
				match = m.lhs + match;
			}
			ss = ss.parentState;
		}
		System.out.println(match + "\t" + s);
	}

	/**
	 * TODO prune the beam (we are doing full Viterbi now, which will be too slow once we add smoothing)
	 * @param s
	 */
	
	public void pruneStateList(LinkedList<ScoredState<State>> list, double delta)
	{
		double best=0;
		for (ScoredState<State> s: list)
		{
			if (s.score > best)
				best = s.score;
		}
	  java.util.ListIterator<ScoredState<State>> li = list.listIterator();
	  while (li.hasNext())
	  {
	  	ScoredState<State> s = li.next();
	  	if (s.score < best * delta)
	  	{
	  		li.remove();
	  	}
	  }
	}
	/**
	 * TODO should move this score-keeping procedure to more general setting
	 * @param s
	 * @param delta
	 */
	
	public void beamSearch(String s, double delta)
	{
		LinkedList<ScoredState<State>>[] statesAtPosition;
		statesAtPosition = new LinkedList[s.length()+1];
		Segmentation segmentation = new Segmentation(this,s,true);
		double bestSoFar = 0;
		for (int i = 0; i <= s.length(); i++)
		{
			LinkedList<ScoredState<State>> newStates = new LinkedList<ScoredState<State>>();
			statesAtPosition[i] = newStates;
			if (i == 0)
			{
				ScoredState start = new ScoredState(stateModel.getStartState(), 1);
				newStates.add(start);
				continue;
			}

			Segment loopSegment = null;
			for (Segment segment : segmentation.segments[i])
			{
				if (segment.multigrams == null)
					continue;
				if (segment.from < i) // loops (insertions) are another problem......
				{
					LinkedList<ScoredState<State>> oldStates = statesAtPosition[segment.from];
					for (ScoredState<State> ss : oldStates)
					{
						for (JointMultigram m : segment.multigrams)
						{
							State next = stateModel.delta(ss.state, m.id);
							if (next != null)
							{
								double p = stateModel.conditionalProbability(ss.state, m.id)
										* ss.score;
								if (p > bestSoFar)
									bestSoFar = p;
								//if (delta > 0 && p < delta * bestSoFar)
									//continue;
								ScoredState nextScoredState = stateMap.getState(next);
								if (nextScoredState == null)
								{
									nextScoredState = new ScoredState(next, p);
									nextScoredState.parentState = ss;
									nextScoredState.multigramId = m.id;
									stateMap.setState(next, nextScoredState);
									newStates.add(nextScoredState);
								} else
								{
									if (p > nextScoredState.score)
									{
										nextScoredState.score = p;
										nextScoredState.parentState = ss;
										nextScoredState.multigramId = m.id;
									}
								}
							}
						}
					}
				} else
				// loop!
				{
					loopSegment = segment;
				}
				int nonLoop = newStates.size();
				if (false && loopSegment != null)
				{
					if (loopSegment.multigrams == null)
						continue;
					LinkedList<ScoredState<State>> todoList = statesAtPosition[i];
					while (todoList.size() > 0)
					{
						LinkedList<ScoredState<State>> newTodoList = new LinkedList<ScoredState<State>>();
						for (ScoredState<State> ss : todoList)
						{
							for (JointMultigram m : segment.multigrams)
							{
								State next = stateModel.delta(ss.state, m.id);
								if (next != null)
								{
									double p = stateModel.conditionalProbability(ss.state, m.id)
											* ss.score;
									ScoredState nextScoredState = stateMap.getState(next);
									if (nextScoredState == null)
									{
										nextScoredState = new ScoredState(next, p);
										nextScoredState.parentState = ss;
										nextScoredState.multigramId = m.id;
										stateMap.setState(next, nextScoredState);
										newTodoList.add(nextScoredState);
									} else
									{
										if (p > nextScoredState.score)
										{
											nextScoredState.score = p;
											nextScoredState.parentState = ss;
											nextScoredState.multigramId = m.id;
										}
									}
								}
							}
						}
						todoList = newTodoList;
						for (ScoredState ss : todoList)
						{
							newStates.add(ss);
						}
					}
					System.err.println("loop states at " + i + " : " + (newStates.size() - nonLoop));
				}
			}
			for (ScoredState<State> ss : newStates)
			{
				//System.err.println(i +  ": " + ((History.State) (ss.state)).toString(multigramSet));
				stateMap.clearState(ss.state);
			}
			if (i == s.length())
			{
				for (ScoredState<State> ss : newStates)
				{
					System.out.print("at " + i +  ": ");
					printState(ss);
				}
			}
			if (delta > 0)
				pruneStateList(newStates, delta);
		}
	}

	public static void testModel(StateModel<History.State> model, MultigramSet multigramSet, 	java.io.BufferedReader stdin)
	{
		MatcherWithMemory<History.State> m = new MatcherWithMemory<History.State>(multigramSet,
				model);
		m.stateMap = new HugeUglyMap(model.size());

		String s = null;
		try
		{
			while ((s = stdin.readLine()) != null)
			{
				m.beamSearch(Alphabet.initialBoundary + s + Alphabet.finalBoundary,10e-6);
				//m.bestFirstSearch(Alphabet.initialBoundary + s + Alphabet.finalBoundary);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void testUnsmoothedModel(String modelFile, java.io.BufferedReader inFile)
	{
		TransducerWithMemory t = TransducerWithMemory.readFromFile(modelFile);
		
		System.err.println("model read...");
		
		testModel( t.getHistory(), t.getMultigramSet(), inFile);	
	}
	
	public static void testSmoothedModel(String modelFile, java.io.BufferedReader inFile)
	{
		AbsoluteDiscountModel  t = AbsoluteDiscountModel.readFromFile(modelFile);
		
		System.err.println("model read...");
		
		testModel( t, t.getMultigramSet(), inFile);	
	}
	
	public static void main(String[] args)
	{
		java.io.BufferedReader inFile;
		if (args.length >= 2)
		{
			try
			{
				inFile = new BufferedReader(new FileReader(args[1]));
			} catch (Exception e)
			{
				e.printStackTrace();
				inFile = null;
				System.exit(1);
			}
		} else
			inFile = new BufferedReader(new java.io.InputStreamReader(System.in));

		String modelFile = "/tmp/model.out";
		
		if (args.length > 0)
			modelFile = args[0];
		testUnsmoothedModel(modelFile, inFile);
	//t.getHistory().printModel(System.out, t.getMultigramSet());
	}
}
