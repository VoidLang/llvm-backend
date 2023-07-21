package wrapper.struct;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.voidlang.llvm.element.*;

import java.util.ArrayList;
import java.util.Collections;

import static org.bytedeco.llvm.global.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.LLVMInitializeNativeTarget;

public class PassByReference2 {
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

        // create a named type "Car" with the two i32 fields
        ArrayList<IRType> members = new ArrayList<>();
        members.add(i32type);
        members.add(i32type);
        IRStruct carType = IRStruct.define(context, "Car", members);

        // create the test function that takes in a pointer for Car
        IRFunctionType createType = IRFunctionType.create(carType.toPointerType(), new ArrayList<>());
        IRFunction createFunction = IRFunction.create(module, "createCar", createType);

        // create the entry section of the main method
        IRBlock block = IRBlock.create(context, createFunction, "entry");
        builder.positionAtEnd(block);

        // create an instance of a Car as a pointer on the stack
        IRValue carPointer = builder.alloc(carType, "carPtr");

        // get the field pointers from the struct by their declaration indices
        IRValue speedPtr = builder.structMemberPointer(carType, carPointer,0, "speedPtr");
        IRValue weightPtr = builder.structMemberPointer(carType, carPointer, 1, "weightPtr");

        // assign the pointers of the Car instance with the following data: { speed = 10, weight = 20 }
        builder.store(i32type.constInt(10), speedPtr);
        builder.store(i32type.constInt(20), weightPtr);

        builder.returnValue(carPointer);

        // create the program entry point
        IRFunctionType mainType = IRFunctionType.create(context, i32type, new ArrayList<>(), false);
        IRFunction main = IRFunction.create(module, "main", mainType);

        // create the entry section of the main method
        block = IRBlock.create(context, main, "entry");
        builder.positionAtEnd(block);

        carPointer = builder.call(createFunction, new ArrayList<>(), "carPtr");

        // get the field pointers from the struct by their declaration indices
        speedPtr = builder.structMemberPointer(carType, carPointer,0, "speedPtr");
        weightPtr = builder.structMemberPointer(carType, carPointer, 1, "weightPtr");

        IRValue speed = builder.load(i32type, speedPtr, "speed");
        IRValue weight = builder.load(i32type, weightPtr, "weight");

        IRValue sum = builder.add(speed, weight);
        builder.returnValue(sum);

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
