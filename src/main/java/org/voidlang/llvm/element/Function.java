package org.voidlang.llvm.element;

import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.List;

import static org.bytedeco.llvm.global.LLVM.*;

public class Function extends Value {
    private final Module module;
    private final String name;
    private final FunctionType type;

    Function(LLVMValueRef handle, Module module, String name, FunctionType type) {
        super(handle);
        this.module = module;
        this.name = name;
        this.type = type;
    }

    public Value getParameter(int index) {
        return new Value(LLVMGetParam(getHandle(), index));
    }

    public Module getModule() {
        return module;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public static Function create(Module module, String name, FunctionType type) {
        return new Function(LLVMAddFunction(module.getHandle(), name, type.getHandle()), module, name, type);
    }
}
