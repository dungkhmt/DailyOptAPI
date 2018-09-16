package routingdelivery.model;

public class StatisticInformation {
	private double totalDistance;
	private int numberTrucks;
	private StatisticRoute[] routeInfo;
	
	
	public StatisticInformation(double totalDistance, int numberTrucks,
			StatisticRoute[] routeInfo) {
		super();
		this.totalDistance = totalDistance;
		this.numberTrucks = numberTrucks;
		this.routeInfo = routeInfo;
	}

	public StatisticRoute[] getRouteInfo() {
		return routeInfo;
	}

	public void setRouteInfo(StatisticRoute[] routeInfo) {
		this.routeInfo = routeInfo;
	}

	public StatisticInformation(double totalDistance, int numberTrucks) {
		super();
		this.totalDistance = totalDistance;
		this.numberTrucks = numberTrucks;
	}

	public int getNumberTrucks() {
		return numberTrucks;
	}

	public void setNumberTrucks(int numberTrucks) {
		this.numberTrucks = numberTrucks;
	}

	public double getTotalDistance() {
		return totalDistance;
	}

	public void setTotalDistance(double totalDistance) {
		this.totalDistance = totalDistance;
	}

	public StatisticInformation(double totalDistance) {
		super();
		this.totalDistance = totalDistance;
	}

	public StatisticInformation() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
