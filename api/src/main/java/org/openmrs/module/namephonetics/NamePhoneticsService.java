package org.openmrs.module.namephonetics;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.springframework.transaction.annotation.Transactional;

public interface NamePhoneticsService extends OpenmrsService{

    @Transactional
    public void saveNamePhonetic(NamePhonetic np) throws APIException;
    
    @Transactional(readOnly=true)
    public NamePhonetic getNamePhonetic(Integer id) throws APIException;
    
    @Transactional
    public void deleteNamePhonetic(NamePhonetic np) throws APIException;

    @Transactional(readOnly=true)
    public String getProcessorClassName(String processorCodeName);
    
    @Transactional(readOnly=true)
    public Map<String, String> getProcessors();
    
    @Transactional(readOnly=true)
    public void registerProcessor(String processorCodeName, String processorClassName) throws APIException;
    
    @Transactional(readOnly=true)
    public List<NamePhonetic> getNamePhoneticsByPersonName(PersonName pn) throws APIException;

	/**
	 * @return all of the matching person names for the passed in searches
	 */
	@Transactional(readOnly=true)
	public Set<PersonName> getAllMatchingNamePhonetics(String givenNameSearch, String middleNameSearch, String familyNameSearch, String familyName2Search);
    
    /**
     * 
     * Finds patients by matching strings to the strings encoded by the algorithms given in the module's 4 global properties.
     * null values for these global properties or for the arguments of this method means that the search won't include that property of PersonName.
     * 
     * @param givenNameSearch (unencoded)
     * @param middleNameSearch (unencoded)
     * @param familyNameSearch (unencoded)
     * @param familyName2Search (unencoded)
     * @return
     * @throws APIException
     */   
    @Transactional(readOnly=true)
    public List<Patient> findPatient(String givenNameSearch, String middleNameSearch, String familyNameSearch, String familyName2Search) throws APIException;

	/**
	 * Rebuilds all name phonetics in the database
	 */
	@Transactional
	public void savePhoneticsForAllPatients();

    /**
	 * Searches for given names that are similar to a search phrase.
     *
	 * @param searchPhrase the search phrase.
	 * @return list of given names that match the search phrase.
	 */
    @Transactional(readOnly = true)
	public List<String> findSimilarGivenNames(String searchPhrase);
    
    /**
	 * Searches for family names that are similar to a search phrase.
	 * 
	 * @param searchPhrase the search phrase.
	 * @return list of family names that match the search phrase.
	 */
    @Transactional(readOnly = true)
	public List<String> findSimilarFamilyNames(String searchPhrase);
    
    /**
     *  @see NamePhoneticsUtil#savePhoneticsForPatient(Patient p)
     */
    @Transactional
    public void savePhoneticsForPerson(Person p, String gpGivenName,  String gpMiddleName, String gpFamilyName, String gpFamilyName2) throws APIException;

    @Transactional
    public void savePhoneticsForPersonName(PersonName pn, String gpGivenName,  String gpMiddleName, String gpFamilyName, String gpFamilyName2) throws APIException;
    
    @Transactional
    public void deleteNamePhonetics(PersonName pn) throws APIException;

}
