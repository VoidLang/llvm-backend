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

    public IRValue size() {
        return new IRValue(LLVMSizeOf(handle));
    }

    public IRValue constFloat(double value) {
        return new IRValue(LLVMConstReal(handle, value));
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

    public static IRType floatType() {
        return new IRType(LLVMFloatType(), IRContext.global());
    }

    public static IRType floatType(IRContext context) {
        return new IRType(LLVMFloatTypeInContext(context.getHandle()), context);
    }

    public static IRType doubleType() {
        return new IRType(LLVMDoubleType(), IRContext.global());
    }

    public static IRType doubleType(IRContext context) {
        return new IRType(LLVMDoubleTypeInContext(context.getHandle()), context);
    }

    public static IRType voidType() {
        return new IRType(LLVMVoidType(), IRContext.global());
    }

    public static IRType voidType(IRContext context) {
        return new IRType(LLVMVoidTypeInContext(context.getHandle()), context);
    }

    public static IRType pointerType(IRType type, int addressSpace) {
        return new IRType(LLVMPointerType(type.getHandle(), addressSpace), type.getContext());
    }

    public static IRType pointerType(IRType type) {
        return pointerType(type, 0);
    }

    public IRType toPointerType() {
        return pointerType(this);
    }

    public static IRType arrayType(IRType type, int size) {
        return new IRType(LLVMArrayType(type.getHandle(), size), type.getContext());
    }

    public IRType toArrayType(int size) {
        return arrayType(this, size);
    }

    public static IRType typeOf(IRValue value) {
        return new IRType(LLVMTypeOf(value.getHandle()), IRContext.global());
    }

    public IRValue constNull() {
        return new IRValue(LLVMConstNull(handle));
    }

    public static IRValue constNull(IRType type) {
        return new IRValue(LLVMConstNull(type.getHandle()));
    }

    public IRValue nullptr() {
        return new IRValue(LLVMConstPointerNull(handle));
    }

    public static IRValue nullptr(IRType type) {
        return new IRValue(LLVMConstPointerNull(type.getHandle()));
    }
}
