package com.havestplanning.solver;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.havestplanning.model.Field;
import com.havestplanning.model.HavestPlanningCluster;
import com.havestplanning.model.HavestPlanningField;
import com.havestplanning.model.HavestPlanningInput;
import com.havestplanning.model.FieldClusterIndices;
import com.havestplanning.model.FieldCluster;
import com.havestplanning.model.HavestPlanningSolution;
import com.havestplanning.model.PlantStandard;
import com.havestplanning.utils.DateTimeUtils;
import com.havestplanning.utils.Utility;


public class Solver {
	protected int DURATION = 355;
	protected HavestPlanningInput input;
	protected HashMap<Date, Integer> mDate2Slot;
	protected HashMap<Date, Integer> mDate2Quantity;
	protected HashMap<Date, ArrayList<Integer>> mDate2ListFields;
	protected Date[] dates;
	protected MField[] fields;
	protected int[] expected_slot;
	
	protected Date[] date_sequence;
	
	ArrayList<FieldClusterIndices> clusterIndices;
	HashMap<FieldCluster, Integer> mCluster2Slot;
	HashMap<Integer, FieldCluster> mSlot2Cluster;
	int startSlot;
	int endSlot;
	
	// computed statistic information
	protected int numberOfFieldsInPlan;
	protected int numberOfDatesInPlan;
	protected int numberOfDatesInPlantStandard;
	protected int initMinQuantityDay;
	protected int initMaxQuantityDay;
	protected int computedMinQuantityDay;
	protected int computedMaxQuantityDay;
	protected int numberFieldsNotPlanned;
	protected int quantityNotPlanned;
	protected int quantityPlanned;
	protected int totalQuantity;
	protected int numberLevels;
	protected int numberOfDaysHarvestExact;
	protected int numberOfDaysPlanned;
	protected int numberOfFieldsCompleted;
	protected int maxDaysLate;
	protected int maxDaysEarly;
	protected int numberOfDaysOverLoad;
	protected int numberOfDaysUnderLoad;
	
	
	
	protected Date startHarvestDate;
	protected Date endHarvestDate;

	// end of computed statistic information
	protected boolean DEBUG = true;
	
	protected PrintWriter log = null;
	public String name(){
		return "api.solver.solve";
	}
	public boolean getDEBUG(){
		return DEBUG;
	}
	public void initLog(){
		try{
			log = new PrintWriter("C:/tmp/havest-planning-log.txt");
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void finalize(){
		log.close();
	}
	public void analyze(){
		totalQuantity = 0;
		fields = new MField[input.getFields().length];
		for(int i = 0; i < fields.length; i++){
			Field F = input.getFields()[i];
			totalQuantity += F.getQuantity();
			
			Date mDate = DateTimeUtils.convertYYYYMMDD2Date(F.getPlant_date());
			
			fields[i] = new MField(F.getCode(), F.getDistrictCode(), 
					F.getOwnerCode(), F.getArea(), F.getPlant_date(), F.getQuantity(),  
					
					F.getDeltaDays(), F.getPlantType(), F.getCategory(), mDate);
			
		}
		
		/*
		// sorting fields
		for(int i = 0; i < fields.length-1; i++){
			for(int j = i+1; j < fields.length; j++){
				if(fields[i].getmDate().compareTo(fields[j].getmDate()) > 0){
					MField tmp = fields[i]; fields[i] = fields[j]; fields[j] = tmp;
				}				
			}
		}
		*/
		
		mDate2Quantity = new HashMap<Date, Integer>();
		mDate2ListFields = new HashMap<Date, ArrayList<Integer>>();

		for(int i = 0; i < fields.length; i++){
			if(mDate2Quantity.get(fields[i].getmDate()) == null){
				mDate2Quantity.put(fields[i].getmDate(), fields[i].getQuantity());
				
				mDate2ListFields.put(fields[i].getmDate(), new ArrayList<Integer>());
				mDate2ListFields.get(fields[i].getmDate()).add(i);
			}else{
				int q = mDate2Quantity.get(fields[i].getmDate());
				q += fields[i].getQuantity();
				mDate2Quantity.put(fields[i].getmDate(),q);
				mDate2ListFields.get(fields[i].getmDate()).add(i);
			}
		}
		dates = new Date[mDate2Quantity.keySet().size()];
		int idx = -1;
		for(Date d: mDate2Quantity.keySet()){
			idx++;
			dates[idx] = d;
			//System.out.println(d.toString() + " : " + mDate2Quantity.get(d));
		}
		
		for(int i = 0; i < dates.length; i++)
			for(int j = i+1; j < dates.length; j++)
				if(dates[i].compareTo(dates[j]) > 0){
					Date tmp = dates[i]; dates[i] = dates[j]; dates[j] = tmp;
				}
		/*
		for(int i = 0; i < dates.length; i++){
			String items = "";
			ArrayList<Integer> L = mDate2ListFields.get(dates[i]);
			for(int j = 0; j < L.size(); j++)
				items += L.get(j) + ",";
			System.out.println(dates[i].getYear() + "-" + (dates[i].getMonth()+1) + "-" + dates[i].getDate() +
					"\t" + dates[i] + "\t" + mDate2Quantity.get(dates[i]) + "\t" + items);
		}
		System.out.println("total : " + dates.length);
		*/
	}

	public int getBestHavestDate(Field f){
		int sl = mDate2Slot.get(DateTimeUtils.convertYYYYMMDD2Date(f.getPlant_date()));
		//int minP = input.getPlantStandard().getMinPeriod(f.getCategory(), f.getPlantType());
		//int maxP = input.getPlantStandard().getMaxPeriod(f.getCategory(), f.getPlantType());
		//return (sl + (minP + maxP)/2);
		int p = input.getPlantStandard().getBestPeriod(f.getCategory(), f.getPlantType());
		//log.println(name() + "::getBestHavestDate(" + f.getCode() + "), sl = " + sl + ", p = " + p + ", bestHavestDate = " + (sl + p));
		return sl + p;
	}
	public void mapDates() {
		System.out.println(name() + "::mapDates, dates.length = " + dates.length + ", dates[0] = " + 
	Utility.dateMonthYear(dates[0]) + ", dates[" + (dates.length-1) + "] = " + Utility.dateMonthYear(dates[dates.length-1]));
		log.println(name() + "::mapDates, dates.length = " + dates.length + ", dates[0] = " + 
				Utility.dateMonthYear(dates[0]) + ", dates[" + (dates.length-1) + "] = " + Utility.dateMonthYear(dates[dates.length-1]));
					
		Date lastDate = Utility.next(dates[dates.length-1],input.getPlantStandard().getMaxPeriod() + 1);
		ArrayList<Date> dateList = new ArrayList<Date>();
		dateList.add(dates[0]);
		Date curDate = dates[0];
		startHarvestDate = Utility.next(dates[0],input.getPlantStandard().getMinPeriod());
		endHarvestDate = lastDate;
		while(true){
			Date d = Utility.next(curDate,1);
			dateList.add(d);
			System.out.println(name() + "::mapDates, dateList[" + (dateList.size()-1) + "] = " + Utility.dateMonthYear(d)
					+ ", lastDate = " + Utility.dateMonthYear(lastDate));
			if(d.compareTo(lastDate) == 0) break;
			curDate = d;
		}
		//for(int i = 0; i < dateList.size(); i++){
			//System.out.println(name() + "::mapDates, dateList[" + i + "] = " + Utility.dateMonthYear(dateList.get(i)));
		//}
		numberOfDatesInPlan = Utility.distance(startHarvestDate, endHarvestDate);
		numberOfFieldsInPlan = fields.length;
		numberOfDatesInPlantStandard = input.getPlantStandard().getMaxRange() + 1;
		System.out.println(name() + "::mapDates, numberOfDatesInPlan = " + numberOfDatesInPlan);
		//System.exit(-1);
		
		mDate2Slot = new HashMap<Date, Integer>();
		int start = 0;//200;
		mDate2Slot.put(dates[0], start);
		date_sequence = new Date[dateList.size()];//new Date[5000];
		for(int i = 0; i < dateList.size(); i++)
			date_sequence[i] = dateList.get(i);
		
		/*
		date_sequence[0] = Utility.next(dates[0],-start);
		for(int i = 1; i < date_sequence.length; i++){
			date_sequence[i] = Utility.next(date_sequence[i-1],1);
		}
		*/
		
		//for(int i = 0; i < date_sequence.length; i++){
		//	System.out.println(i + " : " + Utility.dateMonthYear(date_sequence[i]));
		//}
		
		//System.exit(-1);
		/*
		 * Calendar cal = Calendar.getInstance(); cal.setTime(dates[0]); for(int
		 * i = 0; i < 100; i++){ cal.add(Calendar.DATE, 1);
		 * System.out.println(cal.getTime().toString()); } if(true) return;
		 */

		/*
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
		*/
		
		for(int i = 0; i < date_sequence.length; i++){
			Date d = date_sequence[i];
			mDate2Slot.put(d, i);
		}
		
		for (int i = 0; i < dates.length; i++) {
			System.out.println(dates[i].toString() + "\t" + Utility.dateMonthYear(dates[i]) + "\t"
					+ mDate2Slot.get(dates[i]));
		}
		//System.exit(-1);
		
		expected_slot = new int[fields.length];
		for(int i = 0; i < dates.length; i++){
			Date d = dates[i];
			ArrayList<Integer> L = mDate2ListFields.get(d);
			int sl = mDate2Slot.get(d);
			for(int j: L)
				expected_slot[j] = sl;
		}
	}
	public int rearrangeSlots(FieldCluster[] clusters, int earliest, int start, int end){
		int latest = date_sequence.length;//mDate2Slot.get(dates[end]);
		int eval1 = 1000000;
		int eval2 = 1000000;
		int sel_sl = -1;
		for(int sl = earliest; sl < latest; sl++){
			int e1 = -1;
			int e2 = 0;
			for(int i = 0; i < clusters.length; i++){
				// try to assign clusters i to slot sl + i
				for(int j = 0; j < clusters[i].size(); j++){
					int f = clusters[i].get(j);
					int d = Math.abs(expected_slot[f] - (sl+i));
					if(d > e1){
						e1 = d; e2 = 1;
					}else if(d == e1){
						e2++;
					}
				}
			}
			if(e1 < eval1 || (e1 == eval1 && e2 < eval2)){
				eval1 = e1; eval2 = e2; sel_sl = sl;
			}
		}
		if(sel_sl < 0){
			System.out.println(name() + "::rearrangeSlots, EXCEPTION earliest = " + earliest + ", latest = " + latest);
			System.exit(-1);
		}
		return sel_sl;
	}
	public FieldCluster[] compute(ArrayList<Integer> L, ArrayList<Integer> w){
		MultiKnapsackSolver solver = new MultiKnapsackSolver();
		int[] aw = new int[w.size()];
		for(int i = 0; i < w.size(); i++) aw[i] = w.get(i);
		//FieldCluster[] C = solver.solve(aw,input.getMinP(),input.getMaxP());
		FieldCluster[] C = solver.solve(aw,input.getMachineSetting().getMinLoad(),
				input.getMachineSetting().getMaxLoad());
		FieldCluster[] rC = new FieldCluster[C.length];
		for(int i = 0; i < C.length; i++){
			rC[i] = new FieldCluster();
			rC[i].setWeight(C[i].getWeight());
			for(int j = 0; j < C[i].size(); j++){
				int idx = C[i].get(j);
				rC[i].add(L.get(idx));
			}
		}
		return rC;
	}
	
	private int specifyStart(int min, int sz, int start, int end){
		int m = (start + end)/2;
		int s = m - sz/2 + 1;
		if(s < min) s = min;
		return s;
	}
	/*
	public HavestPlanningSolution solve(HavestPlanningInput input){
		this.input = input;
		//this.DURATION = input.getGrowthDuration();
		this.DURATION = 355;
		analyze();
		mapDates();
		
		//MultiKnapsackSolver knapsack = new MultiKnapsackSolver();
		
		clusterIndices = new ArrayList<FieldClusterIndices>();
		int i = 0;
		while(i < dates.length){
			int S = 0;
			int j = i;
			ArrayList<Integer> L = new ArrayList<Integer>();
			while(j < dates.length){
				S += mDate2Quantity.get(dates[j]);
				ArrayList<Integer> Lj = mDate2ListFields.get(dates[j]); 
				for(int k = 0; k < Lj.size(); k++){
					L.add(Lj.get(k));
				}
			
				//if(input.getMinP() <= S && S <= input.getMaxP()
				if(input.getMachineSetting().getMinLoad() <= S && S <= input.getMachineSetting().getMaxLoad()
						|| S > input.getMachineSetting().getMaxLoad() 
						|| (j+1 < dates.length && mDate2Slot.get(dates[j+1]) - mDate2Slot.get(dates[i]) 
								//>= input.getClusterDuration())
								>= 14)
								
						){
					break;
				}
				j++;
			}
			if(j >= dates.length) j--;// border case
			
			//if(input.getMinP() <= S && S <= input.getMaxP()
			if(input.getMachineSetting().getMinLoad() <= S && S <= input.getMachineSetting().getMaxLoad()
					|| S <= 7000 
					){
				FieldCluster[] clusters = new FieldCluster[1];
				clusters[0] = new FieldCluster();
				for(int e: L) clusters[0].add(e);
				clusters[0].setWeight(S);
				FieldClusterIndices CI = new FieldClusterIndices(clusters, i, j);
				System.out.println(name() + "::solve, admin a cluster (" + i + "," + j + "), dates.sz = " + dates.length);
				clusterIndices.add(CI);
				//break;
			}else if(S > 7000){
				ArrayList<Integer> w = new ArrayList<Integer>();
				for(int k = 0; k < L.size(); k++){
					int q = fields[L.get(k)].getQuantity();
					w.add(q);
				}
				FieldCluster[] C = compute(L, w);
				FieldClusterIndices CI = new FieldClusterIndices(C, i, j);
				clusterIndices.add(CI);
				//break;
			}
			
			i = j+1;
		}

		mCluster2Slot = new HashMap<FieldCluster, Integer>();
		mSlot2Cluster = new HashMap<Integer, FieldCluster>();
		int startIdx = 0;
		for(i = 0; i < clusterIndices.size(); i++){
			int sz = clusterIndices.get(i).getCluster().length;
			int I = clusterIndices.get(i).getStartIdx();
			int J = clusterIndices.get(i).getEndIdx();
			//int s = specifyStart(startIdx, sz, I, J);
			int s = rearrangeSlots(clusterIndices.get(i).getCluster(), startIdx, I, J);
			startIdx = s + sz;

			if(i == 0){
				startSlot = s;
			}else if(i == clusterIndices.size()-1){
				endSlot = startIdx - 1;
			}
			
			for(int j = 0; j < sz; j++){
				System.out.println(clusterIndices.get(i).getCluster()[j].getWeight());
				mCluster2Slot.put(clusterIndices.get(i).getCluster()[j], s+j);
				mSlot2Cluster.put(s+j, clusterIndices.get(i).getCluster()[j]);
			}

			int slot_begin = mDate2Slot.get(dates[clusterIndices.get(i).getStartIdx()]);
			int slot_end = mDate2Slot.get(dates[clusterIndices.get(i).getEndIdx()]);
			System.out.println(slot_begin + "\t" + slot_end + 
					": rearrange \t" + s + " -> " + (startIdx-1));
			System.out.println("---------------------------------------------");
		}
		
		ArrayList<HavestPlanningCluster> list_clusters = new ArrayList<HavestPlanningCluster>();
		double quality = 0;
		//SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-DD");
		for(int sl = startSlot; sl <= endSlot; sl++) {
			FieldCluster C = mSlot2Cluster.get(sl);
			if(C != null){
				Date d = Utility.next(date_sequence[sl],DURATION);// after 11 month
				//String date_str = df.format(d);
				String date_str = DateTimeUtils.date2YYYYMMDD(d);
				
				HavestPlanningField[] HPF = new HavestPlanningField[C.size()];
				int qtt = 0;
				for(int j = 0; j < C.size(); j++){
					int fj = C.get(j);
					MField F = fields[fj];
					Date expected_havest_date = Utility.next(F.getmDate(),DURATION);
					
					//String expected_havest_date_str = df.format(expected_havest_date);
					String expected_havest_date_str = DateTimeUtils.date2YYYYMMDD(expected_havest_date);
					
					int days_late = Utility.distance(d, expected_havest_date);
					HPF[j] = new HavestPlanningField(fields[fj], expected_havest_date_str, 
							fields[fj].getQuantity(),days_late);
					
					//quality += Utility.eval(input.getQualityFunction(), days_late);
					
					qtt += F.getQuantity();
				}
				HavestPlanningCluster hpc = new HavestPlanningCluster(date_str, HPF, qtt, C.size());
				list_clusters.add(hpc);
			}
		}
		HavestPlanningCluster[] arr_clusters = new HavestPlanningCluster[list_clusters.size()];
		for(int ii = 0; ii < list_clusters.size(); ii++)
			arr_clusters[ii] = list_clusters.get(ii);
		HavestPlanningSolution sol = new HavestPlanningSolution(arr_clusters, quality);
		return sol;
	}
	*/
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
