package spellingvariation;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

import spellingvariation.History.State;

public class AbsoluteDiscountModel implements StateModel<History.State>,
java.io.Serializable
{
	private static final long serialVersionUID = -318791996168241101L;
	int MODEL_ORDER = 3;
  static int MAX_ORDER=100;
  double[] discounts = new double[MAX_ORDER]; // discount parameters
  // double[] lambda;    // interpolation parameters
 // double[] nLeftProlongations; // for each n-gram, count number of prolongations "to the left"  (perplexity instead of n)
  transient double[] nRightProlongations; // for each n-gram, count number of prolongations "to the right"  (perplexity instead of n)
  //double[] nLeftRightProlongations; // for each n-gram count number of prolongations in two directions (perplexity)
  transient double[] n1 = new double[MAX_ORDER]; 
  transient double[] n2 = new double[MAX_ORDER];
  transient double[] stateEvidence;
  History mainHistory;
 
  Vector<TransducerWithMemory> unsmoothedModels =
  	new Vector<TransducerWithMemory>();
  
  public AbsoluteDiscountModel()
  {
  }

  /**
   * Smoothed probability.
   * <p>
   * Does not have the apparently desirable property that lower order distribution
   * depends on higher order, as in Kneser-Ney smoothing and in smoothing of log-linear (ME) models
   * with gaussian prior
   * 
   * @param s state
   * @param c observed symbol
   */
  
  public State delta(State state, int c)
  {
  	return delta(state,c,true);
  }
  
	public State delta(State state, int c, boolean tryUplink)
	{
		// First try to extend the history if we are already in a backoff state
		if (state.isWordFinal)
			return null;
		
		if (tryUplink) // this is totally silly, should be precompiled
		{
			State uplink = state.getUplink();
			if (uplink != null)
			{
				State z = uplink.prolongation(c);
				if (z != null)
					return z;
			}
		}
			
		State x = state.stateTransition(c); // no: this attempts to prolong if needed
		
		if (x != null)
		{
			return x;
		} else
		{
			// we need to back off...
			State backoffState = getBackoffState(state);
			
			if (backoffState != null)
			{
				//System.err.println("backing off: " + getEnclosingModelOrder(state) + ":"
				//		+ state + " -> " + getEnclosingModelOrder(backoffState) +  ": " + backoffState);
				return delta(backoffState,c, false); // this may back off more steps if necessary
			} else
			{
				return null;
			}
		}
	}
	

	/** 
	 * rather a lot can be precomputed here for seen n-grams
	 */
	public double conditionalProbability(State s, int c)
	{
		// TODO Auto-generated method stub
		if (s.isWordFinal)
			return 0;
		
		double pUnsmoothed = s.conditionalProbability(c);
		double p1 = Math.max(pUnsmoothed - discounts[getEnclosingModelOrder(s)] / evidenceOf(s), 0);
		State backoffState = getBackoffState(s);
		if (backoffState == null)
		{
			return p1;  // wrong!
		} else
		{
			double pBackoff = conditionalProbability(backoffState, c); // Hola: kan niet zomaar!! moet ook gesmoest!
			return p1 +  s.lambda * pBackoff;
		}
	  // return pUnsmoothed - discounts[order] / X(s) + 
	}

	State getBackoffState(State s)
	{
		if (s.rightParentState == null)
			return null;
		return s.rightParentState.getDownlink();
	}
	
	int getEnclosingModelOrder(State s)
	{
		return s.getEnclosingHistory().MODEL_ORDER;
	}
	
	public void precomputeLambda(State s)
	{
		double discountedSum = 0;
		double x = discounts[s.order] / evidenceOf(s);
		// HM TODo zoek uit: afhankelijk van orde welke je moet pakken
		// voor de wordInitialStates
		for (History.Transition t: s.getStateTransitions())
		{
			double p1 = Math.max(t.p - x, 0);
			discountedSum += p1;
		}
		s.lambda = 1 - discountedSum;
		System.err.println(s + ": evidence= " +  s.evidence + ": lambda =  " + s.lambda);
	}
	
	private double evidenceOf(State s)
	{
		// TODO Auto-generated method stub
		return s.evidence;
	}

	/**
	 * Hoe doe je dit precies..
	 * Als je doodloopt, ga dan omhoog.
	 * Maar later ....
	 * Als (orde &lt;modelorde) en trieTransitie gedefinieerd,
	 * ga dan weer eentje langer 
	 */
		
	public State getStartState()
	{
		// TODO Auto-generated method stub
		return this.unsmoothedModels.get(this.unsmoothedModels.size()-1).histories.startState;
	}

	/**
	 * Check of deze functies eigenlijk wel goed werken!!
	 */
	public State getStateById(int id)
	{
		// TODO Auto-generated method stub
		int index = id % mainHistory.size;
		int modelNumber = id / mainHistory.size;
		State s = unsmoothedModels.get(modelNumber).histories.getStateById(index);
		if (s.index != index || getStateId(s) != id)
		{
			System.err.println("Ramp");
			System.exit(1);
		}
		return s;
	}

	public int getStateId(State s)
	{
		// TODO Auto-generated method stub
		return (getEnclosingModelOrder(s)-1)*mainHistory.size + s.index; // wrong again....
	}

	public int size() 
	{
		// TODO Auto-generated method stub
    return (mainHistory.size + 1) * unsmoothedModels.size();
	}
	
	/**
	 * should be synchronized.
	 * @param alphas
	 * @param betas
	 */
	void setDiscounts(History h)
	{
		int N = h.MODEL_ORDER;
		for (State s: h.allStates)
		{
			System.err.println(s + ": " + evidenceOf(s));
			if (evidenceOf(s) <= 1 && evidenceOf(s) > 0)
				n1[N]++;
		  else if  (evidenceOf(s) <= 2)
		  	n2[N]++;	
		}
		this.discounts[N] = n1[N] / (n1[N] + 2 *n2[N]);
	}
	
	public void estimateParameters(Dataset d)
	{
		MultigramTransducer mt = new MultigramTransducer();
		mt.multigramPruner = new DutchMultigramPruner();
		mt.MAX_ITERATIONS =2;
		mt.MODEL_ORDER = 4;
		mt.estimateParameters(d);
		TransducerWithMemory previousModel = null;
		for (int i=1; i <= MODEL_ORDER; i++)
		{
			TransducerWithMemory ti = new TransducerWithMemory(mt);
			ti.MODEL_ORDER=i;
			ti.MAX_ITERATIONS=3;
			ti.estimateParameters();
			unsmoothedModels.add(ti);
			if (previousModel != null)
			{
				History.connectModels(previousModel.histories,  ti.histories);
			}
			History h = ti.histories;
			setDiscounts(h);
	  	for (State s: h.allStates)
	  	{
	  		precomputeLambda(s);
	  	}
	  	previousModel = ti;
		}
		mainHistory = unsmoothedModels.get(unsmoothedModels.size()-1).histories;
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
	
	public static AbsoluteDiscountModel readFromFile(String filename)
	{
		try
		{
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
			AbsoluteDiscountModel t = (AbsoluteDiscountModel) in.readObject();
			//t.checkConnections();
			in.close();
			System.err.println(" " + t.discounts[t.MODEL_ORDER]);
			return t;
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public void checkConnections()
	{
		//
		for (TransducerWithMemory t: unsmoothedModels)
		{
			History h = t.histories;
			for (State s: h.allStates)
			{
				State b = getBackoffState(s);
				if (b != null)
				{
					System.err.println("backoff" + s + " -> " + b);
				}
			}
		}
	}
	
	public static void usage()
	{
		System.out.println("One argument: training-dataset-filename\nModel is saved to /tmp/model.out");
	}

	public MultigramSet getMultigramSet()
	{
		return unsmoothedModels.get(0).getMultigramSet();
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
		AbsoluteDiscountModel a = new AbsoluteDiscountModel();
		a.estimateParameters(d);
		a.saveToFile("/tmp/modelz.out");
	}
}
