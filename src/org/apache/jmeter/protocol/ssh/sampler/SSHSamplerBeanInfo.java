/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.apache.jmeter.protocol.ssh.sampler;

import java.beans.PropertyDescriptor;

import org.apache.jmeter.testbeans.BeanInfoSupport;

public class SSHSamplerBeanInfo extends BeanInfoSupport {

	public SSHSamplerBeanInfo() {
		super(SSHSampler.class);
		
		createPropertyGroup("host", // $NON-NLS-1$
				new String[]{
				"hostname", // $NON-NLS-1$
				"port", // $NON-NLS-1$
				"connectionTimeout"
		});
		
		createPropertyGroup("user", // $NON-NLS-1$
				new String[]{
				"username", // $NON-NLS-1$
				"password" // $NON-NLS-1$
		});

		createPropertyGroup("Key File",
				    new String[] {
					"sshkeyfile",
					"passphrase"
				    });
		
		createPropertyGroup("execute", new String[]{
				"command" // $NON-NLS-1$
		});
		
		PropertyDescriptor p;
		p = property("username"); // $NON-NLS-1$
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "");
		
		p = property("password"); // $NON-NLS-1$
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "");

		p = property("sshkeyfile");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "");

		p = property("passphrase");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "");
		
		p = property("hostname"); // $NON-NLS-1$
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "");
		
		p = property("port"); // $NON-NLS-1$
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, new Integer(22));

		p = property("connectionTimeout");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, new Integer(5000));
		
		p = property("command"); // $NON-NLS-1$
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "date");
	}
}
