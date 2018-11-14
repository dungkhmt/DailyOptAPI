package routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model;

public class StatisticTrip {
	//private double distance;
	//private double truckCapacity;
	private double load;
	private int nbPoints;

	
	public double getLoad() {
		return load;
	}


	public void setLoad(double load) {
		this.load = load;
	}


	public int getNbPoints() {
		return nbPoints;
	}


	public void setNbPoints(int nbPoints) {
		this.nbPoints = nbPoints;
	}


	public StatisticTrip(double load, int nbPoints) {
		super();
		this.load = load;
		this.nbPoints = nbPoints;
	}


	public StatisticTrip() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
