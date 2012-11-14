package impact.ee.spellingvariation;

/**
 * this is a bit silly
 * @author jesse
 *
 */
interface CodeInterpreter
{
	public String toString(int i);
}

/**
 * abstraction of state model
 * @author jesse
 *
 * @param <StateClass>
 */
public interface StateModel<StateClass>
{
	public StateClass delta(StateClass state, int c);
	public StateClass getStartState();
	public double conditionalProbability(StateClass s, int c);
	public int size(); // this is unpleasant. needed for efficient state retrieval; but rather factor state storage away..
}

