package org.voidlang.llvm.element;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.llvm.LLVM.LLVMModuleRef;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.bytedeco.llvm.global.LLVM.*;

public class IRModule implements Disposable {
    private final LLVMModuleRef handle;

    private final IRContext context;

    private final String name;

    IRModule(LLVMModuleRef handle, IRContext context, String name) {
        this.handle = handle;
        this.context = context;
        this.name = name;
    }

    public IRGlobal addGlobal(IRType type, String name) {
        return new IRGlobal(LLVMAddGlobal(handle, type.getHandle(), name));
    }

    public IRGlobal getGlobal(String name) {
        return new IRGlobal(LLVMGetNamedGlobal(handle, name));
    }

    public void dump() {
        LLVMDumpModule(handle);
    }

    @Override
    public void dispose() {
        LLVMDisposeModule(handle);
    }

    public boolean verify(VerifierFailureAction action, BytePointer error) {
        return LLVMVerifyModule(handle, action.code, error) == 0;
    }

    public LLVMModuleRef getHandle() {
        return handle;
    }

    public IRContext getContext() {
        return context;
    }

    public String getName() {
        return name;
    }

    public static IRModule create(IRContext context, String name) {
        return new IRModule(LLVMModuleCreateWithNameInContext(name, context.getHandle()), context, name);
    }

    public IRMemoryBuffer writeToMemory() {
        return new IRMemoryBuffer(LLVMWriteBitcodeToMemoryBuffer(handle));
    }

    public String print() {
        return LLVMPrintModuleToString(handle).getString(StandardCharsets.UTF_8);
    }

    public enum VerifierFailureAction {
        /**
         * Verifier will print to stderr and abort()
         */
        ABORT_PROCESS(0),

        /**
         * Verifier will print to stderr and return 1
         */
        PRINT_MESSAGE(1),

        /**
         * Verifier will just return 1
         */
        RETURN_STATUS(2);

        private final int code;

        VerifierFailureAction(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static VerifierFailureAction valueOf(int code) {
            return Arrays.stream(values())
                .filter(action -> action.code == code)
                .findFirst()
                .orElse(null);
        }
    }
}
