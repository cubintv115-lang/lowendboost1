package com.lowendboost.optimizer;

import com.lowendboost.config.LowEndBoostConfig;
import com.lowendboost.config.OptimizationMode;
import com.lowendboost.hardware.HardwareInfo;
import com.lowendboost.platform.Platform;

import java.util.logging.Logger;

/**
 * Tối ưu memory.
 * <p>
 * Có 2 cơ chế:
 * - Aggressive: tự động gọi {@code System.gc()} mỗi vài phút.
 * - Passive: điều chỉnh render/simulation distance để giảm chunk cache.
 */
public class MemoryOptimizer implements IOptimizer {

    private static final Logger LOG = Logger.getLogger(MemoryOptimizer.class.getName());

    /** Số tick (~20 ticks/giây) giữa 2 lần GC. 5 phút = 6000 ticks. */
    private static final int GC_INTERVAL_TICKS = 6000;

    private int tickCounter = 0;
    private long lastGcFreedBytes = 0;

    @Override
    public String getName() { return "Memory"; }

    @Override
    public void apply(Platform platform, LowEndBoostConfig config, HardwareInfo hardware) {
        if (config.getMode() == OptimizationMode.OFF) {
            return;
        }
        LOG.info(String.format(
                "Memory: %dMB total, %dMB heap, aggressive=%s",
                hardware.getTotalMemoryMB(), hardware.getMaxHeapMB(),
                config.isAggressiveMemoryManagement()));

        if (config.isAggressiveMemoryManagement()) {
            platform.registerShutdownHook(() -> {
                long freed = platform.forceGarbageCollection();
                LOG.info("Final GC on shutdown freed ~" + (freed / 1024 / 1024) + "MB");
            });
        }
    }

    @Override
    public boolean tick(Platform platform, LowEndBoostConfig config) {
        if (!config.isAggressiveMemoryManagement()) return false;

        tickCounter++;
        if (tickCounter >= GC_INTERVAL_TICKS) {
            tickCounter = 0;
            long before = usedMemory();
            long freed = platform.forceGarbageCollection();
            if (freed > 0) {
                lastGcFreedBytes = freed;
                LOG.fine(String.format("GC freed %dKB (used: %dMB -> %dMB)",
                        freed / 1024, before / 1024 / 1024, usedMemory() / 1024 / 1024));
            }
        }
        return true;
    }

    private long usedMemory() {
        Runtime r = Runtime.getRuntime();
        return r.totalMemory() - r.freeMemory();
    }

    public long getLastGcFreedBytes() { return lastGcFreedBytes; }
}
