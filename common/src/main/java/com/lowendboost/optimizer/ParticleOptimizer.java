package com.lowendboost.optimizer;

import com.lowendboost.config.LowEndBoostConfig;
import com.lowendboost.config.OptimizationMode;
import com.lowendboost.hardware.HardwareInfo;
import com.lowendboost.platform.Platform;

import java.util.logging.Logger;

/**
 * Tối ưu particle.
 * <p>
 * Chuyển particle setting từ "All" (0) sang "Minimal" (2) khi cần.
 * Nếu multiplier = 0, hoàn toàn tắt particle xa.
 */
public class ParticleOptimizer implements IOptimizer {

    private static final Logger LOG = Logger.getLogger(ParticleOptimizer.class.getName());

    @Override
    public String getName() { return "Particle"; }

    @Override
    public void apply(Platform platform, LowEndBoostConfig config, HardwareInfo hardware) {
        if (config.getMode() == OptimizationMode.OFF) {
            return;
        }

        // Chọn particle level dựa trên multiplier
        int level;
        if (config.getParticleMultiplier() >= 0.8f) {
            level = 0; // All
        } else if (config.getParticleMultiplier() >= 0.4f) {
            level = 1; // Decreased
        } else {
            level = 2; // Minimal
        }
        platform.setParticleSetting(level);
        LOG.info(String.format("Particle setting: %d (multiplier=%.2f)", level, config.getParticleMultiplier()));
    }
}
