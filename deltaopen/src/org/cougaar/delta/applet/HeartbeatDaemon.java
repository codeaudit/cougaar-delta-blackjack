/*
  * <copyright>
  *  Copyright 2002 BBNT Solutions, LLC
  *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA)
  *  and the Defense Logistics Agency (DLA).
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the Cougaar Open Source License as published by
  *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
  *
  *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
  *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
  *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
  *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
  *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
  *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
  *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
  *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
  *  PERFORMANCE OF THE COUGAAR SOFTWARE.
  * </copyright>
  */
package org.cougaar.delta.applet;

public abstract class HeartbeatDaemon extends Thread {
  // the default beatPeriod is 5 minutes
  private long beatPeriod = 300000;

  // keep track of whether we've been killed
  private boolean deadButDontKnowItYet = false;

  // have a lock to keep synchronized code from misbehaving
  private Object lock = new Object();

  /**
   *  Have a heart attack!  Effectively, this stops the thread.
   */
  public void heartAttack () {
    synchronized (lock) {
      deadButDontKnowItYet = true;
      interrupt();
    }
  }

  /**
   *  Construct this HeartbeatDaemon with the default beat frequency
   */
  public HeartbeatDaemon () {
    super();
    config();
  }

  /**
   *  Construct this HeartbeatDaemon with a specified period between beats
   *  @param period the period between consecutive calls to "pulse"
   */
  public HeartbeatDaemon (long period) {
    super();
    beatPeriod = period;
    config();
  }

  // Configure the thread for this purpose.
  // The priority is set to one greater than the caller so that when it awakens,
  //   it can muscle in on whatever was running while it slept.
  private void config () {
    setDaemon(true);
    setPriority(getPriority() + 1);
    start();
  }

  /**
   *  Do the work this thread is designed to do.  In particular, sleep most of
   *  the time, but wake periodically and call the pulse() method
   */
  public void run () {
    synchronized (lock) {
      while (!deadButDontKnowItYet) {
        try {
          lock.wait(beatPeriod);
          System.out.println("HeartbeatDaemon::run:  Beep!");
          deadButDontKnowItYet = !pulse();
        }
        catch (Exception what) { }
      }
    }
  }

  /**
   *  Execute a single heartbeat.  A subclass should override this method to
   *  give particular functionality to instances of HeartbeatDaemon.  The
   *  return value determines whether this is the last beat.
   *
   *  @return true if the heartbeat should continue; false otherwise
   */
  public abstract boolean pulse ();
}
