package routingdelivery.smartlog.brenntag.model;

import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;

public class MoveMergeTrip {
	public Point pickup;
	public Point delivery;
	public VehicleTrip trip;
	public Point sel_p;
	public Point sel_d;
	public double eval;
	public MoveMergeTrip(Point pickup, Point delivery, VehicleTrip trip,
			Point sel_p, Point sel_d, double eval) {
		super();
		this.pickup = pickup;
		this.delivery = delivery;
		this.trip = trip;
		this.sel_p = sel_p;
		this.sel_d = sel_d;
		this.eval = eval;
	}
	
	
}
