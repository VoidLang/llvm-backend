package org.voidlang.llvm.element;

import org.bytedeco.llvm.LLVM.LLVMContextRef;

import static org.bytedeco.llvm.global.LLVM.*;

public class Context implements Disposable {
    private final LLVMContextRef handle;

    Context(LLVMContextRef handle) {
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

    public static Context create() {
        return new Context(LLVMContextCreate());
    }

    public static Context global() {
        return new Context(LLVMGetGlobalContext());
    }
}
