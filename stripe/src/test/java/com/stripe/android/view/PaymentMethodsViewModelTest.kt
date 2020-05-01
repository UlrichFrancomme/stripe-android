package com.stripe.android.view

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.KArgumentCaptor
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.isNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.stripe.android.CustomerSession
import com.stripe.android.PaymentSession
import com.stripe.android.exception.APIException
import com.stripe.android.model.PaymentMethod
import com.stripe.android.model.PaymentMethodFixtures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PaymentMethodsViewModelTest {

    private val customerSession: CustomerSession = mock()
    private val listenerArgumentCaptor: KArgumentCaptor<CustomerSession.PaymentMethodsRetrievalListener> = argumentCaptor()
    private val viewModel = PaymentMethodsViewModel(
        application = ApplicationProvider.getApplicationContext(),
        customerSession = customerSession,
        startedFromPaymentSession = true
    )

    @Test
    fun getPaymentMethods_whenSuccess_returnsExpectedPaymentMethods() {
        var paymentMethods: List<PaymentMethod> = emptyList()
        viewModel.getPaymentMethods().observeForever {
            paymentMethods = it.getOrThrow()
        }

        verify(customerSession).getPaymentMethods(
            eq(PaymentMethod.Type.Card),
            isNull(),
            isNull(),
            isNull(),
            eq(EXPECTED_PRODUCT_USAGE),
            listenerArgumentCaptor.capture()
        )

        listenerArgumentCaptor.firstValue.onPaymentMethodsRetrieved(
            listOf(PaymentMethodFixtures.CARD_PAYMENT_METHOD)
        )

        assertThat(paymentMethods)
            .isEqualTo(listOf(PaymentMethodFixtures.CARD_PAYMENT_METHOD))
    }

    @Test
    fun getPaymentMethods_whenError_returnsExpectedException() {
        var throwable: Throwable? = null
        viewModel.getPaymentMethods().observeForever {
            throwable = it.exceptionOrNull()
        }

        verify(customerSession).getPaymentMethods(
            eq(PaymentMethod.Type.Card),
            isNull(),
            isNull(),
            isNull(),
            eq(EXPECTED_PRODUCT_USAGE),
            listenerArgumentCaptor.capture()
        )

        listenerArgumentCaptor.firstValue.onError(
            404, "error!", null
        )

        assertThat(throwable)
            .isInstanceOf(APIException::class.java)
    }

    @Test
    fun onPaymentMethodAdded_shouldUpdateSnackbarData() {
        val values: MutableList<String?> = mutableListOf()
        viewModel.snackbarData.observeForever { values.add(it) }

        viewModel.onPaymentMethodAdded(PaymentMethodFixtures.CARD_PAYMENT_METHOD)
        assertEquals("Added Visa ending in 4242", values[0])
        assertNull(values[1])
    }

    @Test
    fun onPaymentMethodRemoved_shouldUpdateSnackbarData() {
        val values: MutableList<String?> = mutableListOf()
        viewModel.snackbarData.observeForever { values.add(it) }

        viewModel.onPaymentMethodRemoved(PaymentMethodFixtures.CARD_PAYMENT_METHOD)
        assertEquals("Removed Visa ending in 4242", values[0])
        assertNull(values[1])
    }

    private companion object {
        private val EXPECTED_PRODUCT_USAGE = setOf(
            PaymentSession.PRODUCT_TOKEN,
            PaymentMethodsActivity.PRODUCT_TOKEN
        )
    }
}
