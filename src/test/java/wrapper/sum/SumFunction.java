package wrapper.sum;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.voidlang.llvm.element.*;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.llvm.global.LLVM.*;

public class SumFunction {
    public static void main(String[] args) {
        // Initialize LLVM components
        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeTarget();

        // Create the LLVM context and module
        Context context = Context.create();
        Module module = Module.create(context, "my_module");
        Builder builder = Builder.create(context);

        // Create the function type
        Type returnType = Type.int32(context);
        ArrayList<Type> parameterTypes = new ArrayList<>();
        parameterTypes.add(returnType);
        parameterTypes.add(returnType);
        FunctionType sumType = FunctionType.create(context, returnType, parameterTypes, false);

        // Create the function
        Function sum = Function.create(module, "sum", sumType);

        // Create the entry basic block
        Block block = Block.create(context, sum, "entry");
        builder.positionAtEnd(block);

        // get the two operands
        Value left = sum.getParameter(0);
        Value right = sum.getParameter(1);

        // add the two operands and return their sum
        Value add = builder.add(left, right, "result");
        builder.returnValue(add);

        FunctionType mainType = FunctionType.create(context, returnType, new ArrayList<>(), false);

        Function main = Function.create(module, "main", mainType);

        block = Block.create(context, main, "entry");
        builder.positionAtEnd(block);


        List<Value> arguments = new ArrayList<>();
        arguments.add(returnType.constInt(2));
        arguments.add(returnType.constInt(3));

        Value res = builder.call(sum, arguments, "call_res");
        builder.returnValue(res);


        // Verify the module
        BytePointer error = new BytePointer((Pointer) null);
        if (!module.verify(Module.VerifierFailureAction.PRINT_MESSAGE, error)) {
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

        GenericValue result = engine.runFunction(main, new ArrayList<>());
        System.out.println();
        System.out.println("Result: " + result.toInt());

        // Dispose of the allocated resources
        builder.dispose();
        module.dispose();
        context.dispose();
    }
}
