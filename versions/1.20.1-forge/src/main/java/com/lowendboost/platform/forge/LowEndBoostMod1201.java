package com.lowendboost.platform.forge;

import com.lowendboost.LowEndBoost;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.logging.Logger;

/**
 * Entry point chính của mod LowEndBoost cho Minecraft 1.20.1 + Forge 47.x.
 * <p>
 * File này có annotation {@code @Mod("lowendboost")} — Forge sẽ tự load class
 * này khi khởi động game (vì tên class có chữ "Mod" và id trùng với mods.toml).
 * <p>
 * Trên client: đăng ký tick handler để gọi {@link LowEndBoost#onClientTick(int)}.
 * Trên server: chỉ log + đăng ký shutdown hook.
 */
@Mod("lowendboost")
public class LowEndBoostMod1201 {

    private static final Logger LOG = Logger.getLogger(LowEndBoostMod1201.class.getName());

    public LowEndBoostMod1201() {
        // Lấy mod event bus từ FML
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Tạo platform
        ForgePlatform1201 platform = new ForgePlatform1201();
        LOG.info("[LowEndBoost/1.20.1] Mod loaded. Minecraft " + platform.getMinecraftVersion()
                + " + " + platform.getLoaderName() + " " + platform.getLoaderVersion());

        // Khởi tạo orchestrator với platform
        LowEndBoost.get().initialize(platform);

        // Đăng ký tick handler
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            MinecraftForge.EVENT_BUS.register(new ClientTickHandler());
            LOG.info("[LowEndBoost/1.20.1] Registered client tick handler");
            return null;
        });

        DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> {
            MinecraftForge.EVENT_BUS.register(new ServerLifecycleHandler());
            LOG.info("[LowEndBoost/1.20.1] Registered server lifecycle handler");
            return null;
        });

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("[LowEndBoost/1.20.1] Shutting down...");
            LowEndBoost.get().shutdown();
        }, "LowEndBoost-Shutdown"));
    }

    /**
     * Client tick handler: gọi mỗi client tick (~20 lần/giây).
     * Đo delta time và pass cho orchestrator.
     */
    public static class ClientTickHandler {
        private long lastTickNanos = -1;

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            long now = System.nanoTime();
            int deltaMs;
            if (lastTickNanos < 0) {
                deltaMs = 50; // first tick assume 20 tps
            } else {
                deltaMs = (int) Math.min(1000, (now - lastTickNanos) / 1_000_000);
            }
            lastTickNanos = now;
            LowEndBoost.get().onClientTick(deltaMs);
        }
    }

    /** Server-side handler: chỉ cần thiết cho shutdown. */
    public static class ServerLifecycleHandler {
        @SubscribeEvent
        public void onServerStopping(net.minecraftforge.event.server.ServerStoppingEvent event) {
            LowEndBoost.get().shutdown();
        }
    }
}
