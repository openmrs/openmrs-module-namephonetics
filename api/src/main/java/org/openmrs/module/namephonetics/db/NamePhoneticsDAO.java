package org.openmrs.module.namephonetics.db;

import java.util.List;

import org.openmrs.PersonName;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.namephonetics.NamePhonetic;

public interface NamePhoneticsDAO {
    
    public void saveNamePhonetic(NamePhonetic np) throws DAOException;
    
    public NamePhonetic getNamePhonetic(Integer id) throws DAOException;
    
    public void deleteNamePhonetic(NamePhonetic np) throws DAOException;
    
    public List<NamePhonetic> getNamePhoneticsByRenderedString(String search, NamePhonetic.NameField field, String rendererClassName) throws DAOException;

    public List<NamePhonetic> getNamePhoneticsByPersonName(PersonName pn) throws DAOException;
    
    public void deleteNamePhonetics(PersonName pn) throws DAOException;

	public List<PersonName> getAllPersonNames();
}
