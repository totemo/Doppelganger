package io.github.totemo.doppelganger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.amoebaman.kitmaster.utilities.CommandController;

// ----------------------------------------------------------------------------
/**
 * Plugin class.
 */
public class Doppelganger extends JavaPlugin implements Listener
{
  // --------------------------------------------------------------------------
  /**
   * Return the configuration.
   * 
   * @return the configuration.
   */
  public Configuration getConfiguration()
  {
    return _configuration;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the {@link CreatureFactory}.
   * 
   * @return the {@link CreatureFactory}.
   */
  public CreatureFactory getCreatureFactory()
  {
    return _creatureFactory;
  }

  // --------------------------------------------------------------------------

  @Override
  public void onEnable()
  {
    // Saves only if config.yml doesn't exist.
    saveDefaultConfig();
    _configuration.load();

    getServer().getPluginManager().registerEvents(this, this);

    // The Plugin.getLogger() (used by help) is null at Doppelganger
    // construction time.
    if (_commands == null)
    {
      _commands = new Commands(this);
    }
    CommandController.registerCommands(this, _commands);
  }

  // --------------------------------------------------------------------------
  /**
   * Event handler for placing blocks.message
   * 
   * Checks that a named item is stacked on a configured shape made of blocks of
   * the requisite material.
   * 
   * @param event the event.
   */
  @EventHandler(ignoreCancelled = true)
  public void onBlockPlace(BlockPlaceEvent event)
  {
    ItemStack placedItem = event.getItemInHand();
    // Ignore named hoes tilling soil by checking if the item is a block.
    if (!placedItem.hasItemMeta() || !placedItem.getType().isBlock())
    {
      return;
    }

    ItemMeta meta = placedItem.getItemMeta();
    if (meta.hasDisplayName())
    {
      String doppelgangerName = meta.getDisplayName();
      Matcher nameMatcher = _namePattern.matcher(doppelgangerName);
      if (_configuration.warnOnInvalidName() && !_configuration.isArbitraryNameAllowed() && !nameMatcher.matches())
      {
        event.getPlayer().sendMessage(
          ChatColor.DARK_RED + "\"" + doppelgangerName + "\" is not a valid player name.");
      }
      else
      {
        World world = event.getPlayer().getWorld();
        Location loc = event.getBlock().getLocation();

        // Is a specific shape required to spawn a doppelganger of this name?
        ArrayList<CreatureShape> shapes = _creatureFactory.getPlayerShapes(doppelgangerName);
        if (shapes != null)
        {
          // Search the shapes associated with the specific player name.
          CreatureShape shape = null;
          for (CreatureShape tryShape : shapes)
          {
            if (tryShape.isComplete(loc, placedItem.getTypeId()))
            {
              if (tryShape.hasBorder(loc))
              {
                shape = tryShape;
              }
              else
              {
                event.getPlayer().sendMessage(ChatColor.YELLOW + "You need a one block gap horizontally around the shape.");
              }
              break;
            }
          } // for

          if (shape == null)
          {
            event.getPlayer().sendMessage(ChatColor.YELLOW + "That's not how you summon " + doppelgangerName + ".");
          }
          else
          {
            doDoppelganger(doppelgangerName, _creatureFactory.getPlayerCreature(doppelgangerName), shape, event);
          }
        }
        else
        {
          // Generic case where the doppelganger name doesn't matter.
          // Check whether there is a complete creature under the trigger block.
          CreatureShape shape = _creatureFactory.getCreatureShape(loc, event.getPlayer().getItemInHand());
          if (shape != null)
          {
            if (shape.hasBorder(loc))
            {
              String creatureType = shape.chooseCreatureType();
              if (_creatureFactory.isValidCreatureType(creatureType))
              {
                doDoppelganger(doppelgangerName, creatureType, shape, event);
              }
              else
              {
                getLogger().warning(
                  String.format(
                    Locale.US,
                    "Player %s tried to spawn a doppelganger named %s at (%g,%g,%g) in %s of invalid type %s by building a %s.",
                    event.getPlayer().getName(), doppelgangerName, loc.getX(), loc.getY(), loc.getZ(),
                    world.getName(), creatureType, shape.getName()));
              }
            }
            else
            {
              event.getPlayer().sendMessage(ChatColor.YELLOW + "You need a one block gap horizontally around the shape.");
            }
          }
        }
      } // if name is allowed
    }
  } // onBlockPlace

  // --------------------------------------------------------------------------
  /**
   * Vanilla Minecraft doesn't always drop equipment when the drop chance is 1.0
   * (or more). Try to work around that by moving the equipment into the drops
   * if it is not already there.
   * 
   * In Bukkit versions prior to 1.7.9, the equipment was not part of the drops
   * list in the EntityDeathEvent. Bukkit fixed that issue with the API, but had
   * to retain vanilla's handling of drop probabilities, which is still faulty.
   * 
   * This handler will process any entity death, but naturally spawned monsters
   * probably won't have a (near) 1.0 drop chance for the their equipment, and
   * in any case the relocation of the equipment to drops should be benign.
   */
  @EventHandler(ignoreCancelled = true)
  public void onEntityDeath(EntityDeathEvent event)
  {
    final float NEAR_UNITY = 0.999f;
    boolean forcedDrops = false;
    if (event.getEntity() instanceof Creature)
    {
      EntityEquipment equipment = event.getEntity().getEquipment();
      List<ItemStack> drops = event.getDrops();
      if (equipment.getHelmetDropChance() > NEAR_UNITY)
      {
        forcedDrops = true;
        ItemStack helmet = equipment.getHelmet();
        if (helmet != null && !drops.contains(helmet))
        {
          drops.add(helmet);
          equipment.setHelmet(null);
        }
      }
      if (equipment.getChestplateDropChance() > NEAR_UNITY)
      {
        forcedDrops = true;
        ItemStack chestplate = equipment.getChestplate();
        if (chestplate != null && !drops.contains(chestplate))
        {
          drops.add(chestplate);
          equipment.setChestplate(null);
        }
      }
      if (equipment.getLeggingsDropChance() > NEAR_UNITY)
      {
        forcedDrops = true;
        ItemStack leggings = equipment.getLeggings();
        if (leggings != null && !drops.contains(leggings))
        {
          drops.add(leggings);
          equipment.setLeggings(null);
        }
      }
      if (equipment.getBootsDropChance() > NEAR_UNITY)
      {
        forcedDrops = true;
        ItemStack boots = equipment.getBoots();
        if (boots != null && !drops.contains(boots))
        {
          drops.add(boots);
          equipment.setBoots(null);
        }
      }
      if (equipment.getItemInHandDropChance() > NEAR_UNITY)
      {
        forcedDrops = true;
        ItemStack itemInHand = equipment.getItemInHand();
        if (itemInHand != null && !drops.contains(itemInHand))
        {
          drops.add(itemInHand);
          equipment.setItemInHand(null);
        }
      }
    }

    // If a unity drop chance was specified, it's probably a Doppelganger.
    // Also require a custom name, since 'special' mobs that pick up items
    // will always drop them too.
    // Log the drops for verification purposes.
    if (forcedDrops && event.getEntity().getCustomName() != null)
    {
      Location loc = event.getEntity().getLocation();
      StringBuilder drops = new StringBuilder();
      drops.append("At (").append(loc.getBlockX()).append(',');
      drops.append(loc.getBlockY()).append(',');
      drops.append(loc.getBlockZ()).append(") drops:");
      for (ItemStack item : event.getDrops())
      {
        drops.append(' ');
        drops.append(item);
      }
      getLogger().info(drops.toString());
    }
  } // onEntityDeath

  // --------------------------------------------------------------------------
  /**
   * Spawn a doppelganger of the specified type and name.
   * 
   * @param creatureType the type of creature to spawn.
   * @param name the name to show on the name tag. This is also the name of the
   *          player whose head will be worn, unless the creature type includes
   *          a specific mask override. If the name is null or the empty string,
   *          the name defaults to the default name set in the creature type.
   *          If, after the default name is taken into account, the name is
   *          still null or empty, no name tag will be set and no player head
   *          will be worn.
   * @param loc the Location on the ground where the creature will spawn.
   * @return the spawned LivingEntity, or null if it could not be spawned.
   */
  public LivingEntity spawnDoppelganger(String creatureType, String name, Location loc)
  {
    return _creatureFactory.spawnCreature(creatureType, loc, name, this);
  }

  // --------------------------------------------------------------------------
  /**
   * Cancel the original block placement, vaporise the golem blocks and spawn a
   * named LivingEntity of the specified type.
   * 
   * @param doppelgangerName the name of spawned creature.
   * @param creatureType the name of the type of creature to spawn.
   * @param shape the shape of the golem blocks.
   * @param event the BlockPlaceEvent that triggered this.
   */
  protected void doDoppelganger(String doppelgangerName, String creatureType, CreatureShape shape, BlockPlaceEvent event)
  {
    Location loc = event.getBlock().getLocation();
    ItemStack placedItem = event.getItemInHand();

    getLogger().info(
      String.format(Locale.US,
        "Player %s spawned a %s named %s at (%g,%g,%g) in %s by building a %s.",
        event.getPlayer().getName(), creatureType, doppelgangerName,
        loc.getX(), loc.getY(), loc.getZ(), loc.getWorld().getName(), shape.getName()));

    // Cancel placement of the trigger.
    event.setCancelled(true);

    // Remove one trigger item from the item stack.
    if (placedItem.getAmount() > 1)
    {
      placedItem.setAmount(placedItem.getAmount() - 1);
      event.getPlayer().setItemInHand(placedItem);
    }
    else
    {
      event.getPlayer().setItemInHand(null);
    }

    // Vaporise the shape blocks.
    shape.vaporise(loc);

    // Add 0.5 to X and Z so the creature is not on the block boundary.
    Location groundLocation = loc.clone();
    groundLocation.add(0.5, shape.getGroundOffset(), 0.5);
    // TODO: allow a customisable offset above the computed ground position.

    // The doppelganger mob.
    LivingEntity doppelganger = spawnDoppelganger(creatureType, doppelgangerName, groundLocation);
    if (doppelganger == null)
    {
      // If the creature type is invalid, it is a configuration error. The shape
      // items are already lost. Since Configuration.isValidCreatureType() was
      // called prior to entering doDoppelganger(), this shouldn't happen.
      getLogger().severe("Could not spawn " + creatureType);
    }
    else if (doppelganger instanceof Creature)
    {
      // If we can, make the doppelganger the players *problem*.
      ((Creature) doppelganger).setTarget(event.getPlayer());
    }
  } // doDoppelganger

  // --------------------------------------------------------------------------
  /**
   * Pattern describing allowable doppelganger names.
   */
  protected Pattern         _namePattern     = Pattern.compile("^\\w+$");

  /**
   * Handles creation of creatures.
   */
  protected CreatureFactory _creatureFactory = new CreatureFactory();

  /**
   * Configuration management.
   */
  protected Configuration   _configuration   = new Configuration(this, _creatureFactory);

  /**
   * Handles the command line.
   */
  protected Commands        _commands;
} // class Doppelganger
