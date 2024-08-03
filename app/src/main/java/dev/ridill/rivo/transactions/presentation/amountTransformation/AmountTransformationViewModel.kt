package dev.ridill.rivo.transactions.presentation.amountTransformation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.transactions.domain.model.AmountTransformation
import javax.inject.Inject

@HiltViewModel
class AmountTransformationViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val selectedTransformation = savedStateHandle
        .getStateFlow(SELECTED_TRANSFORMATION, AmountTransformation.DIVIDE_BY)

    val factorInput = savedStateHandle
        .getStateFlow(FACTOR_INPUT, String.Empty)

    fun onTransformationSelect(transformation: AmountTransformation) {
        savedStateHandle[SELECTED_TRANSFORMATION] = transformation
    }

    fun onFactorChange(value: String) {
        savedStateHandle[FACTOR_INPUT] = value
    }
}

private const val SELECTED_TRANSFORMATION = "SELECTED_TRANSFORMATION"
private const val FACTOR_INPUT = "FACTOR_INPUT"