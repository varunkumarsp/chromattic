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

package org.chromattic.test.onetomany.hierarchical;

import org.chromattic.test.AbstractTestCase;
import org.chromattic.core.DomainSession;
import org.chromattic.api.ChromatticSession;

import javax.jcr.Node;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class AbstractOneToTestCase<O, M> extends AbstractTestCase {

  /** . */
  private Class<O> oneSide = getOneSideClass();

  /** . */
  private Class<M> manySide = getManySideClass();

  protected void createDomain() {
    addClass(oneSide);
    addClass(manySide);
  }

  public abstract Class<O> getOneSideClass();

  public abstract Class<M> getManySideClass();

  public abstract void setOne(M many, O one);

  public abstract O getOne(M many);

  public void testAdd() throws Exception {
    DomainSession session = login();
    Node rootNode = session.getJCRSession().getRootNode();

    //
    Node aNode = rootNode.addNode("totm_a_a", "totm_a");
    O a = session.findByNode(oneSide, aNode);
    assertNotNull(a);

    //
    Node bNode = aNode.addNode("b", "totm_b");
    M b = session.findByNode(manySide, bNode);
    assertEquals(a, getOne(b));
  }

  public void testLoad() throws Exception {
    DomainSession session = login();
    Node rootNode = session.getJCRSession().getRootNode();
    Node aNode = rootNode.addNode("totm_a_b", "totm_a");
    String aId = aNode.getUUID();
    Node bNode = aNode.addNode("b", "totm_b");
    String bId = bNode.getUUID();
    rootNode.save();

    //
    session = login();
    O a = session.findById(oneSide, aId);
    assertNotNull(a);
    M b = session.findById(manySide, bId);
    assertEquals(a, getOne(b));
  }

  public void testTransientGetParent() throws Exception {
    ChromatticSession session = login();
    M b = session.create(manySide, "totm_b_c");
    try {
      getOne(b);
    }
    catch (IllegalStateException expected) {
    }
  }

  public void testRemovedGetParent() throws Exception {
    DomainSession session = login();
    Node rootNode = session.getJCRSession().getRootNode();
    Node aNode = rootNode.addNode("totm_a_b", "totm_a");
    String aId = aNode.getUUID();
    Node bNode = aNode.addNode("b", "totm_b");
    String bId = bNode.getUUID();
    rootNode.save();

    session = login();
    M b = session.findById(manySide, bId);
    session.remove(b);
    try {
      getOne(b);
    }
    catch (IllegalStateException expected) {
    }
  }
}