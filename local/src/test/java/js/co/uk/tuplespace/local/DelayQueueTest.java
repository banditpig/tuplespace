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

package js.co.uk.tuplespace.local;

import js.co.uk.tuplespace.store.TimeoutQueue;
import js.co.uk.tuplespace.tuple.SimpleTuple;
import js.co.uk.tuplespace.tuple.Tuple;
import static junit.framework.Assert.assertTrue;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DelayQueueTest {

    TimeoutQueue<Tuple> q;
    private int expireCount = 0;
    private ArrayList<Tuple> expiredList;

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


    @Before
    public void setUp() throws Exception {
        final BlockingQueue<Tuple> bq = new ArrayBlockingQueue<Tuple>(10);
        q = new TimeoutQueue<Tuple>(bq);
        expireCount = 0;
        expiredList = new ArrayList<Tuple>();
        Thread rdr = new Thread() {
            public void run() {
                while (true) {
                    try {

                        final Tuple expired = bq.take();
                        expireCount++;
                        expiredList.add(expired);
                        System.out.println("removedv " + (SimpleTuple) expired + "  " + expireCount);


                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        };

        rdr.start();

    }

    public void bigTestConcurrent() {
        for (int i = 0; i < 100; i++) {
            testConcurrent();
            System.out.println("================ " + i);
        }
    }

    @Test
    public void testConcurrent() {


        int last = 3000;
        final CountDownLatch latch = new CountDownLatch(last);
        for (int i = 0; i < last; i++) {
            final int id = i;
            Thread t = new Thread() {

                public void run() {

                    Tuple t = new SimpleTuple(id);
                    try {
                        long sleep = (long) (Math.random() * 100 + id);
                        long expire = (long) (Math.random() * 100 + id);
                        Thread.sleep(sleep);
                        q.add(t, expire, TimeUnit.MILLISECONDS);
                        // System.out.println(sleep + " " + expire);
                        latch.countDown();
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
        sleep(12);
        System.out.println("q size " + q.size());
        org.junit.Assert.assertEquals("expire is " + expireCount, expireCount, last);
        expireCount = 0;
        last = 0;


    }

    @Test
    public void testOrdering() {
        //234
        for (int i = 0; i < 500; i++) {
            SimpleTuple t = new SimpleTuple(i);
            q.add(t, (i + 1), TimeUnit.MILLISECONDS);
        }
        sleep(1);
        assertTrue(expiredList.size() == 500);

        System.out.println(expiredList);
    }

    @Test
    public void testSimpleTimeout() {
        expireCount = 0;
        for (int i = 1; i < 500; i++)
            q.add(new SimpleTuple("Fred " + i), 10 * i, TimeUnit.MILLISECONDS);
        sleep(6);
        assertTrue(q.size() == 0);
        assertTrue(expireCount == 499);
    }

 
    @Test
    public void testAddManyTheSame() {
        //234
        final SimpleTuple simpleTuple = new SimpleTuple("Fred");
        for (int i = 0; i < 500; i++) {

            q.add(simpleTuple, i + 2000, TimeUnit.MILLISECONDS);
        }

        assertTrue(q.size() == 500);
        sleep(3);
        assertTrue(q.size() == 0);

        assertTrue(expiredList.size() == 500);

        //concurrently
        expireCount = 0;
        expiredList.clear();

        int last = 3000;
        final CountDownLatch latch = new CountDownLatch(last);
        for (int i = 0; i < last; i++) {
            final int id = i;
            Thread t = new Thread() {

                public void run() {


                    try {
                        long sleep = (long) (Math.random() * 100 + id);
                        long expire = (long) (Math.random() * 100 + id);
                        Thread.sleep(sleep);
                        q.add(simpleTuple, expire, TimeUnit.MILLISECONDS);
                        // System.out.println(sleep + " " + expire);
                        latch.countDown();
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
        sleep(12);
        System.out.println("q size " + q.size());
        org.junit.Assert.assertEquals("expire is " + expireCount, expireCount, last);
        expireCount = 0;


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

