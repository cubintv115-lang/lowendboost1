package com.lowendboost.hardware;

import com.lowendboost.config.HardwareTier;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.logging.Logger;

/**
 * Phát hiện phần cứng của máy người chơi.
 * <p>
 * Phân loại tier bằng cách kết hợp nhiều yếu tố:
 * <ul>
 *     <li>Tổng RAM hệ thống (Mb)</li>
 *     <li>JVM heap tối đa được cấp</li>
 *     <li>Số CPU core</li>
 *     <li>Loại GPU (onboard / rời yếu / rời mạnh)</li>
 * </ul>
 * Phần phát hiện GPU dùng reflection để gọi LWJGL mà không cần compile-time
 * dependency, vì LWJGL có sẵn ở mọi version Minecraft.
 */
public final class HardwareDetector {

    private static final Logger LOG = Logger.getLogger(HardwareDetector.class.getName());

    private HardwareDetector() {}

    /** Phát hiện phần cứng hiện tại. */
    public static HardwareInfo detect() {
        Runtime runtime = Runtime.getRuntime();
        long maxHeapMB = runtime.maxMemory() / (1024L * 1024L);
        int cores = runtime.availableProcessors();

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        long totalMemoryMB = osBean.getTotalMemorySize() > 0
                ? osBean.getTotalMemorySize() / (1024L * 1024L)
                : estimateTotalMemoryFromHeap(maxHeapMB);

        String osName = System.getProperty("os.name", "Unknown");
        String osArch = System.getProperty("os.arch", "Unknown");
        String osVersion = System.getProperty("os.version", "Unknown");
        String javaVersion = System.getProperty("java.version", "Unknown");

        GpuInfo gpu = detectGpu();
        HardwareTier tier = classifyTier(totalMemoryMB, maxHeapMB, cores, gpu);

        HardwareInfo info = new HardwareInfo(
                totalMemoryMB, maxHeapMB, cores, osName, osArch, osVersion,
                javaVersion, gpu.renderer, gpu.vendor, gpu.version, tier);
        LOG.info("Detected: " + info.describe() + " -> Tier=" + tier);
        return info;
    }

    /**
     * Phân loại tier dựa trên thông số.
     * Quy tắc:
     * - LOW: heap ≤ 2GB hoặc RAM ≤ 4GB, hoặc GPU onboard với CPU 2 cores trở xuống.
     * - MEDIUM: 4GB < RAM < 8GB, hoặc GPU rời yếu, hoặc CPU 2-4 cores.
     * - HIGH: RAM ≥ 8GB, GPU rời mạnh, CPU ≥ 4 cores.
     */
    public static HardwareTier classifyTier(long totalMemoryMB, long maxHeapMB,
                                             int cores, GpuInfo gpu) {
        // 1. Dựa trên RAM
        HardwareTier ramTier;
        if (totalMemoryMB < 4096 || maxHeapMB < 1536) {
            ramTier = HardwareTier.LOW;
        } else if (totalMemoryMB < 8192 || maxHeapMB < 3072) {
            ramTier = HardwareTier.MEDIUM;
        } else {
            ramTier = HardwareTier.HIGH;
        }

        // 2. Dựa trên CPU
        HardwareTier cpuTier;
        if (cores <= 2) cpuTier = HardwareTier.LOW;
        else if (cores <= 4) cpuTier = HardwareTier.MEDIUM;
        else cpuTier = HardwareTier.HIGH;

        // 3. Dựa trên GPU
        HardwareTier gpuTier;
        if (gpu.isIntegrated) {
            gpuTier = HardwareTier.LOW;
        } else if (gpu.isWeak) {
            gpuTier = HardwareTier.MEDIUM;
        } else if (gpu.renderer != null && !gpu.renderer.isEmpty()) {
            gpuTier = HardwareTier.HIGH;
        } else {
            gpuTier = HardwareTier.UNKNOWN;
        }

        // Tier cuối cùng = tier thấp nhất trong 3 yếu tố (nếu 1 yếu tố yếu thì cả hệ thống yếu)
        HardwareTier finalTier = HardwareTier.min(ramTier, cpuTier);
        if (gpuTier != HardwareTier.UNKNOWN) {
            finalTier = HardwareTier.min(finalTier, gpuTier);
        }
        return finalTier;
    }

    /** Ước lượng tổng RAM từ heap khi không truy cập được OS MX bean (sandbox, etc.). */
    private static long estimateTotalMemoryFromHeap(long maxHeapMB) {
        // Thường Minecraft được cấp heap = 1/4 đến 1/2 RAM
        return maxHeapMB * 4;
    }

    // ============== GPU detection qua LWJGL reflection ==============

    /**
     * Thử lấy thông tin GPU qua LWJGL3 (Minecraft 1.17+ dùng LWJGL3).
     * Nếu không lấy được (LWJGL chưa init, sandbox, etc.) trả về rỗng.
     */
    public static GpuInfo detectGpu() {
        try {
            // LWJGL3: org.lwjgl.opengl.GL11C, org.lwjgl.opengl.GL30C
            Class<?> gl11Class = Class.forName("org.lwjgl.opengl.GL11C");
            // GL_RENDERER = 0x1F01
            Object rendererObj = callGlGetString(gl11Class, 0x1F01);
            // GL_VENDOR = 0x1F00
            Object vendorObj = callGlGetString(gl11Class, 0x1F00);
            // GL_VERSION = 0x1F02
            Object versionObj = callGlGetString(gl11Class, 0x1F02);

            String renderer = rendererObj != null ? rendererObj.toString() : "";
            String vendor = vendorObj != null ? vendorObj.toString() : "";
            String version = versionObj != null ? versionObj.toString() : "";

            boolean integrated = isIntegrated(vendor, renderer);
            boolean weak = isWeakGpu(renderer);

            return new GpuInfo(renderer, vendor, version, integrated, weak);
        } catch (Throwable t) {
            LOG.fine("Không detect được GPU qua LWJGL: " + t.getMessage());
            return GpuInfo.UNKNOWN;
        }
    }

    private static Object callGlGetString(Class<?> glClass, int pname) {
        try {
            // glGetString(int) returns String
            return glClass.getMethod("glGetString", int.class).invoke(null, pname);
        } catch (Throwable t) {
            return null;
        }
    }

    private static boolean isIntegrated(String vendor, String renderer) {
        if (vendor == null) return false;
        String v = vendor.toLowerCase();
        if (v.contains("intel") || v.contains("ati") || v.contains("amd")) {
            // Phân biệt với card rời: kiểm tra renderer có chứa "HD Graphics" (Intel onboard)
            // hoặc tên Radeon Vega / RX thường là rời
            String r = renderer == null ? "" : renderer.toLowerCase();
            if (r.contains("hd graphics") || r.contains("uhd graphics") || r.contains("iris")) {
                return true;
            }
            // AMD: Vega 8/10/11 thường là APU onboard
            if (r.contains("vega 8") || r.contains("vega 10") || r.contains("vega 11") ||
                r.contains("radeon(tm) vega")) {
                return true;
            }
            // Nếu chỉ là "Intel" mà không rõ thêm -> mặc định onboard
            if (v.contains("intel") && !r.contains("arc") && !r.contains("xe")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isWeakGpu(String renderer) {
        if (renderer == null) return false;
        String r = renderer.toLowerCase();
        // Mobile / laptop GPU dòng thấp
        return r.contains("gt 7") || r.contains("gt 8") || r.contains("gt 9") ||
                r.contains("gtx 1050") || r.contains("gtx 960") || r.contains("gtx 950") ||
                r.contains("mx 1") || r.contains("mx 2") || r.contains("mx 3") || r.contains("mx 4") ||
                r.contains("radeon rx 5") || r.contains("rx 550") || r.contains("rx 560");
    }

    /** Struct thông tin GPU. */
    public static class GpuInfo {
        public static final GpuInfo UNKNOWN = new GpuInfo("", "", "", false, false);

        public final String renderer;
        public final String vendor;
        public final String version;
        public final boolean isIntegrated;
        public final boolean isWeak;

        public GpuInfo(String renderer, String vendor, String version,
                       boolean isIntegrated, boolean isWeak) {
            this.renderer = renderer == null ? "" : renderer;
            this.vendor = vendor == null ? "" : vendor;
            this.version = version == null ? "" : version;
            this.isIntegrated = isIntegrated;
            this.isWeak = isWeak;
        }
    }
}
