package bleach.hack.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

public class FabricReflect {

	public static Field getField(final Class<?> cls, String obfName, String deobfName) {
		if(cls == null) return null;
        
		Field field = null;
        for (Class<?> cls1 = cls; cls1 != null; cls1 = cls1.getSuperclass()) {
            try { field = cls1.getDeclaredField(obfName); } catch (Exception e) {}
			try { field = cls1.getDeclaredField(deobfName); } catch (Exception e) {}
			
			if(field == null) continue;
			
			if (!Modifier.isPublic(field.getModifiers())) field.setAccessible(true);
			return field;
        }
        
        for (final Class<?> class1 : ClassUtils.getAllInterfaces(cls)) {
            try { field = class1.getField(obfName); } catch (Exception e) {}
			try { field = class1.getField(deobfName); } catch (Exception e) {}
			
			if(field != null) break;
        }
        return field;
	}
	
	public static void writeField(final Object target, final Object value, String obfName, String deobfName) {
		if(target == null) return;
		
		final Class<?> cls = target.getClass();
        final Field field = getField(cls, obfName, deobfName);
        
        if(field == null) return;
        
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        
        try {
			field.set(target, value);
		} catch (Exception e) {}
        
	}
	
	public static Object invokeMethod(Object target, String obfName, String deobfName, Object[] args, Class<?>[] paramTypes) {
		Object o = null;
        try { o = MethodUtils.invokeMethod(target, true, obfName, args); }catch(Exception e) {}
        try { o = MethodUtils.invokeMethod(target, true, deobfName, args); }catch(Exception e) {}
        return o;
    }
}
