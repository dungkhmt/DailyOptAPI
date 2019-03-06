package routingdelivery.smartlog.containertruckmoocassigment.model;

import java.util.ArrayList;
import java.util.HashMap;

public class Measure {
	public double distance;
	public int time;
	public int hardWarehouse;
	public boolean isBalance;
	public int driverId;
	public ArrayList<Warehouse> wh;
	public HashMap<String, String> srcdest;
	
	public Measure(double distance, int time){
		this.distance = distance;
		this.time = time;
	}
	
	public Measure(double distance, int time, int hardWarehouse, boolean isBalance){
		this.distance = distance;
		this.time = time;
		this.hardWarehouse = hardWarehouse;
		this.isBalance = isBalance;
	}
	
	public Measure(double distance, int time,
			int hardWarehouse, boolean isBalance, int driverId,
			ArrayList<Warehouse> wh, HashMap<String, String> srcdest){
		this.distance = distance;
		this.time = time;
		this.hardWarehouse = hardWarehouse;
		this.isBalance = isBalance;
		this.driverId = driverId;
		this.wh = wh;
		this.srcdest = srcdest;
	}
}
