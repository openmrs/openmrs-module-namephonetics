package org.openmrs.module.namephonetics;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

/**
 * see http://community.jboss.org/wiki/UserTypefordealingwithfixedlengthCHARfields?cmd=comphist
 * and http://community.jboss.org/wiki/UserTypeforpersistingaTypesafeEnumerationwithaVARCHARcolumn
 */
public class NameFieldUserType implements UserType {

    private static final int[] SQL_TYPES = {Types.INTEGER};
    public int[] sqlTypes() { return SQL_TYPES; }
    public Class returnedClass() { return NamePhonetic.NameField.class; }
    public boolean equals(Object x, Object y) { return x == y; }
    public Object deepCopy(Object value) { return value; }
    public boolean isMutable() { return false; }
   
    public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner) throws HibernateException, SQLException {

            int name = resultSet.getInt(names[0]);
            NamePhonetic.NameField nf = null;
            if (name == 1)
                nf = NamePhonetic.NameField.GIVEN_NAME;
            else if (name == 2)
                nf = NamePhonetic.NameField.MIDDLE_NAME;
            else if (name == 3)
                nf = NamePhonetic.NameField.FAMILY_NAME;
            else if (name == 4)
                nf = NamePhonetic.NameField.FAMILY_NAME2;
            return resultSet.wasNull() ? null : nf;
    }
    
    public void nullSafeSet(PreparedStatement statement, Object value, int index) throws HibernateException, SQLException {
        if (value == null) {
            statement.setNull(index, Types.INTEGER);
        } else {
        NamePhonetic.NameField nf = (NamePhonetic.NameField) value;  
        statement.setInt(index, nf.getValue());
        }
    }
    public Object assemble(Serializable cached, Object owner) {
        
        return cached;
 
    }
    public Serializable disassemble(Object value) {
 
        return (Serializable) value;
 
    }
    public Object replace(Object original, Object target, Object owner) {
        
        return original;
 
    }
    public int hashCode(Object x) {
 
        return x.hashCode();
 
    }
    
    
}
