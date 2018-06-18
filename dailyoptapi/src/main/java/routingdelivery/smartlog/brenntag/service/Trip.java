package routingdelivery.smartlog.brenntag.service;

public class Trip {
	public RouteNode start;
	public RouteNode end;
	public Trip(RouteNode start, RouteNode end) {
		super();
		this.start = start;
		this.end = end;
	}
	public String toString(){
		return "startNode " + start.toString() + "\nendNode" + end.toString();
	}
	
}
