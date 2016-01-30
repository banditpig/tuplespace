package js.co.uk.tuplespace.local;

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
import js.co.uk.tuplespace.space.Space;
import js.co.uk.tuplespace.space.TupleSpace;
import js.co.uk.tuplespace.store.TransactionException;
import js.co.uk.tuplespace.store.TransactionID;
import js.co.uk.tuplespace.tuple.MatchAllTuplesTemplate;
import js.co.uk.tuplespace.tuple.SimpleTuple;
import js.co.uk.tuplespace.tuple.Tuple;
import js.co.uk.tuplespace.util.SharedVar;
import org.junit.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import js.co.uk.tuplespace.events.SpaceChange;
import js.co.uk.tuplespace.events.SpaceChangeListener;
import js.co.uk.tuplespace.space.Space;
import js.co.uk.tuplespace.space.TupleSpace;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import org.junit.experimental.categories.Categories;

/**
 * The Class SimpleSpaceTestUsingGeneralTupleIF.
 */
public class SimpleSpaceTestUsingGeneralTupleIF implements SpaceChangeListener {

    /**
     * The space.
     */
    private Space space;

    /**
     * Sets the up before class.
     *
     * @throws Exception the exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * Tear down after class.
     *
     * @throws Exception the exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * Sets the up.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        // sure to start with a clean space

        // space = new TupleSpace();
        space = new TupleSpace("SimpleSpaceTestUsingGeneralTupleIF");
        space.addSpaceChangeListener(this);
    }

    @Test(expected=TransactionException.class)
    public void txnExpireBeforeReadDone() throws TransactionException {
        
        space.purgeAllEntries();

        final TransactionID id = space.beginTxn(500L);
        final Tuple noSuchTuple = new SimpleTuple(1);

        space.get(noSuchTuple, id);

    }

    /**
     * Tear down.
     *
     * @throws Exception the exception
     */
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void barrier() {
        space.purgeAllEntries();
        final SharedVar template = new SharedVar("barrier");
        space.put(new SharedVar("barrier", 0));

        int last = 50;

        final CountDownLatch starter = new CountDownLatch(1);
        for (int i = 0; i < last; i++) {
            final Thread t = new Thread() {
                // @Override
                /**
                 * @see java.lang.Thread#run()
                 */
                @Override
                public void run() {

                    try {
                        starter.await();
                        final TransactionID txn = space.beginTxn(2000000L);
                        final SharedVar readVar = (SharedVar) space.get(template, txn);
                        readVar.inc();

                        Thread.sleep((long) (10 * Math.random()));
                        space.put(readVar, txn);
                        space.commitTxn(txn);


                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (TransactionException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            };

            t.start();
        }


        starter.countDown();
        System.out.println("waiting ...");
        final SharedVar var = (SharedVar) space.get(new SharedVar("barrier", last));
        assertTrue(var.getValue() == last);
        assertTrue(space.size() == 0);


    }

    @Test
    public void testTupleRemovalLeavesItOnExpireQueue() throws TransactionException {
        SimpleTuple t = new SimpleTuple(1, 2, 3);
        space.put(t, 1000);
        System.out.println("got " + space.get(new SimpleTuple(1, 2, 3)));
        sleep(2);


    }

    @Test
    public void testManyReadsOnOneTuple() throws TransactionException {
        space.purgeAllEntries();
        final SimpleTuple template = new SimpleTuple(1);
        space.put(new SimpleTuple(1));
        int N = 500;
        final CountDownLatch latch = new CountDownLatch(N);
        final AtomicLong readCnt = new AtomicLong(0);
        for (int i = 0; i < N; i++) {
            final int id = i;
            Thread t = new Thread() {
                public void run() {

                    try {
                        final Tuple res = space.read(template, 2000);
                        assertTrue(res != null);


                    } catch (Exception e) {

                        System.out.println("Ooopsss " + e);
                    } finally {
                        latch.countDown();
                        readCnt.getAndIncrement();
                    }

                }
            };

            t.start();
        }
        sleep(1);

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(1, space.size());
        assertEquals(N, readCnt.intValue());
        System.out.println("done");

    }

    @Test
    public void testSimultaneos() {
        final AtomicLong matchCount = new AtomicLong(0);

        space.purgeAllEntries();
        long start = System.currentTimeMillis();
        int N = 10;
        final CountDownLatch latch = new CountDownLatch(N);
        for (int c = 0; c < N; c++) {

            final int cF = c;

            final Thread t = new Thread() {
                public void run() {

                    int match = 0;
                    for (int i = 0; i < 10; i++) {
                        // System.out.println("i " + i + " c " + cF);
                        int expire = 34 * (i + 1) * 10;

                        Tuple res = null;
                        res = space.get(new SharedVar("x"), 100);
                        if (res != null) {
                            matchCount.getAndIncrement();
                        }
                        if (i % 2 == 0) {

                            space.put(new SharedVar("x", i));
                        }
                        if (i == 3) {
                            // just add some dross that will nver get matched
                            space.put(new SharedVar("fred", 1));
                        }

                        try {
                            Thread.sleep(100);

                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }


                    latch.countDown();

                }
            };

            t.start();
        }

        try {
            System.out.println("latch   wait");
            latch.await();
            System.out.println("latch DOEN  wait");
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // System.out.println("sleeping...");
        // sleep(10);

        //assertTrue("size is "+space.size(),space.size() == 1);// SharedVar("fred", 1)

        //
        // assertTrue(space.tasksSize() == 1);// and the task to kill fred,1
        // assertTrue(space.templatesSize() == 0);

        System.out.println("space.size() " + space.size());
        //System.out.println("template.size() " + space.pendingMatchesCount());
        System.out.println("Matches " + matchCount);
        long end = System.currentTimeMillis();
        double time = (end - start) / 1000;
        System.out.println("Time taken " + time);

    }

    @Test
    public void testMatchAllTemplate() throws TransactionException {

        purgeSpace();

        final Tuple t1 = new SimpleTuple(1, 2, 3);
        final Tuple t2 = new SimpleTuple(1, 2, 4);
        final Tuple t3 = new SimpleTuple(1, 2, 5);

        space.put(t1);
        space.put(t2);
        space.put(t3);
        assertTrue(space.size() == 3);

        final MatchAllTuplesTemplate matchAll = new MatchAllTuplesTemplate();

        assertTrue(space.get(matchAll) != null);
        assertTrue(space.size() == 2);

        assertTrue(space.get(matchAll) != null);
        assertTrue(space.size() == 1);

        assertTrue(space.get(matchAll) != null);
        assertTrue(space.size() == 0);

        assertTrue(space.get(matchAll, 100) == null);

    }

    @Test
    public void test2Gets() throws TransactionException {
        final SharedVar template = new SharedVar("COUNT");
        Thread t1 = new Thread() {
            public void run() {
                SharedVar res1 = null;

                res1 = (SharedVar) space.get(template);

                System.out.println("res1 " + res1);

            }
        };
        Thread t2 = new Thread() {
            public void run() {
                SharedVar res2 = null;

                res2 = (SharedVar) space.get(template, 4000);

                System.out.println("res2 " + res2);
            }
        };
        t1.start();
        t2.start();
        sleep(2);
        SharedVar var = new SharedVar("COUNT", 1);

        space.put(var);

    }

    @Test
    public void testPurgeSpace() throws TransactionException {
        int N = 1000;
        space.purgeAllEntries();
        for (int i = 0; i < N; i++) {

            space.put(new SharedVar("x", i));

        }
        assertTrue(space.size() == N);
        space.purgeAllEntries();
        assertTrue(space.size() == 0);
    }

    @Test
    public void testSharedVar() throws TransactionException {
        final SharedVar template = new SharedVar("Loop Counter");
        space.purgeAllEntries();
        int last = 50;
        final CountDownLatch latch = new CountDownLatch(last);
        final CountDownLatch starter = new CountDownLatch(1);
        for (int i = 0; i < last; i++) {
            final Thread t = new Thread() {
                // @Override
                /**
                 * @see java.lang.Thread#run()
                 */
                @Override
                public void run() {

                    try {
                        starter.await();
                        final SharedVar readVar = (SharedVar) space.get(template);
                        System.out.println(readVar.inc());

                        Thread.sleep((long) (10 * Math.random()));

                        space.put(readVar);

                        latch.countDown();


                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };

            t.start();
        }
        space.put(new SharedVar("Loop Counter", 0));
        try {
            starter.countDown();
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        SharedVar sharedVar = (SharedVar) space.get(template);
        System.out.println(sharedVar);
        assertTrue(sharedVar.getValue() == last);


    }

    public void megaTest() throws TransactionException {
        for (int i = 0; i < 1000; i++) {
            System.out.println(i + "  ++++++++++++++++++++++++++++++++++++");
            testTemplatesRemovedAfterTimeout();
        }
    }

    @Test
    public void tran() {
        purgeSpace();
        final Tuple t1 = new SimpleTuple(1, 2, 3);
        try {
            TransactionID id = space.beginTxn(50000L);
            space.put(t1, id);
        } catch (TransactionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Test
    public void testTemplatesRemovedAfterTimeout() throws TransactionException {


        int match = 0;
        for (int i = 0; i < 10; i++) {
            Tuple res = space.get(new SharedVar("x"), 100 * (i + 1));
            if (res != null) {
                match++;
            }
            if (i % 2 == 0) {

                space.put(new SharedVar("x", i));
            }
        }

        //  space.size();
        System.out.println("sleep...");
        sleep(2);
        // space.size();
        // should be empty and 5 matches
        assertTrue(match == 5);
        // assertTrue(space.templatesSize() == 0);

        assertTrue(space.size() == 0);

        // now try concurrently...
        int addedFred = 0;
        for (int xx = 0; xx < 10; xx++) {


            System.out.println(xx + "  ============================================================== ");
            int N = 20;
            final CountDownLatch latch = new CountDownLatch(N);
            for (int c = 0; c < N; c++) {

                final Thread t = new Thread() {
                    public void run() {

                        int match = 0;
                        for (int i = 0; i < 10; i++) {

                            Tuple res = space.get(new SharedVar("x"), 234 * (i + 1));


                            if (i % 2 == 0) {


                                space.put(new SharedVar("x", i));

                            }
                            if (i == 3) {
                                // just add some dross that will nver get matched


                                space.put(new SharedVar("fred", 1));

                            }

                            try {
                                Thread.sleep((long) (Math.random() * 100));

                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                        latch.countDown();

                    }
                };

                t.start();
            }

            try {
                System.out.println("latch   wait");
                latch.await();
                System.out.println("latch DONE  wait");
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // System.out.println("sleeping...");
            // sleep(10);

            // assertTrue("size is "+space.size(),space.size() == 1);// SharedVar("fred", 1)
            //
            // assertTrue(space.tasksSize() == 1);// and the task to kill fred,1
            // assertTrue(space.templatesSize() == 0);
        }
        System.out.println("****************** all done ***************************");
    }

    /**
     * To test putting tuples in with a finite lifetime, when they expire they
     * are removed.
     */
    @Test
    public void testPutTupleIFInt() throws IOException, TransactionException {

        purgeSpace();
        System.out.println("size " + space.size());
        for (int i = 0; i < 100; i++) {
            final SharedVar tuple = new SharedVar("test", i);
            // put it with a lease of 5 seconds
            space.put(tuple, 10 * 1000);
        }

        // now have 100 tuple
        System.out.println("size " + space.size());
        assertTrue(space.size() == 100);
        // wait >10seconds
        try {
            Thread.sleep(11 * 1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // space should now be empty
        assertEquals(space.size(), 0);


    }

    @Test
    public void testExpires() throws TransactionException {
        purgeSpace();
        for (int i = 0; i < 10; i++) {
            space.put(new SimpleTuple(1), 1000);
        }
        assertTrue(space.size() == 10);
        sleep(2);
        assertTrue(space.size() == 0);
    }

    /**
     * To test putting tuples in with a lifetime of 'forever' i.e. they never
     * expire.
     */
    @Test
    public void testPutTupleIF() throws TransactionException {

        purgeSpace();
        final Tuple tuple1 = new SharedVar("a", 1);
        space.put(tuple1);
        assertTrue(space.size() == 1);

        // CAN  put the same one twice
        space.put(tuple1);
        assertTrue(space.size() == 2);

        space.put(new SharedVar("b", 1));
        space.put(new SharedVar("c", 1));
        space.put(new SharedVar("d", 1));
        space.put(new SharedVar("e", 1));

        assertTrue(space.size() == 6);

        // try adding same ones again - just to be sure!!
        space.put(new SharedVar("b", 1));
        space.put(new SharedVar("b", 1));
        space.put(new SharedVar("b", 1));
        space.put(new SharedVar("b", 1));
        space.put(new SharedVar("b", 1));

        assertTrue(space.size() == 11);

    }

    @Test
    public void testReadIfExists() throws TransactionException {

        purgeSpace();
        SharedVar sv = new SharedVar("a", 1);
        Tuple template = new SharedVar("a", 1);
        assertTrue(space.readIfExists(template) == null);
        space.put(sv);
        assertTrue(space.readIfExists(template) != null);
        assertTrue(space.readIfExists(template).equals(sv));
        assertTrue(space.size() == 1);
    }

    @Test
    public void testReadTuple() throws TransactionException {
        /*
         check that read returnd after a timeout
         check the read does not remove the tuple
         */
        purgeSpace();
        space.put(new SharedVar("a", 1));

        Tuple template = new SharedVar("a", 1);
        assertTrue(space.size() == 1);
        SharedVar read = (SharedVar) space.read(template, 5000);
        assertEquals(1, space.size());

        space.purgeAllEntries();
        assertTrue(space.size() == 0);


        read = (SharedVar) space.read(template, 5000);

        assertTrue(read == null);
        assertTrue(space.size() == 0);
        space.put(new SharedVar("a", 1));
        read = (SharedVar) space.read(template, 5000);
        assertTrue(read != null);

    }

    /**
     * To test getting a tuple using a template and no timeout
     */
    @Test
    public void testGetTupleIF() throws TransactionException {

        purgeSpace();

        space.put(new SharedVar("a", 1));
        space.put(new SharedVar("b", 1));
        space.put(new SharedVar("c", 1));
        space.put(new SharedVar("d", 1));
        space.put(new SharedVar("e", 1));

        assertTrue(space.size() == 5);

        // now try matching
        Tuple template = new SharedVar("a", 1);
        SharedVar take = (SharedVar) space.get(template);
        assertTrue(take.getName().endsWith("a"));
        assertTrue(take.getValue() == 1);
        assertTrue(space.size() == 4);

        // should match "c"
        template = new SharedVar("c");
        take = (SharedVar) space.get(template);
        assertTrue(take.getName().endsWith("c"));
        assertTrue(take.getValue() == 1);
        assertTrue(space.size() == 3);

        template = new SharedVar("d");
        take = (SharedVar) space.get(template);
        assertTrue(take.getName().endsWith("d"));
        assertTrue(take.getValue() == 1);
        assertTrue(space.size() == 2);

        // try some with timeouts
        // //////////////////////////////////////////////////////////
        purgeSpace();

        space.put(new SharedVar("a", 1));
        space.put(new SharedVar("b", 2));
        space.put(new SharedVar("c", 3));
        space.put(new SharedVar("d", 4));
        space.put(new SharedVar("e", 1));
        assertTrue(space.size() == 5);

        template = new SharedVar("a", 12);
        take = (SharedVar) space.get(template, 1000);
        // shouldn't match
        assertTrue(take == null);
        assertTrue(space.size() == 5);

        // //
        // should be able to match this twice on a and e
        template = new SharedVar(null, 1);
        take = (SharedVar) space.get(template, 1000);
        assertTrue(take != null);
        assertTrue(space.size() == 4);

        take = (SharedVar) space.get(template, 1000);
        assertTrue(take != null);
        assertTrue(space.size() == 3);

        // so now should not have a and e
        template = new SharedVar("a", 1);
        take = (SharedVar) space.get(template, 1000);
        assertTrue(take == null);
        assertTrue(space.size() == 3);

        template = new SharedVar("e", 1);
        take = (SharedVar) space.get(template, 1000);
        assertTrue(take == null);
        assertTrue(space.size() == 3);

        // ////////////

        // now have left b,c,d , none have a valeu of 1
        template = new SharedVar(null, 1);
        take = (SharedVar) space.get(template, 1000);
        assertTrue(take == null);
        assertTrue(space.size() == 3);

        template = new SharedVar("e", 1);
        take = (SharedVar) space.get(template, 1000);
        assertTrue(take == null);
        assertTrue(space.size() == 3);

        //
        template = new SharedVar("b");
        take = (SharedVar) space.get(template, 1000);
        assertTrue(take != null);
        assertTrue(take.getName().equals("b"));
        assertTrue(take.getValue() == 2);
        assertTrue(space.size() == 2);

//        template = new SharedVar(3);// c
//        take = (SharedVar) space.get(template, 1000);
//        assertTrue(take != null);
//        assertTrue(take.getName().equals("c"));
//        assertTrue(take.getValue() == 3);
//        assertTrue(space.size() == 1);

        // just d left
        template = new SharedVar("d", 2);// d and not match
        take = (SharedVar) space.get(template, 1000);
        assertTrue(take == null);
        assertTrue(space.size() == 2);

        template = new SharedVar("d", 4);// d and match
        take = (SharedVar) space.get(template, 1000);
        assertTrue(take != null);
        assertTrue(space.size() == 1);
        assertTrue(take.getName().equals("d"));
        assertTrue(take.getValue() == 4);

    }

    @Test
    public void testConcurrency() throws TransactionException {
        purgeSpace();
        final long start = System.currentTimeMillis();
        final Tuple MATCH_ALL = new SimpleTuple(1, 2, "*", "*");
        for (int x = 0; x < 10; x++) {
            purgeSpace();
            final CountDownLatch writeLatch = new CountDownLatch(20);
            for (int i = 0; i < 20; i++) {
                final int id = i;
                Thread writer = new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(10);
                            // Thread.sleep(10 * (long) (10 * Math.random()));
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }


                        space.put(new SimpleTuple(1, 2, 3, id));

                        // System.out.println("added "+id);
                        writeLatch.countDown();

                    }
                };

                writer.start();
            }

            try {
                writeLatch.await();
                System.out.println("size " + space.size());
                assertTrue(space.size() == 20);

            } catch (InterruptedException e) {

                e.printStackTrace();
            }

            //has 20 entries so try and match tyhem
            final CountDownLatch readLatch = new CountDownLatch(4);
            assertTrue(space.size() == 20);
            for (int i = 0; i < 4; i++) {

                Thread reader = new Thread() {
                    int count = 0;

                    @Override
                    public void run() {
                        while (count != 5) {

                            space.get(MATCH_ALL);

                            count++;
                            System.out.println("count " + count);
                        }

                        readLatch.countDown();
                    }
                };
                reader.start();
            }

            try {
                readLatch.await();
                System.out.println("=== space.size() " + space.size());
                //assertTrue(space.size() == 0);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }


        assertTrue(space.size() == 0);
        final long length = (System.currentTimeMillis() - start);
        System.out.println("All done " + length);

        // now similar with SharedVar
        purgeSpace();
        SharedVar sharedVar = new SharedVar("test", 0);
        space.put(sharedVar);

        final SharedVar template = new SharedVar("test");

        // wait for 20 threads
        final CountDownLatch latch = new CountDownLatch(20);
        for (int i = 0; i < 20; i++) {

            Thread reader = new Thread() {
                @Override
                public void run() {

                    try {
                        Thread.sleep((long) (Math.random() * 100));
                        SharedVar readVar = (SharedVar) space.get(template);

                        System.out.println("==>" + readVar.inc());
                        space.put(readVar);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    latch.countDown();
                }
            };
            reader.start();
        }

        try {
            latch.await();

            // get the SharedVar
            SharedVar readVar = (SharedVar) space.get(template);
            assertTrue(readVar.getValue() == 20);
            assertTrue(space.size() == 0);

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * clear space
     */
    private void purgeSpace() {

        space.purgeAllEntries();
        assertTrue(space.size() == 0);
        // assertTrue(space.templatesSize() == 0);
    }

    private void sleep(int seconds) {

        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void spaceChanged(SpaceChange evt) {
        System.out.println(evt);
    }
}
