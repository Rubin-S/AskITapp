package com.askit.app.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.MutableCreationExtras
import com.askit.app.inbox.InboxViewModel
import com.askit.app.jobs.JobsViewModel
import com.askit.app.profile.ProfileViewModel
import com.askit.app.providerdashboard.ProviderDashboardViewModel
import com.askit.app.session.SessionProfile
import com.askit.app.variant.AppVariant
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class AppContainerTest {
    private fun container(clientPreview: Boolean = false) = AppContainer(
        config = AppConfig(backendBaseUrl = null, clientPreview = clientPreview),
        content = AppVariant,
        previewStorage = { null },
        authSessionStore = { null },
    )

    private inline fun <reified T : ViewModel> AppContainer.create(graph: SessionGraph): T =
        viewModelFactory(graph).create(T::class.java, MutableCreationExtras())

    @Test fun viewModelsThatShareAccountStateGetTheGraphsStores() {
        val container = container()
        val graph = container.newSessionGraph()
        val jobs = container.create<JobsViewModel>(graph)
        assertSame(graph.jobs, jobs.store)
        assertSame(graph.profileStore, jobs.profileStore)
        container.create<ProfileViewModel>(graph)
        container.create<ProviderDashboardViewModel>(graph)
        container.create<InboxViewModel>(graph)
    }

    @Test fun theViewAsOtherPartyToolExistsOnlyInClientPreview() {
        assertFalse(container(clientPreview = false).newSessionGraph().jobs.allowViewAsOtherParty)
        assertTrue(container(clientPreview = true).newSessionGraph().jobs.allowViewAsOtherParty)
    }

    @Test fun aNewSessionGraphSharesNothingWithTheOldOne() {
        val container = container()
        val first = container.newSessionGraph()
        first.profileStore.replaceAccount(SessionProfile(accountId = "user-a"))
        val second = container.newSessionGraph()
        assertNotSame(first.profileStore, second.profileStore)
        assertNotSame(first.jobs, second.jobs)
        assertNotSame(first.tasks, second.tasks)
        assertTrue(second.profileStore.profile.value.accountId.isBlank())
    }

    @Test fun noBackendMeansNoAuthApiOrSessionStoreIsBuilt() {
        var sessionStoresBuilt = 0
        val container = AppContainer(
            config = AppConfig(backendBaseUrl = null, clientPreview = false),
            content = AppVariant,
            previewStorage = { null },
            authSessionStore = { sessionStoresBuilt++; null },
        )
        container.create<com.askit.app.auth.AuthViewModel>(container.newSessionGraph())
        assertTrue(sessionStoresBuilt == 0)
    }
}
