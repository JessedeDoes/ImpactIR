package util;


/**
 * Shortest path on 'lazy' graphs
 * @author jesse
 *
 */
public class ShortestPath<VertexClass extends ShortestPath.BasicState, EdgeClass>
{
	java.util.PriorityQueue<MatchState> queue = new java.util.PriorityQueue<MatchState>();
	java.util.Map<VertexClass, MatchState<VertexClass>> itemMap = new java.util.Hashtable<VertexClass, MatchState<VertexClass>>();

	public  static abstract class BasicState
	{
		public abstract boolean isFinal();
		public abstract void relaxEdges(ShortestPath sp, MatchState<BasicState> source);
	}

	public abstract static class BasicEdge
	{

	}

	public void relaxEgde(BasicEdge e, MatchState<VertexClass> source, BasicState target, int cost)
	{
		MatchState<VertexClass> ms = itemMap.get(target);
		if (ms != null)
		{
			if (ms.cost  > source.cost + cost)
			{
				ms.cost = source.cost + cost;
				ms.backLink = e;
				ms.parentState = source;
				queue.remove(ms); // silly way of decreasing cost
				queue.offer(ms);
			}
		} else
		{
			//System.err.println("eek " + e);
			ms = new MatchState(target);
			ms.cost = source.cost + cost;
			ms.parentState = source;
			ms.backLink = e;
			queue.offer(ms);
		}
	}

	public static class MatchState<S extends BasicState> implements Comparable<MatchState<S>>
	{
		public S base;
		public int cost;
		BasicEdge backLink;
		MatchState<S> parentState;

		MatchState(S base)
		{
			this.base = base;
			parentState = null;
		}

		public boolean equals(Object other)
		{
			try
			{
				MatchState<S> so = (MatchState<S>) other;
				return so.base.equals(this.base);
			} catch (Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}
		
		public int compareTo(MatchState other)
		{
			return cost - other.cost;
		}
	}

	public java.util.List<EdgeClass> bestFirstSearch(VertexClass startState)
	{
		queue.clear();
		itemMap.clear();
		MatchState<VertexClass> start = new MatchState<VertexClass>(startState);
		start.cost = 0;
		queue.offer(start);
		while (true)
		{
			MatchState<BasicState> item = queue.poll();
			if (item == null) break;
			if (item.base.isFinal())
			{
				java.util.LinkedList<EdgeClass> path = new java.util.LinkedList<EdgeClass>();
				MatchState<BasicState> itemP = item;
				while (itemP != null)
				{
					if (itemP.backLink != null)
						path.addFirst((EdgeClass) itemP.backLink);
					itemP = itemP.parentState;
				}
				return path;
			}
			item.base.relaxEdges(this, item);
		}
		return null;
	}
}
