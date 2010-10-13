/**
 *  ServingXML
 *
 *  Copyright (C) 2006  Daniel Parker
 *    daniel.parker@servingxml.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **/

package com.servingxml.app;

import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;

import com.servingxml.ioc.resources.IocContainer;
import com.servingxml.util.system.AbstractRuntimeContext;
import com.servingxml.util.system.Logger;
import com.servingxml.util.system.SystemConfiguration;

/**
 *
 *
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class DefaultAppContext extends AbstractRuntimeContext implements
		AppContext {
	private final String appName;
	private final IocContainer resources;
	private SAXTransformerFactory transformerFactory;
	private final Map<String, Object> attributes = new HashMap<String, Object>();

	public DefaultAppContext(String appName, IocContainer resources) {
		super(SystemConfiguration.getLogger());
		this.appName = appName;
		this.resources = resources;
		try {
			transformerFactory = (SAXTransformerFactory) TransformerFactory
					.newInstance();
		} catch (Exception e) {
		}
	}

	public DefaultAppContext(String appName, IocContainer resources,
			Logger logger) {
		super(logger);
		this.resources = resources;
		this.appName = appName;
		try {
			transformerFactory = (SAXTransformerFactory) TransformerFactory
					.newInstance();
		} catch (Exception e) {
		}
	}

	public Object getProperty(String name) {
		return attributes.get(name);
	}

	public Object setProperty(String name, Object value) {
		Object oldValue = attributes.put(name, value);
		if (value == null)
			attributes.remove(name);
		return oldValue;
	}

	public String getAppName() {
		return appName;
	}

	public String getUser() {
		return "";
	}

	public IocContainer getResources() {
		return resources;
	}

	public SAXTransformerFactory getTransformerFactory() {
		return transformerFactory;
	}
}
