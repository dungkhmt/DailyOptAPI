package routingdelivery.smartlog.sem.model;

import routingdelivery.model.RoutingElement;



public class SEMRoutingSolution {
	private SEMShipper shipper;
	private RoutingElement[] elements;
	private int nbOrder;
	private double money;
	private double maxWeight;
	private double totalWeight;
	private double distance;
	
	public SEMRoutingSolution(SEMShipper shipper, RoutingElement[] elements,
			int nbOrder, double money, double maxWeight, double totalWeight,
			double distance) {
		super();
		this.shipper = shipper;
		this.elements = elements;
		this.nbOrder = nbOrder;
		this.money = money;
		this.maxWeight = maxWeight;
		this.totalWeight = totalWeight;
		this.distance = distance;
	}

	public SEMShipper getShipper() {
		return shipper;
	}

	public void setShipper(SEMShipper shipper) {
		this.shipper = shipper;
	}

	public RoutingElement[] getElements() {
		return elements;
	}

	public void setElements(RoutingElement[] elements) {
		this.elements = elements;
	}

	public int getNbOrder() {
		return nbOrder;
	}

	public void setNbOrder(int nbOrder) {
		this.nbOrder = nbOrder;
	}

	public double getMoney() {
		return money;
	}

	public void setMoney(double money) {
		this.money = money;
	}

	public double getMaxWeight() {
		return maxWeight;
	}

	public void setMaxWeight(double maxWeight) {
		this.maxWeight = maxWeight;
	}

	public double getTotalWeight() {
		return totalWeight;
	}

	public void setTotalWeight(double totalWeight) {
		this.totalWeight = totalWeight;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public SEMRoutingSolution() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
