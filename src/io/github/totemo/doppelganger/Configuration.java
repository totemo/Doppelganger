package io.github.totemo.doppelganger;

// --------------------------------------------------------------------------
/**
 * Holds configuration settings.
 */
public class Configuration
{
  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param plugin the associated plugin instance.
   */
  public Configuration(Doppelganger plugin, CreatureFactory creatureFactory)
  {
    _plugin = plugin;
    _creatureFactory = creatureFactory;
  }

  // --------------------------------------------------------------------------
  /**
   * Save the configuration file.
   */
  public void save()
  {
    _plugin.saveConfig();
  }

  // --------------------------------------------------------------------------
  /**
   * Load the configuration file.
   */
  public void load()
  {
    _plugin.reloadConfig();
    _creatureFactory.load(_plugin.getConfig(), _plugin.getLogger());
    _arbitraryNameAllowed = _plugin.getConfig().getBoolean("allow_arbitrary_names", false);
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if the player can name the creature anything at all.
   * 
   * @return true if the player can name the creature anything at all, or false
   *         if the name must be a valid player name.
   */
  public boolean isArbitraryNameAllowed()
  {
    return _arbitraryNameAllowed;
  }

  // --------------------------------------------------------------------------
  /**
   * Reference to the plugin instance.
   */
  protected Doppelganger    _plugin;

  /**
   * Handles creation of creatures.
   */
  protected CreatureFactory _creatureFactory;

  /**
   * True if the player can name the creature anything at all, including names
   * with spaces and punctuation in them, for example. If false, the name must
   * be a valid player name.
   */
  protected boolean         _arbitraryNameAllowed;
} // class Configuration