package com.commercepal.apiservice.payments.integration.telebirr;

import com.commercepal.apiservice.orders.enums.PaymentStatus;
import com.commercepal.apiservice.payments.core.ProcessSuccessPayment;
import com.commercepal.apiservice.payments.integration.telebirr.config.TelebirrUssdProperties;
import com.commercepal.apiservice.payments.integration.telebirr.dto.TelebirrUssdCallbackRequest;
import com.commercepal.apiservice.payments.integration.telebirr.dto.TelebirrUssdPaymentResponse;
import com.commercepal.apiservice.payments.integration.telebirr.exception.SoapCommunicationException;
import com.commercepal.apiservice.payments.integration.telebirr.exception.SoapProcessingException;
import com.commercepal.apiservice.payments.oderPayment.OrderPayment;
import com.commercepal.apiservice.payments.oderPayment.OrderPaymentRepository;
import com.commercepal.apiservice.payments.oderPayment.enums.NextAction;
import com.commercepal.apiservice.payments.oderPayment.dto.PaymentInitiationResponse;
import com.commercepal.apiservice.utils.ResponseCodes;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.xml.transform.StringSource;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelebirrUssdClient {

  private final WebServiceTemplate webServiceTemplate;
  private final TelebirrUssdProperties telebirrUssdProperties;
  private final OrderPaymentRepository orderPaymentRepository;
  private final ProcessSuccessPayment processSuccessPayment;

  private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(
      "yyyyMMddHHmmss");
  private static final String SOAP_NAMESPACE_ENVELOPE = "http://schemas.xmlsoap.org/soap/envelope/";
  private static final String SOAP_NAMESPACE_API = "http://cps.huawei.com/cpsinterface/api_requestmgr";
  private static final String SOAP_NAMESPACE_REQ = "http://cps.huawei.com/cpsinterface/request";
  private static final String SOAP_NAMESPACE_COM = "http://cps.huawei.com/cpsinterface/common";

  /**
   * Initiates a buy goods payment request.
   *
   * @param payment Payment request containing customer phone and amount
   * @return Structured payment response
   * @throws SoapCommunicationException if SOAP communication fails
   * @throws SoapProcessingException    if XML processing fails
   */
  public PaymentInitiationResponse initiatePayment(OrderPayment payment) {
    String originatorConversationId = payment.getReference();
    String timestamp = getCurrentTimestamp();

    log.info("Initiating Telebirr USSD payment | TelebirrUssdClient | initiatePayment | reference={}, phone={}, amount={}",
        originatorConversationId, payment.getAccountNumber(), payment.getAmount());

    try {
      String soapRequest = buildSoapRequest(originatorConversationId, timestamp,
          payment.getAccountNumber(),
          payment.getAmount().toString());

      payment.recordInitRequest(soapRequest);
      orderPaymentRepository.save(payment);

      log.debug("SOAP Request prepared | TelebirrUssdClient | initiatePayment | reference={}",
          originatorConversationId);

      String soapResponse = sendSoapRequest(soapRequest);

      log.info("SOAP Response received | TelebirrUssdClient | initiatePayment | reference={}",
          originatorConversationId);
      log.trace("SOAP Response content | TelebirrUssdClient | initiatePayment | reference={}, response={}",
          originatorConversationId, soapResponse);

      payment.recordInitResponse(soapResponse);
      orderPaymentRepository.save(payment);

      TelebirrUssdPaymentResponse response = parseSoapResponse(soapResponse);

      if ("0".equalsIgnoreCase(response.getResponseCode()) && "0".equalsIgnoreCase(
          response.getServiceStatus())) {
        payment.setGatewayReference(response.getConversationId());
        payment.resolve(PaymentStatus.PENDING, "PENDING");
        orderPaymentRepository.save(payment);

        log.info("Payment initiated successfully | TelebirrUssdClient | initiatePayment | reference={}, conversationId={}",
            originatorConversationId, response.getConversationId());

        return PaymentInitiationResponse.builder()
            .success(true)
            .orderNumber(payment.getOrder() != null ? payment.getOrder().getOrderNumber() : null)
            .paymentReference(payment.getReference())
            .paymentUrl(null)
            .paymentInstructions("Please complete the payment on your phone")
            .nextAction(NextAction.OPEN_ADDITIONAL_INPUT)
            .build();

      } else {
        String errorMessage = String.format("ResponseCode: %s, ServiceStatus: %s",
            response.getResponseCode(), response.getServiceStatus());
        log.error("Payment initiation failed | TelebirrUssdClient | initiatePayment | reference={}, error={}",
            originatorConversationId, errorMessage);

        payment.setGatewayReference("FAILED");
        payment.resolve(PaymentStatus.FAILED, "FAILED");
        orderPaymentRepository.save(payment);

        return PaymentInitiationResponse.builder()
            .success(false)
            .orderNumber(payment.getOrder() != null ? payment.getOrder().getOrderNumber() : null)
            .paymentReference(payment.getReference())
            .paymentUrl(null)
            .paymentInstructions("Payment initiation failed. Please try again.")
            .nextAction(NextAction.OPEN_ADDITIONAL_INPUT)
            .build();
      }
    } catch (SoapCommunicationException | SoapProcessingException e) {
      log.error("Payment initiation failed | TelebirrUssdClient | initiatePayment | reference={}, error={}",
          originatorConversationId, e.getMessage(), e);
      payment.resolve(PaymentStatus.FAILED, e.getMessage());
      orderPaymentRepository.save(payment);

      return PaymentInitiationResponse.builder()
          .success(false)
          .orderNumber(payment.getOrder() != null ? payment.getOrder().getOrderNumber() : null)
          .paymentReference(payment.getReference())
          .paymentUrl(null)
          .paymentInstructions("Payment initiation failed. Please try again.")
          .nextAction(NextAction.OPEN_ADDITIONAL_INPUT)
          .build();
    } catch (Exception e) {
      log.error("Unexpected error during payment initiation | TelebirrUssdClient | initiatePayment | reference={}, error={}",
          originatorConversationId, e.getMessage(), e);
      payment.resolve(PaymentStatus.FAILED, e.getMessage());
      orderPaymentRepository.save(payment);

      return PaymentInitiationResponse.builder()
          .success(false)
          .orderNumber(payment.getOrder() != null ? payment.getOrder().getOrderNumber() : null)
          .paymentReference(payment.getReference())
          .paymentUrl(null)
          .paymentInstructions("Unexpected error during payment processing. Please try again.")
          .nextAction(NextAction.OPEN_ADDITIONAL_INPUT)
          .build();
    }
  }

  private String buildSoapRequest(String originatorConversationId, String timestamp,
      String primaryIdentifier, String amount) {
    // DO NOT create the <soapenv:Envelope> and <soapenv:Body> here.
    // Start directly with the content that goes inside the body.
    return String.format(
        "<api:Request xmlns:api=\"%s\" xmlns:req=\"%s\" xmlns:com=\"%s\">" + "  <req:Header>"
            + "    <req:Version>1.0</req:Version>" + "    <req:CommandID>%s</req:CommandID>"
            + "    <req:OriginatorConversationID>%s</req:OriginatorConversationID>"
            + "    <req:Caller>" + "      <req:CallerType>%d</req:CallerType>"
            + "      <req:ThirdPartyID>%s</req:ThirdPartyID>"
            + "      <req:Password>%s</req:Password>" + "      <req:ResultURL>%s</req:ResultURL>"
            + "    </req:Caller>" + "    <req:KeyOwner>%d</req:KeyOwner>"
            + "    <req:Timestamp>%s</req:Timestamp>" + "  </req:Header>" + "  <req:Body>"
            + "    <req:Identity>" + "      <req:Initiator>"
            + "        <req:IdentifierType>%d</req:IdentifierType>"
            + "        <req:Identifier>%s</req:Identifier>"
            + "        <req:SecurityCredential>%s</req:SecurityCredential>"
            + "        <req:ShortCode>%s</req:ShortCode>" + "      </req:Initiator>"
            + "      <req:PrimaryParty>" + "        <req:IdentifierType>%d</req:IdentifierType>"
            + "        <req:Identifier>%s</req:Identifier>" + "      </req:PrimaryParty>"
            + "      <req:ReceiverParty>" + "        <req:IdentifierType>%d</req:IdentifierType>"
            + "        <req:Identifier>%s</req:Identifier>" + "      </req:ReceiverParty>"
            + "    </req:Identity>" + "    <req:TransactionRequest>" + "      <req:Parameters>"
            + "        <req:Amount>%s</req:Amount>" + "        <req:Currency>%s</req:Currency>"
            + "      </req:Parameters>" + "    </req:TransactionRequest>" + "  </req:Body>"
            + "</api:Request>",
        // Note: The namespace definitions are now in the root element <api:Request>
        SOAP_NAMESPACE_API, SOAP_NAMESPACE_REQ, SOAP_NAMESPACE_COM, telebirrUssdProperties.getCommandId(),
        originatorConversationId, telebirrUssdProperties.getCallerType(), telebirrUssdProperties.getThirdPartyId(),
        telebirrUssdProperties.getPassword(), telebirrUssdProperties.getResultUrl(), telebirrUssdProperties.getKeyOwner(), timestamp,
        telebirrUssdProperties.getInitiatorIdentifierType(), telebirrUssdProperties.getInitiatorIdentifier(),
        telebirrUssdProperties.getSecurityCredential(), telebirrUssdProperties.getShortCode(),
        telebirrUssdProperties.getPrimaryIdentifierType(),
        primaryIdentifier, telebirrUssdProperties.getReceiverIdentifierType(),
        telebirrUssdProperties.getReceiverIdentifier(), amount,
        telebirrUssdProperties.getCurrency());
  }

  /**
   * Sends the SOAP request and returns the response.
   */
  private String sendSoapRequest(String requestXml) {
    StringSource requestPayload = new StringSource(requestXml);

    try {
      StringWriter responseWriter = new StringWriter();

      webServiceTemplate.sendAndReceive(telebirrUssdProperties.getSoapEndpoint(), message -> {
        try {
          Transformer transformer = TransformerFactory.newInstance().newTransformer();
          transformer.transform(requestPayload, message.getPayloadResult());
        } catch (Exception e) {
          log.error("Failed to transform SOAP request payload | TelebirrUssdClient | sendSoapRequest | error={}",
              e.getMessage(), e);
          throw new SoapProcessingException("Failed to set SOAP request payload", e);
        }
      }, message -> {
        try {
          Transformer transformer = TransformerFactory.newInstance().newTransformer();
          transformer.transform(message.getPayloadSource(), new StreamResult(responseWriter));
        } catch (Exception e) {
          log.error("Failed to transform SOAP response payload | TelebirrUssdClient | sendSoapRequest | error={}",
              e.getMessage(), e);
          throw new SoapProcessingException("Failed to read SOAP response payload", e);
        }
      });

      return responseWriter.toString();

    } catch (SoapProcessingException e) {
      throw e;
    } catch (Exception e) {
      log.error("SOAP communication failed | TelebirrUssdClient | sendSoapRequest | error={}",
          e.getMessage(), e);
      throw new SoapCommunicationException("SOAP request failed: " + e.getMessage(), e);
    }
  }

  /**
   * Parses the SOAP response XML into a simplified DTO. Required fields: - OriginatorConversationID
   * (default: "-1" on error) - ConversationID (default: "-1" on error) - ResponseCode -
   * ServiceStatus
   */
  private TelebirrUssdPaymentResponse parseSoapResponse(String soapResponse) {
    TelebirrUssdPaymentResponse response = TelebirrUssdPaymentResponse.builder().build();

    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(new ByteArrayInputStream(soapResponse.getBytes()));

      // Extract ResponseCode
      NodeList responseCodeNodes = document.getElementsByTagName("ResponseCode");
      if (responseCodeNodes.getLength() == 0) {
        responseCodeNodes = document.getElementsByTagName("res:ResponseCode");
      }
      if (responseCodeNodes.getLength() > 0) {
        response.setResponseCode(responseCodeNodes.item(0).getTextContent().trim());
      }

      // Extract ServiceStatus
      NodeList serviceStatusNodes = document.getElementsByTagName("ServiceStatus");
      if (serviceStatusNodes.getLength() == 0) {
        serviceStatusNodes = document.getElementsByTagName("res:ServiceStatus");
      }
      if (serviceStatusNodes.getLength() > 0) {
        response.setServiceStatus(serviceStatusNodes.item(0).getTextContent().trim());
      }

      // Extract ConversationID
      NodeList convIdNodes = document.getElementsByTagName("ConversationID");
      if (convIdNodes.getLength() == 0) {
        convIdNodes = document.getElementsByTagName("res:ConversationID");
      }
      if (convIdNodes.getLength() > 0) {
        response.setConversationId(convIdNodes.item(0).getTextContent().trim());
      }

      // Extract OriginatorConversationID
      NodeList origConvIdNodes = document.getElementsByTagName("OriginatorConversationID");
      if (origConvIdNodes.getLength() == 0) {
        origConvIdNodes = document.getElementsByTagName("res:OriginatorConversationID");
      }
      if (origConvIdNodes.getLength() > 0) {
        response.setOriginatorConversationId(origConvIdNodes.item(0).getTextContent().trim());
      }

      return response;

    } catch (Exception e) {
      log.error("Failed to parse SOAP response | TelebirrUssdClient | parseSoapResponse | error={}",
          e.getMessage(), e);

      // Default values on exception
      response.setResponseCode("999");
      response.setServiceStatus("999");
    }

    return response;
  }


  /**
   * Gets current timestamp in required format.
   */
  private String getCurrentTimestamp() {
    return LocalDateTime.now().format(TIMESTAMP_FORMATTER);
  }

  /**
   * Determines if a transaction is successful based on Telebirr response.
   *
   * <p><b>SUCCESS CRITERIA - BOTH must be "0":</b></p>
   * <ul>
   *   <li>ResponseCode = "0"</li>
   *   <li>ServiceStatus = "0"</li>
   * </ul>
   *
   * @param responseCode  The response code from SOAP response
   * @param serviceStatus The service status from SOAP response
   * @return true if transaction is successful (both values are "0"), false otherwise
   */
  public static boolean isTransactionSuccessful(String responseCode, String serviceStatus) {
    return "0".equals(responseCode) && "0".equals(serviceStatus);
  }

  /**
   * Process Telebirr USSD callback request and update transaction state.
   *
   * @param callback Telebirr callback payload
   * @return JSON response describing the result of the fulfillment
   */
  public JSONObject updateTransactionStatus(TelebirrUssdCallbackRequest callback) {
    JSONObject response = new JSONObject();

    try {
      // STEP 1: Retrieve transaction from DB
      Optional<OrderPayment> optionalPayment = orderPaymentRepository.findByReference(
          callback.getCpalTransactionRef());

      if (optionalPayment.isEmpty()) {
        log.warn("Transaction not found | TelebirrUssdPaymentFulfillmentService | updateTransactionStatus | reference={}",
            callback.getCpalTransactionRef());
        return buildResponse(response, String.valueOf(ResponseCodes.NOT_FOUND), "Failed",
            "The transaction could not be found.");
      }

      OrderPayment payment = optionalPayment.get();

      // STEP 2: Check if already processed
      if (PaymentStatus.SUCCESS == payment.getStatus()) {
        log.info("Transaction already completed | TelebirrUssdPaymentFulfillmentService | updateTransactionStatus | reference={}",
            payment.getReference());
        return buildResponse(response, String.valueOf(ResponseCodes.BAD_REQUEST), "Failed",
            "Transaction already completed.");
      }

      // STEP 3: Extract status from callback and record webhook
      String resultCode = callback.getResultCode() != null ? callback.getResultCode() :
          (Boolean.TRUE.equals(callback.getIsSuccess()) ? "0" : "999");
      String webhookStatus = callback.getResultDesc() != null ? callback.getResultDesc() : resultCode;

      payment.recordWebhook(callback.toString(), webhookStatus);

      // Set gateway reference from callback
      String gatewayRef = callback.getConversationId() != null ? callback.getConversationId() :
          (callback.getTransactionId() != null ? callback.getTransactionId() : null);
      if (gatewayRef != null) {
        payment.setGatewayReference(gatewayRef);
      }
      orderPaymentRepository.save(payment);

      if (Boolean.TRUE.equals(callback.getIsSuccess())) {
        handleSuccess(payment, response);
      } else {
        handleFailure(payment, response, callback.getResultDesc());
      }

      return response;

    } catch (Exception ex) {
      log.error("Error processing Telebirr callback | TelebirrUssdPaymentFulfillmentService | updateTransactionStatus | reference={}, error={}",
          callback.getCpalTransactionRef(), ex.getMessage(), ex);
      return buildResponse(response, String.valueOf(ResponseCodes.INTERNAL_SERVER_ERROR), "Failed",
          "Internal system error.");
    }
  }

  private void handleSuccess(OrderPayment payment, JSONObject response) {
    log.info("Processing successful Telebirr transaction | TelebirrUssdPaymentFulfillmentService | handleSuccess | reference={}",
        payment.getReference());

    payment.resolve(PaymentStatus.SUCCESS, "SUCCESS");
    orderPaymentRepository.save(payment);

    buildResponse(response, String.valueOf(ResponseCodes.SUCCESS), "Transaction Successful",
        "The transaction has been successfully completed.");

    // Trigger downstream fulfillment
    try {
      processSuccessPayment.pickAndProcess(payment);
      log.info("Post-payment workflow triggered successfully | TelebirrUssdPaymentFulfillmentService | handleSuccess | reference={}",
          payment.getReference());
    } catch (Exception e) {
      log.error("Post-payment workflow failed | TelebirrUssdPaymentFulfillmentService | handleSuccess | reference={}, error={}",
          payment.getReference(), e.getMessage(), e);
    }
  }

  private void handleFailure(OrderPayment payment, JSONObject response, String resultDesc) {
    String failureMessage = resultDesc != null && !resultDesc.isEmpty() ? resultDesc : "FAILED";
    log.warn("Processing failed Telebirr transaction | TelebirrUssdPaymentFulfillmentService | handleFailure | reference={}, reason={}",
        payment.getReference(), failureMessage);

    payment.setGatewayReference("FAILED");
    payment.resolve(PaymentStatus.FAILED, failureMessage);
    orderPaymentRepository.save(payment);

    buildResponse(response, String.valueOf(ResponseCodes.BAD_REQUEST), "Failed",
        "Transaction could not be completed.");
  }

  private JSONObject buildResponse(JSONObject json, String statusCode, String statusDescription,
      String statusMessage) {
    return json.put("statusCode", statusCode)
        .put("statusDescription", statusDescription)
        .put("statusMessage", statusMessage);
  }


}

