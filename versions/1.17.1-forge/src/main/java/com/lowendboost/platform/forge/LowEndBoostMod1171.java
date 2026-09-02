package com.lowendboost.platform.forge;

import com.lowendboost.LowEndBoost;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import java.util.logging.Logger;

@Mod("lowendboost")
public class LowEndBoostMod1171 {

    private static final Logger LOG = Logger.getLogger(LowEndBoostMod1171.class.getName());

    public LowEndBoostMod1171() {
        ForgePlatform1171 platform = new ForgePlatform1171();
        LOG.info("[LowEndBoost/1.17.1] Mod loaded. Minecraft " + platform.getMinecraftVersion()
                + " + " + platform.getLoaderName() + " " + platform.getLoaderVersion());

        LowEndBoost.get().initialize(platform);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            MinecraftForge.EVENT_BUS.register(new ClientTickHandler());
            return null;
        });

        DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> {
            MinecraftForge.EVENT_BUS.register(new ServerLifecycleHandler());
            return null;
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LowEndBoost.get().shutdown();
        }, "LowEndBoost-Shutdown"));
    }

    public static class ClientTickHandler {
        private long lastTickNanos = -1;

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            long now = System.nanoTime();
            int deltaMs = (lastTickNanos < 0) ? 50 :
                    (int) Math.min(1000, (now - lastTickNanos) / 1_000_000);
            lastTickNanos = now;
            LowEndBoost.get().onClientTick(deltaMs);
        }
    }

    public static class ServerLifecycleHandler {
        @SubscribeEvent
        public void onServerStopping(net.minecraftforge.event.server.ServerStoppingEvent event) {
            LowEndBoost.get().shutdown();
        }
    }
}
