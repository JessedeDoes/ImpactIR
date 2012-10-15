package impact.ee.trie;
import impact.ee.spellingvariation.Alphabet;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;


/**
 * An extremely simple implementation of a Trie
 * Later on we may want to switch to something like hash-tries for performance.
 * <p>
 * Also consider libdatrie (C implementation) (jun-ichi aoe)
 * <p>
 * http://linux.thai.net/~thep/datrie/datrie.html (Theppitak Karoonboonyanan)
 * <p>
 * Java implementation:
 * http://www.koders.com/java/fid7B3616A04CBAEF19B94977E8A7E3E23DE09CF214.aspx
 * 
 * @author jesse
 *
 */
public class Trie implements java.io.Serializable
{
	private static final long serialVersionUID = -8793179136143545148L;

	
	public Vector<TrieNode> allStates = new Vector<TrieNode>();
	public TrieNode root = new TrieNode();
	int NofNodes = 0;
	int NofTreeNodes = 0;
	int NodeNr;

	private transient Hashtable<TrieNode,TrieNode> nodeTable;

	private void writeObject(ObjectOutputStream out) throws java.io.IOException
	{
	  out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws java.io.IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		for (TrieNode s: allStates)
		{
			if (s.transitions != null) for (Transition t: s.transitions)
			{
				t.node = allStates.get(t.targetIndex);
			}
		}
	}
	
	public  static class  NodeAction
	{
		public void doIt(TrieNode n) {};
	}
	
	public void forAllNodesPreOrder(TrieNode n, NodeAction action)
	{
	  action.doIt(n);
	  if (n.transitions == null)
	  	return;
	  for (Transition t: n.transitions)
	  {
	  	forAllNodesPreOrder(t.node, action);
	  }
	}
	
	public void forAllNodesPreOrder(NodeAction action)
	{
		forAllNodesPreOrder(this.root, action);
	}
	
	public class Transition implements Serializable
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public int character;
		public transient TrieNode node; 
		protected int targetIndex;
		
		private void writeObject(ObjectOutputStream out) throws java.io.IOException
		{
			targetIndex = node.code;
		  out.defaultWriteObject();
		}

		private void readObject(ObjectInputStream in) throws java.io.IOException, ClassNotFoundException
		{
			in.defaultReadObject();
			// TODO in other places as well: WRONG: you cannot do this because allStates may be later than transitions
			if (false) try
			{
				node = allStates.get(targetIndex);
			} catch (Exception e)
			{
				System.err.println("hola " + allStates + " " + targetIndex);
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	public void putOrdered(String s)
	{
		int[] w = new int[s.length()];
		for (int i=0; i < s.length(); i++)
			w[i] = s.charAt(i);
		root.putWordSmartly(w, 0, nodeTable);
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
	
	public static Trie readFromFile(String filename)
	{
		try
		{
			ObjectInputStream out = new ObjectInputStream(new FileInputStream(filename));
			Object o = out.readObject();
			out.close();
			return (Trie) o;
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public void loadWordlist(String filename)
	{
		loadWordlist(filename,false,false);
	}
	
	public boolean contains(String s)
	{
		return root.findNode(s) != null; 
	}
	
	public boolean contains(String s, boolean addWordBoundaries)
	{
		String x = !addWordBoundaries?s:Alphabet.initialBoundaryString + s + Alphabet.finalBoundaryString;
		return root.findNode(x) != null; 
	}
	
	public void loadWordlist(String filename, boolean assumeOrdered, boolean addWordBoundaries)
	{
		//String previousWord = null;
		int nWords = 0;

		java.io.BufferedReader wordlist;

		try
		{
			wordlist = new BufferedReader(new FileReader(filename));
			String word = null;
			while((word = wordlist.readLine())  != null)
			{
				if (addWordBoundaries)
					word = impact.ee.spellingvariation.Alphabet.initialBoundaryString + word + impact.ee.spellingvariation.Alphabet.finalBoundaryString;
				if (nWords % 10000 == 0)
					System.err.printf("%s\n",word);
				if (assumeOrdered)
					this.putOrdered(word);
				else
					this.root.putWord(word);
				nWords++;
			} 
		} catch (Exception e)
		{
			System.err.println("could not load word list from " + filename); 
			e.printStackTrace();
		}

		this.root.reset();
		this.root.number(0);
		System.err.printf("this trie has %d nodes and %d words\n", 
				this.root.nodesBelow, nWords);
	}

	public  class TrieNode implements Serializable
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		Vector<Transition> transitions;
		int nodesBelow = 0;
		boolean closed;
		public boolean isFinal;
		public Object data;
		int code;
		TrieNode parentNode;
		
		public TrieNode()
		{
			closed = false;
			isFinal= false;
			code=0;
			nodesBelow=0;
			data = null;
			parentNode=null;
		}

		void addTransition(int c, TrieNode n)
		{
			if (transitions == null)
				transitions = new Vector<Transition>(0,0);	
			Transition t = new Transition();
			t.character = c;
			t.node = n;
			transitions.addElement(t);
			n.parentNode = this;
		}

		public TrieNode putWord(String s)
		{
			int[] w = new int[s.length()];
			for (int i=0; i < s.length(); i++)
				w[i] = s.charAt(i);
			return putWord(w);
		}

		public TrieNode putWord(String s, Object data)
		{
			int[] w = new int[s.length()];
			for (int i=0; i < s.length(); i++)
				w[i] = s.charAt(i);
			return putWord(w, data);
		}
	
		public TrieNode putWord(int [] w)
		{
			return putWord(w, 0);
		}
		
		public TrieNode putWord(int [] w, Object data)
		{
			return putWord(w, 0, data);
		}
		
		TrieNode putWord(int [] w, int p)
		{
			if (p == w.length) 
			{
				isFinal = true;
				return this;
			}
			int c = w[p];   
			TrieNode next = delta(c);
			if (next == null)
			{
				next = new TrieNode();
				addTransition(c,next);
			}
			return next.putWord(w,p+1);
		}

		public TrieNode findNode(String s) // slow
		{
			int[] w = new int[s.length()];
			for (int i=0; i < s.length(); i++)
				w[i] = s.charAt(i);
			return findNode(w,0);
		}

		TrieNode findNode(int [] w, int p)
		{
			if (p == w.length)
			{
				return this;
			} else
			{
				TrieNode  n = delta(w[p]);
				if (n != null) 
					return n.findNode(w,p+1);
				return null;
			}
		}

		TrieNode putWord(int[] w, int p, Object data)
		{
			if (p == w.length)          
			{
				isFinal = true;
				this.data = data;
				return this;
			}
			int c = w[p];
			TrieNode next = delta(c);
			if (next == null)
			{
				next = new TrieNode();
				addTransition(c,next);
			}
			return next.putWord(w,p+1);
		}

		void reviseLastTransition(Hashtable<TrieNode, TrieNode> pool)
		{
			Transition t;
			TrieNode tr, next;

			if (transitions == null || transitions.size() <= 0) return;
			if (closed) return;

			// fprintf(stderr,"revising last transition in node\n");

			t = transitions.get(transitions.size() -1); 

			next = t.node;
			if (next != null) 
				next.reviseLastTransition(pool);
			tr  = pool.get(next); //Huh
			NofTreeNodes++;
			if (tr != null)
			{
				if (!tr.closed) 
				{
					System.exit(1);
				}


				tr.closed= true;
				t.node = tr;
			} else
			{
				if (NofNodes % 1000 == 0) 
				{ 
					// fprintf(stderr,"NEW NODE %d (%d)\n",NofNodes,NofTreeNodes);
					// next.print();
				}
				NofNodes++;
				// next.print();
				pool.put(next,next);
				next.closed = true;
			}
		}


		void print()
		{
			/*
  static char woord[1000];
  fprintf(stderr,"---------------------\n");
  print(woord,0);
  fprintf(stderr,"---------------------\n");
			 */
		}

		/**
		 * this is a remnant of an earlier working C++ implementation of direct construction of
		 * a minimal DAG from a sorted word list.
		 * 
		 * Will not (yet)  work in the java version
		 * because of different ways hashing works
		 * 
		 * @param w
		 * @param p
		 * @param pool
		 */
		
		void putWordSmartly(int[] w,  int p, Hashtable<TrieNode, TrieNode> pool)
		{
			if (p == w.length) 
			{
				isFinal = true;
				if (false && !closed) 
				{
					reviseLastTransition(pool);
					closed = true;
				}
				return; // Hoho, wrong??
			}
			// fprintf(stderr,"inserting: %s\n",w);
			int c = w[p];

			TrieNode next = delta(c);

			if (next == null) // preceding stuff can be closed
			{
				if (transitions.size() > 0) reviseLastTransition(pool);
				next = new TrieNode();
				addTransition(c,next);
			}
			next.putWordSmartly(w, p+1, pool);
		}

		public TrieNode delta(int c)
		{
			if (transitions != null) for (Transition t: transitions)
			{
				if (t.character == c) return t.node;
			}
			return null;
		}

		void print(int [] woord, int n)
		{
			/*
  Transition *t;
  char c;
  if (final)
  {
    c  = woord[n];
    woord[n] = '\0';
    fprintf(stderr,"%s\n",woord);
    woord[n] = c;
  } 
  for (int i=0; i <transitions.size; i++)
  {
    t = (Transition *) transitions[i];
    woord[n] = t.character ;
    t.node.print(woord, n +1);
  }
			 */
		}

		void setTransition(int c, TrieNode n)
		{
			if (transitions != null) for (Transition t: transitions)
			{
				if (t.character == c) 
				{
					t.node = n;
					return;
				}
			}
		}


		void reset()
		{
			closed = false;
			if (transitions != null) for (Transition t: transitions)
			{
				t.node.reset();
			}
		}

		int number(int n)
		{
			if (closed) return nodesBelow;

			code = NodeNr++;
			allStates.add(this); // oops! beetje eng wel!!!
			closed = true;
			nodesBelow = 1;

			if (transitions != null) for (Transition t: transitions)
			{
				nodesBelow += t.node.number(code+nodesBelow);
			}

			return nodesBelow;
		}

		/**

   Assumption:

   Nodes are `equivalent' in the sense
   that they generate the same language only if all transitions are _identical_
   This is supposed to be ensured in the construction by looking
   up all children before parents.

   for the rest: the hashcode will be the sum of all characters
   in the suffix set. seems not much good (?) lots of clustering. 
		 */

		int Hashcode()
		{
			if (closed)
				return code;
			else 
			{
				int newcode = 0;
				if (transitions != null) for (Transition t: transitions)
				{
					newcode += t.character  +  t.node.Hashcode();
				}
				code = newcode;
				return code;
			}
		}

		boolean equivalent(Object _other)
		{
			int i;
			TrieNode other =  (TrieNode) _other;
			if (other == null) return false; // final state info missing

			if (isFinal != other.isFinal) return false;

			int nTransitions = transitions == null? 0 : transitions.size();
			int nOtherTransitions = other.transitions == null? 0 : other.transitions.size();
			if (nTransitions != nOtherTransitions) return false;

			for (i =0; i < transitions.size(); i++)
			{
				Transition t = transitions.get(i);
				Transition t2 = other.transitions.get(i);
				if (t.character != t2.character
						|| t.node != t2.node) return false;
			}

			return true;
		}

		public int nofTransitions()
		{
			return transitions == null? 0 : transitions.size();
		}

		public Transition transition(int i)
		{
			return transitions == null? null : transitions.get(i);
		}
		
		public int depth()
		{
			if (this.parentNode == null)
				return 0;
			return 1 + this.parentNode.depth();
		}
	}
	

	public static void main(String[] args)
	{
		Trie t = new Trie();
		t.loadWordlist(args[0]);
		try
		{
			t.saveToFile("/tmp/trie.out");
			System.err.println("saved...");
			t = Trie.readFromFile("/tmp/trie.out");
			System.err.println("restored...");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}

