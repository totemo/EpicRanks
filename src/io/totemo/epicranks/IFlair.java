package io.totemo.epicranks;

import org.bukkit.Location;

// ----------------------------------------------------------------------------
/**
 * Interface implemented by flair to show efffects.
 */
public interface IFlair {
    // ------------------------------------------------------------------------
    /**
     * Show the flair centred on a location.
     *
     * @param centre the centre of the flair effect.
     */
    public void tick(Location centre);

} // class IFlair