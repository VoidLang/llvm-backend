package org.voidlang.llvm.element;

import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;

import java.util.List;

import static org.bytedeco.llvm.global.LLVM.LLVMFunctionType;

public class FunctionType extends Type {
    private final Type returnType;
    private final List<Type> parameterTypes;
    private final boolean variadic;

    FunctionType(LLVMTypeRef handle, Context context, Type returnType, List<Type> parameterTypes, boolean variadic) {
        super(handle, context);
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
        this.variadic = variadic;
    }

    public Type getReturnType() {
        return returnType;
    }

    public List<Type> getParameterTypes() {
        return parameterTypes;
    }

    public boolean isVariadic() {
        return variadic;
    }

    public static FunctionType create(Context context, Type returnType, List<Type> parameterTypes, boolean variadic) {
        int parameterLength = parameterTypes.size();
        PointerPointer<LLVMTypeRef> parameters = new PointerPointer<>(parameterLength);
        for (int i = 0; i < parameterLength; i++)
            parameters.put(i, parameterTypes.get(i).getHandle());
        LLVMTypeRef handle = LLVMFunctionType(returnType.getHandle(), parameters, parameterLength, variadic ? 1 : 0);
        return new FunctionType(handle, context, returnType, parameterTypes, variadic);
    }

    public static FunctionType create(Type returnType, List<Type> parameterTypes, boolean variadic) {
        return create(returnType.getContext(), returnType, parameterTypes, variadic);
    }

    public static FunctionType create(Type returnType, List<Type> parameterTypes) {
        return create(returnType, parameterTypes, false);
    }
}
