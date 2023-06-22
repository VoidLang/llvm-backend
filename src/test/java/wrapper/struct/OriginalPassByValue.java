package wrapper.struct;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.*;

import static org.bytedeco.llvm.global.LLVM.*;

public class OriginalPassByValue {
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

        // define the members of the Car struct
        LLVMTypeRef[] structMembers = new LLVMTypeRef[] {
            LLVMInt32TypeInContext(context), // speed
            LLVMInt32TypeInContext(context)  // weight
        };

        // create a named type "Car" in the context
        LLVMTypeRef structType = LLVMStructCreateNamed(context, "Car");
        // set the body of the structure
        LLVMStructSetBody(structType, new PointerPointer<>(structMembers), /* field count */ 2, 0);

        // declare return types in the context
        LLVMTypeRef i32type = LLVMInt32TypeInContext(context);
        LLVMTypeRef voidType = LLVMVoidTypeInContext(context);

        // create the test function that takes in Car by value
        LLVMTypeRef[] testParams = new LLVMTypeRef[] { structType };
        LLVMTypeRef testType = LLVMFunctionType(voidType, new PointerPointer<>(testParams), testParams.length, 0);
        LLVMValueRef testFunction = LLVMAddFunction(module, "test", testType);

        // add the "entry" block to the function, so instructions can be added
        LLVMBasicBlockRef testEntry = LLVMAppendBasicBlock(testFunction, "entry");
        LLVMPositionBuilderAtEnd(builder, testEntry);

        // get the Car instance from the function parameters
        LLVMValueRef param = LLVMGetParam(testFunction, 0);

        // assign new values for the Car parameter
        LLVMBuildInsertValue(builder, param, LLVMConstInt(i32type, 100, 0), 0, "speedInsert");
        LLVMBuildInsertValue(builder, param, LLVMConstInt(i32type, 50, 0), 1, "weightInsert");

        // extract the field values from the Car parameter
        LLVMValueRef testSpeedValue = LLVMBuildExtractValue(builder, param, 0, "speed");
        LLVMValueRef testWeightValue = LLVMBuildExtractValue(builder, param, 1, "weight");

        LLVMBuildRetVoid(builder);

        // create a main method that returns a 32-bit integer
        LLVMTypeRef mainType = LLVMFunctionType(i32type, new PointerPointer<LLVMTypeRef>(0), 0, 0);
        LLVMValueRef mainFunction = LLVMAddFunction(module, "main", mainType);

        // add the "entry" block to the function, so instructions can be added
        LLVMBasicBlockRef mainEntry = LLVMAppendBasicBlock(mainFunction, "entry");
        LLVMPositionBuilderAtEnd(builder, mainEntry);

        // create a new instance of the "Car" type, that is allocated on the STACK
        LLVMValueRef carPointer = LLVMBuildAlloca(builder, structType, "carPtr");

        // assign the values directly to the Car instance
        LLVMBuildStore(builder, LLVMConstInt(i32type, 10, 0), LLVMBuildStructGEP2(builder, structType, carPointer, 0, "speedPtr"));
        LLVMBuildStore(builder, LLVMConstInt(i32type, 20, 0), LLVMBuildStructGEP2(builder, structType, carPointer, 1, "weightPtr"));

        // call the test function that takes Car by value
        LLVMValueRef[] arguments = new LLVMValueRef[] { LLVMBuildLoad2(builder, structType, carPointer, "carInst") };
        LLVMBuildCall2(builder, testType, testFunction, new PointerPointer<>(arguments), 1, "");

        // load the values from the previously assigned pointers and store them in two local variables
        LLVMValueRef speedValue = LLVMBuildLoad2(builder, i32type, LLVMBuildStructGEP2(builder, structType, carPointer, 0, ""), "speed");
        LLVMValueRef weightValue = LLVMBuildLoad2(builder, i32type, LLVMBuildStructGEP2(builder, structType, carPointer, 1, ""), "weight");

        // add the two values
        LLVMValueRef sumValue = LLVMBuildAdd(builder, speedValue, weightValue, "sum");

        // make the main method return their sum
        LLVMBuildRet(builder, sumValue);

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
