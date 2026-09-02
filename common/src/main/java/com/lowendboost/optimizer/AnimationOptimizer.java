package com.lowendboost.optimizer;

import com.lowendboost.config.LowEndBoostConfig;
import com.lowendboost.config.OptimizationMode;
import com.lowendboost.hardware.HardwareInfo;
import com.lowendboost.platform.Platform;

import java.util.logging.Logger;

/**
 * Tối ưu entity animation.
 * <p>
 * Tắt animation (rotate limbs, swing arms,...) cho entity ở xa người chơi
 * vì chúng không ảnh hưởng trải nghiệm nhưng tốn CPU.
 */
public class AnimationOptimizer implements IOptimizer {

    private static final Logger LOG = Logger.getLogger(AnimationOptimizer.class.getName());

    /** Tick mỗi 20 tick (~1 giây) để tránh overhead. */
    private static final int TICK_INTERVAL = 20;

    private int tickCounter = 0;
    private int lastDisabledCount = 0;

    @Override
    public String getName() { return "Animation"; }

    @Override
    public void apply(Platform platform, LowEndBoostConfig config, HardwareInfo hardware) {
        if (config.getMode() == OptimizationMode.OFF) return;
        if (!config.isReduceEntityAnimations()) {
            LOG.info("Entity animation: not reducing");
            return;
        }
        LOG.info(String.format("Entity animation distance: %d blocks", config.getEntityAnimationDistance()));
    }

    @Override
    public boolean tick(Platform platform, LowEndBoostConfig config) {
        if (!config.isReduceEntityAnimations()) return false;

        tickCounter++;
        if (tickCounter < TICK_INTERVAL) return true;
        tickCounter = 0;

        lastDisabledCount = platform.disableDistantEntityAnimations(config.getEntityAnimationDistance());
        if (lastDisabledCount > 0) {
            LOG.fine("Disabled animations for " + lastDisabledCount + " distant entities");
        }
        return true;
    }
}
