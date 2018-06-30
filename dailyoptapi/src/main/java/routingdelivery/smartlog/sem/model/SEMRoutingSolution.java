package routingdelivery.smartlog.sem.model;

import routingdelivery.model.RoutingElement;



public class SEMRoutingSolution {
	private SEMShipper shipper;
	private RoutingElement[] elements;
	private int nbOrder;
	private double money;
	private double weight;
	private double distance;
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
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public SEMRoutingSolution(SEMShipper shipper, RoutingElement[] elements,
			int nbOrder, double money, double weight, double distance) {
		super();
		this.shipper = shipper;
		this.elements = elements;
		this.nbOrder = nbOrder;
		this.money = money;
		this.weight = weight;
		this.distance = distance;
	}
	public SEMRoutingSolution() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
