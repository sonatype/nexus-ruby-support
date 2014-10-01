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
package org.sonatype.nexus.ruby;

import org.jruby.embed.ScriptingContainer;

public class ScriptWrapper
{
  protected final ScriptingContainer scriptingContainer;

  private final Object object;

  public ScriptWrapper(ScriptingContainer scriptingContainer) {
    this.scriptingContainer = scriptingContainer;
    this.object = newScript();
  }

  public ScriptWrapper(ScriptingContainer scriptingContainer, Object object) {
    this.scriptingContainer = scriptingContainer;
    this.object = object;
  }

  protected Object newScript() {
    throw new RuntimeException("not overwritten");
  }

  protected void callMethod(String methodName, Object singleArg) {
    scriptingContainer.callMethod(object, methodName, singleArg);
  }

  protected <T> T callMethod(String methodName, Object singleArg, Class<T> returnType) {
    return scriptingContainer.callMethod(object, methodName, singleArg, returnType);
  }

  protected <T> T callMethod(String methodName, Object[] args, Class<T> returnType) {
    return scriptingContainer.callMethod(object, methodName, args, returnType);
  }

  protected <T> T callMethod(String methodName, Class<T> returnType) {
    return scriptingContainer.callMethod(object, methodName, returnType);
  }

  protected void callMethod(String methodName) {
    scriptingContainer.callMethod(object, methodName);
  }
}