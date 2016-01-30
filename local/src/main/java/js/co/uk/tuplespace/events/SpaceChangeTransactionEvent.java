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

import js.co.uk.tuplespace.store.TimeoutEntry;
import js.co.uk.tuplespace.store.TransactionID;
import js.co.uk.tuplespace.tuple.Tuple;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 
 * @author mike
 */
public class SpaceChangeTransactionEvent implements SpaceChange {

    private final TransactionID txnID;


    private final String spaceName;

    private final Collection<Tuple> tuples;

    private final  EventHint eventHint;


    /**
     * 
     * @param spaceName
     * @param txnID
     * @param items
     * @param eventHint
     */
    public SpaceChangeTransactionEvent(final String spaceName, final TransactionID txnID, final Collection<TimeoutEntry<Tuple>> items, final EventHint eventHint) {

        this.spaceName = spaceName;
        this.txnID = txnID;
        this.eventHint = eventHint;
        this.tuples = extractTuplesFromTimeouts(items);
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
        throw new UnsupportedOperationException();
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
    public TransactionID getTransactionID() {
        return txnID;
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
    @Override
    public String toString() {
        return "SpaceChangeTransactionEvent{" + "txnID=" + txnID + ", spaceName=" + spaceName + ", tuples=" + tuples + ", eventHint=" + eventHint + '}';
    }
    
    

    private Collection<Tuple> extractTuplesFromTimeouts(Collection<TimeoutEntry<Tuple>> items) {

        final Collection<Tuple> tuples = new ArrayList<Tuple>();
        if (items == null) return tuples;

        for (TimeoutEntry<Tuple> timeOut : items) {
            tuples.add(timeOut.getItem());
        }

        return tuples;
    }
}
