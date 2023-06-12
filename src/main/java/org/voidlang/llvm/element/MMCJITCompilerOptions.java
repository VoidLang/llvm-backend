package org.voidlang.llvm.element;

import org.bytedeco.llvm.LLVM.LLVMMCJITCompilerOptions;

public class MMCJITCompilerOptions {
    private final LLVMMCJITCompilerOptions handle;

    MMCJITCompilerOptions(LLVMMCJITCompilerOptions handle) {
        this.handle = handle;
    }

    public LLVMMCJITCompilerOptions getHandle() {
        return handle;
    }

    public static MMCJITCompilerOptions create() {
        return new MMCJITCompilerOptions(new LLVMMCJITCompilerOptions());
    }
}
