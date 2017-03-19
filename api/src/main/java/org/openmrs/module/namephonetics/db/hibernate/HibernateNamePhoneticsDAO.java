package org.openmrs.module.namephonetics.db.hibernate;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.PersonName;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.namephonetics.NamePhonetic;
import org.openmrs.module.namephonetics.db.NamePhoneticsDAO;

public class HibernateNamePhoneticsDAO implements NamePhoneticsDAO {

    protected static final Log log = LogFactory.getLog(HibernateNamePhoneticsDAO.class);
    
    private SessionFactory sessionFactory;
    
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    @Override
	public void saveNamePhonetic(NamePhonetic np) throws DAOException{
        if (np.getRenderedString() != null)
			getCurrentSession().saveOrUpdate(np);
        else
            log.warn("Null rendered string generated during saveNamePhonetic for PersonName " + np.getPersonName() + ", personNameId = " + np.getPersonName().getPersonNameId());
    }
    
    @Override
	public NamePhonetic getNamePhonetic(Integer id) throws DAOException{
		return (NamePhonetic) getCurrentSession().get(NamePhonetic.class, id);
    }
    
    @Override
	public void deleteNamePhonetic(NamePhonetic np) throws DAOException{
		getCurrentSession().delete(np);
    }
    
    /**
     * a name phonetic will never be created without a parent transaction involving a PersonName, right???
     * this can only be triggered through the UI through 'delete patient' from the long demographics form
     */
    @Override
	public void deleteNamePhonetics(PersonName pn) throws DAOException {
    	if (pn != null && pn.getId() != null){
	    	String deleteStr = "delete NamePhonetic where personName = :pn";
			getCurrentSession().setFlushMode(FlushMode.MANUAL);
			getCurrentSession().createQuery(deleteStr).setParameter("pn", pn).executeUpdate();
			getCurrentSession().setFlushMode(FlushMode.AUTO);
    	}
    }

	@Override
	public List<PersonName> getAllPersonNames() {
		return getCurrentSession().createQuery("from PersonName").list();
	}
    
    @Override
	public List<NamePhonetic> getNamePhoneticsByRenderedString(String search, NamePhonetic.NameField field, String rendererClassName) throws DAOException {
		Query q = getCurrentSession().createQuery(
		    "from NamePhonetic np where np.renderedString = :search and nameField = :field and rendererClassName = :className");
        q.setParameter("search", search);
        q.setParameter("field", field);
        q.setParameter("className", rendererClassName);
        List<NamePhonetic> pnList = q.list();
        if (pnList != null)
            return pnList;
        else
            return new ArrayList<NamePhonetic>();
    }
    
    @Override
	public List<NamePhonetic> getNamePhoneticsByPersonName(PersonName pn) throws DAOException {
		Query q = getCurrentSession().createSQLQuery("select * from name_phonetics np where np.person_name_id = :search ")
        .addEntity(NamePhonetic.class);
        q.setParameter("search", pn.getPersonNameId());
        List<NamePhonetic> pnList = q.list();
        if (pnList != null)
            return pnList;
        else
            return new ArrayList<NamePhonetic>();
    }

    //TODO:  put together the master query at the DB level, instead of having to merge results of getNamePhoneticsByRenderedString
	
	/**
	 * Gets the current hibernate session while taking care of the hibernate 3 and 4 differences.
	 * 
	 * @return the current hibernate session.
	 */
	private org.hibernate.Session getCurrentSession() {
		try {
			return sessionFactory.getCurrentSession();
		}
		catch (NoSuchMethodError ex) {
			try {
				Method method = sessionFactory.getClass().getMethod("getCurrentSession", null);
				return (org.hibernate.Session) method.invoke(sessionFactory, null);
			}
			catch (Exception e) {
				throw new RuntimeException("Failed to get the current hibernate session", e);
			}
		}
	}
}
