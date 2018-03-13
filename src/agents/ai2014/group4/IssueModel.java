package agents.ai2014.group4;

import java.util.HashMap;
import java.util.List;

import negotiator.issue.ValueDiscrete;

public class IssueModel{

	private List<ValueDiscrete> values;
	private HashMap<String, Double> utility;
	private double value;
	private String name;
	
	public IssueModel(String name, List<ValueDiscrete> values){
		this.setName(name);
		this.values = values;
		
		this.utility = new HashMap<String, Double>();
		for (ValueDiscrete option: values) {
			utility.put(option.getValue(), 1.0);
		}
	}
	
	public double getUtility(ValueDiscrete option){
		return utility.get(option.getValue());
	}
	
	public void updateUtility(ValueDiscrete option, double change){
		utility.put(option.getValue(), utility.get(option.getValue()) + change);
		//normalisation
		double max=0;
		for(ValueDiscrete o: values){
			max=Math.max(max, utility.get(o));
		}
		for(ValueDiscrete o: values){
			utility.put(o.getValue(), Math.max(0, utility.get(o.getValue()))/max);
		}
	}
	
	public void setValue(double a){
		value = a;
	}
	
	public double getValue(){
		return value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public HashMap<String, Double> getUtility() {
		return utility;
	}

	public void setUtility(HashMap<String, Double> utility) {
		this.utility = utility;
	}

	public List<ValueDiscrete> getValues() {
		return values;
	}

	public void setValues(List<ValueDiscrete> values) {
		this.values = values;
	}
	
	

}
