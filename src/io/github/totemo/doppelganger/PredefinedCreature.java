package io.github.totemo.doppelganger;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Zombie;

// ----------------------------------------------------------------------------
/**
 * Describes predefined creature types that are variations on vanilla creatures.
 * 
 * These creature types are not defined in the Doppelganger configuration file,
 * but are a well known part of the vanilla game. They are all variants of some
 * base LivingEntity type that must be customised by setting an attribute,
 * rather than instantiating a unique class.
 */
public enum PredefinedCreature
{
  WitherSkeleton
  {
    @Override
    public boolean isInstance(LivingEntity living)
    {
      return living instanceof Skeleton &&
             ((Skeleton) living).getSkeletonType() == SkeletonType.WITHER;
    }
    @Override
    public LivingEntity spawn(Location loc)
    {
      Skeleton skeleton = loc.getWorld().spawn(loc, Skeleton.class);
      skeleton.setSkeletonType(SkeletonType.WITHER);
      return skeleton;
    }
  },

  SaddledPig
  {
    @Override
    public boolean isInstance(LivingEntity living)
    {
      return living instanceof Pig && ((Pig) living).hasSaddle();
    }
    @Override
    public LivingEntity spawn(Location loc)
    {
      Pig pig = loc.getWorld().spawn(loc, Pig.class);
      pig.setSaddle(true);
      return pig;
    }
  },

  ZombieVillager
  {
    @Override
    public boolean isInstance(LivingEntity living)
    {
      return living instanceof Zombie && ((Zombie) living).isVillager();
    }

    @Override
    public LivingEntity spawn(Location loc)
    {
      Zombie zombie = loc.getWorld().spawn(loc, Zombie.class);
      zombie.setVillager(true);
      return zombie;
    }
  },

  Blacksmith
  {
    @Override
    public boolean isInstance(LivingEntity living)
    {
      return living instanceof Villager &&
             ((Villager) living).getProfession() == Profession.BLACKSMITH;
    }

    @Override
    public LivingEntity spawn(Location loc)
    {
      Villager villager = loc.getWorld().spawn(loc, Villager.class);
      villager.setProfession(Profession.BLACKSMITH);
      return villager;
    }
  },

  Butcher
  {
    @Override
    public boolean isInstance(LivingEntity living)
    {
      return living instanceof Villager &&
             ((Villager) living).getProfession() == Profession.BUTCHER;
    }

    @Override
    public LivingEntity spawn(Location loc)
    {
      Villager villager = loc.getWorld().spawn(loc, Villager.class);
      villager.setProfession(Profession.BUTCHER);
      return villager;
    }
  },

  Farmer
  {
    @Override
    public boolean isInstance(LivingEntity living)
    {
      return living instanceof Villager &&
             ((Villager) living).getProfession() == Profession.FARMER;
    }

    @Override
    public LivingEntity spawn(Location loc)
    {
      Villager villager = loc.getWorld().spawn(loc, Villager.class);
      villager.setProfession(Profession.FARMER);
      return villager;
    }
  },

  Librarian
  {
    @Override
    public boolean isInstance(LivingEntity living)
    {
      return living instanceof Villager &&
             ((Villager) living).getProfession() == Profession.LIBRARIAN;
    }

    @Override
    public LivingEntity spawn(Location loc)
    {
      Villager villager = loc.getWorld().spawn(loc, Villager.class);
      villager.setProfession(Profession.LIBRARIAN);
      return villager;
    }
  },

  Priest
  {
    @Override
    public boolean isInstance(LivingEntity living)
    {
      return living instanceof Villager &&
             ((Villager) living).getProfession() == Profession.PRIEST;
    }

    @Override
    public LivingEntity spawn(Location loc)
    {
      Villager villager = loc.getWorld().spawn(loc, Villager.class);
      villager.setProfession(Profession.PRIEST);
      return villager;
    }
  };

  // --------------------------------------------------------------------------
  /**
   * Look up the PredefinedCreature enum value by name.
   * 
   * @param name the case insensitive name.
   * @return the PredefinedCreature value, or null if not found.
   */
  public static PredefinedCreature fromName(String name)
  {
    return BY_NAME.get(name.toLowerCase());
  }

  // --------------------------------------------------------------------------
  /**
   * If the LivingEntity is an instance of a PredefinedCreature, return that
   * enum value; otherwise, return null.
   * 
   * @param living the LivingEntity to look up.
   * @return the corresponding PredefinedCreature, or null if not found.
   */
  public static PredefinedCreature fromLivingEntity(LivingEntity living)
  {
    for (PredefinedCreature creature : values())
    {
      if (creature.isInstance(living))
      {
        return creature;
      }
    }
    return null;
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if the specified LivingEntity is of this type.
   * 
   * @param living the creature to examine.
   * @return true if it is this type of PredefinedCreature.
   */
  public abstract boolean isInstance(LivingEntity living);

  // --------------------------------------------------------------------------
  /**
   * Spawn a creature of this type at the specified location.
   * 
   * @param loc the Location.
   * @return the LivingEntity.
   */
  public abstract LivingEntity spawn(Location loc);

  // --------------------------------------------------------------------------
  /**
   * Map from lower case PredefinedCreature name to instance.
   */
  protected static HashMap<String, PredefinedCreature> BY_NAME = new HashMap<String, PredefinedCreature>();

  // --------------------------------------------------------------------------
  // Initialise maps.

  static
  {
    for (PredefinedCreature creature : values())
    {
      BY_NAME.put(creature.name().toLowerCase(), creature);
    }
  }
} // enum PredefinedCreature
