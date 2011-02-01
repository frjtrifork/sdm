package com.trifork.sdm.replication.admin.security;


import static java.lang.annotation.ElementType.*;

import java.lang.annotation.*;
import java.util.*;

import com.google.inject.*;
import com.trifork.sdm.replication.util.ConfiguredModule;


public class WhitelistModule extends ConfiguredModule
{
	@Override
	protected void configureServlets()
	{
		String[] cvrNumbers = getConfig().getStringArray("whitelist");
		Set<String> whitelist = new HashSet<String>(Arrays.asList(cvrNumbers));

		if (whitelist.size() == 0)
		{
			addError("No CVR-numbers have been whitelisted. Change the configuration file.");
		}
		else
		{
			bind(new TypeLiteral<Set<String>>()
			{
			}).annotatedWith(Whitelist.class).toInstance(whitelist);
		}
	}


	@BindingAnnotation
	@Target({ FIELD, PARAMETER, METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Whitelist
	{
	}
}
