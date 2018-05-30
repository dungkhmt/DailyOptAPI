package routingdelivery.smartlog.brenntag.model;

public class ExclusiveItem {
	private String code1;
	private String code2;
	public String getCode1() {
		return code1;
	}
	public void setCode1(String code1) {
		this.code1 = code1;
	}
	public String getCode2() {
		return code2;
	}
	public void setCode2(String code2) {
		this.code2 = code2;
	}
	public ExclusiveItem(String code1, String code2) {
		super();
		this.code1 = code1;
		this.code2 = code2;
	}
	public ExclusiveItem() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
