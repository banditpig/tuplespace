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

package js.co.uk.tuplespace.store;

import java.io.Serializable;

/**
 *  Class to represent the id of a transaction. It wraps a long.
 * A wrapper class is used rather than a long because it makes the Space API easier to use as having a long conflicts
 * with a long used for timeouts in, for example,
 * <p>
 * <code>
 *     public Tuple get(final Tuple template, final long timeOut,final TransactionID txnId)
 * </code>

 */
public class TransactionID implements Serializable {
    /**
     * the long value wrapped by this class
     */
    private final long idValue;

    /**
     * 
     * @param idValue  the long value  of the TransactionID
     */
    public TransactionID(final long idValue) {
        this.idValue = idValue;
    }

    /**
     *  Gets the long value wrapped by this class.
     * @return
     */
    public long getIdValue() {
        return idValue;
    }


    /**
     *   Simple strings up the wrapped long
     * @return    string representation of the class
     */
    public String toString() {
        return "TransactionID{" +
                "idValue=" + idValue +
                '}';
    }

    /**
     *
     * @param o the other in the test for equals
     * @return   true if equal
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransactionID that = (TransactionID) o;

        return idValue == that.idValue;

    }

    /**
     * 
     * @return
     */
    public int hashCode() {
        return (int) (idValue ^ (idValue >>> 32));
    }
}
