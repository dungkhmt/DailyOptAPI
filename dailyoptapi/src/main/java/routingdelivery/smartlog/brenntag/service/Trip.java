package routingdelivery.smartlog.brenntag.service;

public class Trip {
	public RouteNode start;
	public RouteNode end;
	public String type;
	
	public Trip(RouteNode start, RouteNode end, String type) {
		super();
		this.start = start;
		this.end = end;
		this.type = type;
	}
	public String toString(){
		return "type = " + type + ", startNode " + start.toString() + "\nendNode" + end.toString();
	}
	
}
