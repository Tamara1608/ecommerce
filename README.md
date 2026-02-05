# E-Commerce Application - Redis Caching Strategies

## Overview
This e-commerce application demonstrates multiple Redis caching strategies for different data access patterns and consistency requirements. Each strategy is carefully selected based on the specific use case, balancing performance, consistency, and data characteristics.

---

## Implemented Caching Strategies

### 1. **Cache-Aside (Lazy Loading)**
**Description:** Application checks cache first. On cache miss, data is fetched from database and stored in cache for subsequent requests.

**Implementation:**
- **Product Metadata** - `CachedProductRepository.java`
- **Coupon Metadata** - `CachedCouponRepository.java`
- **Orders** - `CachedOrderRepository.java` (read operations)

**Use Case:** Read-heavy operations where data doesn't change frequently

**Location:** 
```
ecommerce/src/main/java/com/example/ecommerce/
├── product/infrastructure/cache/product/CachedProductRepository.java
├── coupon/infrastructure/cache/CachedCouponRepository.java
└── order/infrastructure/cache/order/CachedOrderRepository.java
```

---

### 2. **Write-Through**
**Description:** Data is written to both cache and database simultaneously, ensuring cache consistency.

**Implementation:**
- **Product Metadata** - `CachedProductRepository.java` (create/update operations)
- **User** - `CachedUserRepository.java`
- **Category** - `CachedCategoryRepository.java`

**Use Case:** When data consistency between cache and database is critical

**Location:**
```
ecommerce/src/main/java/com/example/ecommerce/
├── product/infrastructure/cache/product/CachedProductRepository.java
├── user/infrastructure/cache/user/CachedUserRepository.java
└── category/infrastructure/cache/category/CachedCategoryRepository.java
```

**Example:**
```java
// CachedProductRepository.java - Write-Through on create
public Product create(@NonNull Product product) {
    Product saved = productTable.save(product);      // Step 1: Write to DB
    cacheProduct(saved);                              // Step 2: Write to cache
    invalidateAllProductsCache();                     // Step 3: Invalidate collections
    return saved;
}
```

---

### 3. **Read-Through**
**Description:** Cache automatically fetches missing data from database when requested. Similar to cache-aside but cache handles database interaction.

**Implementation:**
- **Category** - `CachedCategoryRepository.java`
- **Product** - `CachedProductRepository.java` (findAll method)

**Use Case:** Frequently read, rarely updated data

**Location:**
```
ecommerce/src/main/java/com/example/ecommerce/
├── category/infrastructure/cache/category/CachedCategoryRepository.java
└── product/infrastructure/cache/product/CachedProductRepository.java
```

---

### 4. **Write-Behind / Write-Back**
**Description:** Data is written to cache first, then asynchronously persisted to database later for high-performance writes.

**Implementation:**
- **Stock Management** - `StockSyncJob.java` 

**Use Case:** High-write scenarios requiring fast response times

**Location:**
```
ecommerce/src/main/java/com/example/ecommerce/product/infrastructure/sync/StockSyncJob.java
```

**Note:** Stock updates are atomic operations in Redis with periodic sync to database.

---

### 5. **TTL / Expiry-Based Caching**
**Description:** Cache entries automatically expire after a calculated time period.

**Implementation:**
- **Coupons** - `CachedCouponRepository.java` (expires at `validUntil` date)
- **Flash Sale Events** - `CachedFlashSaleRepository.java` (expires at event `endDate`)
- **Reviews** - 30-minute TTL

**Use Case:** Temporary, time-sensitive, or event-based data

**Location:**
```
ecommerce/src/main/java/com/example/ecommerce/
├── coupon/infrastructure/cache/CachedCouponRepository.java
├── flashsale/infrastructure/cache/flashsale/CachedFlashSaleRepository.java
└── review/infrastructure/cache/review/CachedReviewRepository.java
```

**Example:**
```java
// CachedCouponRepository.java - TTL based on coupon validity
private void cacheCoupon(Coupon coupon) {
    Duration ttl = Duration.between(LocalDateTime.now(), coupon.getValidUntil());
    redisTemplate.opsForValue().set(cacheKey, coupon, ttl);
}
```

---

### 6. **Atomic Counters (Redis Lua Scripts)**
**Description:** Prevents race conditions during concurrent updates using Redis atomic operations.

**Implementation:**
- **Stock Decrement** - `CachedProductRepository.java` (uses Lua script for atomic stock reduction)

**Use Case:** Concurrency-safe operations, preventing overselling

**Location:**
```
ecommerce/src/main/java/com/example/ecommerce/product/infrastructure/cache/product/CachedProductRepository.java
```

**Example:**
```java
// Atomic stock decrement using Lua script
private static final String ATOMIC_DECREMENT_SCRIPT = 
    "local stock = tonumber(redis.call('GET', KEYS[1]) or '0') " +
    "local quantity = tonumber(ARGV[1]) " +
    "if stock >= quantity then " +
    "    redis.call('DECRBY', KEYS[1], quantity) " +
    "    return 1 " +
    "else " +
    "    return 0 " +
    "end";
```

---

### 7. **Refresh-Ahead**
**Description:** Proactively refreshes cache before expiration to prevent cache misses for frequently accessed data.

**Implementation:**
- **Popular Products** - `PopularProductRefreshJob.java` (scheduled refresh every 2 minutes)

**Use Case:** High-traffic data that should always be cache-warm

**Location:**
```
ecommerce/src/main/java/com/example/ecommerce/product/infrastructure/sync/PopularProductRefreshJob.java
```

**Mechanism:**
- Tracks product views using Redis Sorted Sets
- Scheduled job refreshes top 20 popular products every 2 minutes
- Proactively reloads cache before TTL expires

**Example:**
```java
@Scheduled(fixedRate = 120000) // Every 2 minutes
public void refreshPopularProducts() {
    Set<TypedTuple<Object>> popularProducts = 
        redisTemplate.opsForZSet().reverseRangeWithScores(POPULAR_PRODUCTS_KEY, 0, 19);
    
    for (TypedTuple<Object> tuple : popularProducts) {
        Long ttl = redisTemplate.getExpire(cacheKey, TimeUnit.MINUTES);
        if (ttl <= 2) {
            // Refresh cache before expiration
            redisTemplate.opsForValue().set(cacheKey, product, PRODUCT_CACHE_TTL_MINUTES);
        }
    }
}
```

---

### 8. **Write-Around**
**Description:** Data is written only to database, skipping cache. Cache is populated on first read (lazy loading).

**Implementation:**
- **Orders** - `CachedOrderRepository.java` (create/update operations)
- **Reviews** - `CachedReviewRepository.java`

**Use Case:** Frequently created but rarely read data; prevents cache pollution

**Location:**
```
ecommerce/src/main/java/com/example/ecommerce/
├── order/infrastructure/cache/order/CachedOrderRepository.java
└── review/infrastructure/cache/review/CachedReviewRepository.java
```

**Rationale:** 
- Orders are created frequently during checkout but rarely accessed afterward
- Only popular orders end up in cache (lazy loading on read)
- Reduces unnecessary cache writes

---

## Architecture Highlights

### Repository Pattern with Strategy Injection
Each domain entity has two repository implementations:
- **DatabaseRepository** - Direct database access (no caching)
- **CachedRepository** - Redis-backed caching layer

Configuration decides which implementation is injected via Spring qualifiers.


### API Collection
Import the Postman collection: `E-Commerce-API.postman_collection.json`


