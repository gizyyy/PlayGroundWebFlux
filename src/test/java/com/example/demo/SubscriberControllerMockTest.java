package com.example.demo;

import static org.mockito.Mockito.when;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.example.demo.coldpublisher.ColdPublishService;
import com.example.demo.hotpublisher.HotPublishService;
import com.example.demo.subscriber.SubscriberResource;

import reactor.core.publisher.Mono;

@WebFluxTest(controllers = SubscriberResource.class)
@ContextConfiguration(classes = { SubscriberResource.class, HotPublishService.class, ColdPublishService.class })
public class SubscriberControllerMockTest {

	@Autowired
	WebTestClient webTestClient;

	@MockBean
	HotPublishService hotPublishService;

	@MockBean
	ColdPublishService coldPublishService;

	@Test
	public void testCold() {

		when(coldPublishService.returnASquare(Mockito.anyInt())).thenReturn(Mono.just(4));
		when(coldPublishService.returnATriple(Mockito.anyInt())).thenReturn(Mono.just(8));
		when(coldPublishService.returnATotal(Mockito.anyInt(), Mockito.anyInt())).thenReturn(Mono.just(12));

		webTestClient.get().uri("/stream/depended/2").accept(MediaType.APPLICATION_JSON).exchange().expectStatus()
				.isOk().expectBody(Integer.class).value(userResponse -> {
					Assertions.assertThat(userResponse).isEqualTo(12);
				});
	}

}
