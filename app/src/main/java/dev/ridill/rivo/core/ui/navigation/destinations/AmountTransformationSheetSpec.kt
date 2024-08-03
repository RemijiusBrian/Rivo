package dev.ridill.rivo.core.ui.navigation.destinations

import android.os.Parcelable
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.navigateUpWithResult
import dev.ridill.rivo.transactions.domain.model.AmountTransformation
import dev.ridill.rivo.transactions.presentation.amountTransformation.AmountTransformationSheet
import dev.ridill.rivo.transactions.presentation.amountTransformation.AmountTransformationViewModel
import kotlinx.parcelize.Parcelize
import java.util.Currency

data object AmountTransformationSheetSpec : BottomSheetSpec {

    const val TRANSFORMATION_RESULT = "TRANSFORMATION_RESULT"

    override val route: String = "amount_transformation_sheet"

    override val labelRes: Int = R.string.destination_amount_transformation_selection

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry,
        appCurrencyPreference: Currency
    ) {
        val viewModel: AmountTransformationViewModel = hiltViewModel(navBackStackEntry)
        val selectedTransformation by viewModel.selectedTransformation.collectAsStateWithLifecycle()
        val factorInput = viewModel.factorInput.collectAsStateWithLifecycle()

        AmountTransformationSheet(
            onDismiss = navController::navigateUp,
            selectedTransformation = selectedTransformation,
            onTransformationSelect = viewModel::onTransformationSelect,
            factorInput = { factorInput.value },
            onFactorInputChange = viewModel::onFactorChange,
            onTransformClick = {
                navController.navigateUpWithResult(
                    TRANSFORMATION_RESULT,
                    TransformationResult(
                        transformation = selectedTransformation,
                        factor = factorInput.value
                    )
                )
            }
        )
    }
}

@Parcelize
data class TransformationResult(
    val transformation: AmountTransformation,
    val factor: String
) : Parcelable