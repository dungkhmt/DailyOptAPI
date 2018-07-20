package routingdelivery.smartlog.containertruckmoocassigment.model;

public class WarehouseContainerTransportRequest {
	private String itemOrderID;
	
	private String containerCategory;
	private double weight;
	
	private String fromWarehouseCode;
	private String earlyDateTimeLoad;
	private String lateDateTimeLoad;
	private int loadDuration;
	
	private String toWarehouseCode;
	private String earlyDateTimeUnload;
	private String lateDateTimeUnload;
	private int unloadDuration;
	public String getItemOrderID() {
		return itemOrderID;
	}
	public void setItemOrderID(String itemOrderID) {
		this.itemOrderID = itemOrderID;
	}
	public String getContainerCategory() {
		return containerCategory;
	}
	public void setContainerCategory(String containerCategory) {
		this.containerCategory = containerCategory;
	}
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	public String getFromWarehouseCode() {
		return fromWarehouseCode;
	}
	public void setFromWarehouseCode(String fromWarehouseCode) {
		this.fromWarehouseCode = fromWarehouseCode;
	}
	public String getEarlyDateTimeLoad() {
		return earlyDateTimeLoad;
	}
	public void setEarlyDateTimeLoad(String earlyDateTimeLoad) {
		this.earlyDateTimeLoad = earlyDateTimeLoad;
	}
	public String getLateDateTimeLoad() {
		return lateDateTimeLoad;
	}
	public void setLateDateTimeLoad(String lateDateTimeLoad) {
		this.lateDateTimeLoad = lateDateTimeLoad;
	}
	public int getLoadDuration() {
		return loadDuration;
	}
	public void setLoadDuration(int loadDuration) {
		this.loadDuration = loadDuration;
	}
	public String getToWarehouseCode() {
		return toWarehouseCode;
	}
	public void setToWarehouseCode(String toWarehouseCode) {
		this.toWarehouseCode = toWarehouseCode;
	}
	public String getEarlyDateTimeUnload() {
		return earlyDateTimeUnload;
	}
	public void setEarlyDateTimeUnload(String earlyDateTimeUnload) {
		this.earlyDateTimeUnload = earlyDateTimeUnload;
	}
	public String getLateDateTimeUnload() {
		return lateDateTimeUnload;
	}
	public void setLateDateTimeUnload(String lateDateTimeUnload) {
		this.lateDateTimeUnload = lateDateTimeUnload;
	}
	public int getUnloadDuration() {
		return unloadDuration;
	}
	public void setUnloadDuration(int unloadDuration) {
		this.unloadDuration = unloadDuration;
	}
	public WarehouseContainerTransportRequest(String itemOrderID,
			String containerCategory, double weight, String fromWarehouseCode,
			String earlyDateTimeLoad, String lateDateTimeLoad,
			int loadDuration, String toWarehouseCode,
			String earlyDateTimeUnload, String lateDateTimeUnload,
			int unloadDuration) {
		super();
		this.itemOrderID = itemOrderID;
		this.containerCategory = containerCategory;
		this.weight = weight;
		this.fromWarehouseCode = fromWarehouseCode;
		this.earlyDateTimeLoad = earlyDateTimeLoad;
		this.lateDateTimeLoad = lateDateTimeLoad;
		this.loadDuration = loadDuration;
		this.toWarehouseCode = toWarehouseCode;
		this.earlyDateTimeUnload = earlyDateTimeUnload;
		this.lateDateTimeUnload = lateDateTimeUnload;
		this.unloadDuration = unloadDuration;
	}
	public WarehouseContainerTransportRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	
}
