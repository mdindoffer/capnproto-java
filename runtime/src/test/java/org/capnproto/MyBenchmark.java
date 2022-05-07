package org.capnproto;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

@State(Scope.Thread)
public class MyBenchmark {

    private ByteBuffer inputByteBuffer;

    @Setup(Level.Iteration)
    public void setup() {
        byte[] bytes = {
                0, 0, 0, 0, 7, 0, 0, 0,
                0, 0, 0, 0, 5, 0, 1, 0,
                -80, 88, 70, -78, 95, -22, -98, -83,
                47, 24, 76, -119, 113, 53, 68, -125,
                48, 68, 76, -73, 76, 22, 85, 106,
                -89, -29, -85, 67, -86, 36, 15, -85,
                1, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0};
        inputByteBuffer = ByteBuffer.wrap(bytes);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Fork(value = 10, warmups = 2)
    @Warmup(iterations = 3)
    public void benchmarkSerializeRead(Blackhole blackhole) throws IOException {
        ReadableByteChannel input = new ArrayInputStream(inputByteBuffer);
        MessageReader message = org.capnproto.Serialize.read(input);
        blackhole.consume(message);
    }

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(MyBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
