package routingdelivery.model;

public class ConfigParams {
	private String intCity;
	private String internalVehicleFirst; 
	private int extendLateDelivery;// thoi gian toi da co the noi rong them cho thoi gian muon nhat giao hang
	private int extendCapacity;// tai trong toi da co the noi rong them
	private int timeLimit;// in minute
	
	

	public ConfigParams(String intCity, String internalVehicleFirst,
			int extendLateDelivery, int extendCapacity, int timeLimit) {
		super();
		this.intCity = intCity;
		this.internalVehicleFirst = internalVehicleFirst;
		this.extendLateDelivery = extendLateDelivery;
		this.extendCapacity = extendCapacity;
		this.timeLimit = timeLimit;
	}

	public int getExtendCapacity() {
		return extendCapacity;
	}

	public void setExtendCapacity(int extendCapacity) {
		this.extendCapacity = extendCapacity;
	}

	public ConfigParams(String intCity, String internalVehicleFirst,
			int extendLateDelivery, int timeLimit) {
		super();
		this.intCity = intCity;
		this.internalVehicleFirst = internalVehicleFirst;
		this.extendLateDelivery = extendLateDelivery;
		this.timeLimit = timeLimit;
	}

	public int getExtendLateDelivery() {
		return extendLateDelivery;
	}

	public void setExtendLateDelivery(int extendLateDelivery) {
		this.extendLateDelivery = extendLateDelivery;
	}

	public ConfigParams(String intCity, String internalVehicleFirst,
			int timeLimit) {
		super();
		this.intCity = intCity;
		this.internalVehicleFirst = internalVehicleFirst;
		this.timeLimit = timeLimit;
	}

	public int getTimeLimit() {
		return timeLimit;
	}

	public void setTimeLimit(int timeLimit) {
		this.timeLimit = timeLimit;
	}

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
