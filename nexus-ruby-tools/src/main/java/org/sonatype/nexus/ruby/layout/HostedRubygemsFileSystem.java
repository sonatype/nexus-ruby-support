/*
 * Copyright (c) 2007-2014 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.ruby.layout;

import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.cuba.DefaultRubygemsFileSystem;

/**
 * this class assembles the hosted repository for GET, POST and DELETE request.
 *
 * @author christian
 */
public class HostedRubygemsFileSystem
    extends DefaultRubygemsFileSystem
{
  public HostedRubygemsFileSystem(RubygemsGateway gateway, Storage store) {
    super(new DefaultLayout(),
        new HostedGETLayout(gateway, store),
        new HostedPOSTLayout(gateway, store),
        new HostedDELETELayout(gateway, store));
  }
}