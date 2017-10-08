package localsearch.domainspecific.packing.algorithms;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import com.dailyopt.VRPLoad3D.service.RoutingLoad3DSolver;

import localsearch.domainspecific.packing.models.Model3D;
import localsearch.domainspecific.packing.entities.*;

public interface GreedyConstructiveOrderLoadConstraint {

	public void printCandidatePositions();
	public String name();
	public void init();
	public boolean tryLoad(ArrayList<Item3D> tried_items);
	public boolean load(ArrayList<Item3D> tried_items);
	public void printSolution();
	public void solve();
	public Move3D selectBest(ArrayList<Move3D> moves);
	public void solve(int[] o);
	public void place(int w, int l, int h, Position3D p);
	public ArrayList<Move3D> getSolution();
}