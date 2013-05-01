/*
 * @(#)ZipConstants64.java
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package clime.messadmin.utils.compress.zip;

/*
 * This class defines the constants that are used by the classes
 * which manipulate Zip64 files.
 */
interface ZipConstants64 {

	/*
	 * ZIP64 constants
	 */
	static final long ZIP64_ENDSIG = 0x06064b50L;  // "PK\006\006"
	static final long ZIP64_LOCSIG = 0x07064b50L;  // "PK\006\007"
	static final int  ZIP64_ENDHDR = 56;           // ZIP64 end header size
	static final int  ZIP64_LOCHDR = 20;           // ZIP64 end loc header size
	static final int  ZIP64_EXTHDR = 24;           // EXT header size
	static final int  ZIP64_EXTID  = 0x0001;       // Extra field Zip64 header ID

	static final int  ZIP64_MAGICCOUNT = 0xFFFF;
	static final long ZIP64_MAGICVAL = 0xFFFFFFFFL;

	/*
	 * Language encoding flag EFS
	 */
	static final int EFS = 0x800;       // If this bit is set the filename and
	                                    // comment fields for this file must be
	                                    // encoded using UTF-8.
}
