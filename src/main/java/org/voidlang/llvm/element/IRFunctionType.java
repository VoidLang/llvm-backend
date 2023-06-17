package org.voidlang.llvm.element;

import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;

import java.util.List;

import static org.bytedeco.llvm.global.LLVM.LLVMFunctionType;

public class IRFunctionType extends IRType {
    private final IRType returnType;
    private final List<IRType> parameterTypes;
    private final boolean variadic;

    IRFunctionType(LLVMTypeRef handle, IRContext context, IRType returnType, List<IRType> parameterTypes, boolean variadic) {
        super(handle, context);
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
        this.variadic = variadic;
    }

    public IRType getReturnType() {
        return returnType;
    }

    public List<IRType> getParameterTypes() {
        return parameterTypes;
    }

    public boolean isVariadic() {
        return variadic;
    }

    public static IRFunctionType create(IRContext context, IRType returnType, List<IRType> parameterTypes, boolean variadic) {
        int parameterLength = parameterTypes.size();
        PointerPointer<LLVMTypeRef> parameters = new PointerPointer<>(parameterLength);
        for (int i = 0; i < parameterLength; i++)
            parameters.put(i, parameterTypes.get(i).getHandle());
        LLVMTypeRef handle = LLVMFunctionType(returnType.getHandle(), parameters, parameterLength, variadic ? 1 : 0);
        return new IRFunctionType(handle, context, returnType, parameterTypes, variadic);
    }

    public static IRFunctionType create(IRType returnType, List<IRType> parameterTypes, boolean variadic) {
        return create(returnType.getContext(), returnType, parameterTypes, variadic);
    }

    public static IRFunctionType create(IRType returnType, List<IRType> parameterTypes) {
        return create(returnType, parameterTypes, false);
    }
}
