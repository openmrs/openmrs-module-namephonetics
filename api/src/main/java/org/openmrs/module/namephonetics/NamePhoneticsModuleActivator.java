/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.namephonetics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.Activator;



/**
 * This class contains the logic that is run every time this module
 * is either started or shutdown
 */
public class NamePhoneticsModuleActivator implements Activator  {

    private Log log = LogFactory.getLog(this.getClass());

    /**
     * @see org.openmrs.module.Activator#startup()
     */
    public final void shutdown() {
        onShutdown();
    }

    /**
     * @see org.openmrs.module.Activator#shutdown()
     */
    public final void startup() {
        onStartup();
        log.info("Starting Name Phonetics module");
    }

//    /**
//     * @see java.lang.Runnable#run()
//     */
//    public final void run() {
//        // Wait for context refresh to finish
//     // Wait for context refresh to finish
//
//        ApplicationContext ac = null;
//        NamePhoneticsService nps = null;
//        try {
//            while (ac == null || nps == null) {
//                Thread.sleep(30000);
//                if (NamePhoneticsContextAware.getApplicationContext() != null){
//                    try{
//                        log.info("NamePhoneticsService still waiting for app context and services to load...");
//                        ac = NamePhoneticsContextAware.getApplicationContext();
//                        nps = Context.getService(NamePhoneticsService.class);
//                    } catch (APIException apiEx){}
//                }
//            }
//        } catch (InterruptedException ex) {}
//        try {
//            Thread.sleep(10000);
//            // Start new OpenMRS session on this thread
//            Context.openSession();
//            Context.addProxyPrivilege("View Concept Classes");
//            Context.addProxyPrivilege("View Concepts");
//            Context.addProxyPrivilege("Manage Concepts");
//            Context.addProxyPrivilege("View Global Properties");
//            Context.addProxyPrivilege("Manage Global Properties");
//            Context.addProxyPrivilege("SQL Level Access");
//            Context.addProxyPrivilege("View Forms");
//            Context.addProxyPrivilege("Manage Forms");
//            onLoad(nps);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            throw new RuntimeException("Could not pre-load concepts " + ex);
//        } finally {
//            Context.removeProxyPrivilege("SQL Level Access");
//            Context.removeProxyPrivilege("View Concept Classes");
//            Context.removeProxyPrivilege("View Concepts");
//            Context.removeProxyPrivilege("Manage Concepts");
//            Context.removeProxyPrivilege("View Global Properties");
//            Context.removeProxyPrivilege("Manage Global Properties");
//            Context.removeProxyPrivilege("View Forms");
//            Context.removeProxyPrivilege("Manage Forms");
//            Context.closeSession();
//            
//            log.info("Finished loading namephonetics metadata.");
//        }   
//    }
    
    /**
     * Called when module is being started
     */
    protected void onStartup() {        
    }
    
    /**
     * Called after module application context has been loaded. There is no authenticated
     * user so all required privileges must be added as proxy privileges
     */
//    protected void onLoad(NamePhoneticsService nps) {     
//       nps.registerProcessor("Soundex", new Soundex().getClass().getName());
//       nps.registerProcessor("Refined Soundex", new RefinedSoundex().getClass().getName());
//       nps.registerProcessor("Metaphone", new Metaphone().getClass().getName());
//       nps.registerProcessor("Double Metaphone", new DoubleMetaphone().getClass().getName());
//       nps.registerProcessor("Chichewa Soundex", new ChichewaSoundex().getClass().getName());
//       nps.registerProcessor("Kinyarwanda Soundex", new KinyarwandaSoundex().getClass().getName());
//       nps.registerProcessor("Caverphone", new Caverphone().getClass().getName());
//    }
    
    /**
     * Called when module is being shutdown
     */
    protected void onShutdown() {       
    }
    

	
}
