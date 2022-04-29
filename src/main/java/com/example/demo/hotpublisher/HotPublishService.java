package com.example.demo.hotpublisher;

import java.time.Duration;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class HotPublishService {

	public Flux<Long> count() {
		Flux<Long> intervalFlux = Flux.interval(Duration.ofSeconds(1L));
		return intervalFlux;
	}
}
