package routingdelivery.smartlog.containertruckmoocassigment.model;

public class ContainerTruckMoocSolution {
	private TruckRoute[] truckRoutes;
	private ExportContainerRequest[] unScheduledExportRequests;
	private ImportContainerRequest[] unScheduledImportRequests;
	private WarehouseContainerTransportRequest[] unScheduledWarehouseRequests;
	
	private StatisticInformation statisticInformation;
	private String description;

	
	
	public ContainerTruckMoocSolution(TruckRoute[] truckRoutes,
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

	public ContainerTruckMoocSolution(TruckRoute[] truckRoutes,
			StatisticInformation statisticInformation, String description) {
		super();
		this.truckRoutes = truckRoutes;
		this.statisticInformation = statisticInformation;
		this.description = description;
	}

	public StatisticInformation getStatisticInformation() {
		return statisticInformation;
	}

	public void setStatisticInformation(StatisticInformation statisticInformation) {
		this.statisticInformation = statisticInformation;
	}

	public TruckRoute[] getTruckRoutes() {
		return truckRoutes;
	}

	public void setTruckRoutes(TruckRoute[] truckRoutes) {
		this.truckRoutes = truckRoutes;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ContainerTruckMoocSolution(TruckRoute[] truckRoutes,
			String description) {
		super();
		this.truckRoutes = truckRoutes;
		this.description = description;
	}

	public ContainerTruckMoocSolution() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
