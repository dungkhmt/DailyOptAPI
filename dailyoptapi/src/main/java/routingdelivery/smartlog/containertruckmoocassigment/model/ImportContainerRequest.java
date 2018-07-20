package routingdelivery.smartlog.containertruckmoocassigment.model;

public class ImportContainerRequest {
	private String orderItemID;
	
	private String shipCompanyCode;
	private String depotCode;
	private String containerCategory;// 20, 40, 45
	private double weight;
	private String portCode;
	private String earlyDateTimePickupAtPort;
	private String lateDateTimePickupAtPort;
	private int loadDuration = 3600;
	
	private String wareHouseCode;
	private String earlyDateTimeUnloadAtWarehouse;
	private String lateDateTimeUnloadAtWarehouse;
	private int unloadDuration = 3600;
	
	private String earlyDateTimeDeliveryAtDepot;
	private String lateDateTimeDeliveryAtDepot;
	public ImportContainerRequest(String orderItemID, String shipCompanyCode,
			String depotCode, String containerCategory, double weight,
			String portCode, String earlyDateTimePickupAtPort,
			String lateDateTimePickupAtPort, int loadDuration,
			String wareHouseCode, String earlyDateTimeUnloadAtWarehouse,
			String lateDateTimeUnloadAtWarehouse, int unloadDuration,
			String earlyDateTimeDeliveryAtDepot,
			String lateDateTimeDeliveryAtDepot) {
		super();
		this.orderItemID = orderItemID;
		this.shipCompanyCode = shipCompanyCode;
		this.depotCode = depotCode;
		this.containerCategory = containerCategory;
		this.weight = weight;
		this.portCode = portCode;
		this.earlyDateTimePickupAtPort = earlyDateTimePickupAtPort;
		this.lateDateTimePickupAtPort = lateDateTimePickupAtPort;
		this.loadDuration = loadDuration;
		this.wareHouseCode = wareHouseCode;
		this.earlyDateTimeUnloadAtWarehouse = earlyDateTimeUnloadAtWarehouse;
		this.lateDateTimeUnloadAtWarehouse = lateDateTimeUnloadAtWarehouse;
		this.unloadDuration = unloadDuration;
		this.earlyDateTimeDeliveryAtDepot = earlyDateTimeDeliveryAtDepot;
		this.lateDateTimeDeliveryAtDepot = lateDateTimeDeliveryAtDepot;
	}
	
	
}
