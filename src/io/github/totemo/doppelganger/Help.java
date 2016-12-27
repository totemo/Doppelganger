package io.github.totemo.doppelganger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

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
public class Help {
    // ------------------------------------------------------------------------
    /**
     * Constructor.
     * 
     * @param baseName the base name from which the name of the resource bundle
     *        will be generated.
     * @param plugin the plugin.
     */
    public Help(String baseName, JavaPlugin plugin) {
        // Sometimes, when you recompile and reload,
        // PluginClassLoader.getResourceAsStream() returns null for everything,
        // and then ResourceBundle.getBundle() will throw
        // MissingResourceException. So simulating getBundle() instead.
        try {
            URLClassLoader loader = (URLClassLoader) getClass().getClassLoader();
            for (URL url : loader.getURLs()) {
                // Probably only the Doppelgange.jar file in the URLs, but be
                // safe.
                URI uri = url.toURI();
                if (uri.toString().contains(plugin.getName())) {
                    _bundle = loadResourceBundle(new File(uri), Locale.getDefault(), baseName, plugin.getLogger());
                }
            }
        } catch (Exception ex) {
            // Handled by null check.
        }
        if (_bundle == null) {
            plugin.getLogger().severe("Unable to load the help resources.");
        }

        List<String> topics = split(getString("topics"), ",");
        if (topics != null) {
            for (String topicName : topics) {
                _topics.put(topicName, new Topic(topicName));
            }
        }
    } // constructor

    // ------------------------------------------------------------------------
    /**
     * Show the usage syntax message and optionally the description for the
     * specified topic.
     * 
     * All variants of the usage syntax for the specified topic are listed.
     * 
     * @param sender the sender of the request for usage help.
     * @param topicName the key under which the usage message(s) are filed in
     *        the resources (usually a subcommand name).
     * @param showDescription if true, the description is also shown.
     * @return true if the topicName is in #getTopics().
     */
    public boolean showHelp(CommandSender sender, String topicName, boolean showDescription) {
        Topic topic = _topics.get(topicName);
        if (topic != null) {
            topic.show(sender, showDescription);
        }
        return (topic != null);
    }

    // ------------------------------------------------------------------------
    /**
     * Return the set of all help topic names (in the order they were defined in
     * the configuration).
     * 
     * @return the set of all help topic names.
     */
    public Set<String> getTopics() {
        return _topics.keySet();
    }

    // ------------------------------------------------------------------------
    /**
     * Return the string value from the help resource bundle with the specified
     * key.
     * 
     * @param key the key name in the resource properties file.
     * @return the resource value, or null if not found.
     */
    protected String getString(String key) {
        try {
            // ResourceBundle.getString() always returns non-null.
            return (_bundle != null) ? _bundle.getString(key).replaceAll(LINE_BREAK, "\n") : null;
        } catch (MissingResourceException ex) {
            return null;
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Split the value using the delimiter and return a list of elements with
     * translated colour codes.
     * 
     * @param value the property value containing multiple elements.
     * @param delimiter the delimiter between elements.
     * @return a list of strings, or null if the value argument was null.
     */
    protected static List<String> split(String value, String delimiter) {
        if (value == null) {
            return null;
        } else {
            ArrayList<String> list = new ArrayList<String>();
            for (String item : value.split(delimiter)) {
                list.add(ChatColor.translateAlternateColorCodes('&', item));
            }

            // Special case to show a blank line if value ends with {br} (=
            // '\n').
            if (value.endsWith(delimiter)) {
                list.add("");
            }
            return list;
        }
    } // split

    // ------------------------------------------------------------------------
    /**
     * Implement a reasonable subset of ResourceBundle.getBundle() functionality
     * for when a Bukkit reload breaks ClassLoader.getResourceAsStream().
     * 
     * @param pluginJarFile the plugin's JAR file to load from.
     * @param local the locale to use when selecting the resource bundle.
     * @param base the base name of the language resource properties file.
     * @param logger logs messages
     * @return the most specific available PropertyResourceBundle for the
     *         locale.
     * @throws IOException on error.
     */
    protected ResourceBundle loadResourceBundle(File pluginJarFile, Locale locale, String base, Logger logger)
    throws IOException {
        String lang = (locale.getLanguage().isEmpty()) ? "" : '_' + locale.getLanguage();
        String script = (locale.getScript().isEmpty()) ? "" : '_' + locale.getScript();
        String country = (locale.getCountry().isEmpty()) ? "" : '_' + locale.getCountry();
        String variant = (locale.getVariant().isEmpty()) ? "" : '_' + locale.getVariant();

        // Some of these may be duplicates and that's fine.
        ArrayList<String> paths = new ArrayList<String>();
        paths.add(base + lang + script + country + variant);
        paths.add(base + lang + script + country);
        paths.add(base + lang + script);
        paths.add(base + lang + country + variant);
        paths.add(base + lang + country);
        paths.add(base + lang);
        paths.add(base);

        JarFile jar = null;
        try {
            jar = new JarFile(pluginJarFile);
            for (String path : paths) {
                String propertiesFile = path + ".properties";
                ResourceBundle bundle = loadResourceBundle(jar, propertiesFile);
                if (bundle != null) {
                    logger.fine("Loaded help from " + pluginJarFile.getName() + "/" + propertiesFile);
                    return bundle;
                }
            }
        } finally {
            if (jar != null) {
                jar.close();
            }
        }
        return null;
    } // loadResourceBundle

    // ------------------------------------------------------------------------
    /**
     * Attempt to load the specified resource from the JAR file.
     * 
     * @param jar the open JAR file.
     * @param path the path to the resource.
     * @return a new PropertyResourceBundle if successful, or null if the path
     *         did not match a resource.
     */
    protected ResourceBundle loadResourceBundle(JarFile jar, String path)
    throws IOException {
        ZipEntry entry = jar.getEntry(path);
        if (entry != null) {
            return new PropertyResourceBundle(jar.getInputStream(entry));
        } else {
            return null;
        }
    }

    // ------------------------------------------------------------------------
    /**
     * A single help topic, caching description of a command parsed out of the
     * resource bundle.
     */
    protected final class Topic {
        // --------------------------------------------------------------------
        /**
         * Constructor.
         * 
         * @param topicName the name and search key of the help topic.
         */
        public Topic(String topicName) {
            _header = split(getString(topicName + ".header"), "\n");
            _footer = split(getString(topicName + ".footer"), "\n");
            List<String> variants = split(getString(topicName + ".variants"), ",");
            for (String variantName : variants) {
                _variants.add(new Variant(topicName, variantName));
            }
        }

        // --------------------------------------------------------------------
        /**
         * Show help on this topic to the command sender.
         * 
         * @param sender the recipient of the help messages.
         * @param showDescription if true, the description text is shown;
         *        otherwise only the usage syntax is shown.
         */
        public void show(CommandSender sender, boolean showDescription) {
            if (showDescription) {
                showStringList(sender, _header);
            }
            for (Variant variant : _variants) {
                variant.show(sender, showDescription);
            }
            if (showDescription) {
                showStringList(sender, _footer);
            }
        }

        // --------------------------------------------------------------------
        /**
         * Send each entry in the the list of lines as messages to the
         * CommandSender.
         * 
         * @param sender the sender of the request for help.
         * @param lines the list of lines read from the configuration.
         */
        protected void showStringList(CommandSender sender, List<String> lines) {
            if (lines != null) {
                for (String line : lines) {
                    sender.sendMessage(line);
                }
            }
        }

        // --------------------------------------------------------------------
        /**
         * Describes a variant of the a command/topic.
         */
        protected final class Variant {
            // ----------------------------------------------------------------
            /**
             * Constructor to load a Variant from resources.
             * 
             * @param topicName the topic name (command name).
             * @param variantName a unique identifier for this variant (not
             *        shown to the user).
             */
            public Variant(String topicName, String variantName) {
                _usage = split(getString(topicName + '.' + variantName + ".usage"), "\n");
                _description = split(getString(topicName + '.' + variantName + ".description"), "\n");
            }

            // ----------------------------------------------------------------
            /**
             * Show help for this variant.
             * 
             * @param sender the CommandSender to whom output is sent.
             * @param showDescription if true, the description text is shown;
             *        otherwise only the usage syntax is shown.
             */
            public void show(CommandSender sender, boolean showDescription) {
                showStringList(sender, _usage);
                if (showDescription) {
                    showStringList(sender, _description);
                }
            }

            // ----------------------------------------------------------------
            /**
             * Usage syntax in this variant of the command.
             */
            List<String> _usage;

            /**
             * Description of this variant of the command.
             */
            List<String> _description;
        } // inner class Topic.Variant

        // --------------------------------------------------------------------
        /**
         * Header lines.
         */
        protected List<String> _header;

        /**
         * Footer lines.
         */
        protected List<String> _footer;

        /**
         * All the variants in the order they should be listed.
         */
        protected ArrayList<Variant> _variants = new ArrayList<Help.Topic.Variant>();
    } // inner class Topic

    // ------------------------------------------------------------------------
    /**
     * Representation of explicit line breaks in the Properties file.
     * 
     * A '\n' doesn't cut it (when we want to insert a trailing blank line)
     * because Properties removes trailing white space.
     */
    protected static final String LINE_BREAK = "\\{br\\}";

    // ------------------------------------------------------------------------
    /**
     * Reference to the plugin whose configuration will be loaded for help
     * messages.
     */
    protected ResourceBundle _bundle;

    /**
     * A map from topic name to instance, in presentation order.
     */
    protected LinkedHashMap<String, Topic> _topics = new LinkedHashMap<String, Help.Topic>();
} // class Help