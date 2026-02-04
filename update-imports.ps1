# Script to update all package declarations and imports after restructuring

$replacements = @{
    # Product module
    'com.example.ecommerce.Product.entity.Product' = 'com.example.ecommerce.product.domain.Product'
    'com.example.ecommerce.Product.entity.Stock' = 'com.example.ecommerce.product.domain.Stock'
    'com.example.ecommerce.Product.entity.Category' = 'com.example.ecommerce.product.domain.Category'
    'com.example.ecommerce.Product.entity.ProductPriceHistory' = 'com.example.ecommerce.product.domain.ProductPriceHistory'
    'com.example.ecommerce.Product.IProductService' = 'com.example.ecommerce.product.app.IProductService'
    'com.example.ecommerce.Product.ProductService' = 'com.example.ecommerce.product.app.ProductService'
    'com.example.ecommerce.Product.IProductRepository' = 'com.example.ecommerce.product.infrastructure.persistence.product.IProductRepository'
    'com.example.ecommerce.Product.ProductTable' = 'com.example.ecommerce.product.infrastructure.persistence.product.ProductTable'
    'com.example.ecommerce.Product.CategoryTable' = 'com.example.ecommerce.product.infrastructure.persistence.category.CategoryTable'
    'com.example.ecommerce.Product.ProductDTO' = 'com.example.ecommerce.product.api.dto.ProductDTO'
    'com.example.ecommerce.Product.ProductCreateRequest' = 'com.example.ecommerce.product.api.dto.ProductCreateRequest'
    
    # FlashSale module
    'com.example.ecommerce.FlashSale.entity.FlashSaleEvent' = 'com.example.ecommerce.flashsale.domain.FlashSaleEvent'
    'com.example.ecommerce.FlashSale.FlashSaleService' = 'com.example.ecommerce.flashsale.app.FlashSaleService'
    'com.example.ecommerce.FlashSale.IFlashSaleRepository' = 'com.example.ecommerce.flashsale.infrastructure.persistence.flashsale.IFlashSaleRepository'
    'com.example.ecommerce.FlashSale.FlashSaleTable' = 'com.example.ecommerce.flashsale.infrastructure.persistence.flashsale.FlashSaleTable'
    'com.example.ecommerce.FlashSale.BuyRequest' = 'com.example.ecommerce.flashsale.api.dto.BuyRequest'
    'com.example.ecommerce.FlashSale.FlashSaleEventDTO' = 'com.example.ecommerce.flashsale.api.dto.FlashSaleEventDTO'
    'com.example.ecommerce.FlashSale.FlashSaleResponseDTO' = 'com.example.ecommerce.flashsale.api.dto.FlashSaleResponseDTO'
    'com.example.ecommerce.FlashSale.ProductBasicDTO' = 'com.example.ecommerce.flashsale.api.dto.ProductBasicDTO'
    
    # Order module
    'com.example.ecommerce.Order.entity.Order' = 'com.example.ecommerce.order.domain.Order'
    'com.example.ecommerce.Order.entity.OrderItem' = 'com.example.ecommerce.order.domain.OrderItem'
    'com.example.ecommerce.Order.IOrderService' = 'com.example.ecommerce.order.app.IOrderService'
    'com.example.ecommerce.Order.OrderService' = 'com.example.ecommerce.order.app.OrderService'
    'com.example.ecommerce.Order.IOrderRepository' = 'com.example.ecommerce.order.infrastructure.persistence.order.IOrderRepository'
    'com.example.ecommerce.Order.OrderTable' = 'com.example.ecommerce.order.infrastructure.persistence.order.OrderTable'
    
    # User module
    'com.example.ecommerce.User.entity.User' = 'com.example.ecommerce.user.domain.User'
    'com.example.ecommerce.User.IUserService' = 'com.example.ecommerce.user.app.IUserService'
    'com.example.ecommerce.User.UserService' = 'com.example.ecommerce.user.app.UserService'
    'com.example.ecommerce.User.IUserRepository' = 'com.example.ecommerce.user.infrastructure.persistence.user.IUserRepository'
    'com.example.ecommerce.User.UserTable' = 'com.example.ecommerce.user.infrastructure.persistence.user.UserTable'
    'com.example.ecommerce.User.UserDTO' = 'com.example.ecommerce.user.api.dto.UserDTO'
    'com.example.ecommerce.User.LoginRequest' = 'com.example.ecommerce.user.api.dto.LoginRequest'
    'com.example.ecommerce.User.SignupRequest' = 'com.example.ecommerce.user.api.dto.SignupRequest'
}

$srcPath = "src\main\java\com\example\ecommerce"

Write-Host "Updating imports in all Java files..."

Get-ChildItem -Path $srcPath -Filter "*.java" -Recurse | ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    $modified = $false
    
    foreach ($old in $replacements.Keys) {
        $new = $replacements[$old]
        if ($content -match [regex]::Escape($old)) {
            $content = $content -replace [regex]::Escape($old), $new
            $modified = $true
        }
    }
    
    if ($modified) {
        Set-Content -Path $_.FullName -Value $content -NoNewline
        Write-Host "Updated: $($_.FullName)"
    }
}

Write-Host "Import update complete!"

