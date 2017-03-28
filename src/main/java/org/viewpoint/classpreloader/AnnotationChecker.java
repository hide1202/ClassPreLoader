package org.viewpoint.classpreloader;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AnnotationChecker {
    Path classFilePath;

    public AnnotationChecker(Path classFilePath) {
        this.classFilePath = classFilePath;
    }

    private static String toDescFromClass(Class clazz) {
        return "L" + clazz.getName().replace('.', '/') + ";";
    }

    public boolean checkAnnotation(Class annotationClass) {
        try {
            byte[] bytes = Files.readAllBytes(classFilePath);
            ClassReader reader = new ClassReader(bytes);
            InternalVisitor visitor = new InternalVisitor(annotationClass);
            reader.accept(visitor, ClassReader.SKIP_DEBUG);
            return visitor.hasAnnotation();
        } catch (IOException e) {
            return false;
        }
    }

    private static class InternalVisitor extends ClassVisitor {
        private boolean result = false;
        private Class clazz;

        public InternalVisitor(Class clazz) {
            super(Opcodes.ASM5);
            this.clazz = clazz;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            if (!visible) return super.visitAnnotation(desc, visible);
            if (desc.equals(toDescFromClass(clazz)))
                result = true;
            return super.visitAnnotation(desc, visible);
        }

        boolean hasAnnotation() {
            return this.result;
        }
    }
}
