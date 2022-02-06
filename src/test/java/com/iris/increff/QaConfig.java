package com.iris.increff;

import com.iris.increff.spring.SpringConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
@ComponentScan(basePackages = { "com.iris.increff" },
		excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, value = { SpringConfig.class }))
@PropertySources({ //
		@PropertySource(value = "file:./toyIRISTest.properties", ignoreResourceNotFound = true) //
})
public class QaConfig {


}
