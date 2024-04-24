package com.br.products.domain.usecase.products

import androidx.paging.PagingData
import com.br.products.domain.model.ProductItemDomain
import com.br.products.domain.repository.ProductsRepository
import kotlinx.coroutines.flow.Flow

class GetProductsUseCaseImpl(private val repository: ProductsRepository) : GetProductsUseCase {
    override fun getProducts(query: String): Flow<PagingData<ProductItemDomain>> {
        return repository.getProductsPagingData(query)
    }
}