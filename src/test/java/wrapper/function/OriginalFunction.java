package wrapper.function;

import org.bytedeco.javacpp.*;
import org.bytedeco.llvm.LLVM.*;

import static org.bytedeco.llvm.global.LLVM.*;

public class OriginalFunction {
    public static void main(String[] args) {
        // Initialize LLVM components
        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeTarget();

        // Create the LLVM context and module
        LLVMContextRef context = LLVMContextCreate();
        LLVMModuleRef module = LLVMModuleCreateWithNameInContext("my_module", context);

        // Create the function type
        LLVMTypeRef returnType = LLVMInt32TypeInContext(context);
        LLVMTypeRef functionType = LLVMFunctionType(returnType, new PointerPointer<LLVMTypeRef>(0), 0, 0);

        // Create the function
        LLVMValueRef function = LLVMAddFunction(module, "my_function", functionType);

        // Create the entry basic block
        LLVMBasicBlockRef entryBlock = LLVMAppendBasicBlockInContext(context, function, "entry");
        LLVMBuilderRef builder = LLVMCreateBuilderInContext(context);

        LLVMPositionBuilderAtEnd(builder, entryBlock);


        // Create the constant value
        LLVMValueRef constantValue = LLVMConstInt(returnType, 1337, 0);

        // Build the return instruction
        LLVMBuildRet(builder, constantValue);

        // Verify the module
        BytePointer error = new BytePointer((Pointer) null);
        if (LLVMVerifyModule(module, LLVMPrintMessageAction, error) != 0) {
            System.err.println("Error: " + error.getString());
            LLVMDisposeMessage(error);
            return;
        }

        // Dump the module IR
        LLVMDumpModule(module);

        // Stage 5: Execute the code using MCJIT
        LLVMExecutionEngineRef engine = new LLVMExecutionEngineRef();
        LLVMMCJITCompilerOptions options = new LLVMMCJITCompilerOptions();
        if (LLVMCreateMCJITCompilerForModule(engine, module, options, options.sizeof(), error) != 0) {
            System.err.println("Failed to create JIT compiler: " + error.getString());
            LLVMDisposeMessage(error);
            return;
        }

        LLVMGenericValueRef result = LLVMRunFunction(engine, function, /* argumentCount */ 0, new PointerPointer<LLVMGenericValueRef>());

        System.out.println();
        System.out.println("Result: " + LLVMGenericValueToInt(result, /* signExtend */ 0));

        // Dispose of the allocated resources
        LLVMDisposeBuilder(builder);
        LLVMDisposeModule(module);
        LLVMContextDispose(context);
    }
}
