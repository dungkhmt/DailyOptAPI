package routingdelivery.smartlog.containertruckmoocassigment.model;

public class Mooc {
	private String code;
	private String category;// 20, 40, 45
	private double weight;//20, 40, 45
	private String status;
	private String depotMoocCode;
	private String[] returnDepotCodes;// possible depots when finishing services
	private Intervals[] intervals;
	
	public Mooc(String code, String category, double weight, String status,
			String depotMoocCode, String[] returnDepotCodes, Intervals[] intervals) {
		super();
		this.code = code;
		this.category = category;
		this.weight = weight;
		this.status = status;
		this.depotMoocCode = depotMoocCode;
		this.returnDepotCodes = returnDepotCodes;
		this.intervals = intervals;
	}
	public String[] getReturnDepotCodes() {
		return returnDepotCodes;
	}
	public void setReturnDepotCodes(String[] returnDepotCodes) {
		this.returnDepotCodes = returnDepotCodes;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getDepotMoocCode() {
		return depotMoocCode;
	}
	public void setDepotMoocCode(String depotMoocCode) {
		this.depotMoocCode = depotMoocCode;
	}
	
	public Intervals[] getIntervals() {
		return intervals;
	}
	public void setIntervals(Intervals[] intervals) {
		this.intervals = intervals;
	}
	
	public Mooc() {
		super();
		// TODO Auto-generated constructor stub
	}
		
}
