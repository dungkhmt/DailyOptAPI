package localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps;


import java.io.DataInputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;










import utils.DateTimeUtils;

import java.net.URLEncoder;

import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;

public class GoogleMapsQuery {

	/**
	 * @param args
	 */
	public static final int SPEED = 40;// average speed is 40km/h
	public static final double RATIO = 1.5;// ratio between gMap distance and
											// Havsine distance

	public GoogleMapsQuery() {
	}

	public double getApproximateTravelTimeSecond(double lat1, double long1,
			double lat2, double long2) {
		double t = computeDistanceHaversine(lat1, long1, lat2, long2);
		t = t * 3600.0 / SPEED;
		t = t * RATIO;
		return t;
	}

	public double getApproximateDistanceMeter(double lat1, double long1,
			double lat2, double long2) {
		double t = computeDistanceHaversine(lat1, long1, lat2, long2) * 1000;
		t = t * RATIO;
		return t;
	}
	public double computeDistanceHaversine(String latlng1,
			String latlng2) {
		String[]s = latlng1.split(",");
		double lat1 = Double.valueOf(s[0]);
		double lng1 = Double.valueOf(s[1]);
		s = latlng2.split(",");
		double lat2 = Double.valueOf(s[0]);
		double lng2 = Double.valueOf(s[1]);
		return computeDistanceHaversine(lat1, lng1, lat2, lng2);
	}
	
	public double computeDistanceHaversine(double lat1, double long1,
			double lat2, double long2) {
		double SCALE = 1;
		double PI = 3.14159265;
		long1 = long1 * 1.0 / SCALE;
		lat1 = lat1 * 1.0 / SCALE;
		long2 = long2 * 1.0 / SCALE;
		lat2 = lat2 * 1.0 / SCALE;

		double dlat1 = lat1 * PI / 180;
		double dlong1 = long1 * PI / 180;
		double dlat2 = lat2 * PI / 180;
		double dlong2 = long2 * PI / 180;

		double dlong = dlong2 - dlong1;
		double dlat = dlat2 - dlat1;

		double aHarv = Math.pow(Math.sin(dlat / 2), 2.0) + Math.cos(dlat1)
				* Math.cos(dlat2) * Math.pow(Math.sin(dlong / 2), 2.0);
		double cHarv = 2 * Math.atan2(Math.sqrt(aHarv), Math.sqrt(1.0 - aHarv));

		double R = 6378.137; // in km

		return R * cHarv * SCALE; // in km

	}

	public LatLng getCoordinate(String address) {
		URL url = null;
		try {
			url = new URL(
					"http://maps.google.com/maps/api/geocode/xml?address="
							+ URLEncoder.encode(address, "UTF-8")
							+ "&sensor=false");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		HttpURLConnection urlConn = null;
		try {
			// URL connection channel.
			urlConn = (HttpURLConnection) url.openConnection();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		// Let the run-time system (RTS) know that we want input.
		urlConn.setDoInput(true);

		// Let the RTS know that we want to do output.
		urlConn.setDoOutput(true);
		// No caching, we want the real thing.
		urlConn.setUseCaches(false);
		try {
			urlConn.setRequestMethod("POST");
		} catch (ProtocolException ex) {
			ex.printStackTrace();
		}
		try {
			urlConn.connect();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		DataOutputStream output = null;
		DataInputStream input = null;
		try {
			output = new DataOutputStream(urlConn.getOutputStream());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		// Get response data.
		String str = null;
		try {
			PrintWriter out = new PrintWriter(new FileWriter(
					"http-post-log.txt"));
			input = new DataInputStream(urlConn.getInputStream());
			try {
				DocumentBuilder builder = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();
				Document doc = builder.parse(input);
				doc.getDocumentElement().normalize();

				NodeList nl = doc.getElementsByTagName("geometry");
				Node nod = nl.item(0);
				Element e = (Element) nod;
				if (e == null) {
					return null;
				}

				nl = e.getElementsByTagName("location");
				nod = nl.item(nl.getLength() - 1);
				
				e = (Element) nod;
				nl = e.getElementsByTagName("lat");
				nod = nl.item(0);

				String d_s = nod.getChildNodes().item(0).getNodeValue();
				double lat = Double.valueOf(d_s);

				nl = e.getElementsByTagName("lng");
				nod = nl.item(0);
				d_s = nod.getChildNodes().item(0).getNodeValue();
				double lng = Double.valueOf(d_s);

				return new LatLng(lat, lng);
			} catch (Exception e) {
				e.printStackTrace();
			}

			input.close();
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public double getDistance(String latlng1, String latlng2){
		double distance = 0;
		String[] elements1 =latlng1.split(",");
	
		double lat1 = Double.parseDouble(elements1[0]);
		double lng1 = Double.parseDouble(elements1[1]);
		
		String[] elements2 =latlng2.split(",");
		double lat2 = Double.parseDouble(elements2[0]);
		double lng2 = Double.parseDouble(elements2[1]);
		
		
		distance = getDistance(lat1, lng1, lat2, lng2);
		//System.out.println("[" + lat1 + "," + lng1 + "] ->[" + lat2 + "," + lng2 + "] = " + distance );
		
		return distance;
	}
	
	public double getDistance(double lat1, double lng1, double lat2, double lng2) {
		System.out.println("[" + lat1 + ", " + lng1 + "] -> [" + lat2 + ", " + lng2 + "]");
		URL url = null;
		try {
			url = new URL(
					"http://maps.google.com/maps/api/directions/xml?origin="
							+ lat1 + "," + lng1 + "&destination=" + lat2 + ","
							+ lng2 + "&sensor=false&units=metric");
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
		}
		//System.out.println("URL: " + url);
		
		HttpURLConnection urlConn = null;
		try {
			// URL connection channel.
			urlConn = (HttpURLConnection) url.openConnection();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		// Let the run-time system (RTS) know that we want input.
		urlConn.setDoInput(true);

		// Let the RTS know that we want to do output.
		urlConn.setDoOutput(true);

		// No caching, we want the real thing.
		urlConn.setUseCaches(false);

		try {
			urlConn.setRequestMethod("POST");
		} catch (ProtocolException ex) {
			ex.printStackTrace();
		}

		try {
			urlConn.connect();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		DataOutputStream output = null;
		DataInputStream input = null;

		try {
			output = new DataOutputStream(urlConn.getOutputStream());
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		// Get response data.
		
		double d = -1;
		try {
			PrintWriter out = new PrintWriter(new FileWriter(
					"http-post-log.txt"));
			input = new DataInputStream(urlConn.getInputStream());
			try {
				DocumentBuilder builder = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();
				Document doc = builder.parse(input);
				doc.getDocumentElement().normalize();

				NodeList nl = doc.getElementsByTagName("leg");
//				System.out.println("length: " + nl.getLength());
//				System.out.println("nl: " + nl.toString());
//				System.out.println("nl.item(0): " + nl.item(0).toString());
				Node nod = nl.item(0);
//				
//				
				Element e = (Element) nod;
//								
				if (e == null) {
					System.out.println("e is null");
					return -1;
				}
//				System.out.println("e: " + e.toString());
//				
//				//nl = e.getElementsByTagName("step");
				nl = e.getElementsByTagName("distance");
//				System.out.println("nl: " + nl.toString());
				nod = nl.item(nl.getLength() - 1);
//				System.out.println("nod: " + nod.toString());
				
				e = (Element) nod;
//				System.out.println("e: " + e.toString());
				
				nl = e.getElementsByTagName("text");
//				System.out.println("nl: " + nl.toString());
				nod = nl.item(0);
//				System.out.println("nod: " + nod.toString());

				String d_s = nod.getChildNodes().item(0).getNodeValue();
//				System.out.println("d_s: " + d_s);
				int idx = d_s.indexOf("km");
				if (idx < 0) {
					idx = d_s.indexOf("m");
					if (idx == -1) {
						System.out.println("idx is null");
						return -1;
					}
					d_s = d_s.substring(0, idx);
					d = Double.valueOf(d_s) * 0.001; // convert into km
				} else {
					d_s = d_s.substring(0, idx);
					d = Double.valueOf(d_s);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			input.close();
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return d;
	}

	// distance is measured in m
	public double estimateDistanceMeter(double lat1, double lng1, double lat2,
			double lng2){
		//double d = getDistance(lat1, lng1, lat2, lng2);
		//if(d < 0)
			return getApproximateDistanceMeter(lat1, lng1, lat2, lng2);
		
		//return d*1000;
	}
	
	// speed is measured in m/s
	public int estimateTravelTime(double lat1, double lng1, double lat2,
			double lng2, String mode, int speed, double APPX) {
		double d = computeDistanceHaversine(lat1,lng1,lat2,lng2);
		int appxt = (int)(d*1000*APPX/speed);// approximate traveltime
		//int t = getTravelTime(lat1,lng1,lat2,lng2, mode);
		//if(t < 0) t = appxt;
		//return t;
		return appxt;
	}
	// speeds are measured in m/s
	
	public int estimateTravelTimeWithTimeFrame(double lat1, double lng1, double lat2,
			double lng2, String mode, String startDateTime, double stdSpeed, double denseTrafficSpeed) {
		double d = computeDistanceHaversine(lat1,lng1,lat2,lng2);
		double appxd = d*1000*RATIO;
		int hour = DateTimeUtils.getHour(startDateTime);
		double speed = stdSpeed;
		
		if(7 <= hour && hour <= 9 || 16 <= hour && hour <= 18)
			speed = denseTrafficSpeed;
		
		int appxt = (int)(appxd/speed);// approximate traveltime
		//int t = getTravelTime(lat1,lng1,lat2,lng2, mode);
		double dis = getDistance(lat1, lng1, lat2, lng2);
		int t = (int)(dis*1000/speed);
		
		if(t < 0) t = appxt;
		//System.out.println(name() + "::estimateTravelTimeWithTimeFrame, d = " + d + ", dis = " + dis);
		
		return t;
	}
	
	public int getTravelTime(double lat1, double lng1, double lat2,
			double lng2, String mode) {
		// try to probe maximum 20 times
		int t = -1;
		int maxTrials = 2;
		for (int i = 0; i < maxTrials; i++) {
			t = getTravelTimeOnePost(lat1, lng1, lat2, lng2, mode);
			if (t > -1)
				break;
		}

		return t;
	}
	
	public int getTravelTime(String originAddr, String destinationAddr, String mode) {
		// try to probe maximum 20 times
		int t = -1;
		int maxTrials = 10;
		for (int i = 0; i < maxTrials; i++) {
			t = getTravelTimeOnePost(originAddr, destinationAddr, mode);
			if (t > -1)
				break;
		}

		return t;
	}

	private int getTravelTimeOnePost(double lat1, double lng1, double lat2,
			double lng2, String mode) {

		URL url = null;
		try {
			url = new URL(
					"http://maps.google.com/maps/api/directions/xml?origin="
							+ lat1 + "," + lng1 + "&destination=" + lat2 + ","
							+ lng2 + "&sensor=false&units=metric");
			
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
		}

		HttpURLConnection urlConn = null;
		try {
			// URL connection channel.
			urlConn = (HttpURLConnection) url.openConnection();
		} catch (IOException ex) {
			System.out.println("openConnection failed");
			ex.printStackTrace();
		}

		// Let the run-time system (RTS) know that we want input.
		urlConn.setDoInput(true);

		// Let the RTS know that we want to do output.
		urlConn.setDoOutput(true);

		// No caching, we want the real thing.
		urlConn.setUseCaches(false);

		try {
			urlConn.setRequestMethod("POST");
		} catch (ProtocolException ex) {
			ex.printStackTrace();
		}

		try {
			urlConn.connect();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		DataOutputStream output = null;
		DataInputStream input = null;

		try {
			output = new DataOutputStream(urlConn.getOutputStream());
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		// Get response data.
		String str = null;
		int duration = -1;// in seconds
		try {
			input = new DataInputStream(urlConn.getInputStream());
			try {
				DocumentBuilder builder = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();
				Document doc = builder.parse(input);

				doc.getDocumentElement().normalize();

				NodeList nl = doc.getElementsByTagName("leg");
				Node nod = nl.item(0);
				Element e = (Element) nod;
				if (e == null) {
					return -1;
				}
				nl = e.getElementsByTagName("duration");
				nod = nl.item(nl.getLength() - 1);
				
				e = (Element) nod;
				nl = e.getElementsByTagName("value");
				nod = nl.item(0);

				e = (Element) nod;

				duration = Integer.valueOf(e.getChildNodes().item(0)
						.getNodeValue());

			} catch (Exception e) {
				e.printStackTrace();
			}

			input.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return duration;
	}
	
	public String standardizeAddr(String addr){
		String stdAddr = "";
		String[] s = addr.split(" ");
		for(int i = 0; i < s.length-1; i++)
			stdAddr += s[i].trim() + "+";
		stdAddr += s[s.length-1].trim();
		return stdAddr;
	}
	
	public String name(){
		return "GoogleMapsQuery";
	}
	
	private int getTravelTimeOnePost(String originAddr, String destinationAddr, String mode) {
		String stdOriginAddr = standardizeAddr(originAddr);
		String stdDestinationAddr = standardizeAddr(destinationAddr);
		
		
		URL url = null;
		try {
			url = new URL(
					"http://maps.google.com/maps/api/directions/xml?origin="
							+ stdOriginAddr + "&destination=" + stdDestinationAddr + "&sensor=false&units=metric");
			System.out.println(name() + "::getTravelTimeOnePost, url = " + url);
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
		}

		HttpURLConnection urlConn = null;
		try {
			// URL connection channel.
			urlConn = (HttpURLConnection) url.openConnection();
		} catch (IOException ex) {
			System.out.println("openConnection failed");
			ex.printStackTrace();
		}

		// Let the run-time system (RTS) know that we want input.
		urlConn.setDoInput(true);

		// Let the RTS know that we want to do output.
		urlConn.setDoOutput(true);

		// No caching, we want the real thing.
		urlConn.setUseCaches(false);

		try {
			urlConn.setRequestMethod("POST");
		} catch (ProtocolException ex) {
			ex.printStackTrace();
		}

		try {
			urlConn.connect();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		DataOutputStream output = null;
		DataInputStream input = null;

		try {
			output = new DataOutputStream(urlConn.getOutputStream());
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		// Get response data.
		String str = null;
		int duration = -1;// in seconds
		try {
			input = new DataInputStream(urlConn.getInputStream());
			try {
				DocumentBuilder builder = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();
				Document doc = builder.parse(input);

				doc.getDocumentElement().normalize();

				NodeList nl = doc.getElementsByTagName("leg");
				Node nod = nl.item(0);
				Element e = (Element) nod;
				if (e == null) {
					return -1;
				}
				nl = e.getElementsByTagName("duration");
				nod = nl.item(nl.getLength() - 1);
				
				e = (Element) nod;
				nl = e.getElementsByTagName("value");
				nod = nl.item(0);

				e = (Element) nod;

				duration = Integer.valueOf(e.getChildNodes().item(0)
						.getNodeValue());

			} catch (Exception e) {
				e.printStackTrace();
			}

			input.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return duration;
	}

	public String getLatLngFromAddress(String addr) {
		String latlng = "";
		for(int i = 0; i < 100; i++){
			latlng = getLatLngFromAddressOnePost(addr);
			if(!latlng.equals("") && latlng != null) return latlng;
		}
		return latlng;
	}
	
	public String getLatLngFromAddressOnePost(String addr) {
		String stdAddr = standardizeAddr(addr);
		
		
		URL url = null;
		try {
			url = new URL(
					"http://maps.google.com/maps/api/geocode/xml?address="
							+ stdAddr + "&sensor=false&units=metric");
			//System.out.println(url);
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
		}

		HttpURLConnection urlConn = null;
		try {
			// URL connection channel.
			urlConn = (HttpURLConnection) url.openConnection();
		} catch (IOException ex) {
			System.out.println("openConnection failed");
			ex.printStackTrace();
		}

		// Let the run-time system (RTS) know that we want input.
		urlConn.setDoInput(true);

		// Let the RTS know that we want to do output.
		urlConn.setDoOutput(true);

		// No caching, we want the real thing.
		urlConn.setUseCaches(false);

		try {
			urlConn.setRequestMethod("POST");
		} catch (ProtocolException ex) {
			ex.printStackTrace();
		}

		try {
			urlConn.connect();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		DataOutputStream output = null;
		DataInputStream input = null;

		try {
			output = new DataOutputStream(urlConn.getOutputStream());
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		// Get response data.
		String str = null;
		int duration = -1;// in seconds
		try {
			input = new DataInputStream(urlConn.getInputStream());
			try {
				DocumentBuilder builder = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();
				Document doc = builder.parse(input);

				doc.getDocumentElement().normalize();

				NodeList nl = doc.getElementsByTagName("geometry");
				Node nod = nl.item(0);
				Element e = (Element) nod;
				if (e == null) {
					return "";
				}
				nl = e.getElementsByTagName("location");
				nod = nl.item(0);
				
				e = (Element) nod;
				//nl = e.getElementsByTagName("lat");
				Node nodLat = e.getElementsByTagName("lat").item(0);

				Node nodLng = e.getElementsByTagName("lng").item(0);
				
				e = (Element) nodLat;

				String lat = e.getChildNodes().item(0)
						.getNodeValue();
				
				e = (Element) nodLng;
				String lng = e.getChildNodes().item(0)
						.getNodeValue();
				
				return lat + "," + lng;

			} catch (Exception e) {
				e.printStackTrace();
			}

			input.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return "";
	}

	public Direction getDirection(double lat1, double lng1, double lat2,
			double lng2, String mode) {
		Direction direction = null;
		ArrayList<StepDirection> steps = new ArrayList<StepDirection>();
		URL url = null;
		int durations = 0;
		int distances = 0;
		try {
			url = new URL(
					"http://maps.google.com/maps/api/directions/xml?origin="
							+ lat1 + "," + lng1 + "&destination=" + lat2 + ","
							+ lng2 + "&sensor=false&units=metric&mode=" + mode);
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
		}
		System.out.println("URL: " + url);

		HttpURLConnection urlConn = null;
		try {
			// URL connection channel.
			urlConn = (HttpURLConnection) url.openConnection();
		} catch (IOException ex) {
			System.out.println("openConnection failed");
			ex.printStackTrace();
		}

		// Let the run-time system (RTS) know that we want input.
		urlConn.setDoInput(true);
		urlConn.setDoOutput(true);
		// No caching, we want the real thing.
		urlConn.setUseCaches(false);

		try {
			urlConn.setRequestMethod("POST");
		} catch (ProtocolException ex) {
			ex.printStackTrace();
		}

		try {
			urlConn.connect();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		DataOutputStream output = null;
		DataInputStream input = null;
		try {
			output = new DataOutputStream(urlConn.getOutputStream());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		// Get response data.
		String str = null;
		try {
			input = new DataInputStream(urlConn.getInputStream());
			try {
				DocumentBuilder builder = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();
				Document doc = builder.parse(input);
				doc.getDocumentElement().normalize();

				// read steps
				NodeList nl = doc.getElementsByTagName("step");
				int szLocation = nl.getLength();
				String lat_start_location;
				String lng_start_location;

				String lat_end_location;
				String lng_end_location;

				int duration;
				float distance;
				String modeStep;
				String html_instruction;

				for (int i = 0; i < szLocation; i++) {
					// read start locations
					NodeList nlStart = doc
							.getElementsByTagName("start_location");
					Element e = (Element) nlStart.item(i);
					lat_start_location = e.getElementsByTagName("lat").item(0)
							.getChildNodes().item(0).getNodeValue();
					lng_start_location = e.getElementsByTagName("lng").item(0)
							.getChildNodes().item(0).getNodeValue();

					// read end locations
					NodeList nlEnd = doc.getElementsByTagName("end_location");
					e = (Element) nlEnd.item(i);
					lat_end_location = e.getElementsByTagName("lat").item(0)
							.getChildNodes().item(0).getNodeValue();
					lng_end_location = e.getElementsByTagName("lng").item(0)
							.getChildNodes().item(0).getNodeValue();

					// read duration
					NodeList nlDuration = doc.getElementsByTagName("duration");
					e = (Element) nlDuration.item(i);
					duration = Integer.parseInt(e.getElementsByTagName("value")
							.item(0).getChildNodes().item(0).getNodeValue());
					durations += duration;
					
					// read distance
					NodeList nlDistance = doc.getElementsByTagName("distance");
					e = (Element) nlDistance.item(i);
					distance = Float.parseFloat(e.getElementsByTagName("value")
							.item(0).getChildNodes().item(0).getNodeValue());
					distances += distance;
					
					// read mode
					NodeList nlModeStep = doc
							.getElementsByTagName("travel_mode");
					e = (Element) nlModeStep.item(i);
					modeStep = e.getChildNodes().item(0).getNodeValue();
					
					// read html instruction
					NodeList nlHTML_instructions = doc
							.getElementsByTagName("html_instructions");
					e = (Element) nlHTML_instructions.item(i);
					html_instruction = e.getChildNodes().item(0).getNodeValue();
					
					StepDirection step = new StepDirection(lat_start_location,
							lng_start_location, lat_end_location,
							lng_end_location, duration, distance, modeStep,
							html_instruction);
					steps.add(step);
				}

				// read start address
				String startAdd = null;
				String endAdd = null;
				if (doc.getElementsByTagName("start_address") != null) {
					NodeList nlStartAdd = doc
							.getElementsByTagName("start_address");
					Element e = (Element) nlStartAdd.item(0);
					if (e != null) {
						startAdd = e.getChildNodes().item(0).getNodeValue();
					}

					// read end address
					NodeList nlEndAdd = doc.getElementsByTagName("end_address");
					e = (Element) nlEndAdd.item(0);
					if (e != null) {
						endAdd = e.getChildNodes().item(0).getNodeValue();
		
					}
				}
				
				System.out.println(name() + "::getDirection, distances = " + distances);
				direction = new Direction(steps, startAdd, endAdd, lat1, lng1,
						lat2, lng2, durations, distances, mode);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			input.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return direction;
	}




	
	public static void main(String[] args){
		GoogleMapsQuery G = new GoogleMapsQuery();
		//G.getDirection(21, 105, 21.01, 105, "driving");
		//int t = G.getTravelTime("135 Nguyen Van Cu, Gia Lam, hanoi, vietnam", "45 Nguyen Van Cu, Gia Lam, hanoi, vietnam", "driving");
		
		String p1 = "21.020382, 105.856176";
		String p2 = "21.021849, 105.852131";
		
		double d1 = G.getDistance(p1, p2);
		System.out.print(d1);
		if(true) return;
		
		String src = "An Trach, Cat Linh, Dong Da, Ha Noi, Viet Nam";
		String dest = "My Trung, Nam Dinh, Viet Nam";
		LatLng lls = G.getCoordinate(src);
		LatLng lld = G.getCoordinate(dest);		
				
		lls = new LatLng(21.027708,105.829839);
		lld = new LatLng(20.381863, 106.536314);		
		
		System.out.println("Lat1: " + lls.lat + ", Lon1: " + lls.lng);
		System.out.println("Lat2: " + lld.lat + ", Lon2: " + lld.lng);
		int t = G.getTravelTime(src, dest, "driving");
		
		//int te = G.estimateTravelTimeWithTimeFrame(lls.lat, lls.lng, lld.lat, lld.lng, "driving", "2016-02-02 12:12:00", 10, 3);
		double de = G.estimateDistanceMeter(lls.lat, lls.lng, lld.lat, lld.lng);
		double d = G.getDistance(lls.lat, lls.lng, lld.lat, lld.lng)*1000;
		
		//Direction drt = G.getDirection(lls.lat, lls.lng,  lld.lat, lld.lng, "driving");
		Direction drt = G.getDirection(21.027708, 105.829839, 20.381863, 106.536314, "driving");
		
		System.out.println("Distance: " + drt.getDistances());
		
		System.out.print("\"directItineraries\"" + ":\"");
		ArrayList<StepDirection> lst = drt.getStepsDirection();
		
		System.out.print(lls.lat + "," + lls.lng + ";");
		
		System.out.print("},");
		for(StepDirection sd: lst){
		
			System.out.print(sd.getStartLat() + ",");
			System.out.print(sd.getStartLng() + ";");
			
			
			
		}
		System.out.print(lld.lat + "," + lld.lng + "\"");
		
		//Cung doan lenh do: O day no chay duoc
		GoogleMapsQuery G2 = new GoogleMapsQuery();
		Direction drt2 = G2.getDirection(21.027708, 105.829839, 20.381863, 106.536314, "driving");
		System.out.println("Dis: " + drt2.getDistances());
		
		for(int i = 0; i < 100; i++){
			System.out.println("------------------");
			System.out.println("test: " + G.getDistance("20.6608254,106.3276864", "20.4204865,106.3905338"));	
		}
		
	}
	
}
