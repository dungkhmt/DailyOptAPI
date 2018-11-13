package routingdelivery.smartlog.containertruckmoocassigment.model;

public class MoocPacking {
	private String contTypeCode;
	private int contTypeQuantity;
	public MoocPacking(String conTypeCode, int conTypeQuantity){
		this.contTypeCode = conTypeCode;
		this.contTypeQuantity = conTypeQuantity;
	}
	
	public String getContTypeCode(){
		return this.contTypeCode;
	}
	
	public void setContTypeCode(String contTypeCode){
		this.contTypeCode = contTypeCode;
	}
	
	public int getContTypeQuantity(){
		return this.contTypeQuantity;
	}
	
	public void setContTypeQuantity(int contTypeQuantity){
		this.contTypeQuantity = contTypeQuantity;
	}
}
