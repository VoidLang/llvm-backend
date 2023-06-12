package org.voidlang.llvm.element;

import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.List;

import static org.bytedeco.llvm.global.LLVM.*;

public class Builder implements Disposable {
    private final LLVMBuilderRef handle;
    private final Context context;

    Builder(LLVMBuilderRef handle, Context context) {
        this.handle = handle;
        this.context = context;
    }

    public void positionAtEnd(Block block) {
        LLVMPositionBuilderAtEnd(handle, block.getHandle());
    }

    public void positionBefore(Value value) {
        LLVMPositionBuilderBefore(handle, value.getHandle());
    }

    public Value returnValue(Value value) {
        return new Value(LLVMBuildRet(handle, value.getHandle()));
    }

    public Value add(Value left, Value right, String name) {
        return new Value(LLVMBuildAdd(handle, left.getHandle(), right.getHandle(), name));
    }

    public Value subtract(Value left, Value right, String name) {
        return new Value(LLVMBuildSub(handle, left.getHandle(), right.getHandle(), name));
    }

    public Value multiply(Value left, Value right, String name) {
        return new Value(LLVMBuildMul(handle, left.getHandle(), right.getHandle(), name));
    }

    public Value call(FunctionType type, Function function, List<Value> arguments, String name) {
        PointerPointer<Pointer> args = new PointerPointer<>(arguments.size());
        for (int i = 0; i < arguments.size(); i++) {
            args.put(i, arguments.get(i).getHandle());
        }
        LLVMValueRef res = LLVMBuildCall2(handle, type.getHandle(), function.getHandle(), args, arguments.size(), name);
        return new Value(res);
    }

    public Value call(Function function, List<Value> arguments, String name) {
        PointerPointer<Pointer> args = new PointerPointer<>(arguments.size());
        for (int i = 0; i < arguments.size(); i++) {
            args.put(i, arguments.get(i).getHandle());
        }
        LLVMValueRef res = LLVMBuildCall2(handle, function.getType().getHandle(), function.getHandle(), args, arguments.size(), name);
        return new Value(res);
    }

    @Override
    public void dispose() {
        LLVMDisposeBuilder(handle);
    }

    public LLVMBuilderRef getHandle() {
        return handle;
    }

    public Context getContext() {
        return context;
    }

    public static Builder create(Context context) {
        return new Builder(LLVMCreateBuilderInContext(context.getHandle()), context);
    }
}
