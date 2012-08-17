/*
 * This program is free software. It comes without any warranty, to
 * the extent permitted by applicable law. You can redistribute it
 * and/or modify as you wish.
 * Copyright (c) 2010 Uwe B. Meding <uwe@uwemeding.com>
 */
public class Mutex {

    /**
     * Thread that owns this lock status
     */
    private Thread owner;

    /**
     * Constructor
     */
    public Mutex() {
        this.owner = null;
    }

    /**
     * Acquire this mutex, blocking indefinitely
     * @return true once the mutex has been acquired
     */
    public boolean acquire() throws InterruptedException {

        if (Thread.interrupted()) {
            // we got interrupted
            throw new InterruptedException();
        }
        synchronized (this) {
            if (owner == Thread.currentThread()) {
                // thread already owns this mutex
                return true;
            }
            try {
                while (owner != null) {
                    this.wait();
                }
                // acquired mutex
                owner = Thread.currentThread();
            } catch (InterruptedException ex) {
                // we were interrupeted while acquiring mutex
                notify();
                throw ex;
            }
            return true;
        }
    }

    /**
     * Attempt to acquire this mutex by waiting up to the given millisec
     * @return true if granted, false otherwise
     */
    public boolean attempt(long msecs) throws InterruptedException {

        if (Thread.interrupted()) {
            // we got interrupted
            throw new InterruptedException();
        }
        synchronized (this) {

            if (owner == null) {

                // acquiring mutex
                owner = Thread.currentThread();
                return true;

            } else if (owner == Thread.currentThread()) {

                // we already own the mutex
                return true;

            } else if (msecs <= 0) {

                // we did not get the mutex, the other thread did
                // not release it
                return false;

            } else {

                // waiting for the mutex to become available
                long waitTime = msecs;
                long start = System.currentTimeMillis();
                try {
                    while (true) {
                        this.wait(waitTime);
                        if (owner == null) {

                            owner = Thread.currentThread();
                            // we acquired it after waiting
                            return true;

                        } else {

                            waitTime = msecs - (System.currentTimeMillis() - start);
                            if (waitTime <= 0) {
                                // failed to get mutex in time
                                return false;
                            }
                        }
                    }
                } catch (InterruptedException ex) {
                    // we got interrupted while acquiring the mutex
                    notify();
                    throw ex;
                }
            }
        }
    }

    /**
     * Release this mutex
     * @throw IllegalStateException if calling thread does not own it.
     */
    public synchronized void release() {
        Thread thread = Thread.currentThread();
        if (owner == null) {
            // trying to release unowned mutex
            return;
        }
        if (thread == owner) {
            // releasing mutex
            owner = null;
            this.notify();
            return;
        }
    }
}
