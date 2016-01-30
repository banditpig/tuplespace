/*******************************************************************************
 * Copyright (c) 2011. Mike Houghton.
 *
 *
 * This file is part of 'TupleSpace'.
 *
 * TupleSpace is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * TupleSpace is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with 'TupleSpace'. If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/

package dist;


import java.util.concurrent.CountDownLatch;//assertTrue;
import js.co.uk.tuplespace.dist.hess.local.HessianLocalSpaceCreator;
import js.co.uk.tuplespace.space.Space;
import js.co.uk.tuplespace.store.TransactionException;
import js.co.uk.tuplespace.store.TransactionID;
import js.co.uk.tuplespace.tuple.MatchAllTuplesTemplate;
import js.co.uk.tuplespace.tuple.SimpleTuple;
import js.co.uk.tuplespace.tuple.Tuple;
import org.junit.*;
import static org.junit.Assert.assertTrue;

/**
 *
 */

/**
 *
 */
public class SimpleSpaceTestUsingTuple {
    private static HessianLocalSpaceCreator localSpaceCreator;

    /**
     * The space.
     */
    private Space space;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // PropertyConfigurator.configure(SimpleSpaceTestUsingTuple.class.getResource("/properties/logging.properties"));
        localSpaceCreator  = new HessianLocalSpaceCreator("http://127.0.0.1:8080");
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        // sure to start with a clean space

        //  space = new TupleSpace();
        space = localSpaceCreator.createSpace( "SimpleSpaceTestUsingTuple");

    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }


    /**
     * Test match all template.
     */
    @Test
    public void testMatchAllTemplate() throws TransactionException {

        purgeSpace();

        final Tuple t1 = new SimpleTuple(1, 2, 3);
        final Tuple t2 = new SimpleTuple(1, 2, 4);
        final Tuple t3 = new SimpleTuple(1, 2, 5);

        space.put(t1);
        space.put(t2, null);
        space.put(t3, null);
        assertTrue(space.size() == 3);

        final MatchAllTuplesTemplate matchAll = new MatchAllTuplesTemplate();

        assertTrue(space.get(matchAll, null) != null);
        assertTrue(space.size() == 2);

        assertTrue(space.get(matchAll, null) != null);
        assertTrue(space.size() == 1);

        assertTrue(space.get(matchAll, null) != null);
        assertTrue(space.size() == 0);

        assertTrue(space.get(matchAll, 100, null) == null);

    }


    /**
     * To test putting tuples in with a finite lifetime, when they expire they
     * are removed.
     */
    @Test
    public void testPutTupleIFInt() throws TransactionException {

        purgeSpace();
        final SimpleTuple simpleTuple = new SimpleTuple();
        // put it with a lease of 5 seconds
        space.put(simpleTuple, 5 * 1000, null);
        // now have 1 simpleTuple
        assertTrue(space.size() == 1);
        // wait >5 seconds
        try {
            Thread.sleep(6 * 1000);
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // space should now be empty
        assertTrue(space.size() == 0);

        // try a few more
        for (int i = 0; i < 10; i++) {
            final SimpleTuple t = new SimpleTuple(i);
            space.put(t, 5 * 1000, null);
        }
        assertTrue(space.size() == 10);

        // wait >5 seconds
        try {
            Thread.sleep(6 * 1000);
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertTrue(space.size() == 0);

    }

    /**
     * To test putting tuples in with a lifetime of 'forever' i.e. they never
     * expire.
     */
    @Test
    public void testPutTupleIF() throws TransactionException {

        purgeSpace();
        final Tuple tuple1 = new SimpleTuple(1);
        space.put(tuple1, null);
        assertTrue(space.size() == 1);

        // CAN  put the same one twice
        space.put(tuple1, null);
        assertTrue(space.size() == 2);

        space.put(new SimpleTuple(2), null);
        space.put(new SimpleTuple(3), null);
        space.put(new SimpleTuple(4), null);
        space.put(new SimpleTuple(5), null);

        assertTrue(space.size() == 6);

        // try adding smae ones again - just to be sure!!
        space.put(new SimpleTuple(2), null);
        space.put(new SimpleTuple(3), null);
        space.put(new SimpleTuple(4), null);
        space.put(new SimpleTuple(5), null);
        assertTrue(space.size() == 10);

        purgeSpace();

    }

    /**
     * To test getting a tuple using a template and no timeout
     */
    @Test
    public void testGetTupleIF() throws TransactionException {
        purgeSpace();
        final Tuple t1 = new SimpleTuple(1, 2, 3);
        final Tuple t2 = new SimpleTuple(1, 2, 4);
        final Tuple t3 = new SimpleTuple(1, 2, 5);

        space.put(t1, null);
        space.put(t2, null);
        space.put(t3, null);
        assertTrue(space.size() == 3);

        final Tuple template = new SimpleTuple(1, 2, "*");
        Tuple result = space.get(template, null);
        assertTrue(result != null);
        assertTrue(space.size() == 2);

        result = space.get(template, null);
        assertTrue(result != null);
        assertTrue(space.size() == 1);

        result = space.get(template, null);
        assertTrue(result != null);
        assertTrue(space.size() == 0);

    }

    /**
     * Test for getting with a template and having a timeout
     */
    @Test
    public void testGetTupleIFLong() throws TransactionException {
        purgeSpace();

        final Tuple t1 = new SimpleTuple(1, 2, "fred");
        final Tuple t2 = new SimpleTuple(2, 3, "joe");
        final Tuple t3 = new SimpleTuple(3, 4, "jack");
        final Tuple t4 = new SimpleTuple(4, 5, "bill");
        final Tuple t5 = new SimpleTuple(5, 6, "alan");

        space.put(t1, null);
        space.put(t2, null);
        space.put(t3, null);
        space.put(t4, null);
        space.put(t5, null);
        assertTrue(space.size() == 5);

        // fred
        Tuple template = new SimpleTuple(1, "*", "*");
        Tuple result = space.get(template, 1, null);
        assertTrue(result.equals(t1));
        assertTrue(space.size() == 4);

        // joe
        template = new SimpleTuple(2, "*", "*");
        result = space.get(template, 1, null);
        assertTrue(result.equals(t2));
        assertTrue(space.size() == 3);

        // this should not match as there are too many parameters
        template = new SimpleTuple("*", "*", "*", "*");
        result = space.get(template, 1, null);
        assertTrue(result == null);
        assertTrue(space.size() == 3);

        // alan
        template = new SimpleTuple("*", "*", "alan");
        result = space.get(template, 1, null);
        assertTrue(result.equals(t5));
        assertTrue(space.size() == 2);

        // bill - and fail
        template = new SimpleTuple(5, "*", "bill");
        result = space.get(template, 1, null);
        assertTrue(result == null);
        assertTrue(space.size() == 2);

        // bill
        template = new SimpleTuple(4, "*", "bill");
        result = space.get(template, 1, null);
        assertTrue(result.equals(t4));
        assertTrue(space.size() == 1);

        // jack
        template = new SimpleTuple("*", "*", "*");
        result = space.get(template, 1, null);
        assertTrue(result.equals(t3));
        assertTrue(space.size() == 0);
    }


    @Test
    public void testSize() throws TransactionException {
        purgeSpace();

        final Tuple t1 = new SimpleTuple(1, 2, 3);
        space.put(t1, null);
        assertTrue(space.size() == 1);

        final Tuple t2 = new SimpleTuple(1, 2, 4);
        space.put(t2, null);
        assertTrue(space.size() == 2);

        final Tuple t3 = new SimpleTuple(1, 2, 5);
        space.put(t3, null);
        assertTrue(space.size() == 3);

    }

    @Test
    public void testConcurrency() {
        //be careful using space.size() ITS NOT THREADSAFE can get concurrentModification exception
        purgeSpace();
        final long start = System.currentTimeMillis();
        final Tuple MATCH_ALL = new SimpleTuple(1, 2, "*", "*");
        int MAX = 20;
        for (int x = 0; x < 5; x++) {
            final CountDownLatch writeLatch = new CountDownLatch(MAX);
            for (int i = 0; i < MAX; i++) {
                final int id = i;
                Thread writer = new Thread() {

                    @Override
                    public void run() {
                        try {
                            Thread.sleep(10);
                            // Thread.sleep(10 * (long) (10 * Math.random()));
                        }
                        catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        try {
                            space.put(new SimpleTuple(1, 2, 3, id), null);
                        } catch (TransactionException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                        writeLatch.countDown();
                        System.out.println("" + id);

                    }
                };

                writer.start();
            }

            try {
                writeLatch.await();

                System.out.println("done writes " + space.size());
                assertTrue(space.size() == MAX);

            }
            catch (InterruptedException e) {

                e.printStackTrace();
            }
            final CountDownLatch readLatch = new CountDownLatch(10);
            for (int i = 0; i < 10; i++) {

                Thread reader = new Thread() {

                    int count = 0;

                    @Override
                    public void run() {
                        while (count != 2) {
                            try {
                                System.out.println("-->"+ space.get(MATCH_ALL, null));
                            } catch (TransactionException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                            count++;
                        }

                        readLatch.countDown();
                    }
                };
                reader.start();
            }

            try {
                System.out.println("readLatch.await()...");
                readLatch.await();
                //System.out.println("Size = " + space.size());
                assertTrue(space.size() == 0);
            }
            catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        assertTrue(space.size() == 0);
        final long length = (System.currentTimeMillis() - start);
        System.out.println("All done " + length);

    }

    @Test
    public void testMemoryWithPutAndGet() {
        /*
       This is really a test to run an watch from a profiler to check for memory use and release.
       Best to run with max set to v.high number
        */
        final SimpleTuple template = new SimpleTuple("*");
        Thread reader = new Thread() {
            public void run() {
                while (true) {


                    try {
                        System.out.println("==>" + space.get(template, null));
                        Thread.sleep(5);
                    } catch (TransactionException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }

                }
            }
        };

        reader.setDaemon(true);
        reader.start();
        int max = 50;

        for (int i = 0; i < max; i++) {

            try {
                TransactionID txn = space.beginTxn(500000l);

                System.out.println(i);
                space.put(new SimpleTuple(i), txn);
                if (txn != null) {
                    space.commitTxn(txn);
                }

                Thread.sleep(5);


            } catch (TransactionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }


        }

    }

    /**
     *
     */
    private void purgeSpace() {

        space.purgeAllEntries();
        assertTrue(space.size() == 0);

    }

    private void sleep(int seconds) {

        try {
            Thread.sleep(seconds * 1000);
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}
