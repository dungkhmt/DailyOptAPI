package routingdelivery.smartlog.containertruckmoocassigment.model;

public class ExportContainerTruckMoocRequest {
	private String orderID;
	private String orderCode;
	private ExportContainerRequest[] containerRequest;
	public ExportContainerTruckMoocRequest(String orderID, String orderCode,
			ExportContainerRequest[] containerRequest) {
		super();
		this.orderID = orderID;
		this.orderCode = orderCode;
		this.containerRequest = containerRequest;
	}
	
}
