package io.github.totemo.doppelganger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

// ----------------------------------------------------------------------------
/**
 * Loads command help messages from a named resource bundle hierarchy, e.g.
 * "Help".
 * 
 * The underlying ResourceBundle implementation takes care of localisation. Just
 * ensure that there is a default bundle file in the JAR (e.g.
 * "Help.properties") and add additional bundles for specific languages and
 * locales (e.g. "Help_en.properties", "Help_en_US.properties",
 * "Help_de.properties", etc).
 */
public class Help
{
  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param baseName the base name from which the name of the resource bundle
   *          will be generated.
   * @param logger logs error messages.
   */
  public Help(String baseName, Logger logger)
  {
    try
    {
      _bundle = ResourceBundle.getBundle(baseName);
    }
    catch (MissingResourceException ex)
    {
      // A default bundle should always be present.
      logger.severe("Missing default help bundle, \"" + baseName + "\".");
    }

    // Sometimes, when you recompile and reload,
    // PluginClassLoader.getResourceAsStream() returns null for everything,
    // and then ResourceBundle.getBundle() will throw
    // MissingResourceException. So you know what...
    if (_bundle == null)
    {
      _bundle = new ListResourceBundle()
      {
        @Override
        protected Object[][] getContents()
        {
          return new Object[][]{
            {"topics", "help,info,coords,kill,spawn,maintain"},
            {"help.variants", "1"},
            {"help.1.usage", "&r&f/&6doppel help &f[&6info&f|&6coords&f|&6kill&f|&6spawn&f|&6maintain&f]"},
            {"help.1.description", "&r&f    Show descriptions of &6/doppel &fsubcommands."},

            {"info.variants", "list,shape,creature,player"},
            {"info.header", "&r&eAlternatives:"},
            {"info.list.usage", "&r&f/&6doppel info list"},
            {"info.list.description", "&r&f    List the names of all shapes, creatures and summonable players."},
            {"info.shape.usage", "&r&f/&6doppel info shape &d&oname"},
            {"info.shape.description", "&r&f    Describe the named shape."},
            {"info.creature.usage", "&r&f/&6doppel info creature &d&oname"},
            {"info.creature.description", "&r&f    Describe the named creature type."},
            {"info.player.usage", "&r&f/&6doppel info player &d&oname"},
            {"info.player.description", "&r&f    Describe the named summonable player."},

            {"coords.variants", "here,sphere,box"},
            {"coords.header", "&r&eAlternatives:"},
            {"coords.footer", "&r&eThe world name defaults to that of the player or command block if omitted."},
            {"coords.here.usage", "&r&f/&6doppel coords sphere here &a&oradius &f[&d&oname&f]"},
            {
              "coords.here.description",
              "&r&f    List coordinates of doppelgangers with the specified name in a sphere around the player or command block."},
            {"coords.sphere.usage",
              "&r&f/&6doppel coords sphere &f[&d&oworld&f] &a&ox y z radius &f[&d&oname&f]"},
            {"coords.sphere.description",
              "&r&f    List coordinates of doppelgangers with the specified name in a sphere."},
            {"coords.box.usage",
              "&r&f/&6doppel coords box &f[&d&oworld&f] &a&ox1 y1 z1 x2 y2 z2 &f[&d&oname&f]"},
            {
              "coords.box.description",
              "&r    &fList coordinates of doppelgangers with the specified name in a box (x1,y1,z1) - (x2,y2,z2)."},
            {"kill.variants", "here,sphere,box"},
            {"kill.header", "&r&eAlternatives:"},
            {"kill.footer",
              "&r&eThe world name defaults to that of the player or command block if omitted."},
            {"kill.here.usage", "&r&f/&6doppel kill sphere here &a&oradius &d&oname"},
            {
              "kill.here.description",
              "&r&f    Kill all doppelgangers with the specified name in a sphere around the player or command block."},
            {"kill.sphere.usage", "&r&f/&6doppel kill sphere &f[&d&oworld&f] &a&ox y z radius &d&oname"},
            {"kill.sphere.description", "&r&f    Kill all doppelgangers with the specified name in a sphere."},
            {"kill.box.usage", "&r&f/&6doppel kill box &f[&d&oworld&f] &a&ox1 y1 z1 x2 y2 z2 &d&oname"},
            {"kill.box.description", "&r&f    Kill all doppelgangers with the specified name in a box (x1,y1,z1) - (x2,y2,z2)."},
            {"spawn.variants", "here,at"},
            {"spawn.header", "&r&eAlternatives:"},
            {"spawn.footer",
              "&r&eNote:\n" +
                "&r&e* Anonymous doppelgangers cannot be found or killed by commands.\n" +
                "&r&e* The world name defaults to that of the player or command block if omitted."},
            {"spawn.here.usage", "&r&f/&6doppel spawn here &d&otype &f[&d&oname&f]"},
            {
              "spawn.here.description",
              "&r&f    Spawn a doppelganger of the specified type, with optional name at the location of the player or command block issuing the command."},
            {"spawn.at.usage",
              "&r&f/&6doppel spawn at &f[&d&oworld&f] &a&ox y z &d&otype &f[&d&oname&f]"},
            {
              "spawn.at.description",
              "&r&f    Spawn a doppelganger of the specified type, at the specified location, with optional name."},
            {"maintain.variants", "box,sphere"},
            {"maintain.header", "&r&eAlternatives:"},
            {"maintain.footer",
              "&r&eThe world name defaults to that of the player or command block if omitted."},
            {
              "maintain.box.usage",
              "&r&f/&6doppel maintain at &f[&d&oworld&f] &a&ox y z &r&6box &f[&d&oworld&f] &a&ox1 y1 z1 x2 y2 z2 &d&otype name"},
            {
              "maintain.box.description",
              "&r&f    Spawn a doppelganger with the specified name and type at (x,y,z) in the named world " +
                "if there are no doppelgangers with that name and type in the box (x1,y1,z1) - (x2,y2,z2). If " +
                "there is more than one doppelganger with that name in the box, kill all but the oldest one."},
            {
              "maintain.sphere.usage",
              "&r&f/&6doppel maintain at &f[&d&oworld&f] &a&ox y z &r&6sphere &f[&d&oworld&f] &a&oxc yc zc radius &d&otype name"},
            {
              "maintain.sphere.description",
              "&r&f    Spawn a doppelganger with the specified name and type at (x,y,z) in the named world if " +
                "there are no doppelgangers with that name and type in the sphere whose centre is (xc,yc,zc).  " +
                "If there is more than one doppelganger with that name in the sphere, kill all but the oldest one."}
          };
        }
      };
    }

    List<String> topics = split(getString("topics"), ",");
    if (topics != null)
    {
      for (String topicName : topics)
      {
        _topics.put(topicName, new Topic(topicName));
      }
    }
  } // constructor

  // --------------------------------------------------------------------------
  /**
   * Show the usage syntax message and optionally the description for the
   * specified topic.
   * 
   * All variants of the usage syntax for the specified topic are listed.
   * 
   * @param sender the sender of the request for usage help.
   * @param topicName the key under which the usage message(s) are filed in the
   *          resources (usually a subcommand name).
   * @param showDescription if true, the description is also shown.
   * @return true if the topicName is in #getTopics().
   */
  public boolean showHelp(CommandSender sender, String topicName, boolean showDescription)
  {
    Topic topic = _topics.get(topicName);
    if (topic != null)
    {
      topic.show(sender, showDescription);
    }
    return (topic != null);
  }

  // --------------------------------------------------------------------------
  /**
   * Return the set of all help topic names (in the order they were defined in
   * the configuration).
   * 
   * @return the set of all help topic names.
   */
  public Set<String> getTopics()
  {
    return _topics.keySet();
  }

  // --------------------------------------------------------------------------
  /**
   * Return the string value from the help resource bundle with the specified
   * key.
   * 
   * @param key the key name in the resource properties file.
   * @return the resource value, or null if not found.
   */
  protected String getString(String key)
  {
    try
    {
      // ResourceBundle.getString() always returns non-null.
      return (_bundle != null) ? _bundle.getString(key).replaceAll(LINE_BREAK, "\n") : null;
    }
    catch (MissingResourceException ex)
    {
      return null;
    }
  }

  // --------------------------------------------------------------------------
  /**
   * Split the value using the delimiter and return a list of elements with
   * translated colour codes.
   * 
   * @param value the property value containing multiple elements.
   * @param delimiter the delimiter between elements.
   * @return a list of strings, or null if the value argument was null.
   */
  protected static List<String> split(String value, String delimiter)
  {
    if (value == null)
    {
      return null;
    }
    else
    {
      ArrayList<String> list = new ArrayList<String>();
      for (String item : value.split(delimiter))
      {
        list.add(ChatColor.translateAlternateColorCodes('&', item));
      }

      // Special case to show a blank line if value ends with {br} (= '\n').
      if (value.endsWith(delimiter))
      {
        list.add("");
      }
      return list;
    }
  } // split

  // --------------------------------------------------------------------------
  /**
   * A single help topic, caching description of a command parsed out of the
   * resource bundle.
   */
  protected final class Topic
  {
    // ------------------------------------------------------------------------
    /**
     * Constructor.
     * 
     * @param topicName the name and search key of the help topic.
     */
    public Topic(String topicName)
    {
      _header = split(getString(topicName + ".header"), "\n");
      _footer = split(getString(topicName + ".footer"), "\n");
      List<String> variants = split(getString(topicName + ".variants"), ",");
      for (String variantName : variants)
      {
        _variants.add(new Variant(topicName, variantName));
      }
    }
    // ------------------------------------------------------------------------
    /**
     * Show help on this topic to the command sender.
     * 
     * @param sender the recipient of the help messages.
     * @param showDescription if true, the description text is shown; otherwise
     *          only the usage syntax is shown.
     */
    public void show(CommandSender sender, boolean showDescription)
    {
      if (showDescription)
      {
        showStringList(sender, _header);
      }
      for (Variant variant : _variants)
      {
        variant.show(sender, showDescription);
      }
      if (showDescription)
      {
        showStringList(sender, _footer);
      }
    }

    // ------------------------------------------------------------------------
    /**
     * Send each entry in the the list of lines as messages to the
     * CommandSender.
     * 
     * @param sender the sender of the request for help.
     * @param lines the list of lines read from the configuration.
     */
    protected void showStringList(CommandSender sender, List<String> lines)
    {
      if (lines != null)
      {
        for (String line : lines)
        {
          sender.sendMessage(line);
        }
      }
    }

    // ------------------------------------------------------------------------
    /**
     * Describes a variant of the a command/topic.
     */
    protected final class Variant
    {
      // ----------------------------------------------------------------------
      /**
       * Constructor to load a Variant from resources.
       * 
       * @param topicName the topic name (command name).
       * @param variantName a unique identifier for this variant (not shown to
       *          the user).
       */
      public Variant(String topicName, String variantName)
      {
        _usage = split(getString(topicName + '.' + variantName + ".usage"), "\n");
        _description = split(getString(topicName + '.' + variantName + ".description"), "\n");
      }

      // ----------------------------------------------------------------------
      /**
       * Show help for this variant.
       * 
       * @param sender the CommandSender to whom output is sent.
       * @param showDescription if true, the description text is shown;
       *          otherwise only the usage syntax is shown.
       */
      public void show(CommandSender sender, boolean showDescription)
      {
        showStringList(sender, _usage);
        if (showDescription)
        {
          showStringList(sender, _description);
        }
      }

      // ----------------------------------------------------------------------
      /**
       * Usage syntax in this variant of the command.
       */
      List<String> _usage;

      /**
       * Description of this variant of the command.
       */
      List<String> _description;
    } // inner class Topic.Variant

    // --------------------------------------------------------------------------
    /**
     * Header lines.
     */
    protected List<String>       _header;

    /**
     * Footer lines.
     */
    protected List<String>       _footer;

    /**
     * All the variants in the order they should be listed.
     */
    protected ArrayList<Variant> _variants = new ArrayList<Help.Topic.Variant>();
  }// inner class Topic

  // --------------------------------------------------------------------------
  /**
   * Representation of explicit line breaks in the Properties file.
   * 
   * A '\n' doesn't cut it (when we want to insert a trailing blank line)
   * because Properties removes trailing white space.
   */
  protected static final String          LINE_BREAK = "\\{br\\}";

  // --------------------------------------------------------------------------
  /**
   * Reference to the plugin whose configuration will be loaded for help
   * messages.
   */
  protected ResourceBundle               _bundle;

  /**
   * A map from topic name to instance, in presentation order.
   */
  protected LinkedHashMap<String, Topic> _topics    = new LinkedHashMap<String, Help.Topic>();
} // class Help