package org.gephi.graphstore.benchmark.util;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.IterationParams;
import org.openjdk.jmh.profile.InternalProfiler;
import org.openjdk.jmh.results.AggregationPolicy;
import org.openjdk.jmh.results.IterationResult;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.ScalarResult;

public class MemoryProfiler implements InternalProfiler {

    private boolean shouldSkip(BenchmarkParams benchmarkParams) {
        return benchmarkParams.getParam("nodes") != null &&
            Integer.parseInt(benchmarkParams.getParam("nodes")) < 100000;
    }

    @Override
    public void beforeIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams) {
        if (shouldSkip(benchmarkParams)) {
            return;
        }
        restoreJvm();
    }

    @Override
    public Collection<? extends Result> afterIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams,
                                                       IterationResult result) {
        if (shouldSkip(benchmarkParams)) {
            return Collections.EMPTY_LIST;
        }
        restoreJvm();
        long memory = memoryUsed();

        Collection<ScalarResult> results = new ArrayList<>();
        results.add(new ScalarResult("Avg memory heap", memory / (1024.0 * 1024.0), "mb", AggregationPolicy.AVG));

        return results;
    }

    @Override
    public String getDescription() {
        return "Memory used";
    }

    /**
     * Call GC until no more memory can be freed
     */
    public static void restoreJvm() {
        int maxRestoreJvmLoops = 10;
        long memUsedPrev = memoryUsed();
        for (int i = 0; i < maxRestoreJvmLoops; i++) {
            System.runFinalization();
            System.gc();

            long memUsedNow = memoryUsed();
            // break early if have no more finalization and get constant mem used
            if ((ManagementFactory.getMemoryMXBean()
                .getObjectPendingFinalizationCount() == 0) && (memUsedNow >= memUsedPrev)) {
                break;
            } else {
                memUsedPrev = memUsedNow;
            }
        }
    }

    /**
     * Return the memory used in bytes
     *
     * @return heap memory used in bytes
     */
    public static long memoryUsed() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }
}
