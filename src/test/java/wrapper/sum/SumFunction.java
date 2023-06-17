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
        IRContext context = IRContext.create();
        IRModule module = IRModule.create(context, "my_module");
        IRBuilder builder = IRBuilder.create(context);

        // Create the function type
        IRType returnType = IRType.int32(context);
        ArrayList<IRType> parameterTypes = new ArrayList<>();
        parameterTypes.add(returnType);
        parameterTypes.add(returnType);
        IRFunctionType sumType = IRFunctionType.create(context, returnType, parameterTypes, false);

        // Create the function
        IRFunction sum = IRFunction.create(module, "sum", sumType);

        // Create the entry basic block
        IRBlock block = IRBlock.create(context, sum, "entry");
        builder.positionAtEnd(block);

        // get the two operands
        IRValue left = sum.getParameter(0);
        IRValue right = sum.getParameter(1);

        // add the two operands and return their sum
        IRValue add = builder.add(left, right, "result");
        builder.returnValue(add);

        IRFunctionType mainType = IRFunctionType.create(context, returnType, new ArrayList<>(), false);

        IRFunction main = IRFunction.create(module, "main", mainType);

        block = IRBlock.create(context, main, "entry");
        builder.positionAtEnd(block);


        List<IRValue> arguments = new ArrayList<>();
        arguments.add(returnType.constInt(2));
        arguments.add(returnType.constInt(3));

        IRValue res = builder.call(sum, arguments, "call_res");
        builder.returnValue(res);


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
        IRExecutionEngine engine = IRExecutionEngine.create();
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
