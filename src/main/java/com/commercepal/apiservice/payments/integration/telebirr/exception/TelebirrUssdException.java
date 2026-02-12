package com.commercepal.apiservice.payments.integration.telebirr.exception;

import java.io.Serial;
import lombok.Getter;

/**
 * Base exception for Telebirr USSD payment operations.
 *
 * @author CommercePal Team
 * @version 1.0
 * @since 2025-09-30
 */
@Getter
public class TelebirrUssdException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String errorCode;

    public TelebirrUssdException(String message) {
        super(message);
        this.errorCode = "TELEBIRR_ERROR";
    }

    public TelebirrUssdException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public TelebirrUssdException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "TELEBIRR_ERROR";
    }

    public TelebirrUssdException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

}

