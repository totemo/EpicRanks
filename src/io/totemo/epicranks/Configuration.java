package io.totemo.epicranks;

import java.util.ArrayList;

import org.bukkit.configuration.ConfigurationSection;

// ----------------------------------------------------------------------------
/**
 * Reads and exposes the plugin configuration.
 */
public class Configuration {
    /**
     * Chance that /promote will lead to an increment in rank.
     *
     * Otherwise, /promote randomly selects a new rank.
     */
    public double CHANCE_PROMOTION;

    /**
     * Rank strings with new line characters indicating where to split the
     * string to create lines of the overhead hologram. Newlines are replaced
     * with spaces when showing the rank in chat.
     */
    public ArrayList<Rank> RANKS = new ArrayList<Rank>();

    /**
     * Minimum elapsed time in milliseconds between broadcasts about a specific
     * player finding a diamond.
     */
    public long RATE_BROADCAST_DIAMOND;

    /**
     * Minimum elapsed time in milliseconds between broadcasts about a specific
     * player changing rank.
     */
    public long RATE_BROADCAST_RANK;

    // ------------------------------------------------------------------------
    /**
     * Load the plugin configuration.
     */
    public void reload() {
        EpicRanks.PLUGIN.reloadConfig();

        CHANCE_PROMOTION = EpicRanks.PLUGIN.getConfig().getDouble("chance.promotion");

        RANKS.clear();
        ConfigurationSection rankInfo = EpicRanks.PLUGIN.getConfig().getConfigurationSection("ranks.info");
        for (String id : EpicRanks.PLUGIN.getConfig().getStringList("ranks.order")) {
            EpicRanks.PLUGIN.getLogger().info("Loading rank " + RANKS.size() + " " + id);
            RANKS.add(new Rank(RANKS.size(), rankInfo.getConfigurationSection(id)));
        }

        RATE_BROADCAST_DIAMOND = EpicRanks.PLUGIN.getConfig().getLong("rate.broadcast.diamond");
        RATE_BROADCAST_RANK = EpicRanks.PLUGIN.getConfig().getLong("rate.broadcast.rank");
    } // reload
} // class Configuration;