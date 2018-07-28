package routingdelivery.smartlog.brenntag.model;
import java.util.*;

import routingdelivery.model.Item;

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
