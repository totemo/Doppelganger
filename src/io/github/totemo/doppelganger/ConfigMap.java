package io.github.totemo.doppelganger;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

// ----------------------------------------------------------------------------
/**
 * A helper for reading Map entries under list entry nodes.
 * 
 * Ideally, the Bukkit ConfigurationSection class would do this for me.
 */
public class ConfigMap
{
  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param map the Map containing configuration entries.
   * @param logger logs messages.
   * @param context describes the "context" where configuration entries are
   *          being accessed in warnings written to the logger.
   */
  @SuppressWarnings("unchecked")
  public ConfigMap(Map<?, ?> map, Logger logger, String context)
  {
    _map = (Map<String, Object>) map;
    _logger = logger;
    _context = context;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the String value with the specified name, or the default if not
   * present or the wrong type.
   * 
   * @param name the name of the entry to get.
   * @param def the default value if not found or the wrong type.
   * @return the value of the named entry.
   */
  public String getString(String name, String def)
  {
    return get(name, def, String.class);
  }

  // --------------------------------------------------------------------------
  /**
   * Return the Boolean value with the specified name, or the default if not
   * present or the wrong type.
   * 
   * @param name the name of the entry to get.
   * @param def the default value if not found or the wrong type.
   * @return the value of the named entry.
   */
  public Boolean getBoolean(String name, Boolean def)
  {
    return get(name, def, Boolean.class);
  }

  // --------------------------------------------------------------------------
  /**
   * Return the Integer value with the specified name, or the default if not
   * present or the wrong type.
   * 
   * @param name the name of the entry to get.
   * @param def the default value if not found or the wrong type.
   * @return the value of the named entry.
   */
  public Integer getInteger(String name, Integer def)
  {
    return get(name, def, Integer.class);
  }

  // --------------------------------------------------------------------------
  /**
   * Return the Double value with the specified name, or the default if not
   * present or the wrong type.
   * 
   * @param name the name of the entry to get.
   * @param def the default value if not found or the wrong type.
   * @return the value of the named entry.
   */
  public Double getDouble(String name, Number def)
  {
    // Config might contain an integer. Tolerate and convert to double.
    Number number = get(name, def, Number.class);
    return (number == null) ? null : number.doubleValue();
  }

  // --------------------------------------------------------------------------
  /**
   * Return a List<Number> with the specified name, or the default if not
   * present or not a List<>.
   * 
   * The individual elements are not actually checked to see if they really are
   * numbers.
   * 
   * @param name the name of the entry to get.
   * @param def the default value if not found or the wrong type.
   * @return the value of the named entry.
   */
  @SuppressWarnings("unchecked")
  public List<Number> getNumberList(String name, List<Number> def)
  {
    return get(name, def, List.class);
  }

  // --------------------------------------------------------------------------
  /**
   * Generic method to get a named value from the map and check its type.
   * 
   * @param name the name of the entry to get.
   * @param def the default value if not found or the wrong type.
   * @param cls the expected type of the value.
   * @return the value of the named entry.
   */
  @SuppressWarnings("unchecked")
  protected <T> T get(String name, T def, Class<T> cls)
  {
    Object value = _map.get(name);
    if (value == null)
    {
      value = def;
    }
    // Default value could be null.
    if (value != null)
    {
      if (!cls.isAssignableFrom(value.getClass()))
      {
        _logger.warning("For " + name + " of " + _context + ": expecting " + cls.getName() +
                        "  but got " + value.getClass() + ". Using default.");
      }
    }
    return (T) value;
  } // get

  // --------------------------------------------------------------------------
  /**
   * The Map containing configuration entries.
   */
  protected Map<String, Object> _map;

  /**
   * The Logger.
   */
  protected Logger              _logger;

  /**
   * Describes the "context" where configuration entries are being accessed in
   * warnings written to the logger.
   */
  protected String              _context;
} // class ConfigMap