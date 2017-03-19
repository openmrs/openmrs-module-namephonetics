package org.openmrs.module.namephonetics;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;

/**
 * see http://community.jboss.org/wiki/UserTypefordealingwithfixedlengthCHARfields?cmd=comphist
 * and http://community.jboss.org/wiki/UserTypeforpersistingaTypesafeEnumerationwithaVARCHARcolumn
 */
public class NameFieldUserType implements UserType {

    private static final int[] SQL_TYPES = {Types.INTEGER};
    @Override
	public int[] sqlTypes() { return SQL_TYPES; }
    @Override
	public Class returnedClass() { return NamePhonetic.NameField.class; }
    @Override
	public boolean equals(Object x, Object y) { return x == y; }
    @Override
	public Object deepCopy(Object value) { return value; }
    @Override
	public boolean isMutable() { return false; }
   
	@Override
	public Object nullSafeGet(ResultSet resultSet, String[] names, SessionImplementor session, Object owner)
	    throws HibernateException, SQLException {

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
    
	@Override
	public void nullSafeSet(PreparedStatement statement, Object value, int index, SessionImplementor session)
	    throws HibernateException, SQLException {
        if (value == null) {
            statement.setNull(index, Types.INTEGER);
        } else {
        NamePhonetic.NameField nf = (NamePhonetic.NameField) value;  
        statement.setInt(index, nf.getValue());
        }
    }
    @Override
	public Object assemble(Serializable cached, Object owner) {
        
        return cached;
 
    }
    @Override
	public Serializable disassemble(Object value) {
 
        return (Serializable) value;
 
    }
    @Override
	public Object replace(Object original, Object target, Object owner) {
        
        return original;
 
    }
    @Override
	public int hashCode(Object x) {
 
        return x.hashCode();
 
	}
}
