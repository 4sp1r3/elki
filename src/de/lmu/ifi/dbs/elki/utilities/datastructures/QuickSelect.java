package de.lmu.ifi.dbs.elki.utilities.datastructures;

import java.util.List;

/*
 This file is part of ELKI:
 Environment for Developing KDD-Applications Supported by Index-Structures

 Copyright (C) 2011
 Ludwig-Maximilians-Universität München
 Lehr- und Forschungseinheit für Datenbanksysteme
 ELKI Development Team

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * QuickSelect computes ("selects") the element at a given rank and can be used
 * to compute Medians and arbitrary quantiles by computing the appropriate rank.
 * 
 * This algorithm is essentially an incomplete QuickSort that only descends into
 * that part of the data that we are interested in, and also attributed to
 * Charles Antony Richard Hoare
 * 
 * @author Erich Schubert
 */
public class QuickSelect {
  /**
   * For small arrays, use a simpler method:
   */
  private static final int SMALL = 10;

  /**
   * QuickSelect is essentially quicksort, except that we only "sort" that half
   * of the array that we are interested in.
   * 
   * Note: the array is <b>modified</b> by this.
   * 
   * @param data Data to process
   * @param rank Rank position that we are interested in (integer!)
   * @return Value at the given rank
   */
  public static double quickSelect(double[] data, int rank) {
    quickSelect(data, 0, data.length - 1, rank);
    return data[rank];
  }

  /**
   * Compute the median of an array efficiently using the QuickSelect method.
   * 
   * Note: the array is <b>modified</b> by this.
   * 
   * @param data Data to process
   * @return Median value
   */
  public static double median(double[] data) {
    return median(data, 0, data.length - 1);
  }

  /**
   * Compute the median of an array efficiently using the QuickSelect method.
   * 
   * Note: the array is <b>modified</b> by this.
   * 
   * @param data Data to process
   * @param begin Begin of valid values
   * @param end End of valid values (inclusive!)
   * @return Median value
   */
  public static double median(double[] data, int begin, int end) {
    final int length = (end + 1) - begin;
    assert (length > 0);
    // Integer division is "floor" since we are non-negative.
    final int left = begin + (length - 1) / 2;
    quickSelect(data, begin, end, left);
    if(length % 2 == 1) {
      return data[left];
    }
    else {
      quickSelect(data, begin, end, left + 1);
      return data[left] + (data[left + 1] - data[left]) / 2;
    }
  }

  /**
   * Compute the median of an array efficiently using the QuickSelect method.
   * 
   * Note: the array is <b>modified</b> by this.
   * 
   * @param data Data to process
   * @param quant Quantile to compute
   * @return Value at quantile
   */
  public static double quantile(double[] data, double quant) {
    return quantile(data, 0, data.length - 1, quant);
  }

  /**
   * Compute the median of an array efficiently using the QuickSelect method.
   * 
   * Note: the array is <b>modified</b> by this.
   * 
   * @param data Data to process
   * @param begin Begin of valid values
   * @param end End of valid values (inclusive!)
   * @param quant Quantile to compute
   * @return Value at quantile
   */
  public static double quantile(double[] data, int begin, int end, double quant) {
    final int length = (end + 1) - begin;
    assert (length > 0) : "Quantile on empty set?";
    // Integer division is "floor" since we are non-negative.
    final double dleft = begin + (length - 1) * quant;
    final int ileft = (int) Math.floor(dleft);
    final double err = dleft - ileft;

    quickSelect(data, begin, end, ileft);
    if(err <= Double.MIN_NORMAL) {
      return data[ileft];
    }
    else {
      quickSelect(data, begin, end, ileft + 1);
      // Mix:
      double mix = data[ileft] + (data[ileft + 1] - data[ileft]) * err;
      return mix;
    }
  }

  /**
   * QuickSelect is essentially quicksort, except that we only "sort" that half
   * of the array that we are interested in.
   * 
   * @param data Data to process
   * @param start Interval start
   * @param end Interval end (inclusive)
   * @param rank rank position we are interested in (starting at 0)
   */
  public static void quickSelect(double[] data, int start, int end, int rank) {
    // Optimization for small arrays
    // This also ensures a minimum size below
    if(start + SMALL > end) {
      insertionSort(data, start, end);
      return;
    }

    // Pick pivot from three candidates: start, middle, end
    // Since we compare them, we can also just "bubble sort" them.
    final int middle = (start + end) / 2;
    if(data[start] > data[middle]) {
      swap(data, start, middle);
    }
    if(data[start] > data[end]) {
      swap(data, start, end);
    }
    if(data[middle] > data[end]) {
      swap(data, middle, end);
    }
    // TODO: use more candidates for larger arrays?

    final double pivot = data[middle];
    // Move middle element out of the way, just before end
    // (Since we already know that "end" is bigger)
    swap(data, middle, end - 1);

    // Begin partitioning
    int i = start + 1, j = end - 2;
    // This is classic quicksort stuff
    while(true) {
      while(data[i] <= pivot && i <= j) {
        i++;
      }
      while(data[j] >= pivot && j >= i) {
        j--;
      }
      if(i >= j) {
        break;
      }
      swap(data, i, j);
    }

    // Move pivot (former middle element) back into the appropriate place
    swap(data, i, end - 1);

    // In contrast to quicksort, we only need to recurse into the half we are
    // interested in.
    if(rank < i) {
      quickSelect(data, start, i - 1, rank);
    }
    else if(rank > i) {
      quickSelect(data, i + 1, end, rank);
    }
  }

  /**
   * Sort a small array using repetitive insertion sort.
   * 
   * @param data Data to sort
   * @param start Interval start
   * @param end Interval end
   */
  private static void insertionSort(double[] data, int start, int end) {
    for(int i = start + 1; i <= end; i++) {
      for(int j = i; j > start && data[j - 1] > data[j]; j--) {
        swap(data, j, j - 1);
      }
    }
  }

  /**
   * The usual swap method.
   * 
   * @param data Array
   * @param a First index
   * @param b Second index
   */
  private static final void swap(double[] data, int a, int b) {
    double tmp = data[a];
    data[a] = data[b];
    data[b] = tmp;
  }

  /**
   * QuickSelect is essentially quicksort, except that we only "sort" that half
   * of the array that we are interested in.
   * 
   * Note: the array is <b>modified</b> by this.
   * 
   * @param data Data to process
   * @param rank Rank position that we are interested in (integer!)
   * @return Value at the given rank
   */
  public static <T extends Comparable<? super T>> T quickSelect(T[] data, int rank) {
    quickSelect(data, 0, data.length - 1, rank);
    return data[rank];
  }

  /**
   * Compute the median of an array efficiently using the QuickSelect method.
   * 
   * Note: the array is <b>modified</b> by this.
   * 
   * @param data Data to process
   * @return Median value
   */
  public static <T extends Comparable<? super T>> T median(T[] data) {
    return median(data, 0, data.length - 1);
  }

  /**
   * Compute the median of an array efficiently using the QuickSelect method.
   * 
   * On an odd length, it will return the lower element.
   * 
   * Note: the array is <b>modified</b> by this.
   * 
   * @param data Data to process
   * @param begin Begin of valid values
   * @param end End of valid values (inclusive!)
   * @return Median value
   */
  public static <T extends Comparable<? super T>> T median(T[] data, int begin, int end) {
    final int length = (end + 1) - begin;
    assert (length > 0);
    // Integer division is "floor" since we are non-negative.
    final int left = begin + (length - 1) / 2;
    quickSelect(data, begin, end, left);
    return data[left];
  }

  /**
   * Compute the median of an array efficiently using the QuickSelect method.
   * 
   * Note: the array is <b>modified</b> by this.
   * 
   * @param data Data to process
   * @param quant Quantile to compute
   * @return Value at quantile
   */
  public static <T extends Comparable<? super T>> T quantile(T[] data, double quant) {
    return quantile(data, 0, data.length - 1, quant);
  }

  /**
   * Compute the median of an array efficiently using the QuickSelect method.
   * 
   * It will prefer the lower element.
   * 
   * Note: the array is <b>modified</b> by this.
   * 
   * @param data Data to process
   * @param begin Begin of valid values
   * @param end End of valid values (inclusive!)
   * @param quant Quantile to compute
   * @return Value at quantile
   */
  public static <T extends Comparable<? super T>> T quantile(T[] data, int begin, int end, double quant) {
    final int length = (end + 1) - begin;
    assert (length > 0) : "Quantile on empty set?";
    // Integer division is "floor" since we are non-negative.
    final double dleft = begin + (length - 1) * quant;
    final int ileft = (int) Math.floor(dleft);

    quickSelect(data, begin, end, ileft);
    return data[ileft];
  }

  /**
   * QuickSelect is essentially quicksort, except that we only "sort" that half
   * of the array that we are interested in.
   * 
   * @param data Data to process
   * @param start Interval start
   * @param end Interval end (inclusive)
   * @param rank rank position we are interested in (starting at 0)
   */
  public static <T extends Comparable<? super T>> void quickSelect(T[] data, int start, int end, int rank) {
    // Optimization for small arrays
    // This also ensures a minimum size below
    if(start + SMALL > end) {
      insertionSort(data, start, end);
      return;
    }

    // Pick pivot from three candidates: start, middle, end
    // Since we compare them, we can also just "bubble sort" them.
    final int middle = (start + end) / 2;
    if(data[start].compareTo(data[middle]) > 0) {
      swap(data, start, middle);
    }
    if(data[start].compareTo(data[end]) > 0) {
      swap(data, start, end);
    }
    if(data[middle].compareTo(data[end]) > 0) {
      swap(data, middle, end);
    }
    // TODO: use more candidates for larger arrays?

    final T pivot = data[middle];
    // Move middle element out of the way, just before end
    // (Since we already know that "end" is bigger)
    swap(data, middle, end - 1);

    // Begin partitioning
    int i = start + 1, j = end - 2;
    // This is classic quicksort stuff
    while(true) {
      while(data[i].compareTo(pivot) <= 0 && i <= j) {
        i++;
      }
      while(data[j].compareTo(pivot) >= 0 && j >= i) {
        j--;
      }
      if(i >= j) {
        break;
      }
      swap(data, i, j);
    }

    // Move pivot (former middle element) back into the appropriate place
    swap(data, i, end - 1);

    // In contrast to quicksort, we only need to recurse into the half we are
    // interested in.
    if(rank < i) {
      quickSelect(data, start, i - 1, rank);
    }
    else if(rank > i) {
      quickSelect(data, i + 1, end, rank);
    }
  }

  /**
   * Sort a small array using repetitive insertion sort.
   * 
   * @param data Data to sort
   * @param start Interval start
   * @param end Interval end
   */
  private static <T extends Comparable<? super T>> void insertionSort(T[] data, int start, int end) {
    for(int i = start + 1; i <= end; i++) {
      for(int j = i; j > start && data[j - 1].compareTo(data[j]) > 0; j--) {
        swap(data, j, j - 1);
      }
    }
  }

  /**
   * The usual swap method.
   * 
   * @param data Array
   * @param a First index
   * @param b Second index
   */
  private static final <T extends Comparable<? super T>> void swap(T[] data, int a, int b) {
    T tmp = data[a];
    data[a] = data[b];
    data[b] = tmp;
  }

  /**
   * QuickSelect is essentially quicksort, except that we only "sort" that half
   * of the array that we are interested in.
   * 
   * Note: the array is <b>modified</b> by this.
   * 
   * @param data Data to process
   * @param rank Rank position that we are interested in (integer!)
   * @return Value at the given rank
   */
  public static <T extends Comparable<? super T>> T quickSelect(List<? extends T> data, int rank) {
    quickSelect(data, 0, data.size() - 1, rank);
    return data.get(rank);
  }

  /**
   * Compute the median of an array efficiently using the QuickSelect method.
   * 
   * Note: the array is <b>modified</b> by this.
   * 
   * @param data Data to process
   * @return Median value
   */
  public static <T extends Comparable<? super T>> T median(List<? extends T> data) {
    return median(data, 0, data.size() - 1);
  }

  /**
   * Compute the median of an array efficiently using the QuickSelect method.
   * 
   * On an odd length, it will return the lower element.
   * 
   * Note: the array is <b>modified</b> by this.
   * 
   * @param data Data to process
   * @param begin Begin of valid values
   * @param end End of valid values (inclusive!)
   * @return Median value
   */
  public static <T extends Comparable<? super T>> T median(List<? extends T> data, int begin, int end) {
    final int length = (end + 1) - begin;
    assert (length > 0);
    // Integer division is "floor" since we are non-negative.
    final int left = begin + (length - 1) / 2;
    quickSelect(data, begin, end, left);
    return data.get(left);
  }

  /**
   * Compute the median of an array efficiently using the QuickSelect method.
   * 
   * Note: the array is <b>modified</b> by this.
   * 
   * @param data Data to process
   * @param quant Quantile to compute
   * @return Value at quantile
   */
  public static <T extends Comparable<? super T>> T quantile(List<? extends T> data, double quant) {
    return quantile(data, 0, data.size() - 1, quant);
  }

  /**
   * Compute the median of an array efficiently using the QuickSelect method.
   * 
   * It will prefer the lower element.
   * 
   * Note: the array is <b>modified</b> by this.
   * 
   * @param data Data to process
   * @param begin Begin of valid values
   * @param end End of valid values (inclusive!)
   * @param quant Quantile to compute
   * @return Value at quantile
   */
  public static <T extends Comparable<? super T>> T quantile(List<? extends T> data, int begin, int end, double quant) {
    final int length = (end + 1) - begin;
    assert (length > 0) : "Quantile on empty set?";
    // Integer division is "floor" since we are non-negative.
    final double dleft = begin + (length - 1) * quant;
    final int ileft = (int) Math.floor(dleft);

    quickSelect(data, begin, end, ileft);
    return data.get(ileft);
  }

  /**
   * QuickSelect is essentially quicksort, except that we only "sort" that half
   * of the array that we are interested in.
   * 
   * @param data Data to process
   * @param start Interval start
   * @param end Interval end (inclusive)
   * @param rank rank position we are interested in (starting at 0)
   */
  public static <T extends Comparable<? super T>> void quickSelect(List<? extends T> data, int start, int end, int rank) {
    // Optimization for small arrays
    // This also ensures a minimum size below
    if(start + SMALL > end) {
      insertionSort(data, start, end);
      return;
    }

    // Pick pivot from three candidates: start, middle, end
    // Since we compare them, we can also just "bubble sort" them.
    final int middle = (start + end) / 2;
    if(data.get(start).compareTo(data.get(middle)) > 0) {
      swap(data, start, middle);
    }
    if(data.get(start).compareTo(data.get(end)) > 0) {
      swap(data, start, end);
    }
    if(data.get(middle).compareTo(data.get(end)) > 0) {
      swap(data, middle, end);
    }
    // TODO: use more candidates for larger arrays?

    final T pivot = data.get(middle);
    // Move middle element out of the way, just before end
    // (Since we already know that "end" is bigger)
    swap(data, middle, end - 1);

    // Begin partitioning
    int i = start + 1, j = end - 2;
    // This is classic quicksort stuff
    while(true) {
      while(data.get(i).compareTo(pivot) <= 0 && i <= j) {
        i++;
      }
      while(data.get(j).compareTo(pivot) >= 0 && j >= i) {
        j--;
      }
      if(i >= j) {
        break;
      }
      swap(data, i, j);
    }

    // Move pivot (former middle element) back into the appropriate place
    swap(data, i, end - 1);

    // In contrast to quicksort, we only need to recurse into the half we are
    // interested in.
    if(rank < i) {
      quickSelect(data, start, i - 1, rank);
    }
    else if(rank > i) {
      quickSelect(data, i + 1, end, rank);
    }
  }

  /**
   * Sort a small array using repetitive insertion sort.
   * 
   * @param data Data to sort
   * @param start Interval start
   * @param end Interval end
   */
  private static <T extends Comparable<? super T>> void insertionSort(List<T> data, int start, int end) {
    for(int i = start + 1; i <= end; i++) {
      for(int j = i; j > start && data.get(j - 1).compareTo(data.get(j)) > 0; j--) {
        swap(data, j, j - 1);
      }
    }
  }

  /**
   * The usual swap method.
   * 
   * @param data Array
   * @param a First index
   * @param b Second index
   */
  private static final <T extends Comparable<? super T>> void swap(List<T> data, int a, int b) {
    T tmp = data.get(a);
    data.set(a, data.get(b));
    data.set(b, tmp);
  }
}