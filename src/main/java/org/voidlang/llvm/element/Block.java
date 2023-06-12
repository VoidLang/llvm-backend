package org.voidlang.llvm.element;

import org.bytedeco.llvm.LLVM.LLVMBasicBlockRef;

import static org.bytedeco.llvm.global.LLVM.LLVMAppendBasicBlockInContext;

public class Block {
    private final LLVMBasicBlockRef handle;
    private final Context context;
    private final Function function;
    private final String name;

    Block(LLVMBasicBlockRef handle, Context context, Function function, String name) {
        this.handle = handle;
        this.context = context;
        this.function = function;
        this.name = name;
    }

    public LLVMBasicBlockRef getHandle() {
        return handle;
    }

    public Context getContext() {
        return context;
    }

    public Function getFunction() {
        return function;
    }

    public String getName() {
        return name;
    }

    public static Block create(Context context, Function function, String name) {
        LLVMBasicBlockRef handle = LLVMAppendBasicBlockInContext(context.getHandle(), function.getHandle(), name);
        return new Block(handle, context, function, name);
    }
}
