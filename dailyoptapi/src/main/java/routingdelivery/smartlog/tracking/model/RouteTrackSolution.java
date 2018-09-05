package routingdelivery.smartlog.tracking.model;

public class RouteTrackSolution {
	private int[] outTrackPointIndices;
	private String description;
	public int[] getOutTrackPointIndices() {
		return outTrackPointIndices;
	}
	public void setOutTrackPointIndices(int[] outTrackPointIndices) {
		this.outTrackPointIndices = outTrackPointIndices;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public RouteTrackSolution(int[] outTrackPointIndices, String description) {
		super();
		this.outTrackPointIndices = outTrackPointIndices;
		this.description = description;
	}
	public RouteTrackSolution() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
