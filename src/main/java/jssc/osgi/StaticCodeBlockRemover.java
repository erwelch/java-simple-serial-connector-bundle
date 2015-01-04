package jssc.osgi;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;

import static org.objectweb.asm.Opcodes.*;


public class StaticCodeBlockRemover implements WeavingHook {
    
	public void weave(WovenClass wovenClass) {
		wovenClass.setBytes(transform(wovenClass.getBytes()));
		
	}

    public byte[] transform(byte[] origClassData) {
        ClassReader cr = new ClassReader(origClassData);
        final ClassWriter cw = new ClassWriter(cr, Opcodes.ASM4);
        
        // wrap the ClassWriter with a ClassVisitor that adds the static block to
        // initialize the above fields
        ClassVisitor cv = new ClassVisitor(ASM4, cw) {

          public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (cv == null) {
              return null;
            }
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            if ("<clinit>".equals(name)) {
              return null;
            } else {
              return mv;
            }
          }
        };

        // feed the original class to the wrapped ClassVisitor
        cr.accept(cv, 0);

        // produce the modified class
        byte[] newClassData = cw.toByteArray();
        return newClassData;
      }

}

