package io.totemo.epicranks;

import org.bukkit.Location;

// ----------------------------------------------------------------------------
/**
 * An {@link IFlair} implementation that runs multiple flairs in each tick.
 */
public class ConcurrentFlair implements IFlair {
    // ------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param flairs subsidiary {@link IFlair} instances to run in each
     *        {@link IFlair#tick(Location)}.
     */
    public ConcurrentFlair(IFlair... flairs) {
        _flairs = flairs;
    }

    // ------------------------------------------------------------------------
    /**
     * @see io.totemo.cobble.IFlair#tick(org.bukkit.Location)
     */
    @Override
    public void tick(Location centre) {
        for (IFlair flair : _flairs) {
            Location origin = centre.clone();
            flair.tick(origin);
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Subsidiary flairs.
     */
    protected IFlair[] _flairs;
} // class ConcurrentFlair