/*
 * @(#)ZipCoder.java
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package clime.messadmin.utils.compress.zip;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

import clime.messadmin.utils.Charsets;
import clime.messadmin.utils.backport.java.util.Arrays;

/**
 * Utility class for zipfile name and comment encoding
 */
final class ZipCoder {

	byte[] getBytes(String s) {
		CharsetEncoder ce = encoder().reset();
		char[] ca = s.toCharArray();
		int len = (int)(ca.length * ce.maxBytesPerChar());
		byte[] ba = new byte[len];
		if (len == 0) {
			return ba;
		}
//		// UTF-8 only for now. Other ArrayDecoder only handles
//		// CodingErrorAction.REPLACE mode.
//		if (isUTF8 && ce instanceof sun.nio.cs.ArrayEncoder) {
//			int blen = ((sun.nio.cs.ArrayEncoder)ce).encode(ca, 0, ca.length, ba);
//			if (blen == -1) {
//				throw new IllegalArgumentException("MALFORMED");
//			}
//			return Arrays.copyOf(ba, blen);
//		}
		ByteBuffer bb = ByteBuffer.wrap(ba);
		CharBuffer cb = CharBuffer.wrap(ca);
		CoderResult cr = ce.encode(cb, bb, true);
		if (!cr.isUnderflow()) {
			throw new IllegalArgumentException(cr.toString());
		}
		cr = ce.flush(bb);
		if (!cr.isUnderflow()) {
			throw new IllegalArgumentException(cr.toString());
		}
		if (bb.position() == ba.length) {
			return ba;
		} else {
			return Arrays.copyOf(ba, bb.position());
		}
	}

	boolean isUTF8() {
		return isUTF8;
	}

	private final Charset cs;
	private CharsetEncoder enc;
	private final boolean isUTF8;

	private ZipCoder(Charset cs) {
		this.cs = cs;
		this.isUTF8 = cs.name().equals(Charsets.UTF_8.name());
	}

	static ZipCoder get(Charset charset) {
		return new ZipCoder(charset);
	}

	private CharsetEncoder encoder() {
		if (enc == null) {
			enc = cs.newEncoder()
			  .onMalformedInput(CodingErrorAction.REPORT)
			  .onUnmappableCharacter(CodingErrorAction.REPORT);
		}
		return enc;
	}
}
