package com.gamary.paymentrequestprocessingservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamary.paymentplatformcommons.dto.PaymentRequestDTO;

import com.gamary.paymentrequestprocessingservice.service.PaymentRequestProcessingService;
import com.gamary.paymentrequestprocessingservice.service.dto.ProcessedPaymentRequestDTO;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.modelmapper.ModelMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.function.Function;

@Service
public class PaymentRequestProcessingServiceImpl implements PaymentRequestProcessingService {

    private static final String APP_TOKEN_HEADER = "X-App-Token";
    private static final String API_KEY_HEADER = "API-Key";
    private static final String PAYMENT_REQUEST_API_URL = "/paymentrequests";
    @Value("${Tikkie.appToken}")
    private String appToken;
    @Value("${Tikkie.apiKey}")
    private String apiKey;
    @Value("${Tikkie.baseUrl}")
    private String baseUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ModelMapper modelMapper;

    public PaymentRequestProcessingServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper,ModelMapper modelMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.modelMapper = modelMapper;
    }

    public String callTikkieApi(String paymentRequestDTOJson) throws JsonProcessingException {

        var bodyAndHeaders= new HttpEntity<ProcessedPaymentRequestDTO>(getRequestBodyObject(paymentRequestDTOJson), getRequestHeaders());

        return restTemplate.postForObject(baseUrl+PAYMENT_REQUEST_API_URL,bodyAndHeaders,String.class);

    }

    private HttpHeaders getRequestHeaders(){
        var headers = new HttpHeaders();
        headers.add(APP_TOKEN_HEADER,appToken);
        headers.add(API_KEY_HEADER,apiKey);
        return headers;
    }

    private ProcessedPaymentRequestDTO getRequestBodyObject(String paymentRequestDTOJson) throws JsonProcessingException {
        PaymentRequestDTO paymentRequestDTO = objectMapper.readValue(paymentRequestDTOJson,PaymentRequestDTO.class);
        ProcessedPaymentRequestDTO processedPaymentRequest = modelMapper.map(paymentRequestDTO, ProcessedPaymentRequestDTO.class);
        return processedPaymentRequest;
    }

    @Bean
    public Function<KStream<String, String>, KStream<String, String>> processPaymentRequest() {
        return input -> {
            return input.map((key, paymentRequestDTOJson) ->{
                try {
                    return new KeyValue<String,String>(key,callTikkieApi(paymentRequestDTOJson));
                } catch (JsonProcessingException e) {
                    return new KeyValue<String,String>(null,null);
                }
            });
        };
    }
}
