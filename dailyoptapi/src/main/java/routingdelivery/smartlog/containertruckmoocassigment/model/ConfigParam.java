package routingdelivery.smartlog.containertruckmoocassigment.model;

public class ConfigParam {
	private int cutMoocDuration;
	private int linkMoocDuration;
	private String strategy;
	
	public ConfigParam(int cutMoocDuration, int linkMoocDuration,
			String strategy) {
		super();
		this.cutMoocDuration = cutMoocDuration;
		this.linkMoocDuration = linkMoocDuration;
		this.strategy = strategy;
	}
	public String getStrategy() {
		return strategy;
	}
	public void setStrategy(String strategy) {
		this.strategy = strategy;
	}
	public int getCutMoocDuration() {
		return cutMoocDuration;
	}
	public void setCutMoocDuration(int cutMoocDuration) {
		this.cutMoocDuration = cutMoocDuration;
	}
	public int getLinkMoocDuration() {
		return linkMoocDuration;
	}
	public void setLinkMoocDuration(int linkMoocDuration) {
		this.linkMoocDuration = linkMoocDuration;
	}
	public ConfigParam(int cutMoocDuration, int linkMoocDuration) {
		super();
		this.cutMoocDuration = cutMoocDuration;
		this.linkMoocDuration = linkMoocDuration;
	}
	public ConfigParam() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
