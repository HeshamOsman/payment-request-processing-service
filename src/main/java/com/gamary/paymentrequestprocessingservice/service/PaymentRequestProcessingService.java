package com.gamary.paymentrequestprocessingservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.streams.kstream.KStream;

import java.util.function.Function;

public interface PaymentRequestProcessingService {
    String callTikkieApi(String paymentRequestDTOJson) throws JsonProcessingException;
}
