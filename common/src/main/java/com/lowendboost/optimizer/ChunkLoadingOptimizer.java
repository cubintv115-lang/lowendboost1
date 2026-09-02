package com.lowendboost.optimizer;

import com.lowendboost.config.LowEndBoostConfig;
import com.lowendboost.config.OptimizationMode;
import com.lowendboost.hardware.HardwareInfo;
import com.lowendboost.platform.Platform;

import java.util.logging.Logger;

/**
 * Tối ưu chunk loading.
 * <p>
 * Cách hoạt động: kết hợp với {@link RenderDistanceOptimizer} để giữ
 * render distance thấp, đồng thời yêu cầu platform lazy-load chunks xa.
 * <p>
 * Trên các version Forge, platform implementation sẽ tự xử lý việc
 * skip load chunk thông qua hook {@code ChunkMap} hoặc {@code ChunkWatchEvent}.
 */
public class ChunkLoadingOptimizer implements IOptimizer {

    private static final Logger LOG = Logger.getLogger(ChunkLoadingOptimizer.class.getName());

    @Override
    public String getName() { return "ChunkLoading"; }

    @Override
    public void apply(Platform platform, LowEndBoostConfig config, HardwareInfo hardware) {
        if (config.getMode() == OptimizationMode.OFF) {
            return;
        }
        if (!config.isEnableChunkLazyLoad()) {
            LOG.info("Chunk lazy load: disabled by config");
            return;
        }

        // Logic thực tế nằm trong Platform implementation (qua Forge event hook).
        // Ở đây chỉ log để người dùng biết.
        LOG.info(String.format(
                "Chunk lazy load: enabled, render distance=%d chunks, simulation=%d chunks",
                config.getRenderDistance(), config.getSimulationDistance()));
    }
}
