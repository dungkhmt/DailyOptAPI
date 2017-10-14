package havestplanning.model;

public class ReturnMachineSetting {
	private String description;
	private MachineSetting machineSetting;
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public MachineSetting getMachineSetting() {
		return machineSetting;
	}
	public void setMachineSetting(MachineSetting machineSetting) {
		this.machineSetting = machineSetting;
	}
	public ReturnMachineSetting(String description,
			MachineSetting machineSetting) {
		super();
		this.description = description;
		this.machineSetting = machineSetting;
	}
	public ReturnMachineSetting() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
