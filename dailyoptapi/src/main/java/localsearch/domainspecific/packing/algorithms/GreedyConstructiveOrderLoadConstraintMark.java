package localsearch.domainspecific.packing.algorithms;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import localsearch.domainspecific.packing.entities.Container3D;
import localsearch.domainspecific.packing.entities.Item3D;
import localsearch.domainspecific.packing.entities.Move3D;
import localsearch.domainspecific.packing.entities.Position3D;
import localsearch.domainspecific.packing.models.Model3D;

import com.dailyopt.VRPLoad3D.service.RoutingLoad3DSolver;

public class GreedyConstructiveOrderLoadConstraintMark implements GreedyConstructiveOrderLoadConstraint{
	private RoutingLoad3DSolver solver;
	private Model3D model;
	private int[] o;// order of items to be loaded
	// private int[] x_w;
	// private int[] x_l;
	// private int[] x_h;

	private Item3D[] items;
	private Container3D container;

	public ArrayList<Position3D> candidate_positions;

	// auxiliary data structures
	private int[][][] occ;// occ[iw][il][ih] = 1 if cell occ[iw][il[ih] is
							// occupied
	private ArrayList<Integer> LW;// list of item positions in w-axis
	private ArrayList<Integer> LL;// list of item positions in l-axis
	private ArrayList<Integer> LH;// list of item positions in h-axis
	private boolean[] markW;// markW[i] = true if item position in w-axis is
							// enabled
	private boolean[] markL;// markL[i] = true if item position in L-axis is
							// enabled
	private boolean[] markH;// markH[i] = true if item position in H-axis is
							// enabled

	public ArrayList<Move3D> solution = null;

	// backup data structures
	private ArrayList<Position3D> bku_candidate_positions;

	private int[][][] bku_occ;// occ[iw][il][ih] = 1 if cell occ[iw][il[ih] is
								// occupied
	private ArrayList<Integer> bku_LW;// list of item positions in w-axis
	private ArrayList<Integer> bku_LL;// list of item positions in l-axis
	private ArrayList<Integer> bku_LH;// list of item positions in h-axis
	private boolean[] bku_markW;// markW[i] = true if item position in w-axis is
	// enabled
	private boolean[] bku_markL;// markL[i] = true if item position in L-axis is
	// enabled
	private boolean[] bku_markH;// markH[i] = true if item position in H-axis is
	// enabled
	ArrayList<Move3D> bku_solution = null;

	public GreedyConstructiveOrderLoadConstraintMark() {

	}

	public GreedyConstructiveOrderLoadConstraintMark(Model3D model, RoutingLoad3DSolver solver) {
		this.model = model;
		this.container = model.getContainer();
		this.items = model.getItems();
		this.solver = solver;
	}

	public ArrayList<Move3D> getSolution(){
		return solution;
	}
	public void backup() {
		bku_candidate_positions.clear();
		for (int i = 0; i < candidate_positions.size(); i++)
			bku_candidate_positions.add(candidate_positions.get(i));

		for (int iw = 0; iw < container.getWidth(); iw++)
			for (int il = 0; il < container.getLength(); il++)
				for (int ih = 0; ih < container.getHeight(); ih++)
					bku_occ[iw][il][ih] = occ[iw][il][ih];

		bku_LW.clear();
		for (int i = 0; i < LW.size(); i++)
			bku_LW.add(LW.get(i));

		bku_LL.clear();
		for (int i = 0; i < LL.size(); i++)
			bku_LL.add(LL.get(i));

		bku_LH.clear();
		for (int i = 0; i < LH.size(); i++)
			bku_LH.add(LH.get(i));

		for (int i = 0; i < markW.length; i++)
			bku_markW[i] = markW[i];
		for (int i = 0; i < markL.length; i++)
			bku_markL[i] = markL[i];
		for (int i = 0; i < markH.length; i++)
			bku_markH[i] = markH[i];

		bku_solution.clear();
		for (int i = 0; i < solution.size(); i++)
			bku_solution.add(solution.get(i));

	}

	public void restore() {
		candidate_positions.clear();
		for (int i = 0; i < bku_candidate_positions.size(); i++)
			candidate_positions.add(bku_candidate_positions.get(i));

		for (int iw = 0; iw < container.getWidth(); iw++)
			for (int il = 0; il < container.getLength(); il++)
				for (int ih = 0; ih < container.getHeight(); ih++)
					occ[iw][il][ih] = bku_occ[iw][il][ih];

		LW.clear();
		for (int i = 0; i < bku_LW.size(); i++)
			LW.add(bku_LW.get(i));

		LL.clear();
		for (int i = 0; i < bku_LL.size(); i++)
			LL.add(bku_LL.get(i));

		LH.clear();
		for (int i = 0; i < bku_LH.size(); i++)
			LH.add(bku_LH.get(i));

		for (int i = 0; i < bku_markW.length; i++)
			markW[i] = bku_markW[i];
		for (int i = 0; i < bku_markL.length; i++)
			markL[i] = bku_markL[i];
		for (int i = 0; i < bku_markH.length; i++)
			markH[i] = bku_markH[i];

		solution.clear();
		for (int i = 0; i < bku_solution.size(); i++)
			solution.add(bku_solution.get(i));

	}

	public void printCandidatePositions() {
		System.out.println("candidate_positions.sz = "
				+ candidate_positions.size());
		for (Position3D p : candidate_positions) {
			System.out
					.println(p.getX_w() + "," + p.getX_l() + "," + p.getX_h());
		}
	}

	public void readDataReal(String fn) {
		try {
			Scanner in = new Scanner(new File(fn));
			int W = in.nextInt();
			int L = in.nextInt();
			int H = in.nextInt();
			container = new Container3D(W, L, H);
			ArrayList<Item3D> list = new ArrayList<Item3D>();
			HashMap<Item3D, Integer> mo = new HashMap<Item3D, Integer>();
			int idx = -1;
			while (true) {
				int w = in.nextInt();
				if (w == -1)
					break;
				int l = in.nextInt();
				int h = in.nextInt();
				int volumn = in.nextInt();
				int orderID = in.nextInt();
				for (int i = 0; i < volumn; i++) {
					Item3D I = new Item3D(idx + 1, w, l, h);
					list.add(I);
					idx++;
					mo.put(I, idx);
				}
			}
			o = new int[list.size()];
			items = new Item3D[list.size()];
			for (int i = 0; i < list.size(); i++) {
				items[i] = list.get(i);
				o[i] = mo.get(items[i]);
			}
			model = new Model3D(container, items);

			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void readData(String fn) {
		try {
			Scanner in = new Scanner(new File(fn));
			int W = in.nextInt();
			int L = in.nextInt();
			int H = in.nextInt();
			container = new Container3D(W, L, H);
			ArrayList<Item3D> list = new ArrayList<Item3D>();
			while (true) {
				int w = in.nextInt();
				if (w == -1)
					break;
				int l = in.nextInt();
				int h = in.nextInt();
				Item3D item = new Item3D(list.size(), w, l, h);
				list.add(item);
			}
			in.close();

			items = new Item3D[list.size()];
			for (int i = 0; i < list.size(); i++)
				items[i] = list.get(i);

			model = new Model3D(container, items);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public String name(){
		return "GreedyConstructiveOrderLoadConstraint";
	}
	public void init() {
		candidate_positions = new ArrayList<Position3D>();
		// o = new int[model.getItems().length];
		int width = model.getContainer().getWidth();
		int length = model.getContainer()
						.getLength();
		int height = model.getContainer().getHeight();
		System.out.println(name() + "::init, width = " + width + ", length = " + length + ", height = " + height);
		occ = new int[width][length][height];
		for (int i = 0; i < model.getContainer().getWidth(); i++)
			for (int j = 0; j < model.getContainer().getLength(); j++)
				for (int k = 0; k < model.getContainer().getHeight(); k++)
					occ[i][j][k] = 0;

		markW = new boolean[container.getWidth() + 1];
		markL = new boolean[container.getLength() + 1];
		markH = new boolean[container.getHeight() + 1];

		Arrays.fill(markW, false);
		Arrays.fill(markL, false);
		Arrays.fill(markH, false);
		LL = new ArrayList<Integer>();
		LW = new ArrayList<Integer>();
		LH = new ArrayList<Integer>();

		candidate_positions = new ArrayList<Position3D>();
		candidate_positions.add(new Position3D(0, 0, 0));
		candidate_positions.add(new Position3D(model.getContainer().getWidth(),
				0, 0));
		LW.add(0);
		LL.add(0);
		LH.add(0);
		LW.add(container.getWidth());
		markW[0] = true;
		markL[0] = true;
		markH[0] = true;
		markW[container.getWidth()] = true;

		solution = new ArrayList<Move3D>();

		// backup data structures
		bku_candidate_positions = new ArrayList<Position3D>();
		bku_occ = new int[model.getContainer().getWidth()][model.getContainer()
				.getLength()][model.getContainer().getHeight()];
		bku_markW = new boolean[container.getWidth() + 1];
		bku_markL = new boolean[container.getLength() + 1];
		bku_markH = new boolean[container.getHeight() + 1];

		Arrays.fill(bku_markW, false);
		Arrays.fill(bku_markL, false);
		Arrays.fill(bku_markH, false);
		bku_LL = new ArrayList<Integer>();
		bku_LW = new ArrayList<Integer>();
		bku_LH = new ArrayList<Integer>();

		bku_solution = new ArrayList<Move3D>();

		// x_w = new int[items.length];
		// x_l = new int[items.length];
		// x_h = new int[items.length];

	}

	public boolean tryLoad(ArrayList<Item3D> tried_items) {
		backup();

		for (int i = 0; i < tried_items.size(); i++) {
			Item3D item = tried_items.get(i);
			ArrayList<Move3D> moves = new ArrayList<Move3D>();

			for (Position3D p : candidate_positions) {
				int w = item.getWidth();
				int l = item.getLength();
				int h = item.getHeight();

				// generate all permutations of (w,l,h)
				RotationGenerator RG = new RotationGenerator(w, l, h);
				RG.generate();
				ArrayList<Item3D> gen_items = RG.getItems();

				for (Item3D I : gen_items) {
					// System.out.println("Consider item " + item +
					// " with permutation " + I);
					if (feasiblePosition(p.getX_w(), p.getX_l(), p.getX_h(),
							I.getWidth(), I.getLength(), I.getHeight())) {
						moves.add(new Move3D(p, I.getWidth(), I.getLength(), I
								.getHeight(), item.getItemID()));
					}
				}
			}

			if (moves.size() <= 0) {// cannot load, restore and return false
				restore();
				return false;
			} else {
				Move3D sel_move = selectBest(moves);
				Position3D sel_p = sel_move.getPosition();
				place(sel_move.getW(), sel_move.getL(), sel_move.getH(), sel_p);
				solution.add(sel_move);
			}

		}

		restore();
		return true;
	}

	public boolean load(ArrayList<Item3D> tried_items) {
		// backup();

		for (int i = 0; i < tried_items.size(); i++) {
			Item3D item = tried_items.get(i);
			ArrayList<Move3D> moves = new ArrayList<Move3D>();

			for (Position3D p : candidate_positions) {
				int w = item.getWidth();
				int l = item.getLength();
				int h = item.getHeight();

				// generate all permutations of (w,l,h)
				RotationGenerator RG = new RotationGenerator(w, l, h);
				RG.generate();
				ArrayList<Item3D> gen_items = RG.getItems();

				for (Item3D I : gen_items) {
					// System.out.println("Consider item " + item +
					// " with permutation " + I);
					if (feasiblePosition(p.getX_w(), p.getX_l(), p.getX_h(),
							I.getWidth(), I.getLength(), I.getHeight())) {
						moves.add(new Move3D(p, I.getWidth(), I.getLength(), I
								.getHeight(), item.getItemID()));
					}
				}
			}

			if (moves.size() <= 0) {// cannot load, restore and return false
				// restore();
				return false;
			} else {
				Move3D sel_move = selectBest(moves);
				Position3D sel_p = sel_move.getPosition();
				place(sel_move.getW(), sel_move.getL(), sel_move.getH(), sel_p);
				solution.add(sel_move);
			}

		}

		// restore();
		return true;
	}

	public void printSolution() {
		for (int i = 0; i < solution.size(); i++) {
			Move3D m = solution.get(i);
			String sw = "Sat mep trai";
			if (m.getPosition().getX_w() > 0) {
				for (int j = 0; j < i; j++) {
					Move3D mj = solution.get(j);
					if (mj.getPosition().getX_w() + mj.getW() == m
							.getPosition().getX_w()) {
						sw = "Ben trai item " + mj.getItemID();
					}
				}
			}
			String sl = "Sat vach sau";
			if (m.getPosition().getX_l() > 0) {
				for (int j = 0; j < i; j++) {
					Move3D mj = solution.get(j);
					if (mj.getPosition().getX_l() + mj.getL() == m
							.getPosition().getX_l()) {
						sl = "Truoc item " + mj.getItemID();
					}
				}
			}
			String sh = "Tren san";
			if (m.getPosition().getX_h() > 0) {
				for (int j = 0; j < i; j++) {
					Move3D mj = solution.get(j);
					if (mj.getPosition().getX_h() + mj.getH() == m
							.getPosition().getX_h()) {
						sh = "Tren item " + mj.getItemID();
					}
				}
			}
			System.out.println("solution item " + m.getItemID() + ": "
					+ m.getW() + "," + m.getL() + "," + m.getH() + " at "
					+ m.getPosition() + " : " + sw + ", " + sl + ", " + sh);
		}
	}

	public void solve() {
		init();
		// for(int i = 0; i < o.length; i++) o[i] = i;
		solve(o);
		// for(int i = 0; i < items.length; i++){
		// System.out.println("Item " + i + " " + items[i] + " : at " + x_w[i] +
		// "," + x_l[i] + "," + x_h[i]);
		// }

		printSolution();
	}

	public Move3D selectBest(ArrayList<Move3D> moves) {
		int minH = Integer.MAX_VALUE;
		int minL = Integer.MAX_VALUE;
		Move3D sel_move = null;
		for (Move3D m : moves) {
			// System.out.println("selectBest consider move " + m.getL() + "," +
			// m.getH() + ", minL = " + minL + ", minH = " + minH);
			if (m.getPosition().getX_l() + m.getL() < minL) {
				sel_move = m;
				minL = m.getL() + m.getPosition().getX_l();
				minH = m.getPosition().getX_h();
				// System.out.println("selectBest, update L move " + m.getL() +
				// "," + m.getH() + ", minL = " + minL + ", minH = " + minH);
			} else if (m.getL() + m.getPosition().getX_l() == minL) {
				if (m.getPosition().getX_h() < minH) {
					minH = m.getPosition().getX_h();
					sel_move = m;
					// System.out.println("selectBest, update H move " +
					// m.getL() + "," + m.getH() + ", minL = " + minL +
					// ", minH = " + minH);
				}
			}
		}
		return sel_move;
	}

	public void solve(int[] o) {
		this.o = o;
		// this.x_w = model.getX_w();
		// this.x_l = model.getX_l();
		// this.x_h = model.getX_h();

		for (int i = 0; i < o.length; i++) {
			int item = o[i];
			Position3D sel_p = null;
			ArrayList<Move3D> moves = new ArrayList<Move3D>();

			for (Position3D p : candidate_positions) {
				int w = model.getItems()[item].getWidth();
				int l = model.getItems()[item].getLength();
				int h = model.getItems()[item].getHeight();

				// generate all permutations of (w,l,h)
				RotationGenerator RG = new RotationGenerator(w, l, h);
				RG.generate();
				ArrayList<Item3D> gen_items = RG.getItems();

				for (Item3D I : gen_items) {
					// System.out.println("Consider item " + item +
					// " with permutation " + I);
					if (feasiblePosition(p.getX_w(), p.getX_l(), p.getX_h(),
							I.getWidth(), I.getLength(), I.getHeight())) {
						moves.add(new Move3D(p, I.getWidth(), I.getLength(), I
								.getHeight(), item));
					}
				}
			}

			Move3D sel_move = selectBest(moves);
			sel_p = sel_move.getPosition();
			place(sel_move.getW(), sel_move.getL(), sel_move.getH(), sel_p);
			solution.add(sel_move);

			System.out.println("place item " + i + " at " + sel_p
					+ ", candidate_position = " + candidate_positions.size()
					+ ", moves = " + moves.size());
			// printCandidatePositions();
			// System.out.println("---------------");
		}
	}

	public int volumn(int w, int l, int h) {
		return w * l * h;
	}

	public boolean overlap(int pos_w1, int w1, int pos_l1, int l1, int pos_w2,
			int w2, int pos_l2, int l2) {
		boolean novl = pos_w1 + w1 <= pos_w2 || pos_w2 + l2 <= pos_w1
				|| pos_l1 + l1 <= pos_l2 || pos_l2 + l2 <= pos_l1;
		return !novl;
	}
	
	
	public boolean feasiblePosition(int pos_w, int pos_l, int pos_h, int w,
			int l, int h) {
		// return true if item (w,l,h) can be placed at position (pos_w, pos_l,
		// pos_h) without violating any constraints
		if (pos_w + w > container.getWidth())
			return false;
		if (pos_l + l > container.getLength())
			return false;
		if (pos_h + h > container.getHeight())
			return false;

		for (int iw = pos_w; iw < pos_w + w; iw++) {
			for (int il = pos_l; il < pos_l + l; il++) {
				for (int ih = pos_h; ih < pos_h + h; ih++) {
					if (occ[iw][il][ih] > 0)
						return false;
				}
			}
		}

		for (int i = 0; i < solution.size(); i++) {
			Move3D m = solution.get(i);
			// check if item m is under (pos_w, pos_l, pos_h) but has smaller volumn, size
			if (volumn(m.getW(), m.getL(), m.getH()) < volumn(w, l, h)
					&& overlap(m.getPosition().getX_w(), m.getW(), m
							.getPosition().getX_l(), m.getL(), pos_w, w, pos_l,
							l)
					// && h > m.getH()
					&& pos_h == m.getH() + m.getPosition().getX_h()) {
				return false;
			}
				
			// check if there exists an item in front of (pos_w, pos_l, pos_h) and was loaded in solution
			if(overlap(m.getPosition().getX_w(),m.getW(),m.getPosition().getX_h(),m.getH(),pos_w,w,pos_h,h)
					&& m.getPosition().getX_l() > pos_l
					){
				return false;
			}
		}

		// count the number of occupied cells under pos_h
		if (pos_h > 0) {
			int count1 = 0;
			int count0 = 0;
			for (int i = pos_w; i < pos_w + w; i++)
				for (int j = pos_l; j < pos_l + l; j++)
					if (occ[i][j][pos_h - 1] > 0)
						count1++;
					else
						count0++;
			double f = count1*1.0/(count1 + count0);
			if(f < solver.getInput().getConfigParams().getMinOccupyPad()) return false;
		}

		
		
		return true;
	}

	public void place(int w, int l, int h, Position3D p) {
		// place the ith items (items[i]) at position p w.r.t size (w,l,h),
		// update candidate_positions
		// x_w[i] = p.getX_w();
		// x_l[i] = p.getX_l();
		// x_h[i] = p.getX_h();

		for (int iw = p.getX_w(); iw < w + p.getX_w(); iw++) {
			for (int il = p.getX_l(); il < l + p.getX_l(); il++) {
				for (int ih = p.getX_h(); ih < h + p.getX_h(); ih++) {
					occ[iw][il][ih]++;
				}
			}
		}

		// update candidate_positions
		int iw = p.getX_w() + w;
		int il = p.getX_l() + l;
		int ih = p.getX_h() + h;
		boolean newW = false;
		boolean newL = false;
		boolean newH = false;
		if (!markW[iw]) {
			markW[iw] = true;
			newW = true;
			// LW.add(iw);
			for (int jl : LL) {
				for (int jh : LH) {
					if (checkCandidatePosition(iw, jl, ih)) {

						Position3D cp = new Position3D(iw, jl, jh);
						candidate_positions.add(cp);
					}
				}
			}

		}
		if (!markL[il]) {
			markL[il] = true;
			newL = true;
			// LL.add(il);
			for (int jw : LW) {
				for (int jh : LH) {
					if (checkCandidatePosition(jw, il, jh)) {

						Position3D cp = new Position3D(jw, il, jh);
						candidate_positions.add(cp);
					}
				}
			}
		}
		if (!markH[ih]) {
			markH[ih] = true;
			newH = true;
			// LH.add(ih);
			for (int jw : LW) {
				for (int jl : LL) {
					if (checkCandidatePosition(jw, jl, ih)) {
						Position3D cp = new Position3D(jw, jl, ih);
						candidate_positions.add(cp);
					}
				}
			}
		}
		if (newW && newL && newH) {
			for (int jh : LH) {
				if (checkCandidatePosition(iw, il, jh)) {
					Position3D cp = new Position3D(iw, il, jh);
					candidate_positions.add(cp);
				}
			}
			for (int jl : LL) {
				if (checkCandidatePosition(iw, jl, ih)) {
					Position3D cp = new Position3D(iw, jl, ih);
					candidate_positions.add(cp);
				}
			}
			for (int jw : LW) {
				if (checkCandidatePosition(jw, il, ih)) {
					Position3D cp = new Position3D(jw, il, ih);
					candidate_positions.add(cp);
				}
			}
			if (checkCandidatePosition(iw, il, ih)) {
				Position3D cp = new Position3D(iw, il, ih);
				candidate_positions.add(cp);
			}

		} else if (newW && newL && !newH) {
			for (int jh : LH) {
				if (checkCandidatePosition(iw, il, jh)) {
					Position3D cp = new Position3D(iw, il, jh);
					candidate_positions.add(cp);
				}
			}
		} else if (newW && !newL && newH) {
			for (int jl : LL) {
				if (checkCandidatePosition(iw, jl, ih)) {
					Position3D cp = new Position3D(iw, jl, ih);
					candidate_positions.add(cp);
				}
			}
		} else if (newW && !newL && !newH) {
			// do nothing
		} else if (!newW && newL && newH) {
			for (int jw : LW) {
				if (checkCandidatePosition(jw, il, ih)) {
					Position3D cp = new Position3D(jw, il, ih);
					candidate_positions.add(cp);
				}
			}
		} else if (!newW && newL && !newH) {
			// do nothing
		} else if (!newW && !newL && newH) {
			// do nothing
		} else if (!newW && !newL && !newH) {
			// do nothing
		}
		if (newW)
			LW.add(iw);
		if (newL)
			LL.add(il);
		if (newH)
			LH.add(ih);
	}

	public boolean checkCandidatePosition(int xw, int xl, int xh) {
		// return true if position(xw, xl, xh) can be used to placed an item
		if (xw >= container.getWidth())
			return false;
		if (xl >= container.getLength())
			return false;
		if (xh >= container.getHeight())
			return false;

		for (int il = xl; il < model.getContainer().getLength(); il++) {
			if (occ[xw][il][xh] > 0)
				return false;
		}
		return true;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		GreedyConstructiveOrderLoadConstraintMark S = new GreedyConstructiveOrderLoadConstraintMark();
		// S.readData("data/bin-packing3D/bp3d.txt");
		S.readDataReal("data/bp3d-200-400-200.txt");

		S.solve();
	}

}
