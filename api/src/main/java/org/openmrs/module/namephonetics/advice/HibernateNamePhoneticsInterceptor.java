package org.openmrs.module.namephonetics.advice;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Transaction;
import org.hibernate.type.Type;
import org.openmrs.PersonName;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.namephonetics.NamePhoneticsConstants;
import org.openmrs.module.namephonetics.NamePhoneticsService;


public class HibernateNamePhoneticsInterceptor extends EmptyInterceptor  {
	
	protected final Log log = LogFactory.getLog(HibernateNamePhoneticsInterceptor.class);
	private static final long serialVersionUID = -4905755656759767400L;
	
	protected NamePhoneticsService namePhoneticsService = null;
	protected AdministrationService administrationService = null;
	
	private String gpGivenName = null;
	private String gpMiddleName = null;
	private String gpFamilyName = null;
	private String gpFamilyName2 = null;
	
	private ThreadLocal<ArrayList<PersonName>> personNames = new ThreadLocal<ArrayList<PersonName>>();
	
	public HibernateNamePhoneticsInterceptor(){
		log.info("Initializing HibernateNamePhoneticsInterceptor.");
	}
	
    /**
     * When deleting a PersonName, delete the corresponding name phonetics
     * 
     * NOTE:  the only time a PersonName is ever deleted by itself is in HibernatePersonDAO.deletePersonAndAttributes.
     * There's not method exposed at the service level to directly delete a personName
     */
	public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		if (entity != null && entity instanceof PersonName) {
			getNamePhoneticsService().deleteNamePhonetics((PersonName) entity);
		}
	}
    
	/**
	 * Add any PersonNames that are saved (i.e. created) to the list of person names that we need to update the phonetics for 
	 */
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		if (entity != null && entity instanceof PersonName) {
			addPersonName((PersonName) entity);
		}
		// we never modify the currentState, so we should always return false according to definition
		return false;
	}
	
	/**
	 * Add any PersonNames that are dirty flushed (i.e., modified) to the list of person names that we need to update the phonetics for 
	 */
   public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
	   if (entity != null && entity instanceof PersonName){		
			addPersonName((PersonName) entity);
    	}
	   // we never modify the currentState, so we should always return false according to definition
	   return false;
    }


    /**
     * Here we want to update the name phonetics for all persons we captured during the on save or dirty flush
     */
    public void beforeTransactionCompletion(Transaction tx){
    	if (personNames.get() !=null && !personNames.get().isEmpty()){
    		personUpdateHelper();
    	}
    }

    /**
     * Clears the set of persons that need name phonetics updated
     */
    public void afterTransactionCompletion(Transaction tx){
    	personNames.set(null);
    }
    
    
    
    /**
     * Utility Methods
     */
    
    /**
     * Updates the name phonetics for each person in the personNames set 
     * @param entity
     */
    private void personUpdateHelper(){
    		checkGPs();
    		if (personNames.get() != null) {
			    for (PersonName pn : personNames.get()){
			    	getNamePhoneticsService().savePhoneticsForPersonName(pn, gpGivenName, gpMiddleName, gpFamilyName, gpFamilyName2);
		        }
    		}
    }
    
    /**
     * Add the Person Name to the ThreadLocal list of Person Names
     */
    private void addPersonName(PersonName name) {
    	if (this.personNames.get() == null) {
			this.personNames.set(new ArrayList<PersonName>());
		}
		this.personNames.get().add(name);
    }
    
    
	 private NamePhoneticsService getNamePhoneticsService(){
		 if (this.namePhoneticsService == null){
			 namePhoneticsService = Context.getService(NamePhoneticsService.class);
		 }
		 return namePhoneticsService;
	 }
     
     private AdministrationService getAdministrationService(){
    	 if (this.administrationService == null)
    		 administrationService = Context.getAdministrationService();
    	 return administrationService;
     }
     
     private void checkGPs(){
    	 AdministrationService as = getAdministrationService();
    	 if (gpGivenName == null)
    		 gpGivenName = as.getGlobalProperty(NamePhoneticsConstants.GIVEN_NAME_GLOBAL_PROPERTY);
    	 if (gpMiddleName == null)
    		 gpMiddleName = as.getGlobalProperty(NamePhoneticsConstants.MIDDLE_NAME_GLOBAL_PROPERTY);
    	 if (gpFamilyName == null)
    		 gpFamilyName = as.getGlobalProperty(NamePhoneticsConstants.FAMILY_NAME_GLOBAL_PROPERTY);
    	 if (gpFamilyName2 == null)
    		 gpFamilyName2 = as.getGlobalProperty(NamePhoneticsConstants.FAMILY_NAME2_GLOBAL_PROPERTY);
     }

	
}
