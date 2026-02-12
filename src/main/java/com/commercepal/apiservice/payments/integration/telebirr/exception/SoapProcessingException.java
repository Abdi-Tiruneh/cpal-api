package com.commercepal.apiservice.payments.integration.telebirr.exception;

import java.io.Serial;

/**
 * Exception thrown when SOAP request/response processing fails.
 * This includes XML parsing errors and transformation issues.
 *
 * @author CommercePal Team
 * @version 1.0
 * @since 2025-09-30
 */
public class SoapProcessingException extends TelebirrUssdException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SoapProcessingException(String message) {
        super(message, "SOAP_PROCESSING_ERROR");
    }

    public SoapProcessingException(String message, Throwable cause) {
        super(message, "SOAP_PROCESSING_ERROR", cause);
    }
}

