package com.example.ecommerce.FlashSale;

import com.example.ecommerce.DTO.BuyRequest;
import com.example.ecommerce.Order.IOrderService;
import com.example.ecommerce.Product.IProductService;
import com.example.ecommerce.entity.FlashSaleEvent;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for FlashSaleEvent CRUD operations and flash sale purchases.
 * Repository implementation is injected - can be cached or non-cached.
 */
public class FlashSaleService {
    
    private final IFlashSaleRepository flashSaleRepository;
    private final IProductService productService;
    private final IOrderService orderService;
    
    public FlashSaleService(
            IFlashSaleRepository flashSaleRepository,
            IProductService productService,
            IOrderService orderService) {
        this.flashSaleRepository = flashSaleRepository;
        this.productService = productService;
        this.orderService = orderService;
    }
    
    // -------------------
    // Flash Sale Purchase Operations
    // -------------------
    
    /**
     * Buy products during a flash sale.
     * Checks stock via productService, creates order items, and saves the order.
     * Throws exception if any product is out of stock.
     * 
     * @param user the user making the purchase
     * @param productOrders list of product orders with productId and quantity
     * @throws ResponseStatusException if any product is out of stock
     */
    public void buyProducts(@NonNull User user, @NonNull List<BuyRequest.ProductOrder> productOrders) {
        List<OrderItem> items = new ArrayList<>();
        
        Order tempOrder = new Order();
        tempOrder.setUser(user);
        
        for (BuyRequest.ProductOrder po : productOrders) {
            Product product = productService.returnIfInStock(po.getProductId(), po.getQuantity())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Product " + po.getProductId() + " is out of stock"
                    ));
            
            OrderItem item = new OrderItem();
            item.setOrder(tempOrder);
            item.setProduct(product);
            item.setQuantity(po.getQuantity());
            item.setUnitPrice(product.getPrice());
            item.setOrderItemPrice(product.getPrice() * po.getQuantity());
            items.add(item);
        }
        
        orderService.createOrderForUser(user, items);
    }
    
    // -------------------
    // Flash Sale CRUD Operations
    // -------------------
    
    @NonNull
    public FlashSaleEvent create(@NonNull FlashSaleEvent flashSale) {
        return flashSaleRepository.create(flashSale);
    }
    
    @NonNull
    public List<FlashSaleEvent> findAll() {
        return flashSaleRepository.findAll();
    }
    
    @NonNull
    public FlashSaleEvent findById(@NonNull Long id) {
        return flashSaleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Flash sale event not found with id: " + id
                ));
    }
    
    @NonNull
    public FlashSaleEvent update(@NonNull FlashSaleEvent flashSale) {
        if (flashSale.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Flash sale ID is required for update");
        }
        flashSaleRepository.findById(flashSale.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Flash sale event not found with id: " + flashSale.getId()
                ));
        return flashSaleRepository.update(flashSale);
    }
    
    public void delete(@NonNull Long id) {
        flashSaleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Flash sale event not found with id: " + id
                ));
        flashSaleRepository.delete(id);
    }
}

