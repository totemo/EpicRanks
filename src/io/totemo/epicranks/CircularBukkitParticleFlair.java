package io.totemo.epicranks;

import org.bukkit.Effect;
import org.bukkit.Location;

// ----------------------------------------------------------------------------
/**
 * A type of flair that shows a circling particle effect.
 */
public class CircularBukkitParticleFlair implements IFlair {
    // ------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param effect the Effect to display.
     * @param xzPeriod the wave period in the XZ plane in ticks.
     * @param xzAmplitude the amplitude (radius) in the XZ plane.
     * @param yPeriod the wave period on the Y axis in ticks.
     * @param yAmplitude the Y wave amplitude.
     * @param yOffset a fixed Y offset.
     */
    public CircularBukkitParticleFlair(Effect effect, int xzPeriod, double xzAmplitude,
    int yPeriod, double yAmplitude, double yOffset) {
        this(effect, 0, xzPeriod, xzAmplitude, 0, yPeriod, yAmplitude, yOffset);
    }

    // ------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param effect the Effect to display.
     * @param xzPeriod the wave period in the XZ plane in ticks.
     * @param xzAmplitude the amplitude (radius) in the XZ plane.
     * @param yPeriod the wave period on the Y axis in ticks.
     * @param yAmplitude the Y wave amplitude.
     * @param yOffset a fixed Y offset.
     */
    public CircularBukkitParticleFlair(Effect effect, int xzPhase, int xzPeriod, double xzAmplitude,
    int yPhase, int yPeriod, double yAmplitude, double yOffset) {
        _effect = effect;
        _xzPhase = xzPhase;
        _xzPeriod = xzPeriod;
        _xzAmplitude = xzAmplitude;
        _yPhase = yPhase;
        _yPeriod = yPeriod;
        _yAmplitude = yAmplitude;
        _yOffset = yOffset;
    }

    // ------------------------------------------------------------------------
    /**
     * @see io.totemo.cobble.IFlair#tick(org.bukkit.Location)
     */
    @Override
    public void tick(Location centre) {
        double xzAngle = (_tick % _xzPeriod) * 2.0 * Math.PI / _xzPeriod;
        double yAngle = (_tick % _yPeriod) * 2.0 * Math.PI / _yPeriod;
        double x = _xzAmplitude * Math.cos(xzAngle);
        double z = _xzAmplitude * Math.sin(xzAngle);
        double y = _yAmplitude * Math.sin(yAngle) + _yOffset;
        centre.getWorld().playEffect(centre.add(x, y, z), _effect, 0, 48);
        ++_tick;
    }

    // ------------------------------------------------------------------------
    /**
     * Tick counter; updated once per tick().
     */
    protected int _tick;

    /**
     * Effect to display.
     */
    protected Effect _effect;

    /**
     * Phase advance in the XZ plane, in ticks.
     */
    protected int _xzPhase;

    /**
     * Wave period in the XZ plane, in ticks.
     */
    protected int _xzPeriod;

    /**
     * Amplitude (radius) in the XZ plane.
     */
    protected double _xzAmplitude;

    /**
     * Phase advance on the Y axis, in ticks.
     */
    protected int _yPhase;

    /**
     * Wave period on the Y axis, in ticks.
     */
    protected int _yPeriod;

    /**
     * Y wave amplitude.
     */
    protected double _yAmplitude;

    /**
     * Fixed Y offset.
     */
    protected double _yOffset;
} // class CircularBukkitParticleFlair