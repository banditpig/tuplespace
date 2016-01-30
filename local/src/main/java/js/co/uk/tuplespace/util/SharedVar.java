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

package js.co.uk.tuplespace.util;

import js.co.uk.tuplespace.tuple.Tuple;

/**
 * Implementation of a Shared Variable.
 * It's created with a name and a start value. The idiomatic usage  is to 'get' the 
 * SharedVar, modify its value, and then 'put' it back in the  space. Doing this allows clients
 * to see a consistent and threadsafe view of the  SharedVar. For reliability the  'get' should be done under a 
 * transaction if the space is a remote one. (i.e. not in the  same VM as the client).
 * 
 */
@SuppressWarnings("serial")
public class SharedVar implements Tuple {

    /**
     * The value.
     */
    private Integer value = null;

    /**
     * The name.
     */
    private String name = null;

    /**
     * Instantiates a new shared var.
     *
     * @param name     the name
     * @param startVal the start val
     */
    public SharedVar(final String name, final Integer startVal) {
        this.name = name;
        value = startVal;
    }

    /**
     * Alternate constructor that takes a primitive int.
     * @param name
     * @param startVal
     */
     public SharedVar(final String name, final int startVal) {
        this.name = name;
        value = startVal;
    }

    /**
     * Instantiates a new shared var.
     *
     * @param name the name
     */
    public SharedVar(final String name) {
        this(name, null);
    }


    // -------------------------- OTHER METHODS --------------------------

    /**
     * Inc.
     *
     * @return the integer
     */
    public Integer inc() {
        value++;
        return value;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public Integer getValue() {
        return value;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /*
      * (non-Javadoc)
      *
      * @see java.lang.Object#toString()
      */
    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "SharedVar [name=" + name + ", value=" + value + "]";
    }

    /**
     * Equals.
     *
     * @param o the o
     * @return true, if successful
     */
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SharedVar sharedVar = (SharedVar) o;

        if (name != null ? !name.equals(sharedVar.name) : sharedVar.name != null) return false;
        return !(value != null ? !value.equals(sharedVar.value) : sharedVar.value != null);

    }

    /**
     * Hash code.
     *
     * @return the int
     */
    public int hashCode() {
        int result;
        result = (value != null ? value.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
