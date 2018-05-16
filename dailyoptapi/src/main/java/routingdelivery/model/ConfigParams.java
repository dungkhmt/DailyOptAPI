package routingdelivery.model;

public class ConfigParams {
	private double minOccupyPad = 0.7;
	//private int averageSpeed = 16;// 60km/h = 16 m/s
	private int averageSpeed = 11;// 40km/h = 11 m/s
	
	public ConfigParams(double minOccupyPad, int averageSpeed) {
		super();
		this.minOccupyPad = minOccupyPad;
		this.averageSpeed = averageSpeed;
	}

	public int getAverageSpeed() {
		return averageSpeed;
	}

	public void setAverageSpeed(int averageSpeed) {
		this.averageSpeed = averageSpeed;
	}

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
