package wrapper.console;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.*;

import java.nio.charset.StandardCharsets;

import static org.bytedeco.llvm.global.LLVM.*;

public class Console {
    public static void main(String[] args) throws Exception {
        // Initialize LLVM components
        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeTarget();

        LLVMContextRef context = LLVMContextCreate();
        LLVMModuleRef module = LLVMModuleCreateWithNameInContext("print_test", context);
        LLVMBuilderRef builder = LLVMCreateBuilderInContext(context);

        LLVMTypeRef i32Type = LLVMInt32TypeInContext(context);
        LLVMTypeRef i8Type = LLVMInt8TypeInContext(context);
        // LLVMTypeRef handleType = LLVMPointerTypeInContext(context, 0);
        LLVMTypeRef i8PointerType = LLVMPointerType(i8Type, 0);
        LLVMTypeRef i32PointerType = LLVMPointerType(i32Type, 0);

        // Create the GetStdHandle function declaration
        LLVMTypeRef[] getStdHandleParamTypes = {
            i32Type
        };
        LLVMTypeRef getStdHandleType = LLVMFunctionType(i32Type, new PointerPointer<>(getStdHandleParamTypes), getStdHandleParamTypes.length, 0);
        LLVMValueRef getStdHandleFunc = LLVMAddFunction(module, "GetStdHandle", getStdHandleType);

        LLVMTypeRef getLastErrorType = LLVMFunctionType(i32Type, new PointerPointer<LLVMTypeRef>(), 0, 0);
        LLVMValueRef getLastErrorFunc = LLVMAddFunction(module, "GetLastError", getLastErrorType);

        LLVMTypeRef[] writeConsoleAParamTypes = {
            i32Type, // std handle
            i8PointerType, // message buffer
            i32Type, // buffer length
            i32PointerType, // chars written (output)
            i32Type // NULL
        };

        LLVMTypeRef writeConsoleAType = LLVMFunctionType(i32Type, new PointerPointer<>(writeConsoleAParamTypes), writeConsoleAParamTypes.length, 0);
        LLVMValueRef writeConsoleAFunc = LLVMAddFunction(module, "WriteConsoleA", writeConsoleAType);

        LLVMTypeRef mainType = LLVMFunctionType(i32Type, new PointerPointer<>(), 0, 0);
        LLVMValueRef mainFunction = LLVMAddFunction(module, "main", mainType);
        LLVMBasicBlockRef entry = LLVMAppendBasicBlockInContext(context, mainFunction, "entry");

        LLVMPositionBuilderAtEnd(builder, entry);

        PointerPointer<Pointer> mArgs = new PointerPointer<>(1)
            .put(0, LLVMConstInt(i32Type, -11, 0)); // STANDARD OUTPUT

        // LLVMValueRef consoleHandle = LLVMConstInt(i32Type, getJavaConsoleHandle(), 0);
        LLVMValueRef consoleHandle = LLVMBuildCall2(builder, getStdHandleType, getStdHandleFunc, mArgs, 1, "std_handle");

        String message = "Hello, World!";
        byte[] messageBytes = message.getBytes();

        LLVMValueRef messageString = LLVMConstStringInContext(context, message, message.length(), 1);

        LLVMTypeRef stringType = LLVMArrayType(i8Type, message.length());
        LLVMValueRef constString = LLVMAddGlobal(module, stringType, "text");
        LLVMSetInitializer(constString, messageString);

        // LLVMValueRef bytesWritten = LLVMConstNull(i32PointerType);
        LLVMValueRef bytesWritten = LLVMBuildAlloca(builder, i32Type, "bytesWritten");

        LLVMValueRef[] writeConsoleArgs = {
            consoleHandle,
            LLVMGetNamedGlobal(module, "text"),
            LLVMConstInt(i32Type, messageBytes.length, 0),
            bytesWritten,
            LLVMConstInt(i32Type, 0, 0),
        };

        LLVMBuildCall2(builder, writeConsoleAType, writeConsoleAFunc, new PointerPointer<>(writeConsoleArgs), writeConsoleArgs.length, "print");

        LLVMValueRef written = LLVMBuildLoad2(builder, i32Type, bytesWritten, "written");

        // Build the return instruction
        LLVMBuildRet(builder, written);

        // Stage 3: Verify the module using LLVMVerifier
        BytePointer error = new BytePointer();
        if (LLVMVerifyModule(module, LLVMPrintMessageAction, error) != 0) {
            LLVMDisposeMessage(error);
            return;
        }

        String folder = "C:\\Users\\admin\\Documents\\GitHub\\LLVM-Backend\\src\\test\\java\\wrapper\\console\\files\\";
        LLVMWriteBitcodeToFile(module, folder + "bitcode.bc");
        LLVMPrintModuleToFile(module,  folder + "dump.ll", new BytePointer());

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

        LLVMGenericValueRef result = LLVMRunFunction(engine, mainFunction, 0, new PointerPointer<LLVMGenericValueRef>());

        String debug = LLVMPrintModuleToString(module).getString(StandardCharsets.UTF_8);
        System.err.println(debug);

        System.out.println("Result: " + LLVMGenericValueToInt(result, /* signExtend */ 0));

        // Dispose of the allocated resources
        LLVMDisposeBuilder(builder);
        LLVMDisposeModule(module);
        LLVMContextDispose(context);
    }
}
