package routingdelivery.smartlog.containertruckmoocassigment.model;

public class ContainerTruckMoocSolution {
	private TruckRoute[] truckRoutes;
	
	private String description;

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
