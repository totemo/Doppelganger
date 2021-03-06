package io.github.totemo.doppelganger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

// ----------------------------------------------------------------------------
/**
 * Manages known creature types and creates them on demand.
 */
public class CreatureFactory {
    // ------------------------------------------------------------------------
    /**
     * Return the type name of the specified LivingEntity.
     *
     * @return the type name of the specified LivingEntity.
     */
    public static String getLivingEntityType(LivingEntity living) {
        PredefinedCreature predefined = PredefinedCreature.fromLivingEntity(living);
        return (predefined != null) ? predefined.name() : living.getType().getName();
    }

    // ------------------------------------------------------------------------
    /**
     * Load the creature shapes and types from the configuration file.
     *
     * @param root the root of the configuration hierarchy.
     * @param logger the Logger.
     */
    public void load(ConfigurationSection root, Logger logger) {
        // Wipe out the old configuration if previously loaded.
        _shapes.clear();
        _types.clear();
        _playerCreatures.clear();
        _playerShapes.clear();

        ConfigurationSection shapesSection = root.getConfigurationSection("shapes");
        if (shapesSection != null) {
            for (String shapeName : shapesSection.getKeys(false)) {
                if (getCreatureShape(shapeName) != null) {
                    logger.warning("A shape called " + shapeName + " already exists and can't be redefined.");
                } else {
                    CreatureShape shape = CreatureShape.loadFromSection(shapesSection.getConfigurationSection(shapeName), logger);
                    if (shape == null) {
                        logger.warning("Shape " + shapeName + " was not defined, due to errors in the configuration.");
                    } else {
                        _shapes.put(shapeName.toLowerCase(), shape);
                    }
                }
            }
        }

        ConfigurationSection creaturesSection = root.getConfigurationSection("creatures");
        if (creaturesSection != null) {
            for (String creatureName : creaturesSection.getKeys(false)) {
                if (isValidCreatureType(creatureName)) {
                    // Prevent (inadvertent) redefinition of types.
                    logger.warning("A creature called " + creatureName + " already exists and can't be redefined.");
                } else {
                    CreatureType type = CreatureType.loadFromSection(creaturesSection.getConfigurationSection(creatureName), logger);
                    if (type == null) {
                        logger.warning("Creature " + creatureName + " was not defined, due to errors in the configuration.");
                    } else {
                        if (creatureName.equals(type.getCreatureType())) {
                            // Prevent infinite recursion in spawnCreature().
                            logger.warning("Creature " + creatureName + " cannot be defined in terms of itself.");
                        } else if (isValidCreatureType(type.getCreatureType())) {
                            _types.put(creatureName.toLowerCase(), type);
                        } else {
                            logger.warning("Can't define creature " + type.getName() +
                                           " because we can't spawn a " + type.getCreatureType());
                        }
                    }
                }
            } // for
        }

        ConfigurationSection playersSection = root.getConfigurationSection("players");
        if (playersSection != null) {
            for (String playerName : playersSection.getKeys(false)) {
                if (getPlayerCreature(playerName) != null) {
                    logger.warning("A player creature called " + playerName + " already exists and can't be redefined.");
                } else {
                    ConfigurationSection player = playersSection.getConfigurationSection(playerName);

                    // If a specific creature type to spawn is not specified, it
                    // defaults to a type with the same name as the player (if
                    // that exists).
                    String spawn = player.getString("spawn", playerName);
                    if (!isValidCreatureType(spawn)) {
                        logger.warning("Can't define player " + playerName +
                                       " because there is no creature type named " + spawn);
                    } else {
                        List<String> shapeNameList = player.getStringList("shapes");
                        ArrayList<CreatureShape> shapes = new ArrayList<CreatureShape>();
                        if (shapeNameList != null) {
                            for (String shapeName : shapeNameList) {
                                CreatureShape shape = getCreatureShape(shapeName);
                                if (shape == null) {
                                    logger.warning("Player " + playerName +
                                                   " references undefined shape " + shapeName);
                                } else {
                                    shapes.add(shape);
                                }
                            } // for

                            if (shapes.size() == 0) {
                                logger.warning("Player " + playerName +
                                               " can only be spawned by command because no shapes have been listed.");
                            }
                            _playerShapes.put(playerName.toLowerCase(), shapes);
                            _playerCreatures.put(playerName.toLowerCase(), spawn);
                        }
                    }
                } // if defining
            } // for
        }
    } // load

    // ------------------------------------------------------------------------
    /**
     * Print a human-readable list of the configured shapes, creature types and
     * player-name-specific creatures to the command sender.
     *
     * @param sender the agent requesting the listing.
     */
    public void listConfiguration(CommandSender sender) {
        StringBuilder message = new StringBuilder();
        message.append(ChatColor.GOLD);
        message.append("Shapes:");
        message.append(ChatColor.YELLOW);
        for (CreatureShape shape : _shapes.values()) {
            message.append(' ');
            message.append(shape.getName());
        }
        sender.sendMessage(message.toString());

        message.setLength(0);
        message.append(ChatColor.GOLD);
        message.append("Creatures:");
        message.append(ChatColor.YELLOW);
        for (CreatureType creature : _types.values()) {
            message.append(' ');
            message.append(creature.getName());
        }
        sender.sendMessage(message.toString());

        message.setLength(0);
        message.append(ChatColor.GOLD);
        message.append("Players:");
        message.append(ChatColor.YELLOW);
        for (String player : _playerCreatures.keySet()) {
            message.append(' ');
            message.append(player);
        }
        sender.sendMessage(message.toString());
    } // listConfiguration

    // ------------------------------------------------------------------------
    /**
     * Return the {@link CreatureShape} with the specified name in the
     * configuration, or null if not found.
     *
     * @param name the case insensitive shape name.
     * @return the {@link CreatureShape} with the specified name in the
     *         configuration, or null if not found.
     */
    public CreatureShape getCreatureShape(String name) {
        return _shapes.get(name.toLowerCase());
    }

    // ------------------------------------------------------------------------
    /**
     * Return the {@link CreatureShape} representing the shape used to summon a
     * creature by placing the specified item.
     *
     * The placed item must be named (by an anvil) and the shape and type of the
     * blocks around it must match one of those specified in the configuration.
     *
     * @param loc the location where the triggering item is placed.
     * @param placedItem the item to be tested as a trigger of creature
     *        summoning.
     * @return the {@link CreatureShape} of the creature that would be created,
     *         or null if no creature would be created.
     */
    public CreatureShape getCreatureShape(Location loc, ItemStack placedItem) {
        // Linear search probably doesn't matter. How often do you place
        // explicitly named blocks?
        for (CreatureShape shape : _shapes.values()) {
            if (shape.isComplete(loc, placedItem.getType())) {
                return shape;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------------
    /**
     * Return the CreatureType identified by the specified name, or null if not
     * found.
     *
     * Note that default Minecraft creatures, or the custom creatures defined by
     * the PredefinedCreature enum will not have a corresponding CreatureType
     * instance. The purpose of the CreatureType instance is to apply overrides
     * to the defaults.
     *
     * @param name the case-insensitive creature type name.
     * @return the CreatureType identified by the specified name, or null if not
     *         found.
     */
    public CreatureType getCreatureType(String name) {
        return _types.get(name.toLowerCase());
    }

    // ------------------------------------------------------------------------
    /**
     * Return the specific creature type that will be spawned when the named
     * player is summoned.
     *
     * @param playerName the name of the player whose custom creature type name
     *        will be returned.
     * @return the specific creature type that will be spawned when the named
     *         player is summoned; guaranteed non-null.
     */
    public String getPlayerCreature(String playerName) {
        return _playerCreatures.get(playerName.toLowerCase());
    }

    // ------------------------------------------------------------------------
    /**
     * Return the specific shapes that can summon the specified player.
     *
     * @param playerName the name of the player for whom summoning shapes will
     *        be returned.
     * @return the specific shapes that can summon the specified player; or null
     *         if not set.
     */
    public ArrayList<CreatureShape> getPlayerShapes(String playerName) {
        return _playerShapes.get(playerName.toLowerCase());
    }

    // ------------------------------------------------------------------------
    /**
     * Return true if the specified name signifies a vanilla Minecraft creature
     * (as in known to EntityType) or one of the custom values defined by
     * PredefinedCreature.
     *
     * @param name the case-insensitive creature type.
     * @return true if the creature type is "vanilla", as opposed to defined in
     *         the Doppelganger configuration file.
     */

    public static boolean isVanillaCreatureType(String name) {
        return PredefinedCreature.fromName(name) != null || EntityType.fromName(name) != null;
    }

    // ------------------------------------------------------------------------
    /**
     * Return true if the specified creature is a valid EntityType value or a
     * supported custom creature name.
     *
     * @param creatureType the case-insensitive custom or vanilla creature type.
     * @return true if the specified living entity is a valid EntityType value
     *         or a supported custom creature name.
     */
    public boolean isValidCreatureType(String creatureType) {
        if (getCreatureType(creatureType) != null ||
            PredefinedCreature.fromName(creatureType) != null) {
            return true;
        } else {
            EntityType entityType = EntityType.fromName(creatureType);
            return entityType != null && LivingEntity.class.isAssignableFrom(entityType.getEntityClass());
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Spawn a living entity of the specified type.
     *
     * This method originally returned a Creature, but Bats are Ambient mobs.
     * The next common base class is LivingEntity.
     *
     * If the creature type has a mask, then the creature will wear that
     * player's head, irrespective of the creature's name. If a name is not
     * specified, then the default name from the configuration will be used. If
     * the original name specified is non-empty, or a valid default is provided,
     * the name plate will be displayed.
     *
     * @param creatureType the EntityType.getName() value specifying the
     *        creature type; case-insensitive, or PredefinedCreature.name().
     *        Null or the empty string will result in no spawned creature.
     * @param loc the spawn location (block above ground level).
     * @param name the custom name to assign and display; if null/empty, the
     *        default name from the creature type is used.
     * @param plugin the Plugin, used to schedule future events for special
     *        effects.
     * @return the spawned LivingEntity, or null if nothing was spawned.
     */
    protected LivingEntity spawnCreature(String creatureType, Location loc, String name, Doppelganger plugin) {
        // Spawn the entity.
        LivingEntity livingEntity = null;

        CreatureType type = getCreatureType(creatureType);
        if (type != null) {
            type.doSpawnEffects(plugin, loc);
            type.spawnEscorts(plugin, loc);

            // The creature is recursively defined in terms of spawning another
            // creature and customising that.
            livingEntity = spawnCreature(type.getCreatureType(), loc, null, plugin);
            if (livingEntity != null) {
                type.customise(livingEntity);

                // Spawn the mount if possible.
                if (type.getMount() != null && isValidCreatureType(type.getMount())) {
                    LivingEntity mount = spawnCreature(type.getMount(), loc, null, plugin);
                    mount.setPassenger(livingEntity);
                }
            }
        } else {
            // creatureType refers to either a known EntityType name, or one of
            // the special PredefinedCreature enum values.
            if (creatureType != null && creatureType.length() != 0) {
                PredefinedCreature predefined = PredefinedCreature.fromName(creatureType);
                if (predefined != null) {
                    livingEntity = predefined.spawn(loc);
                } else {
                    EntityType entityType = EntityType.fromName(creatureType);
                    if (entityType != null && entityType != EntityType.UNKNOWN) {
                        Entity entity = loc.getWorld().spawnEntity(loc, entityType);
                        if (entity instanceof LivingEntity) {
                            livingEntity = (LivingEntity) entity;
                        }
                    }
                }
            }
        }

        // Whether a special type or not, name it.
        if (livingEntity != null) {
            // Use the configured default name if no name is specified.
            String usedName = ((name == null || name.length() == 0) && type != null)
                                                                                     ? type.getDefaultName() : name;
            if (usedName != null && usedName.length() != 0) {
                // TODO: Allow custom prefix and/or suffix.
                // TODO: Possibly allow prefix/suffix to indicate creator/owner
                // of creature.
                livingEntity.setCustomName(usedName);
                livingEntity.setCustomNameVisible(true);
            }

            // Make the doppelganger wear the player head or type-specific mask,
            // unless blocked by the keephelmet setting.
            if (type == null || !type.getKeepHelmet()) {
                String playerNameOfHead = (type != null && type.getMask() != null)
                                                                                   ? type.getMask() : usedName;
                if (playerNameOfHead != null && playerNameOfHead.length() != 0) {
                    setPlayerHead(livingEntity, playerNameOfHead);
                }
            }
        }

        // Players should not be able to get a doppelganger's head (or other
        // gear) just by dropping items near it.
        livingEntity.setCanPickupItems(false);
        return livingEntity;
    } // spawnCreature

    // ------------------------------------------------------------------------
    /**
     * Ensure that the doppelganger is wearing the specified player's head.
     *
     * If the creature was configured to be wearing a skull as a helmet,
     * customise that skull item so that settings from the configuration are
     * retained.
     *
     * @param doppelganger the creature.
     * @param name the name of the player whose head will be worn.
     */
    protected static void setPlayerHead(LivingEntity doppelganger, String name) {
        ItemStack helmet = doppelganger.getEquipment().getHelmet();
        if (helmet == null || helmet.getType() != Material.PLAYER_HEAD) {
            helmet = new ItemStack(Material.PLAYER_HEAD, 1);
        }

        SkullMeta meta = (SkullMeta) helmet.getItemMeta();
        meta.setOwner(name);
        helmet.setItemMeta(meta);
        // Player heads are damage value 3.
        helmet.setDurability((short) 3);
        doppelganger.getEquipment().setHelmet(helmet);
    } // setPlayerHead

    // ------------------------------------------------------------------------
    /**
     * Map from lower case shape name to {@link CreatureShape} instance.
     *
     * Use a LinkedHashMap to preserve the ordering defined in the configuration
     * file. That way earlier entries have precedence over later ones.
     */
    protected LinkedHashMap<String, CreatureShape> _shapes = new LinkedHashMap<String, CreatureShape>();

    /**
     * Map from lower case creature type name to {@link CreatureType} instance.
     */
    protected HashMap<String, CreatureType> _types = new HashMap<String, CreatureType>();

    /**
     * Map from lower case player name to {@link CreatureType} name.
     */
    protected HashMap<String, String> _playerCreatures = new HashMap<String, String>();

    /**
     * Map from lower case player name to list of {@link CreatureShape}s
     * describing the shapes that can be built to summon that specific player.
     */
    protected HashMap<String, ArrayList<CreatureShape>> _playerShapes = new HashMap<String, ArrayList<CreatureShape>>();

} // class CreatureFactory
