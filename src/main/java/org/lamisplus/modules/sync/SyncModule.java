//package org.lamisplus.modules.sync;
//
//import com.foreach.across.config.AcrossApplication;
//import com.foreach.across.core.AcrossModule;
//import com.foreach.across.core.context.configurer.ComponentScanConfigurer;
//import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
//
//@AcrossApplication(
//        modules = {
//                AcrossHibernateJpaModule.NAME
//        })
//public class SyncModule  extends AcrossModule {
//
//    private static final String NAME = "SyncModule";
//
//    public SyncModule() {
//        super ();
//        addApplicationContextConfigurer (new ComponentScanConfigurer (
//                getClass ().getPackage ().getName () + ".repository",
//                getClass ().getPackage ().getName () + ".service",
//                getClass ().getPackage ().getName () + ".controller"
//        ));
//    }
//
//    @Override
//    public String getName() {
//        return NAME;
//    }
//}
