package com.readora.readora.dto;

import java.util.Map;

public class PaymentInitResponse {

    private Long paymentId;
    private String gateway = "ESEWA";
    private String formActionUrl;
    private Map<String, String> formFields;
    private boolean free; // true when a FREE plan needed no gateway at all

    public static PaymentInitResponse gatewayForm(Long paymentId, String formActionUrl, Map<String, String> fields) {
        PaymentInitResponse r = new PaymentInitResponse();
        r.paymentId = paymentId;
        r.formActionUrl = formActionUrl;
        r.formFields = fields;
        return r;
    }

    public static PaymentInitResponse alreadyFree(Long paymentId) {
        PaymentInitResponse r = new PaymentInitResponse();
        r.paymentId = paymentId;
        r.free = true;
        return r;
    }

    public Long getPaymentId() { return paymentId; }
    public String getGateway() { return gateway; }
    public String getFormActionUrl() { return formActionUrl; }
    public Map<String, String> getFormFields() { return formFields; }
    public boolean isFree() { return free; }
}