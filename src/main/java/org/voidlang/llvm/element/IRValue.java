package org.voidlang.llvm.element;

import org.bytedeco.llvm.LLVM.LLVMValueRef;

public class IRValue {
    private final LLVMValueRef handle;

    IRValue(LLVMValueRef handle) {
        this.handle = handle;
    }

    public LLVMValueRef getHandle() {
        return handle;
    }
}
