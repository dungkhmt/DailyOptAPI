package routingdelivery.smartlog.containertruckmoocassigment.service;

import routingdelivery.smartlog.containertruckmoocassigment.model.Measure;

public class Utils {
	public static final int FLEX_VEHICLES_ACCESS_WH = 0;
	public static final int HARD_VEHICLES_ACCESS_WH = 1;
	public static int MIN(int a, int b) {
		return a < b ? a : b;
	}
	public static final Measure MIN(Measure a, Measure b){
		if(a == null || b == null)
			return null;		
		if(a.distance < b.distance)
			return a;
		if(a.distance == b.distance
			&& a.time < b.time)
			return a;
		return b;
	}

	public static int MAX(int a, int b) {
		return a > b ? a : b;
	}

}
