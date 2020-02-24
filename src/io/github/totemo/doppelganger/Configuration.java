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
        _fixDropChanceBug = _plugin.getConfig().getBoolean("fix_drop_chance_bug", false);
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
     * If true, when a mob dies and the drop chance of some piece of its
     * equipment is greater than 0.999, force that item to drop; don't rely on
     * vanilla code, Spigot or PaperSpigot getting it right.
     * 
     * See: {@link https://hub.spigotmc.org/jira/browse/SPIGOT-5298}
     * 
     * Unfortunately, the fix can go wrong because of a Spigot bug where
     * identical (stackable) skulls are treated as dissimilar, so allow the fix
     * to be disabled by configuration.
     * 
     * See: {@link https://hub.spigotmc.org/jira/browse/SPIGOT-5403)}
     * 
     * The PaperSpigot project managed to pull the botched resolution of
     * SPIGOT-5403 at just the right time to enshrine the buggy behaviour in
     * last 10 PaperSpigot builds for 1.14.4. PaperSpigot builds 234-243 for
     * 1.14.4 are bad. Use build 233 or disable the fix.
     *
     * @return true if a drop chance of 0.999 or greater should force a drop by
     *         bypassing vanilla/Spigot/PaperSpigot code.
     */
    public boolean fixDropChanceBug() {
        return _fixDropChanceBug;
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

    /**
     * If true, when a mob dies and the drop chance of some piece of its
     * equipment is greater than 0.999, force that item to drop; don't rely on
     * Spigot getting it right.
     */
    protected boolean _fixDropChanceBug;
} // class Configuration