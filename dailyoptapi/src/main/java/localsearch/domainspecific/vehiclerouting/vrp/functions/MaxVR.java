/**
 * Copyright (c) 2015,
 *      Pham Quang Dung (dungkhmt@gmail.com),
 *      Nguyen Thanh Hoang (thnbk56@gmail.com),
 *	Nguyen Van Thanh (nvthanh1994@gmail.com),
 *	Le Kim Thu (thulek@gmail.com) 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * authors: Pham Quang Dung (dungkhmt@gmail.com)
 * date: 11/09/2015
 */

package localsearch.domainspecific.vehiclerouting.vrp.functions;

import java.util.ArrayList;

import localsearch.domainspecific.vehiclerouting.vrp.CBLSVR;
import localsearch.domainspecific.vehiclerouting.vrp.IFunctionVR;
import localsearch.domainspecific.vehiclerouting.vrp.VRManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;

public class MaxVR implements IFunctionVR {

	private ArrayList<IFunctionVR> functions;
	private double value;
	
	public MaxVR(IFunctionVR[] f){
		functions = new ArrayList<IFunctionVR>();
		for(int i = 0; i < f.length; i++)
			functions.add(f[i]);
		
		functions.get(0).getVRManager().post(this);
	}
	
	public VRManager getVRManager() {
		// TODO Auto-generated method stub
		return functions.get(0).getVRManager();
	}

	
	public double getValue() {
		// TODO Auto-generated method stub
		return value;
	}

	
	public double evaluateOnePointMove(Point x, Point y) {
		// TODO Auto-generated method stub
		double nMax = 1-CBLSVR.MAX_INT;
		for(IFunctionVR f : functions){
			double v = f.evaluateOnePointMove(x, y) 
					+ f.getValue();
			nMax = nMax > v ? nMax : v;
		}
		return nMax - value;
	}

	
	public double evaluateTwoPointsMove(Point x, Point y) {
		// TODO Auto-generated method stub
		double nMax = 1-CBLSVR.MAX_INT;
		for(IFunctionVR f : functions){
			double v = f.evaluateTwoPointsMove(x, y) 
					+ f.getValue();
			nMax = nMax > v ? nMax : v;
		}
		return nMax - value;
	}

	
	public double evaluateTwoOptMove1(Point x, Point y) {
		// TODO Auto-generated method stub
		double nMax = 1-CBLSVR.MAX_INT;
		for(IFunctionVR f : functions){
			double v = f.evaluateTwoOptMove1(x, y) 
					+ f.getValue();
			nMax = nMax > v ? nMax : v;
		}
		return nMax - value;
	}

	
	public double evaluateTwoOptMove2(Point x, Point y) {
		// TODO Auto-generated method stub
		double nMax = 1-CBLSVR.MAX_INT;
		for(IFunctionVR f : functions){
			double v = f.evaluateTwoOptMove2(x, y) 
					+ f.getValue();
			nMax = nMax > v ? nMax : v;
		}
		return nMax - value;
	}

	
	public double evaluateTwoOptMove3(Point x, Point y) {
		// TODO Auto-generated method stub
		double nMax = 1-CBLSVR.MAX_INT;
		for(IFunctionVR f : functions){
			double v = f.evaluateTwoOptMove3(x, y) 
					+ f.getValue();
			nMax = nMax > v ? nMax : v;
		}
		return nMax - value;
	}

	
	public double evaluateTwoOptMove4(Point x, Point y) {
		// TODO Auto-generated method stub
		double nMax = 1-CBLSVR.MAX_INT;
		for(IFunctionVR f : functions){
			double v = f.evaluateTwoOptMove4(x, y) 
					+ f.getValue();
			nMax = nMax > v ? nMax : v;
		}
		return nMax - value;
	}

	
	public double evaluateTwoOptMove5(Point x, Point y) {
		// TODO Auto-generated method stub
		double nMax = 1-CBLSVR.MAX_INT;
		for(IFunctionVR f : functions){
			double v = f.evaluateTwoOptMove5(x, y) 
					+ f.getValue();
			nMax = nMax > v ? nMax : v;
		}
		return nMax - value;
	}

	
	public double evaluateTwoOptMove6(Point x, Point y) {
		// TODO Auto-generated method stub
		double nMax = 1-CBLSVR.MAX_INT;
		for(IFunctionVR f : functions){
			double v = f.evaluateTwoOptMove6(x, y) 
					+ f.getValue();
			nMax = nMax > v ? nMax : v;
		}
		return nMax - value;
	}

	
	public double evaluateTwoOptMove7(Point x, Point y) {
		// TODO Auto-generated method stub
		double nMax = 1-CBLSVR.MAX_INT;
		for(IFunctionVR f : functions){
			double v = f.evaluateTwoOptMove7(x, y) 
					+ f.getValue();
			nMax = nMax > v ? nMax : v;
		}
		return nMax - value;
	}

	
	public double evaluateTwoOptMove8(Point x, Point y) {
		// TODO Auto-generated method stub
		double nMax = 1-CBLSVR.MAX_INT;
		for(IFunctionVR f : functions){
			double v = f.evaluateTwoOptMove8(x, y) 
					+ f.getValue();
			nMax = nMax > v ? nMax : v;
		}
		return nMax - value;
	}

	
	public double evaluateOrOptMove1(Point x1, Point x2, Point y) {
		// TODO Auto-generated method stub
		double nMax = 1-CBLSVR.MAX_INT;
		for(IFunctionVR f : functions){
			double v = f.evaluateOrOptMove1(x1, x2, y)
					+ f.getValue();
			nMax = nMax > v ? nMax : v;
		}
		return nMax - value;
	}

	
	public double evaluateOrOptMove2(Point x1, Point x2, Point y) {
		// TODO Auto-generated method stub
		double nMax = 1-CBLSVR.MAX_INT;
		for(IFunctionVR f : functions){
			double v = f.evaluateOrOptMove2(x1, x2, y)
					+ f.getValue();
			nMax = nMax > v ? nMax : v;
		}
		return nMax - value;
	}

	
	public double evaluateThreeOptMove1(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		double nMax = 1-CBLSVR.MAX_INT;
		for(IFunctionVR f : functions){
			double v = f.evaluateThreeOptMove1(x, y, z)
					+ f.getValue();
			nMax = nMax > v ? nMax : v;
		}
		return nMax - value;
	}

	
	public double evaluateThreeOptMove2(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		double nMax = 1-CBLSVR.MAX_INT;
		for(IFunctionVR f : functions){
			double v = f.evaluateThreeOptMove2(x, y, z)
					+ f.getValue();
			nMax = nMax > v ? nMax : v;
		}
		return nMax - value;
	}

	
	public double evaluateThreeOptMove3(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		double nMax = 1-CBLSVR.MAX_INT;
		for(IFunctionVR f : functions){
			double v = f.evaluateThreeOptMove3(x, y, z)
					+ f.getValue();
			nMax = nMax > v ? nMax : v;
		}
		return nMax - value;
	}

	
	public double evaluateThreeOptMove4(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		double nMax = 1-CBLSVR.MAX_INT;
		for(IFunctionVR f : functions){
			double v = f.evaluateThreeOptMove4(x, y, z)
					+ f.getValue();
			nMax = nMax > v ? nMax : v;
		}
		return nMax - value;
	}

	
	public double evaluateThreeOptMove5(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		double nMax = 1-CBLSVR.MAX_INT;
		for(IFunctionVR f : functions){
			double v = f.evaluateThreeOptMove5(x, y, z)
					+ f.getValue();
			nMax = nMax > v ? nMax : v;
		}
		return nMax - value;
	}

	
	public double evaluateThreeOptMove6(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		double nMax = 1-CBLSVR.MAX_INT;
		for(IFunctionVR f : functions){
			double v = f.evaluateThreeOptMove6(x, y, z)
					+ f.getValue();
			nMax = nMax > v ? nMax : v;
		}
		return nMax - value;
	}

	
	public double evaluateThreeOptMove7(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		double nMax = 1-CBLSVR.MAX_INT;
		for(IFunctionVR f : functions){
			double v = f.evaluateThreeOptMove7(x, y, z)
					+ f.getValue();
			nMax = nMax > v ? nMax : v;
		}
		return nMax - value;
	}

	
	public double evaluateThreeOptMove8(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		double nMax = 1-CBLSVR.MAX_INT;
		for(IFunctionVR f : functions){
			double v = f.evaluateThreeOptMove8(x, y, z)
					+ f.getValue();
			nMax = nMax > v ? nMax : v;
		}
		return nMax - value;
	}

	
	public double evaluateCrossExchangeMove(Point x1, Point y1, Point x2, Point y2) {
		// TODO Auto-generated method stub
		double nMax = 1-CBLSVR.MAX_INT;
		for(IFunctionVR f : functions){
			double v = f.evaluateCrossExchangeMove(x1, y1, x2, y2)
					+ f.getValue();
			nMax = nMax > v ? nMax : v;
		}
		return nMax - value;
	}

	
	public void initPropagation() {
		// TODO Auto-generated method stub
		value = 1-CBLSVR.MAX_INT;
		for(IFunctionVR f : functions){
			value = value > f.getValue() ? value : f.getValue();
		}
		//System.out.println(name() + "::initPropagation, value = " + value);
		//System.exit(-1);
	}

	
	public void propagateOnePointMove(Point x, Point y) {
		// TODO Auto-generated method stub
		initPropagation();
	}

	
	public void propagateTwoPointsMove(Point x, Point y) {
		// TODO Auto-generated method stub
		initPropagation();
	}

	
	public void propagateTwoOptMove1(Point x, Point y) {
		// TODO Auto-generated method stub
		initPropagation();
	}

	
	public void propagateTwoOptMove2(Point x, Point y) {
		// TODO Auto-generated method stub
		initPropagation();
	}

	
	public void propagateTwoOptMove3(Point x, Point y) {
		// TODO Auto-generated method stub
		initPropagation();
	}

	
	public void propagateTwoOptMove4(Point x, Point y) {
		// TODO Auto-generated method stub
		initPropagation();
	}

	
	public void propagateTwoOptMove5(Point x, Point y) {
		// TODO Auto-generated method stub
		initPropagation();
	}

	
	public void propagateTwoOptMove6(Point x, Point y) {
		// TODO Auto-generated method stub
		initPropagation();
	}

	
	public void propagateTwoOptMove7(Point x, Point y) {
		// TODO Auto-generated method stub
		initPropagation();
	}

	
	public void propagateTwoOptMove8(Point x, Point y) {
		// TODO Auto-generated method stub
		initPropagation();
	}

	
	public void propagateOrOptMove1(Point x1, Point x2, Point y) {
		// TODO Auto-generated method stub
		initPropagation();
	}

	
	public void propagateOrOptMove2(Point x1, Point x2, Point y) {
		// TODO Auto-generated method stub
		initPropagation();
	}

	
	public void propagateThreeOptMove1(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		initPropagation();
	}

	
	public void propagateThreeOptMove2(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		initPropagation();
	}

	
	public void propagateThreeOptMove3(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		initPropagation();
	}

	
	public void propagateThreeOptMove4(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		initPropagation();
	}

	
	public void propagateThreeOptMove5(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		initPropagation();
	}

	
	public void propagateThreeOptMove6(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		initPropagation();
	}

	
	public void propagateThreeOptMove7(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		initPropagation();
	}

	
	public void propagateThreeOptMove8(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		initPropagation();
	}

	
	public void propagateCrossExchangeMove(Point x1, Point y1, Point x2, Point y2) {
		// TODO Auto-generated method stub
		initPropagation();
	}

	
	public String name(){
		return "Max";
	}
	
	public void propagateTwoPointsMove(Point x1, Point x2, Point y1, Point y2) {
		// TODO Auto-generated method stub
		initPropagation();
	}
	
	public void propagateThreePointsMove(Point x1, Point x2, Point x3, Point y1,
			Point y2, Point y3) {
		// TODO Auto-generated method stub
		initPropagation();
	}
	
	public void propagateFourPointsMove(Point x1, Point x2, Point x3, Point x4, Point y1,
			Point y2, Point y3, Point y4) {
		// TODO Auto-generated method stub
		initPropagation();
	}
	
	public void propagateAddOnePoint(Point x, Point y) {
		// TODO Auto-generated method stub
		initPropagation();
	}
	
	public void propagateRemoveOnePoint(Point x) {
		// TODO Auto-generated method stub
		initPropagation();
	}
	
	public double evaluateTwoPointsMove(Point x1, Point x2, Point y1, Point y2) {
		// TODO Auto-generated method stub
		double nMax = 1-CBLSVR.MAX_INT;
		for(IFunctionVR f : functions){
			double v = f.evaluateTwoPointsMove(x1, x2, y1, y2)
					+ f.getValue();
			nMax = nMax > v ? nMax : v;
		}
		return nMax - value;
	}
	
	public double evaluateThreePointsMove(Point x1, Point x2, Point x3, Point y1,
			Point y2, Point y3) {
		// TODO Auto-generated method stub
		double nMax = 1-CBLSVR.MAX_INT;
		for(IFunctionVR f : functions){
			double v = f.evaluateThreePointsMove(x1, x2, x3, y1, y2, y3)
					+ f.getValue();
			nMax = nMax > v ? nMax : v;
		}
		return nMax - value;
	}
	
	public double evaluateFourPointsMove(Point x1, Point x2, Point x3, Point x4,
			Point y1, Point y2, Point y3, Point y4) {
		// TODO Auto-generated method stub
		double nMax = 1-CBLSVR.MAX_INT;
		for(IFunctionVR f : functions){
			double v = f.evaluateFourPointsMove(x1, x2, x3, x4, y1, y2, y3, y4)
					+ f.getValue();
			nMax = nMax > v ? nMax : v;
		}
		return nMax - value;
	}
	
	public double evaluateAddOnePoint(Point x, Point y) {
		// TODO Auto-generated method stub
		double nMax = 1-CBLSVR.MAX_INT;
		for(IFunctionVR f : functions){
			double v = f.evaluateAddOnePoint(x, y)
					+ f.getValue();
			nMax = nMax > v ? nMax : v;
		}
		return nMax - value;
	}
	
	public double evaluateRemoveOnePoint(Point x) {
		// TODO Auto-generated method stub
		double nMax = 1-CBLSVR.MAX_INT;
		for(IFunctionVR f : functions){
			double v = f.evaluateRemoveOnePoint(x)
					+ f.getValue();
			nMax = nMax > v ? nMax : v;
		}
		return nMax - value;
	}
	
	public void propagateAddRemovePoints(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		initPropagation();
	}
	
	public double evaluateAddRemovePoints(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		double nMax = 1-CBLSVR.MAX_INT;
		for(IFunctionVR f : functions){
			double v = f.evaluateAddRemovePoints(x, y, z)
					+ f.getValue();
			nMax = nMax > v ? nMax : v;
		}
		return nMax - value;
	}
	
	
	public void propagateKPointsMove(ArrayList<Point> x, ArrayList<Point> y) {
		// TODO Auto-generated method stub
		initPropagation();
	}
	
	
	public double evaluateKPointsMove(ArrayList<Point> x, ArrayList<Point> y) {
		// TODO Auto-generated method stub
		double nMax = 1-CBLSVR.MAX_INT;
		for(IFunctionVR f : functions){
			double v = f.evaluateKPointsMove(x, y)
					+ f.getValue();
			nMax = nMax > v ? nMax : v;
		}
		return nMax - value;
	}
	
}