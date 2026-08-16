package com.xinchuang.diagnosis.ledger.vo;

import java.util.Arrays;
import java.util.Objects;

public record LedgerDownload(String fileName, byte[] content) {
    public LedgerDownload {
        content = content == null ? new byte[0] : Arrays.copyOf(content, content.length);
    }

    @Override
    public byte[] content() {
        return Arrays.copyOf(content, content.length);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof LedgerDownload that)) {
            return false;
        }
        return Objects.equals(fileName, that.fileName) && Arrays.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hashCode(fileName) + Arrays.hashCode(content);
    }

    @Override
    public String toString() {
        return "LedgerDownload[fileName=" + fileName + ", contentLength=" + content.length + "]";
    }
}
