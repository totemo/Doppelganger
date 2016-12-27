package io.github.totemo.doppelganger;

// ----------------------------------------------------------------------------
/**
 * Holds configuration settings.
 */
public class Configuration {
    // ------------------------------------------------------------------------
    /**
     * Constructor.
     * 
     * @param plugin the associated plugin instance.
     */
    public Configuration(Doppelganger plugin, CreatureFactory creatureFactory) {
        _plugin = plugin;
        _creatureFactory = creatureFactory;
    }

    // ------------------------------------------------------------------------
    /**
     * Save the configuration file.
     */
    public void save() {
        _plugin.saveConfig();
    }

    // ------------------------------------------------------------------------
    /**
     * Load the configuration file.
     */
    public void load() {
        _plugin.reloadConfig();
        _creatureFactory.load(_plugin.getConfig(), _plugin.getLogger());
        _arbitraryNameAllowed = _plugin.getConfig().getBoolean("allow_arbitrary_names", false);
        _warnOnInvalidName = _plugin.getConfig().getBoolean("warn_on_invalid_name", false);
    }

    // ------------------------------------------------------------------------
    /**
     * Return true if the player can name the creature anything at all.
     * 
     * @return true if the player can name the creature anything at all, or
     *         false if the name must be a valid player name.
     */
    public boolean isArbitraryNameAllowed() {
        return _arbitraryNameAllowed;
    }

    // ------------------------------------------------------------------------
    /**
     * Return true if the player will be told when the name of the block they
     * are placing is not allowed.
     * 
     * @return true if the player will be told when the name of the block they
     *         are placing is not allowed.
     */
    public boolean warnOnInvalidName() {
        return _warnOnInvalidName;
    }

    // ------------------------------------------------------------------------
    /**
     * Reference to the plugin instance.
     */
    protected Doppelganger _plugin;

    /**
     * Handles creation of creatures.
     */
    protected CreatureFactory _creatureFactory;

    /**
     * True if the player can name the creature anything at all, including names
     * with spaces and punctuation in them, for example. If false, the name must
     * be a valid player name.
     */
    protected boolean _arbitraryNameAllowed;

    /**
     * If true, the player will be told when the name of the block they are
     * placing is not allowed.
     */
    protected boolean _warnOnInvalidName;
} // class Configuration