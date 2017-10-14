package havestplanning.model;

public class MachineSetting {
	private int minLoad;
	private int maxLoad;
	public int getMinLoad() {
		return minLoad;
	}
	public void setMinLoad(int minLoad) {
		this.minLoad = minLoad;
	}
	public int getMaxLoad() {
		return maxLoad;
	}
	public void setMaxLoad(int maxLoad) {
		this.maxLoad = maxLoad;
	}
	public MachineSetting(int minLoad, int maxLoad) {
		super();
		this.minLoad = minLoad;
		this.maxLoad = maxLoad;
	}
	public MachineSetting() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
