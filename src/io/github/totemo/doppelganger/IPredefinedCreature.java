package io.github.totemo.doppelganger;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

// ----------------------------------------------------------------------------
/**
 * The interface implemented by all {@link PredefinedCreature}s
 */
public interface IPredefinedCreature
{
  // --------------------------------------------------------------------------
  /**
   * Return true if the specified LivingEntity is of this type.
   *
   * @param living the creature to examine.
   * @return true if it is this type of PredefinedCreature.
   */
  boolean isInstance(LivingEntity living);

  // --------------------------------------------------------------------------
  /**
   * Spawn a creature of this type at the specified location.
   *
   * @param loc the Location.
   * @return the LivingEntity.
   */
  LivingEntity spawn(Location loc);
} // class IPredefinedCreature