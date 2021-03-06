/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.chromattic.docs.reference.website;

import org.chromattic.api.ChromatticSession;
import org.chromattic.docs.reference.AbstractTestCase;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ReferenceMappingTestCase extends AbstractTestCase {

  @Override
  protected Iterable<Class<?>> classes() {
    return Arrays.asList(WebSite.class, Page.class, Content.class);
  }

  public void testOneToManyReference() {
    ChromatticSession session = login();
    WebSite site = session.insert(WebSite.class, "site");
    Content content1 = session.create(Content.class, "1");
    Content content2 = session.create(Content.class, "2");
    site.getContents().add(content1);
    Page root = session.create(Page.class);
    site.setRootPage(root);

    //
    root.setContent(content1);
  }
}