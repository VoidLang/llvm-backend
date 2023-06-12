package org.voidlang.llvm.element;

import org.bytedeco.llvm.LLVM.LLVMValueRef;

public class Value {
    private final LLVMValueRef handle;

    Value(LLVMValueRef handle) {
        this.handle = handle;
    }

    public LLVMValueRef getHandle() {
        return handle;
    }
}
