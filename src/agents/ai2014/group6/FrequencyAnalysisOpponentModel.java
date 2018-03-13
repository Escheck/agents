package agents.ai2014.group6;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import negotiator.Bid;
import negotiator.issue.Value;

public class FrequencyAnalysisOpponentModel implements IOpponentModel {
	
	HashMap<Integer,List<Value>> preferences;
	
	public FrequencyAnalysisOpponentModel() {
		preferences = new HashMap<Integer,List<Value>>();
	}

	@Override
	// Returns the value of an issue that is most often observed. 
	// If all values are observed an equal amount of times, then select the most recent value
	// If the issueId has not yet been observed, return null
	public Value getValue(Integer issueId) {
		List<Value> values = preferences.get(issueId);
		
		if(values == null) { 
			// If no values have been learned for the issue, return null
			return null;
		} else {
			// Otherwise, search value with the highest frequency or most recent
			int maxFreq = 0;
			Value maxVal = null;
			
			for(Value v: values) {
				int freq = Collections.frequency(values, v);
				if(freq > maxFreq) {
					maxFreq = freq;
					maxVal = v;
				}
			}
			
			return maxVal;
		}
	}

	@Override
	public void learnWeights(Bid bid) {
		for(Entry<Integer, Value> issue : bid.getValues().entrySet()) {
			if(!preferences.containsKey(issue.getKey()))
				preferences.put(issue.getKey(), new ArrayList<Value>());
				
			preferences.get(issue.getKey()).add(issue.getValue());
		}
	}

}
