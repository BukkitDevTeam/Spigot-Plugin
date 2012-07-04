package com.md_5.craftbukkit;

import java.util.Random;
import net.minecraft.server.BlockSapling;
import net.minecraft.server.World;

public class SpecialSapling extends BlockSapling {

    public SpecialSapling(int i, int j) {
        super(i, j);
    }

    @Override
    public void a(World world, int i, int j, int k, Random random) {
        if (!world.isStatic) {
            super.a(world, i, j, k, random);
            // CraftBukkit-- start
            int rand = Math.max(2, (int) ((SpecialWorld.growthOdds * 7 / 100F) + 0.5F));
            if (world.getLightLevel(i, j + 1, k) >= 9 && (SpecialWorld.growthOdds < 14F || random.nextInt(rand) == 0)) {
                // CraftBukkit-- end
                int l = world.getData(i, j, k);

                if ((l & 8) == 0) {
                    world.setData(i, j, k, l | 8);
                } else {
                    this.grow(world, i, j, k, random, false, null, null); // CraftBukkit - added bonemeal, player and itemstack
                }
            }
        }
    }
}
