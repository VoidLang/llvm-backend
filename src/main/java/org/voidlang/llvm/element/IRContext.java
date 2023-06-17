package org.voidlang.llvm.element;

import org.bytedeco.llvm.LLVM.LLVMContextRef;

import static org.bytedeco.llvm.global.LLVM.*;

public class IRContext implements Disposable {
    private final LLVMContextRef handle;

    IRContext(LLVMContextRef handle) {
        this.handle = handle;
    }

    public LLVMContextRef getHandle() {
        return handle;
    }

    public boolean isGlobal() {
        return handle == global().handle;
    }

    @Override
    public void dispose() {
        LLVMContextDispose(handle);
    }

    public static IRContext create() {
        return new IRContext(LLVMContextCreate());
    }

    public static IRContext global() {
        return new IRContext(LLVMGetGlobalContext());
    }
}
