package com.commercepal.apiservice.payments.integration.telebirr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelebirrUssdPaymentResponse {

  /**
   * Originator conversation ID (or "-1" in case of exception).
   */
  @Builder.Default
  private String originatorConversationId = "-1";

  /**
   * Conversation ID (or "-1" in case of exception).
   */
  @Builder.Default
  private String conversationId = "-1";

  /**
   * Response code from the SOAP service (0 = success).
   */
  private String responseCode;

  /**
   * Service status from the SOAP service (0 = success).
   */
  private String serviceStatus;
}


