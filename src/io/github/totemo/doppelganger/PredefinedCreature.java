package io.github.totemo.doppelganger;

import java.util.HashMap;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
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
public enum PredefinedCreature implements IPredefinedCreature {
    WitherSkeleton(new IPredefinedCreature() {
        @Override
        public boolean isInstance(LivingEntity living) {
            return living instanceof Skeleton &&
                   ((Skeleton) living).getSkeletonType() == SkeletonType.WITHER;
        }

        @Override
        public LivingEntity spawn(Location loc) {
            Skeleton skeleton = loc.getWorld().spawn(loc, Skeleton.class);
            skeleton.setSkeletonType(SkeletonType.WITHER);
            return skeleton;
        }
    }),

    SaddledPig(new IPredefinedCreature() {
        @Override
        public boolean isInstance(LivingEntity living) {
            return living instanceof Pig && ((Pig) living).hasSaddle();
        }

        @Override
        public LivingEntity spawn(Location loc) {
            Pig pig = loc.getWorld().spawn(loc, Pig.class);
            pig.setSaddle(true);
            return pig;
        }
    }),

    BabyPigZombie(new IPredefinedCreature() {
        @Override
        public boolean isInstance(LivingEntity living) {
            return living instanceof PigZombie && ((PigZombie) living).isBaby();
        }

        @Override
        public LivingEntity spawn(Location loc) {
            PigZombie zombie = loc.getWorld().spawn(loc, PigZombie.class);
            zombie.setBaby(true);
            return zombie;
        }
    }),

    ChargedCreeper(new IPredefinedCreature() {
        @Override
        public boolean isInstance(LivingEntity living) {
            return living instanceof Creeper && ((Creeper) living).isPowered();
        }

        @Override
        public LivingEntity spawn(Location loc) {
            Creeper creeper = loc.getWorld().spawn(loc, Creeper.class);
            creeper.setPowered(true);
            return creeper;
        }
    }),

    Blacksmith(new PredefinedVillager(false, Profession.BLACKSMITH)), BabyBlacksmith(
    new PredefinedVillager(true, Profession.BLACKSMITH)), ZombieBlacksmith(new PredefinedZombie(false, Profession.BLACKSMITH)), BabyZombieBlacksmith(
    new PredefinedZombie(true, Profession.BLACKSMITH)), Butcher(new PredefinedVillager(false, Profession.BUTCHER)), BabyButcher(
    new PredefinedVillager(true, Profession.BUTCHER)), ZombieButcher(new PredefinedZombie(false, Profession.BUTCHER)), BabyZombieButcher(
    new PredefinedZombie(true, Profession.BUTCHER)), Farmer(new PredefinedVillager(false, Profession.FARMER)), BabyFarmer(
    new PredefinedVillager(true, Profession.FARMER)), ZombieFarmer(new PredefinedZombie(false, Profession.FARMER)), BabyZombieFarmer(
    new PredefinedZombie(true, Profession.FARMER)), Librarian(new PredefinedVillager(false, Profession.LIBRARIAN)), BabyLibrarian(
    new PredefinedVillager(true, Profession.LIBRARIAN)), ZombieLibrarian(new PredefinedZombie(false, Profession.LIBRARIAN)), BabyZombieLibrarian(
    new PredefinedZombie(true, Profession.LIBRARIAN)), Priest(new PredefinedVillager(false, Profession.PRIEST)), BabyPriest(
    new PredefinedVillager(true, Profession.PRIEST)), ZombiePriest(
    new PredefinedZombie(false, Profession.PRIEST)), BabyZombiePriest(new PredefinedZombie(true, Profession.PRIEST)),

    BabyVillager(new PredefinedVillager(true, null)), BabyZombie(new PredefinedZombie(true, null)),

    ZombieVillager(new IPredefinedCreature() {
        @Override
        public boolean isInstance(LivingEntity living) {
            return living instanceof Zombie && ((Zombie) living).isVillager();
        }

        @Override
        public LivingEntity spawn(Location loc) {
            Zombie zombie = loc.getWorld().spawn(loc, Zombie.class);
            zombie.setVillagerProfession(getRandomVillagerProfession());
            return zombie;
        }
    }),

    BabyZombieVillager(new IPredefinedCreature() {
        @Override
        public boolean isInstance(LivingEntity living) {
            return living instanceof Zombie &&
                   ((Zombie) living).isVillager() &&
                   ((Zombie) living).isBaby();
        }

        @Override
        public LivingEntity spawn(Location loc) {
            Zombie zombie = loc.getWorld().spawn(loc, Zombie.class);
            zombie.setBaby(true);
            zombie.setVillagerProfession(getRandomVillagerProfession());
            return zombie;
        }
    });

    // ------------------------------------------------------------------------
    /**
     * Look up the PredefinedCreature enum value by name.
     *
     * @param name the case insensitive name.
     * @return the PredefinedCreature value, or null if not found.
     */
    public static PredefinedCreature fromName(String name) {
        return BY_NAME.get(name.toLowerCase());
    }

    // ------------------------------------------------------------------------
    /**
     * If the LivingEntity is an instance of a PredefinedCreature, return that
     * enum value; otherwise, return null.
     *
     * @param living the LivingEntity to look up.
     * @return the corresponding PredefinedCreature, or null if not found.
     */
    public static PredefinedCreature fromLivingEntity(LivingEntity living) {
        for (PredefinedCreature creature : values()) {
            if (creature.isInstance(living)) {
                return creature;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------------
    /**
     * Return a random villager profession.
     *
     * @return a random villager profession.
     */
    public static Villager.Profession getRandomVillagerProfession() {
        Villager.Profession[] professions = Villager.Profession.values();
        return professions[_random.nextInt(professions.length)];
    }

    // ------------------------------------------------------------------------

    private PredefinedCreature(IPredefinedCreature implementation) {
        _implementation = implementation;
    }

    // ------------------------------------------------------------------------
    /**
     * @see io.github.totemo.doppelganger.IPredefinedCreature#isInstance(org.bukkit.entity.LivingEntity)
     */
    @Override
    public boolean isInstance(LivingEntity living) {
        return _implementation.isInstance(living);
    }

    // ------------------------------------------------------------------------
    /**
     * @see io.github.totemo.doppelganger.IPredefinedCreature#spawn(org.bukkit.Location)
     */
    @Override
    public LivingEntity spawn(Location loc) {
        return _implementation.spawn(loc);
    }

    // ------------------------------------------------------------------------
    /**
     * PredefinedCreature implementation delegate for villagers with specified
     * age (adult or baby) and villager profession.
     *
     * Bukkit Villager and Zombie interfaces don't share a common superinterface
     * to control age, so this implementation must be distinct from that of
     * PredefinedZombies.
     */
    static final class PredefinedVillager implements IPredefinedCreature {
        /**
         * Constructor.
         *
         * @param baby if true, the villager is a baby; otherwise it is an
         *        adult.
         * @param profession the villager's profession.
         */
        public PredefinedVillager(boolean baby, Profession profession) {
            _baby = baby;
            _profession = profession;
        }

        // --------------------------------------------------------------------
        /**
         * @see io.github.totemo.doppelganger.IPredefinedCreature#isInstance(org.bukkit.entity.LivingEntity)
         */
        @Override
        public boolean isInstance(LivingEntity living) {
            return living instanceof Villager &&
                   ((Villager) living).isAdult() != _baby &&
                   ((Villager) living).getProfession() == _profession;
        }

        // --------------------------------------------------------------------
        /**
         * @see io.github.totemo.doppelganger.IPredefinedCreature#spawn(org.bukkit.Location)
         */
        @Override
        public LivingEntity spawn(Location loc) {
            Villager villager = loc.getWorld().spawn(loc, Villager.class);
            if (_baby) {
                villager.setBaby();
            }
            if (_profession != null) {
                villager.setProfession(_profession);
            }
            return villager;
        }

        // --------------------------------------------------------------------
        /**
         * True if a baby.
         */
        protected boolean _baby;

        /**
         * Villager profession.
         */
        protected Profession _profession;
    }; // inner class PredefinedVillager

    // ------------------------------------------------------------------------
    /**
     * PredefinedCreature implementation delegate for zombies with specified age
     * (adult or baby) and villager profession.
     *
     * Bukkit Villager and Zombie interfaces don't share a common superinterface
     * to control age, so this implementation must be distinct from that of
     * PredefinedVillagers.
     */
    static final class PredefinedZombie implements IPredefinedCreature {
        /**
         * Constructor.
         *
         * @param baby if true, the zombie villager is a baby; otherwise it is
         *        an adult.
         * @param profession the zombie villager's profession; or null for no
         *        profession (a non-villager).
         */
        public PredefinedZombie(boolean baby, Profession profession) {
            _baby = baby;
            _profession = profession;
        }

        // --------------------------------------------------------------------
        /**
         * @see io.github.totemo.doppelganger.IPredefinedCreature#isInstance(org.bukkit.entity.LivingEntity)
         */
        @Override
        public boolean isInstance(LivingEntity living) {
            return living instanceof Zombie &&
                   ((Zombie) living).isBaby() == _baby &&
                   ((Zombie) living).getVillagerProfession() == _profession;
        }

        // --------------------------------------------------------------------
        /**
         * @see io.github.totemo.doppelganger.IPredefinedCreature#spawn(org.bukkit.Location)
         */
        @Override
        public LivingEntity spawn(Location loc) {
            Zombie zombie = loc.getWorld().spawn(loc, Zombie.class);
            zombie.setBaby(_baby);
            if (_profession != null) {
                zombie.setVillagerProfession(_profession);
            }
            return zombie;
        }

        // --------------------------------------------------------------------
        /**
         * True if a baby.
         */
        protected boolean _baby;

        /**
         * Villager profession, or null for a generic zombie.
         */
        protected Profession _profession;
    }; // inner class PredefinedZombie

    // ------------------------------------------------------------------------
    /**
     * Map from lower case PredefinedCreature name to instance.
     */
    protected static HashMap<String, PredefinedCreature> BY_NAME = new HashMap<String, PredefinedCreature>();

    /**
     * Random number generator.
     */
    protected static Random _random = new Random();

    /**
     * PredefinedCreature enums delegate to this instance, set by the
     * constructor.
     */
    protected final IPredefinedCreature _implementation;

    // ------------------------------------------------------------------------
    // Initialise maps.

    static {
        for (PredefinedCreature creature : values()) {
            BY_NAME.put(creature.name().toLowerCase(), creature);
        }
    }
} // enum PredefinedCreature
