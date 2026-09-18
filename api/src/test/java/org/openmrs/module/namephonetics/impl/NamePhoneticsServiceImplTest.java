package org.openmrs.module.namephonetics.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.PersonName;
import org.openmrs.module.namephonetics.NamePhonetic;
import org.openmrs.module.namephonetics.db.NamePhoneticsDAO;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Plain unit tests (no Spring context) for the delete-then-insert vs insert-only phonetics save
 * behavior, without needing a full database.
 */
public class NamePhoneticsServiceImplTest {

	private NamePhoneticsServiceImpl service;
	private NamePhoneticsDAO dao;

	@BeforeEach
	public void setup() {
		dao = mock(NamePhoneticsDAO.class);
		service = new NamePhoneticsServiceImpl();
		service.setDao(dao);
		service.registerProcessor("Soundex", "org.apache.commons.codec.language.Soundex");
	}

	@Test
	public void savePhoneticsForNewPersonName_shouldNotDeleteExistingPhoneticsBeforeInserting() {
		PersonName pn = new PersonName("Wolfgang", "Amadeus", "Mozart");
		pn.setPersonNameId(123);

		service.savePhoneticsForNewPersonName(pn, "Soundex", "Soundex", "Soundex", null);

		verify(dao, never()).deleteNamePhonetics(any(PersonName.class));
		verify(dao, times(3)).saveNamePhonetic(any(NamePhonetic.class));
	}

	@Test
	public void savePhoneticsForPersonName_shouldStillDeleteExistingPhoneticsBeforeInserting() {
		PersonName pn = new PersonName("Wolfgang", "Amadeus", "Mozart");
		pn.setPersonNameId(123);

		service.savePhoneticsForPersonName(pn, "Soundex", "Soundex", "Soundex", null);

		verify(dao, times(1)).deleteNamePhonetics(pn);
		verify(dao, times(3)).saveNamePhonetic(any(NamePhonetic.class));
	}
}
