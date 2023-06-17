package wrapper.function;


import org.bytedeco.javacpp.*;
import org.voidlang.llvm.element.*;

import java.util.ArrayList;

import static org.bytedeco.llvm.global.LLVM.*;

public class WrappedFunction {
    public static void main(String[] args) {
        // Initialize LLVM components
        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeTarget();

        // Create the LLVM context and module
        IRContext context = IRContext.create();
        IRModule module = IRModule.create(context, "my_module");
        IRBuilder builder = IRBuilder.create(context);

        // Create the function type
        IRType returnType = IRType.int32(context);
        IRFunctionType functionType = IRFunctionType.create(context, returnType, new ArrayList<>(), false);

        // Create the function
        IRFunction function = IRFunction.create(module, "my_function", functionType);

        // Create the entry basic block
        IRBlock block = IRBlock.create(context, function, "entry");
        builder.positionAtEnd(block);

        // Create the constant value
        IRValue constantValue = returnType.constInt(1337, false);

        // Build the return instruction
        builder.returnValue(constantValue);

        // Verify the module
        BytePointer error = new BytePointer((Pointer) null);
        if (!module.verify(IRModule.VerifierFailureAction.PRINT_MESSAGE, error)) {
            System.err.println("Error: " + error.getString());
            LLVMDisposeMessage(error);
            return;
        }

        // Dump the module IR
        module.dump();

        // Stage 5: Execute the code using MCJIT
        IRExecutionEngine engine = IRExecutionEngine.create();
        MMCJITCompilerOptions options = MMCJITCompilerOptions.create();
        if (!engine.createMCJITCompilerForModule(module, options, error)) {
            System.err.println("Failed to create JIT compiler: " + error.getString());
            LLVMDisposeMessage(error);
            return;
        }

        IRGenericValue result = engine.runFunction(function, new ArrayList<>());
        System.out.println();
        System.out.println("Result: " + result.toInt());

        // Dispose of the allocated resources
        builder.dispose();
        module.dispose();
        context.dispose();
    }
}
