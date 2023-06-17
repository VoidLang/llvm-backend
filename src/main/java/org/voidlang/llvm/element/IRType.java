package org.voidlang.llvm.element;

import org.bytedeco.llvm.LLVM.LLVMTypeRef;

import static org.bytedeco.llvm.global.LLVM.*;

public class IRType {
    private final LLVMTypeRef handle;

    private final IRContext context;

    IRType(LLVMTypeRef handle, IRContext context) {
        this.handle = handle;
        this.context = context;
    }

    public LLVMTypeRef getHandle() {
        return handle;
    }

    public IRContext getContext() {
        return context;
    }

    public IRValue constInt(long value, boolean signExtend) {
        return new IRValue(LLVMConstInt(handle, value, signExtend ? 1 : 0));
    }

    public IRValue constInt(long value) {
        return constInt(value, false);
    }

    public static IRValue constInt(IRType type, long value, boolean signExtend) {
        return type.constInt(value, signExtend);
    }

    public static IRValue constInt(IRType type, long value) {
        return type.constInt(value, false);
    }

    public IRGenericValue genericInt(int value, boolean isSigned) {
        return new IRGenericValue(LLVMCreateGenericValueOfInt(handle, value, isSigned ? 1 : 0));
    }

    public IRGenericValue genericInt(int value) {
        return genericInt(value, false);
    }

    public static IRType int1() {
        return new IRType(LLVMInt1Type(), IRContext.global());
    }

    public static IRType int1(IRContext context) {
        return new IRType(LLVMInt1TypeInContext(context.getHandle()), context);
    }

    public static IRType int8() {
        return new IRType(LLVMInt8Type(), IRContext.global());
    }

    public static IRType int8(IRContext context) {
        return new IRType(LLVMInt8TypeInContext(context.getHandle()), context);
    }

    public static IRType int16() {
        return new IRType(LLVMInt16Type(), IRContext.global());
    }

    public static IRType int16(IRContext context) {
        return new IRType(LLVMInt16TypeInContext(context.getHandle()), context);
    }

    public static IRType int32() {
        return new IRType(LLVMInt32Type(), IRContext.global());
    }

    public static IRType int32(IRContext context) {
        return new IRType(LLVMInt32TypeInContext(context.getHandle()), context);
    }

    public static IRType int64() {
        return new IRType(LLVMInt64Type(), IRContext.global());
    }

    public static IRType int64(IRContext context) {
        return new IRType(LLVMInt64TypeInContext(context.getHandle()), context);
    }

    public static IRType int128() {
        return new IRType(LLVMInt128Type(), IRContext.global());
    }

    public static IRType int128(IRContext context) {
        return new IRType(LLVMInt128TypeInContext(context.getHandle()), context);
    }

    public static IRType voidType() {
        return new IRType(LLVMVoidType(), IRContext.global());
    }

    public static IRType voidType(IRContext context) {
        return new IRType(LLVMVoidTypeInContext(context.getHandle()), context);
    }
}
