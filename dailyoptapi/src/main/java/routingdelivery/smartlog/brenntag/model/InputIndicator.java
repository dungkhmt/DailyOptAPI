package routingdelivery.smartlog.brenntag.model;

public class InputIndicator {
	private double shortestDistance;
	private double longestDistance;
	private double shortestTravelTime;
	private double longestTravelTime;
	private double minInternalTruckCapacity;
	private double maxInternalTruckCapacity;
	private double minExternalTruckCapacity;
	private double maxExternalTruckCapacity;
	private int nbIntCityLocations;
	private int nbExtCityLocations;
	private double totalItemWeights;
	

	
	public InputIndicator(double shortestDistance, double longestDistance,
			double shortestTravelTime, double longestTravelTime,
			double minInternalTruckCapacity, double maxInternalTruckCapacity,
			double minExternalTruckCapacity, double maxExternalTruckCapacity,
			int nbIntCityLocations, int nbExtCityLocations,
			double totalItemWeights) {
		super();
		this.shortestDistance = shortestDistance;
		this.longestDistance = longestDistance;
		this.shortestTravelTime = shortestTravelTime;
		this.longestTravelTime = longestTravelTime;
		this.minInternalTruckCapacity = minInternalTruckCapacity;
		this.maxInternalTruckCapacity = maxInternalTruckCapacity;
		this.minExternalTruckCapacity = minExternalTruckCapacity;
		this.maxExternalTruckCapacity = maxExternalTruckCapacity;
		this.nbIntCityLocations = nbIntCityLocations;
		this.nbExtCityLocations = nbExtCityLocations;
		this.totalItemWeights = totalItemWeights;
	}



	public double getShortestDistance() {
		return shortestDistance;
	}



	public void setShortestDistance(double shortestDistance) {
		this.shortestDistance = shortestDistance;
	}



	public double getLongestDistance() {
		return longestDistance;
	}



	public void setLongestDistance(double longestDistance) {
		this.longestDistance = longestDistance;
	}



	public double getShortestTravelTime() {
		return shortestTravelTime;
	}



	public void setShortestTravelTime(double shortestTravelTime) {
		this.shortestTravelTime = shortestTravelTime;
	}



	public double getLongestTravelTime() {
		return longestTravelTime;
	}



	public void setLongestTravelTime(double longestTravelTime) {
		this.longestTravelTime = longestTravelTime;
	}



	public double getMinInternalTruckCapacity() {
		return minInternalTruckCapacity;
	}



	public void setMinInternalTruckCapacity(double minInternalTruckCapacity) {
		this.minInternalTruckCapacity = minInternalTruckCapacity;
	}



	public double getMaxInternalTruckCapacity() {
		return maxInternalTruckCapacity;
	}



	public void setMaxInternalTruckCapacity(double maxInternalTruckCapacity) {
		this.maxInternalTruckCapacity = maxInternalTruckCapacity;
	}



	public double getMinExternalTruckCapacity() {
		return minExternalTruckCapacity;
	}



	public void setMinExternalTruckCapacity(double minExternalTruckCapacity) {
		this.minExternalTruckCapacity = minExternalTruckCapacity;
	}



	public double getMaxExternalTruckCapacity() {
		return maxExternalTruckCapacity;
	}



	public void setMaxExternalTruckCapacity(double maxExternalTruckCapacity) {
		this.maxExternalTruckCapacity = maxExternalTruckCapacity;
	}



	public int getNbIntCityLocations() {
		return nbIntCityLocations;
	}



	public void setNbIntCityLocations(int nbIntCityLocations) {
		this.nbIntCityLocations = nbIntCityLocations;
	}



	public int getNbExtCityLocations() {
		return nbExtCityLocations;
	}



	public void setNbExtCityLocations(int nbExtCityLocations) {
		this.nbExtCityLocations = nbExtCityLocations;
	}



	public double getTotalItemWeights() {
		return totalItemWeights;
	}



	public void setTotalItemWeights(double totalItemWeights) {
		this.totalItemWeights = totalItemWeights;
	}



	public InputIndicator() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
