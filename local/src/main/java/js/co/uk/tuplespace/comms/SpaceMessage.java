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

package js.co.uk.tuplespace.comms;

import java.io.Serializable;


/**
 * 
 * @author mike
 * @param <R>
 * @param <M>
 */
public abstract class SpaceMessage<R extends Serializable, M extends Serializable> implements Message<R, M> {


    private R receiver;
    private M message;

    /**
     * 
     * @param receiver
     * @param message
     */
    protected SpaceMessage(final R receiver, final M message) {
        this.receiver = receiver;
        this.message = message;
    }

    /**
     * 
     * @param receiver
     */
    protected SpaceMessage(final R receiver) {
        this(receiver, null);
    }


    /**
     * 
     * @return
     */
    public R getReceiver() {
        return receiver;
    }

    /**
     * 
     * @param receiver
     */
    public void setReceiver(R receiver) {
        this.receiver = receiver;
    }

    /**
     * 
     * @return
     */
    public M getMessage() {
        return message;
    }

    /**
     * 
     * @param message
     */
    public void setMessage(M message) {
        this.message = message;
    }
}
