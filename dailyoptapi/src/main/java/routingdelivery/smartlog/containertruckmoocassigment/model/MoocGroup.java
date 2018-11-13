package routingdelivery.smartlog.containertruckmoocassigment.model;

public class MoocGroup {
	private String code;
	private MoocPacking[] packing;
	
	public MoocGroup(String code, MoocPacking[] packing){
		this.code = code;
		this.packing = packing;
	}
	
	public String getCode(){
		return this.code;
	}
	
	public void setCode(String code){
		this.code = code;
	}
	
	public MoocPacking[] getPacking(){
		return this.packing;
	}
	
	public void setPacking(MoocPacking[] packing){
		this.packing = packing;
	}

	public MoocGroup() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
