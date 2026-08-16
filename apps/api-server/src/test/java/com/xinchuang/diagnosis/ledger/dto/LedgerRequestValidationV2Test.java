package com.xinchuang.diagnosis.ledger.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LedgerRequestValidationV2Test {
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void businessSystemRejectsUnsupportedStatus() {
        BusinessSystemSaveRequest request = new BusinessSystemSaveRequest();
        request.setStatus("UNKNOWN");

        assertFalse(validator.validateProperty(request, "status").isEmpty());
    }

    @Test
    void businessSystemRejectsNonPositiveRelatedDatabaseId() {
        BusinessSystemSaveRequest request = new BusinessSystemSaveRequest();
        request.setDatabaseId(0L);

        assertFalse(validator.validateProperty(request, "databaseId").isEmpty());
    }

    @Test
    void databaseResourceRejectsInvalidPublicIp() {
        DbResourceSaveRequest request = new DbResourceSaveRequest();
        request.setPublicIp("999.1.1.1");

        assertFalse(validator.validateProperty(request, "publicIp").isEmpty());
    }

    @Test
    void metricQueryRejectsUnsupportedRange() {
        DbMetricRangeQuery query = new DbMetricRangeQuery();
        query.setRange("30D");

        assertFalse(validator.validateProperty(query, "range").isEmpty());
    }

    @Test
    void logQueryRejectsUnsupportedLogLevel() {
        DbInstanceLogPageQuery query = new DbInstanceLogPageQuery();
        query.setLogLevel("DEBUG");

        assertFalse(validator.validateProperty(query, "logLevel").isEmpty());
    }
}
