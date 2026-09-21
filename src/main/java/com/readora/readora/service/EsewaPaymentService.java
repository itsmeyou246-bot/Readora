package com.readora.readora.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class EsewaPaymentService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${esewa.merchant-code}")
    private String merchantCode;

    @Value("${esewa.secret-key}")
    private String secretKey;

    @Value("${esewa.form-url}")
    private String formUrl;

    @Value("${esewa.status-url}")
    private String statusUrl;

    @Value("${app.base-url}")
    private String appBaseUrl;

    public EsewaPaymentService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Builds the signed form fields eSewa's v2 API requires. The frontend must
     * auto-submit these as a POST form to formUrl (getFormActionUrl()) — eSewa
     * does not accept a plain redirect the way Khalti does.
     */
    public Map<String, String> buildFormFields(String transactionUuid, double amount) {
        String signedFieldNames = "total_amount,transaction_uuid,product_code";
        String toSign = "total_amount=" + amount + ",transaction_uuid=" + transactionUuid
                + ",product_code=" + merchantCode;
        String signature = sign(toSign);

        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("amount", String.valueOf(amount));
        fields.put("tax_amount", "0");
        fields.put("total_amount", String.valueOf(amount));
        fields.put("transaction_uuid", transactionUuid);
        fields.put("product_code", merchantCode);
        fields.put("product_service_charge", "0");
        fields.put("product_delivery_charge", "0");
        fields.put("success_url", appBaseUrl + "/api/payments/esewa/success");
        fields.put("failure_url", appBaseUrl + "/api/payments/esewa/failure");
        fields.put("signed_field_names", signedFieldNames);
        fields.put("signature", signature);
        return fields;
    }

    public String getFormActionUrl() {
        return formUrl;
    }

    /** Decodes eSewa's base64 "data" query param from the success redirect. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> decodeCallback(String base64Data) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(base64Data);
        return objectMapper.readValue(decoded, Map.class);
    }

    /** Recomputes the signature eSewa sent back and checks it matches — reject if not. */
    public boolean verifySignature(Map<String, Object> callbackData) {
        Object signedFieldNamesObj = callbackData.get("signed_field_names");
        Object signatureObj = callbackData.get("signature");
        if (signedFieldNamesObj == null || signatureObj == null) return false;

        String[] fieldNames = signedFieldNamesObj.toString().split(",");
        StringBuilder toSign = new StringBuilder();
        for (int i = 0; i < fieldNames.length; i++) {
            if (i > 0) toSign.append(",");
            toSign.append(fieldNames[i]).append("=").append(callbackData.get(fieldNames[i].trim()));
        }

        String expected = sign(toSign.toString());
        return expected.equals(signatureObj.toString());
    }

    /** Independently confirms status by hitting eSewa's status-check API — belt and suspenders. */
    public Map<String, Object> checkStatus(String transactionUuid, double totalAmount) {
        String url = statusUrl + "?product_code=" + merchantCode
                + "&total_amount=" + totalAmount
                + "&transaction_uuid=" + transactionUuid;
        return restTemplate.getForObject(url, Map.class);
    }

    private String sign(String message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Could not sign eSewa payload.", e);
        }
    }
}