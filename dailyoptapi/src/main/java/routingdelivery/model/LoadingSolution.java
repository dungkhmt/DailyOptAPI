package routingdelivery.model;

public class LoadingSolution {
	private Vehicle vehicle;
	private LoadingElement[] loadElements;
	public Vehicle getVehicle() {
		return vehicle;
	}
	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}
	public LoadingElement[] getLoadElements() {
		return loadElements;
	}
	public void setLoadElements(LoadingElement[] loadElements) {
		this.loadElements = loadElements;
	}
	public LoadingSolution(Vehicle vehicle, LoadingElement[] loadElements) {
		super();
		this.vehicle = vehicle;
		this.loadElements = loadElements;
	}
	public LoadingSolution() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
