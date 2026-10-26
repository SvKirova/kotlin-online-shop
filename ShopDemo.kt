package com.example.shopdemo

import java.util.Locale

fun formatPln(amount: Double): String = String.format(Locale.US, "%.2f PLN", amount)

interface Downloadable {
    fun downloadInfo(): String
}

abstract class Product(
    val id: String,
    val name: String,
    val price: Double,
    stockQuantity: Int
) {
    init {
        require(id.isNotBlank()) { "Product ID must not be blank." }
        require(name.isNotBlank()) { "Product name must not be blank." }
        require(price >= 0.0) { "Price must not be negative." }
        require(stockQuantity >= 0) { "Stock must not be negative." }
    }
    var stock: Int = stockQuantity
        private set

    abstract fun description(): String
    internal fun removeStock(quantity: Int): Boolean {
        if (quantity !in 1..stock) return false
        stock -= quantity
        return true
    }
}

class PhysicalProduct(
    id: String,
    name: String,
    price: Double,
    stock: Int,
    val weightKg: Double
) : Product(id, name, price, stock) {

    init {
        require(weightKg > 0.0) { "Weight must be positive." }
    }

    override fun description(): String =
        "Physical product '$name' (${formatPln(price)}), weight: ${String.format(Locale.US, "%.2f", weightKg)} kg, in stock: $stock"
}

class DigitalProduct(
    id: String,
    name: String,
    price: Double,
    licences: Int,
    val fileFormat: String
) : Product(id, name, price, licences), Downloadable {

    init {
        require(fileFormat.isNotBlank()) { "File format must not be blank." }
    }

    override fun description(): String =
        "Digital product '$name' (${formatPln(price)}), format: $fileFormat, licences: $stock"

    override fun downloadInfo(): String =
        "Download '$name' as a .${fileFormat.lowercase()} file."
}

data class SaleRecord(
    val productId: String,
    val quantity: Int,
    val totalPrice: Double
) {
    fun display(): String =
        "product=$productId, quantity=$quantity, total=${formatPln(totalPrice)}"
}

class Shop {
    private val products = linkedMapOf<String, Product>()

    fun addProduct(product: Product): Boolean {
        if (products.containsKey(product.id)) return false
        products[product.id] = product
        return true
    }

    fun findProduct(productId: String): Product? = products[productId]

    // Polymorphism
    fun listProducts() {
        for (product in products.values) {
            println("  ${product.id}: ${product.description()}")
            if (product is Downloadable) {
                println("    ${product.downloadInfo()}")
            }
        }
    }

    fun sell(productId: String, quantity: Int): SaleRecord? {
        val product = products[productId] ?: return null
        if (quantity <= 0) return null
        if (!product.removeStock(quantity)) return null
        return SaleRecord(productId, quantity, product.price * quantity)
    }
}

fun printSaleResult(label: String, record: SaleRecord?) {
    if (record != null) println("  $label -> ${record.display()}")
    else println("  $label -> Rejected sale (check the product ID and quantity)")
}

fun main() {
    val shop = Shop()

    println("Adding products")
    val keyboard = PhysicalProduct("P-001", "Mechanical keyboard", 299.90, 10, 1.1)
    val course = DigitalProduct("D-001", "Kotlin for beginners course", 49.00, 3, "MP4")
    println("  Add keyboard: ${shop.addProduct(keyboard)}")
    println("  Add course:   ${shop.addProduct(course)}")

    println("\nPolymorphism")
    shop.listProducts()

    println("\nSales")
    println("  Keyboard stock before: ${keyboard.stock}")
    printSaleResult("Sell 2 keyboards", shop.sell("P-001", 2))
    println("  Keyboard stock after:  ${keyboard.stock}")

    println("  Course licences before: ${course.stock}")
    printSaleResult("Sell 1 course", shop.sell("D-001", 1))
    println("  Course licences after:  ${course.stock}")

    println("\nRejected sales")
    printSaleResult("Excessive quantity (100 keyboards)", shop.sell("P-001", 100))
    printSaleResult("0 quantity", shop.sell("P-001", 0))
    printSaleResult("Negative quantity", shop.sell("P-001", -5))
    printSaleResult("Unknown ID", shop.sell("X-999", 1))
    println("  Keyboard stock after rejections: ${keyboard.stock}")
    println("  Course licences after rejections: ${course.stock}")

    println("\nDuplicate ID")
    val duplicate = PhysicalProduct("P-001", "Another keyboard", 150.00, 5, 0.9)
    println("  Add duplicate P-001: ${shop.addProduct(duplicate)}")
    println("  Product under P-001 is still: ${shop.findProduct("P-001")?.name}")

    println("\nFinal catalogue")
    shop.listProducts()
}