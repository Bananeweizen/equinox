/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.metatype.tests;

import org.osgi.framework.Bundle;
import org.osgi.service.metatype.*;

public class Bug340899Test extends AbstractTest {
	private static final String ad1description = "This is the description of the attribute definition."; //$NON-NLS-1$
	private static final String ad1name = "AD1 Name"; //$NON-NLS-1$
	private static final String ocd1description = "This is the description of the object class definition."; //$NON-NLS-1$
	private static final String ocd1name = "OCD1 Name"; //$NON-NLS-1$

	/*
	 * When not overridden by either the <MetaData> localization attribute or
	 * the Bundle-Localization manifest header, the default property file base
	 * name of 'OSGI-INF/l10n/bundle' should be used.
	 */
	public void test1() throws Exception {
		execute("tb5", "org.eclipse.equinox.metatype.tests.tb5"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * When not overridden by the <MetaData> localization attribute, the 
	 * property file base name declared within the Bundle-Localization manifest
	 * header should override the default base name.
	 */
	public void test2() throws Exception {
		execute("tb6", "org.eclipse.equinox.metatype.tests.tb6"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * When specified, the property file base name declared within the 
	 * <MetaData> localization attribute should override the 
	 * Bundle-Localization manifest header and the default base name.
	 */
	public void test3() throws Exception {
		execute("tb7", "org.eclipse.equinox.metatype.tests.tb7"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	private void execute(String bundleName, String ocdId) throws Exception {
		Bundle bundle = bundleInstaller.installBundle(bundleName); //$NON-NLS-1$
		try {
			bundle.start();
			MetaTypeInformation mti = metatype.getMetaTypeInformation(bundle);
			assertNotNull("Metatype information not found", mti); //$NON-NLS-1$
			ObjectClassDefinition ocd = mti.getObjectClassDefinition(ocdId, null); //$NON-NLS-1$
			assertNotNull("Object class definition not found", ocd); //$NON-NLS-1$
			assertEquals("Wrong OCD name", ocd1name, ocd.getName()); //$NON-NLS-1$
			assertEquals("Wrong OCD description", ocd1description, ocd.getDescription()); //$NON-NLS-1$
			AttributeDefinition[] ads = ocd.getAttributeDefinitions(ObjectClassDefinition.ALL);
			assertNotNull("Attribute definitions not found", ads); //$NON-NLS-1$
			assertEquals("Wrong number of attribute definitions", 1, ads.length); //$NON-NLS-1$
			AttributeDefinition ad = findAttributeDefinitionById("ad1", ads); //$NON-NLS-1$
			assertNotNull("Attribute definition not found", ad); //$NON-NLS-1$
			assertEquals("Wrong AD name", ad1name, ad.getName()); //$NON-NLS-1$
			assertEquals("Wrong AD description", ad1description, ad.getDescription()); //$NON-NLS-1$
		} finally {
			try {
				bundle.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
