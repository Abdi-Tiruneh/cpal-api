package com.commercepal.apiservice.payments.integration.telebirr.exception;

import java.io.Serial;

/**
 * Exception thrown when SOAP communication fails.
 * This includes network errors, timeouts, and connection issues.
 *
 * @author CommercePal Team
 * @version 1.0
 * @since 2025-09-30
 */
public class SoapCommunicationException extends TelebirrUssdException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SoapCommunicationException(String message) {
        super(message, "SOAP_COMMUNICATION_ERROR");
    }

    public SoapCommunicationException(String message, Throwable cause) {
        super(message, "SOAP_COMMUNICATION_ERROR", cause);
    }
}

