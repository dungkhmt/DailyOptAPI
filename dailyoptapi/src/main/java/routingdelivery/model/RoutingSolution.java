package routingdelivery.model;

public class RoutingSolution {
	private Vehicle vehicle;
	private RoutingElement[] elements;
	private double load;
	private double distance;
	
	public Vehicle getVehicle() {
		return vehicle;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	public double getLoad() {
		return load;
	}

	public void setLoad(double load) {
		this.load = load;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

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

	public RoutingSolution(Vehicle vehicle, RoutingElement[] elements,
			double load, double distance) {
		super();
		this.vehicle = vehicle;
		this.elements = elements;
		this.load = load;
		this.distance = distance;
	}

	public RoutingSolution() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
