package routingdelivery.smartlog.containertruckmoocassigment.model;

public class StatisticInformation {
	private double totalDistance;
	private int numberTrucks;
	
	
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
