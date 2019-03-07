package jdk.incubator.vector;

import jdk.internal.HotSpotIntrinsicCandidate;
import jdk.internal.misc.Unsafe;
import jdk.internal.vm.annotation.ForceInline;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.*;

import jdk.incubator.vector.Vector;

/*non-public*/ class VectorIntrinsics {
    static final Unsafe U = Unsafe.getUnsafe();

    static final long BUFFER_ADDRESS
            = U.objectFieldOffset(Buffer.class, "address");

    // Buffer.limit
    static final long BUFFER_LIMIT
            = U.objectFieldOffset(Buffer.class, "limit");

    // ByteBuffer.hb
    static final long BYTE_BUFFER_HB
            = U.objectFieldOffset(ByteBuffer.class, "hb");

    // ByteBuffer.isReadOnly
    static final long BYTE_BUFFER_IS_READ_ONLY
            = U.objectFieldOffset(ByteBuffer.class, "isReadOnly");

    // Unary
    static final int VECTOR_OP_ABS  = 0;
    static final int VECTOR_OP_NEG  = 1;
    static final int VECTOR_OP_SQRT = 2;
    static final int VECTOR_OP_NOT  = 3;

    // Binary
    static final int VECTOR_OP_ADD  = 4;
    static final int VECTOR_OP_SUB  = 5;
    static final int VECTOR_OP_MUL  = 6;
    static final int VECTOR_OP_DIV  = 7;
    static final int VECTOR_OP_MIN  = 8;
    static final int VECTOR_OP_MAX  = 9;

    static final int VECTOR_OP_AND  = 10;
    static final int VECTOR_OP_OR   = 11;
    static final int VECTOR_OP_XOR  = 12;

    // Ternary
    static final int VECTOR_OP_FMA  = 13;

    // Broadcast int
    static final int VECTOR_OP_LSHIFT  = 14;
    static final int VECTOR_OP_RSHIFT  = 15;
    static final int VECTOR_OP_URSHIFT = 16;

    // Math routines
    static final int VECTOR_OP_TAN = 101;
    static final int VECTOR_OP_TANH = 102;
    static final int VECTOR_OP_SIN = 103;
    static final int VECTOR_OP_SINH = 104;
    static final int VECTOR_OP_COS = 105;
    static final int VECTOR_OP_COSH = 106;
    static final int VECTOR_OP_ASIN = 107;
    static final int VECTOR_OP_ACOS = 108;
    static final int VECTOR_OP_ATAN = 109;
    static final int VECTOR_OP_ATAN2 = 110;
    static final int VECTOR_OP_CBRT = 111;
    static final int VECTOR_OP_LOG = 112;
    static final int VECTOR_OP_LOG10 = 113;
    static final int VECTOR_OP_LOG1P = 114;
    static final int VECTOR_OP_POW = 115;
    static final int VECTOR_OP_EXP = 116;
    static final int VECTOR_OP_EXPM1 = 117;
    static final int VECTOR_OP_HYPOT = 118;

    // enum BoolTest
    static final int BT_eq = 0;
    static final int BT_ne = 4;
    static final int BT_le = 5;
    static final int BT_ge = 7;
    static final int BT_lt = 3;
    static final int BT_gt = 1;
    static final int BT_overflow = 2;
    static final int BT_no_overflow = 6;

    /* ============================================================================ */
    interface BroadcastOperation<V, E> {
        V broadcast(long l, Vector.Species<E> s);
    }

    @HotSpotIntrinsicCandidate
    static
    <VM, E>
    VM broadcastCoerced(Class<VM> vmClass, Class<?> E, int length,
                                  long bits, Vector.Species<E> s,
                                  BroadcastOperation<VM, E> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.broadcast(bits, s);
    }

    /* ============================================================================ */

    @HotSpotIntrinsicCandidate
    static
    <V extends Vector<?>>
    long reductionCoerced(int oprId, Class<?> vectorClass, Class<?> elementType, int length,
                          V v,
                          Function<V,Long> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.apply(v);
    }

    /* ============================================================================ */

    interface VecExtractOp<V> {
        long apply(V v1, int idx);
    }

    @HotSpotIntrinsicCandidate
    static
    <V extends Vector<?>>
    long extract(Class<?> vectorClass, Class<?> elementType, int vlen,
                 V vec, int ix,
                 VecExtractOp<V> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.apply(vec, ix);
    }

    /* ============================================================================ */

    interface VecInsertOp<V> {
        V apply(V v1, int idx, long val);
    }

    @HotSpotIntrinsicCandidate
    static <V extends Vector<?>>
    V insert(Class<V> vectorClass, Class<?> elementType, int vlen,
                        V vec, int ix, long val,
                        VecInsertOp<V> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.apply(vec, ix, val);
    }

    /* ============================================================================ */

    @HotSpotIntrinsicCandidate
    static
    <VM>
    VM unaryOp(int oprId, Class<VM> vmClass, Class<?> elementType, int length,
               VM vm, /*Vector.Mask<E,S> m,*/
               Function<VM, VM> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.apply(vm);
    }

    /* ============================================================================ */

    @HotSpotIntrinsicCandidate
    static
    <VM>
    VM binaryOp(int oprId, Class<? extends VM> vmClass, Class<?> elementType, int length,
                VM vm1, VM vm2, /*Vector.Mask<E,S> m,*/
                BiFunction<VM, VM, VM> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.apply(vm1, vm2);
    }

    /* ============================================================================ */

    interface TernaryOperation<V> {
        V apply(V v1, V v2, V v3);
    }

    @SuppressWarnings("unchecked")
    @HotSpotIntrinsicCandidate
    static
    <VM>
    VM ternaryOp(int oprId, Class<VM> vmClass, Class<?> elementType, int length,
                 VM vm1, VM vm2, VM vm3, /*Vector.Mask<E,S> m,*/
                 TernaryOperation<VM> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.apply(vm1, vm2, vm3);
    }

    /* ============================================================================ */

    // Memory operations

    interface LoadOperation<C, V, E> {
        V load(C container, int index, Vector.Species<E> s);
    }

    @HotSpotIntrinsicCandidate
    static
    <C, VM, E>
    VM load(Class<VM> vmClass, Class<?> E, int length,
           Object base, long offset,    // Unsafe addressing
           // Vector.Mask<E,S> m,
           C container, int index, Vector.Species<E> s,     // Arguments for default implementation
           LoadOperation<C, VM, E> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.load(container, index, s);
    }

    /* ============================================================================ */

    interface LoadVectorOperationWithMap<C, V extends Vector<?>, E> {
        V loadWithMap(C container, int index, int[] indexMap, int indexM, Vector.Species<E> s);
    }

    @HotSpotIntrinsicCandidate
    static
    <C, V extends Vector<?>, W extends IntVector, E>
    V loadWithMap(Class<?> vectorClass, Class<?> E, int length, Class<?> vectorIndexClass,
                  Object base, long offset, // Unsafe addressing
                  W index_vector,
                  C container, int index, int[] indexMap, int indexM, Vector.Species<E> s, // Arguments for default implementation
                  LoadVectorOperationWithMap<C, V, E> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.loadWithMap(container, index, indexMap, indexM, s);
    }

    /* ============================================================================ */

    interface StoreVectorOperation<C, V extends Vector<?>> {
        void store(C container, int index, V v);
    }

    @HotSpotIntrinsicCandidate
    static
    <C, V extends Vector<?>>
    void store(Class<?> vectorClass, Class<?> elementType, int length,
               Object base, long offset,    // Unsafe addressing
               V v,
               // Vector.Mask<E,S> m,
               C container, int index,      // Arguments for default implementation
               StoreVectorOperation<C, V> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        defaultImpl.store(container, index, v);
    }

    /* ============================================================================ */

    interface StoreVectorOperationWithMap<C, V extends Vector<?>> {
        void storeWithMap(C container, int index, V v, int[] indexMap, int indexM);
    }

    @HotSpotIntrinsicCandidate
    static
    <C, V extends Vector<?>, W extends IntVector>
    void storeWithMap(Class<?> vectorClass, Class<?> elementType, int length, Class<?> vectorIndexClass,
                      Object base, long offset,    // Unsafe addressing
                      W index_vector, V v,
                      C container, int index, int[] indexMap, int indexM, // Arguments for default implementation
                      StoreVectorOperationWithMap<C, V> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        defaultImpl.storeWithMap(container, index, v, indexMap, indexM);
    }

    /* ============================================================================ */

    @HotSpotIntrinsicCandidate
    static
    <VM>
    boolean test(int cond, Class<?> vmClass, Class<?> elementType, int length,
                 VM vm1, VM vm2,
                 BiFunction<VM, VM, Boolean> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.apply(vm1, vm2);
    }

    /* ============================================================================ */

    interface VectorCompareOp<V,M> {
        M apply(V v1, V v2);
    }

    @HotSpotIntrinsicCandidate
    static <V extends Vector<E>,
            M extends Vector.Mask<E>,
            E>
    M compare(int cond, Class<V> vectorClass, Class<M> maskClass, Class<?> elementType, int length,
              V v1, V v2,
              VectorCompareOp<V,M> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.apply(v1, v2);
    }

    /* ============================================================================ */

    interface VectorRearrangeOp<V extends Vector<E>,
            Sh extends Vector.Shuffle<E>,
            E> {
        V apply(V v1, Sh shuffle);
    }

    @HotSpotIntrinsicCandidate
    static
    <V extends Vector<E>,
            Sh extends Vector.Shuffle<E>,
            E>
    V rearrangeOp(Class<V> vectorClass, Class<Sh> shuffleClass, Class<?> elementType, int vlen,
            V v1, Sh sh,
            VectorRearrangeOp<V,Sh, E> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.apply(v1, sh);
    }

    /* ============================================================================ */

    interface VectorBlendOp<V extends Vector<E>,
            M extends Vector.Mask<E>,
            E> {
        V apply(V v1, V v2, M mask);
    }

    @HotSpotIntrinsicCandidate
    static
    <V extends Vector<E>,
     M extends Vector.Mask<E>,
     E>
    V blend(Class<V> vectorClass, Class<M> maskClass, Class<?> elementType, int length,
            V v1, V v2, M m,
            VectorBlendOp<V,M, E> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.apply(v1, v2, m);
    }

    /* ============================================================================ */

    interface VectorBroadcastIntOp<V extends Vector<?>> {
        V apply(V v, int i);
    }

    @HotSpotIntrinsicCandidate
    static
    <V extends Vector<?>>
    V broadcastInt(int opr, Class<V> vectorClass, Class<?> elementType, int length,
                   V v, int i,
                   VectorBroadcastIntOp<V> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.apply(v, i);
    }

    /* ============================================================================ */

    interface VectorReinterpretOp<S, VIN, V> {
        V apply(S species, VIN v);
    }

    @HotSpotIntrinsicCandidate
    static
    <S, VIN, V>
    V reinterpret(Class<?> fromVectorClass,
                  Class<?> fromElementType, int fromVLen,
                  Class<?> toVectorClass,
                  Class<?> toElementType, int toVLen,
                  VIN v, S s,
                  VectorReinterpretOp<S, VIN, V> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.apply(s, v);
    }

    /* ============================================================================ */

    interface VectorCastOp<S, VIN, V> {
        V apply(S species, VIN v);
    }

    @HotSpotIntrinsicCandidate
    static
    <S, VIN, V>
    V cast(Class<?> fromVectorClass,
           Class<?> fromElementType, int fromVLen,
           Class<?> toVectorClass,
           Class<?> toElementType, int toVLen,
           VIN v, S s,
           VectorCastOp<S, VIN, V> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.apply(s, v);
    }

    /* ============================================================================ */

    @HotSpotIntrinsicCandidate
    static <V> V maybeRebox(V v) {
        // The fence is added here to avoid memory aliasing problems in C2 between scalar & vector accesses.
        // TODO: move the fence generation into C2. Generate only when reboxing is taking place.
        U.loadFence();
        return v;
    }

    /* ============================================================================ */

    static final int VECTOR_ACCESS_OOB_CHECK = Integer.getInteger("jdk.incubator.vector.VECTOR_ACCESS_OOB_CHECK", 2);

    @ForceInline
    static int checkIndex(int ix, int length, int vlen) {
        switch (VectorIntrinsics.VECTOR_ACCESS_OOB_CHECK) {
            case 0: return ix; // no range check
            case 1: return Objects.checkFromIndexSize(ix, vlen, length);
            case 2: return Objects.checkIndex(ix, length - (vlen - 1));
            default: throw new InternalError();
        }
    }

    @ForceInline
    static IntVector checkIndex(IntVector vix, int length) {
        switch (VectorIntrinsics.VECTOR_ACCESS_OOB_CHECK) {
            case 0: return vix; // no range check
            case 1: // fall-through
            case 2:
                if(vix.lessThan(0).anyTrue() || vix.greaterThanEq(length).anyTrue()) {
                    String msg = String.format("Range check failed: vector %s out of bounds for length %d", vix, length);
                    throw new ArrayIndexOutOfBoundsException(msg);
                }
                return vix;
            default: throw new InternalError();
        }
    }

    static boolean isNonCapturingLambda(Object o) {
        return o.getClass().getDeclaredFields().length == 0;
    }
}
