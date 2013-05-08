package io.github.totemo.doppelganger;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
  }

  // --------------------------------------------------------------------------
  /**
   * Event handler for placing blocks.message
   * 
   * Checks that a named item is stacked on a configured shape made of blocks of
   * the requisite material.
   */
  @EventHandler(ignoreCancelled = true)
  public void onBlockPlace(BlockPlaceEvent event)
  {
    ItemStack placedItem = event.getItemInHand();
    ItemMeta meta = placedItem.getItemMeta();
    if (meta.hasDisplayName())
    {
      String doppelgangerName = meta.getDisplayName();
      Matcher nameMatcher = _namePattern.matcher(doppelgangerName);
      if (!_configuration.isArbitraryNameAllowed() && !nameMatcher.matches())
      {
        event.getPlayer().sendMessage("\"" + doppelgangerName + "\" is not a valid player name.");
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
            if (tryShape.isComplete(world, loc, placedItem.getTypeId()))
            {
              shape = tryShape;
              break;
            }
          } // for

          if (shape == null)
          {
            event.getPlayer().sendMessage("That's not how you summon " + doppelgangerName + ".");
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
          CreatureShape shape = _creatureFactory.getCreatureShape(world, loc, event.getPlayer().getItemInHand());
          if (shape != null)
          {
            String creatureType = shape.chooseCreatureType();
            if (_creatureFactory.isValidCreatureType(creatureType))
            {
              doDoppelganger(doppelgangerName, creatureType, shape, event);
            }
            else
            {
              getLogger().warning(
                String.format(Locale.US,
                  "Player %s tried to spawn a doppelganger named %s at (%g,%g,%g) in %s of invalid type %s by building a %s.",
                  event.getPlayer().getName(), doppelgangerName, loc.getX(), loc.getY(), loc.getZ(),
                  world.getName(), creatureType, shape.getName()));
            }
          }
        }
      } // if name is allowed
    }
  } // onBlockPlace

  // --------------------------------------------------------------------------
  /**
   * Cancel the original block placement, vaporise the golem blocks and spawn a
   * named Creature of the specified type.
   * 
   * @param doppelgangerName the name of spawned creature.
   * @param creatureType the name of the type of creature to spawn.
   * @param shape the shape of the golem blocks.
   * @param event the BlockPlaceEvent that triggered this.
   */
  protected void doDoppelganger(String doppelgangerName, String creatureType, CreatureShape shape, BlockPlaceEvent event)
  {
    World world = event.getPlayer().getWorld();
    Location loc = event.getBlock().getLocation();
    ItemStack placedItem = event.getItemInHand();

    getLogger().info(
      String.format(Locale.US,
        "Player %s spawned a %s named %s at (%g,%g,%g) in %s by building a %s.",
        event.getPlayer().getName(), creatureType, doppelgangerName,
        loc.getX(), loc.getY(), loc.getZ(), world.getName(), shape.getName()));

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
    shape.vaporise(world, loc);

    // Add 0.5 to X and Z so the creature is not on the block boundary.
    Location groundLocation = loc.clone();
    groundLocation.add(0.5, shape.getGroundOffset(), 0.5);

    // If a custom creature, do configured special effects.
    CreatureType type = _creatureFactory.getCreatureType(creatureType);
    if (type != null)
    {
      type.doSpawnEffects(this, world, groundLocation);
    }

    // TODO: allow a customisable offset above the computed ground position.
    // The doppelganger mob.
    LivingEntity doppelganger = _creatureFactory.spawnNamedCreature(creatureType, world, groundLocation, doppelgangerName);
    if (doppelganger == null)
    {
      // If the creature type is invalid, it is a configuration error. The shape
      // items are already lost. Since Configuration.isValidCreatureType() was
      // called prior to entering doDoppelganger(), this shouldn't happen.
      getLogger().severe("Could not spawn " + creatureType);
    }
    else
    {
      // If we can, make the doppelganger the players *problem*.
      if (doppelganger instanceof Creature)
      {
        ((Creature) doppelganger).setTarget(event.getPlayer());
      }

      // Make the doppelganger wear the player head.
      doppelganger.getEquipment().setHelmet(getPlayerHead(doppelgangerName));

      // TODO: remove the fixed drop chance. Should be as configured.
      doppelganger.getEquipment().setHelmetDropChance(1.0f);
    }
  } // doDoppelganger

  // --------------------------------------------------------------------------
  /**
   * Return an ItemStack containing the head of the specified player.
   * 
   * @param name the name of the player.
   * @return an ItemStack containing the head of the specified player.
   */
  protected static ItemStack getPlayerHead(String name)
  {
    ItemStack stack = new ItemStack(Material.SKULL_ITEM, 1);
    stack.setDurability((short) 3);
    SkullMeta meta = (SkullMeta) stack.getItemMeta();
    meta.setOwner(name);
    stack.setItemMeta(meta);
    return stack;
  }

  // --------------------------------------------------------------------------
  /**
   * Pattern describing allowable doppelganger names.
   */
  protected Pattern         _namePattern     = Pattern.compile("^(?:\\w|_)+$");

  /**
   * Handles creation of creatures.
   */
  protected CreatureFactory _creatureFactory = new CreatureFactory();

  /**
   * Configuration management.
   */
  protected Configuration   _configuration   = new Configuration(this, _creatureFactory);
} // class Doppelganger
