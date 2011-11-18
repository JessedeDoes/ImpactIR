package spellingvariation;

import java.io.BufferedReader;
import java.io.FileReader;

import trie.Trie;
import trie.Trie.TrieNode;


/**
*
*/

class StatePlus
{
	History.State baseState;
	TrieNode lexicalNode;
	public boolean equals(Object other)
	{
		try
		{
			StatePlus otherState = (StatePlus) other;
			return (otherState.baseState.index == baseState.index)
					&& (otherState.lexicalNode == lexicalNode);
		} catch (Exception e)
		{
			return false;
		}
	}
	
	public int hashCode()
	{
		return baseState.index + lexicalNode.hashCode();
	}
	
	public String toString()
	{
		return baseState.toString();
	}
}

public class LexiconConstrainedModel implements StateModel<StatePlus>
{
   StateModel<History.State> baseModel;
   MultigramSet multigramSet;
   Trie lexicon = new Trie();
   StatePlus startState = new StatePlus();
   //Hashtable <StatePlus,StatePlus> stateMap = new  Hashtable <StatePlus,StatePlus>();
  
   
   public LexiconConstrainedModel(StateModel<History.State> base, 
  		 String lexiconFilename, MultigramSet mset)
   {
  	 lexicon.loadWordlist(lexiconFilename, false, true);
  	 baseModel = base;
  	 multigramSet = mset;
  	 startState.baseState = baseModel.getStartState();
  	 startState.lexicalNode = lexicon.root;
   }
   
	public double conditionalProbability(StatePlus state, int c)
	{
		// TODO Auto-generated method stub
		String s = multigramSet.getLHS(c);
		//System.err.println(state.baseState + " " + s);
		TrieNode lexNext = state.lexicalNode.findNode(s);
		if (lexNext == null)
			return 0;
		return baseModel.conditionalProbability(state.baseState, c);
	}

	public StatePlus delta(StatePlus state, int c)
	{
		// TODO Auto-generated method stub
		String s = multigramSet.getLHS(c);
		TrieNode lexNext = state.lexicalNode.findNode(s);
		if (lexNext == null)
			return null;
		History.State nextState = baseModel.delta(state.baseState, c);
		if (nextState == null)
			return null;
		StatePlus newState = new StatePlus();
		newState.baseState = nextState;
		newState.lexicalNode = lexNext;
		/*
		StatePlus foundState = stateMap.get(newState);
		if (foundState != null)
			return foundState;
		*/
		return newState;
	}

	public StatePlus getStartState()
	{
		// TODO Auto-generated method stub
		return startState;
	}



	public int size()
	{
		// TODO Auto-generated method stub
		return Integer.MAX_VALUE;
	}
	
	public static void testModel(StateModel<History.State> model, 
			MultigramSet multigramSet, 	
			String lexiconFilename,
			java.io.BufferedReader inFile)
	{
		LexiconConstrainedModel lmodel = new LexiconConstrainedModel(model, lexiconFilename, multigramSet);
		MatcherWithMemory<StatePlus> m = new MatcherWithMemory<StatePlus>(multigramSet,
				lmodel);
		m.stateMap = new HashStateMap();

		System.err.println("start testing..");
		String s = null;
		try
		{
			while ((s = inFile.readLine()) != null)
			{
				//m.beamSearch(Alphabet.initialBoundary + s + Alphabet.finalBoundary,10e-4);
				m.beamSearch(Alphabet.initialBoundary + s + Alphabet.finalBoundary,10e-5);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
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
		TransducerWithMemory t = TransducerWithMemory.readFromFile(modelFile);
		String lexiconFile = "/home/jesse/Projects/Lexicon/wntlex.ls";
		testModel(t.histories,  t.getMultigramSet(), lexiconFile, inFile);
		//testSmoothedModel(modelFile, inFile);
	}
}
