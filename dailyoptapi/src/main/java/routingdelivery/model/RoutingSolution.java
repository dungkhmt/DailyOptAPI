package routingdelivery.model;

public class RoutingSolution {
	private RoutingElement[] elements;

	public RoutingElement[] getElements() {
		return elements;
	}

	public void setElements(RoutingElement[] elements) {
		this.elements = elements;
	}

	public RoutingSolution(RoutingElement[] elements) {
		super();
		this.elements = elements;
	}

	public RoutingSolution() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
