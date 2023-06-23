package wrapper.conditional;

import org.bytedeco.javacpp.*;
import org.bytedeco.llvm.LLVM.*;

import static org.bytedeco.llvm.global.LLVM.*;

public class OriginalIfElse {
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
        LLVMBuilderRef builder = LLVMCreateBuilderInContext(context);

        // Create the "test" function
        LLVMTypeRef testFunctionReturnType = LLVMInt32TypeInContext(context);
        LLVMTypeRef[] testFunctionParamTypes = new LLVMTypeRef[]{ LLVMInt32TypeInContext(context) };
        LLVMTypeRef testFunctionType = LLVMFunctionType(testFunctionReturnType, new PointerPointer<>(testFunctionParamTypes), testFunctionParamTypes.length, 0);
        LLVMValueRef testFunction = LLVMAddFunction(module, "test", testFunctionType);

        // Set the entry block for the "test" function
        LLVMBasicBlockRef entryBlock = LLVMAppendBasicBlock(testFunction, "entry");
        LLVMPositionBuilderAtEnd(builder, entryBlock);

        // Get the function argument
        LLVMValueRef argValue = LLVMGetParam(testFunction, 0);

        // Create basic blocks for the if-then and else clauses
        LLVMBasicBlockRef ifBlock = LLVMAppendBasicBlock(testFunction, "if");
        LLVMBasicBlockRef elseBlock = LLVMAppendBasicBlock(testFunction, "else");
       // LLVMBasicBlockRef mergeBlock = LLVMAppendBasicBlock(testFunction, "merge");

        // Compare the argument with 30
        LLVMValueRef condition = LLVMBuildICmp(builder, LLVMIntSGT, argValue, LLVMConstInt(LLVMInt32TypeInContext(context), 30, 0), "cmp");

        // Emit the branch instruction based on the condition
        LLVMBuildCondBr(builder, condition, ifBlock, elseBlock);

        // Emit the if-then clause
        LLVMPositionBuilderAtEnd(builder, ifBlock);
        LLVMBuildRet(builder, LLVMConstInt(LLVMInt32TypeInContext(context), 200, 0));

        // Emit the branch instruction to jump to the merge block after the if-then clause
        //LLVMBuildBr(builder, mergeBlock);

        // Emit the else clause
        LLVMPositionBuilderAtEnd(builder, elseBlock);
        LLVMBuildRet(builder, LLVMConstInt(LLVMInt32TypeInContext(context), 100, 0));

        // Set the merge block as the current block
        //LLVMPositionBuilderAtEnd(builder, mergeBlock);

        LLVMTypeRef i32Type = LLVMInt32TypeInContext(context);

        LLVMTypeRef functionType = LLVMFunctionType(i32Type, new PointerPointer<LLVMTypeRef>(0), 0, 0);
        LLVMValueRef mainFunction = LLVMAddFunction(module, "main", functionType);

        // add the "entry" block to the function, so instructions can be added
        LLVMBasicBlockRef entry = LLVMAppendBasicBlock(mainFunction, "entry");
        LLVMPositionBuilderAtEnd(builder, entry);

        LLVMValueRef[] arguments = new LLVMValueRef[] { LLVMConstInt(i32Type, 40, 0) };
        LLVMValueRef call = LLVMBuildCall2(builder, testFunctionType, testFunction, new PointerPointer<>(arguments), 1, "");
        LLVMBuildRet(builder, call);

        // Verify the module
        BytePointer error = new BytePointer((Pointer) null);
        if (LLVMVerifyModule(module, LLVMPrintMessageAction, error) != 0) {
            System.err.println("Error: " + error.getString());
            LLVMDisposeMessage(error);
            return;
        }

        LLVMDumpModule(module);

        LLVMExecutionEngineRef engine = new LLVMExecutionEngineRef();
        LLVMMCJITCompilerOptions options = new LLVMMCJITCompilerOptions();
        if (LLVMCreateMCJITCompilerForModule(engine, module, options, options.sizeof(), error) != 0) {
            System.err.println("Failed to create JIT compiler: " + error.getString());
            LLVMDisposeMessage(error);
            return;
        }

        LLVMGenericValueRef result = LLVMRunFunction(engine, mainFunction, 0, new PointerPointer<LLVMGenericValueRef>());

        System.out.println();
        System.out.println("Result: " + LLVMGenericValueToInt(result, 0));

        LLVMDisposeBuilder(builder);
        LLVMDisposeModule(module);
        LLVMContextDispose(context);
    }
}
