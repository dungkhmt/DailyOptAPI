package routingdelivery.model;

public class StatisticRoute {
	private String truckCode;
	private double distance;
	private double truckCapacity;
	private double load;
	private int nbPoints;
	private StatisticTrip[] trips;
	public String getTruckCode() {
		return truckCode;
	}
	public void setTruckCode(String truckCode) {
		this.truckCode = truckCode;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public double getTruckCapacity() {
		return truckCapacity;
	}
	public void setTruckCapacity(double truckCapacity) {
		this.truckCapacity = truckCapacity;
	}
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
	public StatisticTrip[] getTrips() {
		return trips;
	}
	public void setTrips(StatisticTrip[] trips) {
		this.trips = trips;
	}
	public StatisticRoute(String truckCode, double distance,
			double truckCapacity, double load, int nbPoints,
			StatisticTrip[] trips) {
		super();
		this.truckCode = truckCode;
		this.distance = distance;
		this.truckCapacity = truckCapacity;
		this.load = load;
		this.nbPoints = nbPoints;
		this.trips = trips;
	}
	public StatisticRoute() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	
}
