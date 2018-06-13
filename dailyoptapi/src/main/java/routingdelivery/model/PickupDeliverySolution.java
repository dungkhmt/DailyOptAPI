package routingdelivery.model;

public class PickupDeliverySolution {
	private RoutingSolution[] routes;
	private Item[] unScheduledItems;
	
	public PickupDeliverySolution(RoutingSolution[] routes,
			Item[] unScheduledItems) {
		super();
		this.routes = routes;
		this.unScheduledItems = unScheduledItems;
	}

	public Item[] getUnScheduledItems() {
		return unScheduledItems;
	}

	public void setUnScheduledItems(Item[] unScheduledItems) {
		this.unScheduledItems = unScheduledItems;
	}

	public RoutingSolution[] getRoutes() {
		return routes;
	}

	public void setRoutes(RoutingSolution[] routes) {
		this.routes = routes;
	}

	public PickupDeliverySolution(RoutingSolution[] routes) {
		super();
		this.routes = routes;
	}

	public PickupDeliverySolution() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
