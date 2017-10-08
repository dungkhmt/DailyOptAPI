package routingdelivery.model;

public class ConfigParams {
	private double minOccupyPad = 0.7;

	public double getMinOccupyPad() {
		return minOccupyPad;
	}

	public void setMinOccupyPad(double minOccupyPad) {
		this.minOccupyPad = minOccupyPad;
	}

	public ConfigParams(double minOccupyPad) {
		super();
		this.minOccupyPad = minOccupyPad;
	}

	public ConfigParams() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
