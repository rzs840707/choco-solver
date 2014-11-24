/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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

package org.chocosolver.solver.variables.delta;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.search.loop.ISearchLoop;
import org.chocosolver.solver.search.loop.TimeStampedObject;

/**
 * A class to store the removed value of an integer variable.
 * <p/>
 * It defines methods to <code>add</code> a value, <code>clear</code> the structure
 * and execute a <code>Procedure</code> for each value stored.
 */
public final class EnumDelta extends TimeStampedObject implements IEnumDelta {
    private static final int SIZE = 32;

    int[] rem;
    ICause[] causes;
    int last;

    public EnumDelta(ISearchLoop loop) {
		super(loop);
        rem = new int[SIZE];
        causes = new ICause[SIZE];
    }

    private void ensureCapacity() {
        if (last >= rem.length) {
            int[] tmp = new int[last * 3 / 2 + 1];
            ICause[] tmpc = new ICause[last * 3 / 2 + 1];
            System.arraycopy(rem, 0, tmp, 0, last);
            System.arraycopy(causes, 0, tmpc, 0, last);
            rem = tmp;
            causes = tmpc;
        }
    }

	@Override
    public void lazyClear() {
        if (needReset()) {
			last = 0;
			resetStamp();
        }
    }

    /**
     * Adds a new value to the delta
     *
     * @param value value to add
     * @param cause of the removal
     */
    @Override
    public void add(int value, ICause cause) {
		lazyClear();
        ensureCapacity();
        causes[last] = cause;
        rem[last++] = value;
    }

    @Override
    public int get(int idx) {
        return rem[idx];
    }

    @Override
    public ICause getCause(int idx) {
        return causes[idx];
    }

    @Override
    public int size() {
        return last;
    }
}