package org.jastadd.plugin.jastadd.debugger.attributes;

import java.util.Iterator;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;

/**
 * A class that creates an iterable around an instance of iterable within the debugger.
 * 
 * A call to AttributeUtils.isSubType("java.lang.Iterable", object.getJavaType().getName(), project) should
 * be made before invoking this class.
 * @author luke
 *
 */
public class DebugIterable implements Iterable<IJavaValue> {

	private final IJavaObject object;
	private final IJavaThread thread;
	
	public DebugIterable(IJavaObject object, IJavaThread thread) {
		this.object = object;
		this.thread = thread;
	}
	
	@Override
	public Iterator<IJavaValue> iterator() {
		IJavaObject iter = null;
		try {
			iter = (IJavaObject) object.sendMessage("iterator", "()Ljava/util/Iterator;" , new IJavaValue[0], thread, null);
		} catch (DebugException e) {
		}
		
		final IJavaObject iterator = iter;
		
		return new Iterator<IJavaValue>() {

			@Override
			public boolean hasNext() {
				if (iterator != null) {
					try {
						IJavaPrimitiveValue bool =  (IJavaPrimitiveValue) iterator.sendMessage("hasNext", "()Z" , new IJavaValue[0], thread, null);
						return bool.getBooleanValue();
					} catch (DebugException e) {
					}
				}
				return false;
			}

			@Override
			public IJavaValue next() {
				if (iterator != null) {
					try {
						return iterator.sendMessage("next", "()Ljava/lang/Object;" , new IJavaValue[0], thread, null);
					} catch (DebugException e) {
					}
				}
				return null;
			}

			@Override
			public void remove() {
				
			}
			
		};
	}
	
}
