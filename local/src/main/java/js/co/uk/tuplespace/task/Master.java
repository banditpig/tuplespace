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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Base class for Masters in a  Master-Worker pattern.  Uses watermarking to keep the number of tuples
 * under control.
 */
public abstract class Master {

    private final ResultTuple template = new ResultTuple();

    private Future<?> generatorFuture;
    private Future<?> collectorFuture;

    private final ExecutorService executor = Executors.newCachedThreadPool();
    /**
     * 
     */
    protected final Space space;

    //water marking
    private AtomicInteger maxAllowedLevel = new AtomicInteger(0);
    private int currentLevel = 0;



    /**
     * Subclass to generate its own application specific tasks.
     */
    protected abstract void generateTasks();

    /**
     * Subclass to collect its own results.
     */
    protected abstract void collectResults();

    /**
     * Base constructor
     *
     * @param space the space in which to work
     */
    public Master(final Space space) {
        this(space, Integer.MAX_VALUE);

    }

    /**
     * 
     * @param space
     * @param maxAllowedLevel
     */
    public Master(final Space space, final int maxAllowedLevel) {
        this.space = space;
        this.maxAllowedLevel = new AtomicInteger(maxAllowedLevel);

    }
    
    /**
     * 
     * @param maxAllowedLevel
     */
    public final void setMaxAllowedLevel(final int maxAllowedLevel){
        this.maxAllowedLevel.set(maxAllowedLevel);
    }

    /**
     * This executes the  generateTasks() method and the collectResults() method
     * each in a separate thread.
     *
     */
    public final void startGenerationAndCollection() {

        startGeneratingTasks();
        startCollectingResults();
    }


    /**
     * 
     * @param mayInterruptIfRunning
     * @return
     */
    public final boolean stopCollectingResults(boolean mayInterruptIfRunning) {
        return collectorFuture.cancel(mayInterruptIfRunning);
    }


    /**
     * 
     * @param mayInterruptIfRunning
     * @return
     */
    public final boolean stopGeneratingTasks(boolean mayInterruptIfRunning) {
        return generatorFuture.cancel(mayInterruptIfRunning);
    }


    /**
     * Utility method to put a TaskTuple into the space. Also handles the watermarking.
     *
     * @param taskTuple a TaskTuple
     */
    public final void putTaskTuple(final TaskTuple taskTuple) {
        try {
            waitForLowWaterMark();
            space.put(taskTuple, null);
            changeCurrentLevel(1);
        } catch (final TransactionException e) {
            e.printStackTrace();
        }
    }


    /**
     * Utility method to wait  for and remove a ResultTuple from the space. Also handles the watermarking.
     *
     * @return a ResultTuple
     */
    public final ResultTuple getResultTuple() {

        try {
            final ResultTuple result = (ResultTuple) space.get(template, null);
            if (result != null) {
                changeCurrentLevel(-1);
            }
            return result;
        } catch (TransactionException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     */
    private void startCollectingResults() {
        collectorFuture = executor.submit(new Runnable() {
            public void run() {
                collectResults();
            }
        });
    }

    /**
     *
     */
    private void startGeneratingTasks() {
        generatorFuture = executor.submit(new Runnable() {
            public void run() {
                generateTasks();
            }
        });
    }

    private synchronized void waitForLowWaterMark() {

        //rewrite this using condition
        while (currentLevel > maxAllowedLevel.intValue()) {

            try {
                wait();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            /*
            ...and carry on
             */
        }
    }

    private synchronized void changeCurrentLevel(final int amount) {

        currentLevel += amount;
        notifyAll();
    }


}