package com.br.products.presentation.searchproduct.viewmodel

import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.br.infra.coroutines.MutableSingleLiveEvent
import com.br.products.domain.usecase.terms.GetTermsHistoryUseCase
import com.br.products.domain.usecase.terms.SaveTermsHistoryUseCase
import com.br.products.presentation.searchproduct.udf.SearchProductUiAction
import com.br.products.presentation.searchproduct.udf.SearchProductUiSideEffect
import com.br.products.presentation.searchproduct.udf.SearchProductUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class SearchProductViewModel(
    private val getTermsHistoryUseCase: GetTermsHistoryUseCase,
    private val addSearchTermUseCase: SaveTermsHistoryUseCase
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<SearchProductUiState>(SearchProductUiState.OnLoadingState)
    val uiState get() = _uiState.asStateFlow()

    private val _uiSideEffect = MutableSingleLiveEvent<SearchProductUiSideEffect>()
    val uiSideEffect get() = _uiSideEffect.asSingleEvent()

    private var originalTerms = listOf<String>()

    init {
        getSearchTerms()
    }

    fun handleAction(action: SearchProductUiAction) {
        when (action) {
            is SearchProductUiAction.OnClickSearchAction -> {
                // TODO: Log event to click on search
                checkInternetConnection(action.networkAvailable) {
                    saveSearchTerm(action.productName)
                    setNavigateToProductsSideEffect(action.productName)
                }
            }

            is SearchProductUiAction.OnTextChangedAction -> {
                filterTerms(action.productName)
                setSearchButtonState(action.productName)
                updateProductName(action.productName)
            }

            is SearchProductUiAction.OnCancelSearchAction -> {
                _uiState.value = SearchProductUiState.OnResumeState(
                    getCurrentUiModel().copy(
                        productName = "",
                        isSearchButtonEnabled = ImeAction.None,
                        productsHistory = originalTerms
                    )
                )
            }
        }
    }

    private fun filterTerms(productName: String) {
        if (productName.isEmpty()) {
            _uiState.value = SearchProductUiState.OnResumeState(
                getCurrentUiModel().copy(
                    productsHistory = originalTerms
                )
            )
        } else {
            _uiState.value = SearchProductUiState.OnResumeState(
                getCurrentUiModel().copy(
                    productsHistory = originalTerms.filter {
                        it.contains(
                            productName,
                            ignoreCase = true
                        )
                    }
                )
            )
        }
    }

    private fun checkInternetConnection(isAvailable: Boolean, doOnConnection: () -> Unit) {
        if (isAvailable) {
            doOnConnection()
        } else {
            _uiSideEffect.emit(
                SearchProductUiSideEffect.OnShowToastEffect
            )
        }
    }

    private fun saveSearchTerm(term: String) {
        viewModelScope.launch {
            if (term.isNotEmpty()) {
                addSearchTermUseCase(term)
            }
        }
    }

    private fun setSearchButtonState(term: String) {
        _uiState.value = SearchProductUiState.OnResumeState(
            getCurrentUiModel().copy(
                isSearchButtonEnabled = if (term.isNotEmpty()) ImeAction.Search else ImeAction.None
            )
        )
    }

    private fun getSearchTerms() {
        viewModelScope.launch {
            getTermsHistoryUseCase()
                .onStart {
                    _uiState.value = SearchProductUiState.OnLoadingState
                }
                .collect {
                    originalTerms = it
                    _uiState.value = SearchProductUiState.OnResumeState(
                        getCurrentUiModel().copy(
                            productsHistory = it
                        )
                    )
                }
        }
    }

    private fun updateProductName(productName: String) {
        _uiState.value = SearchProductUiState.OnResumeState(
            getCurrentUiModel().copy(
                productName = productName,
            )
        )
    }

    private fun setNavigateToProductsSideEffect(productName: String) {
        _uiSideEffect.emit(
            SearchProductUiSideEffect.OnNavigateToProductDetailsEffect(
                productName
            )
        )
    }

    private fun getCurrentUiModel() = checkNotNull(_uiState.value).uiModel
}