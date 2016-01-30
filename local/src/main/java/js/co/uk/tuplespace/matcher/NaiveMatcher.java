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

package js.co.uk.tuplespace.matcher;

import java.util.Iterator;
import js.co.uk.tuplespace.tuple.MatchAllTuplesTemplate;
import js.co.uk.tuplespace.tuple.SimpleTuple;
import js.co.uk.tuplespace.tuple.Tuple;

/**
 * This matcher is used when matching two tuples of type {@link js.co.uk.tuplespace.tuple.SimpleTuple} <br>
 * The Tuple class uses a Collection to represent its data and this matcher simple compares corresponding entries
 * in the data collection. <br>
 * <p/>
 * An entry of '*' in the template will automatically match an entry in the tuple. <p/>
 * For example if have<br> <br>
 * <p/>
 * <p/>
 * Tuple t = new Tuple(1,2,3);
 * <br>
 * and<br>
 * <p/>
 * Tuple template = new Tuple(1,2,'*'); <br>
 * then match(t, template) would return true.<br>
 * but match(t, new Tuple(1,2,4) would return false.</p> <br>
 * <p/>
 * <b>
 * Note<br>
 * Currently there is no escape mechanism for using '*' as an entry in a Tuple. <br>
 * <p/>
 * </b>
 */
public class NaiveMatcher implements Matcher<Tuple> {

    /**
     * A simple match on the individual entries of a {@link js.co.uk.tuplespace.tuple.SimpleTuple}  collection.<br>
     * <p/>
     * For a Tuple and a Tuple template to match the following must all be true:<p>
     * 1. The Collections returned by {@link js.co.uk.tuplespace.tuple.SimpleTuple#getData()} must have the same size.<br>
     * 2. Corresponding individual entries in the data collections must be equal (based on the entries notion of equality)<br>
     * or the entry in the template collection must be the string literal '*'.
     *
     * @param aTuple    the tuple
     * @param aTemplate the template for matching
     * @return true, if successful
     */
    @Override
    public boolean match(final Tuple aTuple, final Tuple aTemplate) {

        if (aTemplate instanceof MatchAllTuplesTemplate) return true;

        final SimpleTuple simpleTuple = (SimpleTuple) aTuple;
        final SimpleTuple template = (SimpleTuple) aTemplate;

        // can't match on different lengths
        if (simpleTuple.getData().size() != template.getData().size()) {
            return false;
        }

        // template and simpleTuple are of the same length if we get here.
        final Iterator<Object> templateDataItr = template.getData().iterator();
        final Iterator<Object> tupleDataItr = simpleTuple.getData().iterator();

        for (int i = 0; i < template.getData().size(); i++) {
            final Object templateObj = templateDataItr.next();
            final Object tupleObj = tupleDataItr.next();

            // wildcard
            // TODO Need to Escape a * to allow matching against a '*'
            if (templateObj.equals("*")) {

                //do nothing
            } else if (!templateObj.equals(tupleObj)) {
                return false;
            }
        }

        return true;
    }

}
