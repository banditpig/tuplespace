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

import js.co.uk.tuplespace.task.ResultTuple;
import js.co.uk.tuplespace.task.TaskTuple;
import js.co.uk.tuplespace.space.Space;


public class SquareTask extends TaskTuple {

    private Long n = null;
    public SquareTask(){}

    public SquareTask(final Long n){
        this.n = n;
    }
    public ResultTuple execute(Space space) {
        return new SquareTuple( n*n);
    }
}
