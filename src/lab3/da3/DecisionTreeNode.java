package da3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class DecisionTreeNode {

	private Integer order;
	private Integer commander;
	private ArrayList<DecisionTreeNode> children;
	private DecisionTreeNode parent;

	private Integer level;
	private Integer f;

	private boolean orderReceived;
	
	
	public DecisionTreeNode(Integer level)
	{
		setLevel(level);
		setOrderReceived(false);
		children = new ArrayList<DecisionTreeNode>();
		System.out.println("DecisionTreeNode(Integer level) should never be used");
	}
	
	/*
	 * Generate root node.
	 */
	public DecisionTreeNode(Integer commanderId, ArrayList<Integer> lieutenantIds, Integer f, Integer level)
	{
		setF(f);
		setLevel(level);
		setCommander(commanderId);
		setOrderReceived(false);
		children = new ArrayList<DecisionTreeNode>();
		
		if(f > 0)
		{
			for(Integer i : lieutenantIds)
			{
				ArrayList<Integer> reducedLieutenantIds = (ArrayList<Integer>) lieutenantIds.clone();
				reducedLieutenantIds.remove(Integer.valueOf(i));
				DecisionTreeNode childNode = new DecisionTreeNode(i, reducedLieutenantIds, f - 1, level + 1);
				childNode.setParent(this);
				children.add(childNode);
			}
		}
	}
	
	public void addDecision(ByzantineMessage bMessage)
	{
		
		ArrayList<Integer> commanderProcessIds = bMessage.getCommanderProcessIds();
		
			if(commanderProcessIds.size() > (getLevel() + 1) ) //the order is not given by this commander directly, pass the message to descendant node.
			{
				addDecisionToDescendant(bMessage);
			}
			else if (commanderProcessIds.size() == (getLevel() + 1) )  //the last commander's position is same as the tree level, set the order here.
			{
				if(isOrderReceived())
				{
					if(bMessage.isForged())
					{
					System.out.println("Fake bMessage overwrite value!");
					}
					else
					{
						//System.out.println("No reason to receive the message at this late");
					}
				}
				int receivedOrder = bMessage.getValue();
				if(receivedOrder==0 | receivedOrder==1)
				{
					setOrder(receivedOrder);
					setOrderReceived(true);
				}
				else
				{
					setOrder(Integer.valueOf(0));
					setOrderReceived(true);
				}
				setCommander(commanderProcessIds.get(getLevel()));
			}
			else if (commanderProcessIds.size() < (getLevel() + 1) ) // there are no commanders, initialized by divine power.
			{
				setOrder(bMessage.getValue());
				setOrderReceived(true);
				setCommander(bMessage.getLieutenantProcessId());
			}
		
	}
	
	/*
	 * Add decision to one of the descendant of the node.
	 * Add a new node if not exist.
	 */
	public void addDecisionToDescendant(ByzantineMessage bMessage)
	{
		ArrayList<Integer> commanderProcessIds = bMessage.getCommanderProcessIds();

		boolean branchExist = false;
		Integer nextLevel = getLevel()+1;
		Integer commanderNexLevel = commanderProcessIds.get(nextLevel);
		
		for(int i =0; i<children.size(); i++)
		{
			if(children.get(i).getCommander().equals(commanderNexLevel))
			{
				children.get(i).addDecision(bMessage);
				branchExist = true;
			}
		}
		
		if(!branchExist)
		{
			DecisionTreeNode dNode = new DecisionTreeNode(nextLevel);
			dNode.setCommander(commanderNexLevel);
			children.add(dNode);
			dNode.addDecision(bMessage);
		}
	}
	
	public ArrayList<Integer> getAncestorIds()
	{
		ArrayList<Integer> ancestorAndSelfIds = new ArrayList<Integer>();
		DecisionTreeNode ancestor = this;
		while(ancestor.getParent() != null)
		{
			ancestor = ancestor.getParent();
			ancestorAndSelfIds.add(ancestor.getCommander());
		}
		Collections.reverse(ancestorAndSelfIds);
		return ancestorAndSelfIds;
	}
	
	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	public Integer getCommander() {
		return commander;
	}

	public void setCommander(Integer commander) {
		this.commander = commander;
	}

	public ArrayList<DecisionTreeNode> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<DecisionTreeNode> children) {
		this.children = children;
	}
	
	public boolean isOrderReceived() {
		return orderReceived;
	}

	public void setOrderReceived(boolean orderReceived) {
		this.orderReceived = orderReceived;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}
	
	public String toString()
	{
		String nodeString =  "Node level: " + getLevel() + " Commander: " + getCommander() + " Order " +getOrder() + "\n";
		for(DecisionTreeNode childNode : children)
		{
			for(int i = 0; i<getLevel()+1; i++)
			{
				nodeString += "\t";
			}
			nodeString += childNode.toString();
		}
		return nodeString;
	}
	
	public Integer getF() {
		return f;
	}

	public void setF(Integer f) {
		this.f = f;
	}
	
	public DecisionTreeNode getParent() {
		return parent;
	}

	public void setParent(DecisionTreeNode parent) {
		this.parent = parent;
	}
	
	public Integer getMajorityOrder()
	{
		ArrayList<Integer> orders = new ArrayList<Integer>();
		
		if(isOrderReceived())
		{
			orders.add(getOrder());
		}
		
		
		for(DecisionTreeNode childNode : children)
		{
				orders.add(childNode.getMajorityOrder());
		}
		
		if(orders.contains(Integer.valueOf(1)) || orders.contains(Integer.valueOf(0)))
		{
			if(Collections.frequency(orders, 1) > Collections.frequency(orders, 0))
			{
				return 1;
			}
			else
			{
				return 0;
			}
		}
		else
		{
			return null;
		}	
	}

	public String DisplayMajority()
	{
		ArrayList<Integer> m = new ArrayList<Integer>();
		m.add(getMajorityOrder());
		for(DecisionTreeNode d : children)
		{
			m.add(d.getMajorityOrder());
		}
		return m.toString();
	}
}
