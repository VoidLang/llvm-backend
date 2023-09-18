package wrapper;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.voidlang.llvm.element.*;

import java.util.ArrayList;
import java.util.Collections;

import static org.bytedeco.llvm.global.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.LLVMInitializeNativeTarget;

public class ClassMethod {
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

        // declare return types in the context
        IRType i32type = IRType.int32(context);
        IRType voidType = IRType.voidType(context);

        // create a named type "Entity" with one i32 field
        ArrayList<IRType> members = new ArrayList<>();
        members.add(i32type);
        IRStruct entityType = IRStruct.define(context, "Entity", members);

        // create the test function that takes in a pointer for Entity
        IRFunctionType testType = IRFunctionType.create(voidType, Collections.singletonList(IRType.pointerType(entityType)));
        IRFunction testFunction = IRFunction.create(module, "test", testType);

        // add the "entry" block to the function, so instructions can be added
        IRBlock testEntry = IRBlock.create(testFunction, "entry");
        builder.positionAtEnd(testEntry);

        builder.returnVoid();

        // create the program entry point
        IRFunctionType mainType = IRFunctionType.create(context, i32type, new ArrayList<>(), false);
        IRFunction main = IRFunction.create(module, "main", mainType);

        // create the entry section of the main method
        IRBlock block = IRBlock.create(context, main, "entry");
        builder.positionAtEnd(block);

        // create an instance of an Entity as a pointer on the stack
        IRValue entityPointer = builder.alloc(entityType, "carPtr");

        // call the test function that takes in Entity by reference
        builder.call(testFunction, Collections.singletonList(entityPointer));

        builder.returnValue(i32type.constInt(1337));

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
        ExecutionEngine engine = ExecutionEngine.create();
        MMCJITCompilerOptions options = MMCJITCompilerOptions.create();
        if (!engine.createMCJITCompilerForModule(module, options, error)) {
            System.err.println("Failed to create JIT compiler: " + error.getString());
            LLVMDisposeMessage(error);
            return;
        }

        IRGenericValue result = engine.runFunction(main, new ArrayList<>());
        System.out.println();
        System.out.println("Result: " + result.toInt());

        // Dispose of the allocated resources
        builder.dispose();
        module.dispose();
        context.dispose();
    }
}
