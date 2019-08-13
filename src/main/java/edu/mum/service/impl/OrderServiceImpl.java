package edu.mum.service.impl;

import edu.mum.domain.*;
import edu.mum.repository.CartRepository;
import edu.mum.repository.OrderItemRepository;
import edu.mum.repository.OrderRepository;
import edu.mum.service.OrderService;
import edu.mum.util.PdfGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private PdfGenerator pdfGenerator;

    @Override
    public Orders getOrderById(Long id) {
        return orderRepository.findById(id).get();
    }

    @Override
    public Orders saveOrder(Buyer buyer, Orders order) {
        List<CartItem> cartItems = (List) cartRepository.getCartItemByBuyerId(buyer.getId());
        BigDecimal totalAmount = new BigDecimal(0.00);
        for (CartItem ci : cartItems) {
            OrderItem oi = new OrderItem();
            oi.setProduct(ci.getProduct());
            oi.setQuantity(ci.getQuantity());
            order.addOrderItem(oi);
            oi.setOrder(order);
            totalAmount = totalAmount.add(ci.getProduct().getPrice().multiply(new BigDecimal(ci.getQuantity())));
            cartRepository.delete(ci);
        }
        if (order.getUsingPoints() == true) {
            totalAmount = totalAmount.subtract(new BigDecimal(buyer.getPoints()));
            if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                buyer.setPoints(0);
            } else {
                buyer.setPoints(totalAmount.abs().intValue());
            }
        }
        order.setTotalAmount(totalAmount);
        order.setBuyer(buyer);
        order.setOrderedDate(LocalDateTime.now());
        buyer.addOrder(order);
        return orderRepository.save(order);
    }

    @Override
    public void completeOrder(Orders order) {
        order.setStatus(OrderStatus.COMPLETED);
        Integer points = order.getTotalAmount().divide(new BigDecimal(100)).intValue();
        order.getBuyer().setPoints(order.getBuyer().getPoints() + points);
        orderRepository.save(order);
    }

    @Override
    public void cancelOrder(Orders order) {
        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
    }

    @Override
    public File downloadReceipt(Orders order) throws Exception {
        Map<String, Orders> data = new HashMap<String, Orders>();
        data.put("order", order);
        return pdfGenerator.createPdf("buyer/PDF", data);

    }

    @Override
    public OrderItem getOrderItemById(Long itemId) {
        return orderItemRepository.findById(itemId).get();
    }


}