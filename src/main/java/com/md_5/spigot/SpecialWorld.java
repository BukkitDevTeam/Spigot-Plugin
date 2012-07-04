package com.md_5.spigot;

import gnu.trove.map.hash.TLongShortHashMap;
import java.util.List;
import net.minecraft.server.Block;
import net.minecraft.server.Chunk;
import net.minecraft.server.ChunkSection;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityAnimal;
import net.minecraft.server.EntityExperienceOrb;
import net.minecraft.server.EntityGhast;
import net.minecraft.server.EntityGolem;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityItem;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityMonster;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.EntitySlime;
import net.minecraft.server.EntityWaterAnimal;
import net.minecraft.server.EntityWeatherLighting;
import net.minecraft.server.IDataManager;
import net.minecraft.server.MathHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NPC;
import net.minecraft.server.WorldServer;
import net.minecraft.server.WorldSettings;
import org.bukkit.Bukkit;
import org.bukkit.World.Environment;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.util.LongHash;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.generator.ChunkGenerator;

public class SpecialWorld extends WorldServer {

    public static float growthOdds = 100F;
    private TLongShortHashMap chunkTickList;
    public int tickEntityExceptions = 0;
    private int tickChunkErrors;
    public int tickChunkExceptions;
    private int R = this.random.nextInt(12000);
    public int aggregateTicks = 1;
    private float modifiedOdds = 100F;

    public SpecialWorld(MinecraftServer minecraftserver, IDataManager idatamanager, String s, int i, WorldSettings worldsettings, Environment env, ChunkGenerator gen) {
        super(minecraftserver, idatamanager, s, i, worldsettings, env, gen);
        //
        this.chunkTickList = new TLongShortHashMap(getWorld().growthPerTick * 5);
        this.chunkTickList.setAutoCompactionFactor(0.0F);
    }

    @Override
    public boolean addEntity(Entity entity, SpawnReason spawnReason) {
        if (entity == null) {
            return false;
        }
        // CraftBukkit end

        int i = MathHelper.floor(entity.locX / 16.0D);
        int j = MathHelper.floor(entity.locZ / 16.0D);
        boolean flag = false;

        if (entity instanceof EntityHuman) {
            flag = true;
        }

        // CraftBukkit start
        org.bukkit.event.Cancellable event = null;
        if (entity instanceof EntityLiving && !(entity instanceof EntityPlayer)) {
            boolean isAnimal = entity instanceof EntityAnimal || entity instanceof EntityWaterAnimal || entity instanceof EntityGolem;
            boolean isMonster = entity instanceof EntityMonster || entity instanceof EntityGhast || entity instanceof EntitySlime;

            if (spawnReason != SpawnReason.CUSTOM) {
                if (isAnimal && !allowAnimals || isMonster && !allowMonsters) {
                    entity.dead = true;
                    return false;
                }
            }

            event = CraftEventFactory.callCreatureSpawnEvent((EntityLiving) entity, spawnReason);
        } else if (entity instanceof EntityItem) {
            event = CraftEventFactory.callItemSpawnEvent((EntityItem) entity);
            // CraftBukkit-- start
            EntityItem item = (EntityItem) entity;
            int maxSize = item.itemStack.getMaxStackSize();
            if (item.itemStack.count < maxSize) {
                double radius = this.getWorld().itemMergeRadius;
                if (radius > 0) {
                    List<Entity> entities = this.getEntities(entity, entity.boundingBox.grow(radius, radius, radius));
                    for (Entity e : entities) {
                        if (e instanceof EntityItem) {
                            EntityItem loopItem = (EntityItem) e;
                            if (!loopItem.dead && loopItem.itemStack.id == item.itemStack.id && loopItem.itemStack.getData() == item.itemStack.getData()) {
                                int toAdd = Math.min(loopItem.itemStack.count, maxSize - item.itemStack.count);
                                item.itemStack.count += toAdd;
                                loopItem.itemStack.count -= toAdd;
                                if (loopItem.itemStack.count <= 0) {
                                    loopItem.die();
                                }
                            }
                        }
                    }
                }
            }
        } else if (entity instanceof EntityExperienceOrb) {
            EntityExperienceOrb xp = (EntityExperienceOrb) entity;
            double radius = this.getWorld().itemMergeRadius;
            if (radius > 0) {
                List<Entity> entities = this.getEntities(entity, entity.boundingBox.grow(radius, radius, radius));
                for (Entity e : entities) {
                    if (e instanceof EntityExperienceOrb) {
                        EntityExperienceOrb loopItem = (EntityExperienceOrb) e;
                        if (!loopItem.dead) {
                            xp.value += loopItem.value;
                            loopItem.die();
                        }
                    }
                }
            }
            // CraftBukkit-- end
        } else if (entity.getBukkitEntity() instanceof org.bukkit.entity.Projectile) {
            // Not all projectiles extend EntityProjectile, so check for Bukkit interface instead
            event = CraftEventFactory.callProjectileLaunchEvent(entity);
        }

        if (event != null && (event.isCancelled() || entity.dead)) {
            entity.dead = true;
            return false;
        }
        // CraftBukkit end

        if (!flag && !this.isChunkLoaded(i, j)) {
            entity.dead = true; // CraftBukkit
            return false;
        } else {
            if (entity instanceof EntityHuman) {
                EntityHuman entityhuman = (EntityHuman) entity;

                this.players.add(entityhuman);
                this.everyoneSleeping();
            }

            this.getChunkAt(i, j).a(entity);
            this.entityList.add(entity);
            this.c(entity);
            return true;
        }
    }

    private boolean isChunkLoaded(int i, int j) {
        return this.chunkProvider.isChunkLoaded(i, j);
    }

    @Override
    public void entityJoinedWorld(Entity entity, boolean flag) {
        if (!this.server.spawnNPCs && entity instanceof NPC) {
            entity.die();
        }

        if (entity.passenger == null || !(entity.passenger instanceof EntityHuman)) {
            entityJoinedWorld0(entity, flag);
        }
    }

    public void entityJoinedWorld0(Entity entity, boolean flag) {
        if (entity == null) {
            return;
        }
        try {
            tickEntity(entity, flag);
        } catch (Exception e) {
            try {
                tickEntityExceptions++;
                Bukkit.getLogger().severe("CraftBukkit-- has detected an unexpected exception while handling");
                if (!(entity instanceof EntityPlayer)) {
                    Bukkit.getLogger().severe("entity " + entity.toString() + " (id: " + entity.id + ")");
                    Bukkit.getLogger().severe("CraftBukkit-- will kill the entity from the game instead of crashing your server.");
                    entity.die();
                } else {
                    Bukkit.getLogger().severe("player '" + ((EntityPlayer) entity).name + "'. They will be kicked instead of crashing your server.");
                    ((EntityPlayer) entity).getBukkitEntity().kickPlayer("The server experienced and error and was forced to kick you. Please re-login.");
                }
                Bukkit.getLogger().severe("CraftBukkit-- recommends you report this to http://spout.in/cbpp");
                Bukkit.getLogger().severe("");
                Bukkit.getLogger().severe("CraftBukkit-- version: " + Bukkit.getBukkitVersion());
                Bukkit.getLogger().severe("Exception Trace Begins:");
                StackTraceElement[] stack = e.getStackTrace();
                for (int i = 0; i < stack.length; i++) {
                    Bukkit.getLogger().severe("    " + stack[i].toString());
                }
                Bukkit.getLogger().severe("Exception Trace Ends.");
                Bukkit.getLogger().severe("");
            } catch (Throwable t) {
                Bukkit.getLogger().severe("CraftBukkit-- has detected an unexpected exception while attempting to handle an exception (yes you read that correctly).");
                Bukkit.getLogger().severe("CraftBukkit-- recommends you report this to http://spout.in/cbpp");
                Bukkit.getLogger().severe("To avoid loss of data, your server is shutting down");
                Bukkit.getLogger().severe("");
                Bukkit.getLogger().severe("CraftBukkit-- version: " + Bukkit.getBukkitVersion());
                Bukkit.getLogger().severe("Exception Trace Begins:");
                StackTraceElement[] stack = t.getStackTrace();
                for (int i = 0; i < stack.length; i++) {
                    Bukkit.getLogger().severe("    " + stack[i].toString());
                }
                Bukkit.getLogger().severe("Exception Trace Ends.");
                Bukkit.getLogger().severe("");
                Bukkit.shutdown();
            }
        }
    }

    public void tickEntity(Entity entity, boolean flag) {
        // CraftBukkit-- end
        int i = MathHelper.floor(entity.locX);
        int j = MathHelper.floor(entity.locZ);
        byte b0 = 32;

        if (!flag || this.a(i - b0, 0, j - b0, i + b0, 0, j + b0)) {
            entity.bL = entity.locX;
            entity.bM = entity.locY;
            entity.bN = entity.locZ;
            entity.lastYaw = entity.yaw;
            entity.lastPitch = entity.pitch;
            if (flag && entity.bZ) {
                if (entity.vehicle != null) {
                    entity.R();
                } else {
                    entity.F_();
                }
            }

            // MethodProfiler.a("chunkCheck"); // CraftBukkit - not in production code
            if (Double.isNaN(entity.locX) || Double.isInfinite(entity.locX)) {
                entity.locX = entity.bL;
            }

            if (Double.isNaN(entity.locY) || Double.isInfinite(entity.locY)) {
                entity.locY = entity.bM;
            }

            if (Double.isNaN(entity.locZ) || Double.isInfinite(entity.locZ)) {
                entity.locZ = entity.bN;
            }

            if (Double.isNaN((double) entity.pitch) || Double.isInfinite((double) entity.pitch)) {
                entity.pitch = entity.lastPitch;
            }

            if (Double.isNaN((double) entity.yaw) || Double.isInfinite((double) entity.yaw)) {
                entity.yaw = entity.lastYaw;
            }

            int k = MathHelper.floor(entity.locX / 16.0D);
            int l = MathHelper.floor(entity.locY / 16.0D);
            int i1 = MathHelper.floor(entity.locZ / 16.0D);

            if (!entity.bZ || entity.ca != k || entity.cb != l || entity.cc != i1) {
                if (entity.bZ && this.isChunkLoaded(entity.ca, entity.cc)) {
                    this.getChunkAt(entity.ca, entity.cc).a(entity, entity.cb);
                }

                if (this.isChunkLoaded(k, i1)) {
                    entity.bZ = true;
                    this.getChunkAt(k, i1).a(entity);
                } else {
                    entity.bZ = false;
                }
            }

            // MethodProfiler.a(); // CraftBukkit - not in production code
            if (flag && entity.bZ && entity.passenger != null) {
                if (!entity.passenger.dead && entity.passenger.vehicle == entity) {
                    this.playerJoinedWorld(entity.passenger);
                } else {
                    entity.passenger.vehicle = null;
                    entity.passenger = null;
                }
            }
        }
    }

    protected void k() {
        // MethodProfiler.a("buildList"); // CraftBukkit - not in production code
        this.chunkTickList.clear();

        int i;
        int j;
        int k;

        final int optimalChunks = this.getWorld().growthPerTick;

        if (optimalChunks <= 0) {
            return;
        }
        if (players.size() == 0) {
            return;
        }
        //Keep chunks with growth inside of the optimal chunk range
        int chunksPerPlayer = Math.min(200, Math.max(1, (int) (((optimalChunks - players.size()) / (double) players.size()) + 0.5)));
        int randRange = 3 + chunksPerPlayer / 30;

        //odds of growth happening vs growth happening in vanilla
        final float modifiedOdds = Math.max(35, Math.min(100, ((chunksPerPlayer + 1) * 100F) / 25F));
        this.modifiedOdds = modifiedOdds;
        this.growthOdds = modifiedOdds;

        for (i = 0; i < this.players.size(); ++i) {
            EntityHuman entityhuman = (EntityHuman) this.players.get(i);
            int chunkX = MathHelper.floor(entityhuman.locX / 16.0D);
            int chunkZ = MathHelper.floor(entityhuman.locZ / 16.0D);

            //Always update the chunk the player is on
            long key = LongHash.toLong(chunkX, chunkZ);
            int existingPlayers = Math.max(0, chunkTickList.get(key)); //filter out -1's
            chunkTickList.put(key, (short) (existingPlayers + 1));

            //Check and see if we update the chunks surrounding the player this tick
            for (int chunk = 0; chunk < chunksPerPlayer; chunk++) {
                int dx = (random.nextBoolean() ? 1 : -1) * random.nextInt(randRange);
                int dz = (random.nextBoolean() ? 1 : -1) * random.nextInt(randRange);
                long hash = LongHash.toLong(dx + chunkX, dz + chunkZ);
                if (!chunkTickList.contains(hash) && this.isChunkLoaded(dx + chunkX, dz + chunkZ)) {
                    chunkTickList.put(hash, (short) -1); //no players
                }
            }
        }
        EntityHuman entityhuman;
        // CraftBukkit-- End

        // MethodProfiler.a(); // CraftBukkit - not in production code
        if (this.R > 0) {
            --this.R;
        }

        // MethodProfiler.a("playerCheckLight"); // CraftBukkit - not in production code
        if (!this.players.isEmpty() && this.getWorld().randomLightingUpdates) { // CraftBukkit--
            i = this.random.nextInt(this.players.size());
            entityhuman = (EntityHuman) this.players.get(i);
            j = MathHelper.floor(entityhuman.locX) + this.random.nextInt(11) - 5;
            k = MathHelper.floor(entityhuman.locY) + this.random.nextInt(11) - 5;
            int j1 = MathHelper.floor(entityhuman.locZ) + this.random.nextInt(11) - 5;

            this.v(j, k, j1);
        }

        // MethodProfiler.a(); // CraftBukkit - not in production code
    }

    @Override
    protected void l() {
        try {
            tickChunks();
            tickChunkErrors = 0;
        } catch (Exception e) {
            tickChunkErrors++;
            tickChunkExceptions++;
            Bukkit.getLogger().severe("CraftBukkit-- has detected an unexpected exception while ticking chunks");
            if (tickChunkErrors < 5) {
                Bukkit.getLogger().severe("CraftBukkit-- will skip the remainder of the chunks.");
                Bukkit.getLogger().severe("You may notice some delay in growth and decay of wheat or leaves.");
                Bukkit.getLogger().severe("CraftBukkit-- recommends you report this to md_5");
            } else {
                Bukkit.getLogger().severe("This is the 5th consecutive error, your server will shutdown to prevent");
                Bukkit.getLogger().severe("Any data loss. It is recommended you report this and determine the cause immediately.");
            }
            Bukkit.getLogger().severe("");
            Bukkit.getLogger().severe("CraftBukkit-- version: " + Bukkit.getBukkitVersion());
            Bukkit.getLogger().severe("Exception Trace Begins:");
            StackTraceElement[] stack = e.getStackTrace();
            for (int i = 0; i < stack.length; i++) {
                Bukkit.getLogger().severe("    " + stack[i].toString());
            }
            Bukkit.getLogger().severe("Exception Trace Ends.");
            Bukkit.getLogger().severe("");
            if (tickChunkErrors > 0) {
                Bukkit.shutdown();
            }
        }
    }

    protected void tickChunks() {
        this.aggregateTicks--;
        if (this.aggregateTicks != 0) {
            return;
        }
        aggregateTicks = this.getWorld().aggregateTicks;
        // CraftBukkit-- end
        this.k();
        int i = 0;
        int j = 0;
        // Iterator iterator = this.chunkTickList.iterator(); // CraftBukkit

        // CraftBukkit start
        // CraftBukkit-- start
        for (long chunkCoord : chunkTickList._set) {
            int players = chunkTickList.get(chunkCoord);
            int chunkX = LongHash.msw(chunkCoord);
            int chunkZ = LongHash.lsw(chunkCoord);
            // CraftBukkit-- end
            // ChunkCoordIntPair chunkcoordintpair = (ChunkCoordIntPair) iterator.next();
            int k = chunkX * 16;
            int l = chunkZ * 16;

            // MethodProfiler.a("getChunk"); // CraftBukkit - not in production code
            Chunk chunk = this.getChunkAt(chunkX, chunkZ);
            // CraftBukkit end

            // CraftBukkit-- start
            final int totalTicks = this.getWorld().aggregateTicks;
            for (int ticks = totalTicks; ticks > 0; ticks--) {
                // CraftBukkit-- end

                this.a(k, l, chunk);
                // MethodProfiler.b("thunder"); // CraftBukkit - not in production code
                int i1;
                int j1;
                int k1;
                int l1;

                if (this.random.nextInt(100000) == 0 && this.x() && this.w()) {
                    this.g = this.g * 3 + 1013904223;
                    i1 = this.g >> 2;
                    j1 = k + (i1 & 15);
                    k1 = l + (i1 >> 8 & 15);
                    l1 = this.f(j1, k1);
                    if (this.y(j1, l1, k1)) {
                        this.strikeLightning(new EntityWeatherLighting(this, (double) j1, (double) l1, (double) k1));
                        this.m = 2;
                    }
                }

                // MethodProfiler.b("iceandsnow"); // CraftBukkit - not in production code
                if (this.random.nextInt(16) == 0) {
                    this.g = this.g * 3 + 1013904223;
                    i1 = this.g >> 2;
                    j1 = i1 & 15;
                    k1 = i1 >> 8 & 15;
                    l1 = this.f(j1 + k, k1 + l);
                    if (this.t(j1 + k, l1 - 1, k1 + l)) {
                        // CraftBukkit start
                        BlockState blockState = this.getWorld().getBlockAt(j1 + k, l1 - 1, k1 + l).getState();
                        blockState.setTypeId(Block.ICE.id);

                        BlockFormEvent iceBlockForm = new BlockFormEvent(blockState.getBlock(), blockState);
                        this.getServer().getPluginManager().callEvent(iceBlockForm);
                        if (!iceBlockForm.isCancelled()) {
                            blockState.update(true);
                        }
                        // CraftBukkit end
                    }

                    if (this.x() && this.u(j1 + k, l1, k1 + l)) {
                        // CraftBukkit start
                        BlockState blockState = this.getWorld().getBlockAt(j1 + k, l1, k1 + l).getState();
                        blockState.setTypeId(Block.SNOW.id);

                        BlockFormEvent snow = new BlockFormEvent(blockState.getBlock(), blockState);
                        this.getServer().getPluginManager().callEvent(snow);
                        if (!snow.isCancelled()) {
                            blockState.update(true);
                        }
                        // CraftBukkit end
                    }
                }

                // MethodProfiler.b("tickTiles"); // CraftBukkit - not in production code
                ChunkSection[] achunksection = chunk.h();

                j1 = achunksection.length;

                for (k1 = 0; k1 < j1; ++k1) {
                    ChunkSection chunksection = achunksection[k1];

                    if (chunksection != null && chunksection.b()) {
                        for (int i2 = 0; i2 < 3; ++i2) {
                            this.g = this.g * 3 + 1013904223;
                            int j2 = this.g >> 2;
                            int k2 = j2 & 15;
                            int l2 = j2 >> 8 & 15;
                            int i3 = j2 >> 16 & 15;
                            int j3 = chunksection.a(k2, i3, l2);

                            ++j;
                            Block block = Block.byId[j3];

                            if (block != null && block.n()) {
                                ++i;
                                // CraftBukkit-- start
                                if (players < 1) {
                                    // grow fast if no players are in this chunk
                                    this.growthOdds = modifiedOdds;
                                } else {
                                    this.growthOdds = 100;
                                }
                                // CraftBukkit-- end
                                block.a(this, k2 + k, i3 + chunksection.c(), l2 + l, this.random);
                            }
                        }
                    }
                }

            } // CraftBukkit--

            // MethodProfiler.a(); // CraftBukkit - not in production code
        }
    }
}
