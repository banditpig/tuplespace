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

import java.lang.reflect.Field;
import java.util.logging.Logger;
import js.co.uk.tuplespace.tuple.SimpleTuple;
import js.co.uk.tuplespace.tuple.Tuple;

/**
 * This does matching based on a field by field  comparison of the tuple and the template.
 */
@SuppressWarnings("serial")
public class FieldBasedMatcher implements Matcher<Tuple> {


    private static final Logger logger = Logger.getLogger(FieldBasedMatcher.class.getName());


    // use this to match the semantically useful SimpleTuple class
    /**
     * The naive matcher.                                                    `
     */
    private final Matcher<Tuple> naiveMatcher = new NaiveMatcher();

    /**
     * Matching is performed in the following way
     * <p/>
     * If tuple and template are both of type {@link js.co.uk.tuplespace.tuple.SimpleTuple} then a {@link NaiveMatcher} is used to match them and <br>
     * the result of the NaiveMatcher is returned.<p>
     * <p/>
     * <p/>
     * Otherwise the following is done: <p>
     * If the results of calling getClass() on tuple and template are different then false is returned unless the template is
     * a superclass of the tuple as determined by tupleClass.getSuperclass(). <br>
     * (They can't match if they are of different types) <p>
     * <p/>
     * If they are of the same time (as determined by getClass() ) then a field by field comparison is done between the two classes. <br>
     * This field based comparison uses the equals() method. A  null value  in a  template field will always match. 
     *
     * <p/>
     * If all the fields, <b> public and private </b> are equal then the tuple is deemed to match the template and a value of true is returned.<br>
     * Otherwise the method returns false.
     *
     * @param tuple    the tuple to match
     * @param template the template against which to match
     * @return true for a match and false otherwise
     */
    @Override
    public boolean match(final Tuple tuple, final Tuple template) {

        // do this to allow the semantically useful SimpleTuple to be used.
        // ie it's useful in the sense of we can do new SimpleTuple(1,1,2) or new SimpleTuple("fred",1 "xx",1,2,2) etc
        if ((tuple instanceof SimpleTuple) && (template instanceof SimpleTuple)) {

            return naiveMatcher.match(tuple, template);

        }

        final Class tupleClass = tuple.getClass();
        final Class templateClass = template.getClass();

        // same type? also template can be a superttype of the tuple
        if (tupleClass.getSuperclass() == templateClass) return true;

        if (tupleClass != templateClass) return false;

        //if get here then they are both of the same type so a field by field comparison is required.
        // look at fields in the tuple  is it possible that the fields could be picked up in different order?
        final Field[] tupleFields = tuple.getClass().getDeclaredFields();
        final Field[] templateFields = template.getClass().getDeclaredFields();

        Field tupleField;
        Field templateField;

        for (int index = 0; index < tupleFields.length; index++) {
            tupleField = tupleFields[index];
            templateField = templateFields[index];
            // allow access to private
            tupleField.setAccessible(true);
            templateField.setAccessible(true);
            try {

                final Object tuplefFieldValue = tupleField.get(tuple);
                final Object templateFieldValue = templateField.get(template);

                // need to compare tupleFieldValue with the corresponding field in the template
                // final Object templateFieldValue = getTemplateFieldValue(tupleField.getName(), template);

                // nulls or * in the template are used as wildcards
                if ((templateFieldValue == null) || (templateFieldValue.equals("*"))) {
                    continue;
                } else {
                    if (!tuplefFieldValue.equals(templateFieldValue)) {
                        return false;
                    }
                }

            }
            catch (IllegalArgumentException e) {
                logger.warning(""+e);

            }
            catch (IllegalAccessException e) {
                  logger.warning(""+e);


            }
            // reset the access
            tupleField.setAccessible(false);
            templateField.setAccessible(false);

        }

        return true;
    }


}
