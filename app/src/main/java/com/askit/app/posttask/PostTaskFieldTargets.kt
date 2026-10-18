package com.askit.app.posttask

import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester

/**
 * The scroll and focus targets of the Post a task form, one per field Review can point at.
 * Review asks [reveal] for the first invalid field; the sections attach the requesters.
 */
@Stable
internal class PostTaskFieldTargets {
    val category = BringIntoViewRequester()
    val title = BringIntoViewRequester()
    val details = BringIntoViewRequester()
    val workMode = BringIntoViewRequester()
    val location = BringIntoViewRequester()
    val timing = BringIntoViewRequester()
    val date = BringIntoViewRequester()
    val preferences = BringIntoViewRequester()
    val fixedBudget = BringIntoViewRequester()
    val rangeBudget = BringIntoViewRequester()
    val titleFocus = FocusRequester()
    val detailsFocus = FocusRequester()
    val fixedBudgetFocus = FocusRequester()
    val minimumBudgetFocus = FocusRequester()

    /** Scrolls [field] into view and, for text fields, focuses it after the next frame. */
    suspend fun reveal(field: PostTaskValidationField) {
        when (field) {
            PostTaskValidationField.CATEGORY -> category.bringIntoView()

            PostTaskValidationField.TITLE -> {
                title.bringIntoView()
                focusAfterFrame(titleFocus)
            }

            PostTaskValidationField.DETAILS -> {
                details.bringIntoView()
                focusAfterFrame(detailsFocus)
            }

            PostTaskValidationField.WORK_MODE -> workMode.bringIntoView()

            PostTaskValidationField.LOCATION -> location.bringIntoView()

            PostTaskValidationField.TIMING -> timing.bringIntoView()

            PostTaskValidationField.DATE -> date.bringIntoView()

            PostTaskValidationField.AGE_PREFERENCE -> preferences.bringIntoView()

            PostTaskValidationField.FIXED_BUDGET -> {
                fixedBudget.bringIntoView()
                focusAfterFrame(fixedBudgetFocus)
            }

            PostTaskValidationField.BUDGET_RANGE -> {
                rangeBudget.bringIntoView()
                focusAfterFrame(minimumBudgetFocus)
            }
        }
    }
}

@Composable
internal fun rememberPostTaskFieldTargets(): PostTaskFieldTargets = remember { PostTaskFieldTargets() }
