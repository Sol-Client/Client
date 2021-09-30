package me.mcblueparrot.client.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;
import java.util.stream.Collectors;

public class AsmTransformer {

    private List<ClassNodeTransformer> transformers;

    public byte[] transform(String name, byte[] input) {
        List<ClassNodeTransformer> applicable =
                transformers.stream().filter((transformers) -> transformers.test(name)).collect(Collectors.toList());

        if(applicable.isEmpty()) return input;

        ClassReader reader = new ClassReader(input);
        ClassNode clazz = new ClassNode();
        reader.accept(clazz, 0);

        for(ClassNodeTransformer transformer : applicable) {
            transformer.apply(clazz);
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        clazz.accept(writer);
        return writer.toByteArray();
    }

}
