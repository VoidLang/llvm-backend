package org.voidlang.llvm.element;

import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;

import java.util.List;

import static org.bytedeco.llvm.global.LLVM.LLVMStructCreateNamed;
import static org.bytedeco.llvm.global.LLVM.LLVMStructSetBody;

public class IRStruct extends IRType {
    private final List<IRType> members;

    IRStruct(LLVMTypeRef handle, IRContext context, List<IRType> members) {
        super(handle, context);
        this.members = members;
    }

    public static IRStruct define(IRContext context, String name, List<IRType> members) {
        LLVMTypeRef handle = LLVMStructCreateNamed(context.getHandle(), name);

        PointerPointer<LLVMTypeRef> types = new PointerPointer<>(members.size());
        for (int i = 0; i < members.size(); i++)
            types.put(i, members.get(i).getHandle());

        LLVMStructSetBody(handle, types, members.size(), 0);

        return new IRStruct(handle, context, members);
    }
}
