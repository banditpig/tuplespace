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
 * 
 * @author mike
 */
public interface SpaceChange {


    /**
     * 
     * @return
     */
    String getSpaceName();

    /**
     * 
     * @return
     */
    Tuple getTuple();

    /**
     * 
     * @return
     */
    EventHint getEventHint();
    
    /**
     * 
     * @return
     */
    TransactionID getTransactionID();

    /**
     * 
     * @return
     */
    Collection<Tuple> getTuples();
}
