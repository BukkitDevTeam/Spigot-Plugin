package com.md_5.craftbukkit;

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
        FileConfiguration configuration = Extras.instance.getConfig();
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

        Extras.instance.getLogger().info("-------------- [Extras] ----------------");
        Extras.instance.getLogger().info("-------- World Settings For [" + name + "] --------");
        Extras.instance.getLogger().info("Growth Per Chunk: " + growthPerTick);
        Extras.instance.getLogger().info("Item Merge Radius: " + itemMergeRadius);
        Extras.instance.getLogger().info("Random Lighting Updates: " + randomLightingUpdates);
        Extras.instance.getLogger().info("Mob Spawn Range: " + mobSpawnRange);
        Extras.instance.getLogger().info("Aggregate Ticks: " + aggregateTicks);
        Extras.instance.getLogger().info("-------------------------------------------------");
        // CraftBukkit-- end
    }
}
