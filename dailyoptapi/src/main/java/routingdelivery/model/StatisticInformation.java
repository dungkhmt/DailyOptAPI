package routingdelivery.model;

import routingdelivery.smartlog.brenntag.model.SolutionIndicator;

public class StatisticInformation {
	private SolutionIndicator indicator;
	
	private double totalDistance;
	private int numberTrucks;
	private int numberInternalTrucks;
	private int numberExternalTrucks;
	private int numberTrips;
	
	private String executionTime;
	private String timeLimitExpired;
	
	private StatisticRoute[] routeInfo;
	
	
	
	public StatisticInformation(SolutionIndicator indicator,
			double totalDistance, int numberTrucks, int numberInternalTrucks,
			int numberExternalTrucks, int numberTrips, String executionTime,
			String timeLimitExpired, StatisticRoute[] routeInfo) {
		super();
		this.indicator = indicator;
		this.totalDistance = totalDistance;
		this.numberTrucks = numberTrucks;
		this.numberInternalTrucks = numberInternalTrucks;
		this.numberExternalTrucks = numberExternalTrucks;
		this.numberTrips = numberTrips;
		this.executionTime = executionTime;
		this.timeLimitExpired = timeLimitExpired;
		this.routeInfo = routeInfo;
	}

	public SolutionIndicator getIndicator() {
		return indicator;
	}

	public void setIndicator(SolutionIndicator indicator) {
		this.indicator = indicator;
	}

	public StatisticInformation(double totalDistance, int numberTrucks,
			int numberInternalTrucks, int numberExternalTrucks,
			int numberTrips, String executionTime, String timeLimitExpired,
			StatisticRoute[] routeInfo) {
		super();
		this.totalDistance = totalDistance;
		this.numberTrucks = numberTrucks;
		this.numberInternalTrucks = numberInternalTrucks;
		this.numberExternalTrucks = numberExternalTrucks;
		this.numberTrips = numberTrips;
		this.executionTime = executionTime;
		this.timeLimitExpired = timeLimitExpired;
		this.routeInfo = routeInfo;
	}

	public int getNumberTrips() {
		return numberTrips;
	}

	public void setNumberTrips(int numberTrips) {
		this.numberTrips = numberTrips;
	}

	public StatisticInformation(double totalDistance, int numberTrucks,
			int numberInternalTrucks, int numberExternalTrucks,
			String executionTime, String timeLimitExpired,
			StatisticRoute[] routeInfo) {
		super();
		this.totalDistance = totalDistance;
		this.numberTrucks = numberTrucks;
		this.numberInternalTrucks = numberInternalTrucks;
		this.numberExternalTrucks = numberExternalTrucks;
		this.executionTime = executionTime;
		this.timeLimitExpired = timeLimitExpired;
		this.routeInfo = routeInfo;
	}

	public int getNumberInternalTrucks() {
		return numberInternalTrucks;
	}

	public void setNumberInternalTrucks(int numberInternalTrucks) {
		this.numberInternalTrucks = numberInternalTrucks;
	}

	public int getNumberExternalTrucks() {
		return numberExternalTrucks;
	}

	public void setNumberExternalTrucks(int numberExternalTrucks) {
		this.numberExternalTrucks = numberExternalTrucks;
	}

	public StatisticInformation(double totalDistance, int numberTrucks,
			String executionTime, String timeLimitExpired,
			StatisticRoute[] routeInfo) {
		super();
		this.totalDistance = totalDistance;
		this.numberTrucks = numberTrucks;
		this.executionTime = executionTime;
		this.timeLimitExpired = timeLimitExpired;
		this.routeInfo = routeInfo;
	}

	public String getTimeLimitExpired() {
		return timeLimitExpired;
	}

	public void setTimeLimitExpired(String timeLimitExpired) {
		this.timeLimitExpired = timeLimitExpired;
	}

	public StatisticInformation(double totalDistance, int numberTrucks,
			String executionTime, StatisticRoute[] routeInfo) {
		super();
		this.totalDistance = totalDistance;
		this.numberTrucks = numberTrucks;
		this.executionTime = executionTime;
		this.routeInfo = routeInfo;
	}

	public String getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(String executionTime) {
		this.executionTime = executionTime;
	}

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
