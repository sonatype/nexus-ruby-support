/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.ruby;


/**
 * helper  to purge broken files or to recreate the complete specs index 
 * of hosted rubygems repository.
 * 
 * @author christian
 */
public interface RepairHelper {
  
  /**
   * recreate the complete rubygems specs index
   * 
   * @param directory where the repository is located
   */
  void recreateRubygemsIndex( String directory );
  
  /**
   * purge broken dependency files
   *
   * @param directory where the repository is located
   */
  void purgeBrokenDepencencyFiles( String directory );
  
  /**
   *  purge broken gemspec files
   *  
   * @param directory where the repository is located
   */
  void purgeBrokenGemspecFiles( String directory );
  
}