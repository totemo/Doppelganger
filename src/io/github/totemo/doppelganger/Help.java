package io.github.totemo.doppelganger;

import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

// ----------------------------------------------------------------------------
/**
 * Loads command help messages from the plugin configuration file and displays
 * them upon request.
 * 
 * The configuration file hierarchy looks like this:
 * 
 * <pre>
 * help:
 *   topic:
 *     topicname:
 *       header:
 *       - "List of strings"
 *       - "representing lines to show"
 *       footer:
 *       - "List of strings"
 *       - "representing lines to show"
 *       variant:
 *         variantname1:
 *           usage:
 *           - "List of strings"
 *           - "representing lines to show"
 *           description:
 *           - "List of strings"
 *           - "representing lines to show"
 *         variantname2:
 *           usage:
 *           - "List of strings representing lines to show"
 *           description:
 *           - "List of strings representing lines to show"
 * </pre>
 */
public class Help
{
  public Help(Plugin plugin)
  {
    _plugin = plugin;
  }

  // --------------------------------------------------------------------------
  /**
   * Show the usage syntax message and optionally the description for the
   * specified topic.
   * 
   * All variants of the usage syntax for the specified topic are listed.
   * 
   * @param sender the sender of the request for usage help.
   * @param topicName the key under which the usage message(s) are filed in the
   *          configuration (usually a subcommand name).
   * @paran showDescription if true, the description is also shown.
   */
  public void showHelp(CommandSender sender, String topicName, boolean showDescription)
  {
    ConfigurationSection topicsSection = _plugin.getConfig().getConfigurationSection("help.topic");
    ConfigurationSection topicSection = topicsSection.getConfigurationSection(topicName);
    if (topicSection != null)
    {
      if (showDescription)
      {
        showStringList(sender, topicSection.getStringList("header"));
      }
      ConfigurationSection variantSection = topicSection.getConfigurationSection("variant");
      for (String variant : variantSection.getKeys(false))
      {
        showStringList(sender, variantSection.getStringList(variant + ".usage"));
        if (showDescription)
        {
          showStringList(sender, variantSection.getStringList(variant + ".description"));
        }
      }
      if (showDescription)
      {
        showStringList(sender, topicSection.getStringList("footer"));
      }
    }
  } // showUsage

  // --------------------------------------------------------------------------
  /**
   * Return the set of all help topic names (in the order they were defined in
   * the configuration).
   * 
   * @return the set of all help topic names.
   */
  public Set<String> getTopics()
  {
    ConfigurationSection topicsSection = _plugin.getConfig().getConfigurationSection("help.topic");
    return topicsSection.getKeys(false);
  }

  // --------------------------------------------------------------------------
  /**
   * Translate the colour escape codes in each item in the list of strings and
   * show the translated string on a separate line sent to the sender.
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
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
      }
    }
  }

  // --------------------------------------------------------------------------
  /**
   * Reference to the plugin whose configuration will be loaded for help
   * messages.
   */
  Plugin _plugin;
} // class Help