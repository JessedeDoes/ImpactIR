package impact.ee.spellingvariation;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

class FunctionValueComparator implements Comparator<JointMultigram>
{
	public double f(JointMultigram x)
	{
		return 0;
	}

	public int compare(JointMultigram arg0,  JointMultigram arg1)
	{
		if (f(arg0) == f(arg1))
		{
			if (arg0.id < arg1.id) return 1;
			if (arg0.id > arg1.id) return -1;
			return 0;
		}
		if (f(arg0) < f(arg1))
			return 1;
		return -1;
	}
}

/**
 * This class performs multigram lookup and scorekeeping.
 * <p>
 * Builds a trie of sequences of joint unigrams 
 * at each node, there is a link to the multigram
 */

public class MultigramSet implements java.io.Serializable,
		Iterable<JointMultigram>, CodeInterpreter, CodeToStringPairMapping
{ 
	private static final long serialVersionUID = -6133171405887360860L;
	transient Vector<Node> allNodes = new Vector<Node>();
	boolean saveNodes = false;
	int numberOfNodes;
	

	int nMultigrams=0;
	int order = 0;
	Alphabet inputAlphabet;
	Alphabet outputAlphabet;
	Vector<JointMultigram> multigramVector = new Vector<JointMultigram>();
	UnigramTransducer.CodePair[] symbolPairs;
	int nSymbolPairs;
	private Map<String, Vector<JointMultigram>> rhsToMultigrams = new HashMap<String, Vector<JointMultigram>>();
	//private Map<String,Double> lhsFrequencies = new HashMap<String,Double>();
	//private Map<String,Double> rhsFrequencies = new HashMap<String,Double>();

	
	transient CodeToStringPairMapping codeToStringPairMapping = null; // not the one implemented by this set, but the one used to build the set
	protected transient Node startNode = new Node(this); // 
	

	public MultigramSet()
	{

	}

	private void readObject(ObjectInputStream in) throws java.io.IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		if (saveNodes) for (Node s: allNodes)
		{
			for (Transition t: s.transitions)
			{
				t.target = allNodes.get(t.targetIndex);
			}
		}
		
		for (JointMultigram m: this.multigramVector)
		{
			if (!m.active) // only active multigrams
				continue;
			Vector<JointMultigram> v = rhsToMultigrams.get(m.rhs);
			if (v == null)
			{
				v = new Vector<JointMultigram>();
				v.add(m);
				rhsToMultigrams.put(m.rhs,v);
			} else
			{
				v.add(m);
			}
		}
	}
	
	public Iterator<JointMultigram> sort()
	{
		class scoreComparison extends FunctionValueComparator
		{
			public double f(JointMultigram x)
			{
				return scoreTable.get(x.id);
			}
		}
		TreeSet<JointMultigram> s = new TreeSet<JointMultigram>(new scoreComparison());
		s.addAll(multigramMap.keySet());
		//nl.openconvert.log.ConverterLog.defaultLog.println("multigrams in set/map " + s.size() +  " " + multigramMap.size());
		Iterator<JointMultigram> i = s.iterator();
		return i;
	}

	private java.util.HashMap<JointMultigram, Integer> multigramMap 
	= new java.util.HashMap<JointMultigram, Integer>();

	public Iterator<JointMultigram> iterator()
	{
		return multigramMap.keySet().iterator();
	}

	private Vector<Double> scoreTable = new Vector<Double>();

	public MultigramSet(UnigramTransducer t)
	{
		inputAlphabet = t.inputAlphabet;
		outputAlphabet =t.outputAlphabet;
		nSymbolPairs = t.nSymbolPairs;
		symbolPairs = t.symbolPairs;
		this.codeToStringPairMapping = t;
	}

	/**
 * We store the unnormalized multigrams in a trie
 * for convenient multigram lookup and segmentation of words
 *  
 */
	
	public class Transition implements java.io.Serializable
	{
		private static final long serialVersionUID = 8046491014094688339L;
		int symbol;
		double weight;
		/**
		 * transient to avoid cycles for serialization
		 * reinitialized from targetIndex on deserialization
		 */
		transient Node target; 
		int targetIndex;
		
		public Transition(int c, Node n)
		{
			symbol = c; target = n;
		}
		/**
		 * Stuff to avoid the serializability-and-cycles problem
		 * @param out
		 * @throws java.io.IOException
		 */
		
		private void writeObject(ObjectOutputStream out) throws java.io.IOException
		{
			targetIndex = target.nodeId;
		  out.defaultWriteObject();
		}

		private void readObject(ObjectInputStream in) throws java.io.IOException, ClassNotFoundException
		{
			in.defaultReadObject();
	
			// target = allNodes.get(targetIndex); // TODO: save node list to file before serialization
		}
	}

	protected class Node implements java.io.Serializable
	{
		private static final long serialVersionUID = -9106870760180858789L;
		public boolean isFinal;
		public int nodeId; // e/0 0/e and 0/e e/0 are different
		public int multigramId = -1; // they are the same
		Node parentNode = null;

		java.util.Vector<Transition> transitions = null;
		java.util.Vector<Transition> backwardTransitions = null;
		
		Double nodeData;
		MultigramSet set;

		public Node(MultigramSet set)
		{
			this.set = set;   
			this.multigramId = -1;
			this.nodeId = set.numberOfNodes;
			set.numberOfNodes++;
			set.allNodes.add(this);
			this.transitions = new Vector<Transition>();
		}

		public Node transition(int c)
		{
			for (Transition t: transitions)
			{
				if (t.symbol == c) return t.target;
			}
			return null;
		}

		public void makeBackwardLinks()
		{
			for (Transition t: set.startNode.transitions)
			{
				Node n0 = t.target;
				int s0 = t.symbol;
				for (Transition t1: n0.transitions)
				{
					Node n1 = t1.target;
					Node n2 = set.startNode.transition(t1.symbol);
					if (n2 != null)
					{
						linkBackward(n1,n2,s0);
					}
				}
			}
		}
		/**
		 * makes a backward link on "a" 
		 * from y (representing f.i. "bc") to x (representing f.i. "abc")
		 * @param x
		 * @param y
		 * @param s0
		 */
		public void linkBackward(Node x, Node y, int s0)
		{
			y.backwardTransitions.add(new Transition(s0,x));
			for (Transition t: x.transitions)
			{
				Node x1 = t.target;
				Node y1 = y.transition(t.symbol);
				if (y1 != null)
				{
					linkBackward(x1,y1,s0);
				}
			}
		}
		
		public Node insert(int[] multigram, int startPosition, int endPosition)
		{
			return insert(multigram, startPosition, endPosition, null);
		}

		/**
		 * @param multigram a particular instance of a multigram as a sequence of 0-1 units
		 * @param startPosition
		 * @param endPosition
		 * @param data
		 * @return
		 */

		public Node insert(int[] multigram, int startPosition, int endPosition, Double data)
		{
			if (startPosition == endPosition)
			{
				if (data != null) 
					nodeData = data;
				// TODO: do next step only if the node is new (avoids extra hashtable lookup)
				JointMultigram m = set.getAndCreateIfNotExistsMultigram(multigram, endPosition); 
				this.multigramId = m.id;
				return this;
			}
			for (Transition t: transitions)
			{
				if (t.symbol == multigram[startPosition]) 
					return t.target.insert(multigram, startPosition+1, endPosition, data);
			}
			Node n = new Node(this.set);
			n.isFinal = (startPosition == endPosition -1); // no: 
			n.parentNode = this;
			transitions.add(new Transition(multigram[startPosition], n));
			return n.insert(multigram, startPosition+1, endPosition, data);
		}

		public Node shift(int c) // shift (a) [bcd] = [abc]
		{
			return null;
		}

		@Override
		public String toString()
		{
			return "node for " + this.set.getMultigramById(multigramId) + " multigramId=" + this.multigramId; 
		}
	} // end of class Node

	public void addToScore(int multigramId, double w)
	{
		scoreTable.ensureCapacity(multigramId+1);
		if (scoreTable.size() <= multigramId)
		{
			scoreTable.setSize(multigramId+1); 
			scoreTable.setElementAt(0.0, multigramId);
		}
		try
		{
			double old = scoreTable.get(multigramId);
			scoreTable.setElementAt(w + old, multigramId);
		} catch (Exception e)
		{
			nl.openconvert.log.ConverterLog.defaultLog.println(multigramId + "/"  + scoreTable.size());
			scoreTable.setElementAt(w,multigramId);
		}
	}

	public void setScore(int multigramId, double w)
	{
		scoreTable.ensureCapacity(multigramId+1);
		if (scoreTable.size() <= multigramId)
		{
			scoreTable.setSize(multigramId+1);
		}
		try
		{
			scoreTable.setElementAt(w, multigramId);
		} catch (Exception e)
		{
			nl.openconvert.log.ConverterLog.defaultLog.println(multigramId + "/"  + scoreTable.size());
			scoreTable.setElementAt(w,multigramId);
		}
	}

	public void resetScore()
	{
		for (int i=0; i < this.nMultigrams; i++)
		{
			try
			{
				scoreTable.setElementAt(0.0,i);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void checkUp()
	{
		for (int i=0; i < nMultigrams; i++)
		{
			if (this.multigramVector.get(i).id != i)
			{
				nl.openconvert.log.ConverterLog.defaultLog.println("RAMP" + this.multigramVector.get(i));
				System.exit(1);
			}
		}
	}
	
	public JointMultigram getMultigramById(int multigramId)
	{
		return multigramVector.get(multigramId);
	}

	private JointMultigram getAndCreateIfNotExistsMultigram(int[] multigram,
			int endPosition)
	{
		JointMultigram m = new JointMultigram();
		m.set = this;
		m.initData(multigram, endPosition);
		Integer i = this.multigramMap.get(m);
		if (i == null)
		{
			this.multigramMap.put(m, nMultigrams);
			m.id = nMultigrams++;
			this.multigramVector.add(m);
			return m;
		} else
		{
			return multigramVector.get(i);
		}
	}

	public int size()
	{
		return nMultigrams;
	}

	public double getScore(int multigramId)
	{
		if (multigramId >= this.scoreTable.size())
			return 0.0;
		try
		{
			return this.scoreTable.get(multigramId);
		} catch (Exception e)
		{
			return 0.0;
		}
	}
	
	public Vector<JointMultigram> getMultigramsbyRHS(String rhs)
	{
		return rhsToMultigrams.get(rhs);
	}

	public String toString(int i)
	{
		return multigramVector.get(i).toString();
	}
	
	 public void prune(MultigramPruner p)
	 {
		 p.applyAbstraction(this);
		 for (JointMultigram m: this)
		 {
			 m.active = p.isOK(m) || m.isAbstract();
		 }
	 }

	
	public String getLHS(int i)
	{
		return this.multigramVector.get(i).lhs;
	}


	public String getRHS(int i)
	{
		return this.multigramVector.get(i).rhs;
	}
	
	public Statistics statistics(Dataset d, boolean bestMatch)
	{
		return new Statistics(d, bestMatch);
	}
	
	public class Statistics
	{
		int totalLengthLHS=0;
		int totalLengthRHS=0;
		private Map<String,Double> lhsFrequencies = new HashMap<String,Double>();
		private Map<String,Double> rhsFrequencies = new HashMap<String,Double>();
		
		private void countSubstrings(Map<String,Double> map, String s, double lambda)
		{
			for (int i=0; i < s.length() ; i++)
			{
				for (int j=1; j <= order && i+j <= s.length(); j++)
				{
					String x = s.substring(i,i+j);
					Double f;
					if ( ( f = map.get(x)) != null)
					{
						map.put(x,f+lambda);
					}
				}
			}
		}
	
		public double LHSRelativeFrequency(JointMultigram m)
		{
			if (m.lhs.equals(""))
				return 1;
			return lhsFrequencies.get(m.lhs) / totalLengthLHS;
		}
		
		public double RHSRelativeFrequency(JointMultigram m)
		{
			if (m.rhs.equals(""))
				return 1;
			return rhsFrequencies.get(m.rhs) / totalLengthRHS;
		}
		
		public double LHSCount(JointMultigram m)
		{
			if (m.lhs.equals(""))
				return this.totalLengthLHS;
			return lhsFrequencies.get(m.lhs);
		}
		
		public double RHSCount(JointMultigram m)
		{
			if (m.rhs.equals(""))
				return this.totalLengthRHS;
			return rhsFrequencies.get(m.rhs);
		}
		
		public Statistics(Dataset d, boolean bestMatch)
		{
			totalLengthLHS = totalLengthRHS = 0;
			for (JointMultigram m : multigramVector)
			{
				this.lhsFrequencies.put(m.lhs,0.0);
				this.rhsFrequencies.put(m.rhs,0.0);
			}
			for (Dataitem item: d)
			{
				String s= item.target;
				//nl.openconvert.log.ConverterLog.defaultLog.println(s);
				totalLengthRHS+= s.length();
				countSubstrings(rhsFrequencies,s,1);
				if (bestMatch)
				{
					String cand = item.best_match.wordform;
					totalLengthLHS += cand.length();
					countSubstrings(lhsFrequencies,cand,1);
				} else
				{
					for (Candidate c: item.candidates)
					{
						String cand= c.wordform;
						totalLengthLHS += cand.length() * c.lambda;
						countSubstrings(lhsFrequencies,cand,c. lambda);
					}			 
				}
			}
		}
	}


	public JointMultigram createMultigram(String lhs, String rhs)
	{
		JointMultigram x = new JointMultigram();
		x.lhs = lhs;
		x.rhs = rhs;
		return x;
	}
}
