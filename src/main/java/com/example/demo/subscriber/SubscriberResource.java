package com.example.demo.subscriber;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.coldpublisher.ColdPublishService;
import com.example.demo.hotpublisher.HotPublishService;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/stream")
public class SubscriberResource {

	@Autowired
	private HotPublishService hotPublishService;
	@Autowired
	private ColdPublishService coldPublishService;

	private MeterRegistry meterRegistry;
	private Counter whenResultIsBiggerThen100;

	public SubscriberResource(final MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
		this.whenResultIsBiggerThen100 = meterRegistry.counter("whenResultIsBiggerThen100");
	}

	// Hot
	@GetMapping(value = "/hot", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> getMyHotStream() {
		return hotPublishService.count().log().map(i -> "Im counting:" + i.toString());
	}

	// Cold
	@GetMapping(value = "/cold")
	@Timed(value = "my_cold_stream")
	public Mono<Integer> getMyColdStream() {
		Flux<Integer> returnNumberList = coldPublishService.returnNumberList();
		Flux<Integer> returnSquare = coldPublishService.returnSquare(returnNumberList);
		Mono<Integer> returnTotal = coldPublishService.returnTotal(returnSquare);
		return returnTotal;
	}

	// Depended Operations (A + B) -> C
	// We run two paralel operations, when both are finished we forward result of
	// those to a third one
	@GetMapping(value = "/depended/{somenumber}")
	@Timed(value = "my_paralel")
	public Mono<Integer> getSomeParalelThings(@PathVariable("somenumber") Integer somenumber) {
		return Mono.zip(coldPublishService.returnASquare(somenumber), coldPublishService.returnATriple(somenumber))
				.flatMap(tuple -> coldPublishService.returnATotal(tuple.getT1(), tuple.getT2())).map(k -> {
					if (k > 100) {
						whenResultIsBiggerThen100.increment();
					}
					return k;
				});
	}

	// Complicated Paralel / Depended Operations
	// ((A + B) -> C) + ((D + E) -> F) -> (C + F) = G
	@GetMapping(value = "/depended/more/{somenumber}")
	@Timed(value = "my_more_paralel")
	public Mono<Integer> getMoreParalelThings(@PathVariable("somenumber") Integer somenumber) {
		Mono<Integer> merged1 = Mono
				.zip(coldPublishService.returnASquare(somenumber), coldPublishService.returnATriple(somenumber),
						coldPublishService.returnAQuadrad(somenumber))
				.flatMap(tuple -> coldPublishService.returnATotal(tuple.getT1(), tuple.getT2(), tuple.getT3()));
		Mono<Integer> merged2 = Mono
				.zip(coldPublishService.returnASquare(somenumber + 1), coldPublishService.returnATriple(somenumber + 1),
						coldPublishService.returnAQuadrad(somenumber + 1))
				.flatMap(tuple -> coldPublishService.returnATotal(tuple.getT1(), tuple.getT2(), tuple.getT3()));
		return Mono.zip(merged1, merged2)
				.flatMap(tuple -> coldPublishService.returnATotal(tuple.getT1(), tuple.getT2()));

	}

}