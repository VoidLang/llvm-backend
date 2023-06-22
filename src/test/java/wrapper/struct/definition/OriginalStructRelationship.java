package wrapper.struct.definition;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.*;

import static org.bytedeco.llvm.global.LLVM.*;

public class OriginalStructRelationship {
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

        // create a named type "Foo" in the context
        LLVMTypeRef fooType = LLVMStructCreateNamed(context, "Foo");
        // create a named type "Bar" in the context
        LLVMTypeRef barType = LLVMStructCreateNamed(context, "Bar");

        // define the members of the Foo struct
        LLVMTypeRef[] fooMembers = new LLVMTypeRef[] {
            LLVMPointerType(barType, 0)
        };
        // define the members of the Bar struct
        LLVMTypeRef[] barMembers = new LLVMTypeRef[] {
            LLVMPointerType(fooType, 0)
        };

        // set the body of the Foo structure
        LLVMStructSetBody(fooType, new PointerPointer<>(fooMembers), 1, 0);
        // set the body of the Bar structure
        LLVMStructSetBody(barType, new PointerPointer<>(barMembers), 1, 0);

        // create a main method that return a 32-bit integer
        LLVMTypeRef returnType = LLVMInt32TypeInContext(context);
        LLVMTypeRef functionType = LLVMFunctionType(returnType, new PointerPointer<>(0), 0, 0);
        LLVMValueRef mainFunction = LLVMAddFunction(module, "main", functionType);

        // add the "entry" block to the function, so instructions can be added
        LLVMBasicBlockRef entryBlock = LLVMAppendBasicBlock(mainFunction, "entry");
        LLVMPositionBuilderAtEnd(builder, entryBlock);

        // create a new instance of the "Foo" type, that is allocated on the STACK
        LLVMValueRef fooPtr = LLVMBuildAlloca(builder, fooType, "fooPtr");
        // create a new instance of the "Bar" type, that is allocated on the STACK
        LLVMValueRef barPtr = LLVMBuildAlloca(builder, barType, "barPtr");

        // get the Foo field pointer from the Bar struct
        LLVMValueRef fooField = LLVMBuildStructGEP2(builder, barType, barPtr, 0, "foo");
        // get the Bar field pointer from the Foo struct
        LLVMValueRef barField = LLVMBuildStructGEP2(builder, fooType, fooPtr, 0, "bar");

        // store the Foo instance in the Bar instance
        LLVMBuildStore(builder, fooPtr, fooField);
        // store the Bar instance in the Foo instance
        LLVMBuildStore(builder, barPtr, barField);

        // make the main function return the value 10
        LLVMBuildRet(builder, LLVMConstInt(returnType, 10, 0));

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
