package com.md_5.craftbukkit;

import java.util.Random;
import net.minecraft.server.Block;
import net.minecraft.server.BlockStem;
import net.minecraft.server.World;

public class SpecialStem extends BlockStem {

    private Block blockFruit;

    public SpecialStem(int i, Block block) {
        super(i, block);
        this.blockFruit = block;
    }

    @Override
    public void a(World world, int i, int j, int k, Random random) {
        if (world.getLightLevel(i, j + 1, k) >= 9) {
            float f = this.i(world, i, j, k);

            if (random.nextInt((int) ((25.0F / SpecialWorld.growthOdds) / f) + 1) == 0) { // CraftBukkit--
                int l = world.getData(i, j, k);

                if (l < 7) {
                    ++l;
                    org.bukkit.craftbukkit.event.CraftEventFactory.handleBlockGrowEvent(world, i, j, k, this.id, l); // CraftBukkit
                } else {
                    if (world.getTypeId(i - 1, j, k) == this.blockFruit.id) {
                        return;
                    }

                    if (world.getTypeId(i + 1, j, k) == this.blockFruit.id) {
                        return;
                    }

                    if (world.getTypeId(i, j, k - 1) == this.blockFruit.id) {
                        return;
                    }

                    if (world.getTypeId(i, j, k + 1) == this.blockFruit.id) {
                        return;
                    }

                    int i1 = random.nextInt(4);
                    int j1 = i;
                    int k1 = k;

                    if (i1 == 0) {
                        j1 = i - 1;
                    }

                    if (i1 == 1) {
                        ++j1;
                    }

                    if (i1 == 2) {
                        k1 = k - 1;
                    }

                    if (i1 == 3) {
                        ++k1;
                    }

                    int l1 = world.getTypeId(j1, j - 1, k1);

                    if (world.getTypeId(j1, j, k1) == 0 && (l1 == Block.SOIL.id || l1 == Block.DIRT.id || l1 == Block.GRASS.id)) {
                        org.bukkit.craftbukkit.event.CraftEventFactory.handleBlockGrowEvent(world, j1, j, k1, this.blockFruit.id, 0); // CraftBukkit
                    }
                }
            }
        }
    }

    private float i(World world, int i, int j, int k) {
        float f = 1.0F;
        int l = world.getTypeId(i, j, k - 1);
        int i1 = world.getTypeId(i, j, k + 1);
        int j1 = world.getTypeId(i - 1, j, k);
        int k1 = world.getTypeId(i + 1, j, k);
        int l1 = world.getTypeId(i - 1, j, k - 1);
        int i2 = world.getTypeId(i + 1, j, k - 1);
        int j2 = world.getTypeId(i + 1, j, k + 1);
        int k2 = world.getTypeId(i - 1, j, k + 1);
        boolean flag = j1 == this.id || k1 == this.id;
        boolean flag1 = l == this.id || i1 == this.id;
        boolean flag2 = l1 == this.id || i2 == this.id || j2 == this.id || k2 == this.id;

        for (int l2 = i - 1; l2 <= i + 1; ++l2) {
            for (int i3 = k - 1; i3 <= k + 1; ++i3) {
                int j3 = world.getTypeId(l2, j - 1, i3);
                float f1 = 0.0F;

                if (j3 == Block.SOIL.id) {
                    f1 = 1.0F;
                    if (world.getData(l2, j - 1, i3) > 0) {
                        f1 = 3.0F;
                    }
                }

                if (l2 != i || i3 != k) {
                    f1 /= 4.0F;
                }

                f += f1;
            }
        }

        if (flag2 || flag && flag1) {
            f /= 2.0F;
        }

        return f;
    }
}
