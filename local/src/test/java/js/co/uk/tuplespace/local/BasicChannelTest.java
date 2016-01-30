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
package js.co.uk.tuplespace.local;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import js.co.uk.tuplespace.comms.channel.BasicChannel;
import js.co.uk.tuplespace.comms.channel.ChannelTimeoutException;
import js.co.uk.tuplespace.comms.channel.Status;
import js.co.uk.tuplespace.events.EventHint;
import js.co.uk.tuplespace.events.SpaceChange;
import js.co.uk.tuplespace.events.SpaceChangeListener;
import js.co.uk.tuplespace.space.Space;
import js.co.uk.tuplespace.space.TupleSpace;
import js.co.uk.tuplespace.store.TransactionException;
import js.co.uk.tuplespace.tuple.SimpleTuple;
import js.co.uk.tuplespace.tuple.Tuple;
import static org.junit.Assert.*;
import org.junit.*;

/**
 *
 * @author mike
 */
public class BasicChannelTest implements SpaceChangeListener {

  
    static Space space;
    SpaceChange spaceEvent;

    public BasicChannelTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        space = new TupleSpace("BasicChannelTest");


        space.addSpaceChangeListener(this);

    }

    @After
    public void tearDown() {
    }

    
      private static final Integer  LIMIT = 1;
   /**
     * Test of insert method, of class BasicChannel.
     */
    @Test
    public void channelSizeIsCorrect() {

        final BasicChannel chan = new BasicChannel(space, "BasicChannelTest", LIMIT);
        int MAX = 500;
        if (LIMIT != null){
            MAX = LIMIT-1;
        }
         
        for (int i = 0; i < MAX; i++) {
            Tuple tuple = new SimpleTuple(i, 2, 3);
            try {
                chan.insert(tuple);
            } catch (ChannelTimeoutException ex) {
                Logger.getLogger(BasicChannelTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        final Status status = (Status) space.get(Status.createMatchTemplateForNotFull("BasicChannelTest"));


        assertEquals(space.size(), MAX); //MAX messages 

        //nothing removed so hd = 1
        long hd = (long) status.getHead();
        assertEquals(1L, hd);

        //+1 as tail points to the NEXT empty slot
        long tl = (long) status.getTail();
        assertEquals((long) MAX + 1, tl);



    }

    @Test
    public void add_then_remove_with_fast_txn_timeout() throws ChannelTimeoutException {
        final BasicChannel chan = new BasicChannel(space, "BasicChannelTest", 100, LIMIT);

        Tuple tuple = new SimpleTuple(1, 2, 3);

        chan.insert(tuple);
        System.out.println("space " + space.size());
        System.out.println("doing remove...");
        final Tuple t = chan.remove();

        System.out.println("removed --->" + t);
        System.out.println("done and size is " + space.size());
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("FINAL done and size is " + space.size());
    }

    @Test
    public void addThenRemoveAFew()  {

        final BasicChannel chan = new BasicChannel(space, "BasicChannelTest", LIMIT);
         int MAX = 10;
          if (LIMIT != null){
            MAX = LIMIT-1;
        }
        for (int i = 0; i < MAX; i++) {
            Tuple tuple = new SimpleTuple(i, 2, 3);
            try {
                chan.insert(tuple);
            } catch (ChannelTimeoutException ex) {
                Logger.getLogger(BasicChannelTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        int count = 0;


        for (;;) {
             Tuple t = null;
            try {
                t = chan.remove();
            } catch (ChannelTimeoutException ex) {
                Logger.getLogger(BasicChannelTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (t == null) {
                break;
            }
            count++;

        }
        assertEquals(MAX, count);
        //only statusleft
        assertEquals(space.size(), 1);


    }

    @Test
    public void addThenRemoveJustOne(){

        final BasicChannel chan = new BasicChannel(space, "BasicChannelTest", LIMIT);
        final int MAX = 1;
        for (int i = 0; i < MAX; i++) {
            Tuple tuple = new SimpleTuple(i, 2, 3);
            try {
                chan.insert(tuple);
            } catch (ChannelTimeoutException ex) {
                Logger.getLogger(BasicChannelTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        int count = 0;


        for (;;) {
             Tuple t = null;
            try {
                t = chan.remove();
            } catch (ChannelTimeoutException ex) {
                Logger.getLogger(BasicChannelTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (t == null) {
                break;
            }
            count++;

        }
        assertEquals(MAX, count);
        //only status left
        assertEquals(space.size(), 1);


    }

    @Test(expected=ChannelTimeoutException.class)
    public void removeFromEmptyChannel() throws ChannelTimeoutException {
       

         final BasicChannel chan = new BasicChannel(space, "BasicChannelTest", LIMIT);
        //only status on
        assertEquals(space.size(), 1);


        final Tuple t = chan.remove();
        //should retun null and txn aborted
        assertNull(t);
        assertEquals(space.size(), 1);
    }

    //--------------------------------------------------------------------------
    // 
    // Some concurrent test
    //
    @Test
    public void insert_QUICKLY_and_remove_SLOWLY_with_one_producer_and_one_consumer() throws ChannelTimeoutException {


        final int HOW_MANY_TO_INSERT = 500;
        final int HOW_MANY_TO_INSERT_PLUS1 = HOW_MANY_TO_INSERT + 1;

        final CountDownLatch latch = new CountDownLatch(HOW_MANY_TO_INSERT);

        final BasicChannel chan = new BasicChannel(space, "BasicChannelTest", 500L, LIMIT);

        final Thread inserter = new Thread() {

            int inserted = 0;

            @Override
            public void run() {

                while (inserted++ < HOW_MANY_TO_INSERT) {


                    final Tuple t = new SimpleTuple(inserted);
                    try {
                        chan.insert(t);
                    } catch (ChannelTimeoutException ex) {
                        Logger.getLogger(BasicChannelTest.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    sleepRandon(5);

                }

            }
        };


        final Thread remover = new Thread() {

            int removed = 0;

            @Override
            public void run() {

                while (removed < HOW_MANY_TO_INSERT) {
                    try {
                        // sleepRandon(100);
                        Thread.sleep(25);
                    } catch (InterruptedException ex) {
                    }

                     Tuple t = null;
                    try {
                        t = chan.remove();
                    } catch (ChannelTimeoutException ex) {
                        Logger.getLogger(BasicChannelTest.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.out.println(" removing and space size is " + space.size()  + " total removed " + removed);
                    if (t != null) {
                        removed++;
                        latch.countDown();

                    }


                }



            }
        };

        inserter.start();
        remover.start();
        try {
            latch.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(BasicChannelTest.class.getName()).log(Level.SEVERE, null, ex);
        }


       
        //just Status  in space
        assertEquals(1, space.size());

        //remove Status
        final Status status = (Status) space.get(Status.createMatchTemplateForNotFull("BasicChannelTest"));
        //empty space
        assertEquals(0, space.size());


        assertEquals((long) HOW_MANY_TO_INSERT_PLUS1, (long) status.getHead());
        assertEquals((long) HOW_MANY_TO_INSERT_PLUS1, (long) status.getTail());


        System.out.println("  == DONE === ");

    }

    @Test
    public void insert_SLOWLY_and_remove_QUICKLY_with_one_producer_and_one_consumer() {


        final int HOW_MANY_TO_INSERT = 500;
        final int HOW_MANY_TO_INSERT_PLUS1 = HOW_MANY_TO_INSERT + 1;

        final CountDownLatch latch = new CountDownLatch(HOW_MANY_TO_INSERT);

        final BasicChannel chan = new BasicChannel(space, "BasicChannelTest", 500, LIMIT);

        final Thread inserter = new Thread() {

            int inserted = 0;

            @Override
            public void run() {

                while (inserted++ < HOW_MANY_TO_INSERT) {


                    final Tuple t = new SimpleTuple(inserted);
                    try {
                        chan.insert(t);
                    } catch (ChannelTimeoutException ex) {
                        Logger.getLogger(BasicChannelTest.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    sleepRandon(25);

                }

            }
        };


        final Thread remover = new Thread() {

            int removed = 0;

            @Override
            public void run() {

                while (removed < HOW_MANY_TO_INSERT) {


                    sleepRandon(10);
                     Tuple t = null;
                    try {
                        t = chan.remove();
                    } catch (ChannelTimeoutException ex) {
                        Logger.getLogger(BasicChannelTest.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    if (t != null) {
                        removed++;
                        latch.countDown();

                    }


                }



            }
        };

        inserter.start();
        remover.start();
        try {
            latch.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(BasicChannelTest.class.getName()).log(Level.SEVERE, null, ex);
        }


      
        //just Status  in space
        assertEquals(1, space.size());

        //remove Status
        final Status status = (Status) space.get(Status.createMatchTemplateForNotFull("BasicChannelTest"));
        //empty space
        assertEquals(0, space.size());


        assertEquals((long) HOW_MANY_TO_INSERT_PLUS1, (long) status.getHead());
        assertEquals((long) HOW_MANY_TO_INSERT_PLUS1, (long) status.getTail());
        
        System.out.println("  == DONE === ");

    }

    @Test
    public void insert_SLOWLY_and_remove_QUICKLY_with_MANY_producer_and_consumer() {


        final int THREAD_COUNT = 50;


        final int HOW_MANY_TO_INSERT = 30;
        final int HOW_MANY_TO_INSERT_PLUS1 = HOW_MANY_TO_INSERT + 1;

        final CountDownLatch latch = new CountDownLatch(HOW_MANY_TO_INSERT * THREAD_COUNT);

        final BasicChannel chan = new BasicChannel(space, "BasicChannelTest", 4000L, LIMIT);

        for (int i = 0; i < THREAD_COUNT; i++) {


            final Thread inserter = new Thread() {

                int inserted = 0;

                @Override
                public void run() {

                    while (inserted++ < HOW_MANY_TO_INSERT) {


                        final Tuple t = new SimpleTuple(inserted);
                        try {
                            chan.insert(t);
                        } catch (ChannelTimeoutException ex) {
                            Logger.getLogger(BasicChannelTest.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        sleepRandon(25);

                    }

                }
            };

            inserter.start();
        }

        for (int i = 0; i < THREAD_COUNT; i++) {
            final Thread remover = new Thread() {

                int removed = 0;

                @Override
                public void run() {

                    while (removed < HOW_MANY_TO_INSERT) {


                        sleepRandon(10);
                         Tuple t = null;
                        try {
                            t = chan.remove();
                        } catch (ChannelTimeoutException ex) {
                            Logger.getLogger(BasicChannelTest.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        if (t != null) {
                            removed++;
                            latch.countDown();

                        }


                    }



                }
            };
            remover.start();
        }

        try {
            latch.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(BasicChannelTest.class.getName()).log(Level.SEVERE, null, ex);
        }


       
        //just Status  in space
        assertEquals(1, space.size());

        //remove Status
        final Status status = (Status) space.get(Status.createMatchTemplateForNotFull("BasicChannelTest"));
        //empty space
        assertEquals(0, space.size());


        assertEquals((long) HOW_MANY_TO_INSERT * THREAD_COUNT + 1, (long) status.getHead());
        assertEquals((long) HOW_MANY_TO_INSERT * THREAD_COUNT + 1, (long) status.getTail());


        System.out.println("  == DONE === ");

    }

    public void spaceChanged(final SpaceChange evt) {
        System.out.println(evt);
        this.spaceEvent = evt;
    }

    private void sleepRandon(long weight) {



        try {
            Thread.sleep(1 + (long) (weight * Math.random()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }
}
