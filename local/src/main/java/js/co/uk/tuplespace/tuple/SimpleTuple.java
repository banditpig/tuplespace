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

package js.co.uk.tuplespace.tuple;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 *  This is a semantically useful notion of a TupleIF.
 * It allows syntax such as
 * <p/>
 * new Tuple(1,2,3) or new Tuple("x","y","z",1,2,3);
 */
@SuppressWarnings("serial")
public class SimpleTuple implements Tuple, Serializable {

    /**
     * The data.
     */
    @SuppressWarnings("unchecked")
    //this is not a homogeneous collection
    private final Collection<Object> data = new ArrayList<Object>();

    /**
     * Instantiates a new tuple. This constructor uses a variable arguments signature to allow a fluent syntax when creating
     * a tuple. Using this constructs such as<p>
     * <p/>
     * <p/>
     * <i>TupleIF point = new Tuple("x", "y", 22, 17);</i><p>
     * <p/>
     * or<p>
     * <p/>
     * <i>double x = 3;</i> <p>
     * <i>TupleIF f_of_x = new Tuple(x,  Math.sqrt(x));</i><p>
     * <p/>
     * are easily written.
     *
     * @param tupleItems the entries that make up a tuple
     */
    public SimpleTuple(final Object... tupleItems) {

        data.addAll(Arrays.asList(tupleItems));

    }

    /**
     * Basic empty constructor, typically used in conjuntion with the setData  method
     */
    public SimpleTuple(){
        
    }

    /**
     *  Clears any existing entries for this tuple and sets the entries to be the supplied data items
     * @param tupleItems
     */
    public void setData(final  Object... tupleItems) {
        data.clear();
        data.addAll(Arrays.asList(tupleItems));
    }
    
    /**
     * This gets the data items that constitute the tuple. The ordering of entries in the collection<br>
     * will be the order in which the entries are decalred in the constructor when instantiating the Tuple.
     *
     * @return the object collection that is this tuple's data
     */
    public final Collection<Object> getData() {
        return data;
    }

    // Standard notions of equality and hashCode
    /**
     * The notion of equality between two Tuple objects is that their respective data collections are equal.
     *
     * @param obj the other object being considered for equality to this object
     * @return <code>true</code> if this object is the same as the obj
     *         argument; <code>false</code> otherwise.
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        SimpleTuple simpleTuple = (SimpleTuple) obj;

        return data.equals(simpleTuple.data);

    }

    /**
     * To string.
     *
     * @return the string
     */
    public String toString() {
        return "SimpleTuple{" +
                "data=" + data +
                '}';
    }

    /**
     * For a Tuple object the hash code is based on the hash code of the underlying collection of object's that are the data
     * of the Tuple.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return data.hashCode();
    }
}
