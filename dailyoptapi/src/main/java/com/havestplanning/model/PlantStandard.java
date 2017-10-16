package com.havestplanning.model;

import java.util.ArrayList;

import com.havestplanning.utils.Utility;

public class PlantStandard {
	private PlantStandardElement[] plantStandards;

	public PlantStandardElement[] getPlantStandards() {
		return plantStandards;
	}

	public void setPlantStandards(PlantStandardElement[] plantStandards) {
		this.plantStandards = plantStandards;
	}

	public PlantStandard(PlantStandardElement[] plantStandards) {
		super();
		this.plantStandards = plantStandards;
	}

	public PlantStandard() {
		super();
		// TODO Auto-generated constructor stub
	}

	// additional methods
	public int getMinPeriod(String category, String plantType) {
		int minPeriod = Integer.MAX_VALUE;
		for (int i = 0; i < plantStandards.length; i++) {
			PlantStandardElement e = plantStandards[i];
			if (e.getCategory().equals(category)
					&& e.getPlantType().equals(plantType)) {
				if (minPeriod > e.getPlantPeriod())
					minPeriod = e.getPlantPeriod();
			}
		}
		return minPeriod;
	}

	public int getMinPeriod() {
		int minPeriod = Integer.MAX_VALUE;
		for (int i = 0; i < plantStandards.length; i++) {
			PlantStandardElement e = plantStandards[i];
			if (minPeriod > e.getPlantPeriod())
				minPeriod = e.getPlantPeriod();
		}
		return minPeriod;
	}

	public boolean find(String category, String plantType) {
		for (int i = 0; i < plantStandards.length; i++) {
			PlantStandardElement e = plantStandards[i];
			if (e.getCategory().equals(category)
					&& e.getPlantType().equals(plantType)) {
				return true;
			}
		}
		return false;
	}

	public int getMaxPeriod(String category, String plantType) {
		int maxPeriod = 1 - Integer.MAX_VALUE;
		for (int i = 0; i < plantStandards.length; i++) {
			PlantStandardElement e = plantStandards[i];
			if (e.getCategory().equals(category)
					&& e.getPlantType().equals(plantType)) {
				if (maxPeriod < e.getPlantPeriod())
					maxPeriod = e.getPlantPeriod();
			}
		}
		return maxPeriod;
	}

	public int getMaxPeriod() {
		int maxPeriod = 1 - Integer.MAX_VALUE;
		for (int i = 0; i < plantStandards.length; i++) {
			PlantStandardElement e = plantStandards[i];
			if (maxPeriod < e.getPlantPeriod())
				maxPeriod = e.getPlantPeriod();
		}
		return maxPeriod;
	}

	public int getMaxRange() {
		return getMaxPeriod() - getMinPeriod();
	}

	public int getBestPeriod(String category, String plantType) {
		int bestPeriod = -1;
		double best_result = -1;
		ArrayList<Integer> L = new ArrayList<Integer>();
		for (int i = 0; i < plantStandards.length; i++) {
			PlantStandardElement e = plantStandards[i];
			if (e.getCategory().equals(category)
					&& e.getPlantType().equals(plantType)) {
				if (best_result < e.getResult()) {
					best_result = e.getResult();
					bestPeriod = e.getPlantPeriod();
					L.clear();
					L.add(e.getPlantPeriod());
				} else if (best_result == e.getResult()) {
					L.add(e.getPlantPeriod());
				}
			}
		}
		if (L.size() > 0) {
			int[] a = new int[L.size()];
			for (int i = 0; i < L.size(); i++)
				a[i] = L.get(i);
			for (int i = 0; i < a.length - 1; i++)
				for (int j = 0; j < a.length; j++)
					if (a[i] > a[j]) {
						int tmp = a[i];
						a[i] = a[j];
						a[j] = tmp;
					}
			bestPeriod = (a[0] + a[a.length - 1]) / 2;
		}
		return bestPeriod;
	}

	public double evaluateQuality(String category, String plantType, int period) {
		// return the quality of category/plantType with period
		ArrayList<PlantStandardElement> L = new ArrayList<PlantStandardElement>();
		for (int i = 0; i < plantStandards.length; i++) {
			PlantStandardElement e = plantStandards[i];
			if (e.getCategory().equals(category)
					&& e.getPlantType().equals(plantType)) {
				L.add(e);
			}
		}
		PlantStandardElement[] a = new PlantStandardElement[L.size()];
		for (int i = 0; i < L.size(); i++)
			a[i] = L.get(i);
		// sort a in an increasing order of period
		for (int i = 0; i < a.length - 1; i++)
			for (int j = i + 1; j < a.length; j++)
				if (a[i].getPlantPeriod() > a[j].getPlantPeriod()) {
					PlantStandardElement tmp = a[i];
					a[i] = a[j];
					a[j] = tmp;
				}
		if (period <= a[0].getPlantPeriod())
			return a[0].getResult();
		if (period >= a[a.length - 1].getPlantPeriod())
			return a[a.length - 1].getResult();
		for (int i = 0; i < a.length - 1; i++) {
			if (a[i].getPlantPeriod() <= period
					&& period <= a[i + 1].getPlantPeriod()) {
				return Utility.getInducedValue(a[i].getPlantPeriod(),
						a[i].getResult(), a[i + 1].getPlantPeriod(),
						a[i + 1].getResult(), period);
			}
		}
		return 0;
	}
}
