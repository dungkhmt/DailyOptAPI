package com.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

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

import routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model.*;
import routingdelivery.model.PickupDeliveryInput;
import routingdelivery.model.PickupDeliveryMultiSolutions;
import routingdelivery.model.PickupDeliveryRequest;
import routingdelivery.model.PickupDeliverySolution;
import routingdelivery.model.Vehicle;
import routingdelivery.service.PickupDeliverySolver;
import routingdelivery.smartlog.brenntag.model.BrennTagPickupDeliveryInput;
import routingdelivery.smartlog.brenntag.service.BrenntagPickupDeliverySolver;
import routingdelivery.smartlog.brenntag.service.RBrennTagPickupDeliverySolver;
import routingdelivery.smartlog.brenntagmultipickupdelivery.service.DistrictBasedRBrenntagMultiPickupDeliverySolver;
import routingdelivery.smartlog.brenntagmultipickupdelivery.service.RBrenntagMultiPickupDeliverySolver;
import routingdelivery.smartlog.containertruckmoocassigment.model.ContainerTruckMoocInput;
import routingdelivery.smartlog.containertruckmoocassigment.model.ContainerTruckMoocSolution;
import routingdelivery.smartlog.containertruckmoocassigment.service.ContainerTruckMoocService;
import routingdelivery.smartlog.containertruckmoocassigment.service.ContainerTruckMoocSolver;
import routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.service.DistrictBasedMultiPickupDeliveryWeightSpaceSolver;
import routingdelivery.smartlog.sem.model.SEMPickupDeliveryInput;
import routingdelivery.smartlog.sem.model.SEMPickupDeliverySolution;
import routingdelivery.smartlog.sem.service.SEMPickupDeliverySolver;
import routingdelivery.smartlog.tracking.model.RouteTrackInput;
import routingdelivery.smartlog.tracking.model.RouteTrackSolution;
import routingdelivery.smartlog.tracking.service.RouteTrackSolver;
import utils.DateTimeUtils;
import weka.core.pmml.jaxbbindings.AssociationModel;

import com.swm.model.*;
import com.swm.service.AssociationRuleMiner;

@RestController
public class TestAPI {
	// public static final String ROOT_DIR = "C:/DungPQ/daily-opt/tmp/";

	public static final String ROOT_DIR = "/home/smartlog/";
	public static final String SECONDARY_ROOT_DIR = "C:/DungPQ/daily-opt/tmp/";
	
	@RequestMapping(value = "/basic", method = RequestMethod.POST)
	public TestSolution getFields(HttpServletRequest request,
			@RequestBody TestInput input) {
		int c = input.getA() + input.getB();
		c = c * 1000;
		return new TestSolution(c);
	}

	public void writeGlobalRequest(SEMPickupDeliveryInput input) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss");
			Date date = new Date();

			// System.out.println(dateFormat.format(date)); //2014/08/06
			// 15:59:48
			String dt = dateFormat.format(date);
			String[] s = dt.split(":");

			String dir = ROOT_DIR + "/logs/sem/" + DateTimeUtils.currentDate();
			File f = new File(dir);
			if (!f.exists()) {
				f.mkdir();
			}

			String fn = dir + "/request-" + s[0] + s[1] + s[2] + "-" + s[3]
					+ s[4] + s[5] + ".txt";
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
	public void writeGlobalRequest(RouteTrackInput input) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss");
			Date date = new Date();

			// System.out.println(dateFormat.format(date)); //2014/08/06
			// 15:59:48
			String dt = dateFormat.format(date);
			String[] s = dt.split(":");

			String dir = ROOT_DIR + "/logs/routetrack/" + DateTimeUtils.currentDate();
			File f = new File(dir);
			if (!f.exists()) {
				f.mkdir();
			}

			String fn = dir + "/request-" + s[0] + s[1] + s[2] + "-" + s[3]
					+ s[4] + s[5] + ".txt";
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

	public void writeGlobalRequest(ContainerTruckMoocInput input) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss");
			Date date = new Date();

			// System.out.println(dateFormat.format(date)); //2014/08/06
			// 15:59:48
			String dt = dateFormat.format(date);
			String[] s = dt.split(":");

			String dir = ROOT_DIR + "/logs/container/" + DateTimeUtils.currentDate();
			File f = new File(dir);
			if (!f.exists()) {
				f.mkdir();
			}

			String fn = dir + "/request-" + s[0] + s[1] + s[2] + "-" + s[3]
					+ s[4] + s[5] + ".txt";
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

	public void writeGlobalRequest(BrennTagPickupDeliveryInput input, String dir_group) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss");
			Date date = new Date();

			// System.out.println(dateFormat.format(date)); //2014/08/06
			// 15:59:48
			String dt = dateFormat.format(date);
			String[] s = dt.split(":");

			String dir = ROOT_DIR + "/logs/" + dir_group + "/" + DateTimeUtils.currentDate();
			File f = new File(dir);
			if (!f.exists()) {
				f.mkdir();
			}

			String fn = dir + "/request-" + s[0] + s[1] + s[2] + "-" + s[3]
					+ s[4] + s[5] + ".txt";
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

	public void writeGlobalRequest(routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model.BrennTagPickupDeliveryInput input, String dir_group) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss");
			Date date = new Date();

			// System.out.println(dateFormat.format(date)); //2014/08/06
			// 15:59:48
			String dt = dateFormat.format(date);
			String[] s = dt.split(":");

			String dir = ROOT_DIR + "/logs/" + dir_group + "/" + DateTimeUtils.currentDate();
			File f = new File(dir);
			if (!f.exists()) {
				f.mkdir();
			}

			String fn = dir + "/request-" + s[0] + s[1] + s[2] + "-" + s[3]
					+ s[4] + s[5] + ".txt";
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
	public PickupDeliverySolution computePickupDeliverySolution(
			HttpServletRequest request,
			@RequestBody BrennTagPickupDeliveryInput input) {

		// Gson gson = new Gson();
		// String json = gson.toJson(input);
		try {
			writeGlobalRequest(input,"brenntagpickupdelivery");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// PickupDeliverySolver solver = new PickupDeliverySolver();
		// BrenntagPickupDeliverySolver solver = new
		// BrenntagPickupDeliverySolver();
		RBrennTagPickupDeliverySolver solver = new RBrennTagPickupDeliverySolver();
		// return solver.compute(input);
		// return solver.computeNew(input);
		//input.setExternalVehicles(null);
		//input.setVehicleCategories(null);
		
		return solver.computeVehicleSuggestion(input);
	}
	

	@CrossOrigin
	@RequestMapping(value = "/compute-sequence-route", method = RequestMethod.POST)
	public PickupDeliverySolution computeSequenceRoute(
			HttpServletRequest request,
			@RequestBody BrennTagPickupDeliveryInput input) {

		// Gson gson = new Gson();
		// String json = gson.toJson(input);
		try {
			 writeGlobalRequest(input,"reorderroute");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// PickupDeliverySolver solver = new PickupDeliverySolver();
		// BrenntagPickupDeliverySolver solver = new
		// BrenntagPickupDeliverySolver();
		RBrennTagPickupDeliverySolver solver = new RBrennTagPickupDeliverySolver();

		// return solver.compute(input);
		// return solver.computeNew(input);
		//return solver.computeSequenceRoute(input);
		return solver.computeSequenceRouteIgnoreConstraints(input);
	}

	@CrossOrigin
	@RequestMapping(value = "/multi-pickup-delivery", method = RequestMethod.POST)
	public PickupDeliverySolution computeMultiPickupDeliverySolution(
			HttpServletRequest request,
			@RequestBody BrennTagPickupDeliveryInput input) {

		// Gson gson = new Gson();
		// String json = gson.toJson(input);
		try {
			writeGlobalRequest(input,"multibrenntagpickupdelivery");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// PickupDeliverySolver solver = new PickupDeliverySolver();
		// BrenntagPickupDeliverySolver solver = new
		// BrenntagPickupDeliverySolver();
		RBrenntagMultiPickupDeliverySolver solver = new RBrenntagMultiPickupDeliverySolver();

		// return solver.compute(input);
		// return solver.computeNew(input);
		
		//if(true)return solver.computeVehicleSuggestion(input);
		
		//solver.CHECK_AND_LOG = false;// set false when deploy to reduce log time
		solver.CHECK_AND_LOG = true;// call check solution and log info, use when debuging
		
		if(input.getParams().getTimeLimit() == 0)
			input.getParams().setTimeLimit(10);
		
		if(input.getParams().getInternalVehicleFirst() != null && 
				input.getParams().getInternalVehicleFirst().equals("Y")){
			Vehicle[] externalVehicles = input.getExternalVehicles();
			Vehicle[] vehicleCategory = input.getVehicleCategories();
			PickupDeliveryRequest[] req = input.cloneRequests();
			input.setVehicleCategories(null);
			input.setExternalVehicles(null);
			PickupDeliverySolution s = solver.computeVehicleSuggestion(input);
			if(s.getDescription().equals("OK")){
				return s;
			}
			else{// try to use external vehicles
				input.setExternalVehicles(externalVehicles);
				input.setVehicleCategories(vehicleCategory);
				input.setRequests(req);
				return solver.computeVehicleSuggestion(input);
			}
		}else{
			return solver.computeVehicleSuggestion(input);
		}
		//return solver.computeVehicleSuggestion(input);
	}

	@CrossOrigin
	@RequestMapping(value = "/multi-pickup-delivery-multi-solutions", method = RequestMethod.POST)
	public PickupDeliveryMultiSolutions computeMultiPickupDeliveryMultiSolution(
			HttpServletRequest request,
			@RequestBody BrennTagPickupDeliveryInput input) {

		// Gson gson = new Gson();
		// String json = gson.toJson(input);
		try {
			writeGlobalRequest(input,"multibrenntagpickupdelivery");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// PickupDeliverySolver solver = new PickupDeliverySolver();
		// BrenntagPickupDeliverySolver solver = new
		// BrenntagPickupDeliverySolver();
		
		//RBrenntagMultiPickupDeliverySolver solver = new RBrenntagMultiPickupDeliverySolver();
		
		
		
		// return solver.compute(input);
		// return solver.computeNew(input);
		
		//if(true)return solver.computeVehicleSuggestion(input);
		
		
		//solver.CHECK_AND_LOG = false;// set false when deploy to reduce log time
		//solver.CHECK_AND_LOG = true;// call check solution and log info, use when debuging
		
		PickupDeliveryMultiSolutions ms = new PickupDeliveryMultiSolutions();
		long startExecutionTime = System.currentTimeMillis();
		
		if(input.getParams().getTimeLimit() == 0)
			input.getParams().setTimeLimit(10);
		
		PickupDeliveryRequest[] origin_req = input.cloneRequests();
		
		if(input.getParams().getDistrictBased() != null)if(input.getParams().getDistrictBased().equals("Y")){
			
			DistrictBasedRBrenntagMultiPickupDeliverySolver dsolver = 
					new DistrictBasedRBrenntagMultiPickupDeliverySolver(startExecutionTime);
			
			//dsolver.CHECK_AND_LOG = false;// set false when deploy to reduce log time
			dsolver.CHECK_AND_LOG = true;// call check solution and log info, use when debuging

			if(input.getParams().getInternalVehicleFirst() != null && 
					input.getParams().getInternalVehicleFirst().equals("Y")){
				Vehicle[] externalVehicles = input.getExternalVehicles();
				Vehicle[] vehicleCategory = input.getVehicleCategories();
				PickupDeliveryRequest[] dreq = input.cloneRequests();
				input.setVehicleCategories(null);
				input.setExternalVehicles(null);
				PickupDeliverySolution s = dsolver.computeVehicleSuggestion(input);
				if(s.getDescription().equals("OK")){
					PickupDeliverySolution[] solutions = dsolver.collectSolutions();
					ms.addSolutions(solutions);
					//PickupDeliveryMultiSolutions ms = new PickupDeliveryMultiSolutions(solutions);
					//return ms;
				}
				else{// try to use external vehicles
					input.setExternalVehicles(externalVehicles);
					input.setVehicleCategories(vehicleCategory);
					input.setRequests(dreq);
					//PickupDeliveryRequest[] req1 = input.cloneRequests();
					PickupDeliverySolution sol = dsolver.computeVehicleSuggestion(input);
					PickupDeliverySolution[] solutions = dsolver.collectSolutions();
					ms.addSolutions(solutions);
					//PickupDeliveryMultiSolutions ms = new PickupDeliveryMultiSolutions(solutions);
					//return ms;
				}
			}else{
				PickupDeliverySolution sol = dsolver.computeVehicleSuggestion(input);
				PickupDeliverySolution[] solutions = dsolver.collectSolutions();
				ms.addSolutions(solutions);
				//PickupDeliveryMultiSolutions ms = new PickupDeliveryMultiSolutions(solutions);
				//return ms;
			}
			
			if(ms.getSolutions() != null){
				for(int ii = 0; ii < ms.getSolutions().length; ii++){
					PickupDeliverySolution sol = ms.getSolutions()[ii];
					sol.setDescription(sol.getDescription() + " - " + " GOM THEO KHU VUC");
				}
					
				
			}
			ms.sortIncreasingOrderDistance();
			return ms;
		}
		
		
		RBrenntagMultiPickupDeliverySolver solver = new RBrenntagMultiPickupDeliverySolver(startExecutionTime);
		solver.CHECK_AND_LOG = false;// set false when deploy to reduce log time
		//solver.CHECK_AND_LOG = true;// call check solution and log info, use when debuging
		
		input.setRequests(origin_req);
		
		if(input.getParams().getInternalVehicleFirst() != null && 
				input.getParams().getInternalVehicleFirst().equals("Y")){
			Vehicle[] externalVehicles = input.getExternalVehicles();
			Vehicle[] vehicleCategory = input.getVehicleCategories();
			PickupDeliveryRequest[] req = input.cloneRequests();
			input.setVehicleCategories(null);
			input.setExternalVehicles(null);
			PickupDeliverySolution s = solver.computeVehicleSuggestion(input);
			if(s.getDescription().equals("OK")){
				PickupDeliverySolution[] solutions = solver.collectSolutions();
				ms.addSolutions(solutions);
				//PickupDeliveryMultiSolutions ms = new PickupDeliveryMultiSolutions(solutions);
				//return ms;
			}
			else{// try to use external vehicles
				input.setExternalVehicles(externalVehicles);
				input.setVehicleCategories(vehicleCategory);
				input.setRequests(req);
				PickupDeliveryRequest[] req1 = input.cloneRequests();
				PickupDeliverySolution sol = solver.computeVehicleSuggestion(input);
				PickupDeliverySolution[] solutions = solver.collectSolutions();
				ms.addSolutions(solutions);
				//PickupDeliveryMultiSolutions ms = new PickupDeliveryMultiSolutions(solutions);
				//return ms;
				/*
				if(input.getParams().getExtendLateDelivery() == 0 && input.getParams().getExtendCapacity() == 0){
					return ms;
				}
				
				ArrayList<PickupDeliverySolution> L = new ArrayList<PickupDeliverySolution>();
				for(int i = 0; i < solutions.length; i++) L.add(solutions[i]);
				
				for(int i = 0; i < input.getVehicles().length; i++){
					Vehicle vh = input.getVehicles()[i];
					vh.setWeight(vh.getWeight() + input.getParams().getExtendCapacity());
				}
				for(int i = 0; i < input.getVehicleCategories().length; i++){
					Vehicle vh = input.getVehicleCategories()[i];
					vh.setWeight(vh.getWeight() + input.getParams().getExtendCapacity());
				}
				
				input.setRequests(req1);
				for(int i = 0; i < input.getRequests().length; i++){
					PickupDeliveryRequest r = input.getRequests()[i];
					r.extendLateDelivery(input.getParams().getExtendLateDelivery());
				}
				
				sol = solver.computeVehicleSuggestion(input);
				PickupDeliverySolution[] ext_solutions = solver.collectSolutions();
				for(int i = 0; i < ext_solutions.length; i++){
					ext_solutions[i].setDescription(ext_solutions[i].getDescription() + " EXTEND");
				}
				for(int i = 0; i < ext_solutions.length; i++) L.add(ext_solutions[i]);
				
				PickupDeliverySolution[] final_solutions = new PickupDeliverySolution[L.size()];
				for(int i = 0; i < final_solutions.length; i++) final_solutions[i] = L.get(i);
				
				ms = new PickupDeliveryMultiSolutions(final_solutions);
				
				return ms;
				*/
			}
		}else{
			PickupDeliverySolution sol = solver.computeVehicleSuggestion(input);
			PickupDeliverySolution[] solutions = solver.collectSolutions();
			ms.addSolutions(solutions);
			//PickupDeliveryMultiSolutions ms = new PickupDeliveryMultiSolutions(solutions);
			//return ms;
			/*
			if(input.getParams().getExtendLateDelivery() == 0 && input.getParams().getExtendCapacity() == 0){
				return ms;
			}
			
			ArrayList<PickupDeliverySolution> L = new ArrayList<PickupDeliverySolution>();
			for(int i = 0; i < solutions.length; i++) L.add(solutions[i]);
			
			for(int i = 0; i < input.getRequests().length; i++){
				PickupDeliveryRequest r = input.getRequests()[i];
				r.extendLateDelivery(input.getParams().getExtendLateDelivery());
			}
			sol = solver.computeVehicleSuggestion(input);
			PickupDeliverySolution[] ext_solutions = solver.collectSolutions();
			for(int i = 0; i < ext_solutions.length; i++) L.add(ext_solutions[i]);
			
			PickupDeliverySolution[] final_solutions = new PickupDeliverySolution[L.size()];
			for(int i = 0; i < final_solutions.length; i++) final_solutions[i] = L.get(i);
			
			ms = new PickupDeliveryMultiSolutions(final_solutions);
			return ms;
			*/
		}
		ms.sortIncreasingOrderDistance();
		return ms;
		//return solver.computeVehicleSuggestion(input);
	}

	@CrossOrigin
	@RequestMapping(value = "/multi-pickup-delivery-multi-solutions-weightspace-constraint", method = RequestMethod.POST)
	public routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model.PickupDeliveryMultiSolutions computeMultiPickupDeliveryMultiSolutionWeightSpaceContraint(
			HttpServletRequest request,
			@RequestBody routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model.BrennTagPickupDeliveryInput input) {

		// Gson gson = new Gson();
		// String json = gson.toJson(input);
		try {
			writeGlobalRequest(input,"multibrenntagpickupdelivery");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// PickupDeliverySolver solver = new PickupDeliverySolver();
		// BrenntagPickupDeliverySolver solver = new
		// BrenntagPickupDeliverySolver();
		
		//RBrenntagMultiPickupDeliverySolver solver = new RBrenntagMultiPickupDeliverySolver();
		
		
		
		// return solver.compute(input);
		// return solver.computeNew(input);
		
		//if(true)return solver.computeVehicleSuggestion(input);
		
		
		//solver.CHECK_AND_LOG = false;// set false when deploy to reduce log time
		//solver.CHECK_AND_LOG = true;// call check solution and log info, use when debuging
		
		routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model.PickupDeliveryMultiSolutions ms = 
				new routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model.PickupDeliveryMultiSolutions();
		long startExecutionTime = System.currentTimeMillis();
		
		if(input.getParams().getTimeLimit() == 0)
			input.getParams().setTimeLimit(10);
		
		routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model.PickupDeliveryRequest[] origin_req = input.cloneRequests();
		
		if(input.getParams().getDistrictBased() != null)if(input.getParams().getDistrictBased().equals("Y")){
			
			DistrictBasedMultiPickupDeliveryWeightSpaceSolver dsolver = 
					new DistrictBasedMultiPickupDeliveryWeightSpaceSolver(startExecutionTime);
			
			//dsolver.CHECK_AND_LOG = false;// set false when deploy to reduce log time
			dsolver.CHECK_AND_LOG = true;// call check solution and log info, use when debuging

			if(input.getParams().getInternalVehicleFirst() != null && 
					input.getParams().getInternalVehicleFirst().equals("Y")){
				routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model.Vehicle[] externalVehicles = input.getExternalVehicles();
				routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model.Vehicle[] vehicleCategory = input.getVehicleCategories();
				routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model.PickupDeliveryRequest[] dreq = input.cloneRequests();
				input.setVehicleCategories(null);
				input.setExternalVehicles(null);
				routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model.PickupDeliverySolution s = dsolver.computeVehicleSuggestion(input);
				if(s.getDescription().equals("OK")){
					routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model.PickupDeliverySolution[] solutions = dsolver.collectSolutions();
					ms.addSolutions(solutions);
					//PickupDeliveryMultiSolutions ms = new PickupDeliveryMultiSolutions(solutions);
					//return ms;
				}
				else{// try to use external vehicles
					input.setExternalVehicles(externalVehicles);
					input.setVehicleCategories(vehicleCategory);
					input.setRequests(dreq);
					//PickupDeliveryRequest[] req1 = input.cloneRequests();
					routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model.PickupDeliverySolution sol = dsolver.computeVehicleSuggestion(input);
					routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model.PickupDeliverySolution[] solutions = dsolver.collectSolutions();
					ms.addSolutions(solutions);
					//PickupDeliveryMultiSolutions ms = new PickupDeliveryMultiSolutions(solutions);
					//return ms;
				}
			}else{
				routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model.PickupDeliverySolution sol = dsolver.computeVehicleSuggestion(input);
				routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model.PickupDeliverySolution[] solutions = dsolver.collectSolutions();
				ms.addSolutions(solutions);
				//PickupDeliveryMultiSolutions ms = new PickupDeliveryMultiSolutions(solutions);
				//return ms;
			}
			
			if(ms.getSolutions() != null){
				for(int ii = 0; ii < ms.getSolutions().length; ii++){
					routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model.PickupDeliverySolution sol = ms.getSolutions()[ii];
					sol.setDescription(sol.getDescription() + " - " + " GOM THEO KHU VUC");
				}
					
				
			}
			ms.sortIncreasingOrderDistance();
			return ms;
		}
		
		
		routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.service.RBrenntagMultiPickupDeliverySolver solver = 
				new routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.service.RBrenntagMultiPickupDeliverySolver(startExecutionTime);
		solver.CHECK_AND_LOG = false;// set false when deploy to reduce log time
		//solver.CHECK_AND_LOG = true;// call check solution and log info, use when debuging
		
		input.setRequests(origin_req);
		
		if(input.getParams().getInternalVehicleFirst() != null && 
				input.getParams().getInternalVehicleFirst().equals("Y")){
			routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model.Vehicle[] externalVehicles = input.getExternalVehicles();
			routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model.Vehicle[] vehicleCategory = input.getVehicleCategories();
			routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model.PickupDeliveryRequest[] req = input.cloneRequests();
			input.setVehicleCategories(null);
			input.setExternalVehicles(null);
			routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model.PickupDeliverySolution s = solver.computeVehicleSuggestion(input);
			if(s.getDescription().equals("OK")){
				routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model.PickupDeliverySolution[] solutions = solver.collectSolutions();
				ms.addSolutions(solutions);
				//PickupDeliveryMultiSolutions ms = new PickupDeliveryMultiSolutions(solutions);
				//return ms;
			}
			else{// try to use external vehicles
				input.setExternalVehicles(externalVehicles);
				input.setVehicleCategories(vehicleCategory);
				input.setRequests(req);
				routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model.PickupDeliveryRequest[] req1 = input.cloneRequests();
				routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model.PickupDeliverySolution sol = solver.computeVehicleSuggestion(input);
				routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model.PickupDeliverySolution[] solutions = solver.collectSolutions();
				ms.addSolutions(solutions);
				//PickupDeliveryMultiSolutions ms = new PickupDeliveryMultiSolutions(solutions);
				//return ms;
				/*
				if(input.getParams().getExtendLateDelivery() == 0 && input.getParams().getExtendCapacity() == 0){
					return ms;
				}
				
				ArrayList<PickupDeliverySolution> L = new ArrayList<PickupDeliverySolution>();
				for(int i = 0; i < solutions.length; i++) L.add(solutions[i]);
				
				for(int i = 0; i < input.getVehicles().length; i++){
					Vehicle vh = input.getVehicles()[i];
					vh.setWeight(vh.getWeight() + input.getParams().getExtendCapacity());
				}
				for(int i = 0; i < input.getVehicleCategories().length; i++){
					Vehicle vh = input.getVehicleCategories()[i];
					vh.setWeight(vh.getWeight() + input.getParams().getExtendCapacity());
				}
				
				input.setRequests(req1);
				for(int i = 0; i < input.getRequests().length; i++){
					PickupDeliveryRequest r = input.getRequests()[i];
					r.extendLateDelivery(input.getParams().getExtendLateDelivery());
				}
				
				sol = solver.computeVehicleSuggestion(input);
				PickupDeliverySolution[] ext_solutions = solver.collectSolutions();
				for(int i = 0; i < ext_solutions.length; i++){
					ext_solutions[i].setDescription(ext_solutions[i].getDescription() + " EXTEND");
				}
				for(int i = 0; i < ext_solutions.length; i++) L.add(ext_solutions[i]);
				
				PickupDeliverySolution[] final_solutions = new PickupDeliverySolution[L.size()];
				for(int i = 0; i < final_solutions.length; i++) final_solutions[i] = L.get(i);
				
				ms = new PickupDeliveryMultiSolutions(final_solutions);
				
				return ms;
				*/
			}
		}else{
			routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model.PickupDeliverySolution sol = solver.computeVehicleSuggestion(input);
			routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model.PickupDeliverySolution[] solutions = solver.collectSolutions();
			ms.addSolutions(solutions);
			//PickupDeliveryMultiSolutions ms = new PickupDeliveryMultiSolutions(solutions);
			//return ms;
			/*
			if(input.getParams().getExtendLateDelivery() == 0 && input.getParams().getExtendCapacity() == 0){
				return ms;
			}
			
			ArrayList<PickupDeliverySolution> L = new ArrayList<PickupDeliverySolution>();
			for(int i = 0; i < solutions.length; i++) L.add(solutions[i]);
			
			for(int i = 0; i < input.getRequests().length; i++){
				PickupDeliveryRequest r = input.getRequests()[i];
				r.extendLateDelivery(input.getParams().getExtendLateDelivery());
			}
			sol = solver.computeVehicleSuggestion(input);
			PickupDeliverySolution[] ext_solutions = solver.collectSolutions();
			for(int i = 0; i < ext_solutions.length; i++) L.add(ext_solutions[i]);
			
			PickupDeliverySolution[] final_solutions = new PickupDeliverySolution[L.size()];
			for(int i = 0; i < final_solutions.length; i++) final_solutions[i] = L.get(i);
			
			ms = new PickupDeliveryMultiSolutions(final_solutions);
			return ms;
			*/
		}
		ms.sortIncreasingOrderDistance();
		return ms;
		//return solver.computeVehicleSuggestion(input);
	}

	@CrossOrigin
	@RequestMapping(value = "/sem-pickup-delivery", method = RequestMethod.POST)
	public SEMPickupDeliverySolution computeSEMPickupDeliverySolution(
			HttpServletRequest request,
			@RequestBody SEMPickupDeliveryInput input) {

		// Gson gson = new Gson();
		// String json = gson.toJson(input);
		try {
			writeGlobalRequest(input);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// PickupDeliverySolver solver = new PickupDeliverySolver();
		SEMPickupDeliverySolver solver = new SEMPickupDeliverySolver();
		// return solver.compute(input);
		return solver.compute(input);

	}

	@CrossOrigin
	@RequestMapping(value = "/container", method = RequestMethod.POST)
	public ContainerTruckMoocSolution computeContainerSolution(
			HttpServletRequest request,
			@RequestBody ContainerTruckMoocInput input) {

		// Gson gson = new Gson();
		// String json = gson.toJson(input);
		try {
			writeGlobalRequest(input);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// PickupDeliverySolver solver = new PickupDeliverySolver();
		// ContainerTruckMoocSolver solver = new ContainerTruckMoocSolver();
		// return solver.compute(input);
		ContainerTruckMoocService service = new ContainerTruckMoocService();
		return service.solve(input);
		

	}
	@CrossOrigin
	@RequestMapping(value = "/route-track", method = RequestMethod.POST)
	public RouteTrackSolution routeTrack(HttpServletRequest request,
			@RequestBody RouteTrackInput input) {
		try {
			writeGlobalRequest(input);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		RouteTrackSolver solver = new RouteTrackSolver();
		return solver.evaluate(input);
	}
	
	@CrossOrigin
	@RequestMapping(value = "/swm-store-data", method = RequestMethod.POST)
	public StoreDataOuput storeTransactionData(HttpServletRequest request,
			@RequestBody StoreDataInput input) {

		AssociationRuleMiner miner = new AssociationRuleMiner();
		return miner.storeDB(input);

	}

	@CrossOrigin
	@RequestMapping(value = "/swm-get-transaction-data", method = RequestMethod.POST)
	public GetTransactionOutput getTransactionData(HttpServletRequest request,
			@RequestBody GetTransactionInput input) {

		AssociationRuleMiner miner = new AssociationRuleMiner();

		try {
			Solution sol = miner.getAssociationRule(input.getDbName(),
					input.getParams().getMinSupport(), input.getParams()
							.getMinConfidence());

			int ruleNum = sol.getRuleNum();
			double support = sol.getSupport();
			double confidence = sol.getConfidence();
			Rule[] rules = new Rule[sol.getRuleLst().size()];
			for (int i = 0; i < sol.getRuleLst().size(); i++)
				rules[i] = sol.getRuleLst().get(i);

			GetTransactionOutput out = new GetTransactionOutput(ruleNum, support, confidence, rules);
			out.setMsg("OK");
			return out;
		} catch (Exception ex) {
			ex.printStackTrace();
			GetTransactionOutput out = new GetTransactionOutput();
			out.setMsg("KO");
			return out;
		}
	}

	public void computeMultiPickupDeliveryMultiSolution(String jsonfilename){
		try{
			BrennTagPickupDeliveryInput input = null;
			//Scanner in = new Scanner(new File(jsonfilename));
			Gson gson = new Gson();
			BufferedReader in = new BufferedReader(new FileReader(jsonfilename));
			input = gson.fromJson(in, BrennTagPickupDeliveryInput.class);
			

			RBrenntagMultiPickupDeliverySolver solver = new RBrenntagMultiPickupDeliverySolver();

			// return solver.compute(input);
			// return solver.computeNew(input);
			
			//if(true)return solver.computeVehicleSuggestion(input);
			
			//solver.CHECK_AND_LOG = false;// set false when deploy to reduce log time
			solver.CHECK_AND_LOG = true;// call check solution and log info, use when debuging
			
			if(input.getParams().getTimeLimit() == 0)
				input.getParams().setTimeLimit(10);
			
			if(input.getParams().getInternalVehicleFirst() != null && 
					input.getParams().getInternalVehicleFirst().equals("Y")){
				Vehicle[] externalVehicles = input.getExternalVehicles();
				Vehicle[] vehicleCategory = input.getVehicleCategories();
				PickupDeliveryRequest[] req = input.cloneRequests();
				input.setVehicleCategories(null);
				input.setExternalVehicles(null);
				PickupDeliverySolution s = solver.computeVehicleSuggestion(input);
				if(s.getDescription().equals("OK")){
					PickupDeliverySolution[] solutions = solver.collectSolutions();
					PickupDeliveryMultiSolutions ms = new PickupDeliveryMultiSolutions(solutions);
					return;
				}
				else{// try to use external vehicles
					input.setExternalVehicles(externalVehicles);
					input.setVehicleCategories(vehicleCategory);
					input.setRequests(req);
					PickupDeliveryRequest[] req1 = input.cloneRequests();
					PickupDeliverySolution sol = solver.computeVehicleSuggestion(input);
					PickupDeliverySolution[] solutions = solver.collectSolutions();
					for(int i = 0; i < solutions.length; i++){
						solutions[i].setDescription(solutions[i].getDescription() + " ORIGINAL");
					}
					PickupDeliveryMultiSolutions ms = new PickupDeliveryMultiSolutions(solutions);
					if(input.getParams().getExtendLateDelivery() == 0 && input.getParams().getExtendCapacity() == 0){
						return;
					}
					
					ArrayList<PickupDeliverySolution> L = new ArrayList<PickupDeliverySolution>();
					for(int i = 0; i < solutions.length; i++) L.add(solutions[i]);
					
					for(int i = 0; i < input.getVehicles().length; i++){
						Vehicle vh = input.getVehicles()[i];
						vh.setWeight(vh.getWeight() + input.getParams().getExtendCapacity());
					}
					for(int i = 0; i < input.getVehicleCategories().length; i++){
						Vehicle vh = input.getVehicleCategories()[i];
						vh.setWeight(vh.getWeight() + input.getParams().getExtendCapacity());
					}
					
					input.setRequests(req1);
					for(int i = 0; i < input.getRequests().length; i++){
						PickupDeliveryRequest r = input.getRequests()[i];
						r.extendLateDelivery(input.getParams().getExtendLateDelivery());
					}
					
					sol = solver.computeVehicleSuggestion(input);
					PickupDeliverySolution[] ext_solutions = solver.collectSolutions();
					for(int i = 0; i < ext_solutions.length; i++){
						ext_solutions[i].setDescription(ext_solutions[i].getDescription() + " EXTEND");
					}
					for(int i = 0; i < ext_solutions.length; i++) L.add(ext_solutions[i]);
					
					PickupDeliverySolution[] final_solutions = new PickupDeliverySolution[L.size()];
					for(int i = 0; i < final_solutions.length; i++) final_solutions[i] = L.get(i);
					
					ms = new PickupDeliveryMultiSolutions(final_solutions);
					
					return;
				}
			}else{
				PickupDeliverySolution sol = solver.computeVehicleSuggestion(input);
				PickupDeliverySolution[] solutions = solver.collectSolutions();
				PickupDeliveryMultiSolutions ms = new PickupDeliveryMultiSolutions(solutions);
				if(input.getParams().getExtendLateDelivery() == 0 && input.getParams().getExtendCapacity() == 0){
					return;
				}
				
				ArrayList<PickupDeliverySolution> L = new ArrayList<PickupDeliverySolution>();
				for(int i = 0; i < solutions.length; i++) L.add(solutions[i]);
				
				for(int i = 0; i < input.getRequests().length; i++){
					PickupDeliveryRequest r = input.getRequests()[i];
					r.extendLateDelivery(input.getParams().getExtendLateDelivery());
				}
				sol = solver.computeVehicleSuggestion(input);
				PickupDeliverySolution[] ext_solutions = solver.collectSolutions();
				for(int i = 0; i < ext_solutions.length; i++) L.add(ext_solutions[i]);
				
				PickupDeliverySolution[] final_solutions = new PickupDeliverySolution[L.size()];
				for(int i = 0; i < final_solutions.length; i++) final_solutions[i] = L.get(i);
				
				ms = new PickupDeliveryMultiSolutions(final_solutions);
				return;
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
			
	public static void main(String[] args){
		System.out.println("hello");
		TestAPI T =new TestAPI();
		T.computeMultiPickupDeliveryMultiSolution("C:/DungPQ/daily-opt/projects/tmp/request.txt");
	}

}
