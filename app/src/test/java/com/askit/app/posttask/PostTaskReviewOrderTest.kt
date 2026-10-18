package com.askit.app.posttask

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PostTaskReviewOrderTest {
    @Test fun noErrorsMeansNothingToReveal() {
        assertNull(firstInvalidPostTaskField(emptySet()))
    }

    @Test fun theEarliestFieldInTheFormWins() {
        assertEquals(
            PostTaskValidationField.TITLE,
            firstInvalidPostTaskField(
                setOf(PostTaskValidationField.AGE_PREFERENCE, PostTaskValidationField.DATE, PostTaskValidationField.TITLE),
            ),
        )
        assertEquals(
            PostTaskValidationField.FIXED_BUDGET,
            firstInvalidPostTaskField(setOf(PostTaskValidationField.AGE_PREFERENCE, PostTaskValidationField.FIXED_BUDGET)),
        )
    }

    @Test fun everyValidationFieldHasAPlaceInTheReviewOrder() {
        assertEquals(PostTaskValidationField.entries.toSet(), POST_TASK_REVIEW_ORDER.toSet())
        assertEquals(PostTaskValidationField.entries.size, POST_TASK_REVIEW_ORDER.size)
    }

    @Test fun anInvalidFormRevealsTheFirstBrokenField() {
        val viewModel = PostTaskViewModel()
        viewModel.review()
        assertEquals(PostTaskValidationField.CATEGORY, firstInvalidPostTaskField(viewModel.validationErrors()))
    }
}
