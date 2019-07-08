package io.github.totemo.doppelganger;

import java.util.HashMap;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieVillager;

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

    Armorer(new PredefinedVillager(false, Profession.ARMORER)),

    BabyArmorer(new PredefinedVillager(true, Profession.ARMORER)),

    ZombieArmorer(new PredefinedZombieVillager(false, Profession.ARMORER)),

    BabyZombieArmorer(new PredefinedZombieVillager(true, Profession.ARMORER)),

    Butcher(new PredefinedVillager(false, Profession.BUTCHER)),

    BabyButcher(new PredefinedVillager(true, Profession.BUTCHER)),

    ZombieButcher(new PredefinedZombieVillager(false, Profession.BUTCHER)),

    BabyZombieButcher(new PredefinedZombieVillager(true, Profession.BUTCHER)),

    Cartographer(new PredefinedVillager(false, Profession.CARTOGRAPHER)),

    BabyCartographer(new PredefinedVillager(true, Profession.CARTOGRAPHER)),

    ZombieCartographer(new PredefinedZombieVillager(false, Profession.CARTOGRAPHER)),

    BabyZombieCartographer(new PredefinedZombieVillager(true, Profession.CARTOGRAPHER)),

    Cleric(new PredefinedVillager(false, Profession.CLERIC)),

    BabyCleric(new PredefinedVillager(true, Profession.CLERIC)),

    ZombieCleric(new PredefinedZombieVillager(false, Profession.CLERIC)),

    BabyZombieCleric(new PredefinedZombieVillager(true, Profession.CLERIC)),

    Farmer(new PredefinedVillager(false, Profession.FARMER)),

    BabyFarmer(new PredefinedVillager(true, Profession.FARMER)),

    ZombieFarmer(new PredefinedZombieVillager(false, Profession.FARMER)),

    BabyZombieFarmer(new PredefinedZombieVillager(true, Profession.FARMER)),

    Fisherman(new PredefinedVillager(false, Profession.FISHERMAN)),

    BabyFisherman(new PredefinedVillager(true, Profession.FISHERMAN)),

    ZombieFisherman(new PredefinedZombieVillager(false, Profession.FISHERMAN)),

    BabyZombieFisherman(new PredefinedZombieVillager(true, Profession.FISHERMAN)),

    Fletcher(new PredefinedVillager(false, Profession.FLETCHER)),

    BabyFletcher(new PredefinedVillager(true, Profession.FLETCHER)),

    ZombieFletcher(new PredefinedZombieVillager(false, Profession.FLETCHER)),

    BabyZombieFletcher(new PredefinedZombieVillager(true, Profession.FLETCHER)),

    Leatherworker(new PredefinedVillager(false, Profession.LEATHERWORKER)),

    BabyLeatherworker(new PredefinedVillager(true, Profession.LEATHERWORKER)),

    ZombieLeatherworker(new PredefinedZombieVillager(false, Profession.LEATHERWORKER)),

    BabyZombieLeatherworker(new PredefinedZombieVillager(true, Profession.LEATHERWORKER)),

    Librarian(new PredefinedVillager(false, Profession.LIBRARIAN)),

    BabyLibrarian(new PredefinedVillager(true, Profession.LIBRARIAN)),

    ZombieLibrarian(new PredefinedZombieVillager(false, Profession.LIBRARIAN)),

    BabyZombieLibrarian(new PredefinedZombieVillager(true, Profession.LIBRARIAN)),

    Mason(new PredefinedVillager(false, Profession.MASON)),

    BabyMason(new PredefinedVillager(true, Profession.MASON)),

    ZombieMason(new PredefinedZombieVillager(false, Profession.MASON)),

    BabyZombieMason(new PredefinedZombieVillager(true, Profession.MASON)),

    Nitwit(new PredefinedVillager(false, Profession.NITWIT)),

    BabyNitwit(new PredefinedVillager(true, Profession.NITWIT)),

    ZombieNitwit(new PredefinedZombieVillager(false, Profession.NITWIT)),

    BabyZombieNitwit(new PredefinedZombieVillager(true, Profession.NITWIT)),

    Shepherd(new PredefinedVillager(false, Profession.SHEPHERD)),

    BabyShepherd(new PredefinedVillager(true, Profession.SHEPHERD)),

    ZombieShepherd(new PredefinedZombieVillager(false, Profession.SHEPHERD)),

    BabyZombieShepherd(new PredefinedZombieVillager(true, Profession.SHEPHERD)),

    Toolsmith(new PredefinedVillager(false, Profession.TOOLSMITH)),

    BabyToolsmith(new PredefinedVillager(true, Profession.TOOLSMITH)),

    ZombieToolsmith(new PredefinedZombieVillager(false, Profession.TOOLSMITH)),

    BabyZombieToolsmith(new PredefinedZombieVillager(true, Profession.TOOLSMITH)),

    Weaponsmith(new PredefinedVillager(false, Profession.WEAPONSMITH)),

    BabyWeaponsmith(new PredefinedVillager(true, Profession.WEAPONSMITH)),

    ZombieWeaponsmith(new PredefinedZombieVillager(false, Profession.WEAPONSMITH)),

    BabyZombieWeaponsmith(new PredefinedZombieVillager(true, Profession.WEAPONSMITH)),

    BabyVillager(new PredefinedVillager(true, null)),
    
    BabyZombie(new PredefinedZombie(true, null)),

    ZombieVillager(new PredefinedZombieVillager(false, null)),

    BabyZombieVillager(new PredefinedZombieVillager(true, null));

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
                villager.setVillagerExperience(1);
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
     * (adult or baby).
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
         */
        public PredefinedZombie(boolean baby, Profession profession) {
            _baby = baby;
        }

        // --------------------------------------------------------------------
        /**
         * @see io.github.totemo.doppelganger.IPredefinedCreature#isInstance(org.bukkit.entity.LivingEntity)
         */
        @Override
        public boolean isInstance(LivingEntity living) {
            return living instanceof Zombie &&
                   ((Zombie) living).isBaby() == _baby;
        }

        // --------------------------------------------------------------------
        /**
         * @see io.github.totemo.doppelganger.IPredefinedCreature#spawn(org.bukkit.Location)
         */
        @Override
        public LivingEntity spawn(Location loc) {
            Zombie zombie = loc.getWorld().spawn(loc, Zombie.class);
            zombie.setBaby(_baby);
            return zombie;
        }

        // --------------------------------------------------------------------
        /**
         * True if a baby.
         */
        protected boolean _baby;

    }; // inner class PredefinedZombie

    // ------------------------------------------------------------------------
    /**
     * PredefinedCreature implementation delegate for zombie villagers with
     * specified age (adult or baby) and villager profession.
     */
    static final class PredefinedZombieVillager implements IPredefinedCreature {
        /**
         * Constructor.
         *
         * @param baby if true, the zombie villager is a baby; otherwise it is
         *        an adult.
         * @param profession the zombie villager's profession; or null for a
         *        random profession.
         */
        public PredefinedZombieVillager(boolean baby, Profession profession) {
            _baby = baby;
            _profession = profession;
        }

        // --------------------------------------------------------------------
        /**
         * @see io.github.totemo.doppelganger.IPredefinedCreature#isInstance(org.bukkit.entity.LivingEntity)
         */
        @Override
        public boolean isInstance(LivingEntity living) {
            return living instanceof ZombieVillager &&
                   ((ZombieVillager) living).isBaby() == _baby &&
                   (_profession == null || ((ZombieVillager) living).getVillagerProfession() == _profession);
        }

        // --------------------------------------------------------------------
        /**
         * @see io.github.totemo.doppelganger.IPredefinedCreature#spawn(org.bukkit.Location)
         */
        @Override
        public LivingEntity spawn(Location loc) {
            ZombieVillager zombie = loc.getWorld().spawn(loc, ZombieVillager.class);
            zombie.setBaby(_baby);
            zombie.setVillagerProfession(_profession != null ? _profession : getRandomVillagerProfession());
            return zombie;
        }

        // --------------------------------------------------------------------
        /**
         * True if a baby.
         */
        protected boolean _baby;

        /**
         * Villager profession, or null for random.
         */
        protected Profession _profession;
    }; // inner class PredefinedZombieVillager

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
