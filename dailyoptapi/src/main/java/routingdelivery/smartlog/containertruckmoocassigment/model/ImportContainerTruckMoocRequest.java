package routingdelivery.smartlog.containertruckmoocassigment.model;

public class ImportContainerTruckMoocRequest {
	private String orderID;
	private String orderCode;
	private ImportContainerRequest[] containerRequest;
	public ImportContainerTruckMoocRequest(String orderID, String orderCode,
			ImportContainerRequest[] containerRequest) {
		super();
		this.orderID = orderID;
		this.orderCode = orderCode;
		this.containerRequest = containerRequest;
	}

	
}
