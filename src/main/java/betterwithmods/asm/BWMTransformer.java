package betterwithmods.asm;

import betterwithmods.asm.tweaks.TileEntityFurnaceTweaks;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

/**
 * Created by blueyu2 on 11/27/16.
 */
public class BWMTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String s, String s1, byte[] bytes) {
        if("net.minecraft.tileentity.TileEntityFurnace".equals(s) || "aqv".equals(s)){
            return patchTileEntityFurnace(bytes);
        }
        return bytes;
    }

    private byte[] patchTileEntityFurnace(byte[] bytes){
        String name = TileEntityFurnaceTweaks.class.getName();
        name = name.replaceAll("\\.", "/");

        final ClassReader cr = new ClassReader(bytes);
        final ClassNode cn = new ClassNode(Opcodes.ASM5);
        cr.accept(cn, 0);

        for (MethodNode m : cn.methods) {
            if (m.name.equals("func_174904_a")) {
                InsnList list = new InsnList();
                list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                list.add(new VarInsnNode(Opcodes.ALOAD, 1));
                list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, name, "getCookTime", "(Lnet/minecraft/item/ItemStack;)I", false));
                list.add(new InsnNode(Opcodes.IRETURN));
                m.instructions.insertBefore(m.instructions.getFirst(), list);
            } else if (m.name.equals("func_145952_a")) {
                InsnList list = new InsnList();
                list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, name, "getItemBurnTime", "(Lnet/minecraft/item/ItemStack;)I", false));
                list.add(new InsnNode(Opcodes.IRETURN));
                m.instructions.insertBefore(m.instructions.getFirst(), list);
            }
        }

        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cn.accept(cw);
        return cw.toByteArray();
    }
}
