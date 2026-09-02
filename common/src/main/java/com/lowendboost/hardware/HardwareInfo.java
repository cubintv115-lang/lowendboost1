package com.lowendboost.hardware;

import com.lowendboost.config.HardwareTier;

import java.util.Objects;

/**
 * Kết quả phát hiện phần cứng. Immutable.
 */
public class HardwareInfo {

    private final long totalMemoryMB;
    private final long maxHeapMB;
    private final int availableProcessors;
    private final String osName;
    private final String osArch;
    private final String osVersion;
    private final String javaVersion;
    private final String gpuRenderer;
    private final String gpuVendor;
    private final String gpuVersion;
    private final HardwareTier tier;

    public HardwareInfo(long totalMemoryMB, long maxHeapMB, int availableProcessors,
                        String osName, String osArch, String osVersion,
                        String javaVersion, String gpuRenderer, String gpuVendor,
                        String gpuVersion, HardwareTier tier) {
        this.totalMemoryMB = totalMemoryMB;
        this.maxHeapMB = maxHeapMB;
        this.availableProcessors = availableProcessors;
        this.osName = osName;
        this.osArch = osArch;
        this.osVersion = osVersion;
        this.javaVersion = javaVersion;
        this.gpuRenderer = gpuRenderer;
        this.gpuVendor = gpuVendor;
        this.gpuVersion = gpuVersion;
        this.tier = tier;
    }

    public long getTotalMemoryMB() { return totalMemoryMB; }
    public long getMaxHeapMB() { return maxHeapMB; }
    public int getAvailableProcessors() { return availableProcessors; }
    public String getOsName() { return osName; }
    public String getOsArch() { return osArch; }
    public String getOsVersion() { return osVersion; }
    public String getJavaVersion() { return javaVersion; }
    public String getGpuRenderer() { return gpuRenderer; }
    public String getGpuVendor() { return gpuVendor; }
    public String getGpuVersion() { return gpuVersion; }
    public HardwareTier getTier() { return tier; }

    /** True nếu GPU là Intel/AMD onboard (thường yếu). */
    public boolean isIntegratedGpu() {
        if (gpuVendor == null) return false;
        String v = gpuVendor.toLowerCase();
        return v.contains("intel") || v.contains("amd") || v.contains("ati ");
    }

    /** True nếu tên GPU chứa từ khóa chỉ card rời yếu (Mobile / laptop GPU dòng thấp). */
    public boolean isWeakDiscreteGpu() {
        if (gpuRenderer == null) return false;
        String r = gpuRenderer.toLowerCase();
        // Các GPU rời yếu hay gặp trên laptop
        return r.contains("gt 7") || r.contains("gt 8") || r.contains("gt 9") ||
               r.contains("gtx 1050") || r.contains("gtx 960") || r.contains("gtx 950") ||
               r.contains("mx") || r.contains("radeon rx 5") || r.contains("vega 8") ||
               r.contains("vega 10") || r.contains("vega 11");
    }

    public String describe() {
        return String.format(
                "CPU cores=%d, RAM=%dMB, HeapMax=%dMB, OS=%s %s, Java=%s, GPU=%s (%s)",
                availableProcessors, totalMemoryMB, maxHeapMB, osName, osArch,
                javaVersion, gpuRenderer, gpuVendor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HardwareInfo)) return false;
        HardwareInfo that = (HardwareInfo) o;
        return totalMemoryMB == that.totalMemoryMB && maxHeapMB == that.maxHeapMB &&
                availableProcessors == that.availableProcessors && tier == that.tier &&
                Objects.equals(gpuRenderer, that.gpuRenderer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalMemoryMB, maxHeapMB, availableProcessors, tier, gpuRenderer);
    }
}
