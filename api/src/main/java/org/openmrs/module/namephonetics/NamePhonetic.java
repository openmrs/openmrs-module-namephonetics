package org.openmrs.module.namephonetics;

import java.io.Serializable;

import org.apache.commons.codec.StringEncoder;
import org.openmrs.PersonName;
import org.openmrs.util.OpenmrsClassLoader;

public class NamePhonetic implements Serializable {

    private Integer id;
    private String renderedString;
    private PersonName personName;
    private NameField nameField;
    private String rendererClassName;
    
    public NamePhonetic(String renderedString, PersonName pn, NamePhonetic.NameField af, String rendererClassName){
        this.renderedString = renderedString;
        this.personName = pn;
        this.rendererClassName = rendererClassName;
        this.nameField = af;
    }
    
    public NamePhonetic(){}
    
    public static enum NameField {
        GIVEN_NAME(1), MIDDLE_NAME(2), FAMILY_NAME(3), FAMILY_NAME2(4);
        private final int value;
        NameField(int value) {
            this.value = value;
        }
        public int getValue(){
            return value;
        }
    } 


    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getRenderedString() {
        return renderedString;
    }
    public void setRenderedString(String renderedString) {
        this.renderedString = renderedString;
    }
    public PersonName getPersonName() {
        return personName;
    }
    public void setPersonName(PersonName personName) {
        this.personName = personName;
    }
    public NameField getNameField() {
        return nameField;
    }
    public void setNameField(NameField nameField) {
        this.nameField = nameField;
    }
    public String getRendererClassName() {
        return rendererClassName;
    }
    public void setRendererClassName(String rendererClassName) {
        try {
            Class<?> clazz = OpenmrsClassLoader.getInstance().loadClass(rendererClassName);
        } catch (Exception ex){
            throw new RuntimeException("Could not load a class of type " + rendererClassName + ".  This should be a valid implementation of the StringEncoder interface.");
        }
        this.rendererClassName = rendererClassName;
    }
    public void setRendererClassName(StringEncoder se) {
        this.rendererClassName = se.getClass().getName();
    }
    
    
    
}
