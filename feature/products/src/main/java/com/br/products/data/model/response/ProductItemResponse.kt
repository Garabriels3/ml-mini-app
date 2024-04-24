package com.br.products.data.model.response

import com.squareup.moshi.Json

data class ProductItemResponse(
    @Json(name = "id")
    val id: String,
    @Json(name = "title")
    val title: String,
    @Json(name = "price")
    val price: Double,
    @Json(name = "thumbnail")
    val thumbnail: String,
    @Json(name = "condition")
    val condition: String,
    @Json(name = "available_quantity")
    val availableQuantity: Int,
    @Json(name = "shipping")
    val shipping: ShippingResponse,
    val total: Int? = 0
)

data class ShippingResponse(
    @Json(name = "free_shipping")
    val freeShipping: Boolean,
)