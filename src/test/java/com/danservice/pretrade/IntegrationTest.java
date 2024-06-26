package com.danservice.pretrade;

import com.danservice.pretrade.adapter.inbound.api.order.v1.dto.ApiBaseOrderDTO;
import com.danservice.pretrade.adapter.inbound.api.order.v1.dto.ApiCreateOrderResponseDTO;
import com.danservice.pretrade.adapter.inbound.api.order.v1.dto.ApiOrderDTO;
import com.danservice.pretrade.adapter.inbound.api.order.v1.dto.ApiOrderDTO.ApiOrderDTOBuilder;
import com.danservice.pretrade.adapter.inbound.api.order.v1.dto.ApiOrderStatusResponseDTO;
import com.danservice.pretrade.adapter.outbound.client.gfm.GetOrderStatusResponseDTO;
import com.danservice.pretrade.adapter.outbound.client.validation.OrderValidationResponseDTO;
import com.danservice.pretrade.adapter.outbound.kafka.clientorder.v1.dto.KafkaClientOrderDTO;
import com.danservice.pretrade.adapter.repository.OrderRepository;
import com.danservice.pretrade.domain.OrderStatus;
import com.danservice.pretrade.model.OrderEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.danservice.pretrade.IntegrationTest.LoadBalancerTestConfiguration;
import static com.danservice.pretrade.adapter.inbound.api.order.v1.OrdersController.BASE_ENDPOINT_ORDERS;
import static com.danservice.pretrade.adapter.inbound.api.order.v1.dto.ApiOrderDTO.builder;
import static com.danservice.pretrade.adapter.inbound.api.order.v1.dto.ApiResultType.ERROR;
import static com.danservice.pretrade.adapter.inbound.api.order.v1.dto.ApiResultType.SUCCESS;
import static com.danservice.pretrade.domain.OrderStatus.EXECUTED;
import static com.danservice.pretrade.domain.OrderType.LIMIT;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.UP;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static java.util.function.Function.identity;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;
import static org.apache.kafka.common.utils.Utils.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.kafka.support.serializer.JsonDeserializer.TRUSTED_PACKAGES;
import static org.springframework.kafka.support.serializer.JsonDeserializer.TYPE_MAPPINGS;
import static org.springframework.kafka.test.utils.KafkaTestUtils.consumerProps;
import static org.springframework.kafka.test.utils.KafkaTestUtils.getRecords;
import static reactor.core.publisher.Flux.just;

@EmbeddedKafka(partitions = 1, topics = "${dan.topic.client-order}")
@SpringBootTest(classes = {Application.class, LoadBalancerTestConfiguration.class}, webEnvironment = RANDOM_PORT)
class IntegrationTest {
    private static final EasyRandom EASY_RANDOM = new EasyRandom();
    private static final String ENDPOINT_ALL_ORDERS = BASE_ENDPOINT_ORDERS;
    private static final String ENDPOINT_ORDER_ID = BASE_ENDPOINT_ORDERS + "/{orderId}";
    private static final String ENDPOINT_ORDER_STATUS_ORDER_ID = BASE_ENDPOINT_ORDERS + "/{orderId}/status";

    @Value("${dan.topic.client-order}")
    private String clientOrdersTopic;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WireMockServer wireMockServer;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private KafkaProperties kafkaProperties;
    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @BeforeEach
    void beforeEach() {
        wireMockServer.resetAll();
        orderRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    void shouldGetOrder() {
        OrderEntity order = storeNewOrder();
        UUID orderId = order.getId();

        ResponseEntity<ApiOrderDTO> response = testRestTemplate
                .getForEntity(ENDPOINT_ORDER_ID, ApiOrderDTO.class, orderId);

        ApiOrderDTO actual = response.getBody();
        assertNotNull(actual);
        assertEquals(OK, response.getStatusCode());
        verifyGetOrderResponse(actual, orderId);
    }

    @Test
    @SneakyThrows
    void shouldGetAllOrders() {
        OrderEntity order1 = storeNewOrder();
        OrderEntity order2 = storeNewOrder();
        OrderEntity order3 = storeNewOrder();

        ResponseEntity<ApiOrderDTO[]> response = testRestTemplate
                .getForEntity(ENDPOINT_ALL_ORDERS, ApiOrderDTO[].class);

        assertEquals(OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<UUID, ApiOrderDTO> orders = stream(response.getBody()).collect(Collectors.toMap(ApiOrderDTO::getId, identity()));
        assertEquals(3, orders.size());
        assertBaseOrdersEquals(order1, orders.get(order1.getId()));
        assertBaseOrdersEquals(order2, orders.get(order2.getId()));
        assertBaseOrdersEquals(order3, orders.get(order3.getId()));
    }

    @Test
    @SneakyThrows
    void shouldGetOrderStatus() {
        UUID orderId = randomUUID();
        stubOrderStatusCall(orderId, EXECUTED);

        ResponseEntity<ApiOrderStatusResponseDTO> response = testRestTemplate
                .getForEntity(ENDPOINT_ORDER_STATUS_ORDER_ID, ApiOrderStatusResponseDTO.class, orderId);

        ApiOrderStatusResponseDTO actual = response.getBody();
        assertNotNull(actual);
        assertEquals(OK, response.getStatusCode());
        assertEquals(actual.getStatus(), EXECUTED);
    }

    @Test
    @SneakyThrows
    void shouldReturnNoContentIfOrderNotFound() {
        ResponseEntity<ApiOrderDTO> response = testRestTemplate
                .getForEntity(ENDPOINT_ORDER_ID, ApiOrderDTO.class, randomUUID());

        assertEquals(NO_CONTENT, response.getStatusCode());
    }

    @Test
    @SneakyThrows
    void shouldAddNewOrder() {
        stubValidOrderValidationCall();
        ApiOrderDTO newOrderDTO = newCreateOrderDTO();

        ResponseEntity<ApiCreateOrderResponseDTO> result = testRestTemplate
                .postForEntity(BASE_ENDPOINT_ORDERS, newOrderDTO, ApiCreateOrderResponseDTO.class);

        assertEquals(CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        verifyOrderValidationCall(newOrderDTO);
        verifyKafkaClienteOrderProduced(result);
        verifyCreateOrderResponse(newOrderDTO, result);
    }

    @Test
    @SneakyThrows
    void shouldNotAddNewOrderIfValidationCallFails() {
        List<String> errorList = singletonList(randomAlphabetic(50));
        ApiOrderDTO newOrderDTO = newCreateOrderDTO();
        stubInvalidOrderValidationCall(errorList);

        ResponseEntity<ApiCreateOrderResponseDTO> result = testRestTemplate
                .postForEntity(BASE_ENDPOINT_ORDERS, newOrderDTO, ApiCreateOrderResponseDTO.class);

        assertEquals(BAD_REQUEST, result.getStatusCode());
        ApiCreateOrderResponseDTO responseBody = result.getBody();
        assertNotNull(responseBody);
        assertNull(responseBody.getOrder());
        verifyOrderValidationCall(newOrderDTO);
        assertEquals(ERROR, responseBody.getResult());
        assertEquals(errorList, responseBody.getErrors());
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("invalidOrderDTOGenerator")
    void shouldNotAddNewOrderIfInvalid(ApiOrderDTO invalidOrderDTO) {
        ResponseEntity<Object> result = testRestTemplate
                .postForEntity(BASE_ENDPOINT_ORDERS, invalidOrderDTO, Object.class);

        verifyOrderValidationNotCalled();
        assertEquals(BAD_REQUEST, result.getStatusCode());
    }

    private void verifyKafkaClienteOrderProduced(ResponseEntity<ApiCreateOrderResponseDTO> result) {
        List<ConsumerRecord<String, KafkaClientOrderDTO>> consumerRecords = consumeFromKafkaTopic();
        assertEquals(1, consumerRecords.size());
        assertThat(consumerRecords.get(0).value())
                .usingRecursiveComparison()
                .isEqualTo(result.getBody().getOrder());
    }

    private List<ConsumerRecord<String, KafkaClientOrderDTO>> consumeFromKafkaTopic() {
        Map<String, Object> consumerProps = consumerProps("test-group", "true", embeddedKafkaBroker);
        consumerProps.put(TRUSTED_PACKAGES, "*");
        consumerProps.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(TYPE_MAPPINGS, kafkaProperties.getProducer().getProperties().get(TYPE_MAPPINGS));

        ConsumerFactory<String, KafkaClientOrderDTO> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        org.apache.kafka.clients.consumer.Consumer<String, KafkaClientOrderDTO> consumer = consumerFactory.createConsumer();

        embeddedKafkaBroker
                .consumeFromAnEmbeddedTopic(consumer, clientOrdersTopic);

        return toList(
                getRecords(consumer, Duration.of(5, SECONDS))
                        .records(clientOrdersTopic));
    }

    private static void assertBaseOrdersEquals(ApiOrderDTO expected, ApiOrderDTO actual) {
        assertEquals(expected.getType(), actual.getType());
        assertEquals(expected.getPrice(), actual.getPrice());
        assertEquals(expected.getQuantity(), actual.getQuantity());
        assertEquals(expected.getInstrument(), actual.getInstrument());
    }

    private static void assertBaseOrdersEquals(OrderEntity expected, ApiOrderDTO actual) {
        assertEquals(expected.getType(), actual.getType());
        assertEquals(expected.getQuantity(), actual.getQuantity());
        assertEquals(expected.getInstrument(), actual.getInstrument());
        assertEquals(expected.getPrice().setScale(1, UP), actual.getPrice().setScale(1, UP));
    }

    private void verifyDatabaseOrder(ApiOrderDTO actual, UUID orderId) {
        OrderEntity expected = retrieveMandatoryOrder(orderId);

        assertBaseOrdersEquals(expected, actual);
        assertEquals(expected.getId(), actual.getId());
    }

    private void verifyGetOrderResponse(ApiOrderDTO actual, UUID orderId) {
        assertNotNull(actual);
        verifyDatabaseOrder(actual, orderId);
    }

    private void verifyCreateOrderResponse(ApiOrderDTO expected, ResponseEntity<ApiCreateOrderResponseDTO> result) {
        assertNotNull(result.getBody());
        ApiOrderDTO actual = result.getBody().getOrder();

        assertNotNull(actual);
        assertBaseOrdersEquals(expected, actual);
        verifyDatabaseOrder(actual, actual.getId());
        assertEquals(SUCCESS, result.getBody().getResult());
    }

    private OrderEntity retrieveMandatoryOrder(UUID orderId) {
        return orderRepository
                .findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(format("Expected Order with ID [%s] not found", orderId)));
    }

    private OrderEntity storeNewOrder() {
        return orderRepository.save(
                OrderEntity.builder()
                        .type(LIMIT)
                        .instrument(randomAlphabetic(15))
                        .quantity(EASY_RANDOM.nextInt(1, 100))
                        .price(BigDecimal.valueOf(EASY_RANDOM.nextDouble(1.0d, 100.0d))).build());
    }

    private static ApiOrderDTO newCreateOrderDTO() {
        return newCreateOrderDTO(x -> {
        });
    }

    private static ApiOrderDTO newCreateOrderDTO(Consumer<ApiOrderDTOBuilder> modifier) {
        var builder = builder()
                .type(LIMIT)
                .instrument(randomAlphabetic(15))
                .quantity(EASY_RANDOM.nextInt(1, 100))
                .price(BigDecimal.valueOf(EASY_RANDOM.nextDouble(1.0d, 100.0d)));

        modifier.accept(builder);

        return builder.build();
    }

    private void stubValidOrderValidationCall() {
        stubOrderValidationCall(true, emptyList());
    }

    private void stubInvalidOrderValidationCall(List<String> errors) {
        stubOrderValidationCall(false, errors);
    }

    @SneakyThrows
    private void stubOrderValidationCall(boolean valid, List<String> errors) {
        wireMockServer
                .stubFor(post("/orders/v1/validate")
                        .willReturn(aResponse()
                                .withStatus(OK.value())
                                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                .withBody(objectMapper
                                        .writeValueAsString(OrderValidationResponseDTO.builder()
                                                .valid(valid)
                                                .errors(errors).build()))));
    }

    @SneakyThrows
    private void stubOrderStatusCall(UUID orderId, OrderStatus orderStatus) {
        wireMockServer
                .stubFor(get(urlPathTemplate("/orders/v1/{orderId}/status"))
                        .withPathParam("orderId", equalTo(orderId.toString()))
                        .willReturn(aResponse()
                                .withStatus(OK.value())
                                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                .withBody(objectMapper
                                        .writeValueAsString(GetOrderStatusResponseDTO.builder()
                                                .currentStatus(orderStatus).build()))));
    }

    private void verifyOrderValidationCall(ApiBaseOrderDTO orderDTO) {
        wireMockServer.
                verify(postRequestedFor(urlEqualTo("/orders/v1/validate"))
                        .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
                        .withRequestBody(matchingJsonPath("$.type", equalTo(orderDTO.getType().name())))
                        .withRequestBody(matchingJsonPath("$.instrument", equalTo(orderDTO.getInstrument())))
                        .withRequestBody(matchingJsonPath("$.price", equalTo(orderDTO.getPrice().toString())))
                        .withRequestBody(matchingJsonPath("$.quantity", equalTo(String.valueOf(orderDTO.getQuantity()))))
                );
    }

    private void verifyOrderValidationNotCalled() {
        wireMockServer.
                verify(0, postRequestedFor(urlEqualTo("/orders/v1/")));
    }

    private static Stream<Arguments> invalidOrderDTOGenerator() {
        return Stream.of(
                Arguments.of(newCreateOrderDTO(order -> order.price(ZERO))),
                Arguments.of(newCreateOrderDTO(order -> order.quantity(-1))),
                Arguments.of(newCreateOrderDTO(order -> order.instrument(EMPTY))),
                Arguments.of(newCreateOrderDTO(order -> order.price(BigDecimal.valueOf(0.00001)))),
                Arguments.of(newCreateOrderDTO(order -> order.instrument(randomAlphabetic(21)))));
    }

    @TestConfiguration
    static class LoadBalancerTestConfiguration {
        @Bean(initMethod = "start", destroyMethod = "stop")
        public WireMockServer wireMockServer() {
            return new WireMockServer(0);
        }

        @Bean
        @Primary
        public ServiceInstanceListSupplier serviceInstanceListSupplier(final WireMockServer wireMockServer) {
            return new ServiceInstanceListSupplier() {
                @Override
                public String getServiceId() {
                    return "";
                }

                @Override
                public Flux<List<ServiceInstance>> get() {
                    return just(List.of(new DefaultServiceInstance("", "", "localhost", wireMockServer.port(), false)));
                }
            };
        }
    }
}
