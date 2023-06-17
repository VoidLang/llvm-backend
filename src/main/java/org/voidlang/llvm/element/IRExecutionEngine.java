package org.voidlang.llvm.element;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.LLVMExecutionEngineRef;
import org.bytedeco.llvm.LLVM.LLVMGenericValueRef;

import java.util.List;

import static org.bytedeco.llvm.global.LLVM.LLVMCreateMCJITCompilerForModule;
import static org.bytedeco.llvm.global.LLVM.LLVMRunFunction;

public class IRExecutionEngine {
    private final LLVMExecutionEngineRef handle;

    IRExecutionEngine(LLVMExecutionEngineRef handle) {
        this.handle = handle;
    }

    public boolean createMCJITCompilerForModule(IRModule module, MMCJITCompilerOptions options, BytePointer error) {
        return LLVMCreateMCJITCompilerForModule(handle, module.getHandle(), options.getHandle(), options.getHandle().sizeof(), error) == 0;
    }

    public IRGenericValue runFunction(IRFunction function, List<IRGenericValue> arguments) {
        int argsLength = arguments.size();
        PointerPointer<LLVMGenericValueRef> args = new PointerPointer<>(argsLength);
        for (int i = 0; i < arguments.size(); i++)
            args.put(i, arguments.get(i).getHandle());
        return new IRGenericValue(LLVMRunFunction(handle, function.getHandle(), argsLength, args));
    }

    public LLVMExecutionEngineRef getHandle() {
        return handle;
    }

    public static IRExecutionEngine create() {
        return new IRExecutionEngine(new LLVMExecutionEngineRef());
    }
}
