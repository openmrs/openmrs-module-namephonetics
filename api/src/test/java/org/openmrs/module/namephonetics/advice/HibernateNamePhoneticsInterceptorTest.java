package org.openmrs.module.namephonetics.advice;

import org.hibernate.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openmrs.PersonName;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.namephonetics.NamePhoneticsConstants;
import org.openmrs.module.namephonetics.NamePhoneticsService;

import javax.transaction.Synchronization;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Plain unit tests (no Spring context) for HibernateNamePhoneticsInterceptor, in particular that
 * it queues new vs. updated PersonNames separately and processes them from a Synchronization
 * registered on the transaction (the same pattern openmrs-module-event's
 * HibernateEventInterceptor uses), rather than Interceptor.beforeTransactionCompletion(), so that
 * a failure saving phonetics propagates normally instead of being silently swallowed by Hibernate.
 */
public class HibernateNamePhoneticsInterceptorTest {

	private HibernateNamePhoneticsInterceptor interceptor;
	private NamePhoneticsService namePhoneticsService;

	@BeforeEach
	public void setup() {
		interceptor = new HibernateNamePhoneticsInterceptor();

		namePhoneticsService = mock(NamePhoneticsService.class);
		interceptor.namePhoneticsService = namePhoneticsService;

		AdministrationService administrationService = mock(AdministrationService.class);
		when(administrationService.getGlobalProperty(NamePhoneticsConstants.GIVEN_NAME_GLOBAL_PROPERTY)).thenReturn("Soundex");
		when(administrationService.getGlobalProperty(NamePhoneticsConstants.MIDDLE_NAME_GLOBAL_PROPERTY)).thenReturn("Soundex");
		when(administrationService.getGlobalProperty(NamePhoneticsConstants.FAMILY_NAME_GLOBAL_PROPERTY)).thenReturn("Soundex");
		when(administrationService.getGlobalProperty(NamePhoneticsConstants.FAMILY_NAME2_GLOBAL_PROPERTY)).thenReturn("Soundex");
		interceptor.administrationService = administrationService;
	}

	/**
	 * Registers a Synchronization on a mock Transaction via interceptor.afterTransactionBegin(),
	 * and returns the Synchronization that was registered so the test can drive its callbacks.
	 */
	private Synchronization registerSynchronization() {
		Transaction tx = mock(Transaction.class);
		ArgumentCaptor<Synchronization> captor = ArgumentCaptor.forClass(Synchronization.class);
		interceptor.afterTransactionBegin(tx);
		verify(tx).registerSynchronization(captor.capture());
		return captor.getValue();
	}

	@Test
	public void beforeCompletion_shouldSaveQueuedNewPersonNamesWithoutDeletingExistingPhonetics() {
		PersonName pn = new PersonName("Wolfgang", "Amadeus", "Mozart");
		Synchronization sync = registerSynchronization();
		interceptor.onSave(pn, null, null, null, null);

		sync.beforeCompletion();

		verify(namePhoneticsService).savePhoneticsForNewPersonName(eq(pn), eq("Soundex"), eq("Soundex"), eq("Soundex"), eq("Soundex"));
		verify(namePhoneticsService, never()).savePhoneticsForPersonName(any(), any(), any(), any(), any());
	}

	@Test
	public void beforeCompletion_shouldSaveQueuedUpdatedPersonNamesByDeletingThenInserting() {
		PersonName pn = new PersonName("Wolfgang", "Amadeus", "Mozart");
		Synchronization sync = registerSynchronization();
		interceptor.onFlushDirty(pn, null, null, null, null, null);

		sync.beforeCompletion();

		verify(namePhoneticsService).savePhoneticsForPersonName(eq(pn), eq("Soundex"), eq("Soundex"), eq("Soundex"), eq("Soundex"));
		verify(namePhoneticsService, never()).savePhoneticsForNewPersonName(any(), any(), any(), any(), any());
	}

	@Test
	public void beforeCompletion_shouldTreatANameAsNewIfItWasEverAddedViaOnSaveInTheSameTransaction() {
		// e.g. a second flush within the same transaction dirty-checks a name that was already
		// queued as new by an earlier flush's onSave - it still has no existing phonetics saved yet
		PersonName pn = new PersonName("Wolfgang", "Amadeus", "Mozart");
		Synchronization sync = registerSynchronization();
		interceptor.onSave(pn, null, null, null, null);
		interceptor.onFlushDirty(pn, null, null, null, null, null);

		sync.beforeCompletion();

		verify(namePhoneticsService, times(1)).savePhoneticsForNewPersonName(eq(pn), any(), any(), any(), any());
		verify(namePhoneticsService, never()).savePhoneticsForPersonName(any(), any(), any(), any(), any());
	}

	@Test
	public void beforeCompletion_shouldPropagateFailureFromPhoneticsSaveInsteadOfSwallowingIt() {
		PersonName pn = new PersonName("Ludwig", "", "Beethoven");
		Synchronization sync = registerSynchronization();
		interceptor.onSave(pn, null, null, null, null);
		doThrow(new RuntimeException("deadlock inserting into name_phonetics")).when(namePhoneticsService)
				.savePhoneticsForNewPersonName(eq(pn), any(), any(), any(), any());

		assertThrows(RuntimeException.class, sync::beforeCompletion);
	}

	@Test
	public void afterCompletion_shouldClearQueuedPersonNamesSoTheyAreNotProcessedByALaterTransaction() {
		PersonName pn = new PersonName("Wolfgang", "Amadeus", "Mozart");
		Synchronization firstTxSync = registerSynchronization();
		interceptor.onSave(pn, null, null, null, null);

		// simulate the first transaction ending (e.g. rolled back) without ever calling beforeCompletion
		firstTxSync.afterCompletion(0);

		// a later, unrelated transaction should not see the earlier queued name
		Synchronization secondTxSync = registerSynchronization();
		secondTxSync.beforeCompletion();

		verify(namePhoneticsService, never()).savePhoneticsForNewPersonName(any(), any(), any(), any(), any());
	}
}
