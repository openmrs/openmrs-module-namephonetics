package org.openmrs.module.namephonetics.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.namephonetics.NamePhonetic;
import org.openmrs.module.namephonetics.NamePhoneticsConstants;
import org.openmrs.module.namephonetics.NamePhoneticsService;
import org.openmrs.module.namephonetics.NamePhoneticsUtil;
import org.openmrs.module.namephonetics.db.NamePhoneticsDAO;

public class NamePhoneticsServiceImpl extends BaseOpenmrsService implements NamePhoneticsService {

    protected static final Log log = LogFactory.getLog(NamePhoneticsServiceImpl.class);
    
    NamePhoneticsDAO dao;
    //rendererCode, rendererClassName
    private static Map<String, String> processors = null;
    
    public void saveNamePhonetic(NamePhonetic np) throws APIException {
        dao.saveNamePhonetic(np);
    }
    
    
    public NamePhonetic getNamePhonetic(Integer id) throws APIException {
        return dao.getNamePhonetic(id);
    }
    
    public void deleteNamePhonetic(NamePhonetic np) throws APIException {
        dao.deleteNamePhonetic(np);
    }
    
    public List<NamePhonetic> getNamePhoneticsByPersonName(PersonName pn) throws APIException{
        return dao.getNamePhoneticsByPersonName(pn);
    }

    public void setDao(NamePhoneticsDAO dao) {
        this.dao = dao;
    }
    
    /**
     * @see IdentifierSourceService#getProcessor(IdentifierSource)
     */
    public String getProcessorClassName(String name) {
        return getProcessors().get(name);
    }
    
    /**
     * 
     * retrieves a list of NamePhonetics by search string, field, and the rendererCode
     * 
     * @param search
     * @param field
     * @param rendererCode
     * @return
     * @throws APIException
     */
    public List<NamePhonetic> getNamePhoneticsByUnrenderedSearch(String search, NamePhonetic.NameField field, String rendererCode) throws APIException {
        return dao.getNamePhoneticsByRenderedString(NamePhoneticsUtil.encodeString(search, rendererCode), field, this.getProcessorClassName(rendererCode));
    }
    
    /**
     * @see IdentifierSourceService#registerProcessor(Class, IdentifierSourceProcessor)
     */
    public void registerProcessor(String processorCodeName, String processorClassName) throws APIException {
        getProcessors().put(processorCodeName, processorClassName);
        log.info("NamePhonetics registering processor " + processorCodeName + " : " + processorClassName);
    }
    
    /**
     * @return the processors
     */
    public Map<String, String> getProcessors() {
        if (processors == null) {
            processors = new HashMap<String, String>();
        }
        return processors;
    }

    /**
     * ADDS, doesn't simply set
     * @param processors the processors to set
     */
    public void setProcessors(Map<String, String> processorsToAdd) {
        if (processorsToAdd != null) {
            for (Map.Entry<String, String> entry : processorsToAdd.entrySet()) {
                registerProcessor(entry.getKey(), entry.getValue());
            }
        }
    }
    
    
    private Set<PersonName> getAllMatchingNamePhonetics(String givenNameSearch, String middleNameSearch, String familyNameSearch, String familyName2Search){
        Set<NamePhonetic> ret = new LinkedHashSet<NamePhonetic>();
        AdministrationService as = Context.getAdministrationService();
        String gpGivenName = as.getGlobalProperty(NamePhoneticsConstants.GIVEN_NAME_GLOBAL_PROPERTY);
        String gpMiddleName = as.getGlobalProperty(NamePhoneticsConstants.MIDDLE_NAME_GLOBAL_PROPERTY);
        String gpFamilyName = as.getGlobalProperty(NamePhoneticsConstants.FAMILY_NAME_GLOBAL_PROPERTY);
        String gpFamilyName2 = as.getGlobalProperty(NamePhoneticsConstants.FAMILY_NAME2_GLOBAL_PROPERTY);
        
        boolean checkGivenName = false;
        boolean checkMiddleName = false;
        boolean checkFamilyName = false;
        boolean checkFamilyName2 = false;
        
        if(StringUtils.isNotBlank(givenNameSearch) && StringUtils.isNotBlank(gpGivenName)) {    
         
        	List<NamePhonetic> names = getNamePhoneticsByUnrenderedSearch(givenNameSearch, NamePhonetic.NameField.GIVEN_NAME, gpGivenName);
            if (names != null && names.size() > 0){
                for (NamePhonetic np : names){
                    ret.add(np);
                }
            }
            checkGivenName = true;
        }
        if(StringUtils.isNotBlank(middleNameSearch) && StringUtils.isNotBlank(gpMiddleName)) {       
         
        	List<NamePhonetic> names = getNamePhoneticsByUnrenderedSearch(middleNameSearch, NamePhonetic.NameField.MIDDLE_NAME, gpMiddleName);
            if (names != null && names.size() > 0){
                for (NamePhonetic np : names){
                    ret.add(np);
                }
            }
            checkMiddleName = true;
        }
        if(StringUtils.isNotBlank(familyNameSearch) && StringUtils.isNotBlank(gpFamilyName)) {        
         
        	List<NamePhonetic> names = getNamePhoneticsByUnrenderedSearch(familyNameSearch, NamePhonetic.NameField.FAMILY_NAME, gpFamilyName);
            if (names != null && names.size() > 0){
                for (NamePhonetic np : names){
                    ret.add(np);
                }
            }
            checkFamilyName = true;
        }
        if(StringUtils.isNotBlank(familyName2Search) && StringUtils.isNotBlank(gpFamilyName2)) {   
            
            List<NamePhonetic> names = getNamePhoneticsByUnrenderedSearch(familyName2Search, NamePhonetic.NameField.FAMILY_NAME2, gpFamilyName2);
            if (names != null && names.size() > 0 ){
                for (NamePhonetic np : names){
                    ret.add(np);
                }
            }
            checkFamilyName2 = true;
        }
        
        return reconcileNamePhoneticSearchHelper(ret, checkGivenName, checkMiddleName, checkFamilyName, checkFamilyName2);
    }
    
    /**
     * we want to make sure that only NamePhonetics survive if there was a match on all search terms
     */
    private Set<PersonName> reconcileNamePhoneticSearchHelper(Set<NamePhonetic> namePhonetics, boolean checkGivenName, boolean checkMiddleName, boolean checkFamilyName, boolean checkFamilyName2){
            Set<PersonName> ret = new HashSet<PersonName>();
            Set<PersonName> allPersonNames = new HashSet<PersonName>();
            for (NamePhonetic np : namePhonetics){
                allPersonNames.add(np.getPersonName());
            }

            for (PersonName pnTmp : allPersonNames){
                boolean addPersonName = true;
                if (checkGivenName){
                    addPersonName = doPhoneticsContainPersonNameForField(namePhonetics, NamePhonetic.NameField.GIVEN_NAME, pnTmp);
                }
                if (checkMiddleName && addPersonName){
                    addPersonName = doPhoneticsContainPersonNameForField(namePhonetics, NamePhonetic.NameField.MIDDLE_NAME, pnTmp);
                }
                if (checkFamilyName && addPersonName){
                    addPersonName = doPhoneticsContainPersonNameForField(namePhonetics, NamePhonetic.NameField.FAMILY_NAME, pnTmp);
                }
                if (checkFamilyName2 && addPersonName){
                    addPersonName = doPhoneticsContainPersonNameForField(namePhonetics, NamePhonetic.NameField.FAMILY_NAME2, pnTmp);
                }
                if (addPersonName){
                    ret.add(pnTmp);
                }
            }
            return ret;
    }
    
    /**
     * does the list contain a NamePhonetic with the corresonding PersonName?
     */
    private boolean doPhoneticsContainPersonNameForField(Set<NamePhonetic> namePhonetics, NamePhonetic.NameField field, PersonName pn){
        for (NamePhonetic np:namePhonetics){
            if (np.getNameField().equals(field) && np.getPersonName().getPersonNameId().equals(pn.getPersonNameId()))
                return true;
        }
        return false;
    }
    
    public List<Patient> findPatient(String givenNameSearch, String middleNameSearch, String familyNameSearch, String familyName2Search){
        Set<Integer> idList = new HashSet<Integer>();
        Set<PersonName> matches = getAllMatchingNamePhonetics(givenNameSearch, middleNameSearch, familyNameSearch, familyName2Search);
        PatientSetService ps = Context.getPatientSetService();
        for (PersonName pn:matches){
            idList.add(pn.getPerson().getPersonId());
        }
        //TODO: figure out how you want to order them?
          return  ps.getPatients(idList);
    }

   
    
    public void savePhoneticsForPatient(Patient p, String gpGivenName,  String gpMiddleName, String gpFamilyName, String gpFamilyName2){
       savePhoneticsForPerson(p, gpGivenName, gpMiddleName, gpFamilyName, gpFamilyName2);
    }
    
    public void savePhoneticsForPerson(Person p, String gpGivenName,  String gpMiddleName, String gpFamilyName, String gpFamilyName2){
          for (PersonName pn : p.getNames()){
        	  savePhoneticsForPersonName(pn, gpGivenName, gpMiddleName, gpFamilyName, gpFamilyName2);
          }
      }
    
    public void savePhoneticsForPersonName(PersonName pn, String gpGivenName,  String gpMiddleName, String gpFamilyName, String gpFamilyName2){
    	deleteNamePhonetics(pn);
        if (StringUtils.isNotBlank(gpGivenName) && StringUtils.isNotBlank(pn.getGivenName())) {
            saveNamePhonetic(new NamePhonetic(NamePhoneticsUtil.encodeString(pn.getGivenName(), gpGivenName), pn, NamePhonetic.NameField.GIVEN_NAME, getProcessorClassName(gpGivenName)));
        }
        if (StringUtils.isNotBlank(gpMiddleName) && StringUtils.isNotBlank(pn.getMiddleName())) {
            saveNamePhonetic(new NamePhonetic(NamePhoneticsUtil.encodeString(pn.getMiddleName(), gpMiddleName), pn, NamePhonetic.NameField.MIDDLE_NAME, getProcessorClassName(gpMiddleName)));
        }
        if (StringUtils.isNotBlank(gpFamilyName) && StringUtils.isNotBlank(pn.getFamilyName())) {
            saveNamePhonetic(new NamePhonetic(NamePhoneticsUtil.encodeString(pn.getFamilyName(), gpFamilyName), pn, NamePhonetic.NameField.FAMILY_NAME, getProcessorClassName(gpFamilyName)));
        }
        if (StringUtils.isNotBlank(gpFamilyName2) && StringUtils.isNotBlank(pn.getFamilyName2())) {
            saveNamePhonetic(new NamePhonetic(NamePhoneticsUtil.encodeString(pn.getFamilyName2(), gpFamilyName2), pn, NamePhonetic.NameField.FAMILY_NAME2, getProcessorClassName(gpFamilyName2)));
        }
        
    }
  
    public void deleteNamePhonetics(PersonName pn) throws APIException{
    	dao.deleteNamePhonetics(pn);
    }

    /**
     * @see NamePhoneticsService#findSimilarGivenNames(String)
     */
	public List<String> findSimilarGivenNames(String searchPhrase) {
        Set<PersonName> matches = getAllMatchingNamePhonetics(searchPhrase, null, null, null);
        List<String> names = new ArrayList<String>();
        for (PersonName personName : matches) {
        	names.add(personName.getGivenName());
        }
        
		return names;
	}

	 /**
     * @see NamePhoneticsService#findSimilarFamilyNames(String)
     */
	public List<String> findSimilarFamilyNames(String searchPhrase) {
		Set<PersonName> matches = getAllMatchingNamePhonetics(null, null, searchPhrase, null);
        List<String> names = new ArrayList<String>();
        for (PersonName personName : matches) {
        	names.add(personName.getFamilyName());
        }
        
		return names;
	}
}
