package org.bleachhack.eventbus;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.bleachhack.event.Event;

import com.google.common.util.concurrent.MoreExecutors;

public class BleachSubscriber {

	private final Consumer<Object> subscriberCaller;
	private Executor executor = MoreExecutors.directExecutor();
	private final Class<? extends Event> eventClass;
	private final Class<?> targetClass;
	private final String signature;

	public BleachSubscriber(Object target, String methodName, Class<? extends Event> eventClass) {
		this(target, MethodUtils.getAccessibleMethod(target.getClass(), methodName, eventClass), eventClass);
	}

	public BleachSubscriber(Object target, Method method) {
		this(target, method, getEvent(method));
	}

	public BleachSubscriber(Object target, Method method, Class<? extends Event> eventClass) {
		try {
			MethodHandles.Lookup lookup = MethodHandles.lookup();
			CallSite callsite = LambdaMetafactory.metafactory(lookup,
					"accept",
					MethodType.methodType(Consumer.class, target.getClass()),
					MethodType.methodType(void.class, Object.class),
					lookup.unreflect(method),
					//lookup.findVirtual(target.getClass(), methodName, MethodType.methodType(void.class, eventClass)),
					MethodType.methodType(void.class, eventClass));

			subscriberCaller = (Consumer<Object>) callsite.getTarget().invoke(target);

			this.eventClass = eventClass;
			this.targetClass = target.getClass();
			signature = target.getClass().getName() + "." + method.getName() + "(" + method.getParameters()[0].getType().getName() + ")";
		} catch (Throwable t) {
			// Yea, we got a problem
			throw new RuntimeException(t);
		}
	}

	@SuppressWarnings("unchecked")
	private static Class<? extends Event> getEvent(Method method) {
		Parameter[] parameters = method.getParameters();
		if (parameters.length == 0 || !Event.class.isAssignableFrom(parameters[0].getType())) {
			throw new RuntimeException("Tried to create Subscriber with invalid parameters");
		}

		return (Class<? extends Event>) parameters[0].getType();
	}

	public void callSubscriber(Event event) {
		executor.execute(() -> subscriberCaller.accept(event));
	}

	public Class<? extends Event> getEventClass() {
		return eventClass;
	}
	
	public Class<?> getTargetClass() {
		return targetClass;
	}

	public String getSignature() {
		return signature;
	}

	@Override
	public final int hashCode() {
		return signature.hashCode();
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj instanceof BleachSubscriber) {
			BleachSubscriber that = (BleachSubscriber) obj;
			return signature.equals(that.signature);
		}

		return false;
	}
}
