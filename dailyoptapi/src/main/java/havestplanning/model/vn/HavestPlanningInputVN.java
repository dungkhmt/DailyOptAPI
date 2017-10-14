package havestplanning.model.vn;

public class HavestPlanningInputVN {
	private FieldVN[] fields;
	private PlantStandardVN plantStandard;
	private MachineSettingVN machineSetting;
	public FieldVN[] getFields() {
		return fields;
	}
	public void setFields(FieldVN[] fields) {
		this.fields = fields;
	}
	public PlantStandardVN getPlantStandard() {
		return plantStandard;
	}
	public void setPlantStandard(PlantStandardVN plantStandard) {
		this.plantStandard = plantStandard;
	}
	public MachineSettingVN getMachineSetting() {
		return machineSetting;
	}
	public void setMachineSetting(MachineSettingVN machineSetting) {
		this.machineSetting = machineSetting;
	}
	public HavestPlanningInputVN(FieldVN[] fields,
			PlantStandardVN plantStandard, MachineSettingVN machineSetting) {
		super();
		this.fields = fields;
		this.plantStandard = plantStandard;
		this.machineSetting = machineSetting;
	}
	public HavestPlanningInputVN() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
