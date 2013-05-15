package io.github.totemo.doppelganger;

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

// ----------------------------------------------------------------------------
/**
 * Manages a collection of elements with associated probability of selection
 * weights and allows random selection of elements.
 * 
 * The probability of choosing a particular element is its weight divided by the
 * sum of all weights.
 */
public class WeightedSelection<E>
{
  // --------------------------------------------------------------------------
  /**
   * Default constructor.
   */
  public WeightedSelection()
  {
    this(new Random());
  }

  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param random the random number generator to use.
   */
  public WeightedSelection(Random random)
  {
    _random = random;
  }

  // --------------------------------------------------------------------------
  /**
   * Add a choice.
   * 
   * @param choice the chosen object.
   * @param weight its probability weight; this must be greater than 0, or the
   *          choice is not added.
   */
  public void addChoice(E choice, double weight)
  {
    if (weight > 0)
    {
      _total += weight;
      _choices.put(_total, choice);
    }
  }

  // --------------------------------------------------------------------------
  /**
   * Return a randomly selected element, or null if there is nothing to choose.
   * 
   * @return a randomly selected element, or null if there is nothing to choose.
   */
  public E choose()
  {
    double value = _random.nextDouble() * _total;

    // Using floorEntry() is more correct here in the sense that it gives the
    // lower choice right on the boundary, and Random.nextDouble() returns
    // [0.0,1.0). But floorEntry() makes it much harder to extract weights and
    // thresholds from the entries.
    Entry<Double, E> entry = _choices.ceilingEntry(value);
    return (entry != null) ? entry.getValue() : null;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the entrySet() of the underlying collection (mainly for debugging).
   * 
   * @rReturn the entrySet() of the underlying collection
   */
  public Set<Entry<Double, E>> entrySet()
  {
    return _choices.entrySet();
  }

  // --------------------------------------------------------------------------
  /**
   * Return the sum of all of the probability weights.
   * 
   * @return the sum of all of the probability weights.
   */
  public double getTotalWeight()
  {
    return _total;
  }

  // --------------------------------------------------------------------------
  /**
   * The random number generator.
   */
  protected Random                        _random;

  /**
   * Sum of all of the weights of all choices.
   */
  protected final NavigableMap<Double, E> _choices = new TreeMap<Double, E>();

  /**
   * Sum of all of the weights of all choices.
   */
  protected double                        _total   = 0;
} // class WeightedSelection
