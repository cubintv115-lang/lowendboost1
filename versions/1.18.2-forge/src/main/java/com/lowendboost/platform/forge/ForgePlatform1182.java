package com.lowendboost.platform.forge;

import com.lowendboost.config.LowEndBoostConfig;
import com.lowendboost.hardware.HardwareInfo;
import com.lowendboost.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Platform implementation cho Minecraft 1.18.2 + Forge 40.x.
 * <p>
 * API tương tự 1.19.2: truy cập trực tiếp field {@code renderDistance}
 * (không qua {@code Option<T>}). Tên method nhỏ khác biệt ở một số chỗ
 * (vd: {@code distanceToSqr} vs {@code distanceTo}).
 */
public class ForgePlatform1182 implements Platform {

    private static final Logger LOG = Logger.getLogger(ForgePlatform1182.class.getName());
    private static final String MC_VERSION = "1.18.2";

    private final Minecraft mc;

    public ForgePlatform1182() {
        this.mc = Minecraft.getInstance();
    }

    @Override
    public String getMinecraftVersion() { return MC_VERSION; }

    @Override
    public String getLoaderName() { return "Forge"; }

    @Override
    public String getLoaderVersion() {
        return ModList.get().getModContainerById("forge")
                .map(ModContainer::getModInfo)
                .map(info -> info.getVersion().toString())
                .orElse("unknown");
    }

    @Override
    public Path getGameDirectory() {
        return FMLPaths.GAMEDIR.get();
    }

    @Override
    public boolean isDedicatedServer() {
        return FMLLoader.getDist() == Dist.DEDICATED_SERVER;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getRenderDistance() {
        if (mc.options == null) return 0;
        return mc.options.renderDistance;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void setRenderDistance(int distance) {
        if (mc.options == null) return;
        mc.options.renderDistance = clamp(distance, 2, 32);
        mc.options.save();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void setParticleSetting(int level) {
        if (mc.options == null) return;
        int clamped = clamp(level, 0, 2);
        mc.options.particles = clamped == 0 ? net.minecraft.client.ParticleStatus.ALL :
                clamped == 1 ? net.minecraft.client.ParticleStatus.DECREASED :
                                net.minecraft.client.ParticleStatus.MINIMAL;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void setMaxFps(int fps) {
        if (mc.options == null) return;
        mc.options.framerateLimit = clamp(fps, 0, 240);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int disableDistantEntityAnimations(int distanceBlocks) {
        if (mc.level == null || mc.player == null) return 0;
        if (distanceBlocks <= 0) return 0;

        Vec3 playerPos = mc.player.position();
        double distSq = (double) distanceBlocks * distanceBlocks;
        int count = 0;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (entity == mc.player) continue;
            if (living.deathTime > 0 || living.getHealth() <= 0) continue;
            if (entity.distanceToSqr(playerPos) > distSq) count++;
        }
        return count;
    }

    @Override
    public long forceGarbageCollection() {
        Runtime runtime = Runtime.getRuntime();
        long before = runtime.totalMemory() - runtime.freeMemory();
        System.gc();
        try { Thread.sleep(50); } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        System.runFinalization();
        long after = runtime.totalMemory() - runtime.freeMemory();
        return Math.max(0, before - after);
    }

    @Override
    public void registerShutdownHook(Runnable action) {
        Runtime.getRuntime().addShutdownHook(new Thread(action, "LowEndBoost-Shutdown"));
    }

    @Override
    public void sendInfoMessage(String message) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            if (mc.player != null) {
                mc.player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("[LowEndBoost] " + message),
                    true
                );
            } else {
                LOG.info(message);
            }
            return null;
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyConfig(LowEndBoostConfig config, HardwareInfo hardware) {
        if (mc.options == null) return;

        setParticleSetting(
            config.getParticleMultiplier() >= 0.8f ? 0 :
            config.getParticleMultiplier() >= 0.4f ? 1 : 2
        );
        setRenderDistance(config.getRenderDistance());
        if (config.getMaxFps() > 0) {
            setMaxFps(config.getMaxFps());
        }

        LOG.info(String.format("[LowEndBoost/1.18.2] Applied config: renderDist=%d, particles=%.2f, maxFps=%d",
                config.getRenderDistance(), config.getParticleMultiplier(), config.getMaxFps()));
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
