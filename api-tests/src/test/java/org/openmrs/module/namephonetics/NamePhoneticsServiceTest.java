package org.openmrs.module.namephonetics;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PersonService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class NamePhoneticsServiceTest extends BaseModuleContextSensitiveTest {

	@Autowired
	PersonService personService;

	@Autowired @Qualifier("adminService")
	AdministrationService administrationService;

	@Autowired
	NamePhoneticsService namePhoneticsService;

	@Before
	public void setup() {
		updateGlobalProperty(NamePhoneticsConstants.GIVEN_NAME_GLOBAL_PROPERTY, "Soundex");
		updateGlobalProperty(NamePhoneticsConstants.MIDDLE_NAME_GLOBAL_PROPERTY, "Soundex");
		updateGlobalProperty(NamePhoneticsConstants.FAMILY_NAME_GLOBAL_PROPERTY, "Soundex");
		updateGlobalProperty(NamePhoneticsConstants.FAMILY_NAME2_GLOBAL_PROPERTY, "Soundex");
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
		Assert.assertEquals(0, patientsFound.size());
		List<Person> personsFound = namePhoneticsService.findPerson("Albert", "", "Einstein", "");
		Assert.assertEquals(1, personsFound.size());
		Assert.assertEquals(p, personsFound.get(0));
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
