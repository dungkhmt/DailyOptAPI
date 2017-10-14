package havestplanning.solver.multistepsplitfield;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import havestplanning.model.Field;
import havestplanning.model.FieldList;
import havestplanning.model.FieldSolution;
import havestplanning.model.FieldSolutionList;
import havestplanning.model.HavestPlanningCluster;
import havestplanning.model.HavestPlanningField;
import havestplanning.model.HavestPlanningInput;
import havestplanning.model.HavestPlanningSolution;
import havestplanning.model.MachineSetting;
import havestplanning.model.PlantStandard;
import havestplanning.solver.MField;
import havestplanning.solver.Solver;
import havestplanning.utils.DateTimeUtils;
import havestplanning.utils.Utility;

public class SolutionChecker extends Solver {
	
	public void analyzeDates(FieldSolutionList inp_solution){
		HashSet<Date> set_dates = new HashSet<Date>();
		HashMap<String, Date> mStr2Date = new HashMap<String, Date>();
		for(int i = 0; i < inp_solution.getFields().length; i++){
			FieldSolution fs = inp_solution.getFields()[i];
			if(mStr2Date.get(fs.getPlant_date()) == null){
				Date pd = DateTimeUtils.convertYYYYMMDD2Date(fs.getPlant_date());
				set_dates.add(pd);
				mStr2Date.put(fs.getPlant_date(), pd);
			}
			if(mStr2Date.get(fs.getHavest_date()) == null){
				Date hd = DateTimeUtils.convertYYYYMMDD2Date(fs.getHavest_date());
				set_dates.add(hd);
				mStr2Date.put(fs.getHavest_date(), hd);
			}
		}
		
		dates = new Date[set_dates.size()];
		int idx = -1;
		for(Date d: set_dates){
			idx++;
			dates[idx] = d;
			//System.out.println(d.toString() + " : " + mDate2Quantity.get(d));
		}
		
		for(int i = 0; i < dates.length; i++)
			for(int j = i+1; j < dates.length; j++)
				if(dates[i].compareTo(dates[j]) > 0){
					Date tmp = dates[i]; dates[i] = dates[j]; dates[j] = tmp;
				}

		System.out.println("total : " + dates.length);
	
		int sz = Utility.distance(dates[0], dates[dates.length-1]) + 1;
		
		mDate2Slot = new HashMap<Date, Integer>();
		int start = 200;
		mDate2Slot.put(dates[0], start);
		date_sequence = new Date[10*sz];
		date_sequence[0] = Utility.next(dates[0],-start);
		for(int i = 1; i < date_sequence.length; i++){
			date_sequence[i] = Utility.next(date_sequence[i-1],1);
		}
		for(int i = 0; i < date_sequence.length; i++){
			System.out.println(i + " : " + Utility.dateMonthYear(date_sequence[i]));
		}

		for (int i = 1; i < dates.length; i++) {
			Date d = dates[i - 1];
			while (true) {
				d = Utility.next(d);
				start++;
				if (d.compareTo(dates[i]) == 0) {
					break;
				}
			}
			mDate2Slot.put(dates[i], start);
		}

		for (int i = 0; i < dates.length; i++) {
			System.out.println(dates[i].toString() + "\t" + Utility.dateMonthYear(dates[i]) + "\t"
					+ mDate2Slot.get(dates[i]));
		}

		
	}

	public HavestPlanningSolution checkSolution(HavestPlanningInput input,
			FieldSolutionList inp_solution) {

		this.input = input;
		analyzeDates(inp_solution);
		
		String description = "";
		double quality = 0;
		ArrayList<HavestPlanningCluster> cluster = new ArrayList<HavestPlanningCluster>();
		for (int k = 0; k < date_sequence.length; k++) {
			ArrayList<FieldSolution> F = new ArrayList<FieldSolution>();
			for (int i = 0; i < inp_solution.getFields().length; i++) {
				FieldSolution fs = inp_solution.getFields()[i];
				Date d = DateTimeUtils
						.convertYYYYMMDD2Date(fs.getHavest_date());
				System.out.println(name() + "::checkSolution, field[" + fs.getCode() + 
						"], havest_date = " + fs.getHavest_date() + ", d = " + d.toString());
				int di = mDate2Slot.get(d);
				if(di == k){
					F.add(fs);
				}
			}
			if(F.size() > 0){
				Date currentDate = date_sequence[k];
				HavestPlanningField[] HPF = new HavestPlanningField[F.size()];
				int qtt = 0;
				for(int j = 0; j < F.size(); j++){
					FieldSolution f = F.get(j);
					int bestDate = getBestHavestDate(f);
					Date expected_havest_date = date_sequence[bestDate];
					
					String expected_havest_date_str = DateTimeUtils.date2YYYYMMDD(expected_havest_date);
					Date d = DateTimeUtils.convertYYYYMMDD2Date(f.getHavest_date());//date_sequence[xd[fid]];
					int days_late = Utility.distance(expected_havest_date,d);
					HPF[j] = new HavestPlanningField(f, expected_havest_date_str, 
							f.getHavest_quantity(), days_late, -1);
					
					int period = mDate2Slot.get(DateTimeUtils.convertYYYYMMDD2Date(f.getHavest_date())) - 
							mDate2Slot.get(DateTimeUtils.convertYYYYMMDD2Date(f.getPlant_date()));
					
					quality += f.getHavest_quantity() * input.getPlantStandard().evaluateQuality(f.getCategory(),
							f.getPlantType(), period);
							
					qtt += f.getHavest_quantity();
				}
				String date = DateTimeUtils.date2YYYYMMDD(currentDate);
				HavestPlanningCluster C = new HavestPlanningCluster(date, qtt, F.size(), HPF, -1);
				cluster.add(C);
		
				if(qtt > input.getMachineSetting().getMaxLoad()){
					description += "total quantity of date " + date + " = " + qtt + " > maxLoad = " + 
				input.getMachineSetting().getMaxLoad() + "\n";
				}
				
				if(qtt < input.getMachineSetting().getMinLoad()){
					description += "total quantity of date " + date + " = " + qtt + " < minLoad = " + 
							input.getMachineSetting().getMinLoad() + "\n";
				}
			}
		}
		
		// check individual field
		for(int i = 0; i < inp_solution.getFields().length; i++){
			FieldSolution fs = inp_solution.getFields()[i];
			if(fs.getQuantity() < fs.getHavest_quantity()){
				description = description + "Field[" + fs.getCode() + "], havest quantity = " + 
			fs.getHavest_quantity() + " > quantity = " + fs.getQuantity() + "\n";
			}
		
			Date plant_date = DateTimeUtils.convertYYYYMMDD2Date(fs.getPlant_date());
			Date havest_date = DateTimeUtils.convertYYYYMMDD2Date(fs.getHavest_date());
			int period = Utility.distance(plant_date, havest_date);
			int minP = input.getPlantStandard().getMinPeriod(fs.getCategory(), fs.getPlantType());
			int maxP = input.getPlantStandard().getMaxPeriod(fs.getCategory(), fs.getPlantType());
			
			if(period <= minP){
				description += "field[" + fs.getCode() + "], havest period = " + period + " <= minPeriod = " + minP + "\n"; 
			}
			if(period >= maxP){
				description += "field[" + fs.getCode() + "], havest period = " + period + " >= maxPeriod = " + maxP + "\n"; 
			}
		}
		
		HavestPlanningCluster[] a_cluster = new HavestPlanningCluster[cluster.size()];
		for(int i = 0; i < cluster.size(); i++)
			a_cluster[i] = cluster.get(i);
		
		HavestPlanningSolution solution = new HavestPlanningSolution(quality, description, a_cluster);
		return solution;
	}
}
