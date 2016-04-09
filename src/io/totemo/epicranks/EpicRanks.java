package io.totemo.epicranks;

import io.totemo.cobble.Effects;
import io.totemo.cobble.RNG;
import io.totemo.cobble.RateLimiter;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

// ----------------------------------------------------------------------------
/**
 * The Epic Ranks plugin does custom login messages and custom name tags
 * reflecting a player's epic importance.
 *
 * High-rank players can use the /flair command to get a particle effect halo
 * and /mount to get an "epic mount".
 *
 * Mining of diamond ore is announced in chat and the miner hears ghast screams.
 */
public class EpicRanks extends JavaPlugin implements Listener {
    /**
     * Configuration wrapper.
     */
    public static Configuration CONFIG = new Configuration();

    /**
     * This plugin as a singleton.
     */
    public static EpicRanks PLUGIN;

    // ------------------------------------------------------------------------
    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {
        PLUGIN = this;
        saveDefaultConfig();
        CONFIG.reload();

        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.hasPermission("epicranks.vanished")) {
                        // Ensure that the player is on the team corresponding
                        // to their rank when ModMode is not using a Team.
                        if (_scoreboard != null) {
                            Rank rank = _ranks.get(player.getName());
                            if (rank != null) {
                                // To prevent name plate flicker, don't change
                                // Team unnecessarily.
                                Team rankTeam = _scoreboard.getTeam(rank.getId());
                                Team currentTeam = _scoreboard.getEntryTeam(player.getName());
                                // Still wondering why a simple pointer/identity
                                // comparison doesn't work in this test.
                                if (currentTeam == null || !currentTeam.getName().equals(rankTeam.getName())) {
                                    rankTeam.addEntry(player.getName());
                                }
                            }
                        }

                        Flair flair = _flairs.get(player.getName());
                        if (flair != null) {
                            flair.tick(player.getLocation());
                        }
                    }
                } // if not vanished
            } // run
        }, 1, 1);
    } // onEnable

    // ------------------------------------------------------------------------
    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
     */
    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);

        // Remove teams from ModMode's Scoreboard.
        if (_scoreboard != null) {
            for (Rank rank : CONFIG.RANKS) {
                Team team = _scoreboard.getTeam(rank.getId());
                if (team != null) {
                    team.unregister();
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender,
     *      org.bukkit.command.Command, java.lang.String, java.lang.String[])
     *
     *      Commands:
     *      <ul>
     *      <li>/epicranks reload</li>
     *      <li>/rankup (<player>|all)</li>
     *      <li>/donate</li>
     *      <li>/flair</li>
     *      <li>/mount</li>
     *      </ul>
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase(getName())) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                // TODO: fix reloading of Ranks and Teams.
                CONFIG.reload();
                sender.sendMessage(ChatColor.GOLD + getName() + " configuration reloaded.");
                return true;
            }
        } else if (command.getName().equalsIgnoreCase("rankup")) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("all")) {
                    sender.sendMessage(ChatColor.GOLD + "Promoting everyone!!!");
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        cmdRankUp(player);
                    }
                    return true;
                }

                Player player = Bukkit.getPlayer(args[0]);
                if (player != null) {
                    sender.sendMessage(ChatColor.GOLD + "Promoting " + player.getName() + ".");
                    cmdRankUp(player);
                } else {
                    sender.sendMessage(ChatColor.RED + "No matching player is online.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /" + command.getName() + " (<player>|all) - Promote a specific player or everyone.");
            }
            return true;
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (command.getName().equalsIgnoreCase("donate")) {
                cmdRankUp(player);
                return true;
            } else if (command.getName().equalsIgnoreCase("flair")) {
                if (args.length == 0) {
                    cmdFlair(player, null);
                } else if (args.length == 1) {
                    cmdFlair(player, args[0]);
                } else {
                    cmdFlair(player, "help");
                }
                return true;
            } else if (command.getName().equalsIgnoreCase("mount")) {
                cmdMount(player);
                return true;
            }
        }

        sender.sendMessage(ChatColor.RED + "Invalid command syntax.");
        return true;
    } // onCommand

    // ------------------------------------------------------------------------
    /**
     * On player join, set their display name and announce their rank.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (_scoreboard == null) {
            // First player join. Get Scoreboard from ModMode or create default.
            Scoreboard scoreboard = player.getScoreboard();
            if (scoreboard == null) {
                scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
                getLogger().info("Creating new scoreboard.");
            }
            initScoreboard(scoreboard);
        }
        player.setScoreboard(_scoreboard);

        Rank rank = _random.choose(CONFIG.RANKS);
        _ranks.put(player.getName(), rank);

        final String welcomeMessage = ChatColor.translateAlternateColorCodes('&',
                                                                             "Welcome " + rank.getChatForm() + " " + player.getName()
                                                                             + "&f to the server!");
        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
            @Override
            public void run() {
                Bukkit.getServer().broadcastMessage(welcomeMessage);
            }
        }, 1);
    }

    // ------------------------------------------------------------------------
    /**
     * When a player quits, remove them from the team that set their nameplate.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        Rank rank = _ranks.get(playerName);
        Team rankTeam = _scoreboard.getTeam(rank.getId());

        // If the player's Team is owned by this plugin, remove him from it.
        if (_scoreboard.getEntryTeam(playerName) == rankTeam) {
            rankTeam.removeEntry(playerName);
        }
    }

    // ------------------------------------------------------------------------
    /**
     * When boats would break, the player takes falling damage. Stop that.
     */
    @EventHandler()
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getCause() == DamageCause.FALL && event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Vehicle vehicle = (Vehicle) player.getVehicle();
            if (vehicle instanceof Boat && vehicle.hasMetadata(MOUNT_KEY)) {
                event.setCancelled(true);
                event.setDamage(0);
            }
        }
    }

    // ------------------------------------------------------------------------
    /**
     * When a boat that is an epic mount is destroyed, cancel that.
     *
     * Preventing the boat from being damaged with VehicleDamageEvent does not
     * work.
     */
    @EventHandler()
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        Vehicle vehicle = event.getVehicle();
        if (vehicle instanceof Boat && vehicle.hasMetadata(MOUNT_KEY)) {
            event.setCancelled(true);
        }
    }

    // ------------------------------------------------------------------------
    /**
     * When a the rider of an epic mount (boat) exits, remove the boat and the
     * bat supporting it (if it has not despawned).
     */
    @EventHandler()
    public void onVehicleExit(VehicleExitEvent event) {
        Vehicle vehicle = event.getVehicle();
        if (vehicle instanceof Boat && vehicle.hasMetadata(MOUNT_KEY)) {
            if (vehicle.getVehicle() != null && vehicle.getVehicle() instanceof Bat) {
                vehicle.getVehicle().remove();
            }
            vehicle.remove();
        }
    }

    // ------------------------------------------------------------------------
    /**
     * When a player mines a diamond, broadcast an announcement (rate limited to
     * at most once per plaer per 30 seconds).
     *
     * Also play a ghast scream at the player's location.
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (event.getBlock().getType() == Material.DIAMOND_ORE) {
            rateLimitedBroadcast(
                                 ChatColor.AQUA + "Sound the klaxons - " + player.getName() + " mined a diamond!!!",
                                 event.getPlayer(), CONFIG.RATE_BROADCAST_DIAMOND, _lastDiamondBroadcast);
            player.getWorld().playSound(player.getLocation(), Sound.GHAST_SCREAM, 4, (float) (0.5 + 1.5 * _random.nextDouble()));
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Handle the /rankup command by randomly promoting the player.
     *
     * @param player the player to be promoted.
     */
    protected void cmdRankUp(Player player) {
        if (_scoreboard == null) {
            // TODO: error message to sender.
            return;
        }

        if (!player.hasPermission("epicranks.vanished")) {
            spawnFirework(player);
        }

        // Remove the player from his old Rank's team, in case he logs out
        // before the periodic task adds him to the right team.
        Rank rank = _ranks.get(player.getName());
        Team oldTeam = _scoreboard.getTeam(rank.getId());
        oldTeam.removeEntry(player.getName());

        // Set rank == null to signify a random choice is required.
        if (_random.nextDouble() >= CONFIG.CHANCE_PROMOTION) {
            rank = null;
        } else {
            int nextOrdinal = rank.getOrdinal() + 1;
            if (nextOrdinal < CONFIG.RANKS.size()) {
                rank = CONFIG.RANKS.get(nextOrdinal);
            } else {
                rank = null;
            }
        }
        if (rank == null) {
            rank = _random.choose(CONFIG.RANKS);
        }

        _ranks.put(player.getName(), rank);
        rateLimitedBroadcast(
                             player.getName() + " has been promoted to " + rank.getChatForm(),
                             player, CONFIG.RATE_BROADCAST_RANK, _lastRankBroadcast);

        if (rank.canUseFlair()) {
            player.sendMessage(ChatColor.GOLD + "You can now use /flair!!!");
        } else {
            player.sendMessage(ChatColor.GOLD + "Donate today for /flair and other AWESOME donor perks!!!");
        }
        if (rank.canUseMount()) {
            player.sendMessage(ChatColor.GOLD + "You can now use /mount!!!");
        } else {
            player.sendMessage(ChatColor.GOLD + "Donate today for /mount and other AWESOME donor perks!!!");
        }
    } // cmdRankUp

    // ------------------------------------------------------------------------
    /**
     * Handle the /flair [help|none|<name>] command.
     *
     * @param player the Player.
     * @param flairName the name of the flair to use.
     */
    protected void cmdFlair(Player player, String flairName) {
        if (!_ranks.get(player.getName()).canUseFlair()) {
            player.sendMessage(ChatColor.GOLD + "Donate today for /flair and other AWESOME donor perks!!!");
            return;
        }

        if (flairName == null) {
            Flair flair = _flairs.get(player.getName());
            if (flair != null) {
                int newOrdinal = flair.ordinal() + 1;
                if (newOrdinal >= Flair.COUNT) {
                    flair = null;
                } else {
                    flair = Flair.VALUES[newOrdinal];
                }
            } else {
                flair = Flair.VALUES[0];
            }
            setFlair(player, flair);
        } else if (flairName.equalsIgnoreCase("HELP")) {
            player.sendMessage(ChatColor.GOLD + "Usage: /flair [help|<name>] - Set the named flair, or the next flair if no name is specified.");
            player.sendMessage(ChatColor.GOLD + "Valid names are: NONE, " + Flair.NAMES);
        } else if (flairName.equalsIgnoreCase("NONE")) {
            setFlair(player, null);
        } else {
            try {
                setFlair(player, Flair.valueOf(flairName.toUpperCase()));
            } catch (IllegalArgumentException ex) {
                player.sendMessage(ChatColor.RED + "\"" + flairName + "\" is not a valid flair name. Try \"/flair help\".");
            }
        }
    } // changeFlair

    // ------------------------------------------------------------------------
    /**
     * Handle the /mount command.
     *
     * @param player the player running the command.
     */
    protected void cmdMount(Player player) {
        if (!_ranks.get(player.getName()).canUseMount()) {
            player.sendMessage(ChatColor.GOLD + "Donate today for /mount and other AWESOME donor perks!!!");
            return;
        }

        Entity vehicle = player.getVehicle();
        if (vehicle != null) {
            vehicle.eject();
            if (vehicle instanceof Minecart && vehicle.getVehicle() instanceof Bat) {
                vehicle.getVehicle().remove();
                vehicle.remove();
            }
        }

        Location loc = player.getLocation();
        Boat mount = (Boat) loc.getWorld().spawnEntity(loc, EntityType.BOAT);
        mount.setPassenger(player);
        mount.setMetadata(MOUNT_KEY, new FixedMetadataValue(this, Boolean.TRUE));
        Bat bat = (Bat) loc.getWorld().spawnEntity(loc, EntityType.BAT);
        bat.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 0x7FFF_FFFF, 1), true);
        bat.setPassenger(mount);

        player.sendMessage(ChatColor.GOLD + "You fly away on your epic mount!!! (Just watch the landing!)");
    }

    // ------------------------------------------------------------------------
    /**
     * Initialise the specified Scoreboard with a Team for each {@link Rank}.
     *
     * Note: The Scoreboard API documentation implies that only the main
     * scoreboard is persistent, but in fact all Scoreboards are.
     *
     * The ModMode plugin already creates a scoreboard and adds all players to
     * it for the purposes of setting vanished players' name tag colours.
     * Players can't be on two scoreboards, so when the first player joins, we
     * get the Scoreboard that ModMode has set and update it with the Teams we
     * need. After the first player, we use the same scoreboard for all other
     * players who join.
     *
     * A periodic task is used to set the player's team, as simply the easiest
     * way of sensing when ModMode has done messing with the player's name tag
     * and we can use it again.
     *
     * @param scoreboard the scoreboard to add teams to.
     */
    protected void initScoreboard(Scoreboard scoreboard) {
        if (_scoreboard == null) {
            _scoreboard = scoreboard;
            for (Rank rank : CONFIG.RANKS) {
                Team team = _scoreboard.getTeam(rank.getId());
                if (team == null) {
                    team = _scoreboard.registerNewTeam(rank.getId());
                }
                team.setPrefix(rank.getPrefix());
                team.setSuffix(rank.getSuffix());
                team.setNameTagVisibility(NameTagVisibility.ALWAYS);
            }
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Set the Player's flair and tell them what changes to.
     *
     * @param player the Player.
     * @param flair the flair to use, or null to set no flair.
     */
    protected void setFlair(Player player, Flair flair) {
        _flairs.put(player.getName(), flair);
        if (flair == null) {
            player.sendMessage(ChatColor.GOLD + "Your flair has been cleared. Use /flair again to change it!");
        } else {
            player.sendMessage(ChatColor.GOLD + "Your flair is now " + flair.name() + "!!!");
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Broadcast a message about a player, rate limited.
     *
     * @param message the broadcast message.
     * @param player the player triggering the broadcast.
     * @param minMillis the minimum period between broadcasts in milliseconds.
     * @param map the map from player name to corresponding {@link RateLimiter}
     */
    protected void rateLimitedBroadcast(String message, Player player, long minMillis, HashMap<String, RateLimiter> map) {
        RateLimiter limiter = map.get(player.getName());
        if (limiter == null) {
            limiter = new RateLimiter(minMillis);
            map.put(player.getName(), limiter);
        }
        if (limiter.canActNow()) {
            Bukkit.broadcastMessage(message);
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Spawn a firework at the recipient.
     *
     * @param recipient player whose location is used as the origin of the
     *        spawned firework.
     */
    protected void spawnFirework(Player recipient) {
        FireworkEffect.Builder builder = FireworkEffect.builder();
        Effects.randomType(builder, FIREWORK_TYPES, 0.3, 0.3);
        Effects.randomPrimaries(builder, 1, 4, Color.fromRGB(0, 0, 0), Color.fromRGB(255, 255, 255));
        Effects.randomFades(builder, 1, 3, Color.fromRGB(0, 0, 0), Color.fromRGB(255, 255, 255));
        Effects.spawnFirework(recipient.getLocation(), builder.build(), _random.nextInt(0, 2));
    }

    // ------------------------------------------------------------------------
    /**
     * Key of metadata attached to epic mounts (boats).
     *
     * Boats with this key will be removed on dismount, even if the bat is gone.
     */
    protected static final String MOUNT_KEY = "EpicRanks_Mount";

    /**
     * Firework types.
     */
    protected static final FireworkEffect.Type[] FIREWORK_TYPES = FireworkEffect.Type.values();

    /**
     * Map from player name to (transient) {@link Rank}.
     */
    protected HashMap<String, Rank> _ranks = new HashMap<String, Rank>();

    /**
     * Map from player name to {@link Flair}.
     */
    protected HashMap<String, Flair> _flairs = new HashMap<String, Flair>();

    /**
     * Map from player name to {@link RateLimiter} limiting broadcasts about
     * that player finding diamonds.
     */
    protected HashMap<String, RateLimiter> _lastDiamondBroadcast = new HashMap<String, RateLimiter>();

    /**
     * Map from player name to {@link RateLimiter} limiting broadcasts about
     * that player changing rank.
     */
    protected HashMap<String, RateLimiter> _lastRankBroadcast = new HashMap<String, RateLimiter>();

    /**
     * Scoreboard used for nameplates.
     */
    protected Scoreboard _scoreboard;

    /**
     * Random number generator.
     */
    protected RNG _random = new RNG();
} // class EpicRanks