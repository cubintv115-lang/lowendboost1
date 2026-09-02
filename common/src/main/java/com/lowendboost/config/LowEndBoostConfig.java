package com.lowendboost.config;

import com.lowendboost.hardware.HardwareDetector;

import java.util.Objects;

/**
 * Cấu hình chính của mod LowEndBoost.
 * <p>
 * Chứa tất cả các giá trị tối ưu mà người dùng (hoặc auto-detect) có thể điều chỉnh:
 * - {@code mode}: AUTO / CUSTOM / OFF
 * - {@code maxFps}: giới hạn FPS (0 = không giới hạn, 30/60/144 = cap).
 * - {@code renderDistance}: số chunk được render (2-12).
 * - {@code simulationDistance}: khoảng cách simulation entities (4-12).
 * - {@code particleMultiplier}: hệ số giảm particle (0.0 = tắt, 1.0 = đầy đủ).
 * - {@code entityAnimationDistance}: khoảng cách (block) để tắt animation entities.
 * - {@code enableChunkLazyLoad}: lazy load chunks xa.
 * - {@code aggressiveMemoryManagement}: GC thường xuyên hơn, giảm cache.
 */
public class LowEndBoostConfig {

    // ============== Default profiles theo từng tier ==============

    /** Profile cho máy cực yếu: ưu tiên FPS, chấp nhận giảm chất lượng nhiều. */
    public static final LowEndBoostConfig LOW_PROFILE = new LowEndBoostConfig(
            OptimizationMode.AUTO, HardwareTier.LOW,
            60, 6, 4, 0.1f, 8, true, true, true, true, true);

    /** Profile cho máy tầm trung: cân bằng giữa hiệu năng và đồ họa. */
    public static final LowEndBoostConfig MEDIUM_PROFILE = new LowEndBoostConfig(
            OptimizationMode.AUTO, HardwareTier.MEDIUM,
            60, 8, 6, 0.4f, 16, true, true, true, true, false);

    /** Profile cho máy mạnh: chỉ áp dụng vài tối ưu nhẹ, giữ chất lượng cao. */
    public static final LowEndBoostConfig HIGH_PROFILE = new LowEndBoostConfig(
            OptimizationMode.AUTO, HardwareTier.HIGH,
            0, 12, 10, 1.0f, 32, false, false, false, true, false);

    // ============== Fields ==============

    private OptimizationMode mode;
    private HardwareTier tier;
    private int maxFps;
    private int renderDistance;
    private int simulationDistance;
    private float particleMultiplier;
    private int entityAnimationDistance;
    private boolean enableChunkLazyLoad;
    private boolean aggressiveMemoryManagement;
    private boolean reduceEntityAnimations;
    private boolean enableAutoAdjust;
    private boolean disableDistantParticles;

    public LowEndBoostConfig() {
        this.mode = OptimizationMode.AUTO;
        this.tier = HardwareTier.UNKNOWN;
        this.maxFps = 60;
        this.renderDistance = 8;
        this.simulationDistance = 6;
        this.particleMultiplier = 0.5f;
        this.entityAnimationDistance = 16;
        this.enableChunkLazyLoad = true;
        this.aggressiveMemoryManagement = true;
        this.reduceEntityAnimations = true;
        this.enableAutoAdjust = true;
        this.disableDistantParticles = true;
    }

    public LowEndBoostConfig(OptimizationMode mode, HardwareTier tier,
                              int maxFps, int renderDistance, int simulationDistance,
                              float particleMultiplier, int entityAnimationDistance,
                              boolean enableChunkLazyLoad, boolean aggressiveMemoryManagement,
                              boolean reduceEntityAnimations, boolean enableAutoAdjust,
                              boolean disableDistantParticles) {
        this.mode = mode;
        this.tier = tier;
        this.maxFps = maxFps;
        this.renderDistance = renderDistance;
        this.simulationDistance = simulationDistance;
        this.particleMultiplier = particleMultiplier;
        this.entityAnimationDistance = entityAnimationDistance;
        this.enableChunkLazyLoad = enableChunkLazyLoad;
        this.aggressiveMemoryManagement = aggressiveMemoryManagement;
        this.reduceEntityAnimations = reduceEntityAnimations;
        this.enableAutoAdjust = enableAutoAdjust;
        this.disableDistantParticles = disableDistantParticles;
    }

    /** Chọn profile mặc định dựa trên tier phát hiện được. */
    public static LowEndBoostConfig forTier(HardwareTier tier) {
        switch (tier) {
            case LOW: return copyOf(LOW_PROFILE);
            case MEDIUM: return copyOf(MEDIUM_PROFILE);
            case HIGH: return copyOf(HIGH_PROFILE);
            default: return copyOf(MEDIUM_PROFILE);
        }
    }

    public static LowEndBoostConfig copyOf(LowEndBoostConfig src) {
        return new LowEndBoostConfig(
                src.mode, src.tier, src.maxFps, src.renderDistance, src.simulationDistance,
                src.particleMultiplier, src.entityAnimationDistance, src.enableChunkLazyLoad,
                src.aggressiveMemoryManagement, src.reduceEntityAnimations, src.enableAutoAdjust,
                src.disableDistantParticles);
    }

    /**
     * Áp dụng tối ưu thêm nếu RAM thực tế ít hơn dự kiến (downgrade profile).
     * Giúp tránh OOM trên máy có nhiều app chạy nền.
     */
    public void adjustForActualMemory(long actualMaxMemoryMB) {
        if (actualMaxMemoryMB < 2048) {
            // Ít hơn 2GB heap - downgrade mạnh
            this.renderDistance = Math.min(this.renderDistance, 4);
            this.simulationDistance = Math.min(this.simulationDistance, 3);
            this.particleMultiplier = Math.min(this.particleMultiplier, 0.1f);
            this.entityAnimationDistance = Math.min(this.entityAnimationDistance, 6);
            this.aggressiveMemoryManagement = true;
        } else if (actualMaxMemoryMB < 3072) {
            // 2-3GB heap
            this.renderDistance = Math.min(this.renderDistance, 6);
            this.simulationDistance = Math.min(this.simulationDistance, 5);
        }
    }

    /** Tính render distance an toàn dựa trên RAM có sẵn (MB). */
    public static int safeRenderDistanceForMemory(long memoryMB) {
        if (memoryMB < 2048) return 4;
        if (memoryMB < 4096) return 6;
        if (memoryMB < 8192) return 8;
        return 12;
    }

    /** In ra log thông tin config hiện tại. */
    public String describe() {
        return String.format(
                "Mode=%s, Tier=%s, RenderDist=%d, SimDist=%d, Particles=%.2f, MaxFPS=%d, " +
                "ChunkLazy=%s, MemMgmt=%s, EntityAnim=%s, AutoAdjust=%s, DistParticles=%s",
                mode, tier, renderDistance, simulationDistance, particleMultiplier, maxFps,
                enableChunkLazyLoad, aggressiveMemoryManagement, reduceEntityAnimations,
                enableAutoAdjust, disableDistantParticles);
    }

    // ============== Getters/Setters ==============

    public OptimizationMode getMode() { return mode; }
    public void setMode(OptimizationMode mode) { this.mode = mode; }

    public HardwareTier getTier() { return tier; }
    public void setTier(HardwareTier tier) { this.tier = tier; }

    public int getMaxFps() { return maxFps; }
    public void setMaxFps(int maxFps) { this.maxFps = clamp(maxFps, 0, 240); }

    public int getRenderDistance() { return renderDistance; }
    public void setRenderDistance(int renderDistance) { this.renderDistance = clamp(renderDistance, 2, 32); }

    public int getSimulationDistance() { return simulationDistance; }
    public void setSimulationDistance(int simulationDistance) { this.simulationDistance = clamp(simulationDistance, 3, 32); }

    public float getParticleMultiplier() { return particleMultiplier; }
    public void setParticleMultiplier(float particleMultiplier) { this.particleMultiplier = clamp(particleMultiplier, 0.0f, 1.0f); }

    public int getEntityAnimationDistance() { return entityAnimationDistance; }
    public void setEntityAnimationDistance(int d) { this.entityAnimationDistance = clamp(d, 0, 128); }

    public boolean isEnableChunkLazyLoad() { return enableChunkLazyLoad; }
    public void setEnableChunkLazyLoad(boolean v) { this.enableChunkLazyLoad = v; }

    public boolean isAggressiveMemoryManagement() { return aggressiveMemoryManagement; }
    public void setAggressiveMemoryManagement(boolean v) { this.aggressiveMemoryManagement = v; }

    public boolean isReduceEntityAnimations() { return reduceEntityAnimations; }
    public void setReduceEntityAnimations(boolean v) { this.reduceEntityAnimations = v; }

    public boolean isEnableAutoAdjust() { return enableAutoAdjust; }
    public void setEnableAutoAdjust(boolean v) { this.enableAutoAdjust = v; }

    public boolean isDisableDistantParticles() { return disableDistantParticles; }
    public void setDisableDistantParticles(boolean v) { this.disableDistantParticles = v; }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LowEndBoostConfig)) return false;
        LowEndBoostConfig that = (LowEndBoostConfig) o;
        return maxFps == that.maxFps && renderDistance == that.renderDistance &&
                simulationDistance == that.simulationDistance &&
                Float.compare(particleMultiplier, that.particleMultiplier) == 0 &&
                entityAnimationDistance == that.entityAnimationDistance &&
                mode == that.mode && tier == that.tier &&
                enableChunkLazyLoad == that.enableChunkLazyLoad &&
                aggressiveMemoryManagement == that.aggressiveMemoryManagement &&
                reduceEntityAnimations == that.reduceEntityAnimations &&
                enableAutoAdjust == that.enableAutoAdjust &&
                disableDistantParticles == that.disableDistantParticles;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, tier, maxFps, renderDistance, simulationDistance,
                particleMultiplier, entityAnimationDistance, enableChunkLazyLoad,
                aggressiveMemoryManagement, reduceEntityAnimations, enableAutoAdjust,
                disableDistantParticles);
    }
}
