package routingdelivery.smartlog.sem.model;

import java.io.PrintWriter;

import com.google.gson.Gson;

import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.GoogleMapsQuery;
import routingdelivery.model.DistanceElement;
import routingdelivery.model.Item;

public class SEMPickupDeliveryInput {
	private SEMPickupDeliveryRequest[] requests;
	private SEMShipper[] shippers;
	private DistanceElement[] distances;
	private DistanceElement[] traveltimes;
	
	
	public SEMPickupDeliveryInput(SEMPickupDeliveryRequest[] requests,
			SEMShipper[] shippers, DistanceElement[] distances,
			DistanceElement[] traveltimes) {
		super();
		this.requests = requests;
		this.shippers = shippers;
		this.distances = distances;
		this.traveltimes = traveltimes;
	}
	public DistanceElement[] getTraveltimes() {
		return traveltimes;
	}
	public void setTraveltimes(DistanceElement[] traveltimes) {
		this.traveltimes = traveltimes;
	}
	public SEMPickupDeliveryInput(SEMPickupDeliveryRequest[] requests,
			SEMShipper[] shippers, DistanceElement[] distances) {
		super();
		this.requests = requests;
		this.shippers = shippers;
		this.distances = distances;
	}
	public DistanceElement[] getDistances() {
		return distances;
	}
	public void setDistances(DistanceElement[] distances) {
		this.distances = distances;
	}
	public SEMPickupDeliveryRequest[] getRequests() {
		return requests;
	}
	public void setRequests(SEMPickupDeliveryRequest[] requests) {
		this.requests = requests;
	}
	public SEMShipper[] getShippers() {
		return shippers;
	}
	public void setShippers(SEMShipper[] shippers) {
		this.shippers = shippers;
	}
	public SEMPickupDeliveryInput(SEMPickupDeliveryRequest[] requests,
			SEMShipper[] shippers) {
		super();
		this.requests = requests;
		this.shippers = shippers;
	}
	public SEMPickupDeliveryInput() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String[] args){
		String[] pickupLoc = {"0001","0002","0003","0004","0005","0006","0007","0008"};
		String[] deliveryLoc = {"1001","1002","1003","1004","1005","1006","1007","1008"};
		String[] latlngpickup = {"21.01,105.01","21.02,105.02","21.03,105.03","21.04,105.04","21.05,105.05","21.06,105.06","21.07,105.07","21.08,105.08"};
		String[] latlngdelivery = {"21.03,105.01","21.02,105.06","21.01,105.07","21.07,105.03","21.02,105.02","21.07,105.01","21.06,105.04","21.05,105.02"};
		String[] orderID = {"00001","00002","00003","00004","00005","00006","00007","00008"};
		SEMPickupDeliveryRequest[] r = new SEMPickupDeliveryRequest[orderID.length];
		
		int nbItems = 0;
		for(int i = 0; i < orderID.length; i++){
			Item[] I = new Item[1];
			nbItems++;
			I[0] = new Item(0,0,0,"name","000" + nbItems,1,10,300,300,orderID[i],"");
			int amountMoney = 100000;
			
			
			String pickupLocationCode = pickupLoc[i];
			String pickupAddr = "";
			String[] s = latlngpickup[i].split(",");
			double pickupLat = Double.valueOf(s[0]);
			double pickupLng = Double.valueOf(s[1]);
			String earlyPickupTime = "2018-06-30 08:00:00";
			String latePickupTime = "2018-06-30 18:00:00";
			int pickupDuration = 300;
			
			String deliveryLocationCode = deliveryLoc[i];
			String deliveryAddr = "";
			s = latlngdelivery[i].split(",");
			double deliveryLat = Double.valueOf(s[0]);
			double deliveryLng = Double.valueOf(s[1]);
			String earlyDeliveryTime = "2018-06-30 08:00:00";
			String lateDeliveryTime = "2018-06-30 18:00:00";
			int deliveryDuration = 300;
			
			String splitDelivery = "N";// "Y" or "N"

			r[i] = new SEMPickupDeliveryRequest(orderID[i], 
					I, pickupLocationCode, pickupAddr, pickupLat, pickupLng, earlyPickupTime, 
					latePickupTime, pickupDuration, deliveryLocationCode, deliveryAddr, deliveryLat, 
					deliveryLng, earlyDeliveryTime, lateDeliveryTime, deliveryDuration, splitDelivery);
			r[i].setAmountMoney(amountMoney);
		}
		String[] startLocationCodes = {"20001","20002"};
		String[] endLocationCodes = {"30001","30002"};
		String[] latlngStart = {"21.01,105.07","21.06,105.01"};
		String[] latlngEnd = {"21.01,105.07","21.06,105.01"};
		SEMShipper[] shippers = new SEMShipper[2];
		for(int i = 0; i < shippers.length; i++){
			String shipperID = "SH000" + i;
			double weightCapacity = 100;
			int maxOrder = 5;
			int maxAmountMoney = 2000000;
			String startWorkingTime = "2018-06-30 08:00:00";
			String endWorkingTime = "2018-06-30 18:00:00";
			String[] s= latlngStart[i].split(",");
			double startLat = Double.valueOf(s[0]);
			double startLng = Double.valueOf(s[1]);
			String startLocationCode = startLocationCodes[i];
			s = latlngEnd[i].split(",");
			double endLat = Double.valueOf(s[0]);
			double endLng = Double.valueOf(s[1]);
			String endLocationCode = endLocationCodes[i];
			shippers[i] = new SEMShipper(shipperID, weightCapacity, maxOrder, maxAmountMoney, startWorkingTime, endWorkingTime, startLat, startLng, startLocationCode, endLat, endLng, endLocationCode);
		
			shippers[i].setCurrentMoney(0);
			shippers[i].setCurrentNbOrders(0);
			shippers[i].setCurrentWeight(0);
		}
		String[] locCode = new String[pickupLoc.length+deliveryLoc.length + startLocationCodes.length + endLocationCodes.length];
		String[] latlng = new String[latlngpickup.length+latlngdelivery.length+latlngStart.length+latlngEnd.length];
		int idx = -1;
		for(int i = 0; i < pickupLoc.length; i++){
			idx++;
			locCode[idx] = pickupLoc[i];
			latlng[idx] = latlngpickup[i];
		}
		for(int i = 0; i < deliveryLoc.length; i++){
			idx++;
			locCode[idx] = deliveryLoc[i];
			latlng[idx] = latlngdelivery[i];
		}
		for(int i = 0; i < startLocationCodes.length;i++){
			idx++;
			locCode[idx] = startLocationCodes[i];
			latlng[idx] = latlngStart[i];
		}
		for(int i = 0; i < endLocationCodes.length;i++){
			idx++;
			locCode[idx] = endLocationCodes[i];
			latlng[idx] = latlngEnd[i];
		}
		DistanceElement[] dis = new DistanceElement[locCode.length * locCode.length];
		DistanceElement[] T = new DistanceElement[locCode.length * locCode.length];
		idx = -1;
		GoogleMapsQuery G = new GoogleMapsQuery();
		for(int i = 0; i < locCode.length; i++){
			for(int j = 0; j < locCode.length; j++){
				idx++;
				String src = locCode[i];
				String dest = locCode[j];
				String[] si = latlng[i].split(",");
				String[] sj = latlng[j].split(",");
				double lati = Double.valueOf(si[0]);
				double lngi = Double.valueOf(si[1]);
				double latj = Double.valueOf(sj[0]);
				double lngj = Double.valueOf(sj[1]);
				double d = G.computeDistanceHaversine(lati, lngi, latj, lngj);
				double t = d*3600/30;// in seconds
				d = d * 1000;// in meters
				dis[idx] = new DistanceElement(src, dest, d);
				T[idx] = new DistanceElement(src,dest,t);
			}
		}
		SEMPickupDeliveryInput input = new SEMPickupDeliveryInput(r, shippers, dis, T);
		
		try{
			Gson gson = new Gson();
			String json = gson.toJson(input);
			PrintWriter out = new PrintWriter("C:/tmp/SEM.json");
			out.print(json);
			out.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
