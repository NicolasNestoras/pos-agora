package com.nikos.retail.payment;

import com.nikos.retail.order.OrderRepository;
import com.nikos.retail.sale.SaleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private SaleRepository saleRepository;
    @Mock private OrderRepository orderRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void recordPayment_shouldThrow_whenBothSaleAndOrderProvided() {
        PaymentRequest request = new PaymentRequest();
        request.setSaleId(1L);
        request.setOrderId(1L);
        request.setMethod(PaymentMethod.CASH);
        request.setAmount(BigDecimal.TEN);

        assertThrows(IllegalArgumentException.class, () -> paymentService.recordPayment(request));
    }

    @Test
    void recordPayment_shouldThrow_whenNeitherSaleNorOrderProvided() {
        PaymentRequest request = new PaymentRequest();
        request.setMethod(PaymentMethod.CASH);
        request.setAmount(BigDecimal.TEN);

        assertThrows(IllegalArgumentException.class, () -> paymentService.recordPayment(request));
    }
}