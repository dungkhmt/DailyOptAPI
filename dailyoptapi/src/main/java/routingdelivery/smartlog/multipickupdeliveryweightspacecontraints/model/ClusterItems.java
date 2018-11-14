package routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model;
import java.util.*;



public class ClusterItems {
	public ArrayList<Item> items;
	public double weight;
	public ClusterItems(ArrayList<Item> items) {
		super();
		this.items = items;
		weight = 0;
		for(Item I: items)
			weight += I.getWeight();
	}
	
}
