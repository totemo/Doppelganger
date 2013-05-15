package io.github.totemo.doppelganger;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;

import com.amoebaman.kitmaster.utilities.CommandController.SubCommandHandler;

// --------------------------------------------------------------------------
/**
 * Handles the command line.
 */
public class Commands
{
  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param plugin the reference to the Doppelganger plugin.
   */
  public Commands(Doppelganger plugin)
  {
    _plugin = plugin;
  }

  // --------------------------------------------------------------------------

  @SubCommandHandler(parent = "doppel", name = "info", permission = "doppel.info")
  public void onCommandDoppelInfo(CommandSender sender, String[] args)
  {
    if (args.length == 2)
    {
      if (args[1].equals("list"))
      {
        _plugin.getCreatureFactory().listConfiguration(sender);
        return;
      }
    }
    else if (args.length == 3)
    {
      if (args[1].equals("shape"))
      {
        CreatureShape shape = _plugin.getCreatureFactory().getCreatureShape(args[2]);
        if (shape == null)
        {
          sender.sendMessage(ChatColor.RED + "There is no shape by that name.");
        }
        else
        {
          shape.describe(sender);
        }
        return;
      }
      else if (args[1].equals("creature"))
      {
        CreatureType type = _plugin.getCreatureFactory().getCreatureType(args[2]);
        if (type == null)
        {
          sender.sendMessage(ChatColor.RED + "There is no creature type by that name.");
        }
        else
        {
          type.describe(sender);
        }
        return;
      }
      else if (args[1].equals("player"))
      {
        String name = args[2];
        String creature = _plugin.getCreatureFactory().getPlayerCreature(name);
        if (creature == null)
        {
          sender.sendMessage(ChatColor.RED + "No specific creature type is defined for that player name.");
        }
        else
        {
          sender.sendMessage(ChatColor.YELLOW + "Player " + name + " will spawn a creature of type " + creature + ".");
          ArrayList<CreatureShape> shapes = _plugin.getCreatureFactory().getPlayerShapes(name);
          if (shapes.size() == 0)
          {
            sender.sendMessage(ChatColor.YELLOW + name
                               + "can only be spawned by command because no summoning shapes are defined.");
          }
          else
          {
            StringBuilder message = new StringBuilder();
            message.append(ChatColor.YELLOW);
            message.append(name);
            message.append(" can be spawned by the following shapes:");
            for (CreatureShape shape : shapes)
            {
              message.append(' ');
              message.append(shape.getName());
            }
            sender.sendMessage(message.toString());
          }
        }
        return;
      }
    }
    sender.sendMessage(ChatColor.RED +
                       "Usage: /doppel info [list | shape <name> | creature <name> | player <name>]");
  } // onCommandDoppelInfo

  // --------------------------------------------------------------------------
  /**
   * Handle the /doppel coords [&lt;name&gt;] [&lt;radius&gt;] command.
   */
  @SubCommandHandler(parent = "doppel", name = "coords", permission = "doppel.coords")
  public void onCommandDoppelCoords(Player player, String[] args)
  {
    ArrayList<LivingEntity> doppelgangers = null;
    Double radius = Double.MAX_VALUE;
    if (args.length == 1)
    {
      doppelgangers = findDoppelgangers(null, player.getLocation(), radius);
    }
    else if (args.length == 2)
    {
      Double tryRadius = parseDouble(args, 1);
      if (tryRadius == null)
      {
        // tryRadius didn't parse so assume it was a name.
        doppelgangers = findDoppelgangers(args[1], player.getLocation(), radius);
      }
      else
      {
        radius = tryRadius;
        doppelgangers = findDoppelgangers(null, player.getLocation(), radius);
      }
    }
    else if (args.length == 3)
    {
      radius = parseDouble(args, 2);
      if (radius != null && radius >= 0.0)
      {
        doppelgangers = findDoppelgangers(args[1], player.getLocation(), radius);
      }
    }

    if (doppelgangers == null)
    {
      player.sendMessage(ChatColor.RED + "Usage: /doppel coords [<name>] [<radius>]");
    }
    else
    {
      String message;
      if (radius > 1e9)
      {
        message = String.format("There are %d matching doppelgangers loaded in this world.", doppelgangers.size());
      }
      else
      {
        message = String.format("There are %d matching doppelgangers loaded within %5g blocks of you.",
          doppelgangers.size(), radius);
      }
      player.sendMessage(ChatColor.YELLOW + message);

      for (int i = 0; i < doppelgangers.size(); ++i)
      {
        LivingEntity living = doppelgangers.get(i);
        Location loc = living.getLocation();
        player.sendMessage(String.format("%s(%d) %s %s (%d, %d, %d)",
          ChatColor.YELLOW, i + 1, living.getCustomName(), getLivingEntityType(living),
            loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
      }
    }
  } // onCommandDoppelCoords

  // --------------------------------------------------------------------------
  /**
   * Handle the /doppel kill &lt;name&gt; &lt;radius&gt; [[&lt;world&gt;]
   * &lt;x&gt; &lt;y&gt; &lt;z&gt;] command.
   * 
   * @param sender the issuer of the command.
   * @param args command arguments after the initial /doppel.
   */
  @SubCommandHandler(parent = "doppel", name = "kill", permission = "doppel.kill")
  public void onCommandDoppelKill(CommandSender sender, String[] args)
  {
    Double radius = null;
    Location centre = null;
    if (args.length == 3)
    {
      // /doppel kill <name> <radius>
      centre = getLocation(sender);
      if (centre == null)
      {
        sender.sendMessage(ChatColor.RED + "You must specify the world and coordinates.");
      }
      else
      {
        radius = parseDouble(args, 2);
      }
    }
    else if (args.length == 6)
    {
      // /doppel kill <name> <radius> <x> <y> <z>
      // Check that the CommandSender implies a World for the coordinates.
      Location senderLocation = getLocation(sender);
      if (senderLocation == null)
      {
        sender.sendMessage(ChatColor.RED + "You must specify the world.");
      }
      else
      {
        radius = parseDouble(args, 2);
        centre = parseLocation(args, 3, ((Player) sender).getWorld());
      }
    }
    else if (args.length == 7)
    {
      // /doppel kill <name> <radius> <world> <x> <y> <z>
      radius = parseDouble(args, 2);
      World world = sender.getServer().getWorld(args[3]);
      if (world == null)
      {
        sender.sendMessage(ChatColor.RED + "There is no world named " + args[3] + ".");
      }
      else
      {
        centre = parseLocation(args, 4, world);
      }
    }

    // Were the centre and radius were determined somehow?
    if (centre == null || radius == null)
    {
      sender.sendMessage(ChatColor.RED + "Usage: /doppel kill <name> <radius> [[<world>] <x> <y> <z>]");
    }
    else
    {
      ArrayList<LivingEntity> doppelgangers = findDoppelgangers(args[1], centre, radius);
      String message;
      if (radius > 1e9)
      {
        message = String.format("Killing %d matching, loaded doppelgangers in %s.",
          doppelgangers.size(), centre.getWorld().getName());
      }
      else
      {
        message = String.format("Killing %d matching, loaded doppelgangers within %5g blocks of (%d, %d, %d) in %s.",
          doppelgangers.size(), radius, centre.getBlockX(), centre.getBlockY(), centre.getBlockZ(), centre.getWorld().getName());
      }
      sender.sendMessage(ChatColor.YELLOW + message);

      for (int i = 0; i < doppelgangers.size(); ++i)
      {
        LivingEntity living = doppelgangers.get(i);
        Location loc = living.getLocation();
        String description = String.format("%s(%d) %s %s (%d, %d, %d)",
          ChatColor.YELLOW, i + 1, living.getCustomName(), getLivingEntityType(living),
          loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        sender.sendMessage(description);
        _plugin.getLogger().info("Killing " + description);
        living.remove();
      }
    }
  } // onCommandDoppelKill

  // --------------------------------------------------------------------------
  /**
   * Handle the /doppel spawn [[&lt;world&gt;] &lt;x&gt; &lt;y&gt; &lt;z&gt;]
   * &lt;type&gt; [&lt;name&gt;] command.
   * 
   * If the sender is a player and the world and x, y and z coordinates are
   * omitted, the creature spawns at the player location.
   * 
   * @param sender the issuer of the command.
   * @param args command arguments after the initial /doppel.
   */
  @SubCommandHandler(parent = "doppel", name = "spawn", permission = "doppel.spawn")
  public void onCommandDoppelSpawn(CommandSender sender, String[] args)
  {
    String type = null;
    String name = null;
    Location loc = null;
    if (args.length == 2 || args.length == 3)
    {
      // /doppel spawn <type>
      type = args[1];
      if (args.length == 3)
      {
        name = args[2];
      }
      loc = getLocation(sender);
      if (loc == null)
      {
        sender.sendMessage(ChatColor.RED + "You must specify the world and coordinates.");
      }
    }
    else if (args.length == 5)
    {
      // /doppel spawn <x> <y> <z> <type>
      type = args[4];
      Location senderLoc = getLocation(sender);
      if (senderLoc == null)
      {
        sender.sendMessage(ChatColor.RED + "You must specify the world.");
      }
      else
      {
        loc = parseLocation(args, 1, senderLoc.getWorld());
      }
    }
    else if (args.length == 6)
    {
      // Two cases:
      // (1) EITHER (common):
      // /doppel spawn <x> <y> <z> <type> <name>
      // (2) OR (uncommon):
      // /doppel spawn <world> <x> <y> <z> <type>

      // Try case (2) first.
      World world = sender.getServer().getWorld(args[1]);
      if (world == null)
      {
        // World is invalid. Assume this is case (1).
        Location senderLoc = getLocation(sender);
        if (senderLoc == null)
        {
          sender.sendMessage(ChatColor.RED + "You must specify the world.");
        }
        else
        {
          loc = parseLocation(args, 1, senderLoc.getWorld());
          type = args[4];
          name = args[5];
        }
      }
      else
      {
        // World name is valid. Case (2) confirmed.
        loc = parseLocation(args, 2, world);
        type = args[5];
      }
    }
    else if (args.length == 7)
    {
      // /doppel spawn <world> <x> <y> <z> <type> <name>
      World world = sender.getServer().getWorld(args[1]);
      if (world == null)
      {
        sender.sendMessage(ChatColor.RED + "There is no world named " + args[1] + ".");
      }
      else
      {
        loc = parseLocation(args, 2, world);
      }
      type = args[5];
      name = args[6];
    }

    if (loc == null || type == null)
    {
      sender.sendMessage(ChatColor.RED + "Usage: /doppel spawn [[<world>] <x> <y> <z>] <type> [<name>]");
    }
    else
    {
      _plugin.spawnDoppelganger(type, name, loc);
    }
  } // onCommandDoppelSpawn

  // --------------------------------------------------------------------------
  /**
   * 
   * @param sender the issuer of the command.
   * @param args command arguments after the initial /doppel.
   */
  @SubCommandHandler(parent = "doppel", name = "maintain", permission = "doppel.maintain")
  public void onCommandDoppelMaintain(CommandSender sender, String[] args)
  {
    sender.sendMessage(ChatColor.RED + "NOT YET IMPLEMENTED: /doppel maintain");
  }

  // --------------------------------------------------------------------------
  /**
   * Return the Location of the specified CommandSender, or null if the sender
   * has no Location (e.g. the console).
   * 
   * @return the Location of the specified CommandSender, or null if the sender
   *         has no Location (e.g. the console).
   */
  protected Location getLocation(CommandSender sender)
  {
    if (sender instanceof Player)
    {
      return ((Player) sender).getLocation();
    }
    else if (sender instanceof BlockCommandSender)
    {
      return ((BlockCommandSender) sender).getBlock().getLocation();
    }
    else
    {
      return null;
    }
  }

  // --------------------------------------------------------------------------
  /**
   * Attempt to parse a double value from args[index].
   * 
   * @param args the args array passed to the subcommand.
   * @param index the index into args[].
   * @return the parsed double value, or null on error.
   */
  protected Double parseDouble(String[] args, int index)
  {
    if (index >= 0 && index < args.length)
    {
      try
      {
        return Double.parseDouble(args[index]);
      }
      catch (Exception ex)
      {
        // Silent.
      }
    }
    return null;
  }

  // --------------------------------------------------------------------------
  /**
   * Parse an x, y, z Location out of the command line arguments.
   * 
   * @param args the command line arguments array as passed to the subcommand.
   * @param index the index of the x coordinate in args; y and z should follow.
   * @param world the world where the coordinates apply.
   * @return the parsed Location or null on error.
   */
  protected Location parseLocation(String[] args, int index, World world)
  {
    try
    {
      double x = Double.parseDouble(args[index]);
      double y = Double.parseDouble(args[index + 1]);
      double z = Double.parseDouble(args[index + 2]);
      return new Location(world, x, y, z);
    }
    catch (Exception ex)
    {
      // May get here from ArrayIndexOutOfBoundsException,
      // NumberFormatException, etc.
      // Silent.
    }
    return null;
  }

  // --------------------------------------------------------------------------
  /**
   * Return a list of all LivingEntity instances with the specified visible
   * custom name within the specified radius of the centre location.
   * 
   * The search volume is a sphere with the specified centre and radius.
   * 
   * @param name the custom name, which must be visible; if this is null, any
   *          name will do.
   * @param centre the centre of the searched sphere.
   * @param radius the radius of the searched sphere.
   * @return a list of the matching LivingEntity instances.
   */
  protected ArrayList<LivingEntity> findDoppelgangers(String name, Location centre, double radius)
  {
    double radiusSquared = radius * radius;
    ArrayList<LivingEntity> doppelgangers = new ArrayList<LivingEntity>();
    for (LivingEntity living : centre.getWorld().getLivingEntities())
    {
      if (living.isCustomNameVisible() && (name == null || name.equals(living.getCustomName())) &&
          living.getLocation().distanceSquared(centre) < radiusSquared)
      {
        doppelgangers.add(living);
      }
    }
    return doppelgangers;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the type name of the specified LivingEntity.
   * 
   * @return the type name of the specified LivingEntity.
   */
  protected String getLivingEntityType(LivingEntity living)
  {
    if (living instanceof Skeleton && ((Skeleton) living).getSkeletonType() == SkeletonType.WITHER)
    {
      return "WitherSkeleton";
    }
    else
    {
      return living.getType().getName();
    }
  }

  // --------------------------------------------------------------------------
  /**
   * Reference to the Doppelganger plugin.
   */
  protected Doppelganger _plugin;
} // class Commands