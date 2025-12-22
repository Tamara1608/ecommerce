# Caching Strategy Documentation

This document describes the Redis caching strategies implemented for **Product** and **Flash Sale Event** entities in the e-commerce application.

---

## Table of Contents

1. [Product Caching](#product-caching)
2. [Flash Sale Event Caching](#flash-sale-event-caching)
3. [Cache Keys Reference](#cache-keys-reference)
4. [API Endpoints](#api-endpoints)
5. [Performance Optimizations](#performance-optimizations)

---

## Product Caching

### Overview

Product caching uses a **multi-layered approach** to optimize performance and avoid N+1 query problems:

- **Metadata Caching**: Product metadata (DTO) stored separately from stock
- **Stock Caching**: Current stock values cached in Redis (no DB queries)
- **Bulk List Caching**: All products list cached to avoid N+1 problems

### Cache Structure

#### 1. Individual Product Metadata
- **Key Pattern**: `products::{productId}`
- **Value Type**: `ProductDTO` (JSON serialized)
- **TTL**: No expiration (manual invalidation)
- **Purpose**: Fast retrieval of product metadata without JPA lazy-loading issues

**ProductDTO Fields:**
```java
- id
- name
- description
- price
- discount
- imageLink
```

#### 2. Stock Values
- **Key Pattern**: `stock:{productId}`
- **Value Type**: `Integer` (currentStock only)
- **TTL**: No expiration (manual invalidation)
- **Purpose**: Fast stock retrieval from Redis (no DB queries)

#### 3. All Product IDs Set
- **Key**: `products:all_ids`
- **Value Type**: Redis Set
- **Purpose**: Track all cached product IDs

#### 4. All Products List (Optimized)
- **Key**: `products:all_list`
- **Value Type**: `List<ProductDTO>` (JSON serialized)
- **TTL**: No expiration (invalidated on create/update/delete)
- **Purpose**: **Single Redis call** to fetch all products (solves N+1 problem)

### How It Works

#### Fetching All Products from Cache

```java
// Service method: getAllProductsFromCache()

// 1. Try to get cached list (single Redis call)
List<ProductDTO> cachedDTOs = redis.get("products:all_list");

if (cachedDTOs != null) {
    // 2. Batch fetch all stock values from Redis (single multiGet call)
    List<String> stockKeys = ["stock:1", "stock:2", ...];
    List<Integer> stocks = redis.multiGet(stockKeys);
    
    // 3. Enrich DTOs with stock (only from Redis, no DB query)
    return enrichDTOsWithStock(cachedDTOs, stocks);
}

// Cache miss: Load from DB, cache, and return
List<Product> products = productRepository.findAllWithStock();
cacheAllProducts(products);
return convertProductsToResponseDTOs(products);
```

**Performance**: 
- **Cache Hit**: 2 Redis calls (1 for DTO list, 1 batch for all stock)
- **Cache Miss**: 1 DB query + 2 Redis calls (cache write)
- **No N+1 Problem**: All stock fetched in single batch operation

#### ProductResponseDTO Structure

```java
public class ProductResponseDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer discount;
    private String imageLink;
    private Integer currentStock;  // Only currentStock from Redis
}
```

**Note**: Only `currentStock` is included (from Redis). `totalStock` is not cached or returned.

#### Example: Cache Flow

```java
// GET /products (uses getAllProductsFromCache)

// Step 1: Check cache
List<ProductDTO> dtos = redis.get("products:all_list");
// Returns: [ProductDTO(id=1, name="Laptop", ...), ...]

// Step 2: Batch fetch stock from Redis
List<Integer> stocks = redis.multiGet(["stock:1", "stock:2", "stock:3"]);
// Returns: [10, 5, 20]

// Step 3: Enrich and return
List<ProductResponseDTO> response = enrichDTOsWithStock(dtos, stocks);
// Returns: [ProductResponseDTO(id=1, name="Laptop", currentStock=10, ...), ...]
```

### Cache Invalidation

- **Create Product**: Cache new product + invalidate `products:all_list`
- **Update Product**: Update cache + invalidate `products:all_list`
- **Delete Product**: Remove from cache + remove from set + invalidate `products:all_list`

### Why ProductDTO Instead of Product Entity?

Using `ProductDTO` avoids JPA serialization issues:
- No lazy-loaded relationships (`categories`, `flashSales`, `priceHistory`)
- No circular references
- Smaller memory footprint
- Faster serialization/deserialization

---

## Flash Sale Event Caching

### Overview

Flash Sale Event caching uses **TTL (Time-To-Live) based expiration** to automatically remove expired events from cache. This ensures:

- Active flash sales are always cached
- Expired flash sales are automatically removed
- No manual cleanup required

### Cache Structure

#### 1. Individual Flash Sale Event
- **Key Pattern**: `flashsale:event:{flashSaleId}`
- **Value Type**: `FlashSaleEvent` (JSON serialized)
- **TTL**: `Duration.between(now, endDate)` (auto-expires when event ends)
- **Purpose**: Fast retrieval of flash sale details

#### 2. Active Flash Sales List
- **Key**: `flashsales:active`
- **Value Type**: `List<FlashSaleEvent>` (JSON serialized)
- **TTL**: Shortest TTL among all active events
- **Purpose**: Quick access to all active flash sales

#### 3. Flash Sale Products Mapping
- **Key Pattern**: `flashsale:products:{flashSaleId}`
- **Value Type**: `Set<Long>` (product IDs)
- **TTL**: Same as flash sale event TTL
- **Purpose**: Track which products are in a flash sale

#### 4. Product-to-Flash-Sale Reverse Mapping
- **Key Pattern**: `flashsale:product:{productId}`
- **Value Type**: `Long` (flash sale ID)
- **TTL**: Same as flash sale event TTL
- **Purpose**: Fast lookup: "Is this product in a flash sale?"

### How It Works

#### Creating a Flash Sale

```java
// 1. Save to database
FlashSaleEvent saved = repository.save(event);

// 2. Calculate TTL until endDate
Duration ttl = Duration.between(now, saved.getEndDate());

// 3. Cache event with TTL
redis.set("flashsale:event:{id}", saved, ttl);

// 4. Cache product mappings
redis.set("flashsale:products:{id}", productIds, ttl);
for (productId : productIds) {
    redis.set("flashsale:product:{productId}", flashSaleId, ttl);
}

// 5. Invalidate active list (will be rebuilt on next access)
redis.delete("flashsales:active");
```

#### Updating a Flash Sale

**Direct Update Strategy** (no cache miss window):

```java
// 1. Update in database
FlashSaleEvent updated = repository.save(event);

// 2. Update cache directly (overwrites existing)
Duration newTTL = Duration.between(now, updated.getEndDate());
redis.set("flashsale:event:{id}", updated, newTTL);

// 3. Update product mappings (handles additions/removals)
updateFlashSaleProductMappings(updated, newTTL);

// 4. Invalidate active list
redis.delete("flashsales:active");
```

**Product Mapping Update Logic:**
- Compare old product IDs with new product IDs
- Remove mappings for products no longer in flash sale
- Add/update mappings for new products

#### Fetching Active Flash Sales

```java
// 1. Check cache first
List<FlashSaleEvent> cached = redis.get("flashsales:active");

if (cached != null) {
    // Filter to ensure still active (cache might not have expired)
    return cached.stream().filter(this::isActive).toList();
}

// 2. Load from DB, filter active, cache with TTL
List<FlashSaleEvent> active = loadFromDB().filter(isActive);
redis.set("flashsales:active", active, shortestTTL);
return active;
```

#### Checking if Product is in Flash Sale

```java
// Fast lookup using reverse mapping
Long flashSaleId = redis.get("flashsale:product:{productId}");

if (flashSaleId != null) {
    FlashSaleEvent event = redis.get("flashsale:event:{flashSaleId}");
    return isActive(event);
}

return false;
```

### Cache Invalidation

- **Update**: Direct cache update (no deletion)
- **Delete**: Remove all related cache entries:
  - `flashsale:event:{id}`
  - `flashsale:products:{id}`
  - `flashsale:product:{productId}` (for all products)
  - `flashsales:active`

### TTL Calculation

```java
LocalDateTime now = LocalDateTime.now();
Duration ttl = Duration.between(now, event.getEndDate());

// Only cache if TTL > 0 (event hasn't ended)
if (ttl.getSeconds() > 0) {
    redis.set(key, value, ttl);
}
```

---

## Cache Keys Reference

### Product Cache Keys

| Key Pattern | Type | Example | Purpose |
|------------|------|---------|---------|
| `products::{productId}` | String | `products::1` | Product metadata (DTO) |
| `stock:{productId}` | String | `stock:1` | Product stock value |
| `products:all_ids` | Set | `products:all_ids` | All product IDs |
| `products:all_list` | String | `products:all_list` | All products list (DTOs) |

### Flash Sale Cache Keys

| Key Pattern | Type | Example | Purpose |
|------------|------|---------|---------|
| `flashsale:event:{id}` | String | `flashsale:event:1` | Flash sale event |
| `flashsales:active` | String | `flashsales:active` | Active flash sales list |
| `flashsale:products:{id}` | String | `flashsale:products:1` | Product IDs in flash sale |
| `flashsale:product:{productId}` | String | `flashsale:product:5` | Flash sale ID for product |

---

## API Endpoints

### Product Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/products` | Get all products (from cache) |
| `GET` | `/products/{id}` | Get product by ID (from cache) |
| `POST` | `/products` | Create product (caches automatically) |
| `PUT` | `/products/{id}` | Update product (updates cache) |
| `DELETE` | `/products/{id}` | Delete product (removes from cache) |

### Flash Sale Endpoints

#### CRUD Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/flash-sale-event` | Get all flash sales |
| `GET` | `/flash-sale-event/{id}` | Get flash sale by ID |
| `GET` | `/flash-sale-event/active` | Get all active flash sales |
| `GET` | `/flash-sale-event/active/{id}` | Get active flash sale by ID |
| `POST` | `/flash-sale-event` | Create flash sale (caches with TTL) |
| `PUT` | `/flash-sale-event/{id}` | Update flash sale (updates cache) |
| `DELETE` | `/flash-sale-event/{id}` | Delete flash sale (removes from cache) |

#### Query Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/flash-sale-event/check/active` | Check if any flash sale is active |
| `GET` | `/flash-sale-event/product/{productId}/check` | Check if product is in flash sale |
| `GET` | `/flash-sale-event/product/{productId}` | Get flash sale for product |

#### Purchase Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/flashsale/buy` | Buy products during flash sale (requires auth) |

---

## Performance Optimizations

### N+1 Problem Solutions

#### Problem: Fetching All Products
**Before:**
```
1. Get all product IDs (1 Redis call)
2. For each product:
   - Get metadata (N Redis calls)
   - Get stock (N Redis calls)
Total: 1 + 2N Redis calls
```

**After:**
```
1. Get all products list (1 Redis call)
2. Batch get all stock values (1 Redis multiGet call)
Total: 2 Redis calls (no DB queries!)
```

#### Stock Enrichment (No DB Queries)
**Current Implementation:**
```
1. Get ProductDTOs from cache (1 Redis call)
2. Batch get all currentStock values (1 Redis multiGet call)
3. Enrich DTOs with stock from Redis only
Total: 2 Redis calls, 0 DB queries
```

**Key Improvement**: `enrichDTOsWithStock()` only fetches from Redis, eliminating DB queries for stock information.

### Cache Update Strategy

**Flash Sale Updates:**
- **Direct Update**: Overwrites cache entry instead of delete + recreate
- **Benefits**: 
  - No cache miss window
  - Faster updates
  - Better consistency

**Product Updates:**
- **Invalidate List**: Delete `products:all_list` on any change
- **Rebuild on Demand**: List is rebuilt on next `getAllProducts()` call
- **Benefits**: 
  - Always fresh data
  - No stale entries

### TTL-Based Expiration

**Flash Sale Events:**
- Automatically expire when `endDate` is reached
- No manual cleanup needed
- Redis handles expiration automatically

**Benefits:**
- Memory efficient (expired events removed automatically)
- No stale data
- Simplified cache management

---

## Example Usage

### Fetching All Products with Cache

```java
// Service method
List<ProductResponseDTO> products = productService.getAllProductsFromCache();

// Behind the scenes:
// 1. Redis GET "products:all_list" → List<ProductDTO>
// 2. Redis MGET ["stock:1", "stock:2", ...] → List<Integer> (currentStock)
// 3. Enrich DTOs with stock → List<ProductResponseDTO>
//    - No DB queries, all from Redis!
```

### Checking if Product is in Flash Sale

```java
// Service method
boolean inFlashSale = flashSaleService.isProductInActiveFlashSale(productId);

// Behind the scenes:
// 1. Redis GET "flashsale:product:{productId}" → flashSaleId
// 2. Redis GET "flashsale:event:{flashSaleId}" → FlashSaleEvent
// 3. Check if event is active (startDate < now < endDate)
```

### Creating a Flash Sale

```java
// Controller
POST /flash-sale-event
{
  "name": "Summer Sale",
  "startDate": "2024-06-01T00:00:00",
  "endDate": "2024-06-30T23:59:59",
  "products": [1, 2, 3]
}

// Behind the scenes:
// 1. Save to database
// 2. Calculate TTL = Duration.between(now, endDate)
// 3. Cache event with TTL
// 4. Cache product mappings with TTL
// 5. Invalidate active list
```

---

## Best Practices

1. **Always use DTOs for caching** - Avoids JPA serialization issues
2. **Separate stock from metadata** - Enables atomic operations
3. **Use TTL for time-sensitive data** - Flash sales, coupons, etc.
4. **Batch operations when possible** - Use `multiGet()` to avoid N+1
5. **Invalidate related caches** - When updating, invalidate dependent caches
6. **Direct updates over delete+recreate** - Reduces cache miss windows

---

## Troubleshooting

### Cache Not Updating
- Check if cache invalidation is called after updates
- Verify TTL hasn't expired (for flash sales)
- Check Redis connection

### N+1 Performance Issues
- Ensure `products:all_list` is cached
- Use `multiGet()` for batch operations
- Check Redis call counts in logs

### Stale Data
- Verify cache invalidation logic
- Check TTL settings for time-sensitive data
- Ensure updates call cache update methods

---

## Notes

- All cache operations use `RedisTemplate<String, Object>`
- Serialization: `GenericJackson2JsonRedisSerializer`
- Cache is preloaded on application startup via `ProductPreload`
- Flash sale cache is built on-demand (no preloading)

