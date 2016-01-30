/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package js.co.uk.tuplespace.matcher;

import java.util.HashMap;
import java.util.Map;
import js.co.uk.tuplespace.space.Space;
import js.co.uk.tuplespace.space.TupleSpace;
import js.co.uk.tuplespace.tuple.Tuple;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mike
 */
public class RegexMatcherTest {

    private Person fred = new Person("Fred", "Smith", "8 Brockhampton Lane Havant", 32);
    private Person kate = new Person("Fate", "Jones", "12 Brockhampton Lane Havant", 34);
    private Person jack = new Person("Fred", "Houghton", "8 Brockhampton Lane Havant", 36);
    private Person joe = new Person("Joe", "Smith", "44 Brockhampton Lane Havant", 38);
    private final Map<String, String> regexMap = new HashMap<String, String>();
    private final RegexMatcher matcher = new RegexMatcher(new HashMap<String, String>());

    public RegexMatcherTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        regexMap.clear();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of match method, of class RegexMatcher.
     */
    @Test
    public void match_string_field() {

        // boolean aMatch  = Pattern.matches("Fred", "F");

        matcher.addRegexEntry("cName", "^F");
        assertTrue(matcher.match(fred, null));

        matcher.addRegexEntry("age", "\\d2");
        assertTrue(matcher.match(fred, null));

    }

    @Test
    public void match_string_field_in_space() {
        matcher.clearMatchers();
        matcher.addRegexEntry("cName", "^Fa.*");
        Space space = new TupleSpace("matchers");
        space.setMatcher(matcher);
        space.put(fred);
        space.put(kate);

        System.out.println(space.get(RegexMatcher.NULL_TEMPLATE, 1000));
    }

    //--------------------------------------------------------------------------
    private class Person implements Tuple {

        private String cName;
        private String sName;
        private String address;
        private Integer age;

        public Person(String cName, String sName, String address, Integer age) {
            this.cName = cName;
            this.sName = sName;
            this.address = address;
            this.age = age;
        }

        @Override
        public String toString() {
            return "Person{" + "cName=" + cName + ", sName=" + sName + ", address=" + address + ", age=" + age + '}';
        }
    }
}
