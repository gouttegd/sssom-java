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
    private HashEncoding encoding;

    /**
     * Creates a new instance that will produce the standard hash defined by the
     * SSSOM specification.
     * <p>
     * The definition of the “SSSOM standard hash” is still under work. For now, it
     * is a ZBase32-encoded SHA2-256 hash of the canonical S-expression that
     * represents a mapping record. This may change in the future.
     */
    public MappingHasher() {
        this(HashFunction.SHA2_256, HashEncoding.ZBASE32);
    }

    /**
     * Creates a new instance that will produce an “alternative” type of hash.
     * <p>
     * This constructor is mostly intended for testing purposes, until a decision is
     * reached amongst SSSOM developers about what the “standard SSSOM hash” should
     * be.
     * 
     * @param altHash If <code>true</code>, this instance will produce an
     *                “alternative” hash. Otherwise, it will produce the same
     *                standard hash as {@link #MappingHasher()}.
     */
    public MappingHasher(boolean altHash) {
        this(altHash ? HashFunction.FNV64 : HashFunction.SHA2_256, altHash ? HashEncoding.BASE16 : HashEncoding.ZBASE32);
    }

    /**
     * Creates a new instance will full control over the production of the hash.
     * <p>
     * As for {@link #MappingHasher(boolean)}, this constructor is mostly intended
     * for development and testing purposes, while the details about the “SSSOM
     * standard hash” are being finalised. It may be removed after that.
     * 
     * @param function The hash function to use.
     * @param encoding The encoding to use to encode the output of the hash
     *                 function.
     */
    public MappingHasher(HashFunction function, HashEncoding encoding) {
        if ( function.algName != null ) {
            try {
                md = MessageDigest.getInstance(function.algName);
            } catch ( NoSuchAlgorithmException e ) {
            }
        }
        this.encoding = encoding;
    }

    /**
     * Hashes the given mapping.
     * 
     * @param mapping The mapping to hash.
     * @return The unique hash for the mapping.
     */
    public String hash(Mapping mapping) {
        byte[] input = mapping.toSExpr().getBytes(StandardCharsets.UTF_8);
        byte[] digest;

        if ( md != null ) {
            digest = md.digest(input);
            md.reset();
        } else {
            digest = fnv64(input);
        }

        return encoding == HashEncoding.ZBASE32 ? toZBase32(digest) : toHexadecimal(digest);
    }

    /**
     * Generates a FNV64 hash.
     * <p>
     * This method implements the 64-bit variant of the FNV-1a hash function as
     * defined in <a href="https://www.rfc-editor.org/rfc/rfc9923.html">RFC
     * 9923</a>.
     * 
     * @param input The data to hash.
     * @return The resulting hash value, as an array of bytes in little endian
     *         order.
     */
    public static byte[] fnv64(byte[] input) {
        long hash = FNV64_OFFSET;
        for ( byte b : input ) {
            hash ^= b;
            hash *= FNV64_PRIME;
        }

        byte[] digest = new byte[8];
        for ( int i = 0; i < 8; i++ ) {
            digest[i] = (byte) ((hash >> (i * 8)) & 0xFF);
        }
        return digest;
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

    /**
     * Encodes a buffer into its hexadecimal representation.
     * <p>
     * This method implements the Base16 encoding as defined in
     * <a href="https://datatracker.ietf.org/doc/html/rfc4648#section-8">RFC
     * 4648</a>.
     * 
     * @param digest The input buffer to encode.
     * @return The hexadecimal representation of the input buffer.
     */
    public static String toHexadecimal(byte[] digest) {
        StringBuffer sb = new StringBuffer();
        for ( int i = 0; i < digest.length; i++ ) {
            int hi = (digest[i] & 0xF0) >> 4;
            int lo = digest[i] & 0x0F;

            sb.append((char) (hi >= 10 ? hi - 10 + 'A' : hi + '0'));
            sb.append((char) (lo >= 10 ? lo - 10 + 'A' : lo + '0'));
        }
        return sb.toString();
    }

    /**
     * The hash function to use to hash the canonical S-expression of a mapping
     * record.
     */
    public enum HashFunction {
        SHA2_256("SHA-256"),
        FNV64(null);

        String algName;

        HashFunction(String algName) {
            this.algName = algName;
        }
    }

    /**
     * The encoding to use to transform the array of bytes produced by the hash
     * function into a printable string.
     */
    public enum HashEncoding {
        ZBASE32,
        BASE16
    }
}
