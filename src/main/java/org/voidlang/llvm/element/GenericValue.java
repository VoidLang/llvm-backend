package org.voidlang.llvm.element;

import org.bytedeco.llvm.LLVM.LLVMGenericValueRef;

import static org.bytedeco.llvm.global.LLVM.*;

public class GenericValue {
    private final LLVMGenericValueRef handle;

    public GenericValue(LLVMGenericValueRef handle) {
        this.handle = handle;
    }

    public long toInt(boolean isSigned) {
        return LLVMGenericValueToInt(handle, isSigned ? 1 : 0);
    }

    public long toInt() {
        return toInt(false);
    }

    public LLVMGenericValueRef getHandle() {
        return handle;
    }

    public static GenericValue genericInt(Type type, int value, boolean isSigned) {
        return new GenericValue(LLVMCreateGenericValueOfInt(type.getHandle(), value, isSigned ? 1 : 0));
    }
}
