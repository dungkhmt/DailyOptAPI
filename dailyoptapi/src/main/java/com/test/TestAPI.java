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
import utils.DateTimeUtils;


@RestController
public class TestAPI {
	public static final String ROOT_DIR = "C:/DungPQ/daily-opt/tmp/";
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
		BrenntagPickupDeliverySolver solver = new BrenntagPickupDeliverySolver();
		//return solver.compute(input);
		return solver.computeNew(input);
		
	}
	
}
