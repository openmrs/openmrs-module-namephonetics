package org.openmrs.module.namephonetics.advice;

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

import javax.transaction.Synchronization;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;


public class HibernateNamePhoneticsInterceptor extends EmptyInterceptor  {

	protected final Log log = LogFactory.getLog(HibernateNamePhoneticsInterceptor.class);
	private static final long serialVersionUID = -4905755656759767400L;

	protected NamePhoneticsService namePhoneticsService = null;
	protected AdministrationService administrationService = null;

	private String gpGivenName = null;
	private String gpMiddleName = null;
	private String gpFamilyName = null;
	private String gpFamilyName2 = null;

	// PersonName -> isNew (true if the name has no existing phonetics to delete). A single map
	// (rather than separate new/updated lists) both de-duplicates a name that is touched more than
	// once in the same transaction, and ensures that a name known to be new (via onSave) stays
	// classified as new even if it is also flush-dirtied again later in the same transaction.
	private ThreadLocal<LinkedHashMap<PersonName, Boolean>> queuedPersonNames = new ThreadLocal<LinkedHashMap<PersonName, Boolean>>();

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
		if (entity instanceof PersonName) {
			getNamePhoneticsService().deleteNamePhonetics((PersonName) entity);
		}
	}

	/**
	 * Queue any PersonNames that are saved (i.e. created) as new - these cannot already have any
	 * phonetics in the database.
	 */
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		if (entity instanceof PersonName) {
			queuePersonName((PersonName) entity, true);
		}
		// we never modify the currentState, so we should always return false according to definition
		return false;
	}

	/**
	 * Queue any PersonNames that are dirty flushed (i.e., modified) as needing their phonetics
	 * updated, unless already queued as new (see queuePersonName)
	 */
   public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
	   if (entity instanceof PersonName){
			queuePersonName((PersonName) entity, false);
    	}
	   // we never modify the currentState, so we should always return false according to definition
	   return false;
    }


    /**
     * Registers a Synchronization on the transaction, right after it begins, that will save the
     * name phonetics for all PersonNames captured by onSave/onFlushDirty over the course of the
     * whole transaction, right before it commits - and clear them once the transaction is done.
     *
     * This mirrors the pattern openmrs-module-event's HibernateEventInterceptor uses
     * (Transaction.registerSynchronization(), rather than overriding
     * Interceptor.beforeTransactionCompletion()/afterTransactionCompletion() directly). By the time
     * Synchronization.beforeCompletion() runs, the main flush's INSERT/UPDATE statements have already
     * executed, so the parent PersonName's identity-generated personNameId is already populated.
     * Unlike Interceptor.beforeTransactionCompletion(), which Hibernate silently swallows exceptions
     * from (logged only as HHH000088, never rethrown), a Synchronization's beforeCompletion() is not
     * swallowed - Hibernate logs and rethrows it, marking the transaction rollback-only. So a failure
     * here (e.g. a deadlock inserting into name_phonetics) correctly fails and rolls back the whole
     * transaction, including the PersonName that triggered it, instead of being silently discarded
     * while the save is reported as successful.
     */
    public void afterTransactionBegin(Transaction tx){
    	tx.registerSynchronization(new Synchronization(){
    		public void beforeCompletion(){
    			if (hasQueuedPersonNames()){
    				personUpdateHelper();
    			}
    		}
    		public void afterCompletion(int status){
    			queuedPersonNames.set(null);
    		}
    	});
    }



    /**
     * Utility Methods
     */

    private boolean hasQueuedPersonNames(){
    	return queuedPersonNames.get() != null && !queuedPersonNames.get().isEmpty();
    }

    /**
     * Updates the name phonetics for each person name queued up by onSave/onFlushDirty over the
     * course of the transaction
     */
    private void personUpdateHelper(){
    		checkGPs();
    		for (Map.Entry<PersonName, Boolean> entry : queuedPersonNames.get().entrySet()){
    			if (entry.getValue()){
    				getNamePhoneticsService().savePhoneticsForNewPersonName(entry.getKey(), gpGivenName, gpMiddleName, gpFamilyName, gpFamilyName2);
    			}
    			else {
    				getNamePhoneticsService().savePhoneticsForPersonName(entry.getKey(), gpGivenName, gpMiddleName, gpFamilyName, gpFamilyName2);
    			}
    		}
    		queuedPersonNames.set(null);
    }

    /**
     * Queue the given PersonName as needing its phonetics saved. A name already queued as new
     * (isNew=true) stays classified as new even if this is called again for it with isNew=false -
     * it still has no existing phonetics to delete, regardless of how many times Hibernate's
     * dirty-checking touches it before the transaction commits.
     */
    private void queuePersonName(PersonName name, boolean isNew) {
    	if (queuedPersonNames.get() == null) {
			queuedPersonNames.set(new LinkedHashMap<PersonName, Boolean>());
		}
    	Map<PersonName, Boolean> queued = queuedPersonNames.get();
    	if (isNew || !queued.containsKey(name)) {
    		queued.put(name, isNew);
    	}
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
