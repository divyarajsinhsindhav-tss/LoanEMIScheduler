package com.emiLoan.EMILoan.dto.payment.details;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = UpiPaymentDetails.class, name = "UPI"),
        @JsonSubTypes.Type(value = CardPaymentDetails.class, name = "CARD"),
        @JsonSubTypes.Type(value = NetBankingPaymentDetails.class, name = "NET_BANKING")
})
public abstract class PaymentMethodDetails {
}