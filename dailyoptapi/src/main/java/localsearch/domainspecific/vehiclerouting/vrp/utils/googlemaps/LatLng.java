package localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps;

public class LatLng {

	/**
	 * @param args
	 */
	public double lat;
	public double lng;
	public LatLng(double lat, double lng){ this.lat = lat; this.lng = lng;}
	public LatLng(String latlng){
		String[] s = latlng.split(",");
		
		lat = Double.valueOf(s[0].trim());
		lng = Double.valueOf(s[1].trim());
	}
	public String toString(){ return lat + "," + lng;}
}
