package routingdelivery.model;

public class ConfigParams {
	private String intCity;
	private String internalVehicleFirst; 
	
	
	public ConfigParams(String intCity, String internalVehicleFirst) {
		super();
		this.intCity = intCity;
		this.internalVehicleFirst = internalVehicleFirst;
	}

	public String getInternalVehicleFirst() {
		return internalVehicleFirst;
	}

	public void setInternalVehicleFirst(String internalVehicleFirst) {
		this.internalVehicleFirst = internalVehicleFirst;
	}

	public String getIntCity() {
		return intCity;
	}

	public void setIntCity(String intCity) {
		this.intCity = intCity;
	}

	public ConfigParams(String intCity) {
		super();
		this.intCity = intCity;
	}

	public ConfigParams() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
