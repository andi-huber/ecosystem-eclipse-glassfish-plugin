/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.ui.serverview;

import java.util.ArrayList;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource2;

public class TreeNode implements IPropertySource2{

	TreeNode parent = null;
	String name = null;
	String type = null;

	ArrayList<TreeNode> childModules = new ArrayList<TreeNode>();
	
	/*
	 * type is ear, war, ejb etc
	 */
	TreeNode(String name, String type, TreeNode parent){
		this.parent = parent;
		this.name = name;
		this.type = type;
	}
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	public void addChild(TreeNode childModule) {
		childModules.add(childModule);		
	}
	public Object[] getChildren() {
		return childModules.toArray();
	}
	
	public TreeNode getParent() {
		return parent;
	}


	
	
	@Override
	public Object getEditableValue() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
        ArrayList< IPropertyDescriptor > properties = new ArrayList< IPropertyDescriptor >();
       // PropertyDescriptor pd;


        return properties.toArray( new IPropertyDescriptor[ 0 ] );
	}
	@Override
	public Object getPropertyValue(Object id) {

		return null;
	}
	@Override
	public void resetPropertyValue(Object arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setPropertyValue(Object arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean isPropertyResettable(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean isPropertySet(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
