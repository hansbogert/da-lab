package da3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class DecisionTreeNode {

	private Integer order;
	private Integer commander;
	private ArrayList<DecisionTreeNode> children;
	private Integer level;

	private boolean orderReceived;
	
	/*
	 * Generate root node.
	 */
	public DecisionTreeNode()
	{
		setLevel(0);
		setOrderReceived(false);
		children = new ArrayList<DecisionTreeNode>();
	}
	
	public DecisionTreeNode(Integer level)
	{
		setLevel(level);
		setOrderReceived(false);
		children = new ArrayList<DecisionTreeNode>();
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
				int receivedOrder = bMessage.getValue();
				if(receivedOrder==0 | receivedOrder==1)
				{
					setOrder(receivedOrder);
				}
				else
				{
					setOrder(Integer.valueOf(0));
				}
				setCommander(commanderProcessIds.get(getLevel()));
			}
			else if (commanderProcessIds.size() < (getLevel() + 1) ) // there are no commanders, initialized by divine power.
			{
				setOrder(bMessage.getValue());
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
			dNode.setOrder(0);
			children.add(dNode);
			dNode.addDecision(bMessage);
		}
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
	
	public static void main(String[] args)
	{
		ArrayList<Integer> commandersA = new ArrayList<Integer>();
		ArrayList<Integer> lienntautsA = new ArrayList<Integer>(Arrays.asList(2, 3, 4, 5));
		ByzantineMessage bMessage = new ByzantineMessage(1, 1, commandersA, 1, lienntautsA);
		
		ArrayList<Integer> commandersB = new ArrayList<Integer>(Arrays.asList(1));
		ArrayList<Integer> lienntautsB = new ArrayList<Integer>(Arrays.asList(3, 4, 5));
		ByzantineMessage bMessageB = new ByzantineMessage(1, 1, commandersB, 2, lienntautsB);
		
		ArrayList<Integer> commandersC = new ArrayList<Integer>(Arrays.asList(1, 2));
		ArrayList<Integer> lienntautsC = new ArrayList<Integer>(Arrays.asList(4, 5));
		ByzantineMessage bMessageC = new ByzantineMessage(0, 0, commandersC, 3, lienntautsC);
		
		ArrayList<Integer> commandersD = new ArrayList<Integer>(Arrays.asList(1, 3));
		ArrayList<Integer> lienntautsD = new ArrayList<Integer>(Arrays.asList(4, 5));
		ByzantineMessage bMessageD = new ByzantineMessage(0, 1, commandersD, 2, lienntautsD);
		
		ArrayList<Integer> commandersE = new ArrayList<Integer>(Arrays.asList(1, 4, 3));
		ArrayList<Integer> lienntautsE = new ArrayList<Integer>(Arrays.asList(5));
		ByzantineMessage bMessageE = new ByzantineMessage(-1, 0, commandersE, 2, lienntautsE);
		
		System.out.println(bMessage);
		
		DecisionTreeNode dNodeA = new DecisionTreeNode();
		//dNodeA.addDecision(bMessage);
		dNodeA.addDecision(bMessageB);
		dNodeA.addDecision(bMessageC);
		dNodeA.addDecision(bMessageD);
		dNodeA.addDecision(bMessageE);
		
		int finalOrder = dNodeA.getMajorityOrder();
		
		
		System.out.println(dNodeA);
		System.out.println("finalOrder: " + finalOrder);
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
	
	public int getMajorityOrder()
	{
		ArrayList<Integer> orders = new ArrayList<Integer>();
		
		orders.add(getOrder());
		
		for(DecisionTreeNode childNode : children)
		{
			orders.add(childNode.getMajorityOrder());
		}
		
		if(Collections.frequency(orders, 1) > Collections.frequency(orders, 0))
		{
			return 1;
		}
		else
		{
			return 0;
		}
		
	}
}
