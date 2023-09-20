package org.voidlang.llvm.element;

import org.bytedeco.llvm.LLVM.LLVMValueRef;

import static org.bytedeco.llvm.global.LLVM.LLVMSetInitializer;

public class IRGlobal extends IRValue {
    public IRGlobal(LLVMValueRef handle) {
        super(handle);
    }

    public void setInitializer(IRValue value) {
        LLVMSetInitializer(getHandle(), value.getHandle());
    }
}
