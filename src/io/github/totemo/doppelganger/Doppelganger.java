package io.github.totemo.doppelganger;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.amoebaman.kitmaster.utilities.CommandController;

// ----------------------------------------------------------------------------
/**
 * Plugin class.
 */
public class Doppelganger extends JavaPlugin implements Listener
{
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
    if (!placedItem.hasItemMeta())
    {
      return;
    }

    ItemMeta meta = placedItem.getItemMeta();
    if (meta.hasDisplayName())
    {
      String doppelgangerName = meta.getDisplayName();
      Matcher nameMatcher = _namePattern.matcher(doppelgangerName);
      if (!_configuration.isArbitraryNameAllowed() && !nameMatcher.matches())
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
   * Return the {@link CreatureFactory}.
   * 
   * @return the {@link CreatureFactory}.
   */
  public CreatureFactory getCreatureFactory()
  {
    return _creatureFactory;
  }

  // --------------------------------------------------------------------------
  /**
   * Spawn a doppelganger of the specified type and name.
   * 
   * @param creatureType the type of creature to spawn.
   * @param name the name to show on the name tag. This is also the name of the
   *          player whose head will be worn, unless the creature type includes
   *          a specific mask override. If the name is null or the empty string,
   *          no name tag will be set and no player head will be worn.
   * @param loc the Location on the ground where the creature will spawn.
   * @return the spawned LivingEntity, or null if it could not be spawned.
   */
  public LivingEntity spawnDoppelganger(String creatureType, String name, Location loc)
  {
    // If a custom creature, do configured special effects.
    LivingEntity doppelganger = _creatureFactory.spawnCreature(creatureType, loc, name, this);
    if (doppelganger != null)
    {
      // Make the doppelganger wear the player head or type-specific mask.
      CreatureType type = _creatureFactory.getCreatureType(creatureType);
      String playerNameOfHead = (type != null && type.getMask() != null) ? type.getMask() : name;
      if (playerNameOfHead != null && playerNameOfHead.length() != 0)
      {
        setPlayerHead(doppelganger, playerNameOfHead);
      }
    }

    return doppelganger;
  } // spawnDoppelganger

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
   * Ensure that the doppelganger is wearing the specified player's head.
   * 
   * If the creature was configured to be wearing a skull as a helmet, customise
   * that skull item so that settings from the configuation are retained.
   * 
   * @param doppelganger the creature.
   * @param name the name of the player whose head will be worn.
   */
  protected static void setPlayerHead(LivingEntity doppelganger, String name)
  {
    ItemStack helmet = doppelganger.getEquipment().getHelmet();
    if (helmet == null || helmet.getType() != Material.SKULL_ITEM)
    {
      helmet = new ItemStack(Material.SKULL_ITEM, 1);
    }

    SkullMeta meta = (SkullMeta) helmet.getItemMeta();
    meta.setOwner(name);
    helmet.setItemMeta(meta);
    // Player heads are damage value 3.
    helmet.setDurability((short) 3);
    doppelganger.getEquipment().setHelmet(helmet);
  } // setPlayerHead

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
