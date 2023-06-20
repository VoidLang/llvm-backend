package wrapper.struct;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.*;

import static org.bytedeco.llvm.global.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.LLVMModuleCreateWithNameInContext;

public class OriginalNamedStruct {
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

        // create a main method that return a 32-bit integer
        LLVMTypeRef returnType = LLVMInt32TypeInContext(context);
        LLVMTypeRef functionType = LLVMFunctionType(returnType, new PointerPointer<LLVMTypeRef>(0), 0, 0);
        LLVMValueRef mainFunction = LLVMAddFunction(module, "main", functionType);

        // add the "entry" block to the function, so instructions can be added
        LLVMBasicBlockRef entryBlock = LLVMAppendBasicBlock(mainFunction, "entry");
        LLVMPositionBuilderAtEnd(builder, entryBlock);

        // create a new instance of the "Car" type, that is allocated on the STACK
        LLVMValueRef carInstance = LLVMBuildAlloca(builder, structType, "myCar");

        // get the field pointers from the struct by their declaration indices
        LLVMValueRef speedFieldPtr = LLVMBuildStructGEP2(builder, structType, carInstance,0, "speedPtr");
        LLVMValueRef weightFieldPtr = LLVMBuildStructGEP2(builder, structType, carInstance, 1, "weightPtr");

        // assign the pointers of the Car instance with the following data: { speed = 10, weight = 20 }
        LLVMBuildStore(builder, LLVMConstInt(returnType, 10, 0), speedFieldPtr);
        LLVMBuildStore(builder, LLVMConstInt(returnType, 20, 0), weightFieldPtr);

        // load the values from the previously assigned pointers, and store them in two local variables
        LLVMValueRef speedValue = LLVMBuildLoad2(builder, returnType, speedFieldPtr, "speedValue");
        LLVMValueRef weightValue = LLVMBuildLoad2(builder, returnType, weightFieldPtr, "weightValue");

        // add the two values
        LLVMValueRef sumValue = LLVMBuildAdd(builder, speedValue, weightValue, "sumValue");

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

        System.out.println("Execution done successfully");

        LLVMDisposeBuilder(builder);
        LLVMDisposeModule(module);
        LLVMContextDispose(context);
    }
}
