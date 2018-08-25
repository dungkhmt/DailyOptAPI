package com.test;

import java.io.File;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
























import org.springframework.web.bind.annotation.CrossOrigin;
//import org.apache.log4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import routingdelivery.model.PickupDeliveryInput;
import routingdelivery.model.PickupDeliverySolution;
import routingdelivery.service.PickupDeliverySolver;
import routingdelivery.smartlog.brenntag.model.BrennTagPickupDeliveryInput;
import routingdelivery.smartlog.brenntag.service.BrenntagPickupDeliverySolver;
import routingdelivery.smartlog.brenntag.service.RBrennTagPickupDeliverySolver;
import routingdelivery.smartlog.brenntagmultipickupdelivery.service.RBrenntagMultiPickupDeliverySolver;
import routingdelivery.smartlog.containertruckmoocassigment.model.ContainerTruckMoocInput;
import routingdelivery.smartlog.containertruckmoocassigment.model.ContainerTruckMoocSolution;
import routingdelivery.smartlog.containertruckmoocassigment.service.ContainerTruckMoocService;
import routingdelivery.smartlog.containertruckmoocassigment.service.ContainerTruckMoocSolver;
import routingdelivery.smartlog.sem.model.SEMPickupDeliveryInput;
import routingdelivery.smartlog.sem.model.SEMPickupDeliverySolution;
import routingdelivery.smartlog.sem.service.SEMPickupDeliverySolver;
import utils.DateTimeUtils;


@RestController
public class TestAPI {
	public static final String ROOT_DIR = "C:/DungPQ/daily-opt/tmp/";
	//public static final String ROOT_DIR = "/root/projects/smartlog/logs/";
	@RequestMapping(value = "/basic", method = RequestMethod.POST)
	public TestSolution getFields(HttpServletRequest request, @RequestBody TestInput input) {
		int c = input.getA() + input.getB();
		c = c * 1000;
		return new TestSolution(c);
	}
	
	public void writeGlobalRequest(BrennTagPickupDeliveryInput input) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss");
			Date date = new Date();

			// System.out.println(dateFormat.format(date)); //2014/08/06
			// 15:59:48
			String dt = dateFormat.format(date);
			String[] s = dt.split(":");

			String dir = ROOT_DIR + "/" + DateTimeUtils.currentDate();
			File f = new File(dir);
			if (!f.exists()) {
				f.mkdir();
			}

			String fn = dir + "/smart-log" + s[0] + s[1] + s[2]
					+ "-" + s[3] + s[4] + s[5] + ".txt";
			PrintWriter out = new PrintWriter(fn);

			ObjectMapper mapper = new ObjectMapper();
			String jsoninput = mapper.writeValueAsString(input);

			// out.println("input: JSON");
			out.println(jsoninput);

			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@CrossOrigin
	@RequestMapping(value = "/pickup-delivery", method = RequestMethod.POST)
	public PickupDeliverySolution computePickupDeliverySolution(HttpServletRequest request, 
			@RequestBody BrennTagPickupDeliveryInput input){
		
		//Gson gson = new Gson();
		//String json = gson.toJson(input);
		try{
			writeGlobalRequest(input);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		//PickupDeliverySolver solver = new PickupDeliverySolver();
		//BrenntagPickupDeliverySolver solver = new BrenntagPickupDeliverySolver();
		RBrennTagPickupDeliverySolver solver = new RBrennTagPickupDeliverySolver();
		
		//return solver.compute(input);
		//return solver.computeNew(input);
		return solver.computeVehicleSuggestion(input);
	}

	@CrossOrigin
	@RequestMapping(value = "/multi-pickup-delivery", method = RequestMethod.POST)
	public PickupDeliverySolution computeMultiPickupDeliverySolution(HttpServletRequest request, 
			@RequestBody BrennTagPickupDeliveryInput input){
		
		//Gson gson = new Gson();
		//String json = gson.toJson(input);
		try{
			writeGlobalRequest(input);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		//PickupDeliverySolver solver = new PickupDeliverySolver();
		//BrenntagPickupDeliverySolver solver = new BrenntagPickupDeliverySolver();
		RBrenntagMultiPickupDeliverySolver solver = new RBrenntagMultiPickupDeliverySolver();
		
		//return solver.compute(input);
		//return solver.computeNew(input);
		return solver.computeVehicleSuggestion(input);
	}

	@CrossOrigin
	@RequestMapping(value = "/sem-pickup-delivery", method = RequestMethod.POST)
	public SEMPickupDeliverySolution computeSEMPickupDeliverySolution(HttpServletRequest request, 
			@RequestBody SEMPickupDeliveryInput input){
		
		//Gson gson = new Gson();
		//String json = gson.toJson(input);
		try{
			//writeGlobalRequest(input);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		//PickupDeliverySolver solver = new PickupDeliverySolver();
		SEMPickupDeliverySolver solver = new SEMPickupDeliverySolver();
		//return solver.compute(input);
		return solver.compute(input);
		
	}
	
	@CrossOrigin
	@RequestMapping(value = "/container", method = RequestMethod.POST)
	public ContainerTruckMoocSolution computeContainerSolution(HttpServletRequest request, 
			@RequestBody ContainerTruckMoocInput input){
		
		//Gson gson = new Gson();
		//String json = gson.toJson(input);
		try{
			//writeGlobalRequest(input);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		//PickupDeliverySolver solver = new PickupDeliverySolver();
		//ContainerTruckMoocSolver solver = new ContainerTruckMoocSolver();
		//return solver.compute(input);
		ContainerTruckMoocService service = new ContainerTruckMoocService();
		return service.solve(input);
		
	}

	
}
