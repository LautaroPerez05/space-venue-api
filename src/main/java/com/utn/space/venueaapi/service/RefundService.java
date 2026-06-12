package com.utn.space.venueaapi.service;

import com.mercadopago.client.payment.PaymentRefundClient;
import com.utn.space.venueaapi.model.PaymentModel;
import com.utn.space.venueaapi.model.PaymentRefund;
import com.utn.space.venueaapi.repository.PaymentRefundRepository;
import com.utn.space.venueaapi.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final PaymentRepository paymentRepository;
    private final PaymentRefundRepository paymentRefundRepository;

    @Transactional
    public void refundPayment(Long idPayment, BigDecimal amountToRefund) {
        // 1. Verificar que el pago original exista en nuestra BD
        PaymentModel payment = paymentRepository.findById(idPayment)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el pago con ID: " + idPayment));

        try {
            PaymentRefundClient refundClient = new PaymentRefundClient();

            // 2. Ejecutar el reembolso en la API de Mercado Pago (Sandbox)
            // Usamos la ruta completa del SDK para no chocar con tu entidad local
            com.mercadopago.resources.payment.PaymentRefund mpRefund = refundClient.refund(idPayment, amountToRefund);

            // 3. Crear y guardar el registro en nuestra tabla 'payment_refunds'
            PaymentRefund localRefund = new PaymentRefund();
            localRefund.setIdPaymentRefund(mpRefund.getId());
            localRefund.setPayment(payment);
            localRefund.setAmountRefunded(mpRefund.getAmount());
            localRefund.setStatus(mpRefund.getStatus()); // approved, pending, etc.

            paymentRefundRepository.save(localRefund);

            // 4. Actualizar los montos acumulados en la entidad Payment principal
            BigDecimal nuevoTotalReembolsado = payment.getTotalRefundedAmount().add(amountToRefund);
            payment.setTotalRefundedAmount(nuevoTotalReembolsado);

            if (payment.getNetReceivedAmount() != null) {
                payment.setNetReceivedAmount(payment.getNetReceivedAmount().subtract(amountToRefund));
            }

            paymentRepository.save(payment);

        } catch (Exception e) {
            throw new RuntimeException("Error al procesar el reembolso en Mercado Pago: " + e.getMessage(), e);
        }
    }
}
