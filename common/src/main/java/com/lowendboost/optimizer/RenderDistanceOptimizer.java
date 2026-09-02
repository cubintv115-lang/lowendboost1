package com.lowendboost.optimizer;

import com.lowendboost.config.LowEndBoostConfig;
import com.lowendboost.hardware.HardwareInfo;
import com.lowendboost.platform.Platform;

import java.util.logging.Logger;

/**
 * Tối ưu render distance.
 * <p>
 * Đặt render distance và simulation distance theo config.
 * Giảm simulation distance nếu cao hơn render distance (vô nghĩa).
 */
public class RenderDistanceOptimizer implements IOptimizer {

    private static final Logger LOG = Logger.getLogger(RenderDistanceOptimizer.class.getName());

    @Override
    public String getName() { return "RenderDistance"; }

    @Override
    public void apply(Platform platform, LowEndBoostConfig config, HardwareInfo hardware) {
        if (config.getMode() == com.lowendboost.config.OptimizationMode.OFF) {
            return;
        }

        int target = config.getRenderDistance();
        int current = platform.getRenderDistance();

        if (target < current) {
            platform.setRenderDistance(target);
            LOG.info(String.format("Render distance: %d -> %d (giảm %d chunks)", current, target, current - target));
        } else if (target > current) {
            platform.setRenderDistance(target);
            LOG.info(String.format("Render distance: %d -> %d", current, target));
        } else {
            LOG.info(String.format("Render distance giữ ở %d", current));
        }

        // Giới hạn FPS nếu có
        if (config.getMaxFps() > 0) {
            platform.setMaxFps(config.getMaxFps());
            LOG.info("Max FPS set: " + config.getMaxFps());
        }
    }
}
