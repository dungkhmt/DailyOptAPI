package routingdelivery.smartlog.brenntag.model;

import routingdelivery.model.ConfigParams;
import routingdelivery.smartlog.containertruckmoocassigment.model.ConfigParam;

public class SolutionIndicator {
	private double distance;
	private int nbInternalTrucks;
	private int nbExternalTrucks;
	private int nbTrips;
	private double internalTruckLoad;
	private double externalTruckLoad;
	private double internalCapacity;
	private double externalCapacity;
	
	public SolutionIndicator(double distance, int nbInternalTrucks,
			int nbExternalTrucks, int nbTrips, double internalTruckLoad,
			double externalTruckLoad, double internalCapacity,
			double externalCapacity) {
		super();
		this.distance = distance;
		this.nbInternalTrucks = nbInternalTrucks;
		this.nbExternalTrucks = nbExternalTrucks;
		this.nbTrips = nbTrips;
		this.internalTruckLoad = internalTruckLoad;
		this.externalTruckLoad = externalTruckLoad;
		this.internalCapacity = internalCapacity;
		this.externalCapacity = externalCapacity;
	}
	public double getInternalCapacity() {
		return internalCapacity;
	}
	public void setInternalCapacity(double internalCapacity) {
		this.internalCapacity = internalCapacity;
	}
	public double getExternalCapacity() {
		return externalCapacity;
	}
	public void setExternalCapacity(double externalCapacity) {
		this.externalCapacity = externalCapacity;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public int getNbInternalTrucks() {
		return nbInternalTrucks;
	}
	public void setNbInternalTrucks(int nbInternalTrucks) {
		this.nbInternalTrucks = nbInternalTrucks;
	}
	public int getNbExternalTrucks() {
		return nbExternalTrucks;
	}
	public void setNbExternalTrucks(int nbExternalTrucks) {
		this.nbExternalTrucks = nbExternalTrucks;
	}
	public int getNbTrips() {
		return nbTrips;
	}
	public void setNbTrips(int nbTrips) {
		this.nbTrips = nbTrips;
	}
	public double getInternalTruckLoad() {
		return internalTruckLoad;
	}
	public void setInternalTruckLoad(double internalTruckLoad) {
		this.internalTruckLoad = internalTruckLoad;
	}
	public double getExternalTruckLoad() {
		return externalTruckLoad;
	}
	public void setExternalTruckLoad(double externalTruckLoad) {
		this.externalTruckLoad = externalTruckLoad;
	}
	public SolutionIndicator(double distance, int nbInternalTrucks,
			int nbExternalTrucks, int nbTrips, double internalTruckLoad,
			double externalTruckLoad) {
		super();
		this.distance = distance;
		this.nbInternalTrucks = nbInternalTrucks;
		this.nbExternalTrucks = nbExternalTrucks;
		this.nbTrips = nbTrips;
		this.internalTruckLoad = internalTruckLoad;
		this.externalTruckLoad = externalTruckLoad;
	}
	public SolutionIndicator() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public boolean better(SolutionIndicator I, ConfigParams params){
		if(distance >= I.getDistance()) return false;
		if(params.getInternalVehicleFirst().equals("Y")){
			if(nbExternalTrucks > 0){
				if(nbInternalTrucks <= I.getNbInternalTrucks()) return false;
			}else{
				
			}
			if(nbInternalTrucks == I.getNbInternalTrucks() && nbExternalTrucks >= I.getNbExternalTrucks())
				return false;
			if(internalTruckLoad <= I.getInternalTruckLoad()) return false;
		}else{
			if(nbInternalTrucks + nbExternalTrucks >= I.getNbInternalTrucks() + I.getNbExternalTrucks()) 
				return false;
			
		}
		if(internalCapacity + externalCapacity >= I.getInternalCapacity() + I.getExternalCapacity()) return false;
		
		return true;
	}
	
}
