package routingdelivery.smartlog.containertruckmoocassigment.service;

import java.util.ArrayList;
import java.util.HashMap;

import routingdelivery.smartlog.containertruckmoocassigment.model.RouteElement;

public class Container20Requests {
	public HashMap<Integer, ArrayList<RouteElement>> cont20RE;
	public HashMap<Integer, Integer> isNeedCont;
	public HashMap<Integer, Integer> isNeedReturnCont;
	public HashMap<Integer, ArrayList<String>> returnContDepotCode;
	public HashMap<Integer, String> lateDateTimeDeliveryAtDepot;
	public HashMap<Integer, ArrayList<String>> whCode;
	public HashMap<Integer, Double> weights;
	public HashMap<Integer, String> contCategory;
	public HashMap<Integer, String> contCode;
	public HashMap<Integer, String> shipCompanyCode;
	public HashMap<Integer, String> orderCode;
	
	public Container20Requests(){
		cont20RE = new HashMap<Integer, ArrayList<RouteElement>>();
		isNeedCont = new HashMap<Integer, Integer>();
		isNeedReturnCont = new HashMap<Integer, Integer>();
		returnContDepotCode = new HashMap<Integer, ArrayList<String>>();
		lateDateTimeDeliveryAtDepot = new HashMap<Integer, String>();
		whCode = new HashMap<Integer, ArrayList<String>>();
		weights = new HashMap<Integer, Double>();
		contCategory = new HashMap<Integer, String>();
		contCode = new HashMap<Integer, String>();
		shipCompanyCode = new HashMap<Integer, String>();
		orderCode = new HashMap<Integer, String>();
	}
	
	public void removeRequest(int reqCode){
		cont20RE.remove(reqCode);

		isNeedCont.remove(reqCode);
		
		isNeedReturnCont.remove(reqCode);

		returnContDepotCode.remove(reqCode);
		
		lateDateTimeDeliveryAtDepot.remove(reqCode);

		whCode.remove(reqCode);
		
		weights.remove(reqCode);

		contCategory.remove(reqCode);
		
		contCode.remove(reqCode);

		shipCompanyCode.remove(reqCode);
		
		orderCode.remove(reqCode);
	}
}
