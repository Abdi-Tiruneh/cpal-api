package com.commercepal.apiservice.payments.integration.telebirr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelebirrUssdCallbackRequest {

  /**
   * Originator conversation ID (your original request ID)
   */
  private String cpalTransactionRef;

  /**
   * Conversation ID from Telebirr
   */
  private String conversationId;

  /**
   * Result type (0 = success)
   */
  private String resultType;

  /**
   * Result code (0 = success)
   */
  private String resultCode;

  /**
   * Result description
   */
  private String resultDesc;

  /**
   * Transaction ID from Telebirr
   */
  private String transactionId;

  private Boolean isSuccess;

  @Override
  public String toString() {
    return "TelebirrUssdCallbackRequest {" +
        "\n   CPAL TransactionRef : '" + cpalTransactionRef + '\'' +
        "\n   ConversationID      : '" + conversationId + '\'' +
        "\n   ResultType          : '" + resultType + '\'' +
        "\n   ResultCode          : '" + resultCode + '\'' +
        "\n   ResultDesc          : '" + resultDesc + '\'' +
        "\n   TransactionID       : '" + transactionId + '\'' +
        "\n   IsSuccess           : " + isSuccess +
        "\n}";
  }


}

