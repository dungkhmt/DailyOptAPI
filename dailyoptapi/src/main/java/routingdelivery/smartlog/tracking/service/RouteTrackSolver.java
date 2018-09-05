package routingdelivery.smartlog.tracking.service;

import java.util.ArrayList;

import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.GoogleMapsQuery;
import routingdelivery.smartlog.tracking.model.GPoint;
import routingdelivery.smartlog.tracking.model.RouteTrackInput;
import routingdelivery.smartlog.tracking.model.RouteTrackSolution;

public class RouteTrackSolver {
	private RouteTrackInput input;
	public static final double EPS = 0.000001;

	public boolean equal(double a, double b) {
		return Math.abs(a - b) < EPS;
	}

	public boolean identical(GPoint P1, GPoint P2) {
		return equal(P1.getLat(), P2.getLat())
				&& equal(P1.getLng(), P2.getLng());
	}

	public double distance(GPoint P1, GPoint P2) {
		// return Math.sqrt((P1.getX() - P2.getX())*(P1.getX()-P2.getX()) +
		// (P1.getY() - P2.getY())*(P1.getY()-P2.getY()));
		GoogleMapsQuery G = new GoogleMapsQuery();
		return G.computeDistanceHaversine(P1.getLat(), P1.getLng(),
				P2.getLat(), P2.getLng()) * 1000; // in meters
	}

	public double distance(GPoint P0, GPoint P1, GPoint P2) {
		if (identical(P1, P2))
			return distance(P0, P1);
		double x1 = P1.getLat();
		double y1 = P1.getLng();
		double x2 = P2.getLat();
		double y2 = P2.getLng();
		double x0 = P0.getLat();
		double y0 = P0.getLng();
		double l12 = distance(P1, P2);
		double l01 = distance(P0, P1);
		double l02 = distance(P0, P2);
		double p = (l12 + l01 + l02) / 2;
		double area = Math.sqrt(p * (p - l12) * (p - l01) * (p - l02));
		double h = area / l12;
		//System.out.println("l12 = " + l12 + ", l01 = " + l01 + ", l02 = " + l02
		//		+ ", h = " + h);
		double ll12 = l12 * l12;
		double ll01 = l01 * l01;
		double ll02 = l02 * l02;
		// double h = Math.abs((y2-y1)*x0-(x2-x1)*y0+x2*y1-y2*x1)*1.0/(l12);
		if (ll01 > ll02 + ll12)
			return l02;
		if (ll02 > ll01 + ll12)
			return l01;
		return h;
	}

	public RouteTrackSolution evaluate(RouteTrackInput input) {
		this.input = input;
		boolean ok = true;
		GPoint[] P = input.getRoutePoints();
		GPoint[] P0 = input.getTruckPoints();
		ArrayList<Integer> L = new ArrayList<Integer>();

		if (P != null && P.length >= 2 && P0 != null && P0.length > 0) {
			for (int j = 0; j < P0.length; j++) {
				double minDistance = Integer.MAX_VALUE;
				for (int i = 0; i < P.length - 1; i++) {

					GPoint P1 = P[i];
					GPoint P2 = P[i + 1];
					double d = distance(P0[j], P1, P2);
					if (minDistance > d)
						minDistance = d;
				}
				if (minDistance > input.getThreshold()) {
					L.add(j);
				}
			}
		}
		int[] outTrackPointIndices = new int[L.size()];
		for(int i = 0; i < L.size(); i++){
			outTrackPointIndices[i] = L.get(i);
		}
		String test = "OK";
		if (L.size() > 0)
			test = "KO";
		return new RouteTrackSolution(outTrackPointIndices,test);
	}

	public static void main(String[] args) {
		GPoint P0 = new GPoint(0, 0);
		GPoint P1 = new GPoint(0, 2);
		GPoint P2 = new GPoint(2, 0);
		RouteTrackSolver s = new RouteTrackSolver();
		System.out.println(s.distance(P0, P1, P2));

	}
}
