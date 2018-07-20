package routingdelivery.smartlog.containertruckmoocassigment.model;

public class ShipCompany {
	private String code;
	private String[] containerDepotCodes;
	public ShipCompany(String code, String[] containerDepotCodes) {
		super();
		this.code = code;
		this.containerDepotCodes = containerDepotCodes;
	}
	
}
