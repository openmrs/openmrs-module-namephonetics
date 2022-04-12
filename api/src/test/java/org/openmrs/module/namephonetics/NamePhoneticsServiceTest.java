package org.openmrs.module.namephonetics;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Date;
import java.util.List;

public class NamePhoneticsServiceTest extends BaseModuleContextSensitiveTest {

	@Autowired
	PersonService personService;

	@Autowired @Qualifier("adminService")
	AdministrationService administrationService;

	@Autowired
	NamePhoneticsService namePhoneticsService;

	@BeforeEach
	public void setup() {
		updateGlobalProperty(NamePhoneticsConstants.GIVEN_NAME_GLOBAL_PROPERTY, "Soundex");
		updateGlobalProperty(NamePhoneticsConstants.MIDDLE_NAME_GLOBAL_PROPERTY, "Soundex");
		updateGlobalProperty(NamePhoneticsConstants.FAMILY_NAME_GLOBAL_PROPERTY, "Soundex");
		updateGlobalProperty(NamePhoneticsConstants.FAMILY_NAME2_GLOBAL_PROPERTY, "Soundex");
		namePhoneticsService.registerProcessor("Soundex", "org.apache.commons.codec.language.Soundex");
	}

	@Test
	public void shouldReturnRenderers(){
		Assertions.assertNotNull(namePhoneticsService.getProcessors());
		Assertions.assertFalse(namePhoneticsService.getProcessors().isEmpty());
	}

	@Test
	public void testFindPatientDoesNotFailIfAPersonIsMatched() {
		Person p = new Person();
		p.addName(new PersonName("Albert", "", "Einstein"));
		p.setBirthdateFromAge(50, new Date());
		p.setGender("M");
		p = personService.savePerson(p);
		namePhoneticsService.savePhoneticsForAllPatients();
		List<Patient> patientsFound = namePhoneticsService.findPatient("Albert", "", "Einstein", "");
		Assertions.assertEquals(0, patientsFound.size());
		List<Person> personsFound = namePhoneticsService.findPerson("Albert", "", "Einstein", "");
		Assertions.assertEquals(1, personsFound.size());
		Assertions.assertEquals(p, personsFound.get(0));
	}
	
	@Test
	public void savePhoneticsForPerson_shouldSaveNamePhoneticsForPerson() {
		NamePhoneticsService npService = Context.getService(NamePhoneticsService.class);

		// create a name phonetics record for person #2 (Horatio Hornblower)
		Person person = Context.getPersonService().getPerson(2);
		npService.savePhoneticsForPerson(person, "Soundex", "Soundex", "Soundex", "Soundex");

		// get the newly-created Name Phonetics records and make sure they are correct
		List<NamePhonetic> results = npService.getNamePhoneticsByPersonName(person.getPersonName());

		Assertions.assertEquals(3, results.size());

		for (NamePhonetic n : results) {
			Assertions.assertNotNull(n.getNameField());
			if (n.getNameField() == NamePhonetic.NameField.GIVEN_NAME) {
				Assertions.assertEquals("H630", n.getRenderedString());
			}
			if (n.getNameField() == NamePhonetic.NameField.MIDDLE_NAME) {
				Assertions.assertEquals("T230", n.getRenderedString());
			}
			if (n.getNameField() == NamePhonetic.NameField.FAMILY_NAME) {
				Assertions.assertEquals("H651", n.getRenderedString());
			}
		}
	}

	protected void updateGlobalProperty(String propertyName, String propertyValue) {
		GlobalProperty gp = administrationService.getGlobalPropertyObject(propertyName);
		if (gp == null) {
			gp = new GlobalProperty();
			gp.setProperty(propertyName);
			gp.setDescription(propertyName);
		}
		gp.setPropertyValue(propertyValue);
		administrationService.saveGlobalProperty(gp);
	}
}
