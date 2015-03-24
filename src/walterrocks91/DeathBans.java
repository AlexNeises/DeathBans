package walterrocks91;

import me.walterrocks91.Updater.UpdateResult;
import me.walterrocks91.Updater.UpdateType;
import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

@SuppressWarnings("all")
public class DeathBans extends JavaPlugin implements Listener {
    public static DeathBans instance;
    public static HashSet<CommandSender> udconfirm = new HashSet<CommandSender>();

    // �
    private static HashSet<Boolean> downloaded = new HashSet<>();
    public String timeframe = getConfig().getString("timeframe");

    // ID: 89802
    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
    String tag = getConfig().getString("messages.prefix").replaceAll("&", "�")
            + " ";
    File config = new File(getDataFolder(), "config.yml");
    File deathbans = new File(getDataFolder(), "bans.yml");
    FileConfiguration bans = YamlConfiguration
            .loadConfiguration(this.deathbans);
    File lives = new File(getDataFolder(), "lives.yml");

    // Configuration Files
    FileConfiguration life = YamlConfiguration.loadConfiguration(this.lives);
    File bantimes = new File(getDataFolder(), "bantimes.yml");
    FileConfiguration dates = YamlConfiguration
            .loadConfiguration(this.bantimes);
    private boolean donorenabled = getConfig().getBoolean("donorperms.enabled");
    private HashSet<CommandSender> confirm = new HashSet<CommandSender>();

    // On enable.
    public void onEnable() {
        // Check missing config statements.
        if (!getConfig().contains("banlength")) {
            getConfig().set("banlength", 90);
        }
        if (!getConfig().contains("timeframe")) {
            getConfig().set("timeframe", "MINUTE");
        }
        if (!getConfig().contains("startinglives")) {
            getConfig().set("startinglives", 1);
        }
        if (!getConfig().contains("update")) {
            getConfig().set("update", false);
        }
        if (!getConfig().contains("misc.join-message-enabled")) {
            getConfig().set("misc.join-message-enabled", true);
        }
        if (!getConfig().contains("misc.leave-message-enabled")) {
            getConfig().set("misc.leave-message-enabled", true);
        }
        if (!getConfig().contains("misc.death-message-enabled")) {
            getConfig().set("misc.death-message-enabled", true);
        }
        if (!getConfig().contains("messages.kick-message")) {
            getConfig().set("messages.kick-message", "&c&lYou have died.");
        }
        if (!getConfig().contains("messages.player-banned-join-message")) {
            getConfig()
                    .set("messages.player-banned-join-message",
                            "&c&lYou have been killed, you are not permitted to join the server yet.");
        }
        if (!getConfig().contains("messages.prefix")) {
            getConfig().set("messages.prefix", "&f[&cDeathBans&f]");
        }
        if (!getConfig().contains("donorperms.enabled")) {
            getConfig().set("donorperms.enabled", false);
        }
        if (!getConfig().contains("donorperms.level1")) {
            getConfig().set("donorperms.level1", "deathbans.donorone");
        }
        if (!getConfig().contains("donorperms.level2")) {
            getConfig().set("donorperms.level2", "deathbans.donortwo");
        }
        if (!getConfig().contains("donorperms.level3")) {
            getConfig().set("donorperms.level3", "deathbans.donorthree");
        }
        if (!getConfig().contains("donorperms.level4")) {
            getConfig().set("donorperms.level4", "deathbans.donorfour");
        }
        if (!getConfig().contains("donorperms.level5")) {
            getConfig().set("donorperms.level5", "deathbans.donorfive");
        }
        saveConfig();
        instance = this;
        DBApi.dbinstance = this;
        getServer().getScheduler().scheduleSyncDelayedTask(this,
                new Runnable() {
                    public void run() {
                        if (getConfig().getBoolean("update")) {
                            Updater ud = new Updater(DeathBans.instance, 89802,
                                    getFile(), UpdateType.NO_DOWNLOAD, false);
                            if (ud.getResult() == UpdateResult.UPDATE_AVAILABLE) {
                                Updater update = new Updater(
                                        DeathBans.instance, 89802, getFile(),
                                        UpdateType.DEFAULT, true);
                                if (update.getResult() == UpdateResult.SUCCESS) {
                                    DBApi.sendDBMessage(getServer()
                                                    .getConsoleSender(),
                                            "&aDownloaded latest DeathBans file, reloading to make these changes.");
                                    getServer().reload();
                                }
                            }
                        }
                    }
                }, 5 * 20);

        getServer().getPluginManager().registerEvents(this, this);
        if (!config.exists()) {
            saveDefaultConfig();
        }
        if (!deathbans.exists()) {
            try {
                bans.save(deathbans);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!lives.exists()) {
            try {
                life.save(lives);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!bantimes.exists()) {
            try {
                dates.save(bantimes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // On disable.
    public void onDisable() {
        DBApi.saveCustomConfig();
    }

    // DeathBans Commands
    @Override
    public boolean onCommand(CommandSender p, Command cmd, String cmdLabel,
                             String[] args) {
        if (cmdLabel.equalsIgnoreCase("deathbans")
                && !(p instanceof BlockCommandSender)
                || cmdLabel.equalsIgnoreCase("db")
                && !(p instanceof BlockCommandSender)) {
            if ((p.hasPermission("deathbans.admin"))
                    || (p.hasPermission("deathbans.*"))) {
                if (args.length <= 0) {
                    DBApi.sendDBMessage(p,
                            "&e/DeathBans unban &f- Unbans a banned player.");
                    DBApi.sendDBMessage(p,
                            "&e/DeathBans addlives &f- Adds lives to a player.");
                    DBApi.sendDBMessage(p,
                            "&e/DeathBans takelives &f- Remove lives from a player.");
                    DBApi.sendDBMessage(p,
                            "&e/DeathBans lives &f- Shows a players lives.");
                    DBApi.sendDBMessage(p,
                            "&e/DeathBans perms &f- Shows all deathbans permissions.");
                    DBApi.sendDBMessage(p,
                            "&e/DeahBans checkban &f- Check if a player is DeathBanned.");
                    DBApi.sendDBMessage(p,
                            "&e/DeathBans reload &f- Reloads deathbans config file.");
                    if (getConfig().getBoolean("update")) {
                        DBApi.sendDBMessage(
                                p,
                                "&e/DeathBans forceupdate &f- Forcefully updates DeathBans to the latest release.");
                    }
                    DBApi.sendDBMessage(p,
                            "&e/DeathBans reset &f- Resets &lALL &fdeathbans. &c[CONSOLE-ONLY]");
                } else {
                    if (args[0].equalsIgnoreCase("forceupdate")) {
                        if (getConfig().getBoolean("update")) {
                            if (downloaded.contains(true)) {
                                DBApi.sendDBMessage(p,
                                        "&cUpdate already downloaded, please reload/restart your server!");
                                return true;
                            }
                            Updater ud = new Updater(this, 89802, getFile(),
                                    UpdateType.NO_DOWNLOAD, false);
                            if (ud.getResult() == UpdateResult.UPDATE_AVAILABLE) {
                                if (!udconfirm.contains(p)) {
                                    udconfirm.add(p);
                                    DBApi.sendDBMessage(p,
                                            "&aUpdate Available. &oPlease type /deathbans forceupdate again to confirm.");
                                    return true;
                                }
                                Updater update = new Updater(this, 89802,
                                        getFile(), UpdateType.DEFAULT, true);
                                DBApi.sendDBMessage(p,
                                        "&aDownloading latest DeathBans file.");
                                udconfirm.remove(p);
                                if (update.getResult() == UpdateResult.SUCCESS) {
                                    DBApi.sendDBMessage(
                                            p,
                                            "&aDownloaded latest DeathBans file, please reload/restart to make these changes.");
                                    downloaded.add(true);
                                }
                            } else {
                                DBApi.sendDBMessage(p,
                                        "&cNo updates are currently available.");
                            }
                        }
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("reload")) {
                        reloadConfig();
                        DBApi.sendDBMessage(p,
                                "&aReloaded deathbans config file.");
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("reset")) {
                        if (p instanceof Player) {
                            DBApi.sendDBMessage(p,
                                    "&cOnly the console can do that!");
                            return true;
                        }
                        if (!confirm.contains(p)) {
                            confirm.add(p);
                            DBApi.sendDBMessage(
                                    p,
                                    "&aAre you sure you want to reset ALL deathbans? type /deathbans reset again to confirm.");
                            return true;
                        }
                        deathbans.delete();
                        try {
                            DBApi.resetDeathBans();
                            DBApi.sendDBMessage(p,
                                    "&a&lALL Deathbans have been reset.");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        confirm.remove(p);
                        reloadConfig();
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("unban")) {
                        if (args.length <= 1) {
                            p.sendMessage(tag + ChatColor.RED
                                    + "Invalid arguments.");
                            return true;
                        }
                        try {
                            UUID uuid = UUIDFetcher.getUUIDOf(args[1]);
                            if (DBApi.unBanPlayer(uuid)) {
                                DBApi.sendDBMessage(p,
                                        "&aSuccessfully unbanned " + args[1]);
                            } else {
                                DBApi.sendDBMessage(p,
                                        "&cThat player is not banned!");
                            }
                        } catch (Exception e) {
                            DBApi.sendDBMessage(p,
                                    "&cInvalid player. (Could not get UUID)");
                        }
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("addlives")) {
                        if (args.length <= 2) {
                            DBApi.sendDBMessage(p, "&cInvalid arguments.");
                            return true;
                        }
                        Player pl = DBApi.getPlayer(args[1]);
                        if (pl != null) {
                            DBApi.changeLives(pl, Integer.parseInt(args[2]),
                                    Method.ADDITION);
                            DBApi.sendDBMessage(
                                    p,
                                    "&aYou were given " + args[2]
                                            + " lives. Total lives: "
                                            + DBApi.getLives(pl));
                            return true;
                        } else {
                            DBApi.sendDBMessage(p, "&cInvalid player.");
                        }

                        return true;
                    }
                    if (args[0].equalsIgnoreCase("takelives")) {
                        if (args.length <= 2) {
                            p.sendMessage(tag + ChatColor.RED
                                    + "Invalid arguments.");
                            return true;
                        }
                        Player pl = DBApi.getPlayer(args[1]);
                        if (pl != null) {
                            DBApi.changeLives(pl, Integer.parseInt(args[2]),
                                    Method.SUBTRACTION);
                            DBApi.sendDBMessage(
                                    p,
                                    "&aYou have list " + args[2]
                                            + " lives. Total lives: "
                                            + DBApi.getLives(pl));
                            for (Player player : getServer().getOnlinePlayers()) {
                                if (player.hasPermission("deatbans.admin")) {
                                    DBApi.sendDBMessage(player,
                                            "&e" + p.getName() + " has given "
                                                    + args[2] + " lives to "
                                                    + args[1]);
                                }
                            }
                            return true;
                        }
                        DBApi.sendDBMessage(p, "&cInvalid player.");

                        return true;
                    }
                    if (args[0].equalsIgnoreCase("perms")) {
                        DBApi.sendDBMessage(p,
                                "&edeathbans.admin &f- Allows players to use all /deathbans commands.");
                        DBApi.sendDBMessage(p,
                                "&edeathbans.excempt &f- Exempts players from deathbans.");
                        DBApi.sendDBMessage(p,
                                "&edeathbans.* &f- Gives all permissions for deathbans.");
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("lives")) {
                        if (!(p instanceof Player)) {
                            return true;
                        }
                        Player pl = (Player) p;
                        Player target = null;
                        if (args.length == 1) {
                            target = pl;
                            DBApi.sendDBMessage(p, "&aCurrent Lives: &e"
                                    + DBApi.getLives(target));
                        } else if (args.length == 2) {
                            target = DBApi.getPlayer(args[1]);
                            if (target == null) {
                                DBApi.sendDBMessage(p, "&cInvalid player.");
                                return true;
                            }
                            DBApi.sendDBMessage(p,
                                    "&aCurrent Lives of " + target.getName()
                                            + ": &e" + DBApi.getLives(target));
                        } else {
                            DBApi.sendDBMessage(p, "&cInvalid args.");
                        }
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("checkban")) {
                        if (args.length != 2) {
                            DBApi.sendDBMessage(p, "&cInvalid arguments.");
                            return true;
                        }
                        Player target = DBApi.getPlayer(args[1]);
                        if (DBApi.isBanned(target)) {
                            DBApi.sendDBMessage(p, "&eThe player " + args[1]
                                    + " is currently &cBANNED!");
                        } else {
                            DBApi.sendDBMessage(p, "&eThe player " + args[1]
                                    + " is currently &aNOT BANNED!");
                        }
                        return true;
                    }
                    DBApi.sendDBMessage(p, "&cInvalid subcommand.");
                    return true;
                }
            } else {
                if (args.length != 1) {
                    DBApi.sendDBMessage(p,
                            "&e/DeathBans lives &f- Shows a players lives.");
                    return true;
                }
                if (args[0].equalsIgnoreCase("lives")) {
                    if (!(p instanceof Player)) {
                        return true;
                    }
                    Player pl = (Player) p;
                    Player target = null;
                    if (args.length == 1) {
                        target = pl;
                        DBApi.sendDBMessage(p,
                                "&aCurrent Lives: &e" + DBApi.getLives(target));
                    } else if (args.length == 2) {
                        target = DBApi.getPlayer(args[1]);
                        if (target == null) {
                            DBApi.sendDBMessage(p, "&cInvalid player.");
                            return true;
                        }
                        DBApi.sendDBMessage(p,
                                "&aCurrent Lives of " + target.getName()
                                        + ": &e" + DBApi.getLives(target));
                    } else {
                        DBApi.sendDBMessage(p, "&cInvalid args.");
                    }
                    return true;
                } else {
                    DBApi.sendDBMessage(p, "&cInvalid subcommand.");
                    return true;
                }
            }
        }
        return true;
    }

    // Events

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!getConfig().getBoolean("misc.join-message-enabled")) {
            e.setJoinMessage("");
        }
        Player p = e.getPlayer();
        if (!p.hasPlayedBefore()) {
            this.life.set(p.getName(), getConfig().getInt("startinglives"));
            DBApi.saveCustomConfig();
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        if (!getConfig().getBoolean("misc.leave-message-enabled")) {
            e.setQuitMessage("");
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (!getConfig().getBoolean("misc.death-message-enabled")) {
            e.setDeathMessage("");
        }
        Player p = e.getEntity();
        p.getWorld().strikeLightningEffect(p.getLocation());
        if (!p.hasPermission("deathbans.exempt")) {
            if (DBApi.getLives(p) == 0) {
                DBApi.getDates().set(p.getUniqueId().toString(),
                        sdf.format(new Date()));
                TimeFrame tf = TimeFrame.valueOf(timeframe.toUpperCase());
                if (donorenabled) {
                    DonorLevel l = DonorLevel.ZERO;
                    if (p.hasPermission(getConfig().getString(
                            "donorperms.level1"))) {
                        l = DonorLevel.ONE;
                    } else if (p.hasPermission(getConfig().getString(
                            "donorperms.level2"))) {
                        l = DonorLevel.TWO;
                    } else if (p.hasPermission(getConfig().getString(
                            "donorperms.level3"))) {
                        l = DonorLevel.THREE;
                    } else if (p.hasPermission(getConfig().getString(
                            "donorperms.level4"))) {
                        l = DonorLevel.FOUR;
                    } else if (p.hasPermission(getConfig().getString(
                            "donorperms.level5"))) {
                        l = DonorLevel.FIVE;
                    }
                    DBApi.banPlayer(p,
                            getConfig().getString("messages.kick-message"),
                            getConfig().getInt("banlength"), tf, l, true);
                } else {
                    DBApi.banPlayer(p,
                            getConfig().getString("messages.kick-message"),
                            getConfig().getInt("banlength"), tf,
                            DonorLevel.ZERO, true);
                }
            } else {
                DBApi.changeLives(p, 1, Method.SUBTRACTION);
                p.sendMessage(tag + ChatColor.GREEN
                        + "You have died, you now have " + DBApi.getLives(p)
                        + " lives left.");
            }
        }
    }

    @EventHandler
    public void onJoin(AsyncPlayerPreLoginEvent e) {
        UUID uuid = e.getUniqueId();
        if ((DBApi.getBans().contains(uuid.toString()))
                && (DBApi.getBans().getBoolean(uuid.toString()))) {
            boolean timebanned = false;
            boolean lasts = false;
            boolean color = false;
            if (getConfig().getString("messages.player-banned-join-message")
                    .contains("[lasts]")) {
                lasts = true;
            }
            if (getConfig().getString("messages.player-banned-join-message")
                    .contains("[time]")) {
                timebanned = true;
            }
            if (getConfig().getString("messages.player-banned-join-message")
                    .contains("&")) {
                color = true;
            }
            String time = DBApi.getDates().getString(uuid.toString());
            String when = "" + getConfig().getInt("banlength");
            if (color && timebanned && lasts) {
                e.disallow(
                        AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                        getConfig()
                                .getString(
                                        "messages.player-banned-join-message")
                                .replace("[time]", time)
                                .replaceAll("&", "�")
                                .replace(
                                        "[lasts]",
                                        when + " " + timeframe.toLowerCase()
                                                + "s"));
            } else if (color && timebanned) {
                e.disallow(
                        AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                        getConfig()
                                .getString(
                                        "messages.player-banned-join-message")
                                .replace("[time]", time).replaceAll("&", "�"));
            } else if (color) {
                e.disallow(
                        AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                        getConfig().getString(
                                "messages.player-banned-join-message")
                                .replaceAll("&", "�"));
            } else if (color && lasts) {
                e.disallow(
                        AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                        getConfig()
                                .getString(
                                        "messages.player-banned-join-message")
                                .replaceAll("&", "�")
                                .replace(
                                        "[lasts]",
                                        when + " " + timeframe.toLowerCase()
                                                + "s"));
            } else if (timebanned && lasts) {
                e.disallow(
                        AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                        getConfig()
                                .getString(
                                        "messages.player-banned-join-message")
                                .replace("[time]", time)
                                .replace(
                                        "[lasts]",
                                        when + " " + timeframe.toLowerCase()
                                                + "s"));
            } else if (lasts) {
                e.disallow(
                        AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                        getConfig().getString(
                                "messages.player-banned-join-message").replace(
                                "[lasts]",
                                when + " " + timeframe.toLowerCase() + "s"));
            } else if (timebanned) {
                e.disallow(
                        AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                        getConfig().getString(
                                "messages.player-banned-join-message").replace(
                                "[time]", time));
            } else {
                e.disallow(
                        AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                        getConfig().getString(
                                "messages.player-banned-join-message"));
            }
        }
    }
}