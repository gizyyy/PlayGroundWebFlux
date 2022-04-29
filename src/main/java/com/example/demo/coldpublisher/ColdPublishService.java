package com.example.demo.coldpublisher;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ColdPublishService {

	public Flux<Integer> returnNumberList() {
		return Flux.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
	}

	public Flux<Integer> returnSquare(Flux<Integer> numbers) {
		return numbers.map(i -> {
			return i * i;
		});
	}

	public Mono<Integer> returnTotal(Flux<Integer> numbers) {
		return numbers.collectList().flatMap(list -> {
			Integer sum = list.stream().reduce(0, (a, b) -> a + b);
			return Mono.just(sum);
		});
	}

	public Mono<Integer> returnASquare(Integer something) {
		return Mono.just(something).map(k -> k * k);
	}

	public Mono<Integer> returnATriple(Integer something) {
		return Mono.just(something).map(k -> k * k * k);
	}

	public Mono<Integer> returnAQuadrad(Integer something) {
		return Mono.just(something).map(k -> k * k * k * k);
	}

	public Mono<Integer> returnATotal(Integer something1, Integer something2) {
		return Mono.just(something1 + something2);
	}

	public Mono<Integer> returnATotal(Integer something1, Integer something2, Integer something3) {
		return Mono.just(something1 + something2 + something3);
	}
}
