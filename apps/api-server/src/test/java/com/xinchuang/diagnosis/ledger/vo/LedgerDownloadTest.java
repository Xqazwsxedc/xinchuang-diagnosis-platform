package com.xinchuang.diagnosis.ledger.vo;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class LedgerDownloadTest {
    @Test
    void comparesContentByValueAndDefensivelyCopiesBytes() {
        byte[] original = {1, 2, 3};
        LedgerDownload download = new LedgerDownload("ledger.csv", original);
        LedgerDownload sameDownload = new LedgerDownload("ledger.csv", new byte[] {1, 2, 3});

        original[0] = 9;
        byte[] exposedContent = download.content();
        exposedContent[1] = 9;

        assertEquals(download, sameDownload);
        assertEquals(download.hashCode(), sameDownload.hashCode());
        assertArrayEquals(new byte[] {1, 2, 3}, download.content());
        assertNotEquals(download, new LedgerDownload("other.csv", new byte[] {1, 2, 3}));
    }
}
