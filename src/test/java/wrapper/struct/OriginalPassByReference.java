package wrapper.struct;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.*;

import static org.bytedeco.llvm.global.LLVM.*;

public class OriginalPassByReference {
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

        // create the test function that takes in a pointer for Car
        LLVMTypeRef[] testParams = new LLVMTypeRef[] { LLVMPointerType(structType, 0) };
        LLVMTypeRef testType = LLVMFunctionType(voidType, new PointerPointer<>(testParams), testParams.length, 0);
        LLVMValueRef testFunction = LLVMAddFunction(module, "test", testType);

        // add the "entry" block to the function, so instructions can be added
        LLVMBasicBlockRef testEntry = LLVMAppendBasicBlock(testFunction, "entry");
        LLVMPositionBuilderAtEnd(builder, testEntry);

        // get the Car pointer from the function parameters
        LLVMValueRef param = LLVMGetParam(testFunction, 0);

        // get the field pointers from the struct by their declaration indices
        LLVMValueRef testSpeedPtr = LLVMBuildStructGEP2(builder, structType, param, 0, "speedPtr");
        LLVMValueRef testWeightPtr = LLVMBuildStructGEP2(builder, structType, param, 1, "weightPtr");

        // assign the pointers of the Car instance with the following data: { speed = 100, weight = 50 }
        LLVMBuildStore(builder, LLVMConstInt(i32type, 100, 0), testSpeedPtr);
        LLVMBuildStore(builder, LLVMConstInt(i32type, 50, 0), testWeightPtr);

        LLVMBuildRetVoid(builder);

        // create a main method that return a 32-bit integer
        LLVMTypeRef mainType = LLVMFunctionType(i32type, new PointerPointer<LLVMTypeRef>(0), 0, 0);
        LLVMValueRef mainFunction = LLVMAddFunction(module, "main", mainType);

        // add the "entry" block to the function, so instructions can be added
        LLVMBasicBlockRef mainEntry = LLVMAppendBasicBlock(mainFunction, "entry");
        LLVMPositionBuilderAtEnd(builder, mainEntry);

        // create a new instance of the "Car" type, that is allocated on the STACK
        LLVMValueRef carInstance = LLVMBuildAlloca(builder, structType, "myCar");

        // get the field pointers from the struct by their declaration indices
        LLVMValueRef speedFieldPtr = LLVMBuildStructGEP2(builder, structType, carInstance, 0, "speedPtr");
        LLVMValueRef weightFieldPtr = LLVMBuildStructGEP2(builder, structType, carInstance, 1, "weightPtr");

        // assign the pointers of the Car instance with the following data: { speed = 10, weight = 20 }
        LLVMBuildStore(builder, LLVMConstInt(i32type, 10, 0), speedFieldPtr);
        LLVMBuildStore(builder, LLVMConstInt(i32type, 20, 0), weightFieldPtr);

        // call the test function that takes in Car by reference, and modifies its fields
        LLVMValueRef[] arguments = new LLVMValueRef[] { carInstance };
        LLVMBuildCall2(builder, testType, testFunction, new PointerPointer<>(arguments), 1, "");

        // load the values from the previously assigned pointers, and store them in two local variables
        LLVMValueRef speedValue = LLVMBuildLoad2(builder, i32type, speedFieldPtr, "speed");
        LLVMValueRef weightValue = LLVMBuildLoad2(builder, i32type, weightFieldPtr, "weight");

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
