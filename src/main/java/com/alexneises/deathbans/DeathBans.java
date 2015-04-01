/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alexneises.deathbans;

/**
 *
 * @author Neises
 */
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

@SuppressWarnings("all")
public class DeathBans extends JavaPlugin implements Listener {

    // Instance of this class.
    public static DeathBans instance;
    // Used for confirming forced updates.
    public static HashSet<CommandSender> udconfirm = new HashSet();
    // Used to test if a update has already been downloaded before
    // reloading/restarting.
    private static HashSet<Boolean> downloaded = new HashSet();
    // Date Format for ban times.
    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
    // Getting the prefix for all commands.
    String tag = getConfig().getString("messages.prefix").replaceAll("&", "§")
            + " ";
    // Files.
    File config = new File(getDataFolder(), "config.yml");
    File deathbans = new File(getDataFolder(), "bans.yml");
    File lives = new File(getDataFolder(), "lives.yml");
    File bantimes = new File(getDataFolder(), "bantimes.yml");
    File banned = new File(getDataFolder(), "bannedplayers.yml");
    // Configs
    FileConfiguration bans = YamlConfiguration
            .loadConfiguration(this.deathbans);
    FileConfiguration life = YamlConfiguration.loadConfiguration(this.lives);
    FileConfiguration dates = YamlConfiguration
            .loadConfiguration(this.bantimes);
    FileConfiguration ban = YamlConfiguration.loadConfiguration(this.banned);
    // Getting the timeframe from the config as a string.
    private String timeframe = getConfig().getString("timeframe");
    // Testing if donor perms are enabled.
    private boolean donorenabled = getConfig().getBoolean("donorperms.enabled");
    // Used for testing if deathbans reset is confirmed.
    private HashSet<CommandSender> confirm = new HashSet();

    // On enable (lots of random config based stuff)
    public void onEnable() {
        instance = this;
        DBApi.dbinstance = this;
        DBApi.updateConfig(getConfig(), "banlength", 90);
        DBApi.updateConfig(getConfig(), "timeframe", "MINUTE");
        DBApi.updateConfig(getConfig(), "startinglives", 1);
        DBApi.updateConfig(getConfig(), "update", false);
        DBApi.updateConfig(getConfig(), "misc.join-message-enabled", false);
        DBApi.updateConfig(getConfig(), "misc.leave-message-enabled", false);
        DBApi.updateConfig(getConfig(), "misc.death-message-enabled", false);
        DBApi.updateConfig(getConfig(), "messages.kick-message",
                "&c&lYou have died.");
        DBApi.updateConfig(getConfig(), "messages.player-banned-join-message",
                "&c&lYou have been killed, you are not permitted to join the server yet.");
        DBApi.updateConfig(getConfig(), "messages.prefix", "&f[&cDeathBans&f]");
        DBApi.updateConfig(getConfig(), "donorperms.enabled", false);
        DBApi.updateConfig(getConfig(), "donorperms.level1",
                "deathbans.donorone");
        DBApi.updateConfig(getConfig(), "donorperms.level2",
                "deathbans.donortwo");
        DBApi.updateConfig(getConfig(), "donorperms.level3",
                "deathbans.donorthree");
        DBApi.updateConfig(getConfig(), "donorperms.level4",
                "deathbans.donorfour");
        DBApi.updateConfig(getConfig(), "donorperms.level5",
                "deathbans.donorfive");
        DBApi.updateConfig(ban, "bannedplayers", new ArrayList<String>());
        getServer().getScheduler().scheduleSyncDelayedTask(this,
                new Runnable() {
                    public void run() {
                        if (DeathBans.this.getConfig().getBoolean("update")) {
                            Updater ud = new Updater(DeathBans.instance, 89802,
                                    DeathBans.this.getFile(),
                                    Updater.UpdateType.NO_DOWNLOAD, false);
                            if (ud.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE) {
                                Updater update = new Updater(
                                        DeathBans.instance, 89802,
                                        DeathBans.this.getFile(),
                                        Updater.UpdateType.DEFAULT, true);
                                if (update.getResult() == Updater.UpdateResult.SUCCESS) {
                                    DBApi.sendDBMessage(DeathBans.this
                                                    .getServer().getConsoleSender(),
                                            "&aDownloaded latest DeathBans file, reloading to make these changes.");
                                    DeathBans.this.getServer().reload();
                                }
                            }
                        }
                    }
                }, 100L);

        getServer().getPluginManager().registerEvents(this, this);
        if (!this.config.exists()) {
            saveDefaultConfig();
        }
        if (!this.deathbans.exists()) {
            try {
                this.bans.save(this.deathbans);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!this.lives.exists()) {
            try {
                this.life.save(this.lives);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!this.bantimes.exists()) {
            try {
                this.dates.save(this.bantimes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!this.banned.exists()) {
            try {
                this.ban.save(this.banned);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Saving configuration files on disable.
    public void onDisable() {
        saveConfig();
        DBApi.saveCustomConfig();
    }

    // Commands.
    public boolean onCommand(CommandSender p, Command cmd, String cmdLabel,
                             String[] args) {
        if (((cmdLabel.equalsIgnoreCase("deathbans")) && (!(p instanceof BlockCommandSender)))
                || ((cmdLabel.equalsIgnoreCase("db")) && (!(p instanceof BlockCommandSender)))) {
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
                            "&e/DeathBans list &f- Gets a list of deathbanned players.");
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
                    if (args[0].equalsIgnoreCase("list")) {
                        DBApi.sendDBMessage(p, "&eCurrently banned players:");
                        if (ban.getStringList("bannedplayers").isEmpty()) {
                            DBApi.sendDBMessage(p, "- NONE");
                        }
                        for (String s : ban.getStringList("bannedplayers")) {
                            DBApi.sendDBMessage(p, "- " + s);
                        }
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("forceupdate")) {
                        if (getConfig().getBoolean("update")) {
                            if (downloaded.contains(Boolean.valueOf(true))) {
                                DBApi.sendDBMessage(p,
                                        "&cUpdate already downloaded, please reload/restart your server!");
                                return true;
                            }
                            Updater ud = new Updater(this, 89802, getFile(),
                                    Updater.UpdateType.NO_DOWNLOAD, false);
                            if (ud.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE) {
                                if (!udconfirm.contains(p)) {
                                    udconfirm.add(p);
                                    DBApi.sendDBMessage(p,
                                            "&aUpdate Available. &oPlease type /deathbans forceupdate again to confirm.");
                                    return true;
                                }
                                Updater update = new Updater(this, 89802,
                                        getFile(), Updater.UpdateType.DEFAULT,
                                        true);
                                DBApi.sendDBMessage(p,
                                        "&aDownloading latest DeathBans file.");
                                udconfirm.remove(p);
                                if (update.getResult() == Updater.UpdateResult.SUCCESS) {
                                    DBApi.sendDBMessage(
                                            p,
                                            "&aDownloaded latest DeathBans file, please reload/restart to make these changes.");
                                    downloaded.add(Boolean.valueOf(true));
                                }
                            } else {
                                DBApi.sendDBMessage(p,
                                        "&cNo updates are currently available.");
                            }
                            return true;
                        }
                    }
                    if (args[0].equalsIgnoreCase("reload")) {
                        reloadConfig();
                        DBApi.sendDBMessage(p,
                                "&aReloaded deathbans config file.");
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("reset")) {
                        if ((p instanceof Player)) {
                            DBApi.sendDBMessage(p,
                                    "&cOnly the console can do that!");
                            return true;
                        }
                        if (!this.confirm.contains(p)) {
                            this.confirm.add(p);
                            DBApi.sendDBMessage(
                                    p,
                                    "&aAre you sure you want to reset ALL deathbans? type /deathbans reset again to confirm.");
                            return true;
                        }
                        this.deathbans.delete();
                        try {
                            DBApi.resetDeathBans();
                            DBApi.sendDBMessage(p,
                                    "&a&lALL Deathbans have been reset.");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        this.confirm.remove(p);
                        reloadConfig();
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("unban")) {
                        if (args.length <= 1) {
                            p.sendMessage(this.tag + ChatColor.RED
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
                        }
                        DBApi.sendDBMessage(p, "&cInvalid player.");

                        return true;
                    }
                    if (args[0].equalsIgnoreCase("takelives")) {
                        if (args.length <= 2) {
                            p.sendMessage(this.tag + ChatColor.RED
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
                }
                DBApi.sendDBMessage(p, "&cInvalid subcommand.");
                return true;
            }
        }
        return true;
    }

    // Join messages & lives.
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!getConfig().getBoolean("misc.join-message-enabled")) {
            e.setJoinMessage("");
        }
        Player p = e.getPlayer();
        if (!p.hasPlayedBefore()) {
            this.life.set(p.getName(),
                    Integer.valueOf(getConfig().getInt("startinglives")));
            DBApi.saveCustomConfig();
        }
    }

    // Leave messages.
    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        if (!getConfig().getBoolean("misc.leave-message-enabled")) {
            e.setQuitMessage("");
        }
    }

    // Live handling & ban handling.
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
                        this.sdf.format(new Date()));
                TimeFrame tf = TimeFrame.valueOf(this.timeframe.toUpperCase());
                if (this.donorenabled) {
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
                p.sendMessage(this.tag + ChatColor.GREEN
                        + "You have died, you now have " + DBApi.getLives(p)
                        + " lives left.");
            }
        }
    }

    // Handling logins for bans.
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
            int when = getConfig().getInt("banlength");
            if ((color) && (timebanned) && (lasts)) {
                e.disallow(
                        AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                        getConfig()
                                .getString(
                                        "messages.player-banned-join-message")
                                .replace("[time]", time)
                                .replaceAll("&", "§")
                                .replace(
                                        "[lasts]",
                                        when + " "
                                                + this.timeframe.toLowerCase()
                                                + "s"));
            } else if ((color) && (timebanned)) {
                e.disallow(
                        AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                        getConfig()
                                .getString(
                                        "messages.player-banned-join-message")
                                .replace("[time]", time).replaceAll("&", "§"));
            } else if (color) {
                e.disallow(
                        AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                        getConfig().getString(
                                "messages.player-banned-join-message")
                                .replaceAll("&", "§"));
            } else if ((color) && (lasts)) {
                e.disallow(
                        AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                        getConfig()
                                .getString(
                                        "messages.player-banned-join-message")
                                .replaceAll("&", "§")
                                .replace(
                                        "[lasts]",
                                        when + " "
                                                + this.timeframe.toLowerCase()
                                                + "s"));
            } else if ((timebanned) && (lasts)) {
                e.disallow(
                        AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                        getConfig()
                                .getString(
                                        "messages.player-banned-join-message")
                                .replace("[time]", time)
                                .replace(
                                        "[lasts]",
                                        when + " "
                                                + this.timeframe.toLowerCase()
                                                + "s"));
            } else if (lasts) {
                e.disallow(
                        AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                        getConfig().getString(
                                "messages.player-banned-join-message")
                                .replace(
                                        "[lasts]",
                                        when + " "
                                                + this.timeframe.toLowerCase()
                                                + "s"));
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
