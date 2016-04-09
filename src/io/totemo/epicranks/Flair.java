package io.totemo.epicranks;

import org.bukkit.Effect;
import org.bukkit.Location;

// ----------------------------------------------------------------------------
/**
 * Player flair.
 *
 * Typical flair is particle effects swirling around a player.
 */
public enum Flair implements IFlair {
    NOTES(new CircularBukkitParticleFlair(Effect.NOTE, 40, 1.0, 20, 0.25, 0.35)),
    HEARTS(new CircularBukkitParticleFlair(Effect.HEART, 80, 1.0, 40, 0.2, 2.0)),
    TIMELORD(new ConcurrentFlair(
        new CircularBukkitParticleFlair(Effect.HEART, 80, 1.0, 40, 0.2, 2.1),
        new CircularBukkitParticleFlair(Effect.HEART, -35, 1.0, 40, 0.2, 2.1))),
    MAGIC(new CircularBukkitParticleFlair(Effect.FLYING_GLYPH, 100, 1.0, 5, 0.0, 0.0)),
    ELECTRON(new CircularBukkitParticleFlair(Effect.COLOURED_DUST, 100, 1.5, 60, 0.2, 1.0)),
    ATOM(new ConcurrentFlair(
        new CircularBukkitParticleFlair(Effect.COLOURED_DUST, 100, 1.5, 100, 0.7, 1.0),
        new CircularBukkitParticleFlair(Effect.COLOURED_DUST, -90, 1.5, 90, 0.5, 1.0),
        new CircularBukkitParticleFlair(Effect.COLOURED_DUST, -80, 1.5, -80, 0.4, 1.0))),
    ENDERMAN(new CircularBukkitParticleFlair(Effect.ENDER_SIGNAL, 200, 0.0, 5, 0.0, 0.25)),
    HAPPY(new CircularBukkitParticleFlair(Effect.HAPPY_VILLAGER, 20, 1.0, 5, 0.0, 2.1)),
    RAIN(new CircularBukkitParticleFlair(Effect.WATERDRIP, 20, 0.5, 5, 0.5, 2.5)),
    LAVA(new CircularBukkitParticleFlair(Effect.LAVADRIP, 20, 0.5, 5, 0.5, 2.5)),
    EMBERS(new CircularBukkitParticleFlair(Effect.LAVA_POP, 200, 1.5, 5, 0.0, 0.0)),
    GLOOM(new CircularBukkitParticleFlair(Effect.VOID_FOG, 80, 1.5, 5, 0.3, 1.0)),
    FIRE(new CircularBukkitParticleFlair(Effect.MOBSPAWNER_FLAMES, 20, 1.5, 5, 0.0, 0.5)),
    SPARKS(new CircularBukkitParticleFlair(Effect.CRIT, 200, 0, 5, 0.0, 1.0)),
    STEAM(new CircularBukkitParticleFlair(Effect.INSTANT_SPELL, 20, 0, 5, 0.0, 2.0)),
    FORCEFIELD(new CircularBukkitParticleFlair(Effect.MAGIC_CRIT, 200, 0, 5, 0.0, 0.75)),
    WITCH(new CircularBukkitParticleFlair(Effect.WITCH_MAGIC, 80, 1.0, 5, 0.0, 0.0)),
    POTIONS(new CircularBukkitParticleFlair(Effect.POTION_SWIRL_TRANSPARENT, 200, 1.5, 10, 0.5, 0.25)),
    SPLASH(new CircularBukkitParticleFlair(Effect.SPLASH, 100, 1.0, 10, 0.0, 0.0)),

    ;

    /**
     * All Flair values.
     *
     * Computed only once, for efficiency.
     */
    public static final Flair[] VALUES = values();

    /**
     * All flair names, separated by commas.
     */
    public static final String NAMES;

    /**
     * Total number of distinct values.
     */
    public static final int COUNT = VALUES.length;

    // ------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param implementation the implementation.
     */
    Flair(IFlair implementation) {
        _implementation = implementation;
    }

    // ------------------------------------------------------------------------
    /**
     * @see io.totemo.cobble.IFlair#tick(org.bukkit.Location)
     */
    @Override
    public void tick(Location centre) {
        _implementation.tick(centre);
    }

    // ------------------------------------------------------------------------
    /**
     * Flair effect implementation.
     */
    protected IFlair _implementation;

    static {
        StringBuilder names = new StringBuilder();
        String separator = "";
        for (Flair flair : VALUES) {
            names.append(separator);
            names.append(flair.name());
            separator = ", ";
        }
        NAMES = names.toString();
    }
} // enum Flair