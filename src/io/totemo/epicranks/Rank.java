package io.totemo.epicranks;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

// ----------------------------------------------------------------------------
/**
 * Represents a possible player rank.
 */
public class Rank {
    /**
     * Constructor.
     *
     * @param ordinal the zero based ordinal position of the rank in the
     *        promotion sequence.
     * @param section the configuration section containing prefix and suffix
     *        entries. The name of the section is used as the Rank's ID and team
     *        name.
     */
    public Rank(int ordinal, ConfigurationSection section) {
        _id = section.getName();
        _ordinal = ordinal;
        _prefix = ChatColor.translateAlternateColorCodes('&', section.getString("prefix"));
        _prefix = _prefix.substring(0, Math.min(16, _prefix.length()));
        _suffix = ChatColor.translateAlternateColorCodes('&', section.getString("suffix"));
        _suffix = _suffix.substring(0, Math.min(16, _suffix.length()));
        _canUseFlair = section.getBoolean("capabilities.flair");
        _canUseMount = section.getBoolean("capabilities.mount");
    }

    // ------------------------------------------------------------------------
    /**
     * Return the programmatic identifier of this rank, used as a team name.
     *
     * @return the programmatic identifier of this rank, used as a team name.
     */
    public String getId() {
        return _id;
    }

    // ------------------------------------------------------------------------
    /**
     * Return the zero based position ordinal in the promotion sequence.
     *
     * @return the zero based position ordinal in the promotion sequence.
     */
    public int getOrdinal() {
        return _ordinal;
    }

    // ------------------------------------------------------------------------
    /**
     * Return the form of this Rank when written in chat.
     *
     * @return the form of this Rank when written in chat.
     */
    public String getChatForm() {
        return (_prefix + " " + _suffix).replaceAll("\\s+", " ");
    }

    // ------------------------------------------------------------------------
    /**
     * Return the colour translated name tag prefix.
     *
     * @return the colour translated name tag prefix.
     */
    public String getPrefix() {
        return _prefix;
    }

    // ------------------------------------------------------------------------
    /**
     * Return the colour translated name tag suffix.
     *
     * @return the colour translated name tag suffix.
     */
    public String getSuffix() {
        return _suffix;
    }

    // ------------------------------------------------------------------------
    /**
     * Return true if this rank can use /flair.
     *
     * @return true if this rank can use /flair.
     */
    public boolean canUseFlair() {
        return _canUseFlair;
    }

    // ------------------------------------------------------------------------
    /**
     * Return true if this rank can use /mount.
     *
     * @return true if this rank can use /mount.
     */
    public boolean canUseMount() {
        return _canUseMount;
    }

    // ------------------------------------------------------------------------
    /**
     * Programmatic identifier, used as a team name.
     */
    protected String _id;

    /**
     * Zero based position ordinal in the promotion sequence.
     */
    protected int _ordinal;

    /**
     * Colour translated name tag prefix.
     */
    protected String _prefix;

    /**
     * Colour translated name tag suffix.
     */
    protected String _suffix;

    /**
     * True if this rank can use /flair.
     */
    protected boolean _canUseFlair;

    /**
     * True if this rank can use /mount.
     */
    protected boolean _canUseMount;
} // class Rank