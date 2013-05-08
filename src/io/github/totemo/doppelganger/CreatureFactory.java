package io.github.totemo.doppelganger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.inventory.ItemStack;

// --------------------------------------------------------------------------
/**
 * Manages known creature types and creates them on demand.
 */
public class CreatureFactory
{
  // --------------------------------------------------------------------------
  /**
   * Custom creature type signifying a wither skeleton.
   */
  public static final String WITHER_SKELETON = "WitherSkeleton";

  /**
   * Custom creature type signifying a saddled pig.
   */
  public static final String SADDLED_PIG     = "SaddledPig";

  // --------------------------------------------------------------------------
  /**
   * Load the creature shapes and types from the configuration file.
   * 
   * @param root the root of the configuration hierarchy.
   * @param logger the Logger.
   */
  public void load(ConfigurationSection root, Logger logger)
  {
    ConfigurationSection shapesSection = root.getConfigurationSection("shapes");
    _shapes.clear();
    for (String shapeName : shapesSection.getKeys(false))
    {
      CreatureShape shape = CreatureShape.loadFromSection(shapesSection.getConfigurationSection(shapeName), logger);
      _shapes.put(shapeName, shape);
      // shape.dump(_plugin.getLogger());
    }

    ConfigurationSection creaturesSection = root.getConfigurationSection("creatures");
    _types.clear();
    for (String creatureName : creaturesSection.getKeys(false))
    {
      if (isValidCreatureType(creatureName))
      {
        // Prevent (inadvertent) redefinition of types.
        logger.warning("A creature called " + creatureName + " already exists and can't be redefined.");
      }
      else
      {
        CreatureType type = CreatureType.loadFromSection(creaturesSection.getConfigurationSection(creatureName), logger);
        if (creatureName.equals(type.getCreatureType()))
        {
          // Prevent infinite recursion in spawnCreature().
          logger.warning("Creature " + creatureName + " cannot be defined in terms of itself.");
        }
        else if (isValidCreatureType(type.getCreatureType()))
        {
          _types.put(creatureName, type);
        }
        else
        {
          logger.warning("Can't define creature " + type.getName() +
                         " because we can't spawn a " + type.getCreatureType());
        }
      }
    } // for

    ConfigurationSection playersSection = root.getConfigurationSection("players");
    _playerCreatures.clear();
    _playerShapes.clear();
    for (String playerName : playersSection.getKeys(false))
    {
      ConfigurationSection player = playersSection.getConfigurationSection(playerName);

      // If a specific creature type to spawn is not specified, it defaults to
      // a type with the same name as the player (if that exists).
      String spawn = player.getString("spawn", playerName);
      if (!isValidCreatureType(spawn))
      {
        logger.warning("Can't define player " + playerName +
                       " because there is no createure type named " + spawn);
      }
      else
      {
        List<String> shapeNameList = player.getStringList("shapes");
        ArrayList<CreatureShape> shapes = new ArrayList<CreatureShape>();
        if (shapeNameList != null)
        {
          for (String shapeName : shapeNameList)
          {
            CreatureShape shape = getCreatureShape(shapeName);
            if (shape == null)
            {
              logger.warning("Player " + playerName +
                             " references undefined shape " + shapeName);
            }
            else
            {
              shapes.add(shape);
            }
          } // for

          if (shapes.size() == 0)
          {
            logger.warning("Player " + playerName +
                           " can only be spawned by command because no shapes have been listed.");
          }
          _playerShapes.put(playerName, shapes);
          _playerCreatures.put(playerName, spawn);
        }
      }
    } // for
  } // load

  // --------------------------------------------------------------------------
  /**
   * Return the {@link CreatureShape} with the specified name in the
   * configuration, or null if not found.
   * 
   * @return the {@link CreatureShape} with the specified name in the
   *         configuration, or null if not found.
   */
  public CreatureShape getCreatureShape(String name)
  {
    return _shapes.get(name);
  }

  // --------------------------------------------------------------------------
  /**
   * Return the {@link CreatureShape} representing the shape used to summon a
   * creature by placing the specified item.
   * 
   * The placed item must be named (by an anvil) and the shape and type of the
   * blocks around it must match one of those specified in the configuration.
   * 
   * @param world the World.
   * @param loc the location where the triggering item is placed.
   * @param placedItem the item to be tested as a trigger of creature summoning.
   * @return the {@link CreatureShape} of the creature that would be created, or
   *         null if no creature would be created.
   */
  public CreatureShape getCreatureShape(World world, Location loc, ItemStack placedItem)
  {
    // Linear search probably doesn't matter. How often do you place explicitly
    // named blocks?
    for (CreatureShape shape : _shapes.values())
    {
      if (shape.isComplete(world, loc, placedItem.getTypeId()))
      {
        return shape;
      }
    }
    return null;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the CreatureType identified by the specified name, or null if not
   * found.
   * 
   * Note that default Minecraft creatures, or the custom creatures
   * WITHER_SKELETON (a reconfigured Skeleton) and SADDLED_PIG (a reconfigured
   * Pig) will not have a corresponding CreatureType instance. The purpose of
   * the CreatureType instance is to apply overrides to the defaults.
   * 
   * @return the CreatureType identified by the specified name, or null if not
   *         found.
   */
  public CreatureType getCreatureType(String name)
  {
    return _types.get(name);
  }

  // --------------------------------------------------------------------------
  /**
   * Return the specific creature type that will be spawned when the named
   * player is summoned.
   * 
   * @return the specific creature type that will be spawned when the named
   *         player is summoned; guaranteed non-null.
   */
  public String getPlayerCreature(String playerName)
  {
    return _playerCreatures.get(playerName);
  }

  // --------------------------------------------------------------------------
  /**
   * Return the specific shapes that can summon the specified player.
   * 
   * @return the specific shapes that can summon the specified player; or null
   *         if not set.
   */
  public ArrayList<CreatureShape> getPlayerShapes(String playerName)
  {
    return _playerShapes.get(playerName);
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if the specified creature is a valid EntityType value or a
   * supported custom creature name.
   * 
   * @return true if the specified living entity is a valid EntityType value or
   *         a supported custom creature name.
   */
  public boolean isValidCreatureType(String creatureType)
  {
    if (_types.get(creatureType) != null ||
        creatureType.equalsIgnoreCase(WITHER_SKELETON) ||
        creatureType.equalsIgnoreCase(SADDLED_PIG))
    {
      return true;
    }
    else
    {
      EntityType entityType = EntityType.fromName(creatureType);
      return entityType != null && LivingEntity.class.isAssignableFrom(entityType.getEntityClass());
    }
  }

  // --------------------------------------------------------------------------
  /**
   * Create a creature of the specified type, with the specified name.
   * 
   * @param creatureType the EntityType.getName() value specifying the creature
   *          type; case-insensitive, or a supported custom name.
   * @param world the World.
   * @param loc the spawn location (block above ground level).
   * @param name the custom name to assign and display.
   * @return the spawned LivingEntity, or null if nothing was spawned.
   */
  protected LivingEntity spawnNamedCreature(String creatureType, World world, Location loc, String name)
  {
    LivingEntity creature = spawnCreature(creatureType, world, loc);
    if (creature != null)
    {
      // TODO: Allow custom prefix and/or suffix.
      // TODO: Possibly allow prefix/suffix to indicate original creator, e.g.
      // "totemo's pet Notch"
      creature.setCustomName(name);
      creature.setCustomNameVisible(true);
    }
    return creature;
  }

  // --------------------------------------------------------------------------
  /**
   * Spawn a living entity of the specified type.
   * 
   * This method originally returned a Creature, but Bats are Ambient mobs. The
   * next common base class is LivingEntity.
   * 
   * @param creatureType the EntityType.getName() value specifying the creature
   *          type; case-insensitive, or "WitherSkeleton". Null or the empty
   *          string will result in no spawned creature.
   * @param world the World.
   * @param loc the spawn location (block above ground level).
   * @return the spawned LivingEntity, or null if nothing was spawned.
   */
  protected LivingEntity spawnCreature(String creatureType, World world, Location loc)
  {
    // Spawn the entity.
    LivingEntity livingEntity = null;

    CreatureType type = getCreatureType(creatureType);
    if (type != null)
    {
      // The creature is recursively defined in terms of spawning another
      // creature and customising that.
      livingEntity = spawnCreature(type.getCreatureType(), world, loc);
      if (livingEntity != null)
      {
        type.customise(livingEntity);

        // Spawn the mount if possible.
        if (isValidCreatureType(type.getMount()))
        {
          LivingEntity mount = spawnCreature(type.getMount(), world, loc);
          mount.setPassenger(livingEntity);
        }
      }
    }
    else
    {
      // creatureType refers to either a known EntityType name, or one of the
      // special values WITHER_SKELETON or SADDLED_PIG.
      if (creatureType != null && creatureType.length() != 0)
      {
        if (creatureType.equalsIgnoreCase(WITHER_SKELETON))
        {
          Skeleton skeleton = world.spawn(loc, Skeleton.class);
          skeleton.setSkeletonType(SkeletonType.WITHER);
          livingEntity = skeleton;
        }
        else if (creatureType.equalsIgnoreCase(SADDLED_PIG))
        {
          Pig pig = world.spawn(loc, Pig.class);
          pig.setSaddle(true);
          livingEntity = pig;
        }
        else
        {
          EntityType entityType = EntityType.fromName(creatureType);
          if (entityType != null && entityType != EntityType.UNKNOWN)
          {
            Entity entity = world.spawnEntity(loc, entityType);
            if (entity instanceof LivingEntity)
            {
              livingEntity = (LivingEntity) entity;
            }
          }
        }
      }
    }
    return livingEntity;
  } // spawnCreature

  // --------------------------------------------------------------------------
  /**
   * List of {@link CreatureShape} instances.
   * 
   * Use a LinkedHashMap to preserve the ordering defined in the configuration
   * file. That way earlier entries have precedence over later ones.
   */
  protected LinkedHashMap<String, CreatureShape>      _shapes          = new LinkedHashMap<String, CreatureShape>();

  /**
   * Map from creature type name to {@link CreatureType} instance.
   */
  protected HashMap<String, CreatureType>             _types           = new HashMap<String, CreatureType>();

  /**
   * Map from player name to {@link CreatureType} name.
   */
  protected HashMap<String, String>                   _playerCreatures = new HashMap<String, String>();

  /**
   * Map from player name to list of {@link CreatureShape} describing the shapes
   * that can be built to summon that specific player.
   */
  protected HashMap<String, ArrayList<CreatureShape>> _playerShapes    = new HashMap<String, ArrayList<CreatureShape>>();

} // class CreatureFactory
