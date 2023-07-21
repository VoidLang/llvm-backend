package wrapper.console;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.*;

import static org.bytedeco.llvm.global.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.LLVMInitializeNativeTarget;

public class Console {
    public static void main(String[] args) {
        // Initialize LLVM components
        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeTarget();

        // Dynamically load kernel32 library
        LLVMLoadLibraryPermanently("kernel32.dll");

        LLVMModuleRef module = LLVMModuleCreateWithName("my_module");
        LLVMBuilderRef builder = LLVMCreateBuilder();

        // Create the GetStdHandle function declaration
        LLVMTypeRef[] getStdHandleParamTypes = {
            LLVMInt32Type()
        };
        LLVMTypeRef getStdHandleType = LLVMFunctionType(LLVMInt32Type(), new PointerPointer<>(getStdHandleParamTypes), getStdHandleParamTypes.length, 0);
        LLVMValueRef getStdHandleFunc = LLVMAddFunction(module, "GetStdHandle", getStdHandleType);

        // Call the GetStdHandle function
        LLVMValueRef STD_OUTPUT_HANDLE = LLVMConstInt(LLVMInt32Type(), -11, 0);
        LLVMValueRef[] getStdHandleArgs = { STD_OUTPUT_HANDLE };

        LLVMValueRef consoleHandle = LLVMBuildCall2(builder, getStdHandleType, getStdHandleFunc, new PointerPointer<>(getStdHandleArgs), getStdHandleArgs.length, "consoleHandle");
        consoleHandle = LLVMBuildIntToPtr(builder, consoleHandle, LLVMPointerType(LLVMInt8Type(), 0), "consoleHandle");

        // Declare the WriteConsole function type
        LLVMTypeRef[] paramTypes = {
            LLVMPointerType(LLVMInt8Type(), 0),
            LLVMInt32Type(),
            LLVMInt32Type(),
            LLVMPointerType(LLVMInt32Type(), 0)
        };
        LLVMTypeRef writeConsoleType = LLVMFunctionType(LLVMInt32Type(), new PointerPointer<>(paramTypes), paramTypes.length, 0);

        // Declare and define the function in the module
        LLVMValueRef writeConsoleFunc = LLVMAddFunction(module, "WriteConsoleA", writeConsoleType);


        LLVMTypeRef mainType = LLVMFunctionType(LLVMInt32Type(), new PointerPointer<>(), 0, 0);
        LLVMValueRef mainFunction = LLVMAddFunction(module, "main", mainType);
        LLVMBasicBlockRef entry = LLVMAppendBasicBlock(mainFunction, "entry");
        LLVMPositionBuilderAtEnd(builder, entry);

        /*
        // Call the WriteConsole function
        String message = "Hello, console!";

        byte[] messageBytes = message.getBytes();
        LLVMValueRef bytesWritten = LLVMConstNull(LLVMPointerType(LLVMInt32Type(), 0));
        LLVMValueRef[] writeConsoleArgs = {
            consoleHandle,
            LLVMConstString(message, message.length(), 0),
            LLVMConstInt(LLVMInt32Type(), messageBytes.length, 0),
            LLVMConstInt(LLVMInt32Type(), 0, 0),
            bytesWritten
        };
        LLVMBuildCall2(builder, writeConsoleType, writeConsoleFunc, new PointerPointer<>(writeConsoleArgs), writeConsoleArgs.length, "writeConsoleCall");
        */

        LLVMBuildRet(builder, LLVMConstInt(LLVMInt32Type(), 10, 0));

        // Stage 3: Verify the module using LLVMVerifier
        BytePointer error = new BytePointer();
        if (LLVMVerifyModule(module, LLVMPrintMessageAction, error) != 0) {
            LLVMDisposeMessage(error);
            return;
        }

        // Create the execution engine and load the module
        LLVMExecutionEngineRef engine = new LLVMExecutionEngineRef();
        BytePointer errorMessage = new BytePointer((Pointer) null);
        if (LLVMCreateJITCompilerForModule(engine, module, 2, errorMessage) != 0) {
            System.err.println("Failed to create execution engine: " + errorMessage.getString());
            LLVMDisposeMessage(errorMessage);
            // Clean up and return
            LLVMDisposeModule(module);
            return;
        }

        // Clean up resources
        LLVMDisposeExecutionEngine(engine);
        LLVMDisposeModule(module);
    }
}
