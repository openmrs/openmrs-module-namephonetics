package org.openmrs.module.namephonetics.phoneticsalgorithm;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;
import org.apache.commons.codec.language.DoubleMetaphone;

/**
 * A encoder that uses the alternative double metaphone encoding instead of the standard one
 * The alternate encoding supposedly encodes a name using its native language pronunication instead of it's English one
 * Simply delegates the encoding to the DoubleMetaphone class, setting the alternate flag to true
 * (the standard DoubleMetaphone encode method calls doubleMetaphone(value, false)
 */
public class DoubleMetaphoneAlternate implements StringEncoder {

	public Object encode(Object obj) throws EncoderException {
		try {
            String value = (String) obj;
            return encode(value);
        } catch (Exception ex){
            throw new RuntimeException("DoubleMetaphoneAlternatve Phonetic Algorithm was passed something that was not a string to encode.");
        }  
    }

	public String encode(String value) throws EncoderException {
	    DoubleMetaphone encoder = new DoubleMetaphone();
	    return encoder.doubleMetaphone(value, true);
    }
	
}
