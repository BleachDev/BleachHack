package org.bleachhack.util.doom.utils;

import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

public enum BinarySearch {;
        /**
         * Binary search supporting search for one type of objects
         * using object of another type, given from any object of one type
         * a function can get an object of another type
         * 
         * @param list of one type of objects
         * @param converter from one type of objects to another
         * @param key a value of another object type
         * @return 
         */
        public static <T, E extends Comparable<? super E>> int find(final List<? extends T> list,
                                                                    final Function<? super T, ? extends E> converter,
                                                                    final E key)
        { return find(list, converter, 0, list.size(), key); }

        /**
         * Binary search supporting search for one type of objects
         * using object of another type, given from any object of one type
         * a function can get an object of another type
         * 
         * @param array of one type of objects
         * @param converter from one type of objects to another
         * @param key a value of another object type
         * @return 
         */
        public static <T, E extends Comparable<? super E>> int find(final T[] array,
                                                                    final Function<? super T, ? extends E> converter,
                                                                    final E key)
        { return find(array, converter, 0, array.length, key); }

        /**
         * Binary search supporting search for one type of objects
         * using object of another type, given from any object of one type
         * a function can get an object of another type
         * 
         * @param list of one type of objects
         * @param comparator - a comparator for objects of type E
         * @param converter from one type of objects to another
         * @param key a value of another object type
         * @return 
         */
        public static <T, E> int find(final List<? extends T> list,
                                      final Function<? super T, ? extends E> converter,
                                      final Comparator<? super E> comparator,
                                      final E key)
        { return find(list, converter, comparator, 0, list.size(), key); }

        /**
         * Binary search supporting search for one type of objects
         * using object of another type, given from any object of one type
         * a function can get an object of another type
         * 
         * @param array of one type of objects
         * @param comparator - a comparator for objects of type E
         * @param converter from one type of objects to another
         * @param key a value of another object type
         * @return 
         */
        public static <T, E> int find(final T[] array,
                                      final Function<? super T, ? extends E> converter,
                                      final Comparator<? super E> comparator,
                                      final E key)
        { return find(array, converter, comparator, 0, array.length, key); }

        /**
         * Binary search supporting search for one type of objects
         * using primitive integer, given from any object
         * of one type a function can get a primitive integer
         * 
         * @param list of one type of objects
         * @param converter from one type of objects to a primitive integer
         * @param a primitive integer key value
         * @return 
         */
        public static <T> int findByInt(final List<? extends T> list,
                                        final ToIntFunction<? super T> converter,
                                        final int key)
        { return findByInt(list, converter, 0, list.size(), key); }
        
        /**
         * Binary search supporting search for one type of objects
         * using primitive integer, given from any object
         * of one type a function can get a primitive integer
         * 
         * @param array of one type of objects
         * @param converter from one type of objects to a primitive integer
         * @param a primitive integer key value
         * @return 
         */
        public static <T> int findByInt(final T[] array,
                                        final ToIntFunction<? super T> converter,
                                        final int key)
        { return findByInt(array, converter, 0, array.length, key); }
        
        /**
         * Binary search supporting search for one type of objects
         * using primitive integer, given from any object
         * of one type a function can get a primitive integer
         * 
         * @param list of one type of objects
         * @param converter from one type of objects to a primitive integer
         * @param comparator - a comparator for primitive integer values
         * @param a primitive integer key value
         * @return 
         */
        public static <T> int findByInt(final List<? extends T> list,
                                        final ToIntFunction<? super T> converter,
                                        final IntBinaryOperator comparator,
                                        final int key)
        { return findByInt(list, converter, comparator, 0, list.size(), key); }
        
        /**
         * Binary search supporting search for one type of objects
         * using primitive integer, given from any object
         * of one type a function can get a primitive integer
         * 
         * @param array of one type of objects
         * @param converter from one type of objects to a primitive integer
         * @param comparator - a comparator for primitive integer values
         * @param a primitive integer key value
         * @return 
         */
        public static <T> int findByInt(final T[] array,
                                        final ToIntFunction<? super T> converter,
                                        final IntBinaryOperator comparator,
                                        final int key)
        { return findByInt(array, converter, comparator, 0, array.length, key); }
        
        /**
         * Binary search supporting search for one type of objects
         * using primitive long, given from any object
         * of one type a function can get a primitive long
         * 
         * @param list of one type of objects
         * @param converter from one type of objects to a primitive long
         * @param a primitive long key value
         * @return 
         */
        public static <T> int findByLong(final List<? extends T> list,
                                         final ToLongFunction<? super T> converter,
                                         final long key)
        { return findByLong(list, converter, 0, list.size(), key); }
        
        /**
         * Binary search supporting search for one type of objects
         * using primitive long, given from any object
         * of one type a function can get a primitive long
         * 
         * @param array of one type of objects
         * @param converter from one type of objects to a primitive long
         * @param a primitive long key value
         * @return 
         */
        public static <T> int findByLong(final T[] array,
                                         final ToLongFunction<? super T> converter,
                                         final long key)
        { return findByLong(array, converter, 0, array.length, key); }
        
        /**
         * Binary search supporting search for one type of objects
         * using primitive long, given from any object
         * of one type a function can get a primitive long
         * 
         * @param list of one type of objects
         * @param converter from one type of objects to a primitive long
         * @param comparator - a comparator for primitive long values
         * @param a primitive long key value
         * @return 
         */
        public static <T> int findByLong(final List<? extends T> list,
                                         final ToLongFunction<? super T> converter,
                                         final LongComparator comparator,
                                         final long key)
        { return findByLong(list, converter, comparator, 0, list.size(), key); }
        
        /**
         * Binary search supporting search for one type of objects
         * using primitive long, given from any object
         * of one type a function can get a primitive long
         * 
         * @param array of one type of objects
         * @param converter from one type of objects to a primitive long
         * @param comparator - a comparator for primitive long values
         * @param a primitive long key value
         * @return 
         */
        public static <T> int findByLong(final T[] array,
                                         final ToLongFunction<? super T> converter,
                                         final LongComparator comparator,
                                         final long key)
        { return findByLong(array, converter, comparator, 0, array.length, key); }
        
        /**
         * Binary search supporting search for one type of objects
         * using primitive double, given from any object
         * of one type a function can get a primitive double
         * 
         * @param list of one type of objects
         * @param converter from one type of objects to a primitive double
         * @param a primitive double key value
         * @return 
         */
        public static <T> int findByDouble(final List<? extends T> list,
                                           final ToDoubleFunction<? super T> converter,
                                           final double key)
        { return findByDouble(list, converter, 0, list.size(), key); }

        /**
         * Binary search supporting search for one type of objects
         * using primitive double, given from any object
         * of one type a function can get a primitive double
         * 
         * @param array of one type of objects
         * @param converter from one type of objects to a primitive double
         * @param a primitive double key value
         * @return 
         */
        public static <T> int findByDouble(final T[] array,
                                           final ToDoubleFunction<? super T> converter,
                                           final double key)
        { return findByDouble(array, converter, 0, array.length, key); }

        /**
         * Binary search supporting search for one type of objects
         * using primitive double, given from any object
         * of one type a function can get a primitive double
         * 
         * @param list of one type of objects
         * @param converter from one type of objects to a primitive double
         * @param comparator - a comparator for primitive double values
         * @param a primitive double key value
         * @return 
         */
        public static <T> int findByDouble(final List<? extends T> list,
                                           final ToDoubleFunction<? super T> converter,
                                           final DoubleComparator comparator,
                                           final double key)
        { return findByDouble(list, converter, comparator, 0, list.size(), key); }

        /**
         * Binary search supporting search for one type of objects
         * using primitive double, given from any object
         * of one type a function can get a primitive double
         * 
         * @param array of one type of objects
         * @param converter from one type of objects to a primitive double
         * @param comparator - a comparator for primitive double values
         * @param a primitive double key value
         * @return 
         */
        public static <T> int findByDouble(final T[] array,
                                           final ToDoubleFunction<? super T> converter,
                                           final DoubleComparator comparator,
                                           final double key)
        { return findByDouble(array, converter, comparator, 0, array.length, key); }

        /**
         * Binary search supporting search for one type of objects
         * using object of another type, given from any object of one type
         * a function can get an object of another type
         * 
         * @param list of one type of objects
         * @param converter from one type of objects to another
         * @param from - an index (inclusive) from which to start search
         * @param to - an index (exclusive) from which to start search
         * @param key a value of another object type
         * @return 
         */
        public static <T, E extends Comparable<? super E>> int find(final List<? extends T> list,
                                                                    final Function<? super T, ? extends E> converter,
                                                                    final int from, final int to, final E key)
        {
                final IntFunction<? extends T> getter = listGetter(list);
                return findByIndex(i -> converter.apply(getter.apply(i)).compareTo(key), from, to);
        }
            
        /**
         * Binary search supporting search for one type of objects
         * using object of another type, given from any object of one type
         * a function can get an object of another type
         * 
         * @param array of one type of objects
         * @param converter from one type of objects to another
         * @param from - an index (inclusive) from which to start search
         * @param to - an index (exclusive) from which to start search
         * @param key a value of another object type
         * @return 
         */
        public static <T, E extends Comparable<? super E>> int find(final T[] array,
                                                                    final Function<? super T, ? extends E> converter,
                                                                    final int from, final int to, final E key)
        {
                rangeCheck(array.length, from, to);
                return findByIndex(i -> converter.apply(array[i]).compareTo(key), from, to);
        }
        /**
         * Binary search supporting search for one type of objects
         * using object of another type, given from any object of one type
         * a function can get an object of another type
         * 
         * @param list of one type of objects
         * @param converter from one type of objects to another
         * @param comparator - a comparator for objects of type E
         * @param from - an index (inclusive) from which to start search
         * @param to - an index (exclusive) from which to start search
         * @param key a value of another object type
         * @return
         */
        public static <T, E> int find(final List<? extends T> list,
                                      final Function<? super T, ? extends E> converter,
                                      final Comparator<? super E> comparator,
                                      final int from, final int to, final E key)
        {
                final IntFunction<? extends T> getter = listGetter(list);
                return findByIndex(i -> comparator.compare(converter.apply(getter.apply(i)), key), from, to);
        }
        
        /**
         * Binary search supporting search for one type of objects
         * using object of another type, given from any object of one type
         * a function can get an object of another type
         * 
         * @param array of one type of objects
         * @param converter from one type of objects to another
         * @param comparator - a comparator for objects of type E
         * @param from - an index (inclusive) from which to start search
         * @param to - an index (exclusive) from which to start search
         * @param key a value of another object type
         * @return
         */
        public static <T, E> int find(final T[] array,
                                      final Function<? super T, ? extends E> converter,
                                      final Comparator<? super E> comparator,
                                      final int from, final int to, final E key)
        {
                rangeCheck(array.length, from, to);
                return findByIndex(i -> comparator.compare(converter.apply(array[i]), key), from, to);
        }
        
        /**
         * Binary search supporting search for one type of objects
         * using primitive integer, given from any object
         * of one type a function can get a primitive integer
         * 
         * @param list of one type of objects
         * @param converter from one type of objects to a primitive integer
         * @param from - an index (inclusive) from which to start search
         * @param to - an index (exclusive) from which to start search
         * @param a primitive integer key value
         * @return 
         */
        public static <T> int findByInt(final List<? extends T> list,
                                        final ToIntFunction<? super T> converter,
                                        final int from, final int to, final int key)
        {
                final IntFunction<? extends T> getter = listGetter(list);
                return findByInt(i -> converter.applyAsInt(getter.apply(i)), from, to, key);
        }
        
        /**
         * Binary search supporting search for one type of objects
         * using primitive integer, given from any object
         * of one type a function can get a primitive integer
         * 
         * @param array of one type of objects
         * @param converter from one type of objects to a primitive integer
         * @param from - an index (inclusive) from which to start search
         * @param to - an index (exclusive) from which to start search
         * @param a primitive integer key value
         * @return 
         */
        public static <T> int findByInt(final T[] array,
                                        final ToIntFunction<? super T> converter,
                                        final int from, final int to, final int key)
        {
                rangeCheck(array.length, from, to);
                return findByInt(i -> converter.applyAsInt(array[i]), from, to, key);
        }
        
        /**
         * Binary search supporting search for one type of objects
         * using primitive integer, given from any object
         * of one type a function can get a primitive integer
         * 
         * @param list of one type of objects
         * @param converter from one type of objects to a primitive integer
         * @param comparator - a comparator for primitive integer values
         * @param from - an index (inclusive) from which to start search
         * @param to - an index (exclusive) from which to start search
         * @param a primitive integer key value
         * @return 
         */
        public static <T> int findByInt(final List<? extends T> list,
                                        final ToIntFunction<? super T> converter,
                                        final IntBinaryOperator comparator,
                                        final int from, final int to, final int key)
        {
                final IntFunction<? extends T> getter = listGetter(list);
                return findByIndex(i -> comparator.applyAsInt(converter.applyAsInt(getter.apply(i)), key), from, to);
        }
        
        /**
         * Binary search supporting search for one type of objects
         * using primitive integer, given from any object
         * of one type a function can get a primitive integer
         * 
         * @param array of one type of objects
         * @param converter from one type of objects to a primitive integer
         * @param comparator - a comparator for primitive integer values
         * @param from - an index (inclusive) from which to start search
         * @param to - an index (exclusive) from which to start search
         * @param a primitive integer key value
         * @return 
         */
        public static <T> int findByInt(final T[] array,
                                        final ToIntFunction<? super T> converter,
                                        final IntBinaryOperator comparator,
                                        final int from, final int to, final int key)
        {
                rangeCheck(array.length, from, to);
                return findByIndex(i -> comparator.applyAsInt(converter.applyAsInt(array[i]), key), from, to);
        }
        
        /**
         * Binary search supporting search for one type of objects
         * using primitive long, given from any object
         * of one type a function can get a primitive long
         * 
         * @param list of one type of objects
         * @param converter from one type of objects to a primitive long
         * @param from - an index (inclusive) from which to start search
         * @param to - an index (exclusive) from which to start search
         * @param a primitive long key value
         * @return
         */
        public static <T> int findByLong(final List<? extends T> list,
                                         final ToLongFunction<? super T> converter,
                                         final int from, final int to, final long key)
        {
                final IntFunction<? extends T> getter = listGetter(list);
                return findByLong(i -> converter.applyAsLong(getter.apply(i)), from, to, key);
        }
        
        /**
         * Binary search supporting search for one type of objects
         * using primitive long, given from any object
         * of one type a function can get a primitive long
         * 
         * @param array of one type of objects
         * @param converter from one type of objects to a primitive long
         * @param from - an index (inclusive) from which to start search
         * @param to - an index (exclusive) from which to start search
         * @param a primitive long key value
         * @return
         */
        public static <T> int findByLong(final T[] array,
                                         final ToLongFunction<? super T> converter,
                                         final int from, final int to, final long key)
        {
                rangeCheck(array.length, from, to);
                return findByLong(i -> converter.applyAsLong(array[i]), from, to, key);
        }
        
        /**
         * Binary search supporting search for one type of objects
         * using primitive long, given from any object
         * of one type a function can get a primitive long
         * 
         * @param list of one type of objects
         * @param converter from one type of objects to a primitive long
         * @param comparator - a comparator for primitive long values
         * @param from - an index (inclusive) from which to start search
         * @param to - an index (exclusive) from which to start search
         * @param a primitive long key value
         * @return
         */
        public static <T> int findByLong(final List<? extends T> list,
                                         final ToLongFunction<? super T> converter,
                                         final LongComparator comparator,
                                         final int from, final int to, final long key)
        {
                final IntFunction<? extends T> getter = listGetter(list);
                return findByIndex(i -> comparator.compareAsLong(converter.applyAsLong(getter.apply(i)), key), from, to);
        }
        
        /**
         * Binary search supporting search for one type of objects
         * using primitive long, given from any object
         * of one type a function can get a primitive long
         * 
         * @param array of one type of objects
         * @param converter from one type of objects to a primitive long
         * @param comparator - a comparator for primitive long values
         * @param from - an index (inclusive) from which to start search
         * @param to - an index (exclusive) from which to start search
         * @param a primitive long key value
         * @return
         */
        public static <T> int findByLong(final T[] array,
                                         final ToLongFunction<? super T> converter,
                                         final LongComparator comparator,
                                         final int from, final int to, final long key)
        {
                rangeCheck(array.length, from, to);
                return findByIndex(i -> comparator.compareAsLong(converter.applyAsLong(array[i]), key), from, to);
        }
        
        /**
         * Binary search supporting search for one type of objects
         * using primitive double, given from any object
         * of one type a function can get a primitive double
         * 
         * @param list of one type of objects
         * @param converter from one type of objects to a primitive double
         * @param from - an index (inclusive) from which to start search
         * @param to - an index (exclusive) from which to start search
         * @param a primitive double key value
         * @return 
         */
        public static <T> int findByDouble(final List<? extends T> list,
                                           final ToDoubleFunction<? super T> converter,
                                           final int from, final int to, final double key)
        {
                final IntFunction<? extends T> getter = listGetter(list);
                return findByDouble(i -> converter.applyAsDouble(getter.apply(i)), from, to, key);
        }
            
        /**
         * Binary search supporting search for one type of objects
         * using primitive double, given from any object
         * of one type a function can get a primitive double
         * 
         * @param array of one type of objects
         * @param converter from one type of objects to a primitive double
         * @param from - an index (inclusive) from which to start search
         * @param to - an index (exclusive) from which to start search
         * @param a primitive double key value
         * @return 
         */
        public static <T> int findByDouble(final T[] array,
                                           final ToDoubleFunction<? super T> converter,
                                           final int from, final int to, final double key)
        {
                rangeCheck(array.length, from, to);
                return findByDouble(i -> converter.applyAsDouble(array[i]), from, to, key);
        }
            
        /**
         * Binary search supporting search for one type of objects
         * using primitive double, given from any object
         * of one type a function can get a primitive double
         * 
         * @param list of one type of objects
         * @param converter from one type of objects to a primitive double
         * @param comparator - a comparator for primitive double values
         * @param from - an index (inclusive) from which to start search
         * @param to - an index (exclusive) from which to start search
         * @param a primitive double key value
         * @return 
         */
        public static <T> int findByDouble(final List<? extends T> list,
                                           final ToDoubleFunction<? super T> converter,
                                           final DoubleComparator comparator,
                                           final int from, final int to, final double key)
        {
                final IntFunction<? extends T> getter = listGetter(list);
                return findByIndex(i -> comparator.compareAsDouble(converter.applyAsDouble(getter.apply(i)), key), from, to);
        }
            
        /**
         * Binary search supporting search for one type of objects
         * using primitive double, given from any object
         * of one type a function can get a primitive double
         * 
         * @param array of one type of objects
         * @param converter from one type of objects to a primitive double
         * @param comparator - a comparator for primitive double values
         * @param from - an index (inclusive) from which to start search
         * @param to - an index (exclusive) from which to start search
         * @param a primitive double key value
         * @return 
         */
        public static <T> int findByDouble(final T[] array,
                                           final ToDoubleFunction<? super T> converter,
                                           final DoubleComparator comparator,
                                           final int from, final int to, final double key)
        {
                rangeCheck(array.length, from, to);
                return findByIndex(i -> comparator.compareAsDouble(converter.applyAsDouble(array[i]), key), from, to);
        }
            
        /**
         * Blind binary search, presuming there is some sorted structure,
         * whose sorting is someway ensured by some key object, using the getter
         * who, given an index in the invisible structure, can produce a key
         * object someway used to sort it.
         * 
         * @param getter - a function accepting indexes, producing a key object used for sort
         * @param from - an index (inclusive) from which to start search
         * @param to - an index (exclusive) from which to start search
         * @param key - a key object
         */
        public static <E extends Comparable<? super E>> int find(final IntFunction<? extends E> getter,
                                                                 final int from, final int to, final E key)
        { return findByIndex(i -> getter.apply(i).compareTo(key), from, to); }

        /**
         * Blind binary search, presuming there is some sorted structure,
         * whose sorting is someway ensured by some key object, using the getter
         * who, given an index in the invisible structure, can produce a key
         * object someway used to sort it.
         * 
         * @param getter - a function accepting indexes, producing a key object used for sort
         * @param comparator - a comparator for objects of type E
         * @param from - an index (inclusive) from which to start search
         * @param to - an index (exclusive) from which to start search
         * @param key - a key object
         */
        public static <E> int find(final IntFunction<? extends E> getter,
                                   final Comparator<? super E> comparator,
                                   final int from, final int to, final E key)
        { return findByIndex(i -> comparator.compare(getter.apply(i), key), from, to); }
            
        /**
         * Blind binary search, presuming there is some sorted structure,
         * whose sorting is someway ensured by primitive integer key,
         * using the getter who, given an index in the invisible structure, can produce
         * the primitive integer key someway used to sort it.
         * 
         * @param getter - a function accepting indexes, producing a primitive integer used for sort
         * @param from - an index (inclusive) from which to start search
         * @param to - an index (exclusive) from which to start search
         * @param key - a primitive integer key
         */
        public static int findByInt(final IntUnaryOperator getter,
                                    final int from, final int to, final int key)
        { return findByInt(getter, Integer::compare, from, to, key); }
        
        /**
         * Blind binary search, presuming there is some sorted structure,
         * whose sorting is someway ensured by primitive integer key,
         * using the getter who, given an index in the invisible structure, can produce
         * the primitive integer key someway used to sort it.
         * 
         * @param getter - a function accepting indexes, producing a primitive integer used for sort
         * @param comparator - a comparator for primitive integers
         * @param from - an index (inclusive) from which to start search
         * @param to - an index (exclusive) from which to start search
         * @param key - a primitive integer key
         */
        public static int findByInt(final IntUnaryOperator getter,
                                    final IntBinaryOperator comparator,
                                    final int from, final int to, final int key)
        { return findByIndex(i -> comparator.applyAsInt(getter.applyAsInt(i), key), from, to); }
        
        /**
         * Blind binary search, presuming there is some sorted structure,
         * whose sorting is someway ensured by primitive long key,
         * using the getter who, given an index in the invisible structure, can produce
         * the primitive long key someway used to sort it.
         * 
         * @param getter - a function accepting indexes, producing a primitive long used for sort
         * @param from - an index (inclusive) from which to start search
         * @param to - an index (exclusive) from which to start search
         * @param key - a primitive long key
         */
        public static int findByLong(final LongGetter getter,
                                     final int from, final int to, final long key)
        { return findByLong(getter, Long::compare, from, to, key); }
        
        /**
         * Blind binary search, presuming there is some sorted structure,
         * whose sorting is someway ensured by primitive long key,
         * using the getter who, given an index in the invisible structure, can produce
         * the primitive long key someway used to sort it.
         * 
         * @param getter - a function accepting indexes, producing a primitive long used for sort
         * @param comparator - a comparator for primitive long values
         * @param from - an index (inclusive) from which to start search
         * @param to - an index (exclusive) from which to start search
         * @param key - a primitive long key
         */
        public static int findByLong(final LongGetter getter,
                                     final LongComparator comparator,
                                     final int from, final int to, final long key)
        { return findByIndex(i -> comparator.compareAsLong(getter.getAsLong(i), key), from, to); }
        
        /**
         * Blind binary search, presuming there is some sorted structure,
         * whose sorting is someway ensured by primitive double key,
         * using the getter who, given an index in the invisible structure, can produce
         * the primitive double key someway used to sort it.
         * 
         * @param getter - a function accepting indexes, producing a primitive double used for sort
         * @param from - an index (inclusive) from which to start search
         * @param to - an index (exclusive) from which to start search
         * @param key - a primitive double key
         */
        public static int findByDouble(final DoubleGetter getter,
                                       final int from, final int to, final double key)
        { return findByDouble(getter, Double::compare, from, to, key); }
        
        /**
         * Blind binary search, presuming there is some sorted structure,
         * whose sorting is someway ensured by primitive double key,
         * using the getter who, given an index in the invisible structure, can produce
         * the primitive double key someway used to sort it.
         * 
         * @param getter - a function accepting indexes, producing a primitive double used for sort
         * @param comparator - a comparator for primitive double values
         * @param from - an index (inclusive) from which to start search
         * @param to - an index (exclusive) from which to start search
         * @param key - a primitive double key
         */
        public static int findByDouble(final DoubleGetter getter,
                                       final DoubleComparator comparator,
                                       final int from, final int to, final double key)
        { return findByIndex(i -> comparator.compareAsDouble(getter.getAsDouble(i), key), from, to); }

        /**
         * Blind binary search applying array elements to matching function until it returns 0
         * @param list of one type of objects
         * @param matcher - a matcher returning comparison result based on single list element
         **/
        public static <T> int findByMatch(final T[] array,
                                          final ToIntFunction<? super T> matcher)
        { return findByMatch(array, matcher, 0, array.length); }
        
        /**
         * Blind binary search applying List elements to matching function until it returns 0
         * @param list of one type of objects
         * @param matcher - a matcher returning comparison result based on single list element
         **/
        public static <T> int findByMatch(final List<? extends T> list,
                                          final ToIntFunction<? super T> matcher)
        { return findByMatch(list, matcher, 0, list.size()); }
        
        /**
         * Blind binary search applying array elements to matching function until it returns 0
         * @param list of one type of objects
         * @param matcher - a matcher returning comparison result based on single list element
         * @param from - an index (inclusive) from which to start search
         * @param to - an index (exclusive) from which to start search
         **/
        public static <T> int findByMatch(final T[] array,
                                          final ToIntFunction<? super T> matcher,
                                          final int from,
                                          final int to)
        {
                rangeCheck(array.length, from, to);
                return findByIndex(i -> matcher.applyAsInt(array[i]), from, to);
        }
        
        /**
         * Blind binary search applying List elements to matching function until it returns 0
         * @param list of one type of objects
         * @param matcher - a matcher returning comparison result based on single list element
         * @param from - an index (inclusive) from which to start search
         * @param to - an index (exclusive) from which to start search
         **/
        public static <T> int findByMatch(final List<? extends T> list,
                                          final ToIntFunction<? super T> matcher,
                                          final int from,
                                          final int to)
        {
                final IntFunction<? extends T> getter = listGetter(list);
                return findByIndex(i -> matcher.applyAsInt(getter.apply(i)), from, to);
        }
        
        /**
         * Blind binary search applying index to comparison function until it returns 0
         * @param comparator - index-comparing function
         * @param from - an index (inclusive) from which to start search
         * @param to - an index (exclusive) from which to start search
         **/
        public static int findByIndex(final IntUnaryOperator comparator, final int from, final int to) {
                int low = from;
                int high = to - 1;

                while (low <= high) {
                        int mid = (low + high) >>> 1;
                        int cmp = comparator.applyAsInt(mid);

                        if (cmp < 0)
                                low = mid + 1;
                        else if (cmp > 0)
                                high = mid - 1;
                        else
                                return mid; // key found
                }
                return -(low + 1);  // key not found
        }
        
        /**
         * A copy of Arrays.rangeCheck private method from JDK
         */
        private static void rangeCheck(int arrayLength, int fromIndex, int toIndex) {
                if (fromIndex > toIndex)
                        throw new IllegalArgumentException(
                                "fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
                if (fromIndex < 0)
                        throw new ArrayIndexOutOfBoundsException(fromIndex);
                if (toIndex > arrayLength)
                        throw new ArrayIndexOutOfBoundsException(toIndex);
        }
        
        /**
         * A copy of Collections.get private method from JDK
         */
        private static <T> T get(ListIterator<? extends T> i, int index) {
                T obj = null;
                int pos = i.nextIndex();
                if (pos <= index)
                        do
                                obj = i.next();
                        while (pos++ < index);
                else
                        do
                                obj = i.previous();
                        while (--pos > index);
                return obj;
        }

        private static <T, L extends List<? extends T>> IntFunction<? extends T> listGetter(final L list) {
                if (list instanceof RandomAccess)
                        return ((List<? extends T>) list)::get;
                
                final ListIterator<? extends T> it = list.listIterator();
                return i -> get(it, i);
        }

        @FunctionalInterface
        public interface LongComparator {
                int compareAsLong(long f1, long f2);
        }

        @FunctionalInterface
        public interface DoubleComparator {
                int compareAsDouble(double f1, double f2);
        }

        @FunctionalInterface
        public interface LongGetter {
                long getAsLong(int i);
        }

        @FunctionalInterface
        public interface DoubleGetter {
                double getAsDouble(int i);
        }
}