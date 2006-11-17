/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.osgi.internal.verifier;

public interface JarVerifierConstant {

	public static final String SHA1_STR = "SHA1"; //$NON-NLS-1$
	public static final String MD5_STR = "MD5"; //$NON-NLS-1$
	public static final String MD2_STR = "MD2"; //$NON-NLS-1$

	public static final String DOT_DSA = ".DSA"; //$NON-NLS-1$
	public static final String DOT_RSA = ".RSA"; //$NON-NLS-1$
	public static final String DOT_SF = ".SF"; //$NON-NLS-1$
	public static final String META_INF = "META-INF/"; //$NON-NLS-1$
	public static final String META_INF_MANIFEST_MF = "META-INF/MANIFEST.MF"; //$NON-NLS-1$
	public static final String[] EMPTY_STRING = new String[0];

	//
	// following are variables and methods to cache the entries related data
	// for a given MF file
	//
	public static final String MF_ENTRY_NEWLN_NAME = "\nName: "; //$NON-NLS-1$
	public static final String MF_ENTRY_NAME = "Name: "; //$NON-NLS-1$
	public static final String MF_DIGEST_PART = "-Digest: "; //$NON-NLS-1$
	public static final String digestManifestSearch = "-Digest-Manifest: "; //$NON-NLS-1$
	public static final int digestManifestSearchLen = digestManifestSearch.length();

	public static final int SIGNEDDATA_OID[] = {1, 2, 840, 113549, 1, 7, 2};
	public static final int MD5_OID[] = {1, 2, 840, 113549, 2, 5};
	public static final int MD2_OID[] = {1, 2, 840, 113549, 2, 2};
	public static final int SHA1_OID[] = {1, 3, 14, 3, 2, 26};
	public static final int DSA_OID[] = {1, 2, 840, 10040, 4, 1};
	public static final int RSA_OID[] = {1, 2, 840, 113549, 1, 1, 1};
	public static final int TIMESTAMP_OID[] = {1, 2, 840, 113549, 1, 9, 16, 2, 14};
	public static final int SIGNING_TIME[] = {1, 2, 840, 113549, 1, 9, 5};

	// constant for certs chain trust service
	public static final String TRUST_AUTHORITY = "osgi.certificate.trust.authority"; //$NON-NLS-1$
	public static final Object DEFAULT_TRUST_AUTHORITY = "org.eclipse.osgi"; //$NON-NLS-1$
}
