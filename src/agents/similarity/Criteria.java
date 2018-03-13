package agents.similarity;

import negotiator.Bid;
import negotiator.xml.SimpleElement;

public interface Criteria {

	public double getValue(Bid pBid);
	
	public void loadFromXML(SimpleElement pRoot);
}
