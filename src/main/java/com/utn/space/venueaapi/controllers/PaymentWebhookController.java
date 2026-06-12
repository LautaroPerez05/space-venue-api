package com.utn.space.venueaapi.controllers;

import com.utn.space.venueaapi.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final IPaymentService paymentService;

    @PostMapping("/webhook")
    public ResponseEntity<Void> receiveWebhook(
            @RequestParam(value = "topic", required = false) String topic,
            @RequestParam(value = "id", required = false) Long id,
            @RequestBody(required = false) Map<String, Object> data) {

        // Mercado Pago a veces envía el topic y el id por Query Params ("topic=payment"),
        // y otras veces (en formatos más nuevos) los envía dentro del Body JSON.
        String currentTopic = topic;
        Long currentId = id;

        if (data != null && data.containsKey("action")) {
            currentTopic = (String) data.get("type");
            if (data.containsKey("data")) {
                Map<String, Object> dataContent = (Map<String, Object>) data.get("data");
                currentId = Long.parseLong((String) dataContent.get("id"));
            }
        }

        if ("payment".equals(currentTopic) && currentId != null) {
            log.info("Webhook recibido de Mercado Pago para el pago ID: {}", currentId);
            paymentService.processNotification(currentId);
        }

        // Siempre devolvemos 200 OK a Mercado Pago para que no reintente el envío
        return ResponseEntity.ok().build();
    }
}