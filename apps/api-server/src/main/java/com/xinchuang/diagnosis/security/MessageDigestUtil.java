package com.xinchuang.diagnosis.security;

import java.security.MessageDigest;

public final class MessageDigestUtil {
    private MessageDigestUtil() {
    }

    public static boolean equals(byte[] left, byte[] right) {
        return MessageDigest.isEqual(left, right);
    }
}
