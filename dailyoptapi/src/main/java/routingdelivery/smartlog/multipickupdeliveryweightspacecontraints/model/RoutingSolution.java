package routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model;

import java.util.ArrayList;

public class RoutingSolution {
	private Vehicle vehicle;
	private RoutingElement[] elements;
	private double load;
	private double distance;
	
	public double computeMaxLoad(){
		double maxLoad = 0;
		if(elements!=null){
			for(int i = 0; i < elements.length; i++){
				if(elements[i].getLoad() > maxLoad) maxLoad = elements[i].getLoad();
			}
		}
		return maxLoad;
	}
	public void insertHead(ArrayList<RoutingElement> RE){
		ArrayList<RoutingElement> tmp = new ArrayList<RoutingElement>();
		double baseDistance = 0;
		if(RE.size() > 0)
			baseDistance = RE.get(RE.size()-1).getDistance();
		
		for(int i = 0; i < RE.size(); i++){
			tmp.add(RE.get(i));
			
		}
		for(int i = 1; i < elements.length; i++){// start from 1 -> ignore the first (start element route which is the depot)
			tmp.add(elements[i]);
			elements[i].setDistance(baseDistance + elements[i].getDistance());
		}
		
		elements = new RoutingElement[tmp.size()];
		for(int i = 0; i < tmp.size(); i++)
			elements[i] = tmp.get(i);
	
		distance += baseDistance;
	}
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
