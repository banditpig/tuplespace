/*
 * ******************************************************************************
 *  * Copyright (c) 2012. Mike Houghton.
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import js.co.uk.tuplespace.tuple.Tuple;

/**
 * This matcher uses a regular expression against the string value of selected
 * fields in the source tuple. It is constructed with a map of field names
 * against the regular expression to match the field. The fields do not have to
 * be string types but it would seem semantically more meaningful for them to be
 * so.
 *
 *
 *
 */
public class RegexMatcher implements Matcher<Tuple> {

    /**
     * Use this as the 'template' when querying a TupleSpace that has a
     * RegexMatcher. Do this because if a regex matcher is used the template is
     * not used when matching.
     */
    public static final EmptyTuple NULL_TEMPLATE = new EmptyTuple();
    
    private final List<RegexFieldMatcher<Tuple>> regExFieldMatchers = new ArrayList<RegexFieldMatcher<Tuple>>();

    
    /**
     * 
     */
    public RegexMatcher() {
    }

    
    /**
     *
     * @param regexMap
     */
    public RegexMatcher(final Map<String, String> regexMap) {

        setRegexMap(regexMap);;
    }

    /**
     *
     * @param fieldName
     * @param regEx
     */
    public void addRegexEntry(final String fieldName, final String regEx) {

        regExFieldMatchers.add(new RegexFieldMatcher<Tuple>(fieldName, regEx));
    }

    /**
     *
     * @param regexMap
     */
    public   void setRegexMap(Map<String, String> regexMap) {

        regExFieldMatchers.clear();
        for (String regExKey : regexMap.keySet()) {
            regExFieldMatchers.add(new RegexFieldMatcher<Tuple>(regexMap.get(regExKey), regExKey));
        }

    }

    /**
     *
     */
    public void clearMatchers() {
        regExFieldMatchers.clear();
    }

    @Override
    public boolean match(final Tuple source, final Tuple unused) {


        for (RegexFieldMatcher<Tuple> fMatcher : regExFieldMatchers) {

            if (!fMatcher.isMatch(source)) {
                return false;
            }
        }

        //all patterns and fields have matched
        return true;
    }

    private static class EmptyTuple implements Tuple {
    }

    private class RegexFieldMatcher<T extends Tuple> {

        private final String fieldName;
        private final String regEx;
        private final Pattern pattern;

       private RegexFieldMatcher(String fieldName, String regEx) {
            this.fieldName = fieldName;
            this.regEx = regEx;
            pattern = Pattern.compile(regEx);
            
        }

        boolean isMatch(final T source) {

            String fieldValue = null;;
            try {

                fieldValue = getFieldValue(fieldName, source);
                final java.util.regex.Matcher matcher = pattern.matcher(fieldValue);
                final boolean aMatch = matcher.lookingAt();
                Logger.getLogger(RegexMatcher.class.getName()).log(Level.INFO, "Match ==> " + aMatch + " " + regEx + " < > " + fieldValue);

                return aMatch;

            } catch (IllegalArgumentException ex) {
                Logger.getLogger(RegexMatcher.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(RegexMatcher.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchFieldException ex) {
                Logger.getLogger(RegexMatcher.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;


        }

        /**
         * Gets the string value of the field called fieldName in the Tuple
         * class source.
         *
         * @param fieldName
         * @param source
         * @return
         */
        private String getFieldValue(final String fieldName, final Tuple source) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException {


            final Class sourceClass = source.getClass();
            final Field field = sourceClass.getDeclaredField(fieldName);
            field.setAccessible(true);

            final String value = field.get(source).toString();
            field.setAccessible(false);
            Logger.getLogger(RegexMatcher.class.getName()).log(Level.INFO, "field value of " + fieldName + "  is " + value);
            return value;



        }
    }
}
