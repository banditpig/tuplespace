/*******************************************************************************
 * Copyright (c) 2011. Mike Houghton.
 *
 *
 * This file is part of 'TupleSpace'.
 *
 * 'TupleSpace' is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * 'TupleSpace' is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with 'TupleSpace'. If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/

package dist;

import js.co.uk.tuplespace.space.Space;
import js.co.uk.tuplespace.space.TupleSpace;
import js.co.uk.tuplespace.store.TransactionException;
import js.co.uk.tuplespace.store.TransactionID;
import js.co.uk.tuplespace.tuple.SimpleTuple;
import js.co.uk.tuplespace.tuple.Tuple;
import js.co.uk.tuplespace.util.SharedVar;
import org.junit.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import js.co.uk.tuplespace.dist.hess.local.HessianLocalSpaceCreator;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: hought_m
 * Date: 05-Jul-2010
 * Time: 15:21:23
 * To change this template use File | Settings | File Templates.
 */
public class TestTrans {
    
    private static HessianLocalSpaceCreator localSpaceCreator;

    Tuple a = new SimpleTuple("a");
    Tuple b = new SimpleTuple("b");
    Tuple c = new SimpleTuple("c");
    Tuple d = new SimpleTuple("d");

    Tuple w = new SimpleTuple("w");

    Tuple x = new SimpleTuple("x");
    Tuple y = new SimpleTuple("y");
    Tuple z = new SimpleTuple("z");


    int MAX = 500;
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
        // PropertyConfigurator.configure(SimpleSpaceTestUsingGeneralTupleIF.class.getResource("/properties/logging.properties"));
         localSpaceCreator  = new HessianLocalSpaceCreator("http://127.0.0.1:8080");
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


        System.out.println("MAKE SPACE");
        space = localSpaceCreator.createSpace("TestTrans");
        space.purgeAllEntries();
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
    public void testVeryBasics(){
        space.put(a);
        System.out.println(space.size());
    }
    @Test
    public void testGetAfterReadInSameTxn() {

        try {
            space.put(a, null);
            System.out.println("a " + space.size());
            TransactionID txn = space.beginTxn(10000l);
            System.out.println("read " + space.read(a, 1000, txn));
            System.out.println("b " + space.size());
            System.out.println("get " + space.get(a, 1000, txn));
            System.out.println("a " + space.size());
            space.commitTxn(txn);
            System.out.println("done " + space.size());
            assertEquals(space.size(), 0);


        } catch (TransactionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    @Test
    public void testGetOutsideTxnFailsOnTupleReadInTxn() {

        try {
            space.put(a, null);
            assertEquals(space.size(), 1);

            TransactionID txn = space.beginTxn(20000l);
            Tuple rd = space.read(a, 2000, txn);
            System.out.println(txn);
            assertEquals(space.size(), 1);
            assertTrue(rd != null);

            Tuple get = space.get(a, 500, null);
            assertTrue(get == null);

            space.commitTxn(txn);
            get = space.get(a, 500, null);
            assertTrue(get != null);

        } catch (TransactionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    @Test
    public void testGetInTXNWithPut() {
        /*
        get in a txn runs in both main space and txn space.
        The get in txn  should be released from its wait if a suitable tuple is put into the space (eitehr under
         the same txn as the get or under no txn
         */

        try {
            final TransactionID txn = space.beginTxn(20000L);

            //puts target tuple in without txn first - to be sure
            Thread t = new Thread() {
                public void run() {
                    try {
                        Thread.sleep(1000);
                        space.put(a, null);
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (TransactionException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }

                }
            };
            t.start();
            //get without txn first - to be sure
            Tuple rd = space.get(a, 20000, null);
            assertTrue(rd != null);
            //expect the get to block for about 1 second
            System.out.println("ok");
            purgeSpace();
            //=========


            final TransactionID txn1 = space.beginTxn(20000L);

            //puts target tuple in
            t = new Thread() {
                public void run() {
                    try {
                        Thread.sleep(1000);
                        space.put(a, txn1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (TransactionException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }

                }
            };
            t.start();
            //get without txn first - to be sure
            Tuple rd1 = space.get(a, 10000, txn1);
            System.out.println("got rd1 " + rd1);
            assertTrue(rd1 != null);
            //expect the get to block for about 1 second
            //TODO currently it does not, it takes aboout 8 seconsd, the get executes in main space first
            //and then in txn space
            System.out.println("ok2");
            purgeSpace();
            //=========


        } catch (TransactionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Test
    public void testWaiting() {

        try {
            space.put(a, null);
            TransactionID t1 = space.beginTxn(2000l);
            assertTrue(space.read(a, 10000, t1) != null);
            TransactionID t2 = space.beginTxn(10000l);
            System.out.println(space.get(a, 4000, t2));


        } catch (TransactionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }


    @Test
    public void beginThenAbortEmptyTransactions() {
        int MAX = 200;

    }


    public void xx() {
        int N = 200;
        final CountDownLatch latch = new CountDownLatch(N);
        final int innerLoop = 1000;
        for (int loop = 0; loop < N; loop++) {

            final int loopFinal = loop;
            Thread t = new Thread() {
                public void run() {
                    try {
                        Thread.sleep((long) (1000 * Math.random()));
                        final TransactionID tx = space.beginTxn(1000000L);
                        for (int i = 0; i < innerLoop; i++) {
                            space.put(new SimpleTuple(1, 2, 3), 8000, tx);
                        }


                        space.commitTxn(tx);


                        latch.countDown();
                    } catch (TransactionException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }

                }
            };
            t.start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        System.out.println("done");
        junit.framework.Assert.assertEquals(innerLoop * N, space.size());
        System.out.println("size " + space.size());
        sleep(60);
        junit.framework.Assert.assertEquals(0, space.size());
        System.out.println("size " + space.size());

    }


    @Test
    public void testMultipleTxn() throws TransactionException {
        int N = 2;
        final CountDownLatch latch = new CountDownLatch(N);
        TransactionID tx = null;
        try {
            tx = space.beginTxn(10000L);
        } catch (TransactionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        final TransactionID txn = tx;
        final Tuple t1 = new SimpleTuple(1, 2, 3);
        final Tuple t2 = new SimpleTuple(1, 2, 4);
        final Tuple t3 = new SimpleTuple(1, 2, 5);

        space.put(t1, txn);
        space.put(t2, txn);
        space.put(t3, txn);
        assertTrue(space.size() == 0);

        final Tuple template = new SimpleTuple(1, 2, "*");
        Tuple res = space.get(template, 1000, txn);
        System.out.println("got  Tuple res = space.get(template, expire, txn); " + res);
        final AtomicLong matchCount = new AtomicLong(0);
        for (int i = 0; i < N; i++) {


            final Thread t = new Thread() {

                public void run() {


                    int match = 0;
                    for (int i = 0; i < 10; i++) {
                        // System.out.println("i " + i + " c " + cF);
                        int expire = 34 * (i + 1) * 10;

                        Tuple res = null;
                        try {
                            res = space.get(template, expire, txn);
                        } catch (TransactionException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                        if (res != null) {

                            matchCount.getAndIncrement();
                        }
                        if (i % 2 == 0) {

                            try {
                                space.put(new SharedVar("x", i), txn);
                            } catch (TransactionException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                        }
                        if (i == 3) {
                            // just add some dross that will nver get matched
                            try {
                                space.put(new SharedVar("fred", 1), txn);
                            } catch (TransactionException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                        }

                        try {
                            Thread.sleep(100);
                            if (res != null) {
                                space.put(res, null);
                            }


                        }

                        catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (TransactionException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    }


                    latch.countDown();


                }
            };

            t.start();
        }

        try {
            latch.await();
            space.commitTxn(txn);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        catch (TransactionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Test
    public void testWaitForCommit() throws TransactionException {
        final Tuple template = new SimpleTuple("a");
        Thread t = new Thread() {
            public void run() {
                try {
                    System.out.println("waiting...");
                    space.read(template, 600000, null);
                    System.out.println("boooo!");
                } catch (TransactionException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        };
        //wait for a "a"
        t.start();

        TransactionID txn = space.beginTxn(10000l);

        Tuple tpl = new SimpleTuple("a");
        space.put(tpl, 1000, txn);
        space.commitTxn(txn);
        assertEquals(1, space.size());

        sleep(2);
        assertEquals(0, space.size());

    }

    @Test
    public void testSimultaneos() {
        final AtomicLong matchCount = new AtomicLong(0);

      space.purgeAllEntries();
        long start = System.currentTimeMillis();
        int N = 30;
        final CountDownLatch latch = new CountDownLatch(N);
        for (int c = 0; c < N; c++) {

            final int cF = c;

            final Thread t = new Thread() {

                public void run() {
                    try {
                        TransactionID txn = space.beginTxn(120000L);

                        int match = 0;
                        for (int i = 0; i < 50; i++) {
                            // System.out.println("i " + i + " c " + cF);
                            int expire = 34 * (i + 1) * 10;

                            Tuple res = space.get(new SharedVar("x"), 100, txn);
                            if (res != null) {
                                matchCount.getAndIncrement();
                            }
                            if (i % 2 == 0) {

                                space.put(new SharedVar("x", i), txn);
                            }
                            if (i == 3) {
                                // just add some dross that will nver get matched
                                space.put(new SharedVar("fred", 1), txn);
                            }

                            try {
                                Thread.sleep(100);

                            }
                            catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                        space.commitTxn(txn);

                    } catch (TransactionException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    finally {
                        latch.countDown();
                    }

                }
            };

            t.start();
        }

        try {
            System.out.println("latch   wait");
            latch.await();
            System.out.println("latch DOEN  wait");
        }
        catch (InterruptedException e) {
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
    public void testGetInTxn() throws TransactionException {
        TransactionID tx = null;
        try {
            tx = space.beginTxn(10000L);
        } catch (TransactionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        final Tuple t1 = new SimpleTuple(1, 2, 3);
        final Tuple t2 = new SimpleTuple(1, 2, 4);
        final Tuple t3 = new SimpleTuple(1, 2, 5);
        /*
       put these in BUT  in the thread below the line
         Tuple res = space.get(template, expire, txn); doesnt work!!
         seems like gets in txn don't work!
        */
        space.put(t1, tx);
        space.put(t2, tx);
        space.put(t3, tx);
        // assertTrue(space.size() == 0);

        final Tuple template = new SimpleTuple(1, 2, "*");
        Tuple res = space.get(template, 1000, tx);
        System.out.println("got  Tuple res = space.get(template, expire, txn); " + res);
    }

    @Test
    public void testBasics() {

        purgeSpace();
        try {
            final TransactionID id = space.beginTxn(5000L);
            assertTrue(id != null);
            space.put(a, id);
            space.put(b, id);
            //not in main space
            assertEquals(space.size(), 0);
            //put c outside txn
            space.put(c, null);
            assertEquals(space.size(), 1);
            //just quick check that it reads ok and then put it back
            Tuple read = space.get(c, 1000, null);
            assertEquals(space.size(), 0);
            sleep(2);
            assertTrue(read.equals(c));

            //and back
            space.put(c, null);

            //size is one (=c)  as a,b are under a tx
            assertTrue(space.size() == 1);

            //a is only visible in the txn
            Tuple rd = space.readIfExists(a, null);
            assertTrue(rd == null);

            rd = space.readIfExists(a, id);
            assertTrue(rd != null);
            assertTrue(rd.equals(a));

            //do readif exist - this goes in main or txn space and MUST mark the
            //read tuple as unavailabel for gets
            System.out.println("readif c");
            rd = space.readIfExists(c, id);
            assertTrue(rd != null);
            assertTrue(rd.equals(c));
            assertTrue(space.size() == 1);

            //now try getting after readIfExists
            System.out.println("Doing get");
            read = space.get(c, 1000, null);  //read for 1 second
            System.out.println("read fo c " + read);
            sleep(2); //catchup


            assertTrue(read == null); //////////////////////////////
            assertTrue(space.size() == 1);


        } catch (TransactionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    @Test
    public void testAbort1() throws TransactionException {
        purgeSpace();
        final TransactionID id;
        space.put(w, null);
        space.put(x, null);

        try {
            id = space.beginTxn(10000L);
            space.put(a, id);
            space.put(b, id);
            Tuple read = space.readIfExists(w, id);//should now be marked as unavailable for gets
            assertTrue(read.equals(w));

            //try to get w - should fail
            read = space.get(w, 500, null);  //read for 1 second
            sleep(1); //catchup
            assertTrue(read == null);

            //abort should make w available again
            space.abortTxn(id);
            read = space.get(w, 500, null);  //read for 1 second
            sleep(1); //catchup
            assertTrue(read != null);
            assertTrue(read.equals(w));


        } catch (TransactionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    @Test
    public void testAbort2() throws TransactionException {

        purgeSpace();
        final TransactionID id;
        space.put(w, 10000, null);
        space.put(x, 4500, null);
        assertTrue(space.size() == 2);
        try {
            id = space.beginTxn(500000L);
            space.put(a, id);
            space.put(b, id);
            assertTrue(space.size() == 2);

            space.get(w, id);
            assertTrue(space.size() == 1);
            System.out.println("1");
            space.get(x, id);
            assertTrue(space.size() == 0);
            System.out.println("2");

            //now abort and w, x should go back
            space.abortTxn(id);   ///////////////
            System.out.println("3");
            assertTrue(space.size() == 2);
            //now wait 2 seconds and x should be removed
            sleep(6);
            System.out.println("size "+space.size());
            assertTrue(space.size() == 1);
            assertTrue(space.readIfExists(w, null).equals(w));
            System.out.println("4");

            sleep(10);
            assertTrue(space.size() == 0);

        } catch (TransactionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    @Test
    public void testCommit() {

        purgeSpace();
        final TransactionID idOne;
        final TransactionID idTwo;
        try {
            idOne = space.beginTxn(10000L);
            idTwo = space.beginTxn(10000L);
            space.put(a, idOne);
            space.put(b, idOne);
            space.put(c, idOne);
            assertTrue(space.size() == 0);

            space.put(w, idTwo);
            space.put(x, idTwo);
            space.put(y, idTwo);
            assertTrue(space.size() == 0);

            space.commitTxn(idOne);
            assertTrue(space.size() == 3);
            space.commitTxn(idTwo);
            assertTrue(space.size() == 6);

           space.commitTxn(idTwo);


        } catch (final TransactionException e) {
            assertTrue("Caught TransactionException caused by  space.commitTxn(idTwo)", true);
        }

    }

    @Test
    public void testAbortAfterValueTimeout() {

        space.put(a, 1000);   //a shoud go after 1 second
        try {
            TransactionID id = space.beginTxn(66000L); //timeout afterages
            space.get(a, id); //get under a txn
            assertEquals(space.size(), 0);
            sleep(2);
            space.abortTxn(id);

            assertEquals(space.size(), 0);


        } catch (TransactionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    @Test
    public void testTimeoutInTxn() throws TransactionException, InterruptedException {

        //simple
        try {
            TransactionID txn = space.beginTxn(4000L); //timeout 4
            space.put(a,500,txn);
            sleep(1);
            space.commitTxn(txn);
            //should be empty
            assertEquals(space.size(), 0);
            

        } catch (TransactionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        space.purgeAllEntries();
        final TransactionID txn = space.beginTxn(40000L);
        int last =10;
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch end = new CountDownLatch(last);

        for(int i=0;i<last;i++){

            Thread t = new Thread(){

                public void run() {
                    try {
                        start.await();
                        space.put(a,1000,txn);
                        System.out.println("writin  "+txn);

                        end.countDown();
                    } catch (TransactionException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            };
            t.start();
        }

        start.countDown();
        end.await();
          space.commitTxn(txn);
        System.out.println("size "+space.size());
        assertEquals(space.size(), last);
        System.out.println("sleep...");
        sleep(2);//should all timeout

        assertEquals(space.size(), 0);


    }


    @Test
    public void testCommitAfterValueTimeout() {

        space.put(a, 1000);   //a shoud go after 1 second
        try {
            TransactionID id = space.beginTxn(2000L); //timeout afterages
            space.get(a, id); //get under a txn
            assertEquals(space.size(), 0);
            space.abortTxn(id);
            assertEquals(space.size(), 1);
            sleep(2);
            assertEquals(space.size(), 0);


        } catch (TransactionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Test
    public void testTxnTimeout() throws TransactionException {

        purgeSpace();
        space.put(w, null);
        space.put(x, null);
        assertTrue(space.size() == 2);
        //now get w, x under a txn and let it timeout, expectr w, x to go back into space
        try {
            TransactionID id = space.beginTxn(5000L); //timeout after 5 seconds
            space.get(w, 1000, id);
            space.get(x, 1000, id);
            space.get(a, 1000, id);
            assertTrue(space.size() == 0);
            sleep(6);
            System.out.println("size " + space.size());
            assertTrue(space.size() == 2);

            //a commit now should force a TransactionException
            //todo test for TransactionException here
        } catch (TransactionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    @Test
    public void testPutWithTimeout() {
        purgeSpace();
        try {
            final TransactionID txn = space.beginTxn(20000000L);
            space.put(a, 2000, txn);
            space.put(b, 2000, txn);
            space.put(c, 2000, txn);
            assertTrue(space.size() == 0);
            sleep(1);
            space.commitTxn(txn);
            //  assertTrue(space.readIfExists(a,null).equals(a));
            assertTrue(space.size() == 3);
            sleep(6);
            assertEquals(0, space.size());


        } catch (TransactionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Test
    public void testgetInMainSpaceWithAbort() throws TransactionException {
        purgeSpace();
        space.put(a, 1000, null);
        assertTrue(space.size() == 1);
        try {
            final TransactionID txn = space.beginTxn(20000000L);

            Tuple get = space.get(a, txn);
            assertTrue(get.equals(a));
            assertTrue(space.size() == 0);
            space.abortTxn(txn);
            assertTrue(space.size() == 1);
            sleep(2);
            assertTrue(space.size() == 0);


        } catch (TransactionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    @Test
    public void testXX() {
        purgeSpace();
        try {
            final TransactionID txn = space.beginTxn(20000000L);
            space.put(a, 4000, txn);
            space.put(b, 4000, txn);
            space.put(c, 4000, txn);
            Thread t = new Thread() {
                public void run() {
                    try {

                        space.get(d, 2000, txn);
                    } catch (TransactionException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            };
            t.start();

            space.put(d, null);
            sleep(1);
            space.commitTxn(txn);
            //  assertTrue(space.readIfExists(a,null).equals(a));
            assertTrue(space.size() == 3);
             sleep(5);
            assertTrue(space.size() == 0);
        } catch (TransactionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void sleep(final int period) {
        try {
            Thread.sleep(period * 1000);
        }
        catch (InterruptedException e) {

        }

    }

    private void purgeSpace() {

        space.purgeAllEntries();
        assertTrue(space.size() == 0);

    }

}
