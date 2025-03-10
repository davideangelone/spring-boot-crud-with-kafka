# Sample Payment Application with Kafka

### Sample payment application that uses Kafka to send and receive messages.

Here is how it works:

- User places order with /place endpoint
- OrderService writes order on database and send message to Kafka order topic
- OrderListener receives message from order topic, process payment (simulated) and sends message to payment topic
- PaymentListener receives message from payment topic, updates stock (simulated) and sends message to inventory topic
- InventoryListener receives message from inventory topic and updates order on database

CircuitBreaker and Retryable are used to manage network failures (randomly simulated in this example)

Docker compose is supplied to build all needed containers for this application

Available endpoints:
- http://localhost:8080/swagger-ui/index.html (Swagger UI console)
- http://localhost:9000 (RedPanda Kafka console, useful to see messages)
- http://localhost:8080/orders/place (Place order, available on SwaggerUI console)
