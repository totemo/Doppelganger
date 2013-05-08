package io.github.totemo.doppelganger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

// ----------------------------------------------------------------------------
/**
 * Describes a type of creature to be summoned, including its mob type, health,
 * armour and potion effect characteristics.
 */
public class CreatureType
{
  // --------------------------------------------------------------------------
  /**
   * Load this CreatureType from the specified section.
   * 
   * The expected format is:
   * 
   * <pre>
   * spawn: WitherSkeleton
   * despawns: false
   * health: 60
   * air: 72000
   * chestplate:
   * - item: 311
   *   damage: 3
   *   dropchance: 0.0
   * potions:
   * - type: invisibility
   * </pre>
   * 
   * @param name the name of the creature to load.
   * @param section the configuration section to load.
   * @param logger logs messages.
   * @return a new CreatureType instance, or null on error.
   */
  public static CreatureType loadFromSection(ConfigurationSection section, Logger logger)
  {
    String spawn = section.getString("spawn", "");
    String mount = section.getString("mount", "");
    String mask = section.getString("mask", null);
    boolean despawns = section.getBoolean("despawns", true);
    int health = section.getInt("health", 20);
    int air = section.getInt("air", 20 * 60);
    String soundName = section.getString("sound", "").toUpperCase();
    Sound sound = null;
    try
    {
      if (soundName.length() != 0)
      {
        sound = Sound.valueOf(soundName);
      }
    }
    catch (Exception ex)
    {
      logger.warning("Creature " + section.getName() + " has invalid sound " + soundName);
    }

    int minStrikes = section.getInt("lightning.min", 0);
    int maxStrikes = section.getInt("lightning.max", minStrikes);
    double strikeRange = section.getDouble("lightning.range", 2.0);
    int strikeDuration = section.getInt("lightning.duration", 30);
    CreatureType type = new CreatureType(section.getName(), spawn, mount, mask, health, air, despawns,
                                         sound, minStrikes, maxStrikes, strikeRange, strikeDuration);

    // Load up the potion effects.
    if (section.isList("potions"))
    {
      try
      {
        type.loadPotions(section.getMapList("potions"), logger);
      }
      catch (Exception ex)
      {
        logger.warning(ex.getClass().getName() + " loading potions for " + section.getName());
      }
    }

    // Load armour and weapon.
    type._helmet = loadItem(section.getConfigurationSection("helmet"), logger);
    type._chestPlate = loadItem(section.getConfigurationSection("chestplate"), logger);
    type._leggings = loadItem(section.getConfigurationSection("leggings"), logger);
    type._boots = loadItem(section.getConfigurationSection("boots"), logger);
    type._weapon = loadItem(section.getConfigurationSection("weapon"), logger);
    type._helmetDropChance = (float) section.getDouble("helmet.dropchance", 0.0f);
    type._chestPlateDropChance = (float) section.getDouble("chestplate.dropchance", 0.0f);
    type._leggingsDropChance = (float) section.getDouble("leggings.dropchance", 0.0f);
    type._bootsDropChance = (float) section.getDouble("boots.dropchance", 0.0f);
    type._weaponDropChance = (float) section.getDouble("weapon.dropchance", 0.0f);
    return type;
  } // loadFromSection

  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param name a unique identifier for this type.
   * @param creatureType the type of creature to spawn.
   * @param mount the type of creature that this creature is riding.
   * @param mask the name of the player whose head this creature should always
   *          wear (as a mask), irrespective of the player name it was summoned
   *          with.
   * @param health the maximum health of the creature in half-hearts.
   * @param air the lung capacity of the creature in ticks.
   * @param despawns true if the creature will despawn when the player is too
   *          far away.
   * @param sound sound played when spawning (can be null).
   * @param minStrikes minimum number of lightning strikes when spawned.
   * @param maxStrikes maximum number of lightning strikes when spawned.
   * @param strikeRange maximum distance of strikes from spawn location.
   * @param strikeDuration maximum period between spawning and last strike in
   *          ticks.
   */
  public CreatureType(String name, String creatureType, String mount, String mask,
                      int health, int air, boolean despawns, Sound sound,
                      int minStrikes, int maxStrikes, double strikeRange, int strikeDuration)
  {
    _name = name;
    _creatureType = creatureType;
    _mount = mount;
    _mask = mask;
    _health = health;
    _air = air;
    _despawns = despawns;
    _sound = sound;
    _minStrikes = minStrikes;
    _maxStrikes = maxStrikes;
    _strikeRange = strikeRange;
    _strikeDuration = strikeDuration;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the unique identifier of this creature.
   * 
   * @return the unique identifier of this creature.
   */
  public String getName()
  {
    return _name;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the name of the CreatureType instance upon which this instance is
   * based.
   * 
   * @return the name of the CreatureType instance upon which this instance is
   *         based.
   */
  public String getCreatureType()
  {
    return _creatureType;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the type of creature that this creature is riding.
   * 
   * @return the type of creature that this creature is riding; if not riding
   *         anthing, it is the empty string.
   */
  public String getMount()
  {
    return _mount;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the name of the player whose head this creature should always wear,
   * irrespective of the player name it was summoned with.
   * 
   * @return the name of the player whose head this creature should always wear,
   *         irrespective of the player name it was summoned with, or null if
   *         not set.
   */
  public String getMask()
  {
    return _mask;
  }

  // --------------------------------------------------------------------------
  /**
   * Do sound and damage-free lighting strike effects.
   * 
   * @param plugin the originating Plugin.
   * @param world the World.
   * @param loc the Location where the creature will spawn.
   */
  public void doSpawnEffects(Plugin plugin, World world, Location loc)
  {
    if (_sound != null)
    {
      world.playSound(loc, _sound, 1, 1);
    }

    int strikes = _minStrikes + (int) Math.round(Math.random() * (_maxStrikes - _minStrikes));
    if (strikes > 0)
    {
      // First strike is always immediate.
      world.strikeLightningEffect(loc);
      for (int i = 1; i < strikes; ++i)
      {
        scheduleRandomStrike(plugin, world, loc, _strikeRange, _strikeDuration);
      }
    }
  } // doSpawnEffects

  // --------------------------------------------------------------------------
  /**
   * Apply the custom attributes embodied by this type to the specified
   * creature.
   * 
   * @param entity the creature to customise.
   */
  public void customise(LivingEntity entity)
  {
    entity.setMaxHealth(_health);
    entity.setHealth(_health);
    entity.setMaximumAir(_air);
    entity.setRemoveWhenFarAway(_despawns);
    entity.addPotionEffects(_potions);
    entity.getEquipment().setHelmet(_helmet);
    entity.getEquipment().setHelmetDropChance(_helmetDropChance);
    entity.getEquipment().setChestplate(_chestPlate);
    entity.getEquipment().setChestplateDropChance(_chestPlateDropChance);
    entity.getEquipment().setLeggings(_leggings);
    entity.getEquipment().setLeggingsDropChance(_leggingsDropChance);
    entity.getEquipment().setBoots(_boots);
    entity.getEquipment().setBootsDropChance(_bootsDropChance);
    entity.getEquipment().setItemInHand(_weapon);
    entity.getEquipment().setItemInHandDropChance(_weaponDropChance);
  } // customise

  // --------------------------------------------------------------------------
  /**
   * Load the potion effects defined in the "potions" section of this
   * CreatureType's configuration
   * 
   * @param potions the "potions" ConfigurationSection.
   * @param logger logs messages.
   */
  protected void loadPotions(List<Map<?, ?>> potions, Logger logger)
  {
    try
    {
      for (int i = 0; i < potions.size(); ++i)
      {
        try
        {
          ConfigMap map = new ConfigMap(potions.get(i), logger, getName() + " potion " + i);
          String typeName = map.getString("type", null);
          if (typeName == null)
          {
            logger.warning("Potion " + i + " has no type specified.");
          }
          else
          {
            PotionEffectType type = PotionEffectType.getByName(typeName);
            if (type == null)
            {
              logger.warning(typeName + " is not a valid potion effect type.");
            }
            else
            {
              Integer duration = map.getInteger("duration", Integer.MAX_VALUE);
              Integer amplifier = map.getInteger("amplifier", 1);
              Boolean ambient = map.getBoolean("ambient", true);
              _potions.add(new PotionEffect(type, duration, amplifier, ambient));
            }
          }
        }
        catch (Exception ex)
        {
          logger.warning(ex.getClass().getName() + " defining potion effect for " + getName());
        }
      } // for
    }
    catch (Exception ex)
    {
      logger.warning(ex.getClass().getName() + " loading potons in " + getName());
    }
  } // loadPotions

  // --------------------------------------------------------------------------
  /**
   * Load an item (armour or weapon, with optional enchants) and return it as an
   * ItemStack.
   * 
   * @param section the configuration section to load from.
   * @param logger logs messages.
   */
  protected static ItemStack loadItem(ConfigurationSection section, Logger logger)
  {
    ItemStack item = null;
    if (section != null)
    {
      Material material = Material.getMaterial(section.getString("item", "").toUpperCase());
      if (material == null)
      {
        logger.warning(section.getCurrentPath() + " has invalid item type.");
      }
      else
      {
        int damage = section.getInt("damage", 0);
        item = new ItemStack(material, 1, (short) damage);

        if (section.isList("enchantments"))
        {
          try
          {
            List<Map<?, ?>> enchantments = section.getMapList("enchantments");
            for (int i = 0; i < enchantments.size(); ++i)
            {
              try
              {
                ConfigMap map = new ConfigMap(enchantments.get(i), logger,
                                              section.getCurrentPath() + " enchantment " + i);
                String typeName = map.getString("type", null);
                if (typeName == null)
                {
                  logger.warning("Enchantment " + i + " has no type specified.");
                }
                else
                {
                  Enchantment type = Enchantment.getByName(typeName.toUpperCase());
                  if (type == null)
                  {
                    logger.warning(typeName + " is not a valid enchantment type.");
                  }
                  else
                  {
                    item.addEnchantment(type, map.getInteger("level", 1));
                  }
                }
              }
              catch (Exception ex)
              {
                logger.warning(ex.getClass().getName() + " defining enchantment " +
                               i + " for " + section.getCurrentPath());
              }
            } // for
          }
          catch (Exception ex)
          {
            logger.warning(ex.getClass().getName() + " loading enchantments for " + section.getCurrentPath());
          }
        }
      }
    }
    return item;
  } // loadItem

  // --------------------------------------------------------------------------
  /**
   * Schedule a random, damage-free lighting strike effect within the specified
   * world.
   * 
   * @param plugin the originating Plugin.
   * @param world the World where the strike occurs.
   * @param centre the centre of the random coordinate range.
   * @param maxRange the maximum distance of the strike from centre on the X and
   *          Z axes.
   * @param maxDelay the maximum number of ticks to wait before the strike
   *          occurs.
   */
  protected static void scheduleRandomStrike(Plugin plugin, final World world, Location centre, double maxRange, long maxDelay)
  {
    long delay = Math.round(Math.random() * maxDelay);
    double range = maxRange * Math.random();
    double angle = 2.0 * Math.PI * Math.random();
    double dx = range * Math.cos(angle);
    double dz = range * Math.sin(angle);
    final Location loc = centre.clone();
    loc.add(dx, 0, dz);
    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
    {
      @Override
      public void run()
      {
        world.strikeLightningEffect(loc);
      }
    }, delay);
  } // scheduleRandomStrike

  // --------------------------------------------------------------------------
  /**
   * A unique identifier for this type of creature.
   */
  protected String                  _name;

  /**
   * The type of the creature to spawn, including custom types.
   */
  protected String                  _creatureType;

  /**
   * The type of creature that this creature is riding.
   */
  protected String                  _mount;

  /**
   * The name of the player whose head this creature should always wear,
   * irrespective of the player name it was summoned with.
   */
  protected String                  _mask;

  /**
   * Maximum health in half-hearts.
   */
  protected int                     _health;

  /**
   * The lung capacity of the creature in ticks.
   */
  protected int                     _air;

  /**
   * True if the creature will despawn when the player is too far away.
   */
  protected boolean                 _despawns;

  /**
   * The sound of this creature spawning.
   */
  protected Sound                   _sound;

  /**
   * Minimum number of lighting strikes on summoning.
   */
  protected int                     _minStrikes;

  /**
   * Maximum number of lighting strikes on summoning.
   */
  protected int                     _maxStrikes;

  /**
   * Maximum distance of lighting strikes from spawn point.
   */
  protected double                  _strikeRange;

  /**
   * Maximum period between spawning and last strike in ticks.
   */
  protected int                     _strikeDuration;

  /**
   * Potion effects to apply when the creature is spawned.
   */
  protected ArrayList<PotionEffect> _potions = new ArrayList<PotionEffect>();

  /**
   * Helmet.
   */
  protected ItemStack               _helmet;

  /**
   * Chestplate.
   */
  protected ItemStack               _chestPlate;

  /**
   * Leggings.
   */
  protected ItemStack               _leggings;

  /**
   * Boots.
   */
  protected ItemStack               _boots;

  /**
   * Item in hand.
   */
  protected ItemStack               _weapon;

  /**
   * Chance of dropping helmet, [0.0, 1.0].
   */
  protected float                   _helmetDropChance;

  /**
   * Chance of dropping chestplate, [0.0, 1.0].
   */
  protected float                   _chestPlateDropChance;

  /**
   * Chance of dropping leggings, [0.0, 1.0].
   */
  protected float                   _leggingsDropChance;

  /**
   * Chance of dropping boots, [0.0, 1.0].
   */
  protected float                   _bootsDropChance;

  /**
   * Chance of dropping item in hand, [0.0, 1.0].
   */
  protected float                   _weaponDropChance;
} // class CreatureType
