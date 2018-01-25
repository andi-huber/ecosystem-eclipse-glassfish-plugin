/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools;

import static org.eclipse.glassfish.tools.utils.JdtUtil.findOrCreateJvm;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jst.server.core.IJavaRuntime;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyBinding;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Resource;
import org.eclipse.sapphire.ValuePropertyBinding;
import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.VersionConstraint;
import org.eclipse.sapphire.platform.StatusBridge;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.RuntimeWorkingCopy;
import org.eclipse.wst.server.core.model.RuntimeDelegate;

@SuppressWarnings("restriction")
public final class GlassFishRuntime extends RuntimeDelegate implements IJavaRuntime {

    public static final String TYPE_ID = "glassfish";
    public static final IRuntimeType TYPE = ServerCore.findRuntimeType( TYPE_ID );
    
	public static final String ATTR_SERVER_ROOT = "server.root"; //$NON-NLS-1$
	public static final String ATTR_SERVER_JDK = "server.jdk";
	
	public final static boolean IS_MACOSX = Platform.OS_MACOSX.equals(Platform.getOS());
	
	public static final String DEFAULT_JRE_KEY = "###DefaultJREForGlassFishCode###";
	
	private static final VersionConstraint VERSION_CONSTRAINT_3_1 = new VersionConstraint( "[1.6-1.7]" );
	private static final VersionConstraint VERSION_CONSTRAINT_4 = new VersionConstraint( "[1.7" );
	private static final VersionConstraint VERSION_CONSTRAINT_5 = new VersionConstraint( "[1.8" );

	private IGlassfishRuntimeModel model;
	
    public static String createDefaultRuntimeName( final Version version )
    {
        String baseName = "GlassFish";

        if( version != null )
        {
            if( version.matches( "[5-6)" ) )
            {
                baseName += " 5";
            }
            else if( version.matches( "[4-5)" ) )
            {
                baseName += " 4";
            }
            else if( version.matches( "[3.1-4)" ) )
            {
                baseName += " 3.1";
            }
        }
        
        int counter = 1;
        
        while( true )
        {
            final String name = createDefaultRuntimeName( baseName, counter );
            
            if( ServerCore.findRuntime( name ) == null )
            {
                return name;
            }
            
            counter++;
        }
    }
    
    private static String createDefaultRuntimeName( final String baseName, final int counter )
    {
        final StringBuilder buf = new StringBuilder();
        
        buf.append( baseName );
        
        if( counter != 1 )
        {
            buf.append( " (" );
            buf.append( counter );
            buf.append( ')' );
        }
        
        return buf.toString();
    }
	
	public Version getVersion()
	{
	    final IPath location = getRuntime().getLocation();
	    
	    if( location != null )
	    {
	        final GlassFishInstall gfInstall = GlassFishInstall.find( location.toFile() );
	        
	        if( gfInstall != null )
	        {
	            return gfInstall.version();
	        }
	    }
	    
	    return null;
	}
	
	public IStatus validateVersion()
	{
        final Version version = getVersion();
        
        if (version == null) {
            // should not happen if called after validateServerLocation
            return new Status(Status.ERROR, GlassfishToolsPlugin.SYMBOLIC_NAME, Messages.runtimeNotValid);
        }
        if (!version.matches("[3.1-6)")) {
            return new Status(Status.ERROR, GlassfishToolsPlugin.SYMBOLIC_NAME, Messages.unsupportedVersion);
        }
        return Status.OK_STATUS;
	}
	
	public VersionConstraint getJavaVersionConstraint()
	{
	    final Version version = getVersion();
	    
	    if( version != null )
	    {
            if( version.matches( "[5" ) )
            {
                return VERSION_CONSTRAINT_5;
            }
            else if( version.matches( "[4" ) )
    	    {
    	        return VERSION_CONSTRAINT_4;
    	    }
    	    else
    	    {
    	        return VERSION_CONSTRAINT_3_1;
    	    }
	    }
	    
	    return null;
	}
	
	public synchronized IGlassfishRuntimeModel getModel()
	{
        if( this.model == null )
        {
            this.model = IGlassfishRuntimeModel.TYPE.instantiate(new ConfigResource(getRuntime()));
            this.model.initialize();
        }
        
        return this.model;
	}
	
	@Override
	public IVMInstall getVMInstall()
	{
		return findOrCreateJvm( getModel().getJavaRuntimeEnvironment().text() );
	}
	
    @Override
	public boolean isUsingDefaultJRE() {
		return false;
	}

	IStatus validateServerLocation() {
		IPath location = getRuntime().getLocation();
		// this is maybe redundant check to GUI annotation but
		// needed in case where GUI is not involved
		if (location == null || !location.toFile().exists())   {
			return new Status(IStatus.ERROR, GlassfishToolsPlugin.SYMBOLIC_NAME, NLS.bind(Messages.pathDoesNotExist, "Specified path"));
		}
		GlassFishInstall gfInstall = GlassFishInstall.find(location.toFile());
		if (gfInstall == null) {
			return new Status(IStatus.ERROR, GlassfishToolsPlugin.SYMBOLIC_NAME, Messages.notValidGlassfishInstall);
		}
		return Status.OK_STATUS;
	}
	
	
	@Override
	
	public IStatus validate()
	{
		IStatus status = super.validate();
		
		if( status.isOK() )
		{
		    status = StatusBridge.create( getModel().validation() );
		}

		return status;
	}
	
	@Override
	
    public void dispose()
    {
        super.dispose();
        
        synchronized( this )
        {
            if( this.model != null )
            {
                this.model.dispose();
                this.model = null;
            }
        }
    }

    private static final class ConfigResource extends Resource
    {
		private final IRuntime runtime;
		
		public ConfigResource( final IRuntime runtime )
		{
			super( null );
			
			if( runtime == null )
			{
			    throw new IllegalArgumentException();
			}
			
			this.runtime = runtime;
		}

		@Override
		protected PropertyBinding createBinding(Property property) {
			final PropertyDef p = property.definition();
            
            if( p == IGlassfishRuntimeModel.PROP_NAME )
            {
            	return new ValuePropertyBinding()
            	{
                    @Override
                    
                    public String read()
                    {
                        return ConfigResource.this.runtime.getName();
                    }
                    
					@Override
					
					public void write( final String value )
					{
					    if( ConfigResource.this.runtime instanceof RuntimeWorkingCopy )
					    {
					        final RuntimeWorkingCopy wc = (RuntimeWorkingCopy) ConfigResource.this.runtime;
					        wc.setName( value );
					    }
					    else
					    {
					        throw new UnsupportedOperationException();
					    }
					}
				};
            }
            else if( p == IGlassfishRuntimeModel.PROP_JAVA_RUNTIME_ENVIRONMENT )
            {
            	return new AttributeValueBinding( this.runtime, ATTR_SERVER_JDK);
            }
            else if( p == IGlassfishRuntimeModel.PROP_SERVER_ROOT )
            {
            	return new ValuePropertyBinding()
            	{
                    @Override
                    
                    public String read()
                    {
                        final IPath path = ConfigResource.this.runtime.getLocation();
                        return path == null ? null : path.toOSString();
                    }
                    
                    @Override
                    
                    public void write( final String value )
                    {
                        if( ConfigResource.this.runtime instanceof RuntimeWorkingCopy )
                        {
                            final RuntimeWorkingCopy wc = (RuntimeWorkingCopy) ConfigResource.this.runtime;
                            final IPath path = value == null ? Path.EMPTY : new Path(value);
                            wc.setLocation( path );
                        }
                        else
                        {
                            throw new UnsupportedOperationException();
                        }
                    }
				};
            }
            
            throw new IllegalStateException();
		}

		@Override
		
		public <A> A adapt( final Class<A> adapterType )
		{
			if( adapterType == IRuntime.class )
            {
                return adapterType.cast( this.runtime );
            }
            
            return super.adapt( adapterType );
		}
	}
	
	private static final class AttributeValueBinding extends ValuePropertyBinding
    {
        private final IRuntime runtime;
        private final String attribute;
        
        public AttributeValueBinding( final IRuntime runtime, final String attribute )
        {
            this.runtime = runtime;
            this.attribute = attribute;
        }
        
        @Override
        
        public String read()
        {
            return ( (org.eclipse.wst.server.core.internal.Runtime) this.runtime ).getAttribute( this.attribute, (String) null );
        }
        
		@Override
		
        public void write( final String value )
        {
            if( this.runtime instanceof RuntimeWorkingCopy )
            {
                final RuntimeWorkingCopy wc = (RuntimeWorkingCopy) this.runtime;
                wc.setAttribute( this.attribute, value );
            }
            else
            {
                throw new UnsupportedOperationException();
            }
        }
    }

}
