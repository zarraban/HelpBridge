package com.example.help_bridge.common.scheduling;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Component
class InstanceMetricsJob {

    private final MeterRegistry meterRegistry;
    private final String instanceId;

    InstanceMetricsJob(MeterRegistry meterRegistry,
                       @Value("${helpbridge.scheduling.instance-id:${HOSTNAME:localhost}:${server.port:8080}}")
                       String instanceId) {
        this.meterRegistry = meterRegistry;
        this.instanceId = instanceId;
    }

    @Scheduled(cron = "${helpbridge.scheduling.instance-metrics.cron:0 0 * * * *}", zone = "UTC")
    public void report() {
        MemoryUsage heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        long uptimeSeconds = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        log.info("job=instance-metrics instance={} processCpuPercent={} heapUsedBytes={} heapMaxBytes={} "
                        + "diskFreeBytes={} uptimeSeconds={}",
                instanceId, processCpuPercent(), heap.getUsed(), heap.getMax(), diskFreeBytes(), uptimeSeconds);
    }

    private String processCpuPercent() {
        return Optional.ofNullable(meterRegistry.find("process.cpu.usage").gauge())
                .map(Gauge::value)
                .filter(value -> Double.isFinite(value) && value >= 0)
                .map(value -> String.format(Locale.ROOT, "%.2f", Math.min(value, 1.0) * 100))
                .orElse("unavailable");
    }

    private String diskFreeBytes() {
        try {
            return Long.toString(Files.getFileStore(Path.of(".")).getUsableSpace());
        } catch (IOException | SecurityException exception) {
            return "unavailable";
        }
    }
}
