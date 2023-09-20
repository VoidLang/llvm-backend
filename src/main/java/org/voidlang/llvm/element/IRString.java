package org.voidlang.llvm.element;

import static org.bytedeco.llvm.global.LLVM.LLVMConstStringInContext;

public class IRString extends IRValue {
    private final String message;
    private final int length;
    private final boolean nullTerminate;

    private IRType type;

    public IRString(IRContext context, String message, int length, boolean nullTerminate) {
        // null terminate is inverted, as it wants 'do not terminate'
        super(LLVMConstStringInContext(context.getHandle(), message, length, nullTerminate ? 0 : 1));
        this.message = message;
        this.length = length;
        this.nullTerminate = nullTerminate;

        type = IRType.arrayType(IRType.int8(context), length);
    }

    public IRString(IRContext context, String message, boolean nullTerminate) {
        this(context, message, message.length(), nullTerminate);
    }

    public String getMessage() {
        return message;
    }

    public int getLength() {
        return length;
    }

    public boolean isNullTerminate() {
        return nullTerminate;
    }

    public IRType getType() {
        return type;
    }
}
