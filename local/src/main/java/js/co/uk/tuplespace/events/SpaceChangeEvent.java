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

package js.co.uk.tuplespace.events;

import js.co.uk.tuplespace.store.TransactionID;
import js.co.uk.tuplespace.tuple.Tuple;

import java.util.Collection;

/**
 Plan is to have Space fire out events of this type. They can be 'fired' as tuples being added into
 a space that is dedicated to just handling space changes...
 */
public class SpaceChangeEvent implements SpaceChange{

    private final TransactionID txnID;


    private  String spaceName;
    private  Tuple tuple;
    private  Collection<Tuple> tuples;

    private  EventHint eventHint;



    /**
     * Creates SpaceChangeEvent with the supplied values
     * @param spaceName
     * @param txnID
     * @param tuple
     * @param eventHint
     */
    public SpaceChangeEvent(final String spaceName, TransactionID txnID, final Tuple tuple, final EventHint eventHint) {
        this.spaceName = spaceName;
        this.tuple = tuple;
        this.eventHint = eventHint;
        this.txnID = txnID;
    }

    /**
     * 
     * @return
     */
    @Override
    public Collection<Tuple> getTuples() {
        return tuples;
    }
    /**
     * 
     * @return
     */
    public TransactionID getTransactionID(){
        return txnID;
    }
    /**
     * 
     * @return
     */
    @Override
    public String getSpaceName() {
        return spaceName;
    }

    /**
     * 
     * @return
     */
    @Override
    public Tuple getTuple() {
        return tuple;
    }

    /**
     * 
     * @return
     */
    @Override
    public EventHint getEventHint() {
        return eventHint;
    }

    /**
     * 
     * @return
     */
    @Override
    public String toString() {
        return "SpaceChangeEvent{" +
                "txnID=" + txnID +
                ", spaceName='" + spaceName + '\'' +
                ", tuple=" + tuple +
                ", eventHint=" + eventHint +
                '}';
    }


}
