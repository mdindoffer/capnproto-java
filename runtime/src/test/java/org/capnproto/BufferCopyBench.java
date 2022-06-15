package org.capnproto;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.CompilerControl;
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

import java.nio.ByteBuffer;
import java.util.Random;

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@Fork(value = 10, warmups = 2)
@Warmup(iterations = 3)
public class BufferCopyBench {

    private ByteBuffer srcBuffer;
    private ByteBuffer dstBuffer;
    private int length;
    private int srcOffset;
    private int dstOffset;

    @Setup(Level.Iteration)
    public void setup() {
        Random random = new Random();
        byte[] srcData = new byte[128];
        random.nextBytes(srcData);
        srcBuffer = ByteBuffer.wrap(srcData);

        byte[] dstData = new byte[128];
        random.nextBytes(dstData);
        dstBuffer = ByteBuffer.wrap(dstData);

        length = 64;

        srcOffset = 8;
        dstOffset = 16;
    }

//    @Benchmark
//    public void bmarkOldCopy(Blackhole blackhole) {
//        oldCopy(blackhole, srcBuffer, srcOffset, dstBuffer, dstOffset, length);
//    }

    @Benchmark
    public void bmarkMemcpy(Blackhole blackhole) {
        memcpy(blackhole, dstBuffer, dstOffset, srcBuffer, srcOffset, length);
    }

    @Benchmark
    public void bmarkCopyBufferRange(Blackhole blackhole) {
        copyBufferRange(blackhole, srcBuffer, srcOffset, dstBuffer, dstOffset, length);
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    private static void memcpy(Blackhole bh, ByteBuffer dstBuffer, int dstByteOffset, ByteBuffer srcBuffer, int srcByteOffset, int length) {
        ByteBuffer dstDup = dstBuffer.duplicate();
        dstDup.position(dstByteOffset);
        dstDup.limit(dstByteOffset + length);
        ByteBuffer srcDup = srcBuffer.duplicate();
        srcDup.position(srcByteOffset);
        srcDup.limit(srcByteOffset + length);
        dstDup.put(srcDup);

        bh.consume(srcBuffer);
        bh.consume(dstBuffer);
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    private static void oldCopy(Blackhole bh, ByteBuffer srcBuffer, int srcOffset, ByteBuffer dstBuffer, int dstOffset, int length) {
        for (int i = 0; i < length; ++i) {
            dstBuffer.put(dstOffset + i, srcBuffer.get(srcOffset + i));
        }

        bh.consume(srcBuffer);
        bh.consume(dstBuffer);
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    private static void copyBufferRange(Blackhole bh, ByteBuffer source, int srcOffset, ByteBuffer dest, int destOffset, int length) {
        //store original positions
        source.mark();
        dest.mark();
        //setup positions for copying
        source.position(srcOffset);
        dest.position(destOffset);
        //set limit for value to demarcate ROI
        int originalSrcLimit = source.limit();
        source.limit(srcOffset + length);
        //copy
        dest.put(source);//from both positions until value limit, positions then incremented
        //reset positions
        source.reset();
        dest.reset();
        //reset limit
        source.limit(originalSrcLimit);

        bh.consume(source);
        bh.consume(dest);
    }

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(BufferCopyBench.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
