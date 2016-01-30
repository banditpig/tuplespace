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

/**
 *
 */
package js.co.uk.tuplespace.matcher;

import java.io.Serializable;

/**
 * This defines the nature of matching between tuples.
 */
public interface Matcher<V> extends Serializable {

    /**
     * The match method.
     *
     * @param source   the tuple to be matched against the supplied template
     * @param template the template use in the matching
     * @return true, if successful
     */
    public boolean match(final V source, final V template);

}
