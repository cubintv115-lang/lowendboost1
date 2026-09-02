package com.lowendboost;

import com.lowendboost.config.ConfigLoader;
import com.lowendboost.config.HardwareTier;
import com.lowendboost.config.LowEndBoostConfig;
import com.lowendboost.config.OptimizationMode;
import com.lowendboost.hardware.HardwareDetector;
import com.lowendboost.hardware.HardwareInfo;
import com.lowendboost.optimizer.AnimationOptimizer;
import com.lowendboost.optimizer.ChunkLoadingOptimizer;
import com.lowendboost.optimizer.IOptimizer;
import com.lowendboost.optimizer.MemoryOptimizer;
import com.lowendboost.optimizer.ParticleOptimizer;
import com.lowendboost.optimizer.RenderDistanceOptimizer;
import com.lowendboost.platform.Platform;
import com.lowendboost.util.FpsMonitor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Orchestrator chính cho mod LowEndBoost.
 * <p>
 * Class này KHÔNG dùng {@code @Mod} (annotation đó chỉ dành cho version
 * cụ thể trong sub-project). Mỗi entry point riêng cho từng MC version
 * sẽ tạo một instance của {@code LowEndBoost} và gọi các method tương ứng.
 * <p>
 * Luồng hoạt động:
 * <ol>
 *     <li>Khởi tạo: detect phần cứng, đọc config, chọn profile</li>
 *     <li>Apply config: gọi từng optimizer qua {@link Platform}</li>
 *     <li>Tick mỗi frame: memory optimizer, animation optimizer, FPS monitor</li>
 *     <li>Shutdown: dọn dẹp</li>
 * </ol>
 */
public class LowEndBoost {

    private static final Logger LOG = Logger.getLogger(LowEndBoost.class.getName());

    /** Singleton — chỉ 1 instance cho mỗi mod load. */
    private static LowEndBoost INSTANCE;

    public static LowEndBoost get() {
        if (INSTANCE == null) {
            INSTANCE = new LowEndBoost();
        }
        return INSTANCE;
    }

    // ============== State ==============

    private final List<IOptimizer> optimizers = new ArrayList<>();
    private final FpsMonitor fpsMonitor = new FpsMonitor();

    private Platform platform;
    private HardwareInfo hardware;
    private LowEndBoostConfig config;
    private Path configFile;
    private boolean initialized = false;

    public LowEndBoost() {
        // Tạo sẵn các optimizer theo thứ tự ưu tiên
        optimizers.add(new RenderDistanceOptimizer());
        optimizers.add(new ParticleOptimizer());
        optimizers.add(new ChunkLoadingOptimizer());
        optimizers.add(new AnimationOptimizer());
        optimizers.add(new MemoryOptimizer());
    }

    /** Khởi tạo mod với platform cụ thể (được gọi bởi version-specific entry point). */
    public void initialize(Platform platform) {
        if (initialized) {
            LOG.warning("LowEndBoost đã được khởi tạo trước đó, bỏ qua.");
            return;
        }
        this.platform = platform;
        this.hardware = HardwareDetector.detect();
        this.configFile = platform.getGameDirectory().resolve("config").resolve("lowendboost.properties");

        loadAndApplyConfig();
        initialized = true;
    }

    /** Đọc config từ file (hoặc dùng default) rồi áp dụng lên platform. */
    public void loadAndApplyConfig() {
        LowEndBoostConfig defaultCfg = chooseDefaultForTier(hardware.getTier());
        defaultCfg.adjustForActualMemory(hardware.getMaxHeapMB());
        defaultCfg.setTier(hardware.getTier());

        if (Files.exists(configFile)) {
            this.config = ConfigLoader.load(configFile, defaultCfg);
            // Mode AUTO: dùng default, mode CUSTOM: dùng từ file
            if (config.getMode() == OptimizationMode.AUTO) {
                this.config = defaultCfg;
            }
        } else {
            this.config = defaultCfg;
            // Lưu file mặc định để user tinh chỉnh
            try {
                ConfigLoader.save(configFile, defaultCfg);
                LOG.info("Tạo config mặc định tại: " + configFile);
            } catch (Exception e) {
                LOG.warning("Không tạo được config file: " + e.getMessage());
            }
        }

        applyAllOptimizers();
    }

    private LowEndBoostConfig chooseDefaultForTier(HardwareTier tier) {
        switch (tier) {
            case LOW: return LowEndBoostConfig.LOW_PROFILE;
            case MEDIUM: return LowEndBoostConfig.MEDIUM_PROFILE;
            case HIGH: return LowEndBoostConfig.HIGH_PROFILE;
            default: return LowEndBoostConfig.MEDIUM_PROFILE;
        }
    }

    /** Áp dụng tất cả optimizer lên platform. */
    public void applyAllOptimizers() {
        if (platform == null || config == null || hardware == null) return;

        if (config.getMode() == OptimizationMode.OFF) {
            LOG.info("LowEndBoost: chế độ OFF, không áp dụng tối ưu");
            return;
        }

        platform.applyConfig(config, hardware);

        for (IOptimizer opt : optimizers) {
            try {
                opt.apply(platform, config, hardware);
            } catch (Throwable t) {
                LOG.warning("Optimizer '" + opt.getName() + "' lỗi: " + t.getMessage());
            }
        }

        LOG.info("=" + "=".repeat(60));
        LOG.info("LowEndBoost enabled — " + hardware.describe());
        LOG.info("Config: " + config.describe());
        LOG.info("=" + "=".repeat(60));
    }

    /** Tick mỗi frame. Gọi bởi version-specific event handler. */
    public void onClientTick(int deltaMs) {
        if (!initialized || config == null) return;
        if (config.getMode() == OptimizationMode.OFF) return;

        fpsMonitor.recordFrame(deltaMs);

        for (IOptimizer opt : optimizers) {
            try {
                opt.tick(platform, config);
            } catch (Throwable t) {
                LOG.fine("Optimizer tick '" + opt.getName() + "' lỗi: " + t.getMessage());
            }
        }
    }

    /** Gọi khi mod unload (server shutdown, client quit). */
    public void shutdown() {
        if (!initialized) return;
        for (IOptimizer opt : optimizers) {
            try { opt.shutdown(platform); }
            catch (Throwable t) { LOG.fine("Shutdown '" + opt.getName() + "' lỗi: " + t.getMessage()); }
        }
        // Lưu config để lần sau dùng
        if (config != null && configFile != null) {
            try { ConfigLoader.save(configFile, config); } catch (Exception ignored) {}
        }
        initialized = false;
        LOG.info("LowEndBoost đã tắt.");
    }

    // ============== Getters cho entry point riêng ==============

    public Platform getPlatform() { return platform; }
    public HardwareInfo getHardware() { return hardware; }
    public LowEndBoostConfig getConfig() { return config; }
    public FpsMonitor getFpsMonitor() { return fpsMonitor; }
    public void setConfig(LowEndBoostConfig config) {
        this.config = config;
        applyAllOptimizers();
    }
}
