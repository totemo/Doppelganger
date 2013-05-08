package io.github.totemo.doppelganger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

// ----------------------------------------------------------------------------
/**
 * Describes the shape of a creature in terms of the final, triggering block's
 * Material, and the relative positions and materials of the blocks that need to
 * be in place around it in order to summon the creature.
 */
public class CreatureShape
{
  // --------------------------------------------------------------------------
  /**
   * Load this CreatureShape from the specified section.
   * 
   * The expected format is:
   * 
   * <pre>
   *    head: pumpkin
   *    body:
   *    - material: diamond_block
   *      offset: [0, -1, 0]
   *    - material: diamond_block
   *      offset: [0, -2, 0]
   *    summon:
   *    - weight: 1.0
   *      spawn: ToughWitherSkeleton
   * </pre>
   * 
   * Note that the "body" can be omitted, in which case placing a named head
   * block will summon a creature. Also, the "summon" can be omitted, in which
   * case nothing will be summoned by default. This may still be useful if
   * specific player names override what is summoned.
   * 
   * @param section the configuration section to load.
   * @param logger logs messages.
   * @return a new CreatureShape instance, or null on error.
   */
  public static CreatureShape loadFromSection(ConfigurationSection section, Logger logger)
  {
    try
    {
      Material headMaterial = Material.matchMaterial(section.getString("head", ""));
      CreatureShape shape = new CreatureShape(section.getName(), headMaterial);

      if (section.isList("body"))
      {
        try
        {
          List<Map<?, ?>> blocks = section.getMapList("body");
          for (int i = 0; i < blocks.size(); ++i)
          {
            ConfigMap map = new ConfigMap(blocks.get(i), logger, shape.getName() + " block " + i);
            String materialName = map.getString("material", "");
            Material mat = Material.matchMaterial(materialName);
            List<Number> offset = map.getNumberList("offset", null);
            if (mat != null && offset != null && offset.size() == 3)
            {
              Vector vector = new Vector(offset.get(0).intValue(),
                                         offset.get(1).intValue(),
                                         offset.get(2).intValue());
              shape.addCreatureBlock(mat, vector);
            }
            else
            {
              logger.warning("block index " + i + " is incorrectly specified");
              return null;
            }
          } // for each block in the body
        }
        catch (Exception ex)
        {
          logger.warning(ex.getClass().getName() + " loading body in " + section.getCurrentPath());
        }
      } // body

      if (section.isList("summon"))
      {
        try
        {
          List<Map<?, ?>> summons = section.getMapList("summon");
          for (int i = 0; i < summons.size(); ++i)
          {
            ConfigMap map = new ConfigMap(summons.get(i), logger, shape.getName() + " summon " + i);
            double weight = map.getDouble("weight", 1.0);
            String spawn = map.getString("spawn", "");
            shape.addCreatureType(spawn, weight);
          }
        }
        catch (Exception ex)
        {
          logger.warning(ex.getClass().getName() + " loading summon in " + section.getCurrentPath());
        }
      } // summon
      return shape;
    }
    catch (Exception ex)
    {
      logger.warning(ex.getClass().getName() + " defining creature shape " + section.getCurrentPath());
    }
    return null;
  } // loadFromSection

  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param name the name of this shape in the configuration.
   * @param triggerMaterial the Material of the final placed block that triggers
   *          creature spawning.
   */
  public CreatureShape(String name, Material triggerMaterial)
  {
    _name = name;
    _triggerMaterialId = triggerMaterial.getId();
  }

  // --------------------------------------------------------------------------
  /**
   * Return the name of this shape in the configuration.
   * 
   * @return the name of this shape in the configuration.
   */
  public String getName()
  {
    return _name;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the Material ID of the trigger.
   * 
   * @return the Material ID of the trigger.
   */
  public int getTriggerMaterialId()
  {
    return _triggerMaterialId;
  }

  // --------------------------------------------------------------------------
  /**
   * Signify that the {@link CreatureType} signified by its name can be summoned
   * by this shape, with the specified weighted probability.
   * 
   * The chance of a specific {@link CreatureType} being spawned is its weight
   * divided by the sum of the weights of all registered types for this shape.
   * 
   * @param type the name of the {@link CreatureType}.
   * @param weight the probability weight.
   */
  public void addCreatureType(String type, double weight)
  {
    _types.addChoice(type, weight);
  }

  // --------------------------------------------------------------------------
  /**
   * Return the name of the spawned CreatureType.
   * 
   * If more than one {@link CreatureType} is associated with this shape, a type
   * will be selected at random according to weighted probability.
   * 
   * @return the type name of the spawned CreatureType.
   */
  public String chooseCreatureType()
  {
    return _types.choose();
  }

  // --------------------------------------------------------------------------
  /**
   * Define the Material its position relative to the trigger block.
   * 
   * @param material the Material of the pre-placed creature block.
   * @param offset the position of that relative to the final trigger block.
   */
  public void addCreatureBlock(Material material, Vector offset)
  {
    _materialIds.add(material.getId());
    _offsets.add(offset);
    if (offset.getBlockY() < _groundOffset)
    {
      _groundOffset = offset.getBlockY();
    }
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if placing the trigger block at the specified Location would
   * result in a complete creature shape.
   * 
   * @param world the World.
   * @param loc the location where the trigger block would be placed.
   * @return true if placing the trigger block at the specified location would
   *         result in a complete creature shape.
   */
  public boolean isCreatureShape(World world, Location loc)
  {
    for (int i = 0; i < _materialIds.size(); ++i)
    {
      if (getCreatureBlock(world, loc, i).getTypeId() != _materialIds.get(i))
      {
        return false;
      }
    }
    return true;
  } // isCreatureShape

  // --------------------------------------------------------------------------
  /**
   * Set all of the blocks of the creature shape at the specified location to
   * air.
   */
  public void vaporise(World world, Location loc)
  {
    for (int i = 0; i < _offsets.size(); ++i)
    {
      getCreatureBlock(world, loc, i).setTypeId(0);
    }
  }

  // --------------------------------------------------------------------------
  /**
   * Return the offset in Y coordinate of the lowest blocks in the shape.
   * 
   * @return the offset in Y coordinate of the lowest blocks in the shape.
   */
  public int getGroundOffset()
  {
    return _groundOffset;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the block in the World comprising the creature shape with specified
   * index.
   * 
   * @param world the World.
   * @param loc the Location.
   * @param index the 0-based index of the block in the shape; 0 is first one
   *          added.
   */
  protected Block getCreatureBlock(World world, Location loc, int index)
  {
    Vector offset = _offsets.get(index);
    int x = loc.getBlockX() + offset.getBlockX();
    int y = loc.getBlockY() + offset.getBlockY();
    int z = loc.getBlockZ() + offset.getBlockZ();
    return world.getBlockAt(x, y, z);
  }

  // --------------------------------------------------------------------------
  /**
   * Dump internal state to the specified Logger for debugging.
   * 
   * @param logger the Logger.
   */
  protected void dump(Logger logger)
  {
    logger.info("Trigger: " + Material.getMaterial(_triggerMaterialId).name());
    logger.info("Ground offset: " + _groundOffset);
    logger.info("Blocks:");
    for (int i = 0; i < _materialIds.size(); ++i)
    {
      String material = Material.getMaterial(_materialIds.get(i)).name();
      String offset = _offsets.get(i).toString();
      logger.info(i + ": " + material + " at " + offset);
    }
    for (Entry<Double, String> summon : _types.entrySet())
    {
      logger.info("summon " + summon.getValue());
    }
  } // dump

  // --------------------------------------------------------------------------
  /**
   * The name of this shape in the configuration.
   */
  protected String                    _name;

  /**
   * The material of the placed, named block that triggers a search for a
   * creature shape.
   */
  protected int                       _triggerMaterialId;

  /**
   * The relative Y offset of the lowest block(s) in the shape.
   */
  protected int                       _groundOffset;

  /**
   * Expected Material ID at corresponding offset from trigger block.
   */
  protected ArrayList<Integer>        _materialIds = new ArrayList<Integer>();

  /**
   * Offset relative to trigger block to look for corresponding _materials
   * element.
   */
  protected ArrayList<Vector>         _offsets     = new ArrayList<Vector>();

  /**
   * Manages weighted random selection of creature type name to spawn.
   */
  protected WeightedSelection<String> _types       = new WeightedSelection<String>();
} // class CreatureShape
