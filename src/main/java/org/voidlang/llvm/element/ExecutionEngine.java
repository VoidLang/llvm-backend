package org.voidlang.llvm.element;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.LLVMExecutionEngineRef;
import org.bytedeco.llvm.LLVM.LLVMGenericValueRef;

import java.util.List;

import static org.bytedeco.llvm.global.LLVM.LLVMCreateMCJITCompilerForModule;
import static org.bytedeco.llvm.global.LLVM.LLVMRunFunction;

public class ExecutionEngine {
    private final LLVMExecutionEngineRef handle;

    ExecutionEngine(LLVMExecutionEngineRef handle) {
        this.handle = handle;
    }

    public boolean createMCJITCompilerForModule(Module module, MMCJITCompilerOptions options, BytePointer error) {
        return LLVMCreateMCJITCompilerForModule(handle, module.getHandle(), options.getHandle(), options.getHandle().sizeof(), error) == 0;
    }

    public GenericValue runFunction(Function function, List<GenericValue> arguments) {
        int argsLength = arguments.size();
        PointerPointer<LLVMGenericValueRef> args = new PointerPointer<>(argsLength);
        for (int i = 0; i < arguments.size(); i++)
            args.put(i, arguments.get(i).getHandle());
        return new GenericValue(LLVMRunFunction(handle, function.getHandle(), argsLength, args));
    }

    public LLVMExecutionEngineRef getHandle() {
        return handle;
    }

    public static ExecutionEngine create() {
        return new ExecutionEngine(new LLVMExecutionEngineRef());
    }
}
