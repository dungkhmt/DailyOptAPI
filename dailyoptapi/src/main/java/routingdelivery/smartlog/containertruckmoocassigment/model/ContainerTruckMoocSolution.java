package routingdelivery.smartlog.containertruckmoocassigment.model;

public class ContainerTruckMoocSolution {
	private TruckRouteList[] truckRoutes;
	private ExportContainerRequest[] unScheduledExportRequests;
	private ImportContainerRequest[] unScheduledImportRequests;
	private WarehouseContainerTransportRequest[] unScheduledWarehouseRequests;
	
	private StatisticInformation statisticInformation;
	private String description;

	
	

	public ContainerTruckMoocSolution(TruckRouteList[] truckRoutes,
			ExportContainerRequest[] unScheduledExportRequests,
			ImportContainerRequest[] unScheduledImportRequests,
			WarehouseContainerTransportRequest[] unScheduledWarehouseRequests,
			StatisticInformation statisticInformation, String description) {
		super();
		this.truckRoutes = truckRoutes;
		this.unScheduledExportRequests = unScheduledExportRequests;
		this.unScheduledImportRequests = unScheduledImportRequests;
		this.unScheduledWarehouseRequests = unScheduledWarehouseRequests;
		this.statisticInformation = statisticInformation;
		this.description = description;
	}




	public TruckRouteList[] getTruckRoutes() {
		return truckRoutes;
	}




	public void setTruckRoutes(TruckRouteList[] truckRoutes) {
		this.truckRoutes = truckRoutes;
	}




	public ExportContainerRequest[] getUnScheduledExportRequests() {
		return unScheduledExportRequests;
	}




	public void setUnScheduledExportRequests(
			ExportContainerRequest[] unScheduledExportRequests) {
		this.unScheduledExportRequests = unScheduledExportRequests;
	}




	public ImportContainerRequest[] getUnScheduledImportRequests() {
		return unScheduledImportRequests;
	}




	public void setUnScheduledImportRequests(
			ImportContainerRequest[] unScheduledImportRequests) {
		this.unScheduledImportRequests = unScheduledImportRequests;
	}




	public WarehouseContainerTransportRequest[] getUnScheduledWarehouseRequests() {
		return unScheduledWarehouseRequests;
	}




	public void setUnScheduledWarehouseRequests(
			WarehouseContainerTransportRequest[] unScheduledWarehouseRequests) {
		this.unScheduledWarehouseRequests = unScheduledWarehouseRequests;
	}




	public StatisticInformation getStatisticInformation() {
		return statisticInformation;
	}




	public void setStatisticInformation(StatisticInformation statisticInformation) {
		this.statisticInformation = statisticInformation;
	}




	public String getDescription() {
		return description;
	}




	public void setDescription(String description) {
		this.description = description;
	}




	public ContainerTruckMoocSolution() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
