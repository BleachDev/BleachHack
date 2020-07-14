/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
 * Copyright (c) 2019 Bleach.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bleach.hack.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

public class FabricReflect {

	public static Field getField(final Class<?> cls, String obfName, String deobfName) {
		if (cls == null) return null;
        
		Field field = null;
        for (Class<?> cls1 = cls; cls1 != null; cls1 = cls1.getSuperclass()) {
            try { field = cls1.getDeclaredField(obfName); } catch (Exception e) {}
			try { field = cls1.getDeclaredField(deobfName); } catch (Exception e) {}
			
			if (field == null) continue;
			
			if (!Modifier.isPublic(field.getModifiers())) field.setAccessible(true);
			return field;
        }
        
        for (final Class<?> class1 : ClassUtils.getAllInterfaces(cls)) {
            try { field = class1.getField(obfName); } catch (Exception e) {}
			try { field = class1.getField(deobfName); } catch (Exception e) {}
			
			if (field != null) break;
        }
        return field;
	}
	
	public static Object getFieldValue(final Object target, String obfName, String deobfName) {
		if (target == null) return null;
        
		Class<?> cls = target.getClass();
		Field field = null;
        for (Class<?> cls1 = cls; cls1 != null; cls1 = cls1.getSuperclass()) {
            try { field = cls1.getDeclaredField(obfName); } catch (Exception e) {}
			try { field = cls1.getDeclaredField(deobfName); } catch (Exception e) {}
			
			if (field == null) continue;
			
			if (!Modifier.isPublic(field.getModifiers())) field.setAccessible(true);
			try { return field.get(target); } catch (Exception e) {}
        }
        
        for (final Class<?> class1 : ClassUtils.getAllInterfaces(cls)) {
            try { field = class1.getField(obfName); } catch (Exception e) {}
			try { field = class1.getField(deobfName); } catch (Exception e) {}
			
			if (field != null) break;
        }
        try { return field.get(target); } catch (Exception e) { return null; }
	}
	
	public static void writeField(final Object target, final Object value, String obfName, String deobfName) {
		if (target == null) return;
		
		final Class<?> cls = target.getClass();
        final Field field = getField(cls, obfName, deobfName);
        
        if (field == null) return;
        
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        
        try {
			field.set(target, value);
		} catch (Exception e) {}
        
	}
	
	public static Object invokeMethod(Object target, String obfName, String deobfName, Object... args) {
		/* i just gave up here */
		Object o = null;
        try { o = MethodUtils.invokeMethod(target, true, obfName, args); } catch (Exception e) {
        	try { o = MethodUtils.invokeMethod(target, true, deobfName, args); } catch (Exception e1) {
        		System.err.println("Error reflecting method: " + deobfName + "/" + obfName);
        	}}
        return o;
    }
}
