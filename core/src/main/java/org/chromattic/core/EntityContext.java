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

package org.chromattic.core;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.chromattic.api.Status;
import org.chromattic.api.format.ObjectFormatter;
import org.chromattic.common.logging.Logger;
import org.chromattic.core.jcr.info.NodeTypeInfo;
import org.chromattic.core.mapper.PrimaryTypeMapper;
import org.chromattic.core.jcr.LinkType;

import javax.jcr.RepositoryException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public final class EntityContext extends ObjectContext{

  /** The logger. */
  private static final Logger log = Logger.getLogger(EntityContext.class);

  /** The related type. */
  final PrimaryTypeMapper mapper;

  /** The object instance. */
  final Object object;

  /** The property map. */
  final PropertyMap properties;

  /** The list of mixins. */
  final Map<Class, MixinContext> mixins;

  /** The related state. */
  EntityContextState state;

  EntityContext(PrimaryTypeMapper mapper, EntityContextState state) throws RepositoryException {
    this.state = null;
    this.mapper = mapper;
    this.object = mapper.createObject(this);
    this.state = state;
    this.properties = new PropertyMap(this);
    this.mixins = new HashMap<Class, MixinContext>();
  }

  public DomainSession getSession() {
    return state.getSession();
  }

  public Status getStatus() {
    return state.getStatus();
  }

  public Object getObject() {
    return object;
  }

  @Override
  public EntityContext getEntity() {
    return this;
  }

  public NodeTypeInfo getTypeInfo() {
    EntityContextState state = getEntity().state;
    return state.getTypeInfo();
  }

  public void addMixin(MixinContext mixinCtx) {
    state.getSession().addMixin(this, mixinCtx);
  }

  public MixinContext getMixin(Class<?> mixinClass) {
    return state.getSession().getMixin(this, mixinClass);
  }

  public String getAttribute(NodeAttributeType type) {
    DomainSession session = state.getSession();
    switch (type) {
      case NAME:
        return state.getName();
      case ID:
        return state.getId();
      case PATH:
        return state.getPath();
      case WORKSPACE_NAME:
        return session.getJCRSession().getWorkspace().getName();
      default:
        throw new AssertionError();
    }
  }

  public void remove() {
    state.getSession().remove(this);
  }

  public <T> Iterator<T> getReferents(final String name, Class<T> filterClass, LinkType linkType) {
    return state.getSession().getReferents(this, name, filterClass, linkType);
  }

  public String getName() {
    return getAttribute(NodeAttributeType.NAME);
  }

  public String getId() {
    return getAttribute(NodeAttributeType.ID);
  }

  public String getPath() {
    return getAttribute(NodeAttributeType.PATH);
  }

  public void setName(String name) {
    state.getSession().setName(this, name);
  }

  public Object getReferenced(String name, LinkType linkType) {
    return state.getSession().getReferenced(this, name, linkType);
  }

  public void setReferenced(String name, Object referenced, LinkType linkType) {
    DomainSession session = state.getSession();
    EntityContext referencedCtx = null;
    if (referenced != null) {
      referencedCtx = session.unwrapEntity(referenced);
    }

    //
    session.setReferenced(this, name, referencedCtx, linkType);
  }

  public boolean addReference(String name, Object referent, LinkType linkType) {
    DomainSession session = state.getSession();
    EntityContext referentCtx = session.unwrapEntity(referent);
    return session.setReferenced(referentCtx, name, this, linkType);
  }

  public Map<String, Object> getPropertyMap() {
    return properties;
  }

  public void removeChild(String name) {
    if (getStatus() != Status.PERSISTENT) {
      throw new IllegalStateException("Can only insert/remove a child of a persistent object");
    }

    //
    state.getSession().removeChild(this, name);
  }

  public void orderBefore(EntityContext srcCtx, EntityContext dstCtx) {
    state.getSession().orderBefore(this, srcCtx, dstCtx);
  }

  public void addChild(EntityContext childCtx) {
    String name = childCtx.state.getName();
    addChild(name, childCtx);
  }

  public void addChild(Object child) {
    DomainSession session = state.getSession();
    EntityContext childCtx = session.unwrapEntity(child);
    addChild(childCtx);
  }

  public void addChild(String name, EntityContext childCtx) {
    if (childCtx.getStatus() == Status.PERSISTENT) {
      state.getSession().move(childCtx, this);
    } else {
      state.getSession().persist(this, childCtx, name);
    }
  }

  public void addChild(String name, Object child) {
    DomainSession session = state.getSession();
    EntityContext childCtx = session.unwrapEntity(child);
    addChild(name, childCtx);
  }

  public Object getChild(String name) {
    return state.getSession().getChild(this, name);
  }

  public <T> Iterator<T> getChildren(Class<T> filterClass) {
    return state.getSession().getChildren(this, filterClass);
  }

  public Object getParent() {
    return state.getSession().getParent(this);
  }


  /**
   * Finds a suitable formatter scoped for this entity context. The returned value might
   * change over method calls (i.e it would not be a good idea to cache it for a long time).
   *
   * @return the object formatter
   */
  private ObjectFormatter getFormatter()
  {
    // Find a formatter
    ObjectFormatter formatter = mapper.getFormatter();
    if (formatter == null)
    {
      formatter = getSession().domain.objectFormatter;
    }
    return formatter;
  }

  public Object invoke(Object o, Method method, Object[] args) throws Throwable {
    return mapper.invoke(this, method, args);
  }

  @Override
  public String toString() {
    return "EntityContext[state=" + state + ",mapper=" + mapper + "]";
  }
}
