/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.ds.instance;

import java.lang.reflect.*;
import org.eclipse.equinox.ds.Log;
import org.eclipse.equinox.ds.model.ComponentDescription;
import org.eclipse.equinox.ds.model.ComponentConfiguration;
import org.eclipse.equinox.ds.resolver.Reference;
import org.eclipse.equinox.ds.service.ComponentInstanceImpl;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.log.LogService;

/**
 * Invoke a method on a Service Component implementation class instance:
 * activate, deactivate, bind or unbind
 * 
 * @version $Revision: 1.1 $
 */

class InvokeMethod {

	private BuildDispose buildDispose;

	InvokeMethod(BuildDispose buildDispose) {
		this.buildDispose = buildDispose;
	}

	/**
	 * Invoke the activate method of the Service Component if one exists
	 * 
	 * @param instance The instance of the component
	 * @param context The component context
	 */
	void activateComponent(Object instance, ComponentContext context) throws IllegalAccessException, InvocationTargetException {

		//cache activate method
		ComponentDescription cd = ((ComponentInstanceImpl) context.getComponentInstance()).getComponentConfiguration().getComponentDescription();
		Method method = cd.getActivateMethod();
		if (method == null && !cd.isActivateMethodInitialized()) {
			method = findActivateOrDeactivateMethod("activate", instance.getClass());
			//also sets cd.activateMethodInitialized flag
			cd.setActivateMethod(method);
		}

		// Create an array of parameters to pass to the method
		// The activate method requires the ComponentContext
		Object[] parameterTypes = new Object[] {context};

		if (method != null) {
			invokeMethod(method, instance, parameterTypes);
		}
	}

	/**
	 * Invoke the deactivate method of the Service Component if one exists
	 * 
	 * @param instance The instance of the component
	 * @param context The component context
	 */
	void deactivateComponent(Object instance, ComponentContext context) throws IllegalAccessException, InvocationTargetException {

		//cache deactivate method
		ComponentDescription cd = ((ComponentInstanceImpl) context.getComponentInstance()).getComponentConfiguration().getComponentDescription();
		Method method = cd.getDeactivateMethod();
		if (method == null && !cd.isDeactivateMethodInitialized()) {
			method = findActivateOrDeactivateMethod("deactivate", instance.getClass());
			//also sets cd.deactivateMethodInitialized flag
			cd.setDeactivateMethod(method);
		}

		// Create an array of parameters to pass to the method
		// The deactivate method requires the ComponentContext
		Object[] parameterTypes = new Object[] {context};

		if (method != null) {
			invokeMethod(method, instance, parameterTypes);
		}
	}

	/**
	 * Invoke a bind method of a Service Component
	 * 
	 * @param bind
	 * @param instance
	 * @param param service object or {@link ServiceReference}
	 * 
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	void bindComponent(Method bindMethod, Object instance, Object param) throws IllegalAccessException, InvocationTargetException {
		// Create an array of parameters to pass to the method
		Object[] parameterTypes = new Object[] {param};
		invokeMethod(bindMethod, instance, parameterTypes);
	}

	/**
	 * unbind method of the Service Component
	 * 
	 * @param unbind
	 * @param instance
	 * @param param service object or {@link ServiceReference}
	 * 
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	void unbindComponent(Method unbindMethod, Object instance, Object param) throws IllegalAccessException, InvocationTargetException {
		// Create an array of parameters to pass to the method
		Object[] parameterTypes = new Object[] {param};
		invokeMethod(unbindMethod, instance, parameterTypes);
	}

	/**
	 * invokeMethod - invoke a Method on the Service Compoent via reflection
	 * 
	 * @param method- the method name to invoke
	 * @param instance - instance to invoke method on
	 * @param parameterTypes - array of parameters to pass to the method
	 * 
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */

	private void invokeMethod(Method method, Object instance, Object[] parameterTypes) throws IllegalAccessException, InvocationTargetException {

		// If the method is declared protected or public, SCR will call the
		// method
		int mod = method.getModifiers();
		if ((Modifier.isProtected(mod)) || (Modifier.isPublic(mod))) {
			// if the method is protected must set accessibility(true) to invoke
			// it
			if (Modifier.isProtected(mod))
				method.setAccessible(true);
			// invoke the method
			method.invoke(instance, parameterTypes);
		}
	}

	private static final Class[] PARAM_COMPONENTCONTEXT = new Class[] {ComponentContext.class};
	private static final Class[] PARAM_SERVICEREFERENCE = new Class[] {ServiceReference.class};

	/**
	 * Search through class and the superclasses for an activate or deactivate
	 * method.
	 * 
	 * If method if found but not public or protected, log an error and return null
	 * 
	 * @param methodName name of method to look for
	 * @param consumerClass Object to look in
	 * @return method or null if not found
	 */
	private Method findActivateOrDeactivateMethod(String methodName, Class consumerClass) {
		Method method = null;
		while (!consumerClass.equals(java.lang.Object.class) && method == null) {

			// search this class' methods
			try {
				method = consumerClass.getDeclaredMethod(methodName, PARAM_COMPONENTCONTEXT);
			} catch (NoSuchMethodException e) {
				// we'll try the superclass
			}
			if (method != null)
				break;

			// we couldn't find the method - try the superclass
			consumerClass = consumerClass.getSuperclass();
		}

		// if method is not protected or public, log error message
		if (method != null) {
			int modifier = method.getModifiers();
			if (!(Modifier.isProtected(modifier) || Modifier.isPublic(modifier))) {
				// log error
				Log.log(LogService.LOG_ERROR, "[SCR] Method " + methodName + " is not protected or public.");
				method = null;
			}
		}

		return method;
	}

	/**
	 * Search through class and the superclasses for a bind or unbind method.
	 * 
	 * See OSGi R4 Specification section 112.3.1 "Accessing Services" for an 
	 * explanation of the method search algorithm used for bind and unbind
	 * methods.
	 * 
	 * Searching for the bind or unbind method may require a service object.  If
	 * the object has not already been acquired, this method may call 
	 * {@link BuildDispose#getService(ComponentConfiguration, Reference, ServiceReference)} 
	 * to get it.
	 * 
	 * If method can not be found we log an error and return null.
	 * 
	 * @param componentInstance Object to look in
	 * @param reference Reference object
	 * @param serviceReference
	 * @param methodName name of method to look for
	 * 
	 * @return the method or null if no method was found
	 * 
	 */
	Method findBindOrUnbindMethod(ComponentInstanceImpl componentInstance, Reference reference, ServiceReference serviceReference, String methodName) {
		Class consumerClass = componentInstance.getInstance().getClass();
		Object serviceObject = null;
		Class serviceObjectClass = null;
		Class interfaceClass = null;
		Class[] param_interfaceClass = null;
		Method method = null;
		while (consumerClass != null) {

			// search this class' methods
			// look for various forms of bind methods

			// 1) check for bind(ServiceReference) method
			try {
				method = consumerClass.getDeclaredMethod(methodName, PARAM_SERVICEREFERENCE);
			} catch (NoSuchMethodException e) {
			}
			if (method != null)
				break;

			// we need a serviceObject to keep looking, create one if necessary
			if (serviceObject == null) {
				serviceObject = reference.getServiceObject(serviceReference);
				if (serviceObject == null) {
					serviceObject = buildDispose.getService(reference, serviceReference);
				}
				if (serviceObject == null) {
					// we could not create a serviceObject because of
					// circularity
					return null;
				}
				reference.addServiceReference(serviceReference, serviceObject);
				serviceObjectClass = serviceObject.getClass();

				// figure out the interface class - this is guaranteed to
				// succeed or else
				// the framework would not have let us have the service object
				Class searchForInterfaceClass = serviceObjectClass;
				String interfaceName = reference.getReferenceDescription().getInterfacename();
				while (searchForInterfaceClass != null) {
					// first look through interfaces
					Class[] interfaceClasses = searchForInterfaceClass.getInterfaces();
					for (int i = 0; i < interfaceClasses.length; i++) {
						if (interfaceClasses[i].getName().equals(interfaceName)) {
							interfaceClass = interfaceClasses[i];
							break;
						}
					}
					if (interfaceClass != null) {
						break;
					}

					// also check the class itself
					if (searchForInterfaceClass.getName().equals(interfaceName)) {
						interfaceClass = searchForInterfaceClass;
						break;
					}

					// advance up the superclasses
					searchForInterfaceClass = searchForInterfaceClass.getSuperclass();
				}

				param_interfaceClass = new Class[] {interfaceClass};

			} // end if(serviceObject == null)

			// 2) check for bind(Service interface) method
			try {
				method = consumerClass.getDeclaredMethod(methodName, param_interfaceClass);
			} catch (NoSuchMethodException e) {
			}
			if (method != null)
				break;

			// 3) check for bind(class.isAssignableFrom(serviceObjectClass))
			// method
			Method[] methods = consumerClass.getDeclaredMethods();
			for (int i = 0; i < methods.length; i++) {
				Class[] params = methods[i].getParameterTypes();
				if (params.length == 1 && methods[i].getName().equals(methodName) && params[0].isAssignableFrom(serviceObjectClass)) {

					method = methods[i];
					break;
				}
			}
			if (method != null)
				break;

			// we couldn't find the method - try the superclass
			consumerClass = consumerClass.getSuperclass();
		}

		if (method == null) {
			// log error = we could not find the method
			Log.log(LogService.LOG_ERROR, "[SCR] Could not find method " + methodName + ".");
			return null;
		}

		// if method is not protected or public, log error message
		int modifier = method.getModifiers();
		if (!(Modifier.isProtected(modifier) || Modifier.isPublic(modifier))) {
			// log error
			Log.log(LogService.LOG_ERROR, "[SCR] Method " + methodName + " is not protected or public.");
			return null;
		}

		return method;
	}

}
