package com.gamary.paymentrequestprocessingservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gamary.paymentplatformcommons.dto.PaymentRequestDTO;


import com.gamary.paymentrequestprocessingservice.service.dto.ProcessedPaymentRequestDTO;
import org.modelmapper.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


@SpringBootApplication
public class PaymentRequestProcessingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentRequestProcessingServiceApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate(){
		return new RestTemplate();
	}


	@Bean
	public ModelMapper modelMapper(){

		ModelMapper modelMapper = new ModelMapper();
		Provider<LocalDate> localDateProvider = new AbstractProvider<LocalDate>() {
			@Override
			public LocalDate get() {
				return LocalDate.now();
			}
		};

		Converter<String, LocalDate> toStringDate = new AbstractConverter<String, LocalDate>() {
			@Override
			protected LocalDate convert(String source) {
				DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				LocalDate localDate = LocalDate.parse(source, format);
				return localDate;
			}
		};


		modelMapper.createTypeMap(String.class, LocalDate.class);
		modelMapper.addConverter(toStringDate);
		modelMapper.getTypeMap(String.class, LocalDate.class).setProvider(localDateProvider);
		modelMapper.createTypeMap(PaymentRequestDTO.class, ProcessedPaymentRequestDTO.class).addMapping(PaymentRequestDTO::getId,ProcessedPaymentRequestDTO::setReferenceId);


		return modelMapper;
	}



	@Bean
	public ObjectMapper objectMapper(){
		return new ObjectMapper().registerModule(new JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
	}

}
