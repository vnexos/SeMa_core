package com.vnexos.sema.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.UUID;

/**
 * Implements all UUID version generators.
 * 
 * @author Trần Việt Đăng Quang
 */
public class UUIDUtil {
  private static final UUID DEFAULT_NAMESPACE = UUID.fromString("e7448193-a8dc-4e2e-8815-ecbc304fa857");
  public static final UUID EMPTY = UUID.fromString("00000000-0000-0000-0000-000000000000");

  /**
   * Converts UUID to byte map.
   * 
   * @param uuid the UUID value
   * @return the byte array
   */
  private static byte[] toBytes(UUID uuid) {
    byte[] bytes = new byte[16];
    long msb = uuid.getMostSignificantBits();
    long lsb = uuid.getLeastSignificantBits();

    for (int i = 0; i < 8; i++) {
      bytes[i] = (byte) (msb >>> (8 * (7 - i)));
    }
    for (int i = 8; i < 16; i++) {
      bytes[i] = (byte) (lsb >>> (8 * (15 - i)));
    }

    return bytes;
  }

  /**
   * Convert byte array to UUID.
   * 
   * @param data the array of byte
   * @return value of UUID
   */
  private static UUID fromBytes(byte[] data) {
    long msb = 0;
    long lsb = 0;

    for (int i = 0; i < 8; i++) {
      msb = (msb << 8) | (data[i] & 0xff);
    }
    for (int i = 8; i < 16; i++) {
      lsb = (lsb << 8) | (data[i] & 0xff);
    }

    return new UUID(msb, lsb);
  }

  /**
   * Generates UUID version 1.
   * 
   * <p>
   * UUID version 1 is a time-based UUID.
   * 
   * @return generated UUID v1
   */
  public static UUID v1() {
    long time = Instant.now().toEpochMilli();
    long mostSigBits = (time & 0xFFFFFFFFFFFF0000L) << 16;
    mostSigBits |= 0x0000000000001000L;

    long leastSigBits = UUID.randomUUID().getLeastSignificantBits();
    leastSigBits &= 0x3FFFFFFFFFFFFFFFL;
    leastSigBits |= 0x8000000000000000L;

    return new UUID(mostSigBits, leastSigBits);
  }

  /**
   * Generates UUID version 2.
   * 
   * <p>
   * UUID version 2 is a DCE Security UUID. This UUID is same as UUIDv1 but POSIX
   * UID/GID information.
   * 
   * @return generated UUID v2
   */
  public static UUID v2() {
    return v1();
  }

  /**
   * Generates UUID version 3.
   * 
   * <p>
   * UUID version 3 is a name-based (MD5 hash).
   * 
   * @param namespace the namespace identifier
   * @param name      the name to generate UUID
   * @return generated UUID v3
   */
  public static UUID v3(UUID namespace, String name) {
    try {
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      md5.update(toBytes(namespace));
      md5.update(name.getBytes(StandardCharsets.UTF_8));
      byte[] hash = md5.digest();

      hash[6] &= 0x0f;
      hash[6] |= 0x30; // version 3
      hash[8] &= 0x3f;
      hash[8] |= 0x80;

      return fromBytes(hash);
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate UUIDv3", e);
    }
  }

  /**
   * Generates UUID version 3.
   * 
   * <p>
   * UUID version 3 is a name-based (MD5 hash).
   * 
   * @param name the name to generate UUID
   * @return generated UUID v3
   */
  public static UUID v3(String name) {
    return v3(DEFAULT_NAMESPACE, name);
  }

  /**
   * Generates UUID version 4.
   * 
   * <p>
   * UUID version 4 is a random UUID.
   * 
   * @return generated UUID v4
   */
  public static UUID v4() {
    return UUID.randomUUID();
  }

  /**
   * Generates UUID version 5.
   * 
   * <p>
   * UUID version 5 is a name-based (SHA-1 hash).
   * 
   * @param namespace the namespace identifier
   * @param name      the name to generate UUID
   * @return generated UUID v5
   */
  public static UUID v5(UUID namespace, String name) {
    try {
      MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
      sha1.update(toBytes(namespace));
      sha1.update(name.getBytes(StandardCharsets.UTF_8));
      byte[] hash = sha1.digest();

      hash[6] &= 0x0f;
      hash[6] |= 0x50; // version 5
      hash[8] &= 0x3f;
      hash[8] |= 0x80;

      return fromBytes(hash);
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate UUIDv5", e);
    }
  }

  /**
   * Generates UUID version 5.
   * 
   * <p>
   * UUID version 5 is a name-based (MD5 hash).
   * 
   * @param name the name to generate UUID
   * @return generated UUID v5
   */
  public static UUID v5(String name) {
    return v5(DEFAULT_NAMESPACE, name);
  }

  /**
   * Generates UUID version 6.
   * 
   * <p>
   * UUID version 6 is a reordered time-based.
   * 
   * @return generated UUID v6
   */
  public static UUID v6() {
    long timestamp = Instant.now().toEpochMilli();
    long timeHigh = (timestamp >>> 32) & 0xFFFF_FFFFL;
    long timeMid = (timestamp >>> 16) & 0xFFFF;
    long timeLow = timestamp & 0xFFFF;

    long mostSigBits = (timeHigh << 32) | (timeMid << 16) | timeLow;
    mostSigBits &= ~(0xF000L << 48); // clear version bits
    mostSigBits |= 0x6000L << 48; // set version 6

    long leastSigBits = UUID.randomUUID().getLeastSignificantBits();
    leastSigBits &= 0x3FFFFFFFFFFFFFFFL;
    leastSigBits |= 0x8000000000000000L;

    return new UUID(mostSigBits, leastSigBits);
  }
}
