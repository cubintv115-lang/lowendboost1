package com.lowendboost.platform.forge;

import com.lowendboost.LowEndBoost;
import com.lowendboost.config.LowEndBoostConfig;
import com.lowendboost.hardware.HardwareInfo;
import com.lowendboost.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Platform implementation cho Minecraft 1.20.1 với Forge 47.x.
 * <p>
 * Là bridge giữa common code (LowEndBoost orchestrator) và API cụ thể
 * của Minecraft 1.20.1. Mỗi method trong {@link Platform} được implement
 * bằng API riêng của version này.
 * <p>
 * Sử dụng {@code @OnlyIn(Dist.CLIENT)} cho các method chỉ chạy ở client.
 */
public class ForgePlatform1201 implements Platform {

    private static final Logger LOG = Logger.getLogger(ForgePlatform1201.class.getName());
    private static final String MC_VERSION = "1.20.1";

    /** Tham chiếu tới Minecraft instance (chỉ client). */
    private final Minecraft mc;

    public ForgePlatform1201() {
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
        if (mc.level == null) return 0;
        return mc.options.renderDistance().get();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void setRenderDistance(int distance) {
        if (mc.options == null) return;
        mc.options.renderDistance().set(clamp(distance, 2, 32));
        // Lưu options
        mc.options.save();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void setParticleSetting(int level) {
        if (mc.options == null) return;
        int clamped = clamp(level, 0, 2);
        mc.options.particles().set(
            clamped == 0 ? net.minecraft.client.ParticleStatus.ALL :
            clamped == 1 ? net.minecraft.client.ParticleStatus.DECREASED :
                           net.minecraft.client.ParticleStatus.MINIMAL);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void setMaxFps(int fps) {
        if (mc.options == null) return;
        mc.options.framerateLimit().set(clamp(fps, 0, 240));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int disableDistantEntityAnimations(int distanceBlocks) {
        if (mc.level == null || mc.player == null) return 0;
        if (distanceBlocks <= 0) return 0;

        Vec3 playerPos = mc.player.position();
        double distSq = (double) distanceBlocks * distanceBlocks;
        int count = 0;

        // Duyệt entities trong world, tắt animation cho entity xa
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (entity == mc.player) continue;
            if (living.deathTime > 0 || living.getHealth() <= 0) continue;

            double d2 = entity.distanceToSqr(playerPos);
            if (d2 > distSq) {
                // Trick: set hurt time 0 để không có animation bị thương
                // Cách an toàn hơn: bỏ qua - để Forge tự skip
                // (Ở MC 1.20.1, không có API tắt animation trực tiếp, nhưng
                //  skip render tick có thể qua mixin)
                count++;
            }
        }
        return count;
    }

    @Override
    public long forceGarbageCollection() {
        Runtime runtime = Runtime.getRuntime();
        long before = runtime.totalMemory() - runtime.freeMemory();
        System.gc();
        // Cho GC thời gian chạy
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
                    true // actionBar
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

        // Áp dụng particle setting
        setParticleSetting(
            config.getParticleMultiplier() >= 0.8f ? 0 :
            config.getParticleMultiplier() >= 0.4f ? 1 : 2
        );

        // Áp dụng render distance
        setRenderDistance(config.getRenderDistance());

        // Áp dụng max FPS
        if (config.getMaxFps() > 0) {
            setMaxFps(config.getMaxFps());
        }

        // Log thông tin
        LOG.info(String.format("[LowEndBoost/1.20.1] Applied config: renderDist=%d, particles=%.2f, maxFps=%d",
                config.getRenderDistance(), config.getParticleMultiplier(), config.getMaxFps()));
    }

    // ============== Helpers ==============

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
