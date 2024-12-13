package net.toshayo.waterframes.transformers;

import net.toshayo.waterframes.WaterFramesPlugin;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


public class MemoryAllocVisitor extends ClassVisitor {
    private final String transformedName;

    public MemoryAllocVisitor(String transformedName, ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
        this.transformedName = transformedName;
    }

    @Override
    public MethodVisitor visitMethod(int access, String methodName, String desc, String signature, String[] exceptions) {
        if (methodName.equals("create") || methodName.equals("resize") || methodName.equals("free")) {
            MethodVisitor methodVisitor = cv.visitMethod(access, methodName, desc, signature, exceptions);
            return new MethodVisitor(Opcodes.ASM5, methodVisitor) {
                @Override
                public void visitCode() {
                    WaterFramesPlugin.LOGGER.info("Patching {}.{}{}", transformedName, methodName, desc);
                    switch (methodName) {
                        case "create":
                            mv.visitVarInsn(Opcodes.ILOAD, 0);
                            break;
                        case "resize":
                            mv.visitVarInsn(Opcodes.ALOAD, 0);
                            mv.visitVarInsn(Opcodes.ILOAD, 1);
                            break;
                        case "free":
                            mv.visitVarInsn(Opcodes.ALOAD, 0);
                            break;
                    }
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "net/toshayo/waterframes/PluginUtils", "MemoryAlloc_" + methodName, desc, false);
                    if (methodName.equals("free")) {
                        mv.visitInsn(Opcodes.RETURN);
                    } else {
                        mv.visitInsn(Opcodes.ARETURN);
                    }
                }
            };
        }
        return super.visitMethod(access, methodName, desc, signature, exceptions);
    }
}
