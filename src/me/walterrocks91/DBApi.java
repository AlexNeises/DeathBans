package me.walterrocks91;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("all")
public abstract class DBApi implements Plugin {
    static DeathBans dbinstance;
    static DBApi instance;

    private DBApi() {
        instance = this;
    }

    public static UUID getUUID(String playerName) {
        try {
            return UUIDFetcher.getUUIDOf(playerName);
        } catch (Exception e) {
        }
        return null;
    }

    public static FileConfiguration getLives() {
        if (dbinstance.life == null) {
            try {
                dbinstance.life.save(dbinstance.lives);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dbinstance.life;
    }

    public static FileConfiguration getBans() {
        if (dbinstance.bans == null) {
            try {
                dbinstance.bans.save(dbinstance.deathbans);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dbinstance.bans;
    }

    public static FileConfiguration getDates() {
        if (dbinstance.bantimes == null) {
            try {
                dbinstance.dates.save(dbinstance.bantimes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dbinstance.dates;
    }

    public static void sendDBMessage(CommandSender c, String msg) {
        if (msg.contains("&")) {
            msg = msg.replaceAll("&", "§");
        }
        c.sendMessage(dbinstance.tag + msg);
    }

    public static String getBanTime(Player p) {
        return getDates().getString(p.getUniqueId().toString());
    }

    public static String getBanTime(String name) {
        return getDates().getString(getPlayer(name).getUniqueId().toString());
    }

    public static String getBanTime(UUID uuid) {
        return getDates().getString(uuid.toString());
    }

    public static void broadcastDBMessage(String msg) {
        if (msg.contains("&")) {
            msg = msg.replaceAll("&", "§");
        }
        dbinstance.getServer().broadcastMessage(dbinstance.tag + msg);
    }

    public static boolean banPlayer(final Player p, String kickReason,
                                    int time, TimeFrame tf, DonorLevel dl, boolean permOverride) {
        if ((permOverride) && (p.hasPermission("deathbans.exempt"))) {
            return false;
        }
        int level = 1;
        if (dl == DonorLevel.ONE) {
            level = 2;
        } else if (dl == DonorLevel.TWO) {
            level = 3;
        } else if (dl == DonorLevel.THREE) {
            level = 4;
        } else if (dl == DonorLevel.FOUR) {
            level = 5;
        } else if (dl == DonorLevel.FIVE) {
            level = 6;
        } else {
            level = 1;
        }
        p.kickPlayer(kickReason.replaceAll("&", "§"));
        List<String> banned = dbinstance.ban.getStringList("bannedplayers");
        banned.add(p.getName());
        dbinstance.ban.set("bannedplayers", banned);
        saveCustomConfig();
        if (tf == TimeFrame.MINUTE) {
            getBans().set(p.getUniqueId().toString(), Boolean.valueOf(true));
            dbinstance.getServer().getScheduler()
                    .scheduleSyncDelayedTask(
                            dbinstance,
                            new Runnable() {
                                public void run() {
                                    DBApi.getBans().set(
                                            p.getUniqueId().toString(),
                                            Boolean.valueOf(false));
                                    List<String> banned = DBApi.instance
                                            .getConfig().getStringList(
                                                    "bannedplayers");
                                    banned.remove(p.getName());
                                    DBApi.instance.getConfig().set(
                                            "bannedplayers", banned);
                                }
                            },
                            dbinstance.getConfig().getInt("banlength") * 20
                                    * 60 / level);
        } else if (tf == TimeFrame.HOUR) {
            getBans().set(p.getUniqueId().toString(), Boolean.valueOf(true));
            dbinstance.getServer().getScheduler()
                    .scheduleSyncDelayedTask(
                            dbinstance,
                            new Runnable() {
                                public void run() {
                                    DBApi.getBans().set(
                                            p.getUniqueId().toString(),
                                            Boolean.valueOf(false));
                                    List<String> banned = DBApi.instance
                                            .getConfig().getStringList(
                                                    "bannedplayers");
                                    banned.remove(p.getName());
                                    DBApi.instance.getConfig().set(
                                            "bannedplayers", banned);
                                }
                            },
                            dbinstance.getConfig().getInt("banlength") * 20
                                    * 60 * 60 / level);
        } else if (tf == TimeFrame.DAY) {
            getBans().set(p.getUniqueId().toString(), Boolean.valueOf(true));
            dbinstance.getServer().getScheduler()
                    .scheduleSyncDelayedTask(
                            dbinstance,
                            new Runnable() {
                                public void run() {
                                    DBApi.getBans().set(
                                            p.getUniqueId().toString(),
                                            Boolean.valueOf(false));
                                    List<String> banned = DBApi.instance
                                            .getConfig().getStringList(
                                                    "bannedplayers");
                                    banned.remove(p.getName());
                                    DBApi.instance.getConfig().set(
                                            "bannedplayers", banned);
                                }
                            },
                            dbinstance.getConfig().getInt("banlength") * 20
                                    * 60 * 60 * 24 / level);
        } else {
            getBans().set(p.getUniqueId().toString(), Boolean.valueOf(true));
            dbinstance.getServer().getScheduler()
                    .scheduleSyncDelayedTask(dbinstance, new Runnable() {
                        public void run() {
                            DBApi.getBans().set(p.getUniqueId().toString(),
                                    Boolean.valueOf(false));
                            List<String> banned = DBApi.instance.getConfig()
                                    .getStringList("bannedplayers");
                            banned.remove(p.getName());
                            DBApi.instance.getConfig().set("bannedplayers",
                                    banned);
                        }
                    }, dbinstance.getConfig().getInt("banlength") * 20 / level);
        }
        saveCustomConfig();
        return isBanned(p);
    }

    public static boolean banPlayer(String name, String kickReason, int time,
                                    TimeFrame tf, DonorLevel dl, boolean permOverride) {
        final Player p = getPlayer(name);
        if ((permOverride) && (p.hasPermission("deathbans.exempt"))) {
            return false;
        }
        int level = 1;
        if (dl == DonorLevel.ONE) {
            level = 2;
        } else if (dl == DonorLevel.TWO) {
            level = 3;
        } else if (dl == DonorLevel.THREE) {
            level = 4;
        } else if (dl == DonorLevel.FOUR) {
            level = 5;
        } else if (dl == DonorLevel.FIVE) {
            level = 6;
        } else {
            level = 1;
        }
        List<String> banned = dbinstance.ban.getStringList("bannedplayers");
        banned.add(p.getName());
        dbinstance.ban.set("bannedplayers", banned);
        p.kickPlayer(kickReason.replaceAll("&", "§"));
        if (tf == TimeFrame.MINUTE) {
            getBans().set(p.getUniqueId().toString(), Boolean.valueOf(true));
            dbinstance.getServer().getScheduler()
                    .scheduleSyncDelayedTask(
                            dbinstance,
                            new Runnable() {
                                public void run() {
                                    DBApi.getBans().set(
                                            p.getUniqueId().toString(),
                                            Boolean.valueOf(false));
                                }
                            },
                            dbinstance.getConfig().getInt("banlength") * 20
                                    * 60 / level);
        } else if (tf == TimeFrame.HOUR) {
            getBans().set(p.getUniqueId().toString(), Boolean.valueOf(true));
            dbinstance.getServer().getScheduler()
                    .scheduleSyncDelayedTask(
                            dbinstance,
                            new Runnable() {
                                public void run() {
                                    DBApi.getBans().set(
                                            p.getUniqueId().toString(),
                                            Boolean.valueOf(false));
                                }
                            },
                            dbinstance.getConfig().getInt("banlength") * 20
                                    * 60 * 60 / level);
        } else if (tf == TimeFrame.DAY) {
            getBans().set(p.getUniqueId().toString(), Boolean.valueOf(true));
            dbinstance.getServer().getScheduler()
                    .scheduleSyncDelayedTask(
                            dbinstance,
                            new Runnable() {
                                public void run() {
                                    DBApi.getBans().set(
                                            p.getUniqueId().toString(),
                                            Boolean.valueOf(false));
                                }
                            },
                            dbinstance.getConfig().getInt("banlength") * 20
                                    * 60 * 60 * 24 / level);
        } else {
            getBans().set(p.getUniqueId().toString(), Boolean.valueOf(true));
            dbinstance.getServer().getScheduler()
                    .scheduleSyncDelayedTask(dbinstance, new Runnable() {
                        public void run() {
                            DBApi.getBans().set(p.getUniqueId().toString(),
                                    Boolean.valueOf(false));
                        }
                    }, dbinstance.getConfig().getInt("banlength") * 20 / level);
        }
        saveCustomConfig();
        return isBanned(p);
    }

    public static boolean unBanPlayer(UUID uuid) {
        if (getBans().contains(uuid.toString())) {
            getBans().set(uuid.toString(), Boolean.valueOf(false));
            saveCustomConfig();
            return true;
        }
        return false;
    }

    public static Player getPlayer(String name) {
        for (Player p : dbinstance.getServer().getOnlinePlayers()) {
            if (p.getName().equalsIgnoreCase(name)) {
                return p;
            }
        }
        return null;
    }

    public static void changeLives(Player p, int amount, Method m) {
        if (m == Method.ADDITION) {
            getLives().set(p.getName(),
                    Integer.valueOf(getLives().getInt(p.getName()) + amount));
        } else {
            getLives().set(p.getName(),
                    Integer.valueOf(getLives().getInt(p.getName()) - amount));
        }
        saveCustomConfig();
    }

    public static int getLives(Player p) {
        return dbinstance.life.getInt(p.getName());
    }

    public static boolean isBanned(Player p) {
        return getBans().getBoolean(p.getUniqueId().toString());
    }

    public static void setLives(Player p, int lives) {
        dbinstance.getConfig().set(p.getUniqueId().toString(),
                Integer.valueOf(lives));
    }

    public static void resetDates() throws IOException {
        dbinstance.bantimes.delete();
        dbinstance.bantimes.createNewFile();
        saveCustomConfig();
    }

    public static void resetLives() throws IOException {
        dbinstance.lives.delete();
        dbinstance.lives.createNewFile();
        saveCustomConfig();
    }

    public static void resetDeathBans() throws IOException {
        dbinstance.deathbans.delete();
        dbinstance.deathbans.createNewFile();
        saveCustomConfig();
    }

    public static void disableDeathBans() {
        instance.getPluginLoader().disablePlugin(dbinstance);
    }

    public static void saveCustomConfig() {
        try {
            getBans().save(dbinstance.deathbans);
            getLives().save(dbinstance.lives);
            getDates().save(dbinstance.bantimes);
            dbinstance.ban.save(dbinstance.banned);
        } catch (IOException e) {
            e.printStackTrace();
            sendDBMessage(dbinstance.getServer().getConsoleSender(),
                    "&c[SEVERE] Could not save custom configuration files.");
        }
    }

    public static void updateConfig(FileConfiguration conf, String value,
                                    String set) {
        if (!conf.contains(value)) {
            conf.set(value, set);
        }
        dbinstance.saveConfig();
        saveCustomConfig();
    }

    public static void updateConfig(FileConfiguration conf, String value,
                                    int set) {
        if (!conf.contains(value)) {
            conf.set(value, set);
        }
        dbinstance.saveConfig();
        saveCustomConfig();
    }

    public static void updateConfig(FileConfiguration conf, String value,
                                    boolean set) {
        if (!conf.contains(value)) {
            conf.set(value, set);
        }
        dbinstance.saveConfig();
        saveCustomConfig();
    }

    public static void updateConfig(FileConfiguration conf, String value,
                                    List<String> set) {
        if (!conf.contains(value)) {
            conf.set(value, set);
        }
        dbinstance.saveConfig();
        saveCustomConfig();
    }
}
