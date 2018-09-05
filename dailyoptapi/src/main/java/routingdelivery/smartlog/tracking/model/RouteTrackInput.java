package routingdelivery.smartlog.tracking.model;

import com.google.gson.Gson;



public class RouteTrackInput {
	private GPoint[] routePoints;
	private GPoint[] truckPoints;
	private double threshold;

	


	public RouteTrackInput() {
		super();
		// TODO Auto-generated constructor stub
	}




	public RouteTrackInput(GPoint[] routePoints, GPoint[] truckPoints,
			double threshold) {
		super();
		this.routePoints = routePoints;
		this.truckPoints = truckPoints;
		this.threshold = threshold;
	}




	public GPoint[] getRoutePoints() {
		return routePoints;
	}




	public void setRoutePoints(GPoint[] routePoints) {
		this.routePoints = routePoints;
	}




	public GPoint[] getTruckPoints() {
		return truckPoints;
	}




	public void setTruckPoints(GPoint[] truckPoints) {
		this.truckPoints = truckPoints;
	}




	public double getThreshold() {
		return threshold;
	}




	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}




	public static void main(String args[]){
		GPoint[] P = new GPoint[5];
		P[0] = new GPoint(21,105);
		P[1] = new GPoint(21.1232, 105.542);
		P[2] = new GPoint(21.6535,105.4433);
		P[3] = new GPoint(21.111, 105.3333);
		P[4] = new GPoint(21.432, 105.6134);
		GPoint[] P0 = new GPoint[4];
		P0[0] = new GPoint((P[0].getLat() + P[1].getLat())/2,(P[0].getLng() + P[1].getLng())/2);
		P0[1] = new GPoint(10,10);
		P0[2] = new GPoint((P[3].getLat() + P[4].getLat())/2,(P[3].getLng() + P[4].getLng())/2);
		P0[3] = new GPoint(105,105);
		double threshold = 500;
		RouteTrackInput input = new RouteTrackInput(P, P0, threshold);
		Gson gson = new Gson();
		String json = gson.toJson(input);
		System.out.println(json);
	}
}
