package routingdelivery.smartlog.containertruckmoocassigment.service;

import routingdelivery.smartlog.containertruckmoocassigment.model.Measure;

public class Utils {
	public static final int FLEX_VEHICLES_ACCESS_WH = -1;
	public static final int HOME_VEHICLES_ACCESS_WH = 0;
	public static final int COM_VEHICLES_ACCESS_WH 	= 1;
	
	public static final int NO_HARD_WH 	= 0;
	public static final int HARD_PICKUP_WH 	= 1;
	public static final int HARD_DELIVERY_WH = 2;
	public static final int HARD_PICKUP_AND_DELIVERY_WH = 3;
	
	public static final int CAT_MOOC_CHUA_CO_KE_HOACH = 2;
	
	public static final int CANNOT_ACCESS_HARD_WH = 1;
	public static final int CANNOT_FIND_ROMOOC = 2;
	public static final int CANNOt_FIND_SWAP_REQUEST = 3;
	
	
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
