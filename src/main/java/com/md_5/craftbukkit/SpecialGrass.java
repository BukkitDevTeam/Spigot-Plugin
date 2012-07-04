package com.md_5.craftbukkit;

import java.util.Random;
import net.minecraft.server.Block;
import net.minecraft.server.BlockGrass;
import net.minecraft.server.World;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockSpreadEvent;

public class SpecialGrass extends BlockGrass {

    public SpecialGrass(int i) {
        super(i);
    }

    @Override
    public void a(World world, int i, int j, int k, Random random) {
        if (!world.isStatic) {
            if (world.getLightLevel(i, j + 1, k) < 4 && Block.lightBlock[world.getTypeId(i, j + 1, k)] > 2) {
                // CraftBukkit start - reuse getLightLevel
                org.bukkit.World bworld = world.getWorld();
                org.bukkit.block.BlockState blockState = bworld.getBlockAt(i, j, k).getState();
                blockState.setTypeId(Block.DIRT.id);

                BlockFadeEvent event = new BlockFadeEvent(blockState.getBlock(), blockState);
                world.getServer().getPluginManager().callEvent(event);

                if (!event.isCancelled()) {
                    blockState.update(true);
                }
                // CraftBukkit end
            } else if (world.getLightLevel(i, j + 1, k) >= 9) {
                // CraftBukkit-- start
                int loops = Math.max(4, Math.max(20, (int) (4 * 100F / SpecialWorld.growthOdds)));
                for (int l = 0; l < loops; ++l) {
                    // CraftBukkit-- end
                    int i1 = i + random.nextInt(3) - 1;
                    int j1 = j + random.nextInt(5) - 3;
                    int k1 = k + random.nextInt(3) - 1;
                    int l1 = world.getTypeId(i1, j1 + 1, k1);

                    if (world.getTypeId(i1, j1, k1) == Block.DIRT.id && world.getLightLevel(i1, j1 + 1, k1) >= 4 && Block.lightBlock[l1] <= 2) {
                        // CraftBukkit start
                        org.bukkit.World bworld = world.getWorld();
                        org.bukkit.block.BlockState blockState = bworld.getBlockAt(i1, j1, k1).getState();
                        blockState.setTypeId(this.id);

                        BlockSpreadEvent event = new BlockSpreadEvent(blockState.getBlock(), bworld.getBlockAt(i, j, k), blockState);
                        world.getServer().getPluginManager().callEvent(event);

                        if (!event.isCancelled()) {
                            blockState.update(true);
                        }
                        // CraftBukkit end
                    }
                }
            }
        }
    }
}
