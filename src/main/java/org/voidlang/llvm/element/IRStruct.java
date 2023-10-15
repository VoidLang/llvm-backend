package org.voidlang.llvm.element;

import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;

import java.util.List;

import static org.bytedeco.llvm.global.LLVM.*;

public class IRStruct extends IRType {
    IRStruct(LLVMTypeRef handle, IRContext context) {
        super(handle, context);
    }

    public IRStruct setMembers(List<IRType> members) {
        PointerPointer<Pointer> args = new PointerPointer<>(members.size());
        for (int i = 0; i < members.size(); i++)
            args.put(i, members.get(i).getHandle());
        LLVMStructSetBody(getHandle(), args, members.size(), 0);
        return this;
    }

    public static IRStruct define(IRContext context, String name) {
        return new IRStruct(LLVMStructCreateNamed(context.getHandle(), name), context);
    }

    public static IRStruct define(IRContext context, String name, List<IRType> members) {
        return define(context, name).setMembers(members);
    }
}
