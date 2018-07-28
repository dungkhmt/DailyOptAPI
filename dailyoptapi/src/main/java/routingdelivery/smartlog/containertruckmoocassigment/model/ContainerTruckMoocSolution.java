package routingdelivery.smartlog.containertruckmoocassigment.model;

public class ContainerTruckMoocSolution {
	private TruckRoute[] truckRoutes;
	private StatisticInformation statisticInformation;
	private String description;

	
	
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