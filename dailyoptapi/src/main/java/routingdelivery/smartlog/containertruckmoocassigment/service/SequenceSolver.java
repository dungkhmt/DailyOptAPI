package routingdelivery.smartlog.containertruckmoocassigment.service;

import routingdelivery.smartlog.containertruckmoocassigment.model.ComboContainerMoocTruck;
import routingdelivery.smartlog.containertruckmoocassigment.model.DeliveryWarehouseInfo;
import routingdelivery.smartlog.containertruckmoocassigment.model.ExportContainerRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.ImportContainerRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.Permutation;
import routingdelivery.smartlog.containertruckmoocassigment.model.PickupWarehouseInfo;
import routingdelivery.smartlog.containertruckmoocassigment.model.Port;
import routingdelivery.smartlog.containertruckmoocassigment.model.SequenceSolution;
import routingdelivery.smartlog.containertruckmoocassigment.model.Warehouse;

public class SequenceSolver {
	ContainerTruckMoocSolver solver;

	public SequenceSolver(ContainerTruckMoocSolver solver) {
		this.solver = solver;
	}

	public SequenceSolution solve(ComboContainerMoocTruck combo,
			PickupWarehouseInfo[] WH, Port P) {
		PermutationGenerator G = new PermutationGenerator();
		Permutation[] perm = G.generate(WH.length);

		int[] seq = null;
		for (int i = 0; i < perm.length; i++) {
			int[] x = perm[i].p;
			seq = x;
			break;
		}
		SequenceSolution sol = new SequenceSolution();
		sol.seq = seq;
		return sol;
	}

	public SequenceSolution solve(String startLocationCode, String startTime,
			String[] locationCode, String[] earlyArrivalTime,
			String[] lateArrivalTime, int[] duration,
			String[] earlyDepartureTime, String[] lateDepartureTime,
			String endLocationCode, String lateTime) {

		PermutationGenerator G = new PermutationGenerator();
		Permutation[] perm = G.generate(locationCode.length);

		int[] seq = null;
		for (int i = 0; i < perm.length; i++) {
			int[] x = perm[i].p;
			seq = x;
			break;
		}
		SequenceSolution sol = new SequenceSolution();
		sol.seq = seq;
		return sol;

	}

	public SequenceSolution solve(String startLocation, int startTime,
			ImportContainerRequest ir, ExportContainerRequest er,
			String endLocation) {
		PermutationGenerator G = new PermutationGenerator();
		int ni = ir.getDeliveryWarehouses().length;
		int ne = er.getPickupWarehouses().length;
		Permutation[] i_perm = G.generate(ni);
		Permutation[] e_perm = G.generate(ne);

		int[] seq = new int[ni + ne];
		double minD = Integer.MAX_VALUE;
		for (int i = 0; i < i_perm.length; i++) {
			int[] si = i_perm[i].p;
			for (int j = 0; j < e_perm.length; j++) {
				int[] se = e_perm[j].p;
				String[] lc = new String[ni + ne + 2];
				lc[0] = startLocation;
				lc[ni + ne + 1] = endLocation;
				for (int k = 0; k < ni; k++) {
					DeliveryWarehouseInfo dwi = ir.getDeliveryWarehouses()[si[k]];
					Warehouse wh = solver.getWarehouseFromCode(dwi
							.getWareHouseCode());
					lc[k + 1] = wh.getLocationCode();
				}
				for (int k = 0; k < ne; k++) {
					PickupWarehouseInfo pwi = er.getPickupWarehouses()[se[k]];
					Warehouse wh = solver.getWarehouseFromCode(pwi
							.getWareHouseCode());
					lc[ni + k + 1] = wh.getLocationCode();
				}
				double d = 0;
				for (int k = 0; k < ni + ne + 1; k++) {
					d = d + solver.getDistance(lc[k], lc[k + 1]);
				}
				if (d < minD) {
					minD = d;
					for (int k = 0; k < ni; k++)
						seq[k] = si[k];
					for (int k = 0; k < ne; k++)
						seq[ni + k] = se[k];
				}
			}
		}
		SequenceSolution sol = new SequenceSolution(seq, minD);
		return sol;
	}

	public SequenceSolution solve(String startLocation, int startTime,
			ImportContainerRequest ir, String endLocation) {
		PermutationGenerator G = new PermutationGenerator();
		int ni = ir.getDeliveryWarehouses().length;
		Permutation[] i_perm = G.generate(ni);

		int[] seq = new int[ni];
		double minD = Integer.MAX_VALUE;
		for (int i = 0; i < i_perm.length; i++) {
			int[] si = i_perm[i].p;
			String[] lc = null;
			if (endLocation != null) {
				lc = new String[ni + 2];
				lc[0] = startLocation;
				lc[ni + 1] = endLocation;
			} else {
				lc = new String[ni + 1];
				lc[0] = startLocation;
			}
			for (int k = 0; k < ni; k++) {
				DeliveryWarehouseInfo dwi = ir.getDeliveryWarehouses()[si[k]];
				Warehouse wh = solver.getWarehouseFromCode(dwi
						.getWareHouseCode());
				lc[k + 1] = wh.getLocationCode();
			}
			double d = 0;
			for (int k = 0; k < lc.length - 1; k++) {
				d = d + solver.getDistance(lc[k], lc[k + 1]);
			}
			if (d < minD) {
				minD = d;
				for (int k = 0; k < ni; k++)
					seq[k] = si[k];
			}
		}
		SequenceSolution sol = new SequenceSolution(seq, minD);
		return sol;
	}

	public SequenceSolution solve(String startLocation, int startTime,
			ExportContainerRequest er, String endLocation) {
		PermutationGenerator G = new PermutationGenerator();
		int ne = er.getPickupWarehouses().length;
		Permutation[] e_perm = G.generate(ne);

		int[] seq = new int[ne];
		double minD = Integer.MAX_VALUE;
		for (int j = 0; j < e_perm.length; j++) {
			int[] se = e_perm[j].p;
			String[] lc = null;
			if (endLocation != null) {
				lc = new String[ne + 2];
				lc[0] = startLocation;
				lc[ne + 1] = endLocation;
			} else {
				lc = new String[ne + 1];
				lc[0] = startLocation;
			}
			for (int k = 0; k < ne; k++) {
				PickupWarehouseInfo pwi = er.getPickupWarehouses()[se[k]];
				Warehouse wh = solver.getWarehouseFromCode(pwi
						.getWareHouseCode());
				lc[k + 1] = wh.getLocationCode();
			}
			double d = 0;
			for (int k = 0; k < lc.length-1; k++) {
				d = d + solver.getDistance(lc[k], lc[k + 1]);
			}
			if (d < minD) {
				minD = d;
				for (int k = 0; k < ne; k++)
					seq[k] = se[k];
			}
		}
		SequenceSolution sol = new SequenceSolution(seq, minD);
		return sol;
	}

}
