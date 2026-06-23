package com.nikos.retail.payment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nikos.retail.common.exception.ResourceNotFoundException;
import com.nikos.retail.order.Order;
import com.nikos.retail.order.OrderRepository;
import com.nikos.retail.order.OrderStatus;
import com.nikos.retail.sale.Sale;
import com.nikos.retail.sale.SaleRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SaleRepository saleRepository;
    private final OrderRepository orderRepository;

    public PaymentService(PaymentRepository paymentRepository,
                           SaleRepository saleRepository,
                           OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.saleRepository = saleRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public PaymentResponse recordPayment(PaymentRequest request) {
        boolean hasSale = request.getSaleId() != null;
        boolean hasOrder = request.getOrderId() != null;

        if (hasSale == hasOrder) {
            // true == true (both set) OR false == false (neither set) - both invalid
            throw new IllegalArgumentException("Payment must reference exactly one of saleId or orderId");
        }

        Payment payment = new Payment();
        payment.setMethod(request.getMethod());
        payment.setAmount(request.getAmount());

        if (hasSale) {
            Sale sale = saleRepository.findById(request.getSaleId())
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + request.getSaleId()));
            payment.setSale(sale);
        } else {
            Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));
            payment.setOrder(order);

            // a successful payment on an order advances its status
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);
            }
        }

        Payment saved = paymentRepository.save(payment);
        return PaymentResponse.fromEntity(saved);
    }

    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        return PaymentResponse.fromEntity(payment);
    }
}