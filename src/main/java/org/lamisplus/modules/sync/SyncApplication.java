package org.lamisplus.modules.sync;

import com.foreach.across.config.AcrossApplication;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.configurer.ComponentScanConfigurer;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.web.AcrossWebModule;
import org.lamisplus.modules.base.BaseModule;
import org.springframework.boot.SpringApplication;

@AcrossApplication( modules = {
		AcrossHibernateJpaModule.NAME, AcrossWebModule.NAME, BaseModule.NAME},
		modulePackageClasses = {BaseModule.class})
public class SyncApplication extends AcrossModule {

	public static final String name = "SyncModule";

	public static void main(String[] args) {
		SpringApplication.run(SyncApplication.class, args);
	}

	public SyncApplication(){
		super();
		addApplicationContextConfigurer(new ComponentScanConfigurer(
				getClass().getPackage().getName() + "config",
				getClass().getPackage().getName() + "controller",
				getClass().getPackage().getName() + "domain",
				getClass().getPackage().getName() + "repository",
				getClass().getPackage().getName() + "service"
		));
	}

	@Override
	public String getName() {
		return name;
	}
}
