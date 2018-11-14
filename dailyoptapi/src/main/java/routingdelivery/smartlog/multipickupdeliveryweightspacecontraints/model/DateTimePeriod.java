package routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model;

public class DateTimePeriod {
	private String start;
	private String end;
	public String getStart() {
		return start;
	}
	public void setStart(String start) {
		this.start = start;
	}
	public String getEnd() {
		return end;
	}
	public void setEnd(String end) {
		this.end = end;
	}
	public DateTimePeriod(String start, String end) {
		super();
		this.start = start;
		this.end = end;
	}
	public DateTimePeriod() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
