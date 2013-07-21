/*
 * Written by Dawid Kurzyniec, on the basis of public specifications and
 * public domain sources from JSR 166, and released to the public domain,
 * as explained at http://creativecommons.org/licenses/publicdomain.
 */

package clime.messadmin.utils.backport.java.util;

import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.AbstractQueue;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

/**
 * Augments {@link java.util.Collections} with methods added in Java 6.0
 * and higher. Adds support for dynamically typesafe collection wrappers,
 * and several utility methods.
 *
 * @see java.util.Collections
 */
public class Collections {

    private Collections() {}

    // other utils

    /**
     * Returns a set backed by the specified map.  The resulting set displays
     * the same ordering, concurrency, and performance characteristics as the
     * backing map.  In essence, this factory method provides a {@link Set}
     * implementation corresponding to any {@link Map} implementation.  There
     * is no need to use this method on a {@link Map} implementation that
     * already has a corresponding {@link Set} implementation (such as {@link
     * HashMap} or {@link TreeMap}).
     *
     * <p>Each method invocation on the set returned by this method results in
     * exactly one method invocation on the backing map or its <tt>keySet</tt>
     * view, with one exception.  The <tt>addAll</tt> method is implemented
     * as a sequence of <tt>put</tt> invocations on the backing map.
     *
     * <p>The specified map must be empty at the time this method is invoked,
     * and should not be accessed directly after this method returns.  These
     * conditions are ensured if the map is created empty, passed directly
     * to this method, and no reference to the map is retained, as illustrated
     * in the following code fragment:
     * <pre>
     *    Set&lt;Object&gt; weakHashSet = Collections.newSetFromMap(
     *        new WeakHashMap&lt;Object, Boolean&gt;());
     * </pre>
     *
     * @param map the backing map
     * @return the set backed by the map
     * @throws IllegalArgumentException if <tt>map</tt> is not empty
     * @since 1.6
     */
    public static <E> Set<E> newSetFromMap(Map<E, Boolean> map) {
        return new SetFromMap(map);
    }

    /**
     * Returns a view of a {@link Deque} as a Last-in-first-out (Lifo)
     * {@link Queue}. Method <tt>add</tt> is mapped to <tt>push</tt>,
     * <tt>remove</tt> is mapped to <tt>pop</tt> and so on. This
     * view can be useful when you would like to use a method
     * requiring a <tt>Queue</tt> but you need Lifo ordering.
     *
     * <p>Each method invocation on the queue returned by this method
     * results in exactly one method invocation on the backing deque, with
     * one exception.  The {@link Queue#addAll addAll} method is
     * implemented as a sequence of {@link Deque#addFirst addFirst}
     * invocations on the backing deque.
     *
     * @param deque the deque
     * @return the queue
     * @since  1.6
     */
    public static <T> Queue<T> asLifoQueue(Deque<T> deque) {
        return new AsLifoQueue(deque);
    }

    private static class SetFromMap<E> extends AbstractSet<E> implements Serializable {

        private final static Boolean PRESENT = Boolean.TRUE;

        final Map<E, Boolean> map;
        transient Set<E> keySet;

        SetFromMap(Map<E, Boolean> map) {
            this.map = map;
            this.keySet = map.keySet();
        }

        public int hashCode()               { return keySet.hashCode(); }
        public int size()                   { return map.size(); }
        public void clear()                 { map.clear(); }
        public boolean isEmpty()            { return map.isEmpty(); }
        public boolean add(E o)             { return map.put(o, PRESENT) == null; }
        public boolean contains(Object o)   { return map.containsKey(o); }
        public boolean equals(Object o)     { return o == this || keySet.equals(o); }
        public boolean remove(Object o)     { return map.remove(o) == PRESENT; }

        public boolean removeAll(Collection<?> c) { return keySet.removeAll(c); }
        public boolean retainAll(Collection<?> c) { return keySet.retainAll(c); }
        public Iterator<E> iterator()             { return keySet.iterator(); }
        public Object[] toArray()                 { return keySet.toArray(); }
        public <T> T[] toArray(T[] a)             { return keySet.toArray(a); }

        public String toString()          { return keySet.toString(); }
        public boolean containsAll(Collection<?> c) {return keySet.containsAll(c);}
        // addAll is the only inherited implementation

        public boolean addAll(Collection<? extends E> c) {
            boolean modified = false;
            for (E e : c) {
                modified |= (map.put(e, PRESENT) == null);
            }
            return modified;
        }

        private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException
        {
            in.defaultReadObject();
            keySet = map.keySet();
        }
    }

    private static class AsLifoQueue<E> extends AbstractQueue<E>
        implements Queue<E>, Serializable
    {
        final Deque<E> deque;
        AsLifoQueue(Deque<E> deque)         { this.deque = deque; }
        public boolean add(E e)             { deque.addFirst(e); return true; }
        public boolean offer(E e)           { return deque.offerFirst(e); }
        public E remove()                   { return deque.removeFirst(); }
        public E poll()                     { return deque.pollFirst(); }
        public E element()                  { return deque.getFirst(); }
        public E peek()                     { return deque.peekFirst(); }
        public int size()                   { return deque.size(); }
        public void clear()                 { deque.clear(); }
        public boolean isEmpty()            { return deque.isEmpty(); }
        public Object[] toArray()           { return deque.toArray(); }
        public <T> T[] toArray(T[] a)       { return deque.toArray(a); }
        public boolean contains(Object o)   { return deque.contains(o); }
        public boolean remove(Object o)     { return deque.remove(o); }
        public Iterator<E> iterator()       { return deque.iterator(); }
        public String toString()            { return deque.toString(); }
	    public boolean containsAll(Collection<?> c) { return deque.containsAll(c); }
        public boolean removeAll(Collection<?> c)   { return deque.removeAll(c); }
        public boolean retainAll(Collection<?> c)   { return deque.retainAll(c); }
        // We use inherited addAll; forwarding addAll would be wrong
    }

}
