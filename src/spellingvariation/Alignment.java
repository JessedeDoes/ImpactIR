
package spellingvariation;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.alg.*;

import spellingvariation.Alphabet.CodedString;

import java.util.ArrayList;
import java.util.List;


/**
 * Main function: constructRestrictedGraph
 * Prune the full alignment matrix obtained from the memoryless unigram transducer 
 * to the more plausible possibilities<br>
 * This is done by applying a K-shortest-path construction on the full alignment graph
 * <p>
 * This construction is not very fast. There should be a faster way for DAGs
 * </p>
 */

public class Alignment
{
	private UnigramTransducer t;
	private int m;
	private int n;
	private CodedString x;
	private CodedString y;
	private Position [][] vertexMatrix;
	private List<GraphPath<Position,Transition>> pathList;
	private List<Position[]> condensedPathList = new ArrayList<Position[]>();
	private SimpleDirectedWeightedGraph<Position, Transition> fullGraph;
	protected AlignmentGraph restrictedGraph;
	private EdgeFactory<Position, Transition>  edgeFactory = new MyEdgeFactory();
	
	private Position getVertexAt(int i, int j)
	{
		return vertexMatrix[i][j];
	}

	private double weightOf(int i, int j)
	{
		double w = -Math.log(t.delta[i][j]);
		if (Double.isNaN(w)) 
			w = Double.POSITIVE_INFINITY;
		return w;
	}

	protected class Transition extends DefaultWeightedEdge
	{
		private static final long serialVersionUID = 7972340445253456740L;
		int symbol;
		public Transition()
		{
			
		}
	}
	
	protected class Position implements Cloneable
	{
		int lpos; 
		int rpos;
		int index; // is this needed?
		protected boolean visited = false;
		
		@Deprecated public Position clone() // not used..
		{
			Position p = new Position();
      p.lpos = lpos;
      p.rpos = rpos;
      p.index = index;
      return p;
		}
		public String toString()
		{
			return "[" + lpos + "," + rpos + "]";
		}
	}
	
	protected class AlignmentGraph extends SimpleDirectedWeightedGraph<Alignment.Position, Transition>
	{
		public AlignmentGraph(EdgeFactory<Position, Transition> ef) 
		{
			super((EdgeFactory<Position, Transition>) ef);
		}
		public Position startPosition;
		public Position endPosition;
	}
	
	public AlignmentGraph constructRestrictedGraph()
	{
		restrictedGraph = new AlignmentGraph(edgeFactory);
		for (GraphPath p: pathList)
		{
			//Position[] positions = new Position[p.getEdgeList().size()+1];
			if (Double.isInfinite(p.getWeight())) // or otherwise too small
			{
				 // break; // TODO zoek uit waarom dit NIET kan
			}
			int i=0;
			for (Object o: p.getEdgeList())
			{
				Transition edge = (Transition) o;
				Position source = (Position) fullGraph.getEdgeSource(edge);
				Position target = (Position) fullGraph.getEdgeTarget(edge);
				restrictedGraph.addVertex(source); // TODO what if already in graph??
				restrictedGraph.addVertex(target); // TODO what if already in graph??
				Transition e = restrictedGraph.getEdge(source, target);
				if (e == null)
				{
					   /* System.err.printf("Add edge from [%d,%d] to [%d,%d] symbol [%d=%c/%c] weight=%f\n",
					   source.lpos, source.rpos, target.lpos, target.rpos, edge.symbol,
					   t.inputAlphabet.decode(t.symbolPairs[edge.symbol].lhs),
					   t.outputAlphabet.decode(t.symbolPairs[edge.symbol].rhs),
					   fullGraph.getEdgeWeight(edge));
             */
	
					e = restrictedGraph.addEdge(source, target);
					restrictedGraph.setEdgeWeight(e,fullGraph.getEdgeWeight(edge));
					e.symbol = edge.symbol;
				}
			}
			//positions[i] = (Position) p.getEndVertex();
			//condensedPathList.add(positions);
			//System.err.println(p.getEdgeList().size() + " " +  p.getWeight());
		}
		restrictedGraph.startPosition = this.getVertexAt(0, 0);  
		restrictedGraph.endPosition = this.getVertexAt(m,n);
		return restrictedGraph;
	}

	private void constructFullGraph()
	{
		vertexMatrix = new Position [m+1][n+1];
		for (int i=0; i<= m; ++i)
			for (int j=0; j <=n; j++)
			{
				Position vertex = new Position();
				vertex.lpos=i;
				vertex.rpos=j;
				fullGraph.addVertex(vertex);
				vertexMatrix[i][j] = vertex;
			}

		for (int i=0; i<= m; ++i)
		{
			for (int j=0; j <=n; j++)
			{
				Position vertex = getVertexAt(i,j);
				if (i > 0 && j > 0) // replace
				{ 
					double w = weightOf(x.get(i-1),y.get(j-1)); // -Math.log(t.delta[x.get(i-1)][y.get(j-1)]); // should use -log(delta) idiot
					Position source = getVertexAt(i-1,j-1);
					Transition newEdge = fullGraph.addEdge(source, vertex);
					newEdge.symbol = t.getCodeForSymbolPair(x.get(i-1), y.get(j-1));
					fullGraph.setEdgeWeight(newEdge, w);
				} 
				if (i > 0) // delete
				{
					double w = weightOf(x.get(i-1),Alphabet.空); // -Math.log(t.delta[x.get(i-1)][Transducer.空]);
					Position source = getVertexAt(i-1,j);
					Transition newEdge = fullGraph.addEdge(source, vertex);
					newEdge.symbol = t.getCodeForSymbolPair(x.get(i-1), Alphabet.空);
					fullGraph.setEdgeWeight(newEdge, w);
				}
				if (j > 0) // insert
				{
					double w = weightOf(Alphabet.空,y.get(j-1)); // -Math.log(t.delta[Transducer.空][y.get(j-1)]);
					Position source = getVertexAt(i,j-1);
					Transition newEdge = fullGraph.addEdge(source, vertex);
					newEdge.symbol = t.getCodeForSymbolPair(Alphabet.空, y.get(j-1));
					fullGraph.setEdgeWeight(newEdge, w);
				}
			}
		}
	}



	/**
	 * Should replace this by a simple representation not depending on the graph
	 * @return
	 */

	/**
	 * 
	 * @param t
	 * @param x
	 * @param y
	 * @param k number of shortest paths to construct
	 */
	
	public List<Position[]> getCondensedPathList()
	{
		return condensedPathList;
	}

	public class MyEdgeFactory implements EdgeFactory<Position,Transition>
	{

		public Transition createEdge(Position arg0, Position arg1) 
		{	
			return new Transition();
		}
	}
	
	public Alignment(UnigramTransducer t, CodedString x, CodedString y, int k)
	{  
		this.t = t; this.x = x; this.y = y;
		this.m = x.size; this.n = y.size;

		fullGraph = new SimpleDirectedWeightedGraph<Position, Transition>(edgeFactory);

		this.constructFullGraph();
		if (k>1)
		{
			KShortestPaths<Position, Transition> K = 
				new KShortestPaths<Position, Transition>(fullGraph, getVertexAt(0,0), k);
			pathList = K.getPaths(getVertexAt(m,n));
		} else // probably faster
		{
			DijkstraShortestPath<Position, Transition> D = 
				new DijkstraShortestPath<Position, Transition>(fullGraph, getVertexAt(0,0), getVertexAt(m,n));
			GraphPath p = D.getPath(); 
			pathList = new ArrayList();
			pathList.add(p);
		}
		
		// System.err.println(x + ", " + y + ": " + pathList.get(0).getWeight() + "-" + pathList.get(pathList.size()-1).getWeight());

		for (GraphPath p: pathList)
		{
			Position[] positions = new Position[p.getEdgeList().size()+1];
			int i=0;
			for (Object o: p.getEdgeList())
			{
				Transition edge = (Transition) o;
				positions[i++] = (Position) fullGraph.getEdgeSource(edge);
			}
			positions[i] = (Position) p.getEndVertex();
			condensedPathList.add(positions);
			//System.err.println(p.getEdgeList().size() + " " +  p.getWeight());
		}
		constructRestrictedGraph();
		if (false)
		{
			System.err.println("Full graph has " 
					+ fullGraph.vertexSet().size() + " vertices and "
					+ fullGraph.edgeSet().size() + " edges.");
			System.err.println("Restricted graph has " 
					+ restrictedGraph.vertexSet().size() + " vertices and "
					+ restrictedGraph.edgeSet().size() + " edges.");
		}
		vertexMatrix = null;
		fullGraph = null;
		pathList = null;
	}
}
