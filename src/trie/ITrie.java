package trie;

public interface ITrie<NodeClass>
{
	
		public NodeClass delta(NodeClass state, int c);
		public NodeClass getStartState();
		public boolean isFinal(NodeClass state);
		public void setNodeData(NodeClass node, Object data);
		public void loadWordlist(String fileName);
		public Object getNodeData(NodeClass node);
}
