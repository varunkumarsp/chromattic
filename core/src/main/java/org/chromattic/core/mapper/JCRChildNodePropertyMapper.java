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

package org.chromattic.core.mapper;

import org.chromattic.core.EntityContext;
import org.chromattic.core.MixinContext;
import org.chromattic.core.bean.BeanValueInfo;
import org.chromattic.core.bean.SingleValuedPropertyInfo;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class JCRChildNodePropertyMapper extends JCRNodePropertyMapper<EntityContext> {

  public JCRChildNodePropertyMapper(SingleValuedPropertyInfo<BeanValueInfo> info) throws ClassNotFoundException {
    super(EntityContext.class, info);
  }

  @Override
  public Object get(EntityContext context) throws Throwable {
    Object parent = context.getParent();
    Class<?> relatedClass =  getRelatedClass();

    // If it fits the parent use it
    if (relatedClass.isInstance(parent)) {
      return parent;
    } else {

      // A bit stupid to unwrap something we could get unwrapped
      EntityContext parentCtx = context.getSession().unwrapEntity(parent);



      MixinContext mixinCtx = parentCtx.getMixin(relatedClass);

      if (mixinCtx != null) {
        return mixinCtx.getObject();
      }


      return null;
    }
  }
}
