package org.voidlang.llvm.element;

import org.bytedeco.llvm.LLVM.LLVMBasicBlockRef;

import static org.bytedeco.llvm.global.LLVM.LLVMAppendBasicBlockInContext;

public class IRBlock {
    private final LLVMBasicBlockRef handle;
    private final IRContext context;
    private final IRFunction function;
    private final String name;

    IRBlock(LLVMBasicBlockRef handle, IRContext context, IRFunction function, String name) {
        this.handle = handle;
        this.context = context;
        this.function = function;
        this.name = name;
    }

    public LLVMBasicBlockRef getHandle() {
        return handle;
    }

    public IRContext getContext() {
        return context;
    }

    public IRFunction getFunction() {
        return function;
    }

    public String getName() {
        return name;
    }

    public static IRBlock create(IRContext context, IRFunction function, String name) {
        LLVMBasicBlockRef handle = LLVMAppendBasicBlockInContext(context.getHandle(), function.getHandle(), name);
        return new IRBlock(handle, context, function, name);
    }
}
