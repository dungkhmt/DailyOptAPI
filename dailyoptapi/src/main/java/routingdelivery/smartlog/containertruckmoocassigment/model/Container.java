package routingdelivery.smartlog.containertruckmoocassigment.model;

public class Container {
	private String code;
	private int weight;
	private String categoryCode;
	private String depotContainerCode;
	public Container(String code, int weight, String categoryCode,
			String depotContainerCode) {
		super();
		this.code = code;
		this.weight = weight;
		this.categoryCode = categoryCode;
		this.depotContainerCode = depotContainerCode;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
	public String getCategoryCode() {
		return categoryCode;
	}
	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}
	public String getDepotContainerCode() {
		return depotContainerCode;
	}
	public void setDepotContainerCode(String depotContainerCode) {
		this.depotContainerCode = depotContainerCode;
	}
	
}
