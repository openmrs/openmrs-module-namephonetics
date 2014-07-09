package org.openmrs.module.namephonetics.db.hibernate;

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
    
    public void saveNamePhonetic(NamePhonetic np) throws DAOException{
        if (np.getRenderedString() != null)
            sessionFactory.getCurrentSession().saveOrUpdate(np);
        else
            log.warn("Null rendered string generated during saveNamePhonetic for PersonName " + np.getPersonName() + ", personNameId = " + np.getPersonName().getPersonNameId());
    }
    
    public NamePhonetic getNamePhonetic(Integer id) throws DAOException{
        return (NamePhonetic) sessionFactory.getCurrentSession().get(NamePhonetic.class, id);
    }
    
    public void deleteNamePhonetic(NamePhonetic np) throws DAOException{
        sessionFactory.getCurrentSession().delete(np);
    }
    
    /**
     * a name phonetic will never be created without a parent transaction involving a PersonName, right???
     * this can only be triggered through the UI through 'delete patient' from the long demographics form
     */
    public void deleteNamePhonetics(PersonName pn) throws DAOException {
    	if (pn != null && pn.getId() != null){
	    	String deleteStr = "delete NamePhonetic where personName = :pn";
	    	sessionFactory.getCurrentSession().setFlushMode(FlushMode.MANUAL);
	    	sessionFactory.getCurrentSession().createQuery(deleteStr).setParameter("pn", pn).executeUpdate();
	    	sessionFactory.getCurrentSession().setFlushMode(FlushMode.AUTO);
    	}
    }

	public List<PersonName> getAllPersonNames() {
		return sessionFactory.getCurrentSession().createQuery("from PersonName").list();
	}
    
    public List<NamePhonetic> getNamePhoneticsByRenderedString(String search, NamePhonetic.NameField field, String rendererClassName) throws DAOException {
        Query q = sessionFactory.getCurrentSession().createQuery("from NamePhonetic np where np.renderedString = :search and nameField = :field and rendererClassName = :className");
        q.setParameter("search", search);
        q.setParameter("field", field);
        q.setParameter("className", rendererClassName);
        List<NamePhonetic> pnList = (List<NamePhonetic>) q.list();
        if (pnList != null)
            return pnList;
        else
            return new ArrayList<NamePhonetic>();
    }
    
    public List<NamePhonetic> getNamePhoneticsByPersonName(PersonName pn) throws DAOException {
        Query q = sessionFactory.getCurrentSession().createSQLQuery("select * from name_phonetics np where np.person_name_id = :search ")
        .addEntity(NamePhonetic.class);
        q.setParameter("search", pn.getPersonNameId());
        List<NamePhonetic> pnList = (List<NamePhonetic>) q.list();
        if (pnList != null)
            return pnList;
        else
            return new ArrayList<NamePhonetic>();
    }

    //TODO:  put together the master query at the DB level, instead of having to merge results of getNamePhoneticsByRenderedString
}
