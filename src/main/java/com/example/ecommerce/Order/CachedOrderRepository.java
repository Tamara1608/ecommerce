package com.example.ecommerce.Order;

import com.example.ecommerce.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Cached repository implementation for Order CRUD operations.
 * Uses optimistic caching strategy - checks cache first, falls back to database.
 * Caches entire Order objects with key pattern: order:{id}
 */
@Repository
@Qualifier("cachedOrderRepository")
@RequiredArgsConstructor
public class CachedOrderRepository implements IOrderRepository {
    
    private final OrderTable orderTable;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String CACHE_KEY_PREFIX = "order:";
    private static final String ALL_ORDERS_KEY = "orders:all";
    private static final String USER_ORDERS_PREFIX = "orders:user:";
    
    @Override
    @NonNull
    public Order create(@NonNull Order order) {
        Order saved = orderTable.save(order);
        cacheOrder(saved);
        invalidateAllOrdersCache();
        if (saved.getUser() != null) {
            invalidateUserOrdersCache(saved.getUser().getId());
        }
        return saved;
    }
    
    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public List<Order> findAll() {
        List<Order> cached = (List<Order>) redisTemplate.opsForValue().get(ALL_ORDERS_KEY);
        return cached != null ? cached : new ArrayList<>();
    }
    
    @Override
    @NonNull
    public Optional<Order> findById(@NonNull Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        
        if (cached != null) {
            return Optional.of((Order) cached);
        }
        
        Optional<Order> orderOpt = orderTable.findById(id);
        orderOpt.ifPresent(this::cacheOrder);
        
        return orderOpt;
    }
    
    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public List<Order> findByUserId(@NonNull Long userId) {
        String cacheKey = USER_ORDERS_PREFIX + userId;
        List<Order> cached = (List<Order>) redisTemplate.opsForValue().get(cacheKey);
        
        if (cached != null) {
            return cached;
        }
        
        List<Order> orders = orderTable.findOrdersWithItemsAndProductsByUserId(userId);
        if (!orders.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, orders);
        }
        
        return orders;
    }
    
    @Override
    @NonNull
    public Order update(@NonNull Order order) {
        Order updated = orderTable.save(order);
        cacheOrder(updated);
        invalidateAllOrdersCache();
        if (updated.getUser() != null) {
            invalidateUserOrdersCache(updated.getUser().getId());
        }
        return updated;
    }
    
    @Override
    public void delete(@NonNull Long id) {
        Optional<Order> orderOpt = orderTable.findById(id);
        orderTable.deleteById(id);
        evictFromCache(id);
        invalidateAllOrdersCache();
        orderOpt.ifPresent(order -> {
            if (order.getUser() != null) {
                invalidateUserOrdersCache(order.getUser().getId());
            }
        });
    }
    
    private void cacheOrder(Order order) {
        String cacheKey = CACHE_KEY_PREFIX + order.getId();
        redisTemplate.opsForValue().set(cacheKey, order);
    }
    
    private void evictFromCache(Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        redisTemplate.delete(cacheKey);
    }
    
    private void invalidateAllOrdersCache() {
        redisTemplate.delete(ALL_ORDERS_KEY);
    }
    
    private void invalidateUserOrdersCache(Long userId) {
        redisTemplate.delete(USER_ORDERS_PREFIX + userId);
    }
}

