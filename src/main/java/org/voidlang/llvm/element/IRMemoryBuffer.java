package org.voidlang.llvm.element;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.llvm.LLVM.LLVMMemoryBufferRef;

import static org.bytedeco.llvm.global.LLVM.LLVMGetBufferSize;
import static org.bytedeco.llvm.global.LLVM.LLVMGetBufferStart;

public class IRMemoryBuffer {
    private final LLVMMemoryBufferRef handle;

    IRMemoryBuffer(LLVMMemoryBufferRef handle) {
        this.handle = handle;
    }

    public long size() {
        return LLVMGetBufferSize(handle);
    }

    public BytePointer getBufferStart() {
        return LLVMGetBufferStart(handle);
    }
}
