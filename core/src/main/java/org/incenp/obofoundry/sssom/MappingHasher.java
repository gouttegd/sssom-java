/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2025 Damien Goutte-Gattat
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.incenp.obofoundry.sssom.model.Mapping;

/**
 * Creates deterministic hash values from mappings.
 */
public class MappingHasher {

    private final static long FNV64_PRIME = 1099511628211L;
    private final static long FNV64_OFFSET = -3750763034362895579L;

    // Z-Base32 output alphabet
    private static char[] ZB32 = { 'y', 'b', 'n', 'd', 'r', 'f', 'g', '8', 'e', 'j', 'k', 'm', 'c', 'p', 'q', 'x', 'o',
            't', '1', 'u', 'w', 'i', 's', 'z', 'a', '3', '4', '5', 'h', '7', '6', '9' };
    private MessageDigest md;

    /**
     * Creates a new instance.
     */
    public MappingHasher() {
        this(false);
    }

    /**
     * Creates a new instance that produces fast, FNV64-based hexadecimal-encoded
     * hash values.
     * 
     * @param useFNV If <code>true</code>, this instance will produce hash values
     *               using the FNV64 function, rather than the SHA2-256 function.
     */
    public MappingHasher(boolean useFNV) {
        if ( !useFNV) {
            try {
                md = MessageDigest.getInstance("SHA-256");
            } catch ( NoSuchAlgorithmException e ) { }
        }
    }

    /**
     * Hashes the given mapping using the SSSOM standard hash procedure.
     * 
     * @param mapping The mapping to hash.
     * @return The unique hash for the mapping.
     */
    public String hash(Mapping mapping) {
        if ( md != null ) {
            byte[] digest = md.digest(mapping.toSExpr().getBytes(StandardCharsets.UTF_8));
            md.reset();
            return toZBase32(digest);
        } else {
            // Either SHA2-256 is not available (which should likely never happen), or the
            // user specifically asked for FNV64 hashes.
            long hash = FNV64_OFFSET;
            for ( byte b : mapping.toSExpr().getBytes(StandardCharsets.UTF_8) ) {
                hash ^= b;
                hash *= FNV64_PRIME;
            }

            StringBuffer sb = new StringBuffer();
            for ( int i = 0; i < 8; i++ ) {
                int hi = (byte) ((hash >> (i * 8 + 4)) & 0x0F);
                int lo = (byte) ((hash >> (i * 8)) & 0x0F);
                sb.append((char) (hi >= 10 ? hi - 10 + 'A' : hi + '0'));
                sb.append((char) (lo >= 10 ? lo - 10 + 'A' : lo + '0'));
            }
            return sb.toString();
        }
    }

    /**
     * Encodes a buffer into its Z-Base32 string representation.
     * <p>
     * This method implements the Z-Base32 encoding as defined in
     * <a href="https://tools.ietf.org/html/rfc6189#section-5.1.6">RFC 6189
     * §5.1.6</a> and <a href=
     * "http://philzimmermann.com/docs/human-oriented-base-32-encoding.txt">human-oriented
     * base32 encoding</a>.
     * 
     * @param digest The input buffer to encode.
     * @return The Z-Base32-encoded string representation of the input buffer.
     */
    public static String toZBase32(byte[] digest) {
        StringBuffer sb = new StringBuffer();
        for ( int i = 0; i < digest.length; i += 5 ) {
            long val;
            int k;

            for ( val = k = 0; k < 5; k++ ) {
                if ( i + k < digest.length ) {
                    val |= (long) (digest[i + k] & 0xFF) << ((4 - k) * 8);
                }
            }

            sb.append(ZB32[(int) ((val & 0xF800000000L) >> 35L)]);
            sb.append(ZB32[(int) ((val & 0x7C0000000L) >> 30L)]);
            if ( i + 1 < digest.length ) {
                sb.append(ZB32[(int) ((val & 0x3E000000) >> 25)]);
                sb.append(ZB32[(int) ((val & 0x1F00000) >> 20)]);
            }
            if ( i + 2 < digest.length ) {
                sb.append(ZB32[(int) ((val & 0xF8000) >> 15)]);
            }
            if ( i + 3 < digest.length ) {
                sb.append(ZB32[(int) ((val & 0x7C00) >> 10)]);
                sb.append(ZB32[(int) ((val & 0x3E0) >> 5)]);
            }
            if ( i + 4 < digest.length ) {
                sb.append(ZB32[(int) (val & 0x1F)]);
            }
        }
        return sb.toString();
    }
}
