package routingdelivery.smartlog.containertruckmoocassigment.model;

public class ConfigParam {
	private int cutMoocDuration;
	private int linkMoocDuration;
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
	
	
}
