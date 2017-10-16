package com.havestplanning.solver.multistepsplitfield;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import localsearch.constraints.basic.AND;
import localsearch.constraints.basic.IsEqual;
import localsearch.constraints.basic.LessOrEqual;
import localsearch.constraints.basic.OR;
import localsearch.functions.conditionalsum.ConditionalSum;
import localsearch.model.ConstraintSystem;
import localsearch.model.IConstraint;
import localsearch.model.LocalSearchManager;
import localsearch.model.VarIntLS;

import org.apache.poi.ss.usermodel.DateUtil;

import com.havestplanning.model.Field;
import com.havestplanning.model.HavestPlanningCluster;
import com.havestplanning.model.HavestPlanningField;
import com.havestplanning.model.HavestPlanningInput;
import com.havestplanning.model.HavestPlanningSolution;
import com.havestplanning.model.InputAnalysisInfo;
import com.havestplanning.model.PlantStandard;
import com.havestplanning.solver.MField;
import com.havestplanning.solver.Solver;
import com.havestplanning.utils.DateTimeUtils;
import com.havestplanning.utils.Utility;

public class SolverMultiStepSplitFields extends Solver {
	/*
	 * protected LocalSearchManager mgr; protected VarIntLS[] x; protected
	 * ConstraintSystem S; protected ConditionalSum[] load;
	 */

	// protected int[] w;// quantity of field i
	protected LeveledHavestPlanSolution sol;
	protected ArrayList<LeveledHavestPlanSolution> solutions;

	
	protected ConstrainedMultiKnapsackSolver S;

	// protected int[] x;// x[i] is the date field i is allocated
	// protected int[] load;// load[i] is the total quantity of fields allocated
	// in date i

	/*
	 * private void stateModel(){ mgr = new LocalSearchManager(); x = new
	 * VarIntLS[fields.length]; w = new int[fields.length]; for(int i = 0; i <
	 * fields.length; i++){ int sl = mDate2Slot.get(fields[i].getmDate()); x[i]
	 * = new VarIntLS(mgr,sl - fields[i].getDeltaDays(), sl +
	 * fields[i].getDeltaDays()); x[i].setValue(sl);
	 * 
	 * w[i] = fields[i].getQuantity(); }
	 * 
	 * S = new ConstraintSystem(mgr); int m = 800; load = new ConditionalSum[m];
	 * for(int i = 0; i < m; i++){ load[i] = new ConditionalSum(x, w, i);
	 * 
	 * IConstraint c1 = new LessOrEqual(input.getMinP(), load[i]); IConstraint
	 * c2 = new LessOrEqual(load[i], input.getMaxP()); IConstraint c = new
	 * IsEqual(load[i], 0); S.post(new OR(c,new AND(c1,c2))); }
	 * 
	 * 
	 * 
	 * mgr.close();
	 * 
	 * for(int i = 0; i < load.length; i++){ log.println("load[" + i + "] = " +
	 * load[i].getValue()); System.out.println("load[" + i + "] = " +
	 * load[i].getValue()); } System.out.println("S = " + S.violations());
	 * 
	 * }
	 */

	public PrintWriter getLog() {
		return log;
	}

	public HavestPlanningInput getInput() {
		return input;
	}

	public void stateModel() {

	}

	
	public void extendDateSequence(Date fromDate, Date toDate){
		Date startDate = fromDate;
		if(startDate.compareTo(date_sequence[0]) >= 0) startDate = date_sequence[0];
		Date lastDate = toDate;
		if(lastDate.compareTo(date_sequence[date_sequence.length-1]) <= 0) lastDate = date_sequence[date_sequence.length-1];
		System.out.println(name() + "::extendDateSequence, startDate = " + DateTimeUtils.date2YYYYMMDD(startDate) + 
				", lastDate = " + DateTimeUtils.date2YYYYMMDD(lastDate));
		
		ArrayList<Date> dateList = new ArrayList<Date>();
		
		//for(int i = 0; i < date_sequence.length; i++)
		//	dateList.add(date_sequence[i]);
		Date curDate = startDate;//date_sequence[date_sequence.length-1];
		dateList.add(curDate);
		while(true){
			Date d = Utility.next(curDate,1);
			dateList.add(d);
			System.out.println(name() + "::extendDateSequence, dateList[" + (dateList.size()-1) + "] = " + Utility.dateMonthYear(d)
					+ ", lastDate = " + Utility.dateMonthYear(lastDate));
			if(d.compareTo(lastDate) >= 0) break;
			curDate = d;
		}
		date_sequence = new Date[dateList.size()];
		for(int i = 0; i < dateList.size(); i++){
			date_sequence[i] = dateList.get(i);
			mDate2Slot.put(date_sequence[i], i);
		}
		
	}
	
	public void search(int maxNbSteps, int timeLimit, int delta_left, int delta_right, String startDatePlanStr) {
		
		int nbHarvestDays = totalQuantity/input.getMachineSetting().getMinLoad() + 1;
		
		Date sdp = DateTimeUtils.convertYYYYMMDD2Date(startDatePlanStr);
		Date lastDate = Utility.next(sdp, nbHarvestDays);
		
		extendDateSequence(sdp,lastDate);
		
		System.out.println(name() + "::search, startDatePlan = " + startDatePlanStr);
		System.out.println(name() + "::search, sdp = " + sdp.toString());
		
		int startDatePlan = mDate2Slot.get(sdp);
		int n = input.getFields().length;
		int[] qtt = new int[n];
		for (int i = 0; i < n; i++)
			qtt[i] = input.getFields()[i].getQuantity();
		int minLoad = input.getMachineSetting().getMinLoad();// 5000;
		int maxLoad = input.getMachineSetting().getMaxLoad();// 6000;

		int m = date_sequence.length;
		int[] preload = new int[m];
		for (int i = 0; i < m; i++)
			preload[i] = 0;
		int[] minDate = new int[n];
		int[] maxDate = new int[n];
		int[] expected_dates = new int[n];

		
		// int deltaDay = 30;
		for (int i = 0; i < n; i++) {
			MField f = fields[i];
			/*
			 * int sl = mDate2Slot.get(f.getmDate()); minDate[i] = sl -
			 * input.getFields()[i].getDeltaDays(); maxDate[i] = sl +
			 * input.getFields()[i].getDeltaDays();
			 */
			Date d = DateTimeUtils.convertYYYYMMDD2Date(f.getPlant_date());
			int plantStart = mDate2Slot.get(d);
			minDate[i] = plantStart
					+ input.getPlantStandard().getMinPeriod(f.getCategory(),
							f.getPlantType());
			
			
			maxDate[i] = plantStart
					+ input.getPlantStandard().getMaxPeriod(f.getCategory(),
							f.getPlantType());
			
			int p = input.getPlantStandard().getBestPeriod(f.getCategory(),
					f.getPlantType());
			expected_dates[i] = plantStart
					+ p;
			if(i == 0){
				log.println(name() + "::search, field " + fields[i].getCode() + 
						", plantStart = " + plantStart + ", bestPeriod = " + p + ", expectedDate = " + 
						expected_dates[i]);
			}
			if(minDate[i] < expected_dates[i] - delta_left){
				minDate[i] = expected_dates[i] - delta_left;
			}
			if(maxDate[i] > expected_dates[i] + delta_right){
				maxDate[i] = expected_dates[i] + delta_right;
			}
			
			System.out.println(name() + "::search, field[" + i
					+ "], plantStart = " + plantStart + ", category = "
					+ f.getCategory() + ", plantType = " + f.getPlantType() + ", minDate = " + (minDate[i] - plantStart)
					+ ", maxDate = " + (maxDate[i] - plantStart));
			
			//System.out.println(name() + "::search, plantStandard = "
			//		+ input.getPlantStandard());
			
		}
		int totalQuantity = 0;
		HashSet<Integer> D = new HashSet<Integer>();
		int E = Integer.MAX_VALUE;
		int L = 0;
		int min_range = Integer.MAX_VALUE;
		int max_range = 0;
		for(int i = 0; i < n; i++){
			totalQuantity += fields[i].getQuantity();
			for(int d = minDate[i]; d <= maxDate[i]; d++){
				D.add(d);
			}
			if(E > minDate[i]) E = minDate[i];
			if(L < maxDate[i]) L = maxDate[i];
			if(min_range > maxDate[i] - minDate[i]) min_range = maxDate[i] - minDate[i];
			if(max_range < maxDate[i] - minDate[i]) max_range = maxDate[i] - minDate[i];
		}
		System.out.println(name() + "::search, n = " + n + ", m = " + m + ", totalQuantity = " + totalQuantity +
				", E = " + E + ", L = " + L + ", D.sz = " + D.size() + ", min_rng = " + min_range + 
				", max_rng = " + max_range + ", MAX = " + (D.size()*6000));
		//System.exit(-1);
		
		
		double[][] p = new double[n][m];
		/*
		for(int i = 0; i < n; i++){
			Date date = DateTimeUtils.convertYYYYMMDD2Date(fields[i].getPlant_date());
			int plantStart = mDate2Slot.get(date);

			for(int d = 0; d < m; d++){
				
				if(d < minDate[i] || d > maxDate[i]) p[i][d] = 0;
				else{
					int period = d - plantStart; 
							p[i][d] = input.getPlantStandard().evaluateQuality(fields[i].getCategory(), fields[i].getPlantType(),
						period);
							System.out.println(name() + "::search, SET p[" + i + "," + d + "] = " + p[i][d]);
				}
			}
		}
		*/
		
		//S = new ConstrainedMultiKnapsackSolver(this);
		S = new GreedyConstrainedMultiKnapsackSolver(this);

		// analyze min-max Quantity of days
		S.setInput(preload, qtt, minDate, maxDate, expected_dates, minLoad, maxLoad,p);
		S.stateModel();
		S.initSolutionExpectedDate();
		
		int[] load = S.getLoads();
		initMinQuantityDay = Integer.MAX_VALUE;
		initMaxQuantityDay = 1-initMinQuantityDay;
		numberOfDaysOverLoad = 0;
		numberOfDaysUnderLoad = 0;
		for(int i = 0; i < load.length; i++)if(load[i] > 0){
			if(initMinQuantityDay > load[i]) initMinQuantityDay = load[i];
			if(initMaxQuantityDay < load[i]) initMaxQuantityDay = load[i];
			if(load[i] < input.getMachineSetting().getMinLoad()) numberOfDaysUnderLoad++;
			if(load[i] > input.getMachineSetting().getMaxLoad()) numberOfDaysOverLoad++;
		}
		int[] b = new int[m];
		for(int i = 0; i < b.length; i++) b[i] = 0;
		int[] x = S.getX();
		for(int i = 0; i < x.length; i++) b[x[i]] = 1;
		numberOfDaysHarvestExact = 0;
		for(int i = 0; i < b.length; i++) numberOfDaysHarvestExact += b[i];
		
		// end of analyze min-max Quantity of days
		
		
		solutions = new ArrayList<LeveledHavestPlanSolution>();

		numberOfFieldsCompleted = 0;
		
		// reset b: b[i] = 1 if day i is planner (harvest)
		for(int i = 0; i < b.length; i++) b[i] = 0;
		
		int nbSteps = 0;
		while (nbSteps < maxNbSteps) {
			// check feasibility for before solving
			boolean feasible = true;
			int nbEmptyFields = 0;
			for(int i = 0; i < qtt.length; i++){
				if(qtt[i] == 0) nbEmptyFields++;
			}
			if(nbEmptyFields == qtt.length) feasible = false;
			
			for (int i = 0; i < qtt.length; i++) {
				boolean ok = false;
				for (int d = minDate[i]; d <= maxDate[i]; d++) {
					if (preload[d] < maxLoad) {
						ok = true;
						break;
					}
				}
				if(!ok){
					feasible = false;
					break;
				}
			}
			if (!feasible)
				break;

			S.name = "ConstraintMultiKnapsackSolver[" + (solutions.size() + 1) + "]";
			// solve the problem
			LeveledHavestPlanSolution s = S.solve(preload, qtt, minDate,
					maxDate, expected_dates, minLoad, maxLoad, startDatePlan, timeLimit);
			solutions.add(s);
			//System.out.println(name() + "::search, solutions[" + solutions.size() + "] = " + s.toString());
			//,System.out.println(name() + "*******************************************************************************");
			// update remaining qtt, preload for next steps
			int[] xd = s.getXd();
			int[] sq = s.getQuantity();
			
			for(int i = 0; i < n; i++){
				int delta = Math.abs(xd[i]- expected_dates[i]); 
				//if(delta > 50){
				//	System.out.println(name() + "::search, BUG, xd[" + i + "] = " + xd[i] + 
				//			", expected_date[" + i + "] = " + expected_dates[i]);
				//	System.exit(-1);
					
				//}
				String msg = "TRUETRUE";
				//if(delta > 50) msg = "FALSEFALSE";
				log.println(name() + "::search, xd[" + i + "] = " + xd[i] + 
							", expected_date[" + i + "] = " + expected_dates[i] + ", minDate = " + minDate[i] + 
							", maxDate = " + maxDate[i] + ", MSGMSG = " + msg);
			}
			
			
			for(int i = 0; i < xd.length; i++) b[xd[i]] = 1;
			
			for (int i = 0; i < n; i++) {
				qtt[i] = qtt[i] - sq[i];
			
				if(qtt[i] == 0) numberOfFieldsCompleted += 1;
			}
			
			for (int d = 0; d < m; d++) {
				for (int i = 0; i < n; i++) {
					if (xd[i] == d) {
						preload[d] = preload[d] + sq[i];
					}
				}
			}
			nbSteps++;
			//if(solutions.size() >= 1) break;
			
			
		}

		numberOfDaysPlanned = 0;
		for(int i = 0; i < m; i++) numberOfDaysPlanned += b[i];
		
		
	}
	public InputAnalysisInfo analyze(HavestPlanningInput input) {
		this.input = input;
		analyze();
		mapDates();
		
		String earliestPlantDate = DateTimeUtils.date2YYYYMMDD(dates[0]);
		String latestPlantDate = DateTimeUtils.date2YYYYMMDD(dates[dates.length-1]);
		int numberOfFields = input.getFields().length;
		int totalQuantity = 0;
		int numberOfDatesPlantRange = Utility.distance(earliestPlantDate, latestPlantDate);
		int maxQuantity = -1;
		int minQuantity = Integer.MAX_VALUE;
		
		for(int i = 0; i < input.getFields().length; i++){
			Field f = input.getFields()[i];
			totalQuantity += f.getQuantity();
			if(minQuantity > f.getQuantity()) minQuantity = f.getQuantity();
			if(maxQuantity < f.getQuantity()) maxQuantity = f.getQuantity();
		}

		return new InputAnalysisInfo(earliestPlantDate, latestPlantDate, 
				numberOfFields, totalQuantity, 
				numberOfDatesPlantRange, maxQuantity, minQuantity);
	}
	
	public HavestPlanningSolution solve(HavestPlanningInput input, int maxNbSteps, int timeLimit,
			int delta_left, int delta_right, String startDatePlan) {
		initLog();

		this.input = input;
		// this.DURATION = input.getGrowthDuration();
		analyze();
		mapDates();
		
		if(date_sequence.length > 2000){
			String des = "field " + fields[0].getCode() + " with plant_date = " + fields[0].getPlant_date() + 
					", field " + fields[fields.length-1].getCode() + " with plant_date = " + 
					fields[fields.length-1].getPlant_date() + ", maxPeriod = " + input.getPlantStandard().getMaxPeriod();
			System.out.println(name() + "::solve, EXCEPTION des = " + des);
			HavestPlanningSolution sol = new HavestPlanningSolution();
			sol.setDescription(des);
			return sol;
		}
		
		System.out.println(name() + "::solve date_sequence = "
				+ date_sequence.length + " ...");
		for (int i = 0; i < date_sequence.length; i++) {
			Date d = date_sequence[i];
			ArrayList<Integer> L = mDate2ListFields.get(d);
			if (L == null)
				L = new ArrayList<Integer>();
			if (L.size() > 0) {
				// System.out.print("date " + i + "(" +
				// DateTimeUtils.date2YYYYMMDD(d) + "), sz = " + L.size() +
				// " : ");
				if(getDEBUG()) log.print("date " + i + "(" + DateTimeUtils.date2YYYYMMDD(d)
						+ "), sz = " + L.size() + ", total = "
						+ mDate2Quantity.get(d) + " : ");
				// for(int j: L){
				// log.print(j + "(" + fields[j].getQuantity() + ") ");
				// System.out.print(j + " ");
				// }
				// System.out.println();
				if(getDEBUG()) log.println();
			}
		}

		//
		stateModel();

		search(maxNbSteps, timeLimit, delta_left, delta_right, startDatePlan);

		System.out.println("finished, number of levels = " + solutions.size());

		numberLevels = solutions.size();
		
		ArrayList<HavestPlanningCluster> cluster = new ArrayList<HavestPlanningCluster>();
		int quality = 0;

		HashMap<Integer, HavestPlanningCluster> mDate2Cluster = new HashMap<Integer, HavestPlanningCluster>();
		HashMap<Integer, Integer> mDate2Quantity = new HashMap<Integer, Integer>();
		
		numberOfDaysPlanned = 0;
		int[] day_planned = new int[date_sequence.length];
		for(int i = 0; i < day_planned.length; i++) day_planned[i] = 0;
		
		maxDaysEarly = 0;
		maxDaysLate = 0;
		
		for (int k = 0; k < solutions.size(); k++) {
			sol = solutions.get(k);
			
			int[] xd = sol.getXd();
			int[] sq = sol.getQuantity();
			
			for(int j = 0; j < xd.length; j++){
				Field f = fields[j];//input.getFields()[j];
				int bd = getBestHavestDate(f);
				int delta = xd[j] - bd;
				String msg = "TRUE";
				//if(Math.abs(delta) > 50) msg = "FALSE";
				String hd = DateTimeUtils
						.date2YYYYMMDD(date_sequence[xd[j]]);
				String bhd = DateTimeUtils
						.date2YYYYMMDD(date_sequence[bd]);
				log.println(name() + "::solve, RESULT xd[" + j + "] = " + xd[j] + " = " + hd + ", field " + f.getCode() + 
						", bestDate = " + bd + " = " + bhd + ", MSG = " + msg);
				
			}
			
			for(int i = 0; i < xd.length; i++) day_planned[xd[i]] = 1;
			
			for (int i = 0; i < date_sequence.length; i++) {
				ArrayList<Integer> F = new ArrayList<Integer>();
				for (int j = 0; j < xd.length; j++)
					if (xd[j] == i)
						F.add(j);

				if (F.size() > 0) {
					// Date currentDate =
					// Utility.next(date_sequence[i],DURATION);
					Date currentDate = date_sequence[i];

					double sugarQuantityOfCluster = 0;
					HavestPlanningField[] HPF = new HavestPlanningField[F
							.size()];
					int qtt = 0;
					for (int j = 0; j < F.size(); j++) {
						int fid = F.get(j);
						// MField f = fields[fid];
						Field f = input.getFields()[fid];
						// MField mf = fields[fid];
						// int sl = mDate2Slot.get(f.getmDate());
						// Date expected_havest_date =
						// Utility.next(f.getmDate(),DURATION);
						int bestDate = getBestHavestDate(f);
						Date expected_havest_date = date_sequence[bestDate];

						String expected_havest_date_str = DateTimeUtils
								.date2YYYYMMDD(expected_havest_date);
						Date d = date_sequence[xd[fid]];
						// d = Utility.next(d,DURATION);
						int days_late = Utility.distance(expected_havest_date,
								d);
						
						
						if(days_late < 0){
							if(maxDaysEarly < (-days_late)) maxDaysEarly = -days_late;
						}else{
							if(maxDaysLate < days_late) maxDaysLate = days_late;
						}
						
						int period = xd[fid]
								- mDate2Slot
										.get(DateTimeUtils
												.convertYYYYMMDD2Date(f
														.getPlant_date()));

						// quality += Utility.eval(input.getQualityFunction(),
						// days_late);
						double sugarQuantity =  sq[fid]
								* input.getPlantStandard().evaluateQuality(
										f.getCategory(), f.getPlantType(),
										period);
						
						//HPF[j].setSugarQuantity(sugarQuantity);
						quality += sugarQuantity;
						sugarQuantityOfCluster += sugarQuantity;
						HPF[j] = new HavestPlanningField(f,
								expected_havest_date_str, sq[fid], days_late, sugarQuantity);
 
						qtt += sq[fid];//f.getQuantity();
					}
					String date = DateTimeUtils.date2YYYYMMDD(currentDate);
					//HavestPlanningCluster Ck = new HavestPlanningCluster(date,
					//		HPF, qtt, F.size(), sugarQuantityOfCluster);
					HavestPlanningCluster Ck = new HavestPlanningCluster(date,
							qtt, F.size(), HPF, sugarQuantityOfCluster);
					
					HavestPlanningCluster Ci = mDate2Cluster.get(i);
					if(Ci == null){
						cluster.add(Ck);
						mDate2Cluster.put(i, Ck);
					}else{
						// concatenate  the fields in Ck and Ci
						Ci.concate(Ck);
					}
				}
			}
		}

		computedMinQuantityDay = Integer.MAX_VALUE;
		computedMaxQuantityDay = 1-computedMinQuantityDay;
		quantityPlanned = 0;
		totalQuantity = 0;
		numberOfDaysPlanned = 0;
		for(int i = 0; i < day_planned.length; i++) numberOfDaysPlanned += day_planned[i];
		
		for(int i = 0; i < fields.length; i++)
			totalQuantity += fields[i].getQuantity();
		
		for (int i = 0; i < cluster.size(); i++)if(cluster.get(i).getQuantity() > 0){
			quantityPlanned += cluster.get(i).getQuantity();
			
			if(computedMinQuantityDay > cluster.get(i).getQuantity()) 
				computedMinQuantityDay = cluster.get(i).getQuantity();
			if(computedMaxQuantityDay < cluster.get(i).getQuantity()) 
				computedMaxQuantityDay = cluster.get(i).getQuantity();
			
		}
			
		quantityNotPlanned = totalQuantity - quantityPlanned;
		
		HavestPlanningCluster[] a_cluster = new HavestPlanningCluster[cluster
				.size()];
		for (int i = 0; i < cluster.size(); i++)
			a_cluster[i] = cluster.get(i);

		numberOfDaysOverLoad = 0;
		numberOfDaysUnderLoad = 0;
		for(int i = 0; i < a_cluster.length; i++){
			HavestPlanningCluster c = a_cluster[i];
			if(c.getQuantity() < input.getMachineSetting().getMinLoad()) numberOfDaysUnderLoad++;
			if(c.getQuantity() > input.getMachineSetting().getMaxLoad()) numberOfDaysOverLoad++;
		}
		
		//HavestPlanningSolution solution = new HavestPlanningSolution(a_cluster,	quality);
		HavestPlanningSolution solution = new HavestPlanningSolution(quality,"success",numberOfFieldsInPlan,
				numberOfDatesInPlan, numberOfDatesInPlantStandard, initMinQuantityDay, initMaxQuantityDay,
				computedMinQuantityDay, computedMaxQuantityDay, 0, quantityNotPlanned, quantityPlanned, totalQuantity,
				numberLevels,numberOfDaysHarvestExact,numberOfDaysPlanned,numberOfFieldsCompleted,
				maxDaysLate, maxDaysEarly,numberOfDaysOverLoad, numberOfDaysUnderLoad);
		
		solution.setClusters(a_cluster);
		
		
		finalize();
		
		return solution;
	}

	public String name() {
		return "SolverMultiStepSplitFields";
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
