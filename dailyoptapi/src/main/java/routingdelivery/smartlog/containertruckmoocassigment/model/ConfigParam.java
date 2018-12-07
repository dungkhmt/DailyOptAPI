package routingdelivery.smartlog.containertruckmoocassigment.model;

public class ConfigParam {
	private int cutMoocDuration;
	private int linkMoocDuration;
	private int hourPrev;
	private int hourPost;
	private String strategy;
	
	public ConfigParam(int cutMoocDuration, int linkMoocDuration,
			int hourPrev, int hourPost,
			String strategy) {
		super();
		this.cutMoocDuration = cutMoocDuration;
		this.linkMoocDuration = linkMoocDuration;
		this.hourPrev = hourPrev;
		this.hourPost = hourPost;
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
	public int getHourPrev(){
		return hourPrev;
	}
	public void setHourPrev(int hourPrev){
		this.hourPrev = hourPrev;
	}
	public int getHourPost(){
		return hourPost;
	}
	public void setHourPost(int hourPost){
		this.hourPost = hourPost;
	}
	public ConfigParam() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
