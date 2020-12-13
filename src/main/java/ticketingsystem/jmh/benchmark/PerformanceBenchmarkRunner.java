package ticketingsystem.jmh.benchmark;

import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import ticketingsystem.*;

public class PerformanceBenchmarkRunner {
        public static void main(String[] args) throws RunnerException {
                Options opt = new OptionsBuilder().include(PerformanceBenchmark.class.getSimpleName())
                                .result("result.json")
                                .resultFormat(ResultFormatType.JSON)
                                .build();
                new Runner(opt).run();
        }
}