package com.enderdevelopment.plugin;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class Main extends JavaPlugin implements Listener, CommandExecutor {
    private static final int MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 100;
    private static final double HEALTH_PER_LEVEL = 10.0;
    private static final double DAMAGE_PER_LEVEL = 2.0;
    private static final double ARMOR_PER_LEVEL = 0.25;
    private static final double SPEED_PER_LEVEL = 0.01;
    private static final double KNOCKBACK_PER_LEVEL = 0.01;

    private NamespacedKey levelKey;
    private NamespacedKey scaledKey;
    private NamespacedKey healthBaseKey;
    private NamespacedKey damageBaseKey;
    private NamespacedKey armorBaseKey;
    private NamespacedKey speedBaseKey;
    private NamespacedKey knockbackBaseKey;

    @Override
    public void onEnable() {
        levelKey = new NamespacedKey(this, "mob_level");
        scaledKey = new NamespacedKey(this, "mob_level_stats_applied");
        healthBaseKey = new NamespacedKey(this, "base_health");
        damageBaseKey = new NamespacedKey(this, "base_damage");
        armorBaseKey = new NamespacedKey(this, "base_armor");
        speedBaseKey = new NamespacedKey(this, "base_speed");
        knockbackBaseKey = new NamespacedKey(this, "base_knockback");
        Bukkit.getPluginManager().registerEvents(this, this);
        if (getCommand("moblevel") != null) {
            getCommand("moblevel").setExecutor(this);
        }
        for (var world : Bukkit.getWorlds()) {
            for (var entity : world.getEntities()) {
                if (entity instanceof Mob mob) {
                    initializeMob(mob);
                }
            }
        }
    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        initializeMob(event.getEntity());
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity instanceof Mob mob) {
                initializeMob(mob);
            }
        }
    }

    private void initializeMob(Mob mob) {
        if (!mob.isValid() || mob.isDead()) {
            return;
        }
        PersistentDataContainer data = mob.getPersistentDataContainer();
        saveBaseValuesIfMissing(mob, data);
        Integer level = data.get(levelKey, PersistentDataType.INTEGER);
        if (level == null) {
            level = chooseLevel();
            data.set(levelKey, PersistentDataType.INTEGER, level);
        }
        if (!Boolean.TRUE.equals(data.get(scaledKey, PersistentDataType.BOOLEAN))) {
            applyStats(mob, data, level);
            data.set(scaledKey, PersistentDataType.BOOLEAN, true);
        }
        updateName(mob, level);
    }

    private void saveBaseValuesIfMissing(Mob mob, PersistentDataContainer data) {
        saveBaseValue(mob, data, Attribute.MAX_HEALTH, healthBaseKey);
        saveBaseValue(mob, data, Attribute.ATTACK_DAMAGE, damageBaseKey);
        saveBaseValue(mob, data, Attribute.ARMOR, armorBaseKey);
        saveBaseValue(mob, data, Attribute.MOVEMENT_SPEED, speedBaseKey);
        saveBaseValue(mob, data, Attribute.KNOCKBACK_RESISTANCE, knockbackBaseKey);
    }

    private void saveBaseValue(Mob mob, PersistentDataContainer data, Attribute attribute, NamespacedKey key) {
        if (data.has(key, PersistentDataType.DOUBLE)) {
            return;
        }
        AttributeInstance instance = mob.getAttribute(attribute);
        if (instance != null) {
            data.set(key, PersistentDataType.DOUBLE, instance.getBaseValue());
        }
    }

    private int chooseLevel() {
        int roll = ThreadLocalRandom.current().nextInt(1, 101);
        if (roll <= 5) {
            return ThreadLocalRandom.current().nextInt(51, 101);
        }
        if (roll <= 25) {
            return ThreadLocalRandom.current().nextInt(21, 51);
        }
        return ThreadLocalRandom.current().nextInt(1, 21);
    }

    private void applyStats(Mob mob, PersistentDataContainer data, int level) {
        setScaledAttribute(mob, data, Attribute.MAX_HEALTH, healthBaseKey, HEALTH_PER_LEVEL * level, 1.0, 2048.0);
        setScaledAttribute(mob, data, Attribute.ATTACK_DAMAGE, damageBaseKey, DAMAGE_PER_LEVEL * level, 0.0, 2048.0);
        setScaledAttribute(mob, data, Attribute.ARMOR, armorBaseKey, ARMOR_PER_LEVEL * level, 0.0, 100.0);
        setScaledAttribute(mob, data, Attribute.MOVEMENT_SPEED, speedBaseKey, SPEED_PER_LEVEL * level, 0.0, 1.0);
        setScaledAttribute(mob, data, Attribute.KNOCKBACK_RESISTANCE, knockbackBaseKey, KNOCKBACK_PER_LEVEL * level, 0.0, 1.0);
        AttributeInstance health = mob.getAttribute(Attribute.MAX_HEALTH);
        if (health != null) {
            mob.setHealth(health.getValue());
        }
    }

    private void setScaledAttribute(Mob mob, PersistentDataContainer data, Attribute attribute, NamespacedKey baseKey,
            double bonus, double minimum, double maximum) {
        AttributeInstance instance = mob.getAttribute(attribute);
        Double base = data.get(baseKey, PersistentDataType.DOUBLE);
        if (instance != null && base != null) {
            instance.setBaseValue(Math.min(maximum, Math.max(minimum, base + bonus)));
        }
    }

    private void updateName(Mob mob, int level) {
        String type = mob.getType().key().value().replace('_', ' ');
        String displayName = String.format(Locale.ROOT, "%s Lv.%d", titleCase(type), level);
        mob.customName(Component.text(displayName).color(level >= 51 ? NamedTextColor.LIGHT_PURPLE
                : level >= 21 ? NamedTextColor.GOLD : NamedTextColor.GREEN));
        mob.setCustomNameVisible(false);
    }

    private String titleCase(String text) {
        String[] words = text.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by a player.", NamedTextColor.RED));
            return true;
        }
        Mob mob = getTargetMob(player);
        if (mob == null) {
            player.sendMessage(Component.text("Look directly at a mob within 100 blocks.", NamedTextColor.RED));
            return true;
        }
        if (args.length == 0) {
            int level = getLevel(mob);
            player.sendMessage(Component.text(mob.getType().key().value() + " is level " + level + ".", NamedTextColor.YELLOW));
            return true;
        }
        if (!player.hasPermission("moblevels.admin")) {
            player.sendMessage(Component.text("You do not have permission to change mob levels.", NamedTextColor.RED));
            return true;
        }
        try {
            int level = Integer.parseInt(args[0]);
            if (level < MIN_LEVEL || level > MAX_LEVEL) {
                throw new NumberFormatException();
            }
            setLevel(mob, level);
            player.sendMessage(Component.text("Set the mob to level " + level + ".", NamedTextColor.GREEN));
        } catch (NumberFormatException exception) {
            player.sendMessage(Component.text("Level must be a whole number from 1 to 100.", NamedTextColor.RED));
        }
        return true;
    }

    private Mob getTargetMob(Player player) {
        RayTraceResult result = player.getWorld().rayTraceEntities(player.getEyeLocation(),
                player.getEyeLocation().getDirection(), 100.0, entity -> entity instanceof Mob);
        return result != null && result.getHitEntity() instanceof Mob mob ? mob : null;
    }

    private int getLevel(Mob mob) {
        Integer level = mob.getPersistentDataContainer().get(levelKey, PersistentDataType.INTEGER);
        if (level == null) {
            initializeMob(mob);
            level = mob.getPersistentDataContainer().get(levelKey, PersistentDataType.INTEGER);
        }
        return level == null ? MIN_LEVEL : level;
    }

    private void setLevel(Mob mob, int level) {
        mob.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, level);
        mob.getPersistentDataContainer().set(scaledKey, PersistentDataType.BOOLEAN, false);
        initializeMob(mob);
    }
}
