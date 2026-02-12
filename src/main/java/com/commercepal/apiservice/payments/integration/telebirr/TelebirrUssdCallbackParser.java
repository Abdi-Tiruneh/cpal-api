package com.commercepal.apiservice.payments.integration.telebirr;

import com.commercepal.apiservice.payments.integration.telebirr.dto.TelebirrUssdCallbackRequest;
import com.commercepal.apiservice.payments.integration.telebirr.exception.SoapProcessingException;
import java.io.ByteArrayInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

@Slf4j
@Component
public class TelebirrUssdCallbackParser {

    public TelebirrUssdCallbackRequest parseCallback(String xmlCallback) {
        log.debug("Parsing Telebirr callback XML");
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xmlCallback.getBytes()));

            TelebirrUssdCallbackRequest callback = TelebirrUssdCallbackRequest.builder().build();

            // Extract OriginatorConversationID
            String origConvId = extractTextContent(document, "OriginatorConversationID", "res:OriginatorConversationID");
            callback.setCpalTransactionRef(origConvId);

            // Extract ConversationID
            String convId = extractTextContent(document, "ConversationID", "res:ConversationID");
            callback.setConversationId(convId);

            // Extract ResultType
            String resultType = extractTextContent(document, "ResultType", "res:ResultType");
            callback.setResultType(resultType);

            // Extract ResultCode
            String resultCode = extractTextContent(document, "ResultCode", "res:ResultCode");
            callback.setResultCode(resultCode);

            // Extract ResultDesc
            String resultDesc = extractTextContent(document, "ResultDesc", "res:ResultDesc");
            callback.setResultDesc(resultDesc);

            // Extract TransactionID
            String transactionId = extractTextContent(document, "TransactionID", "res:TransactionID");
            callback.setTransactionId(transactionId);

            // Determine if transaction is successful (both ResultType and ResultCode must be "0")
            boolean isSuccess = "0".equals(resultType) && "0".equals(resultCode);
            callback.setIsSuccess(isSuccess);

            if (isSuccess) {
                log.info("Callback parsed successfully - TransactionID: {}, ConversationID: {}, Status: SUCCESS",
                        transactionId, convId);
            } else {
                log.warn("Callback parsed with failure status - TransactionID: {}, ResultType: {}, ResultCode: {}, ResultDesc: {}",
                        transactionId, resultType, resultCode, resultDesc);
            }

            return callback;

        } catch (Exception e) {
            log.error("Failed to parse Telebirr callback XML", e);
            throw new SoapProcessingException("Failed to parse callback XML: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts text content from XML element, trying both with and without namespace prefix.
     */
    private String extractTextContent(Document document, String tagName, String namespacedTagName) {
        NodeList nodes = document.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            nodes = document.getElementsByTagName(namespacedTagName);
        }
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return null;
    }

}
