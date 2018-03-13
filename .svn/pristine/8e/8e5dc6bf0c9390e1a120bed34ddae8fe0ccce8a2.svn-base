package agents.ai2014.group12;

import java.util.ArrayList;
import java.util.Set;

import negotiator.issue.ValueDiscrete;
import negotiator.utility.EvaluatorDiscrete;

/**
 * PreferenceBlock is our representation of the Issue
 *
 */
public class PreferenceBlock {
	ArrayList<Node> nodeList = new ArrayList<Node>();
	String issue;
	Double weight;
	
	/**
	 * Constructor for the PreferenceBlock
	 * @param block, string to parse to get the values of the issue out.
	 * @param issueName
	 * @param weight
	 */
	public PreferenceBlock(EvaluatorDiscrete evaluator, String issueName, Double weight){
		this.issue = issueName;
		this.weight = weight;
		Set<ValueDiscrete> evalSet = evaluator.getValues();
		Object[] evalArray = evalSet.toArray();
		for(Object i : evalArray) {
			ValueDiscrete description = (ValueDiscrete) i;
			Double val = evaluator.getDoubleValue(description);
			Node node = new Node(description.getValue(), val, false);
			nodeList.add(node);
		}
		orderNodesLowToHigh();
	}
	
	public void normalizeNodeValues() {
		Double sum = sumNodeValues();
		if(sum > 0) {
			for(int i = 0;i<nodeList.size();i++) {
				nodeList.get(i).setValue(nodeList.get(i).getValue()/sum);
			}
		}
	}
	
	public Double sumNodeValues() {
		Double sum = 0.0;
		for(int i = 0; i<nodeList.size();i++) {
			sum += nodeList.get(i).getValue();
		}
		return sum;
	}
	
	/**
	 * Returns the node that is currently the highest for this issue
	 * @return
	 */
	public Node getHighestPreference() {
		double max = 0;
		Node rtn = new Node("", 0, false);
		for(Node n : nodeList) {
			if(n.getValue() >= max) {
				rtn = n;
				max = n.getValue();
			}
		}
		return rtn;
	}
	
	/**
	 * Searches the nodeList of this issue for the value name of a node
	 * @param s
	 * @return index of the found node
	 */
	public int indexOf(String s) {
		int index = 0;
		for(int i = 0; i < nodeList.size(); i++) {
			if(nodeList.get(i).getName().equals(s)) {
				index = i;
			}
		}
		return index;
	}
	
	/**
	 * Method to order the issue's nodes from low to high
	 */
	public void orderNodesLowToHigh() {
		nodeList = quicksort(nodeList);
	}

	/**
	 * Basic quicksort algorithm
	 * @param input
	 * @return sorted arraylist of nodes
	 */
	public ArrayList<Node> quicksort(ArrayList<Node> input) {
		if(input.size() <= 1){
			return input;
		}
		
		int middle = (int) Math.ceil((double)input.size() / 2);
		Node pivot = input.get(middle);
 
		ArrayList<Node> less = new ArrayList<Node>();
		ArrayList<Node> greater = new ArrayList<Node>();
		
		for (int i = 0; i < input.size(); i++) {
			if(input.get(i).getValue() <= pivot.getValue()){
				if(i == middle){
					continue;
				}
				less.add(input.get(i));
			}
			else{
				greater.add(input.get(i));
			}
		}
		
		return concatenate(quicksort(less), pivot, quicksort(greater));
	}
	
	/**
	 * Helper method for the quicksort algorithm, is used to concatenate the two arraylists to eachother
	 * @param less
	 * @param pivot
	 * @param greater
	 * @return concatenated arraylist
	 */
	private ArrayList<Node> concatenate(ArrayList<Node> less, Node pivot, ArrayList<Node> greater){
		
		ArrayList<Node> list = new ArrayList<Node>();
		
		for (int i = 0; i < less.size(); i++) {
			list.add(less.get(i));
		}
		
		list.add(pivot);
		
		for (int i = 0; i < greater.size(); i++) {
			list.add(greater.get(i));
		}
		
		return list;
	}
	
	/**
	 * Nodelist getter
	 * @return
	 */
	public ArrayList<Node> getList(){
		return nodeList;
	}

	/**
	 * Method to retrieve all the nodes that have not yet been flagged
	 * @return
	 */
	public ArrayList<Node> getValuesWithoutFlag() {
		ArrayList<Node> rtn = new ArrayList<Node>();
		for(int i = 0; i<nodeList.size(); i++) {
			if(!nodeList.get(i).getFlag()) {
				rtn.add(nodeList.get(i));
			}
		}
		return rtn;
	}

	/**
	 * Method to retrieve the index of the highest ordered value that is not yet flagged.
	 * @return
	 */
	public int getHighestIndexWithoutFlag() {
		int rtnIndex = -1;
		for(int i = 0; i<nodeList.size(); i++) {
			if(nodeList.get(i).getFlag()) {
				return i-1;
			}
		}
		return rtnIndex;
	}
	
}
