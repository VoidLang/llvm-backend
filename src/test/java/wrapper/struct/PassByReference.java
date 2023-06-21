package wrapper.struct;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.voidlang.llvm.element.*;

import java.util.ArrayList;
import java.util.Collections;

import static org.bytedeco.llvm.global.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.LLVMInitializeNativeTarget;

public class PassByReference {
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

        IRType i32type = IRType.int32(context);
        IRType voidType = IRType.voidType(context);

        // create a named type "Car" with the two i32 fields
        ArrayList<IRType> members = new ArrayList<>();
        members.add(i32type);
        members.add(i32type);
        IRStruct carType = IRStruct.define(context, "Car", members);

        // create the test function that takes in a pointer for Car
        IRFunctionType testType = IRFunctionType.create(voidType, Collections.singletonList(IRType.pointerType(carType)));
        IRFunction testFunction = IRFunction.create(module, "test", testType);

        // add the "entry" block to the function, so instructions can be added
        IRBlock testEntry = IRBlock.create(testFunction, "entry");
        builder.positionAtEnd(testEntry);

        // get the Car pointer from the function parameters
        IRValue parameter = testFunction.getParameter(0);

        // get the field pointers from the struct by their declaration indices
        IRValue testSpeedPtr = builder.structMemberPointer(carType, parameter, 0, "speedPtr");
        IRValue testWeightPtr = builder.structMemberPointer(carType, parameter, 1, "weightPtr");

        // assign the pointers of the Car instance with the following data: { speed = 100, weight = 50 }
        builder.store(i32type.constInt(100), testSpeedPtr);
        builder.store(i32type.constInt(50), testWeightPtr);

        builder.returnVoid();

        // create the program entry point
        IRFunctionType mainType = IRFunctionType.create(context, i32type, new ArrayList<>(), false);
        IRFunction main = IRFunction.create(module, "main", mainType);

        // create the entry section of the main method
        IRBlock block = IRBlock.create(context, main, "entry");
        builder.positionAtEnd(block);

        // create an instance of a Car as a pointer on the stack
        IRValue carInstance = builder.alloc(carType, "myCar");

        // get the field pointers from the struct by their declaration indices
        IRValue speedPtr = builder.structMemberPointer(carType, carInstance,0, "speedPtr");
        IRValue weightPtr = builder.structMemberPointer(carType, carInstance, 1, "weightPtr");

        // assign the pointers of the Car instance with the following data: { speed = 10, weight = 20 }
        builder.store(i32type.constInt(10), speedPtr);
        builder.store(i32type.constInt(20), weightPtr);

        // call the test function that takes in Car by reference, and modifies its fields
        builder.call(testFunction, Collections.singletonList(carInstance), "");

        // load the values from the previously assigned pointers, and store them in two local variables
        IRValue speed = builder.load(i32type, speedPtr, "speed");
        IRValue weight = builder.load(i32type, weightPtr, "weight");

        // make the main method return their sum
        IRValue sum = builder.add(speed, weight, "sum");
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
