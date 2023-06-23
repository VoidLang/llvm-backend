import org.bytedeco.javacpp.*;
import org.bytedeco.llvm.LLVM.*;

import static org.bytedeco.llvm.global.LLVM.*;

public class Factorial {
    // a 'char *' used to retrieve error messages from LLVM
    private static final BytePointer error = new BytePointer();

    public static void main(String[] args) {
        // Stage 1: Initialize LLVM components
        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeTarget();

        // Stage 2: Build the factorial function.
        LLVMContextRef context = LLVMContextCreate();
        LLVMModuleRef module = LLVMModuleCreateWithNameInContext("factorial", context);
        LLVMBuilderRef builder = LLVMCreateBuilderInContext(context);
        LLVMTypeRef i32Type = LLVMInt32TypeInContext(context);
        LLVMTypeRef factorialType = LLVMFunctionType(i32Type, i32Type, /* argumentCount */ 1, /* isVariadic */ 0);

        LLVMValueRef factorial = LLVMAddFunction(module, "factorial", factorialType);
        LLVMSetFunctionCallConv(factorial, LLVMCCallConv);

        LLVMValueRef n = LLVMGetParam(factorial, /* parameterIndex */0);
        LLVMValueRef zero = LLVMConstInt(i32Type, 0, /* signExtend */ 0);
        LLVMValueRef one = LLVMConstInt(i32Type, 1, /* signExtend */ 0);
        LLVMBasicBlockRef entry = LLVMAppendBasicBlockInContext(context, factorial, "entry");
        LLVMBasicBlockRef ifFalse = LLVMAppendBasicBlockInContext(context, factorial, "if_false");
        LLVMBasicBlockRef exit = LLVMAppendBasicBlockInContext(context, factorial, "exit");

        LLVMPositionBuilderAtEnd(builder, entry);
        LLVMValueRef condition = LLVMBuildICmp(builder, LLVMIntEQ, n, zero, "condition = n == 0");
        LLVMBuildCondBr(builder, condition, exit, ifFalse);

        LLVMPositionBuilderAtEnd(builder, ifFalse);
        LLVMValueRef nMinusOne = LLVMBuildSub(builder, n, one, "nMinusOne = n - 1");
        PointerPointer<Pointer> arguments = new PointerPointer<>(1)
                .put(0, nMinusOne);
        LLVMValueRef factorialResult = LLVMBuildCall2(builder, factorialType, factorial, arguments, 1, "factorialResult = factorial(nMinusOne)");
        LLVMValueRef resultIfFalse = LLVMBuildMul(builder, n, factorialResult, "resultIfFalse = n * factorialResult");
        LLVMBuildBr(builder, exit);

        LLVMPositionBuilderAtEnd(builder, exit);
        LLVMValueRef phi = LLVMBuildPhi(builder, i32Type, "result");
        PointerPointer<Pointer> phiValues = new PointerPointer<>(2)
                .put(0, one)
                .put(1, resultIfFalse);
        PointerPointer<Pointer> phiBlocks = new PointerPointer<>(2)
                .put(0, entry)
                .put(1, ifFalse);
        LLVMAddIncoming(phi, phiValues, phiBlocks, /* pairCount */ 2);
        LLVMBuildRet(builder, phi);



        // SETUP MAIN METHOD


        // LLVMTypeRef mainType = LLVMFunctionType(i32Type, new LLVMTypeRef[]{}, 0, 0);
        LLVMTypeRef mainType = LLVMFunctionType(i32Type, new PointerPointer<>(), /* argumentCount */ 0, /* isVariadic */ 0);
        LLVMValueRef mainFunction = LLVMAddFunction(module, "main", mainType);
        LLVMBasicBlockRef mainEntry = LLVMAppendBasicBlockInContext(context, mainFunction, "entry");
        LLVMPositionBuilderAtEnd(builder, mainEntry);

        PointerPointer<Pointer> mArgs = new PointerPointer<>(1)
            .put(0, LLVMConstInt(i32Type, 10, 0));

        LLVMValueRef res = LLVMBuildCall2(builder, factorialType, factorial, mArgs, 1, "factorialResult");
        //LLVMValueRef result = LLVMBuildCall2(builder, factorialType, factorial, arguments, 1, "factorialResult = factorial(nMinusOne)");
        LLVMBuildRet(builder, res);


        // <end>






        // Stage 3: Verify the module using LLVMVerifier
        if (LLVMVerifyModule(module, LLVMPrintMessageAction, error) != 0) {
            LLVMDisposeMessage(error);
            return;
        }

        // Stage 4: Create a pass pipeline using the legacy pass manager
        LLVMPassManagerRef pm = LLVMCreatePassManager();
        // LLVMAddAggressiveInstCombinerPass(pm);
        LLVMAddNewGVNPass(pm);
        LLVMAddCFGSimplificationPass(pm);
        LLVMRunPassManager(pm, module);
        LLVMDumpModule(module);

        // Stage 5: Execute the code using MCJIT
        LLVMExecutionEngineRef engine = new LLVMExecutionEngineRef();
        LLVMMCJITCompilerOptions options = new LLVMMCJITCompilerOptions();
        if (LLVMCreateMCJITCompilerForModule(engine, module, options, 3, error) != 0) {
            System.err.println("Failed to create JIT compiler: " + error.getString());
            LLVMDisposeMessage(error);
            return;
        }


        LLVMGenericValueRef argument = LLVMCreateGenericValueOfInt(i32Type, 10, /* signExtend */ 0);
        LLVMGenericValueRef result = LLVMRunFunction(engine, factorial, /* argumentCount */ 1, argument);
        System.out.println();
        System.out.println("; Running factorial(10) with MCJIT...");
        System.out.println("; Result: " + LLVMGenericValueToInt(result, /* signExtend */ 0));

        // WRITE IR OUTPUT TO FILE
        LLVMPrintModuleToFile(module, "D:\\.dev\\GitHub\\LLVM-Backend\\data\\output.ll", error);
        LLVMWriteBitcodeToFile(module, "D:\\.dev\\GitHub\\LLVM-Backend\\data\\output.bc");

        // Stage 6: Dispose of the allocated resources
        LLVMDisposeExecutionEngine(engine);
        LLVMDisposePassManager(pm);
        LLVMDisposeBuilder(builder);
        LLVMContextDispose(context);
    }
}
