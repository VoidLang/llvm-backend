package org.voidlang.llvm.element;

import org.bytedeco.llvm.LLVM.LLVMGenericValueRef;

import static org.bytedeco.llvm.global.LLVM.*;

public class IRGenericValue {
    private final LLVMGenericValueRef handle;

    public IRGenericValue(LLVMGenericValueRef handle) {
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

    public static IRGenericValue genericInt(IRType type, int value, boolean isSigned) {
        return new IRGenericValue(LLVMCreateGenericValueOfInt(type.getHandle(), value, isSigned ? 1 : 0));
    }
}
