/*
 * ******************************************************************************
 *  * Copyright (c) 2011. Mike Houghton.
 *  *
 *  *
 *  * This file is part of 'TupleSpace'.
 *  *
 *  * 'TupleSpace' is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 *  * License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
 *  * any later version.
 *  *
 *  * 'TupleSpace' is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License along with 'TupleSpace'.
 *  * If not, see http://www.gnu.org/licenses/.
 *  *****************************************************************************
 */

package js.co.uk.tuplespace.examples;

import js.co.uk.tuplespace.space.Space;
import js.co.uk.tuplespace.task.Master;
import js.co.uk.tuplespace.task.PoisonPill;

public class SquareMaster extends Master {
    private Long last = 200000L;

    public SquareMaster(final Space space, final Long last) {
        super(space);
        this.last = last;

    }


    public void generateTasks() {
        for (Long i = 1L; i <= last; i++) {
            SquareTask task = new SquareTask(i);


            this.putTaskTuple(task);

        }
    }

    public void collectResults() {
        //50000->41667916675000

        //150000 -> 1125011250025000

        //200 000   2666686666700000
        Long sums = last * (last + 1) * (2 * last + 1) / 6;


        System.out.println("by hand " + sums);

        long sum = 0;
        for (int i = 1; i <= last; i++) {

            final SquareTuple res = (SquareTuple) getResultTuple();
            sum += res.getValue();
            System.out.println(i + " Sum " + sum);

        }
        System.out.println("done  " + sum + " size " + space.size() + " pending " + space.pendingMatchesCount());

        space.put(new PoisonPill());

    }

    public static void main(String[] args) {
        System.out.println(41667916675000L - 41667896738775L);
        long last = 100000;
        Long sums = last * (last + 1) * (2 * last + 1) / 6;
        System.out.println(sums);


    }
}
