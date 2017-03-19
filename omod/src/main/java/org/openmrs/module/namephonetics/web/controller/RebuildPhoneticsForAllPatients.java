package org.openmrs.module.namephonetics.web.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.namephonetics.NamePhoneticsConstants;
import org.openmrs.module.namephonetics.NamePhoneticsService;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/module/namephonetics/rebuildPhonetics")
public class RebuildPhoneticsForAllPatients {

    protected static final Log log = LogFactory.getLog(RebuildPhoneticsForAllPatients.class);
    
	private static Map<User, Map<String, Object>> volatileUserData = new WeakHashMap<User, Map<String, Object>>();
	
    @RequestMapping(method = RequestMethod.GET)
    public void getRequest(ModelMap model){
        AdministrationService as = Context.getAdministrationService();
        NamePhoneticsService nps = Context.getService(NamePhoneticsService.class);
        String gpGivenName = as.getGlobalProperty(NamePhoneticsConstants.GIVEN_NAME_GLOBAL_PROPERTY);
        String gpMiddleName = as.getGlobalProperty(NamePhoneticsConstants.MIDDLE_NAME_GLOBAL_PROPERTY);
        String gpFamilyName = as.getGlobalProperty(NamePhoneticsConstants.FAMILY_NAME_GLOBAL_PROPERTY);
        String gpFamilyName2 = as.getGlobalProperty(NamePhoneticsConstants.FAMILY_NAME2_GLOBAL_PROPERTY);
        model.put("gpGivenName", gpGivenName);
        model.put("gpMiddleName", gpMiddleName);
        model.put("gpFamilyName", gpFamilyName);
        model.put("gpFamilyName2", gpFamilyName2);
        model.put("processors", nps.getProcessors().keySet());
    }
    
    @RequestMapping(method = RequestMethod.POST)
    public Object doStuff(
            @RequestParam(value="givenNameSelect", required=true) String givenName,
            @RequestParam(value="middleNameSelect", required=true) String middleName,
            @RequestParam(value="familyNameSelect", required=true) String familyName,
            @RequestParam(value="familyName2Select", required=true) String familyName2,
            @RequestParam(value="submit", required=true) String submit,
            HttpServletRequest request){
        
        
        AdministrationService as = Context.getAdministrationService();
        as.saveGlobalProperty(new GlobalProperty(NamePhoneticsConstants.GIVEN_NAME_GLOBAL_PROPERTY, givenName));
        as.saveGlobalProperty(new GlobalProperty(NamePhoneticsConstants.MIDDLE_NAME_GLOBAL_PROPERTY, middleName));
        as.saveGlobalProperty(new GlobalProperty(NamePhoneticsConstants.FAMILY_NAME_GLOBAL_PROPERTY, familyName));
        as.saveGlobalProperty(new GlobalProperty(NamePhoneticsConstants.FAMILY_NAME2_GLOBAL_PROPERTY, familyName2));
		setVolatileUserData(WebConstants.OPENMRS_MSG_ATTR, "All settings for given, middle, family, family2 saved.");
        
        if (submit.equals("Generate Phonetics for All Patients")){
			Context.getService(NamePhoneticsService.class).savePhoneticsForAllPatients();
            request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "All Phonetics Rebuilt Successfully.");
        }
        
        return "redirect:/module/namephonetics/rebuildPhonetics.form";
    }
	
	/**
	 * Get a piece of information for the currently authenticated user. This information is stored
	 * only temporarily. When a new module is loaded or the server is restarted, this information
	 * will disappear. If there is not information by this key, null is returned TODO: This needs to
	 * be refactored/removed
	 * 
	 * @param key identifying string for the information
	 * @return the information stored
	 */
	public static Object getVolatileUserData(String key) {
		User u = Context.getAuthenticatedUser();
		if (u == null) {
			throw new APIAuthenticationException();
		}
		Map<String, Object> myData = volatileUserData.get(u);
		if (myData == null) {
			return null;
		} else {
			return myData.get(key);
		}
	}
	
	/**
	 * Set a piece of information for the currently authenticated user. This information is stored
	 * only temporarily. When a new module is loaded or the server is restarted, this information
	 * will disappear
	 * 
	 * @param key identifying string for this information
	 * @param value information to be stored
	 */
	public static void setVolatileUserData(String key, Object value) {
		User u = Context.getAuthenticatedUser();
		if (u == null) {
			throw new APIAuthenticationException();
		}
		Map<String, Object> myData = volatileUserData.get(u);
		if (myData == null) {
			myData = new HashMap<String, Object>();
			volatileUserData.put(u, myData);
		}
		myData.put(key, value);
	}
}
