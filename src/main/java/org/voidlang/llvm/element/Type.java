package org.voidlang.llvm.element;

import org.bytedeco.llvm.LLVM.LLVMTypeRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import static org.bytedeco.llvm.global.LLVM.*;

public class Type {
    private final LLVMTypeRef handle;

    private final Context context;

    Type(LLVMTypeRef handle, Context context) {
        this.handle = handle;
        this.context = context;
    }

    public LLVMTypeRef getHandle() {
        return handle;
    }

    public Context getContext() {
        return context;
    }

    public Value constInt(long value, boolean signExtend) {
        return new Value(LLVMConstInt(handle, value, signExtend ? 1 : 0));
    }

    public Value constInt(long value) {
        return constInt(value, false);
    }

    public static Value constInt(Type type, long value, boolean signExtend) {
        return type.constInt(value, signExtend);
    }

    public static Value constInt(Type type, long value) {
        return type.constInt(value, false);
    }

    public GenericValue genericInt(int value, boolean isSigned) {
        return new GenericValue(LLVMCreateGenericValueOfInt(handle, value, isSigned ? 1 : 0));
    }

    public GenericValue genericInt(int value) {
        return genericInt(value, false);
    }

    public static Type int1() {
        return new Type(LLVMInt1Type(), Context.global());
    }

    public static Type int1(Context context) {
        return new Type(LLVMInt1TypeInContext(context.getHandle()), context);
    }

    public static Type int8() {
        return new Type(LLVMInt8Type(), Context.global());
    }

    public static Type int8(Context context) {
        return new Type(LLVMInt8TypeInContext(context.getHandle()), Context.global());
    }

    public static Type int16() {
        return new Type(LLVMInt16Type(), Context.global());
    }

    public static Type int16(Context context) {
        return new Type(LLVMInt16TypeInContext(context.getHandle()), Context.global());
    }

    public static Type int32() {
        return new Type(LLVMInt32Type(), Context.global());
    }

    public static Type int32(Context context) {
        return new Type(LLVMInt32TypeInContext(context.getHandle()), Context.global());
    }

    public static Type int64() {
        return new Type(LLVMInt64Type(), Context.global());
    }

    public static Type int64(Context context) {
        return new Type(LLVMInt64TypeInContext(context.getHandle()), Context.global());
    }

    public static Type int128() {
        return new Type(LLVMInt128Type(), Context.global());
    }

    public static Type int128(Context context) {
        return new Type(LLVMInt128TypeInContext(context.getHandle()), Context.global());
    }
}
