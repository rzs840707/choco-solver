/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 03/10/11
 * Time: 19:56
 */

package solver.constraints.propagators.nary;

import choco.kernel.ESat;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IStateInt;
import gnu.trove.list.array.TIntArrayList;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;

import java.util.BitSet;

/**
 * Subcircuit propagator (one circuit and several loops)
 */
public class PropSubcircuit extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int n;
	private IntVar length;
    private IStateInt[] origin, end, size;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropSubcircuit(IntVar[] vars, IntVar length, Constraint constraint, Solver solver) {
        super(vars, solver, constraint, PropagatorPriority.UNARY, true);
        n = vars.length;
		this.length = length;
        origin = new IStateInt[n];
        end = new IStateInt[n];
        size = new IStateInt[n];
        for (int i = 0; i < n; i++) {
            origin[i] = environment.makeInt(i);
            end[i] = environment.makeInt(i);
            size[i] = environment.makeInt(1);
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        TIntArrayList fixedVar = new TIntArrayList();
        for (int i = 0; i < n; i++) {
			vars[i].updateLowerBound(0,this);
			vars[i].updateUpperBound(n-1,this);
            if (vars[i].instantiated() && i!=vars[i].getValue()) {
                fixedVar.add(i);
            }
        }
        for (int i = 0; i < fixedVar.size(); i++) {
            varInstantiated(fixedVar.get(i), vars[fixedVar.get(i)].getValue());
        }
    }

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
        int next = vars[idxVarInProp].getValue();
		if(idxVarInProp!=next){
			varInstantiated(idxVarInProp, next);
			vars[next].removeValue(next,this);
		}
    }

    private void varInstantiated(int var, int val) throws ContradictionException {
        int last = end[val].get();
        int start = origin[var].get();
        if (origin[val].get() != val) {
            contradiction(vars[var], "");
        }
        if (val == start) {
			length.instantiateTo(size[start].get(),this);
        } else {
            size[start].add(size[val].get());
			if (size[start].get() == length.getUB()) {
                vars[last].instantiateTo(start, this);
            }
            if (size[start].get() < length.getLB()) {
                vars[last].removeValue(start, this);
            }
            origin[last].set(start);
            end[start].set(last);
        }
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INSTANTIATE.mask;
    }

    @Override
    public ESat isEntailed() {
		if(!length.instantiated()){
			return ESat.UNDEFINED;
		}
        for (int i = 0; i < n; i++) {
            if (!vars[i].instantiated()) {
                return ESat.UNDEFINED;
            }
        }
        BitSet visited = new BitSet(n);
        int i = 0;
		while(i<n && vars[i].getValue()==i){
			i++;
		}
		if(i==n){
			if(length.getValue()==0){
				return ESat.TRUE;
			}else{
				return ESat.FALSE;
			}
		}
		int first = i;
        int size = 0;
        while (size != length.getValue()) {
            size++;
            i = vars[i].getValue();
            if (visited.get(i)) {
                return ESat.FALSE;
            }
            visited.set(i);
        }
        if (i == first) {
			for(int j=0;j<n;j++){
				if(vars[j].getValue()!=j && !visited.get(j)){
					return ESat.FALSE;
				}
			}
            return ESat.TRUE;
        } else {
            return ESat.FALSE;
        }
    }
}
