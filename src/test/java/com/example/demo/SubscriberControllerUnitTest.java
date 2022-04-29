package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.example.demo.coldpublisher.ColdPublishService;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Getter
@Setter
class TotalAssertion {
	private Integer number1;
	private Integer number2;
	private Integer total;
	private Boolean valid;
}

class TotalAssertionAccessor implements ArgumentsAggregator {

	@Override
	public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context)
			throws ArgumentsAggregationException {

		final List<String> tokens = Collections.list(new StringTokenizer(accessor.getString(0), "/")).stream()
				.map(token -> (String) token).collect(Collectors.toList());

		return new TotalAssertion(Integer.valueOf(tokens.get(0)), Integer.valueOf(tokens.get(1)),
				Integer.valueOf(tokens.get(2)), accessor.getBoolean(1));
	}
}

//Extension for Parameterized Test
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { ColdPublishService.class })
public class SubscriberControllerUnitTest {

	@Autowired
	private ColdPublishService coldPublishService;

	private static Stream<Arguments> generator() {
		return Stream.of(Arguments.of(1), Arguments.of(2), Arguments.of(3));
	}

	@ParameterizedTest
	@MethodSource("generator")
	@DisplayName("Should be able to calculate squares")
	public void shoulBeAbleToCalculateSquares(final Integer number) {
		Mono<Integer> returnASquare = coldPublishService.returnASquare(number);
		assertThat(returnASquare.block()).isEqualTo(number * number);
	}

	@ParameterizedTest
	@CsvSource(value = { "1/2/3, true", "2/3/5, true", "2/5/5, false" })
	@DisplayName("Should be able to calculate triples")
	public void shouldCalculateTotals(@AggregateWith(TotalAssertionAccessor.class) TotalAssertion totalAssertion) {

		if (totalAssertion.getValid())
			assertThat(
					coldPublishService.returnATotal(totalAssertion.getNumber1(), totalAssertion.getNumber2()).block())
							.isEqualTo(totalAssertion.getTotal());

		if (!totalAssertion.getValid())
			assertThat(
					coldPublishService.returnATotal(totalAssertion.getNumber1(), totalAssertion.getNumber2()).block())
							.isNotEqualTo(totalAssertion.getTotal());
	}
}