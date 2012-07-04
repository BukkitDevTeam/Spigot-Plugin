package com.md_5.spigot;

import gnu.trove.list.linked.TLongLinkedList;
import gnu.trove.map.hash.TLongObjectHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import net.minecraft.server.Block;
import net.minecraft.server.BlockTorch;
import net.minecraft.server.IBlockAccess;
import net.minecraft.server.World;
import org.bukkit.event.block.BlockRedstoneEvent;

public class SpecialRedstoneTorch extends BlockTorch {

    private boolean isOn = false;
    private static Map<World, TLongObjectHashMap<TLongLinkedList>> torchUpdates = new HashMap<World, TLongObjectHashMap<TLongLinkedList>>(); // CraftBukkit--

    @Override
    public int a(int i, int j) {
        return i == 1 ? Block.REDSTONE_WIRE.a(i, j) : super.a(i, j);
    }

    // CraftBukkit-- start
    protected static long key(int x, int y, int z) {
        return (((long) x) & 0x1FFFFF) << 42 | (((long) z) & 0x1FFFFF) << 21 | ((long) y) & 0x1FFFFF;
    }

    protected static TLongObjectHashMap<TLongLinkedList> getTorchUpdates(World world) {
        TLongObjectHashMap<TLongLinkedList> updates = torchUpdates.get(world);
        if (updates == null) {
            updates = new TLongObjectHashMap<TLongLinkedList>();
            torchUpdates.put(world, updates);
        }
        return updates;
    }

    private boolean a(World world, int x, int y, int z, boolean flag) {
        long key = key(x, y, z);
        TLongLinkedList currentTorchUpdates;
        TLongObjectHashMap<TLongLinkedList> worldTorchUpdates = getTorchUpdates(world);
        if (worldTorchUpdates.containsKey(key)) {
            currentTorchUpdates = worldTorchUpdates.get(key);
        } else {
            currentTorchUpdates = new TLongLinkedList();
            worldTorchUpdates.put(key, currentTorchUpdates);
        }

        if (flag) {
            currentTorchUpdates.add(world.getTime());
        }

        return currentTorchUpdates.size() > 8;
    }

    private void removeOutdatedUpdates(World world, int x, int y, int z) {
        long key = key(x, y, z);
        TLongObjectHashMap<TLongLinkedList> worldTorchUpdates = getTorchUpdates(world);
        if (!worldTorchUpdates.containsKey(key)) {
            return;
        }
        TLongLinkedList currentTorchUpdates = worldTorchUpdates.get(key);

        long minTime = world.getTime() - 100L;
        int currentTorchUpdatesLen = currentTorchUpdates.size();
        for (int i = 0; i < currentTorchUpdatesLen; ++i) {
            if (currentTorchUpdates.get(i) < minTime) {
                currentTorchUpdates.removeAt(i);
                --i;
                --currentTorchUpdatesLen;
            }
        }
    }
    // CraftBukkit-- end

    protected SpecialRedstoneTorch(int i, int j, boolean flag) {
        super(i, j);
        this.isOn = flag;
        this.a(true);
    }

    @Override
    public int d() {
        return 2;
    }

    @Override
    public void onPlace(World world, int i, int j, int k) {
        if (world.getData(i, j, k) == 0) {
            super.onPlace(world, i, j, k);
        }

        if (this.isOn) {
            world.applyPhysics(i, j - 1, k, this.id);
            world.applyPhysics(i, j + 1, k, this.id);
            world.applyPhysics(i - 1, j, k, this.id);
            world.applyPhysics(i + 1, j, k, this.id);
            world.applyPhysics(i, j, k - 1, this.id);
            world.applyPhysics(i, j, k + 1, this.id);
        }
    }

    @Override
    public void remove(World world, int i, int j, int k) {
        if (this.isOn) {
            world.applyPhysics(i, j - 1, k, this.id);
            world.applyPhysics(i, j + 1, k, this.id);
            world.applyPhysics(i - 1, j, k, this.id);
            world.applyPhysics(i + 1, j, k, this.id);
            world.applyPhysics(i, j, k - 1, this.id);
            world.applyPhysics(i, j, k + 1, this.id);
        }
        removeOutdatedUpdates(world, i, j, k); // CraftBukkit--
    }

    @Override
    public boolean a(IBlockAccess iblockaccess, int i, int j, int k, int l) {
        if (!this.isOn) {
            return false;
        } else {
            int i1 = iblockaccess.getData(i, j, k);

            return i1 == 5 && l == 1 ? false : (i1 == 3 && l == 3 ? false : (i1 == 4 && l == 2 ? false : (i1 == 1 && l == 5 ? false : i1 != 2 || l != 4)));
        }
    }

    private boolean g(World world, int i, int j, int k) {
        int l = world.getData(i, j, k);

        return l == 5 && world.isBlockFaceIndirectlyPowered(i, j - 1, k, 0) ? true : (l == 3 && world.isBlockFaceIndirectlyPowered(i, j, k - 1, 2) ? true : (l == 4 && world.isBlockFaceIndirectlyPowered(i, j, k + 1, 3) ? true : (l == 1 && world.isBlockFaceIndirectlyPowered(i - 1, j, k, 4) ? true : l == 2 && world.isBlockFaceIndirectlyPowered(i + 1, j, k, 5))));
    }

    @Override
    public void a(World world, int i, int j, int k, Random random) {
        boolean flag = this.g(world, i, j, k);

        removeOutdatedUpdates(world, i, j, k); // CraftBukkit--

        // CraftBukkit start
        org.bukkit.plugin.PluginManager manager = world.getServer().getPluginManager();
        org.bukkit.block.Block block = world.getWorld().getBlockAt(i, j, k);
        int oldCurrent = this.isOn ? 15 : 0;

        BlockRedstoneEvent event = new BlockRedstoneEvent(block, oldCurrent, oldCurrent);
        // CraftBukkit end

        if (this.isOn) {
            if (flag) {
                // CraftBukkit start
                if (oldCurrent != 0) {
                    event.setNewCurrent(0);
                    manager.callEvent(event);
                    if (event.getNewCurrent() != 0) {
                        return;
                    }
                }
                // CraftBukkit end

                world.setTypeIdAndData(i, j, k, Block.REDSTONE_TORCH_OFF.id, world.getData(i, j, k));
                if (this.a(world, i, j, k, true)) {
                    world.makeSound((double) ((float) i + 0.5F), (double) ((float) j + 0.5F), (double) ((float) k + 0.5F), "random.fizz", 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);

                    for (int l = 0; l < 5; ++l) {
                        double d0 = (double) i + random.nextDouble() * 0.6D + 0.2D;
                        double d1 = (double) j + random.nextDouble() * 0.6D + 0.2D;
                        double d2 = (double) k + random.nextDouble() * 0.6D + 0.2D;

                        world.a("smoke", d0, d1, d2, 0.0D, 0.0D, 0.0D);
                    }
                }
            }
        } else if (!flag && !this.a(world, i, j, k, false)) {
            // CraftBukkit start
            if (oldCurrent != 15) {
                event.setNewCurrent(15);
                manager.callEvent(event);
                if (event.getNewCurrent() != 15) {
                    return;
                }
            }
            // CraftBukkit end

            world.setTypeIdAndData(i, j, k, Block.REDSTONE_TORCH_ON.id, world.getData(i, j, k));
        }
    }

    @Override
    public void doPhysics(World world, int i, int j, int k, int l) {
        super.doPhysics(world, i, j, k, l);
        world.c(i, j, k, this.id, this.d());
    }

    @Override
    public boolean d(World world, int i, int j, int k, int l) {
        return l == 0 ? this.a(world, i, j, k, l) : false;
    }

    @Override
    public int getDropType(int i, Random random, int j) {
        return Block.REDSTONE_TORCH_ON.id;
    }

    @Override
    public boolean isPowerSource() {
        return true;
    }
}
