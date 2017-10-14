package havestplanning.controller;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import havestplanning.model.Field;
import havestplanning.model.FieldCodeList;
import havestplanning.model.FieldList;
import havestplanning.model.FieldSolutionList;
import havestplanning.model.HavestPlanningInput;
import havestplanning.model.HavestPlanningSolution;
import havestplanning.model.InputAnalysisInfo;
import havestplanning.model.MachineSetting;
import havestplanning.model.PlantStandard;
import havestplanning.model.PlantStandardElement;
import havestplanning.model.ReturnAddFields;
import havestplanning.model.ReturnFields;
import havestplanning.model.ReturnMachineSetting;
import havestplanning.model.ReturnPlantStandard;
import havestplanning.model.ReturnSetPlantStandard;
import havestplanning.model.ReturnStart;
import havestplanning.model.RunParameters;
import havestplanning.model.vn.FieldListVN;
import havestplanning.model.vn.FieldVN;
import havestplanning.model.vn.HavestPlanningInputVN;
import havestplanning.model.vn.HavestPlanningSolutionVN;
import havestplanning.model.vn.MachineSettingVN;
import havestplanning.model.vn.PlantStandardVN;
import havestplanning.model.vn.ReturnAddFieldsVN;
import havestplanning.model.vn.ReturnSetPlantStandardVN;
import havestplanning.solver.Solver;
import havestplanning.solver.multistepsplitfield.SolutionChecker;
import havestplanning.solver.multistepsplitfield.SolverMultiStepSplitFields;

import com.fasterxml.jackson.core.JsonParser;
import com.google.gson.Gson;

@RestController
public class HavestPlanningController {
	public static String ROOT = "C:/ezRoutingAPIROOT/havestplanning";

	public String name() {
		return "HavestPlanningController";
	}

	/*
	@RequestMapping(value = "/havest-plan", method = RequestMethod.POST)
	public HavestPlanningSolution computeHavestPlanningSolution(
			HttpServletRequest request, @RequestBody HavestPlanningInput input) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");
		System.out.println(name() + "::computeVRP3DSolution, path = " + path);

		// Solver solver = new Solver();
		SolverMultiStepSplitFields solver = new SolverMultiStepSplitFields();

		if (input.getPlantStandard() == null)
			input.initDefaultPlantStandard();

		return solver.solve(input);
	}
	*/
	@RequestMapping(value = "/havest-plan/start", method = RequestMethod.POST)
	public ReturnStart start(HttpServletRequest request,
			@RequestBody HavestPlanningInput input) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		return null;
	}

	@RequestMapping(value = "/havest-plan/get-fields", method = RequestMethod.POST)
	public ReturnFields getFields(HttpServletRequest request) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		// path = "C:/ezRoutingAPIROOT/havestplanning/fields.json";
		path = ROOT + "/fields.json";
		try {
			String fieldFilename = path;
			Gson gson = new Gson();

			FieldList FL = gson.fromJson(new FileReader(fieldFilename),
					FieldList.class);

			System.out.println(name() + "::getFields, fieldList.sz = "
					+ FL.getFields().length);



			return new ReturnFields(FL.getFields().length,
					"successful", FL);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@RequestMapping(value = "/havest-plan/get-plant-standard", method = RequestMethod.POST)
	public ReturnPlantStandard getPlantStandard(HttpServletRequest request) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		// path = "C:/ezRoutingAPIROOT/havestplanning/fields.json";
		path = ROOT + "/plant-standard.json";
		try {
			String fieldFilename = path;
			Gson gson = new Gson();

			PlantStandard ps = gson.fromJson(new FileReader(fieldFilename),
					PlantStandard.class);

			System.out.println(name() + "::getPlantStandard, ps = " + ps.toString());



			return new ReturnPlantStandard("successful", ps);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@RequestMapping(value = "/havest-plan/get-machine-setting", method = RequestMethod.POST)
	public ReturnMachineSetting getMachineSetting(HttpServletRequest request) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		// path = "C:/ezRoutingAPIROOT/havestplanning/fields.json";
		path = ROOT + "/machine-setting.json";
		try {
			String fieldFilename = path;
			Gson gson = new Gson();

			MachineSetting ms = gson.fromJson(new FileReader(fieldFilename),
					MachineSetting.class);

			System.out.println(name() + "::getMachineSetting, machine setting = "
					+ ms.toString());



			return new ReturnMachineSetting("successful", ms);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@RequestMapping(value = "/havest-plan/get-solution", method = RequestMethod.POST)
	public HavestPlanningSolution getSolution(HttpServletRequest request) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		// path = "C:/ezRoutingAPIROOT/havestplanning/fields.json";
		path = ROOT + "/harvest-plan-solution.json";
		try {
			String fieldFilename = path;
			Gson gson = new Gson();

			HavestPlanningSolution sol = gson.fromJson(new FileReader(fieldFilename),
					HavestPlanningSolution.class);

			System.out.println(name() + "::getSolution, solution = "
					+ sol.toString());

			sol.sort();

			return sol;
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	@RequestMapping(value = "/havest-plan/get-solution-vn", method = RequestMethod.POST)
	public HavestPlanningSolutionVN getSolutionVN(HttpServletRequest request) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		// path = "C:/ezRoutingAPIROOT/havestplanning/fields.json";
		path = ROOT + "/harvest-plan-solution-vn.json";
		try {
			String fieldFilename = path;
			Gson gson = new Gson();

			HavestPlanningSolutionVN sol = gson.fromJson(new FileReader(fieldFilename),
					HavestPlanningSolutionVN.class);

			System.out.println(name() + "::getSolution, solution = "
					+ sol.toString());

			//sol.sort();

			return sol;
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@RequestMapping(value = "/havest-plan/add-fields", method = RequestMethod.POST)
	public ReturnAddFields addFields(HttpServletRequest request,
			@RequestBody FieldList fieldList) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		// path = "C:/ezRoutingAPIROOT/havestplanning/fields.json";
		path = ROOT + "/fields.json";
		try {
			/*
			 * JSONParser parser = new JSONParser(); Object obj =
			 * parser.parse(new FileReader(path)); JSONObject jsonObject =
			 * (JSONObject)obj; JSONArray jarr =
			 * (JSONArray)jsonObject.get("fields"); ArrayList<Field> L = new
			 * ArrayList<Field>(); Iterator it = jarr.iterator();
			 * while(it.hasNext()){ JSONObject o = (JSONObject)it.next(); String
			 * code = (String)o.get("code"); String districtCode =
			 * (String)o.get("districtCode"); String ownerCode =
			 * (String)o.get("ownerCode"); double area = (double)o.get("area");
			 * 
			 * //private Date date;// optimal havesting date String plant_date =
			 * (String)o.get("plant_date");
			 * 
			 * long l_quantity = (long)o.get("quantity");
			 * 
			 * int quantity = (int)l_quantity;
			 * 
			 * String category = (String)o.get("category"); String plantType =
			 * (String)o.get("plantType");
			 * 
			 * long l_deltaDays = (long)o.get("deltaDays"); int deltaDays =
			 * (int)l_deltaDays;
			 * 
			 * Field f = new Field(code,
			 * districtCode,ownerCode,area,plant_date,quantity,category,
			 * plantType,deltaDays); System.out.println(f); L.add(f); }
			 */

			String fieldFilename = path;
			Gson gson = new Gson();

			FieldList FL = gson.fromJson(new FileReader(fieldFilename),
					FieldList.class);

			System.out.println(name() + "::addFields, fieldList.sz = "
					+ fieldList);

			// check duplication of field code
			boolean duplication = false;
			String codes = "";
			for (int j = 0; j < fieldList.getFields().length; j++) {
				Field f = fieldList.getFields()[j];
				if (FL.getFields() != null)
					for (int i = 0; i < FL.getFields().length; i++) {
						if (f.getCode().equals(FL.getFields()[i].getCode())) {
							duplication = true;
							codes += f.getCode() + ", ";
							break;
						}
					}
			}
			if (duplication) {
				return new ReturnAddFields(FL.getFields().length,
						"duplicated fields " + codes, FL);
			}

			Field[] F = null;

			if (FL.getFields() == null) {
				F = fieldList.getFields();
			}else if(fieldList.getFields() == null){
				F = FL.getFields();
			}else {
				F = new Field[FL.getFields().length
						+ fieldList.getFields().length];
				int idx = -1;
				for (int i = 0; i < FL.getFields().length; i++) {
					idx++;
					F[idx] = FL.getFields()[i];
				}
				for (int i = 0; i < fieldList.getFields().length; i++) {
					idx++;
					F[idx] = fieldList.getFields()[i];
				}
			}
			FieldList newFieldList = new FieldList(F);

			PrintWriter out = new PrintWriter(path);
			out.print(gson.toJson(newFieldList));
			out.close();

			return new ReturnAddFields(newFieldList.getFields().length,
					"successful", newFieldList);
			// return newFieldList;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@RequestMapping(value = "/havest-plan/remove-fields", method = RequestMethod.POST)
	public ReturnAddFields removeFields(HttpServletRequest request,
			@RequestBody FieldCodeList fieldList) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		// path = "C:/ezRoutingAPIROOT/havestplanning/fields.json";
		path = ROOT + "/fields.json";
		try {
			String fieldFilename = path;
			Gson gson = new Gson();

			FieldList FL = gson.fromJson(new FileReader(fieldFilename),
					FieldList.class);

			System.out.println(name() + "::addFields, fieldList.sz = "
					+ fieldList);
			
			ArrayList<Field> l_fields = new ArrayList<Field>();

			// check duplication of field code
			if(FL.getFields() != null)for (int i = 0; i < FL.getFields().length; i++) {
				Field f = FL.getFields()[i];
				boolean exists = false;
				for (int j = 0; j < fieldList.getFields().length; j++) {
					if (fieldList.getFields()[j].getCode().equals(f.getCode())) {
						exists = true;
						break;
					}
				}
				if (!exists) {
					l_fields.add(f);
				}
			}

			Field[] arr_fields = new Field[l_fields.size()];
			for (int i = 0; i < l_fields.size(); i++)
				arr_fields[i] = l_fields.get(i);
			FieldList newFieldList = new FieldList(arr_fields);

			PrintWriter out = new PrintWriter(path);
			out.print(gson.toJson(newFieldList));
			out.close();

			return new ReturnAddFields(newFieldList.getFields().length,
					"successful", newFieldList);
			// return newFieldList;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	@RequestMapping(value = "/havest-plan/add-fields-vn", method = RequestMethod.POST)
	public ReturnAddFieldsVN addFields(HttpServletRequest request,
			@RequestBody FieldListVN fieldList) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		// path = "C:/ezRoutingAPIROOT/havestplanning/fields.json";
		path = ROOT + "/fields-vn.json";
		try {
			/*
			 * JSONParser parser = new JSONParser(); Object obj =
			 * parser.parse(new FileReader(path)); JSONObject jsonObject =
			 * (JSONObject)obj; JSONArray jarr =
			 * (JSONArray)jsonObject.get("fields"); ArrayList<Field> L = new
			 * ArrayList<Field>(); Iterator it = jarr.iterator();
			 * while(it.hasNext()){ JSONObject o = (JSONObject)it.next(); String
			 * code = (String)o.get("code"); String districtCode =
			 * (String)o.get("districtCode"); String ownerCode =
			 * (String)o.get("ownerCode"); double area = (double)o.get("area");
			 * 
			 * //private Date date;// optimal havesting date String plant_date =
			 * (String)o.get("plant_date");
			 * 
			 * long l_quantity = (long)o.get("quantity");
			 * 
			 * int quantity = (int)l_quantity;
			 * 
			 * String category = (String)o.get("category"); String plantType =
			 * (String)o.get("plantType");
			 * 
			 * long l_deltaDays = (long)o.get("deltaDays"); int deltaDays =
			 * (int)l_deltaDays;
			 * 
			 * Field f = new Field(code,
			 * districtCode,ownerCode,area,plant_date,quantity,category,
			 * plantType,deltaDays); System.out.println(f); L.add(f); }
			 */

			String fieldFilename = path;
			Gson gson = new Gson();

			FieldListVN FL = gson.fromJson(new FileReader(fieldFilename),
					FieldListVN.class);

			System.out.println(name() + "::addFields, fieldList.sz = "
					+ fieldList);

			// check duplication of field code
			boolean duplication = false;
			String codes = "";
			for (int j = 0; j < fieldList.getFields().length; j++) {
				FieldVN f = fieldList.getFields()[j];
				if (FL.getFields() != null)
					for (int i = 0; i < FL.getFields().length; i++) {
						if (f.getMA_RUONGMIA().equals(FL.getFields()[i].getMA_RUONGMIA())) {
							duplication = true;
							codes += f.getMA_RUONGMIA() + ", ";
							break;
						}
					}
			}
			if (duplication) {
				return new ReturnAddFieldsVN(FL.getFields().length,
						"duplicated fields " + codes, FL);
			}

			FieldVN[] F = null;

			if (FL.getFields() == null) {
				F = fieldList.getFields();
			}else if(fieldList.getFields() == null){
				F = FL.getFields();
			}else {
				F = new FieldVN[FL.getFields().length
						+ fieldList.getFields().length];
				int idx = -1;
				for (int i = 0; i < FL.getFields().length; i++) {
					idx++;
					F[idx] = FL.getFields()[i];
				}
				for (int i = 0; i < fieldList.getFields().length; i++) {
					idx++;
					F[idx] = fieldList.getFields()[i];
				}
			}
			FieldListVN newFieldList = new FieldListVN(F);

			PrintWriter out = new PrintWriter(path);
			out.print(gson.toJson(newFieldList));
			out.close();

			return new ReturnAddFieldsVN(newFieldList.getFields().length,
					"successful", newFieldList);
			// return newFieldList;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@RequestMapping(value = "/havest-plan/remove-fields-vn", method = RequestMethod.POST)
	public ReturnAddFieldsVN removeFieldsVN(HttpServletRequest request,
			@RequestBody FieldCodeList fieldList) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		// path = "C:/ezRoutingAPIROOT/havestplanning/fields.json";
		path = ROOT + "/fields-vn.json";
		try {
			String fieldFilename = path;
			Gson gson = new Gson();

			FieldListVN FL = gson.fromJson(new FileReader(fieldFilename),
					FieldListVN.class);

			System.out.println(name() + "::addFieldsVN, fieldList.sz = "
					+ fieldList);
			
			ArrayList<FieldVN> l_fields = new ArrayList<FieldVN>();

			// check duplication of field code
			if(FL.getFields() != null)for (int i = 0; i < FL.getFields().length; i++) {
				FieldVN f = FL.getFields()[i];
				boolean exists = false;
				for (int j = 0; j < fieldList.getFields().length; j++) {
					if (fieldList.getFields()[j].getCode().equals(f.getMA_RUONGMIA())) {
						exists = true;
						break;
					}
				}
				if (!exists) {
					l_fields.add(f);
				}
			}

			FieldVN[] arr_fields = new FieldVN[l_fields.size()];
			for (int i = 0; i < l_fields.size(); i++)
				arr_fields[i] = l_fields.get(i);
			FieldListVN newFieldList = new FieldListVN(arr_fields);

			PrintWriter out = new PrintWriter(path);
			out.print(gson.toJson(newFieldList));
			out.close();

			return new ReturnAddFieldsVN(newFieldList.getFields().length,
					"successful", newFieldList);
			// return newFieldList;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@RequestMapping(value = "/havest-plan/set-fields", method = RequestMethod.POST)
	public FieldList setFields(HttpServletRequest request,
			@RequestBody FieldList input) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		// path = "C:/ezRoutingAPIROOT/havestplanning/plant-standard.json";
		path = ROOT + "/fields.json";

		if (input.getFields() == null) {
			try {
				PrintWriter out = new PrintWriter(path);
				out.print("{}");
				out.close();
				return input;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		for (int i = 0; i < input.getFields().length; i++) {
			Field f = input.getFields()[i];
			if (f.getCategory() == null || f.getCategory().equals(""))
				f.setCategory("-");
			if (f.getPlantType() == null || f.getPlantType().equals(""))
				f.setPlantType("-");
		}
		try {
			PrintWriter out = new PrintWriter(path);
			Gson gson = new Gson();
			out.print(gson.toJson(input));
			out.close();

			return input;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	@RequestMapping(value = "/havest-plan/set-fields-vn", method = RequestMethod.POST)
	public FieldListVN setFieldsVN(HttpServletRequest request,
			@RequestBody FieldListVN input) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		// path = "C:/ezRoutingAPIROOT/havestplanning/plant-standard.json";
		path = ROOT + "/fields-vn.json";

		if (input.getFields() == null) {
			try {
				PrintWriter out = new PrintWriter(path);
				out.print("{}");
				out.close();
				return input;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		for (int i = 0; i < input.getFields().length; i++) {
			FieldVN f = input.getFields()[i];
			if (f.getMA_GIONGMIA() == null || f.getMA_GIONGMIA().equals(""))
				f.setMA_GIONGMIA("-");
			if (f.getMA_LOAIDAT() == null || f.getMA_LOAIDAT().equals(""))
				f.setMA_LOAIDAT("-");
			if (f.getMA_GOCMIA() == null || f.getMA_GOCMIA().equals(""))
				f.setMA_GOCMIA("-");
			if (f.getMA_LOAIMIA() == null || f.getMA_LOAIMIA().equals(""))
				f.setMA_LOAIMIA("-");
		}
		try {
			PrintWriter out = new PrintWriter(path);
			Gson gson = new Gson();
			out.print(gson.toJson(input));
			out.close();

			return input;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@RequestMapping(value = "/havest-plan/set-plant-standard", method = RequestMethod.POST)
	public ReturnSetPlantStandard setPlantStandard(HttpServletRequest request,
			@RequestBody PlantStandard input) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		// path = "C:/ezRoutingAPIROOT/havestplanning/plant-standard.json";
		path = ROOT + "/plant-standard.json";

		try {
			PrintWriter out = new PrintWriter(path);
			Gson gson = new Gson();
			out.print(gson.toJson(input));
			out.close();

			return new ReturnSetPlantStandard(input.getPlantStandards().length);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	@RequestMapping(value = "/havest-plan/set-plant-standard-vn", method = RequestMethod.POST)
	public ReturnSetPlantStandardVN setPlantStandardVN(HttpServletRequest request,
			@RequestBody PlantStandardVN input) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		// path = "C:/ezRoutingAPIROOT/havestplanning/plant-standard.json";
		path = ROOT + "/plant-standard-vn.json";

		try {
			PrintWriter out = new PrintWriter(path);
			Gson gson = new Gson();
			out.print(gson.toJson(input));
			out.close();

			return new ReturnSetPlantStandardVN(input.getSUGAR_TIEUCHUAN_CCS().length);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@RequestMapping(value = "/havest-plan/set-machine", method = RequestMethod.POST)
	public MachineSetting setMachine(HttpServletRequest request,
			@RequestBody MachineSetting input) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		// path = "C:/ezRoutingAPIROOT/havestplanning/machine-setting.json";
		path = ROOT + "/machine-setting.json";

		try {
			PrintWriter out = new PrintWriter(path);
			Gson gson = new Gson();
			out.print(gson.toJson(input));
			out.close();

			return input;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	@RequestMapping(value = "/havest-plan/set-machine-vn", method = RequestMethod.POST)
	public MachineSettingVN setMachineVN(HttpServletRequest request,
			@RequestBody MachineSettingVN input) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		// path = "C:/ezRoutingAPIROOT/havestplanning/machine-setting.json";
		path = ROOT + "/machine-setting-vn.json";

		try {
			PrintWriter out = new PrintWriter(path);
			Gson gson = new Gson();
			out.print(gson.toJson(input));
			out.close();

			return input;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@RequestMapping(value = "/havest-plan/compute", method = RequestMethod.POST)
	public HavestPlanningSolution compute(HttpServletRequest request
	 , @RequestBody RunParameters param
	) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		String fieldFilename = ROOT + "/fields.json";
		String setPlatStandardFilename = ROOT + "/plant-standard.json";
		String machineSettingFilename = ROOT + "/machine-setting.json";
		// reset harvest-plan-solution.json
		path = ROOT + "/harvest-plan-solution.json";

		try {
			PrintWriter out = new PrintWriter(path);
			out.print("{}");
			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		int timeLimit = param.getTimeLimit();
		int maxNbSteps = param.getNbSteps();
		Gson gson = new Gson();
		try {
			FieldList fieldList = gson.fromJson(new FileReader(fieldFilename),
					FieldList.class);
			PlantStandard ps = gson.fromJson(new FileReader(
					setPlatStandardFilename), PlantStandard.class);
			MachineSetting ms = gson.fromJson(new FileReader(
					machineSettingFilename), MachineSetting.class);

			HavestPlanningInput input = new HavestPlanningInput(
					fieldList.getFields(), ps, ms);

			SolverMultiStepSplitFields solver = new SolverMultiStepSplitFields();

			if (input.getPlantStandard() == null)
				input.initDefaultPlantStandard();

			String des = input.checkConsistency(); 
			if(!des.equals("OK")){
				HavestPlanningSolution ret_sol = new HavestPlanningSolution();
				ret_sol.setDescription(des);
				return ret_sol;
			}
			HavestPlanningSolution sol = solver.solve(input, maxNbSteps, timeLimit,param.getDeltaPlantDateLeft(),
					param.getDeltaPlantDateRight(), param.getStartDatePlan());
			
			String json = gson.toJson(sol);
			//System.out.println(name() + "::compute, RETURN " + json);
			
			
			path = ROOT + "/harvest-plan-solution.json";

			try {
				PrintWriter out = new PrintWriter(path);
				out.print(gson.toJson(sol));
				out.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			HavestPlanningSolution ret_sol = new HavestPlanningSolution(sol.getQuality(), 
					sol.getDescription(), sol.getNumberOfFieldsInPlan(), 
					sol.getNumberOfDatesInPlan(), sol.getNumberOfDatesInPlantStandard(), 
					sol.getInitMinQuantityDay(), sol.getInitMaxQuantityDay(), 
					sol.getComputedMinQuantityDay(), sol.getComputedMaxQuantityDay(), 
					sol.getNumberFieldsNotPlanned(), sol.getQuantityNotPlanned(), 
					sol.getQuantityPlanned(), sol.getTotalQuantity(), sol.getNumberOfLevels(), 
					sol.getNumberOfDaysHarvestExact(), sol.getNumberOfDaysPlanned(), 
					sol.getNumberOfFieldsCompleted(), sol.getMaxDaysLate(), sol.getMaxDaysEarly(), 
					sol.getNumberOfDaysOverLoad(), 
					sol.getNumberOfDaysUnderLoad());
			
			return ret_sol;
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@RequestMapping(value = "/havest-plan/compute-vn", method = RequestMethod.POST)
	public HavestPlanningSolutionVN computeVN(HttpServletRequest request
	 , @RequestBody RunParameters param
	) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		String fieldFilename = ROOT + "/fields-vn.json";
		String setPlatStandardFilename = ROOT + "/plant-standard-vn.json";
		String machineSettingFilename = ROOT + "/machine-setting-vn.json";
		// reset harvest-plan-solution.json
		path = ROOT + "/harvest-plan-solution-vn.json";

		try {
			PrintWriter out = new PrintWriter(path);
			out.print("{}");
			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		int timeLimit = param.getTimeLimit();
		int maxNbSteps = param.getNbSteps();
		Gson gson = new Gson();
		try {
			FieldListVN fieldListVN = gson.fromJson(new FileReader(fieldFilename),
					FieldListVN.class);
			PlantStandardVN PSVN = gson.fromJson(new FileReader(
					setPlatStandardFilename), PlantStandardVN.class);
			MachineSettingVN MSVN = gson.fromJson(new FileReader(
					machineSettingFilename), MachineSettingVN.class);

			Field[] fields = new Field[fieldListVN.getFields().length];
			for(int i = 0; i < fields.length; i++){
				fields[i] = fieldListVN.getFields()[i].convert();
			}
			FieldList fieldList = new FieldList(fields);
			
			PlantStandardElement[] pse = new PlantStandardElement[PSVN.getSUGAR_TIEUCHUAN_CCS().length];
			for(int i = 0; i < pse.length; i++){
				pse[i] = PSVN.getSUGAR_TIEUCHUAN_CCS()[i].convert();
			}
			PlantStandard ps = new PlantStandard(pse);
			
			int int_min_load = (int)MSVN.getCONGSUAT_MIN();
			int int_max_load = (int)MSVN.getCONGSUAT_MAX();
			MachineSetting ms = new MachineSetting(int_min_load, int_max_load);
			
			HavestPlanningInput input = new HavestPlanningInput(
					fieldList.getFields(), ps, ms);

			SolverMultiStepSplitFields solver = new SolverMultiStepSplitFields();

			if (input.getPlantStandard() == null)
				input.initDefaultPlantStandard();

			String des = input.checkConsistency(); 
			if(!des.equals("OK")){
				HavestPlanningSolutionVN ret_sol = new HavestPlanningSolutionVN();
				ret_sol.setMO_TA(des);
				return ret_sol;
			}
			HavestPlanningSolution sol = solver.solve(input, maxNbSteps, timeLimit,param.getDeltaPlantDateLeft(),
					param.getDeltaPlantDateRight(), param.getStartDatePlan());
			
			
			HavestPlanningSolutionVN solvn = new HavestPlanningSolutionVN();
			solvn.convertFrom(sol);
			
			String json = gson.toJson(solvn);
			//System.out.println(name() + "::compute, RETURN " + json);
			
			
			path = ROOT + "/harvest-plan-solution-vn.json";

			try {
				PrintWriter out = new PrintWriter(path);
				out.print(gson.toJson(solvn));
				out.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			HavestPlanningSolutionVN ret_sol = new HavestPlanningSolutionVN(sol.getQuality(), 
					sol.getDescription(), sol.getNumberOfFieldsInPlan(), 
					sol.getNumberOfDatesInPlan(), sol.getNumberOfDatesInPlantStandard(), 
					sol.getInitMinQuantityDay(), sol.getInitMaxQuantityDay(), 
					sol.getComputedMinQuantityDay(), sol.getComputedMaxQuantityDay(), 
					sol.getNumberFieldsNotPlanned(), sol.getQuantityNotPlanned(), 
					sol.getQuantityPlanned(), sol.getTotalQuantity(), sol.getNumberOfLevels(), 
					sol.getNumberOfDaysHarvestExact(), sol.getNumberOfDaysPlanned(), 
					sol.getNumberOfFieldsCompleted(), sol.getMaxDaysLate(), sol.getMaxDaysEarly(), 
					sol.getNumberOfDaysOverLoad(), 
					sol.getNumberOfDaysUnderLoad(),null);
			
			return ret_sol;
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	
	
	@RequestMapping(value = "/havest-plan/analyze-input", method = RequestMethod.POST)
	public InputAnalysisInfo analyzeInput(HttpServletRequest request
	 
	) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		String fieldFilename = ROOT + "/fields.json";
		String setPlatStandardFilename = ROOT + "/plant-standard.json";
		String machineSettingFilename = ROOT + "/machine-setting.json";
		// reset harvest-plan-solution.json
		path = ROOT + "/harvest-plan-solution.json";

		try {
			PrintWriter out = new PrintWriter(path);
			out.print("{}");
			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		Gson gson = new Gson();
		try {
			FieldList fieldList = gson.fromJson(new FileReader(fieldFilename),
					FieldList.class);
			PlantStandard ps = gson.fromJson(new FileReader(
					setPlatStandardFilename), PlantStandard.class);
			MachineSetting ms = gson.fromJson(new FileReader(
					machineSettingFilename), MachineSetting.class);

			HavestPlanningInput input = new HavestPlanningInput(
					fieldList.getFields(), ps, ms);

			SolverMultiStepSplitFields solver = new SolverMultiStepSplitFields();

			if (input.getPlantStandard() == null)
				input.initDefaultPlantStandard();

			String des = input.checkConsistency(); 
			if(!des.equals("OK")){
				
				return null;
			}
			InputAnalysisInfo info = solver.analyze(input);
			
			String json = gson.toJson(info);
			//System.out.println(name() + "::compute, RETURN " + json);
			
			
			path = ROOT + "/harvest-plan-solution.json";

			try {
				PrintWriter out = new PrintWriter(path);
				out.print(gson.toJson(info));
				out.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			return info;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@RequestMapping(value = "/havest-plan/check-solution", method = RequestMethod.POST)
	public HavestPlanningSolution checkSolution(HttpServletRequest request,
			@RequestBody FieldSolutionList input_solution) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		String fieldFilename = ROOT + "/fields.json";
		String setPlatStandardFilename = ROOT + "/plant-standard.json";
		String machineSettingFilename = ROOT + "/machine-setting.json";

		Gson gson = new Gson();
		try {
			FieldList fieldList = gson.fromJson(new FileReader(fieldFilename),
					FieldList.class);
			PlantStandard ps = gson.fromJson(new FileReader(
					setPlatStandardFilename), PlantStandard.class);
			MachineSetting ms = gson.fromJson(new FileReader(
					machineSettingFilename), MachineSetting.class);

			// HavestPlanningInput input = new
			// HavestPlanningInput(fieldList.getFields(),ps,ms);
			HavestPlanningInput input = new HavestPlanningInput(
					input_solution.getFields(), ps, ms);

			SolutionChecker checker = new SolutionChecker();

			return checker.checkSolution(input, input_solution);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

}
