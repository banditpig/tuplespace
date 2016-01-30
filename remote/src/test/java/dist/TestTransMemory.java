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

import org.junit.*;

import static org.junit.Assert.assertTrue;

import js.co.uk.tuplespace.store.Transaction;
import js.co.uk.tuplespace.store.TransactionException;
import js.co.uk.tuplespace.store.TransactionID;
import js.co.uk.tuplespace.tuple.SimpleTuple;
import js.co.uk.tuplespace.tuple.Tuple;
import js.co.uk.tuplespace.space.Space;
import js.co.uk.tuplespace.space.TupleSpace;

import java.util.ArrayList;
import js.co.uk.tuplespace.dist.hess.local.HessianLocalSpaceCreator;

import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: mike
 * Date: 06-Nov-2010
 * Time: 16:14:28
 * To change this template use File | Settings | File Templates.
 */
public class TestTransMemory {
    
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
        space = localSpaceCreator.createSpace("testTransMemory");
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

    //==================================================================================================================
    /*
   Main txn testing routines. Mainly for looking at memory and thread use. Main scenarios are:

   Empty Transactions  + Abort
   ==============================
     Aborted manually (ie abortTxn) this can be done just after each txn is created OR a bumch of txns can be created into
     a lits  and then each aborted in turn.


   NOT  Empty Transactions  + Abort
   =================================
   Same cases as above except that the space should not have anything in as though the txns are not empty they are aborted
   so any tuples should not enter the space.



   Empty Transactions + Commit
   ============================


   NOT Empty Transactions + Commit
   ================================





    */

    //=========================== ABORT ===========================================


    @Test
    public void testALotofEmptyTransactionsAbortedManually() {

        //abort each just after its been created
        //========================================

        for (int i = 0; i < MAX; i++) {
            try {
                System.out.println("i " + i);
                TransactionID txn = space.beginTxn(10000000L);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {

                }

                space.abortTxn(txn);
                System.out.println("------------------");
            } catch (TransactionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }

        //make  a batch of txns, save them and then abort
        //========================================
        ArrayList<TransactionID> txns = new ArrayList<TransactionID>();
        for (int i = 1; i <= MAX; i++) {
            try {
                TransactionID txn = space.beginTxn(1000000000l); //very long timeout
                txns.add(txn);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {

                }

                System.out.println(" up i " + i);
            } catch (TransactionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }


        }
        System.out.println("========================================= wait 10 seconds");
        sleepy(10);

        for (TransactionID txn : txns) {
            try {
                System.out.println(" down i " + txn);
                space.abortTxn(txn);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {

                }
            } catch (TransactionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }


        }
        sleepy(10);


    }

    //-------------------
    @Test
    public void testALotofEmptyTransactionsAbortedByTimeout() {

        for (int i = 0; i < MAX; i++) {
            try {
                System.out.println("i " + i);
                TransactionID txn = space.beginTxn(i + 2000L);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {

                }


            } catch (TransactionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }
        System.out.println("all in, now wait 60 seconds ");
        sleepy(60);
    }

    //-----------------------------------------  NOT EMPTY TXNS ---------------------------------------------
    @Test
    public void testALotofNOTEmptyTransactionsAbortedByTimeout() {

        for (int i = 0; i < MAX; i++) {
            try {
                System.out.println("i " + i);
                TransactionID txn = space.beginTxn(i + 2000L);
                space.put(a, txn);
                space.put(b, txn);
                space.put(c, txn);

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {

                }


            } catch (TransactionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }
        System.out.println("all in, now wait 20 seconds ");
        sleepy(20);
        assertEquals(space.size(), 0);
    }

    //-----------------------
    @Test
    public void testALotofNOTEmptyTransactionsAbortedManually() {

        //abort each just after its been created
        //========================================

        for (int i = 0; i < MAX; i++) {
            try {
                System.out.println("i " + i);
                TransactionID txn = space.beginTxn(10000000L);
                space.put(a, txn);
                space.put(b, txn);
                space.put(c, txn);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {

                }

                space.abortTxn(txn);
                System.out.println("------------------");
            } catch (TransactionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }
        sleepy(2);
        assertEquals(space.size(), 0);

        //make  a batch of txns, save them and then abort
        //========================================
        ArrayList<TransactionID> txns = new ArrayList<TransactionID>();
        for (int i = 1; i <= MAX; i++) {
            try {
                TransactionID txn = space.beginTxn(1000000000l); //very long timeout
                space.put(a, txn);
                space.put(b, txn);
                space.put(c, txn);
                txns.add(txn);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {

                }

                System.out.println(" up i " + i);
            } catch (TransactionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }


        }
        System.out.println("========================================= wait 10 seconds");
        sleepy(10);

        for (TransactionID txn : txns) {
            try {
                System.out.println(" down i " + txn);
                space.abortTxn(txn);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {

                }
            } catch (TransactionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }


        }
        sleepy(10);
        assertEquals(space.size(), 0);


    }

    //=============================== COMMITS ========================
    @Test
    public void testALotofEmptyTransactionsCommited() {

        //commit each just after its been created
        //========================================

        for (int i = 0; i < MAX; i++) {
            try {
                System.out.println("making empty for immediate commit" + i);
                TransactionID txn = space.beginTxn(10000000L);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {

                }

                space.commitTxn(txn);

            } catch (TransactionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }

        //make  a batch of txns, save them and then commit
        //========================================
        ArrayList<TransactionID> txns = new ArrayList<TransactionID>();
        for (int i = 1; i <= MAX; i++) {
            try {
                System.out.println(" up i making for batch commit " + i);
                TransactionID txn = space.beginTxn(1000000000l); //very long timeout
                txns.add(txn);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {

                }


            } catch (TransactionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }


        }
        System.out.println("========================================= wait 10 seconds for commit ");
        sleepy(10);

        for (TransactionID txn : txns) {
            try {
                System.out.println(" down  commit " + txn);
                space.commitTxn(txn);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {

                }
            } catch (TransactionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }


        }
        System.out.println("done commits, GC and  sleep 10");
        System.gc();
        sleepy(10);
        assertEquals(space.size(), 0);


    }
    //--------

    @Test
    public void testALotOfNOTEmptyTransactionsCommited() {
        //commit each just after its been created - each tuple has a time out of 5 seconds
        //========================================

        final SimpleTuple template = new SimpleTuple("a", "*");
//        for (int i = 0; i < MAX; i++) {
//            try {
//                System.out.println("making NOT Empty for immediate commit" + i);
//                TransactionID txn = space.beginTxn(70000L);
//                space.put(new SimpleTuple("a",i),8000,null);
//                try {
//                    Thread.sleep(10);
//                }
//                catch (InterruptedException e) {
//
//                }
//               Tuple t = space.read(template, 2000,txn);
//                System.out.println("t "+t);
//                space.commitTxn(txn);
//                assertEquals(space.size(), i + 1);
//                System.out.println("size " + space.size());
//
//            } catch (TransactionException e) {
//                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            }
//
//        }
//
//       assertEquals(MAX, space.size());
//
//        System.out.println("done  " + MAX + "  sleep GC and sleep 10");
//
//        sleep(10);
//         System.gc();
//         assertEquals(0, space.size());
//
//        space.purgeAllEntries();
//        System.out.println("Space cleared size is " + space.size());
//        System.out.println("Now do batch of txns for commit");
        //make  a batch of txns, save them and then commit
        //========================================
        ArrayList<TransactionID> txns = new ArrayList<TransactionID>();
        for (int i = 1; i <= MAX; i++) {
            try {
                System.out.println(" up i making for batch commit " + i);
                TransactionID txn = space.beginTxn(1000000000l); //very long timeout
                space.put(new SimpleTuple("a" + i), 30000, txn);
                txns.add(txn);
                assertEquals(space.size(), 0);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {

                }


            } catch (TransactionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }


        }
        System.out.println("========================================= now  commit ");

        for (TransactionID txn : txns) {
            try {
                System.out.println(" down  commit " + txn);
                space.commitTxn(txn);
//                try {
//                    Thread.sleep(1);
//                }
//                catch (InterruptedException e) {
//
//                }
            } catch (TransactionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }


        }
        assertEquals(MAX, space.size());
        System.out.println("done commits, GC and  sleep 15");

        System.gc();
        sleepy(60);
        System.gc();
        System.out.println("all done");
        assertEquals(0, space.size());


    }

    @Test
    public void testMemoryOkAfterTXNTimeout() {
        for (int i = 0; i < 500; i++) {

            try {

                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {

                }
                System.out.println(space.beginTxn(1000l));
            } catch (TransactionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }

        System.out.println("---- sssss");
        sleepy(10);

    }

    //==================================================================================================================
    //=============================================================================================
    @Test
    public void mainTXNTest() {

        testALotofEmptyTransactionsAbortedManually();


        testALotofEmptyTransactionsAbortedByTimeout();


        testALotofNOTEmptyTransactionsAbortedByTimeout();


        testALotofNOTEmptyTransactionsAbortedManually();
        testALotOfNOTEmptyTransactionsCommited();

        System.out.println("ALL DONE  do gc and sleep 10");
        System.gc();
        sleepy(10);
    }

    //      public void testTXNOnMap() {
//
//          TimeoutMap<Integer, Tuple> spaceMap = new TimeoutMap<Integer, Tuple>();
//
//          spaceMap.setTransactionsEnabled(true);
//          spaceMap.setMatcher(new FieldBasedMatcher());
//
//          int MAX = 500;
//          for (int i = 0; i < MAX; i++) {
//              try {
//                  long x1 = Runtime.getRuntime().freeMemory();
//                  System.out.println("x1 " + x1);
//                  TransactionID txn = spaceMap.beginTxn(10000000L);
//                  try {
//                      Thread.sleep(250);
//                  }
//                  catch (InterruptedException e) {
//
//                  }
//
//                  long x2 = Runtime.getRuntime().freeMemory();
//                  System.out.println("x2 " + x2 + "  x1-x2 " + (x1 - x2));
//                  spaceMap.abortTxn(txn);
//                  long x3 = Runtime.getRuntime().freeMemory();
//                  System.out.println("x3 " + x3 + "  x1-x3 " + (x1 - x3));
//                  System.out.println("------------------");
//              } catch (TransactionException e) {
//                  e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//              }
//
//          }
//
//      }
//
    public void sleepy(final int period) {
        try {
            Thread.sleep(period * 1000);
        } catch (InterruptedException e) {

        }

    }


    private void purgeSpace() {

        space.purgeAllEntries();
        assertTrue(space.size() == 0);

    }

}
