package com.md_5.spigot;

import net.minecraft.server.WorldServer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.generator.ChunkGenerator;

public class SpecialCraftWorld extends CraftWorld {

    public int growthPerTick = 650;
    public double itemMergeRadius = 3;
    public boolean randomLightingUpdates = false;
    public int mobSpawnRange = 4;
    public int aggregateTicks = 4;

    public SpecialCraftWorld(WorldServer world, ChunkGenerator gen, Environment env) {
        super(world, gen, env);
        // CraftBukkit-- Start
        FileConfiguration configuration = Spigot.instance.getConfig();
        String name;
        if (world.worldData == null || world.worldData.name == null) {
            name = "default";
        } else {
            name = world.worldData.name.replaceAll(" ", "_");
        }

        //load defaults first
        growthPerTick = configuration.getInt("world-settings.default.growth-chunks-per-tick", growthPerTick);
        itemMergeRadius = configuration.getDouble("world-settings.default.item-merge-radius", itemMergeRadius);
        randomLightingUpdates = configuration.getBoolean("world-settings.default.random-light-updates", randomLightingUpdates);
        mobSpawnRange = configuration.getInt("world-settings.default.mob-spawn-range", mobSpawnRange);
        aggregateTicks = Math.max(1, configuration.getInt("world-settings.default.aggregate-chunkticks", aggregateTicks));

        //override defaults with world specific, if they exist
        growthPerTick = configuration.getInt("world-settings." + name + ".growth-chunks-per-tick", growthPerTick);
        itemMergeRadius = configuration.getDouble("world-settings." + name + ".item-merge-radius", itemMergeRadius);
        randomLightingUpdates = configuration.getBoolean("world-settings." + name + ".random-light-updates", randomLightingUpdates);
        mobSpawnRange = configuration.getInt("world-settings." + name + ".mob-spawn-range", mobSpawnRange);
        aggregateTicks = Math.max(1, configuration.getInt("world-settings." + name + ".aggregate-chunkticks", aggregateTicks));

        Spigot.instance.getLogger().info("-------------- [Extras] ----------------");
        Spigot.instance.getLogger().info("-------- World Settings For [" + name + "] --------");
        Spigot.instance.getLogger().info("Growth Per Chunk: " + growthPerTick);
        Spigot.instance.getLogger().info("Item Merge Radius: " + itemMergeRadius);
        Spigot.instance.getLogger().info("Random Lighting Updates: " + randomLightingUpdates);
        Spigot.instance.getLogger().info("Mob Spawn Range: " + mobSpawnRange);
        Spigot.instance.getLogger().info("Aggregate Ticks: " + aggregateTicks);
        Spigot.instance.getLogger().info("-------------------------------------------------");
        // CraftBukkit-- end
    }
}
