package wrapper.struct.definition;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.voidlang.llvm.element.*;

import java.util.ArrayList;
import java.util.Collections;

import static org.bytedeco.llvm.global.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.LLVMInitializeNativeTarget;

public class StructRelationship {
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

        // create a named type "Foo" in the context
        IRStruct fooType = IRStruct.define(context, "Foo");
        // create a named type "Bar" in the context
        IRStruct barType = IRStruct.define(context, "Bar");

        // set the body of the Foo structure
        fooType.setMembers(Collections.singletonList(IRType.pointerType(barType)));
        // set the body of the Bar structure
        barType.setMembers(Collections.singletonList(IRType.pointerType(fooType)));

        // declare return types in the context
        IRType i32type = IRType.int32(context);

        // create the program entry point
        IRFunctionType mainType = IRFunctionType.create(context, i32type, new ArrayList<>(), false);
        IRFunction mainFunction = IRFunction.create(module, "main", mainType);

        // add the "entry" block to the function, so instructions can be added
        IRBlock entry = IRBlock.create(mainFunction, "entry");
        builder.positionAtEnd(entry);

        // create a new instance of the "Foo" type, that is allocated on the STACK
        IRValue fooPtr = builder.alloc(fooType, "fooPtr");
        // create a new instance of the "Bar" type, that is allocated on the STACK
        IRValue barPtr = builder.alloc(barType, "barPtr");

        // get the Foo field pointer from the Bar struct
        IRValue fooField = builder.structMemberPointer(barType, barPtr, 0, "foo");
        // get the Bar field pointer from the Foo struct
        IRValue barField = builder.structMemberPointer(fooType, fooPtr, 0, "bar");

        // store the Foo instance in the Bar instance
        builder.store(fooPtr, fooField);
        // store the Bar instance in the Foo instance
        builder.store(barPtr, barField);

        // make the main function return the value 10
        builder.returnValue(i32type.constInt(10));

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

        IRGenericValue result = engine.runFunction(mainFunction, new ArrayList<>());
        System.out.println();
        System.out.println("Result: " + result.toInt());

        // Dispose of the allocated resources
        builder.dispose();
        module.dispose();
        context.dispose();
    }
}
