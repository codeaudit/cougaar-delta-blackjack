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

package org.cougaar.delta.util.variant;

import org.cougaar.delta.util.html.EnumeratedArray;
import java.util.*;

/**
 *  The OrderedTraversal class is a form of Enumeration that takes in a list
 *  of Objects in the form of an array, a Vector, or another Enumeration and
 *  iterates through those objects as if they had been sorted into a particular
 *  order.  This effect is achieved by constructing a binary heap (to impose a
 *  partial ordering on the elements) and repeatedly popping off the top member
 *  of the heap.
 *  <br><br>
 *  The ordering of the elements is based on a set of {@link Variant} keys calculated
 *  from the elements themselves by application of a {@link VariantMap}, which must be
 *  supplied by the caller at the time of construction.  Elements are then
 *  visited in either increasing or decreasing order (also specified at
 *  creation) in terms of the type of {@link Variant} being produced by the map.  If
 *  two or more elements have keys that are indistinguishable, the ordering
 *  amongst themselves is preserved.
 */
public class OrderedTraversal implements Enumeration {
  // the root node for the binary heap
  private Node root = null;

  // the mapping for that determines the order of the elements
  private VariantMap keyMap = null;

  // a flag indicating (if true) that elements are to be traversed in
  // descending order
  private boolean descending = false;

  // inner class representing the nodes in the binary heap
  private class Node {
    // the Variant key belonging to the data currently residing in this Node
    public Variant key = null;

    // the data currently residing in this Node
    public Object data = null;

    // the left-hand child of this Node
    public Node left = null;

    // the right-hand child of this Node
    public Node right = null;

    // create a Node with given children but no data
    public Node (Node n1, Node n2) {
      left = n1;
      right = n2;
    }

    // create a Node with the given data and key values but no children
    public Node (Object o, Variant k) {
      data = o;
      key = k;
    }

    // Promote data and key pairs up through the heap structure.  The return
    // value indicates whether the promoted branch is now vacant (and hence may
    // be destroyed).
    public boolean promote () {
      if (left == null && right == null)
        return true;
      if (left == null)
        promoteRight();
      else if (right == null)
        promoteLeft();
      else {
        int comp = compareVariants(left.key, right.key);
        if (comp == 0 || ((comp < 0) ^ descending))
          promoteLeft();
        else
          promoteRight();
      }
      return false;
    }

    //  Compare one {@link Variant} with another, allowing for the possibility of null
    //  values.  Since whitespace always precedes a letter, and since null and
    //  whitespace look the same in the GUI, it's more intuitive to have null come
    //  first (since nothing is less than something), followed by whitespace, followed
    //  by letters in alphabetical order.  This way, all the "blank" values are
    //  always kept together.
    private int compareVariants (Variant left, Variant right) {
      if (left == null) {
        if (right == null)
          return 0;
        else
          return -1;
      }
      else if (right == null)
        return 1;
      else {
        return left.compareTo(right);
      }
    }

    // promote the left branch
    private void promoteLeft () {
      data = left.data;
      key = left.key;
      if (left.promote())
        left = null;
    }

    // promote the right branch
    private void promoteRight () {
      data = right.data;
      key = right.key;
      if (right.promote())
        right = null;
    }
  } //end of inner class Node

  // form a new branch of no more than the given maximum depth from elements
  // of the provided Enumeration
  private Node makeNewBranch (Enumeration objs, int depth) {
    if (!objs.hasMoreElements())
      return null;
    Object obj = null;
    if (depth == 1) {
      obj = objs.nextElement();
      return new Node(obj, keyMap.map(obj));
    }
    int d = depth - 1;
    return join(makeNewBranch(objs, d), makeNewBranch(objs, d));
  }

  // join two branches together in accordance with the defining heap structure
  private Node join (Node left, Node right) {
    if (left == null)
      return right;
    else if (right == null)
      return left;
    else {
      Node n = new Node(left, right);
      n.promote();
      return n;
    }
  }

  /**
   *  An heuristic name for the value indicating ascending order traversal
   */
  public static final int ASCENDING = 1;

  /**
   *  An heuristic name for the value indicating descending order traversal
   */
  public static final int DESCENDING = -1;

  /**
   *  A standard {@link VariantMap} that can be used for sorting strings.
   */
  public static final VariantMap STRING = new VariantMap() {
    public Variant map (Object w) {
      return new VariantText(w.toString());
    }
  };

  /**
   *  A standard {@link VariantMap} for sorting strings in a case insensitive manner.
   */
  public static final VariantMap STRING_IGNORE_CASE = new VariantMap() {
    public Variant map (Object w) {
      return new VariantText(w.toString().toUpperCase());
    }
  };

  /**
   *  Construct an {@link OrderedTraversal} that will visit the elements of the given
   *  array.
   *  @param array the array of objects to be visited
   *  @param m the {@link VariantMap} to give ordering to the elements
   *  @param d direction of the traversal (ASCENDING or DESCENDING)
   */
  public OrderedTraversal (Object[] array, VariantMap m, int d) {
    this(new EnumeratedArray(array), m, d);
  }

  /**
   *  Construct an {@link OrderedTraversal} that will visit the elements of the given
   *  Vector.
   *  @param array the array of objects to be visited
   *  @param m the {@link VariantMap} to give ordering to the elements
   *  @param d direction of the traversal (ASCENDING or DESCENDING)
   */
  public OrderedTraversal (Vector vec, VariantMap m, int d) {
    this(vec.elements(), m, d);
  }

  /**
   *  Construct an {@link OrderedTraversal} that will visit the elements of the given
   *  Enumeration.
   *  @param array the array of objects to be visited
   *  @param m the {@link VariantMap} to give ordering to the elements
   *  @param d direction of the traversal (ASCENDING or DESCENDING)
   */
  public OrderedTraversal (Enumeration objs, VariantMap k, int d) {
    keyMap = k;
    descending = d < 0;
    for (int depth = 1; objs.hasMoreElements(); depth++) {
      root = join(root, makeNewBranch(objs, depth));
    }
  }

  /**
   *  Construct an {@link OrderedTraversal} that will visit the elements of the given
   *  array.  By default, the order of the traversal is assigned to be ASCENDING.
   *  @param array the array of objects to be visited
   *  @param m the {@link VariantMap} to give ordering to the elements
   */
  public OrderedTraversal (Object[] array, VariantMap m) {
    this(array, m, ASCENDING);
  }

  /**
   *  Construct an {@link OrderedTraversal} that will visit the elements of the given
   *  Vector.  By default, the order of the traversal is assigned to be
   *  ASCENDING.
   *  @param array the array of objects to be visited
   *  @param m the {@link VariantMap} to give ordering to the elements
   */
  public OrderedTraversal (Vector vec, VariantMap m) {
    this(vec, m, ASCENDING);
  }

  /**
   *  Construct an {@link OrderedTraversal} that will visit the elements of the given
   *  Enumeration.  By default, the order of the traversal is assigned to be
   *  ASCENDING.
   *  @param array the array of objects to be visited
   *  @param m the {@link VariantMap} to give ordering to the elements
   */
  public OrderedTraversal (Enumeration objs, VariantMap k) {
    this(objs, k, ASCENDING);
  }

  public Object nextElement () {
    if (root == null)
      return null;
    Object o = root.data;
    if (root.promote())
      root = null;
    return o;
  }

  /**
   *  Discover whether there are more elements to visit in this Enumeration
   *  @return true if the heap has more nodes to visit
   */
  public boolean hasMoreElements () {
    return root != null;
  }

}
