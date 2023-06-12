import org.bytedeco.javacpp.*;
import org.bytedeco.llvm.LLVM.*;

import static org.bytedeco.llvm.global.LLVM.*;
public class TwoModuleFactorial {
    // a 'char *' used to retrieve error messages from LLVM
    private static final BytePointer error = new BytePointer();

    public static void main(String[] args) {
        // Stage 1: Initialize LLVM components
        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeTarget();

        // Stage 2: Create the factorial module
        LLVMContextRef factorialContext = LLVMContextCreate();
        LLVMModuleRef factorialModule = LLVMModuleCreateWithNameInContext("factorial", factorialContext);
        LLVMBuilderRef factorialBuilder = LLVMCreateBuilderInContext(factorialContext);
        LLVMTypeRef i32Type = LLVMInt32TypeInContext(factorialContext);
        LLVMTypeRef factorialType = LLVMFunctionType(i32Type, i32Type, /* argumentCount */ 1, /* isVariadic */ 0);

        LLVMValueRef factorial = LLVMAddFunction(factorialModule, "factorial", factorialType);
        LLVMSetFunctionCallConv(factorial, LLVMCCallConv);

        LLVMValueRef n = LLVMGetParam(factorial, /* parameterIndex */0);
        LLVMValueRef zero = LLVMConstInt(i32Type, 0, /* signExtend */ 0);
        LLVMValueRef one = LLVMConstInt(i32Type, 1, /* signExtend */ 0);
        LLVMBasicBlockRef entry = LLVMAppendBasicBlockInContext(factorialContext, factorial, "entry");
        LLVMBasicBlockRef ifFalse = LLVMAppendBasicBlockInContext(factorialContext, factorial, "if_false");
        LLVMBasicBlockRef exit = LLVMAppendBasicBlockInContext(factorialContext, factorial, "exit");

        LLVMPositionBuilderAtEnd(factorialBuilder, entry);
        LLVMValueRef condition = LLVMBuildICmp(factorialBuilder, LLVMIntEQ, n, zero, "condition = n == 0");
        LLVMBuildCondBr(factorialBuilder, condition, exit, ifFalse);

        LLVMPositionBuilderAtEnd(factorialBuilder, ifFalse);
        LLVMValueRef nMinusOne = LLVMBuildSub(factorialBuilder, n, one, "nMinusOne = n - 1");
        PointerPointer<Pointer> arguments = new PointerPointer<>(1)
                .put(0, nMinusOne);
        LLVMValueRef factorialResult = LLVMBuildCall2(factorialBuilder, factorialType, factorial, arguments, 1, "factorialResult = factorial(nMinusOne)");
        LLVMValueRef resultIfFalse = LLVMBuildMul(factorialBuilder, n, factorialResult, "resultIfFalse = n * factorialResult");
        LLVMBuildBr(factorialBuilder, exit);

        LLVMPositionBuilderAtEnd(factorialBuilder, exit);
        LLVMValueRef phi = LLVMBuildPhi(factorialBuilder, i32Type, "result");
        PointerPointer<Pointer> phiValues = new PointerPointer<>(2)
                .put(0, one)
                .put(1, resultIfFalse);
        PointerPointer<Pointer> phiBlocks = new PointerPointer<>(2)
                .put(0, entry)
                .put(1, ifFalse);
        LLVMAddIncoming(phi, phiValues, phiBlocks, /* pairCount */ 2);
        LLVMBuildRet(factorialBuilder, phi);

        LLVMContextRef mainContext = LLVMContextCreate();
        LLVMModuleRef mainModule = LLVMModuleCreateWithNameInContext("main", mainContext);
        LLVMBuilderRef mainBuilder = LLVMCreateBuilderInContext(mainContext);

        LLVMTypeRef mainType = LLVMFunctionType(i32Type, new PointerPointer<>(), /* argumentCount */ 0, /* isVariadic */ 0);
        LLVMValueRef mainFunction = LLVMAddFunction(mainModule, "main", mainType);
        LLVMBasicBlockRef mainEntry = LLVMAppendBasicBlockInContext(mainContext, mainFunction, "entry");
        LLVMPositionBuilderAtEnd(mainBuilder, mainEntry);

        PointerPointer<Pointer> mArgs = new PointerPointer<>(1)
                .put(0, LLVMConstInt(i32Type, 10, 0));

        LLVMValueRef res = LLVMBuildCall2(mainBuilder, factorialType, factorial, mArgs, 1, "factorialResult");
        LLVMBuildRet(mainBuilder, res);

        // Link the factorial module into the main module
        LLVMLinkModules2(mainModule, factorialModule);

        // Stage 4: Verify the module using LLVMVerifier
        if (LLVMVerifyModule(mainModule, LLVMPrintMessageAction, error) != 0) {
            LLVMDisposeMessage(error);
            return;
        }

        // Stage 5: Create a pass pipeline using the legacy pass manager
        LLVMPassManagerRef pm = LLVMCreatePassManager();
        // LLVMAddAggressiveInstCombinerPass(pm);
        LLVMAddNewGVNPass(pm);
        LLVMAddCFGSimplificationPass(pm);
        LLVMRunPassManager(pm, mainModule);
        LLVMDumpModule(mainModule);

        // Stage 6: Execute the code using MCJIT
        LLVMExecutionEngineRef engine = new LLVMExecutionEngineRef();
        LLVMMCJITCompilerOptions options = new LLVMMCJITCompilerOptions();
        if (LLVMCreateMCJITCompilerForModule(engine, mainModule, options, 3, error) != 0) {
            System.err.println("Failed to create JIT compiler: " + error.getString());
            LLVMDisposeMessage(error);
            return;
        }

        LLVMGenericValueRef argument = LLVMCreateGenericValueOfInt(i32Type, 10, /* signExtend */ 0);
        LLVMGenericValueRef result = LLVMRunFunction(engine, factorial, /* argumentCount */ 1, argument);
        System.out.println();
        System.out.println("; Running factorial(10) with MCJIT...");
        System.out.println("; Result: " + LLVMGenericValueToInt(result, /* signExtend */ 0));

        // Stage 7: Dispose of the allocated resources
        LLVMDisposeExecutionEngine(engine);
        LLVMDisposePassManager(pm);
        LLVMDisposeBuilder(mainBuilder);
        LLVMDisposeBuilder(factorialBuilder);
        LLVMContextDispose(mainContext);
        LLVMContextDispose(factorialContext);
    }
}
