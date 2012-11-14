package impact.ee.spellingvariation;

import impact.ee.spellingvariation.Alignment.AlignmentGraph;
import impact.ee.spellingvariation.Alignment.Position;

import java.util.List;
import java.util.Vector;

import org.jgrapht.EdgeFactory;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

/**
 * 
 * @author jesse
 * <p>
 * This object builds multigram "segmentation graphs" from unigram alignment graphs,
 * given a certain set of (joint) multigrams.
 * 
 */

public class AlignmentSegmenter
{
	private Alignment.AlignmentGraph g0;
	private MultigramSet multigramTrie;
	private SegmentationGraph segmentation;
	private EdgeFactory<Alignment.Position, Transition>  edgeFactory = new MyEdgeFactory();

	class MyEdgeFactory implements EdgeFactory<Alignment.Position,Transition>
	{
		public Transition createEdge(Position arg0, Position arg1)
		{
			return new Transition();
		}
	}
	
	public AlignmentSegmenter(MultigramSet ms)
	{
		this.multigramTrie = ms;
	}

	class Transition extends DefaultWeightedEdge
	{
		private static final long serialVersionUID = 5057643152177432637L;
		int multigramId;
		public Transition() { };
	}

	public class SegmentationGraph
			extends SimpleDirectedWeightedGraph<Alignment.Position, AlignmentSegmenter.Transition>
	{
		private java.util.Vector<Position> forward = null;
		private java.util.Vector<Position> backward = null;

		private static final long serialVersionUID = -1200550625492543937L;
		
		public SegmentationGraph(EdgeFactory<Alignment.Position, Transition> ef) 
		{
			super((EdgeFactory<Alignment.Position, Transition>) ef);
		}

		public java.util.Iterator<Position> getForwardIterator()
		{
			if (forward == null)
				this.initForwardBackward();
			return forward.iterator();
		}

		public Vector<Position> getForwardVector()
		{
			if (forward == null)
				this.initForwardBackward();
			return forward;
		}

		public Vector<Position> getBackwardVector()
		{
			if (forward == null)
				this.initForwardBackward();
			return backward;
		}

		/**
		 * prepare a vector of nodes in forward topological order
		 * and one in backward topological order
		 */
		
		private void initForwardBackward() // also set node indexes in top. order
		{
			java.util.Iterator<Position> i = 
				new TopologicalOrderIterator<Position, AlignmentSegmenter.Transition>(this);
			forward = new java.util.Vector<Position>();
			int index=0;
			while (i.hasNext())
			{
				Position p = i.next();
				p.index = index++;
				forward.add(p);
			}
			backward = new java.util.Vector<Position>();
			for (int j=forward.size()-1; j >=0; j--)
			{
				backward.add(forward.get(j));
			}
		}

		public java.util.Iterator<Position> getBackwardIterator() // Oops die is er niet!
		{
			if (forward == null)
			{
				this.initForwardBackward();
			}
			return backward.iterator();
		}
		public Position startPosition;
		public Position endPosition;
	}

	// decision to be made: will we allow multiple transitions between two nodes?
	// I suppose we would rather not: 
	// prefer [e/0 0/e] to be the same as [e/e] or [0/e e/0], etc.

	public void findSegmentationsFrom(Alignment.Position p)
	{
		if (p.visited)
			return;
		p.visited=true;
		MultigramSet.Node n = multigramTrie.startNode;
		for (Alignment.Transition t: g0.outgoingEdgesOf(p))
		{
			MultigramSet.Node next = n.transition(t.symbol);
			if (next != null)
			{
				findSegmentationsFromTo(p, g0.getEdgeTarget(t), next);
			}
		}
	}

	public void findSegmentationsFromTo(Position from, Position to, MultigramSet.Node n)
	{
		if (n.isFinal)
		{
			// add transition in the new graph
			Transition e = segmentation.getEdge(from, to);
			if (e == null)
			{
				if (n.set.getMultigramById(n.multigramId).active)
				{
					e = segmentation.addEdge(from, to);
				//.setEdgeWeight(e,fullGraph.getEdgeWeight(edge));
					e.multigramId = n.multigramId;
					//if (false) System.err.println("Yep transition from " + from + " to " + to + " on " + multigramTrie.getMultigramById(e.multigramId));
				}
			}
			findSegmentationsFrom(to);
		}
		for (Alignment.Transition t: g0.outgoingEdgesOf(to))
		{
			MultigramSet.Node next = n.transition(t.symbol);
			if (next != null)
			{
				findSegmentationsFromTo(from, g0.getEdgeTarget(t), next);
			}
		}
	}
	/**
	 * Main function for this class
	 * @param g
	 * @return
	 */
	public SegmentationGraph getSegmentation(AlignmentGraph g)
	{
		g0 = g;
		segmentation = new SegmentationGraph(edgeFactory);
		// keep all vertices; some may get isolated (not if we keep the basic 0-1/0-1 grams)
		for (Position p: g.vertexSet())
		{
			// hm can graphs share a vertex?
			p.visited = false;
			segmentation.addVertex(p);
		}
		findSegmentationsFrom(g.startPosition); // oops how to get start node...
		segmentation.startPosition = g.startPosition;
		segmentation.endPosition = g.endPosition;
		// need to renumber vertices? not really, this happens in the topological sorting bit

		int i=0;
		for (Position p: g.vertexSet())
			p.index =i++;
		return segmentation;
	}

	public static List<Transition> getShortestPath(SegmentationGraph g)
	{
		DijkstraShortestPath<Position, Transition> dsp = 
			new DijkstraShortestPath<Position, Transition>(g, g.startPosition, g.endPosition);
		return dsp.getPathEdgeList();
	}
}
