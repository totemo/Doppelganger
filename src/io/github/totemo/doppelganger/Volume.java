package io.github.totemo.doppelganger;

import org.bukkit.Location;
import org.bukkit.World;

// ----------------------------------------------------------------------------
/**
 * Abstract base of classes representing a Volume of space that can test a point
 * for intersection.
 */
public abstract class Volume
{
  // --------------------------------------------------------------------------
  /**
   * Return true if the Volume contains the specified Location.
   * 
   * @param loc the Location.
   * @return true if the Volume contains the specified Location.
   */
  public abstract boolean contains(Location loc);

  // --------------------------------------------------------------------------
  /**
   * Return the world containing this Volume.
   * 
   * @return the world containing this Volume.
   */
  public abstract World getWorld();

  // --------------------------------------------------------------------------
  /**
   * A spherical Volume.
   */
  public static final class Sphere extends Volume
  {
    /**
     * Constructor.
     * 
     * @param centre the centre of the sphere.
     * @param radius the radius.
     */
    public Sphere(Location centre, double radius)
    {
      _centre = centre.clone();
      _radius = radius;
    }

    // ------------------------------------------------------------------------
    /**
     * @see io.github.totemo.doppelganger.Volume#contains(org.bukkit.Location)
     */
    @Override
    public boolean contains(Location loc)
    {
      return loc.getWorld() == _centre.getWorld() &&
             loc.distanceSquared(_centre) < _radius * _radius;
    }

    // --------------------------------------------------------------------------
    /**
     * @see io.github.totemo.doppelganger.Volume#getWorld()
     */
    @Override
    public World getWorld()
    {
      return _centre.getWorld();
    }

    // ------------------------------------------------------------------------
    /**
     * Return the centre of the sphere.
     * 
     * @return the centre of the sphere.
     */
    public Location getCentre()
    {
      return _centre;
    }

    // ------------------------------------------------------------------------
    /**
     * Return the radius.
     * 
     * @return the radius.
     */
    public double getRadius()
    {
      return _radius;
    }

    // ------------------------------------------------------------------------
    /**
     * Centre of the sphere.
     */
    private Location _centre;

    /**
     * Radius in blocks.
     */
    private double   _radius;
  } // inner class Volume.Sphere

  // --------------------------------------------------------------------------
  /**
   * A cuboid volume.
   */
  public static final class Box extends Volume
  {
    /**
     * Constructor.
     * 
     * @param corner1 a corner.
     * @param corner1 a corner.
     */
    public Box(Location corner1, Location corner2)
    {
      if (corner1.getWorld() != corner2.getWorld())
      {
        throw new IllegalArgumentException("the corners of the box are in different worlds");
      }

      _c1 = new Location(corner1.getWorld(),
                         Math.min(corner1.getX(), corner2.getX()),
                         Math.min(corner1.getY(), corner2.getY()),
                         Math.min(corner1.getZ(), corner2.getZ()));
      _c2 = new Location(corner2.getWorld(),
                         Math.max(corner1.getX(), corner2.getX()),
                         Math.max(corner1.getY(), corner2.getY()),
                         Math.max(corner1.getZ(), corner2.getZ()));
    }

    // ------------------------------------------------------------------------
    /**
     * @see io.github.totemo.doppelganger.Volume#contains(org.bukkit.Location)
     */
    @Override
    public boolean contains(Location loc)
    {
      return loc.getWorld() == _c1.getWorld() &&
             loc.getX() >= _c1.getX() && loc.getX() <= _c2.getX() &&
             loc.getY() >= _c1.getY() && loc.getY() <= _c2.getY() &&
             loc.getZ() >= _c1.getZ() && loc.getZ() <= _c2.getZ();
    }

    // --------------------------------------------------------------------------
    /**
     * @see io.github.totemo.doppelganger.Volume#getWorld()
     */
    @Override
    public World getWorld()
    {
      return _c1.getWorld();
    }

    // ------------------------------------------------------------------------
    /**
     * Corner 1.
     */
    private Location _c1;

    /**
     * Diagonally opposite corner 2.
     */
    private Location _c2;
  } // inner class Box
} // class Volume