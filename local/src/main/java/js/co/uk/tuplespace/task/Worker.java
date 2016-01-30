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

package js.co.uk.tuplespace.task;

import js.co.uk.tuplespace.space.Space;
import js.co.uk.tuplespace.store.TransactionException;
import js.co.uk.tuplespace.store.TransactionID;
import js.co.uk.tuplespace.tuple.Tuple;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * 
 * @author mike
 */
public class Worker {

    private static final long TEN_MINUTES = 10 * 60 * 1000L;

    private final PoisonPill poisonPillTemplate = new PoisonPill();

    private final Tuple template = new TaskTuple();

    private final Space space;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private Future<?> task;
    private volatile boolean keepRunning = true;

    private final Thread workThread;

    /**
     * 
     * @param simulateFailures
     */
    public void setSimulateFailures(boolean simulateFailures) {
        this.simulateFailures = simulateFailures;
    }

    private boolean simulateFailures = false;

    /**
     * 
     * @param space
     */
    public Worker(final Space space) {
        this.space = space;
        final Thread pillThread = makePillThread();
        executor.submit(pillThread);
        workThread = makeWorkThread();

    }

    private Thread makePillThread() {
        final Thread pill = new Thread() {
            public void run() {
                space.read(poisonPillTemplate);
                stopWork();
            }
        };

        return pill;
    }

    /**
     * 
     */
    public void startWork() {

        task = executor.submit(workThread);
    }

    /**
     * 
     */
    public void stopWork() {

        keepRunning = false;
        task.cancel(true);

    }


    private Thread makeWorkThread() {
        final Thread thread = new Thread() {
            public void run() {
                while (keepRunning) {
                    try {

                        //make a transaction  to get the task - maybe parameterise the txn timeout?

                        final TransactionID txn = space.beginTxn(TEN_MINUTES);
                        final TaskTuple task = (TaskTuple) space.get(template,2000, txn);
                        if(task != null) {
                            final ResultTuple result = task.execute(space);
                            space.put(result, txn);

                            if (simulateFailures && Math.random() > 0.75) {

                                space.abortTxn(txn);
                            } else {

                                space.commitTxn(txn);

                            }

                        }
                        else{
                            space.abortTxn(txn);
                        }


                    } catch (TransactionException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        thread.setDaemon(true);
        return thread;
    }


}