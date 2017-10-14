package havestplanning.model.vn;

public class MachineSettingVN {
	private float CONGSUAT_MIN;
	private float CONGSUAT_MAX;
	public float getCONGSUAT_MIN() {
		return CONGSUAT_MIN;
	}
	public void setCONGSUAT_MIN(float cONGSUAT_MIN) {
		CONGSUAT_MIN = cONGSUAT_MIN;
	}
	public float getCONGSUAT_MAX() {
		return CONGSUAT_MAX;
	}
	public void setCONGSUAT_MAX(float cONGSUAT_MAX) {
		CONGSUAT_MAX = cONGSUAT_MAX;
	}
	public MachineSettingVN(float cONGSUAT_MIN, float cONGSUAT_MAX) {
		super();
		CONGSUAT_MIN = cONGSUAT_MIN;
		CONGSUAT_MAX = cONGSUAT_MAX;
	}
	public MachineSettingVN() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
