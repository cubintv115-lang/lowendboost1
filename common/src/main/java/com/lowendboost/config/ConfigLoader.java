package com.lowendboost.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Đọc/ghi config từ file properties đơn giản.
 * File được lưu ở {@code config/lowendboost.properties} trong thư mục game.
 * <p>
 * Format: key=value, một dòng một key. Comment bắt đầu bằng #.
 */
public final class ConfigLoader {

    private static final Logger LOG = Logger.getLogger(ConfigLoader.class.getName());

    private ConfigLoader() {}

    /**
     * Đọc config từ file. Nếu file không tồn tại, trả về {@code defaults}.
     * Nếu một key bị thiếu, dùng giá trị từ {@code defaults}.
     */
    public static LowEndBoostConfig load(Path configFile, LowEndBoostConfig defaults) {
        if (!Files.exists(configFile)) {
            return LowEndBoostConfig.copyOf(defaults);
        }

        Properties props = new Properties();
        try (var reader = Files.newBufferedReader(configFile)) {
            props.load(reader);
        } catch (IOException e) {
            LOG.warning("Không đọc được config file, dùng defaults: " + e.getMessage());
            return LowEndBoostConfig.copyOf(defaults);
        }

        LowEndBoostConfig cfg = LowEndBoostConfig.copyOf(defaults);

        // Mode
        String mode = props.getProperty("mode");
        if (mode != null) {
            try { cfg.setMode(OptimizationMode.valueOf(mode.toUpperCase())); }
            catch (IllegalArgumentException ignored) {}
        }

        // Số nguyên
        cfg.setMaxFps(parseInt(props.getProperty("maxFps"), cfg.getMaxFps()));
        cfg.setRenderDistance(parseInt(props.getProperty("renderDistance"), cfg.getRenderDistance()));
        cfg.setSimulationDistance(parseInt(props.getProperty("simulationDistance"), cfg.getSimulationDistance()));
        cfg.setEntityAnimationDistance(parseInt(props.getProperty("entityAnimationDistance"), cfg.getEntityAnimationDistance()));

        // Float
        try {
            float pm = Float.parseFloat(props.getProperty("particleMultiplier", String.valueOf(cfg.getParticleMultiplier())));
            cfg.setParticleMultiplier(pm);
        } catch (NumberFormatException ignored) {}

        // Booleans
        cfg.setEnableChunkLazyLoad(parseBool(props.getProperty("enableChunkLazyLoad"), cfg.isEnableChunkLazyLoad()));
        cfg.setAggressiveMemoryManagement(parseBool(props.getProperty("aggressiveMemoryManagement"), cfg.isAggressiveMemoryManagement()));
        cfg.setReduceEntityAnimations(parseBool(props.getProperty("reduceEntityAnimations"), cfg.isReduceEntityAnimations()));
        cfg.setEnableAutoAdjust(parseBool(props.getProperty("enableAutoAdjust"), cfg.isEnableAutoAdjust()));
        cfg.setDisableDistantParticles(parseBool(props.getProperty("disableDistantParticles"), cfg.isDisableDistantParticles()));

        return cfg;
    }

    /** Ghi config ra file. Tự tạo thư mục nếu cần. */
    public static void save(Path configFile, LowEndBoostConfig config) {
        try {
            Path parent = configFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Properties props = new Properties();
            props.setProperty("mode", config.getMode().name());
            props.setProperty("maxFps", String.valueOf(config.getMaxFps()));
            props.setProperty("renderDistance", String.valueOf(config.getRenderDistance()));
            props.setProperty("simulationDistance", String.valueOf(config.getSimulationDistance()));
            props.setProperty("particleMultiplier", String.valueOf(config.getParticleMultiplier()));
            props.setProperty("entityAnimationDistance", String.valueOf(config.getEntityAnimationDistance()));
            props.setProperty("enableChunkLazyLoad", String.valueOf(config.isEnableChunkLazyLoad()));
            props.setProperty("aggressiveMemoryManagement", String.valueOf(config.isAggressiveMemoryManagement()));
            props.setProperty("reduceEntityAnimations", String.valueOf(config.isReduceEntityAnimations()));
            props.setProperty("enableAutoAdjust", String.valueOf(config.isEnableAutoAdjust()));
            props.setProperty("disableDistantParticles", String.valueOf(config.isDisableDistantParticles()));

            try (var writer = Files.newBufferedWriter(configFile)) {
                writer.write("# LowEndBoost configuration\n");
                writer.write("# mode: AUTO | CUSTOM | OFF\n");
                writer.write("# maxFps: 0 = unlimited, otherwise 30-240\n");
                writer.write("#\n");
                props.store(writer, null);
            }
        } catch (IOException e) {
            LOG.warning("Không ghi được config file: " + e.getMessage());
        }
    }

    private static int parseInt(String s, int def) {
        if (s == null) return def;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return def; }
    }

    private static boolean parseBool(String s, boolean def) {
        if (s == null) return def;
        String t = s.trim().toLowerCase();
        if (t.equals("true") || t.equals("yes") || t.equals("1") || t.equals("on")) return true;
        if (t.equals("false") || t.equals("no") || t.equals("0") || t.equals("off")) return false;
        return def;
    }
}
