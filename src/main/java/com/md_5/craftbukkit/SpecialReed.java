package com.md_5.craftbukkit;

import java.util.Random;
import net.minecraft.server.BlockReed;
import net.minecraft.server.World;

public class SpecialReed extends BlockReed {

    public SpecialReed(int i, int j) {
        super(i, j);
    }

    public void a(World world, int i, int j, int k, Random random) {
        if (world.isEmpty(i, j + 1, k)) {
            int l;

            for (l = 1; world.getTypeId(i, j - l, k) == this.id; ++l) {
                ;
            }

            if (l < 3) {
                int i1 = world.getData(i, j, k);

                // CraftBukkit-- start
                byte neededData = (byte) Math.max(3, (int) ((world.growthOdds * 15 / 100F) + 0.5F));
                if (i1 >= neededData) {
                    // CraftBukkit-- end
                    org.bukkit.craftbukkit.event.CraftEventFactory.handleBlockGrowEvent(world, i, j + 1, k, this.id, 0); // CraftBukkit
                    world.setData(i, j, k, 0);
                } else {
                    world.setData(i, j, k, i1 + 1);
                }
            }
        }
    }
}
