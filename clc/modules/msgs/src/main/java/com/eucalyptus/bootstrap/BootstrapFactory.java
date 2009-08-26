package com.eucalyptus.bootstrap;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Logger;
import org.mule.config.ConfigResource;

import com.eucalyptus.util.BaseDirectory;
import com.eucalyptus.util.EucalyptusProperties;
import com.eucalyptus.util.LogUtils;
import com.eucalyptus.util.ServiceJarFile;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class BootstrapFactory {
  private static Logger                               LOG           = Logger.getLogger( BootstrapFactory.class );
  private static Multimap<Resource, ResourceProvider> resources     = Multimaps.newArrayListMultimap( );
  private static Multimap<Resource, Bootstrapper>     bootstrappers = Multimaps.newArrayListMultimap( );

  public static void initResourceProviders( ) {
    for ( Resource r : Resource.values( ) ) {
      for ( ResourceProvider p : r.getProviders( ) ) {
        LOG.info( "Loaded " + LogUtils.dumpObject( p ) );
      }
    }
  }

  public static void initConfigurationResources( ) throws IOException {
    for ( Resource r : Resource.values( ) ) {
      for ( ResourceProvider p : r.initProviders( ) ) {
        LOG.info( "Loading resource provider:" + p.getName( ) + " -- " + p.getOrigin( ) );
        for ( ConfigResource cfg : p.getConfigurations( ) ) {
          LOG.info( "-> " + cfg.getUrl( ) );
        }
      }
    }
  }

  public static void initBootstrappers( ) {
    File libDir = new File( BaseDirectory.LIB.toString( ) );
    for ( File f : libDir.listFiles( ) ) {
      if ( f.getName( ).startsWith( EucalyptusProperties.NAME ) && f.getName( ).endsWith( ".jar" ) && !f.getName( ).matches( ".*-ext-.*" ) ) {
        LOG.debug( "Found eucalyptus component jar: " + f.getName( ) );
        ServiceJarFile jar;
        try {
          jar = new ServiceJarFile( f );
        } catch ( IOException e ) {
          LOG.error( e.getMessage( ) );
          continue;
        }
        List<Bootstrapper> bsList = jar.getBootstrappers( );
        for ( Bootstrapper bootstrap : bsList ) {
          for ( Resource r : Resource.values( ) ) {
            if ( r.providedBy( bootstrap.getClass( ) ) || Resource.Nothing.equals( r ) ) {
              Provides provides = bootstrap.getClass( ).getAnnotation( Provides.class );
              if( provides == null ) {
                LOG.info( "-X Skipping bootstrapper " + bootstrap.getClass( ).getSimpleName( ) + " since Provides is not specified." );
              } else {
                Component component = provides.component( );
                if ( component != null && !component.isEnabled( ) ) {
                  LOG.info( "-X Skipping bootstrapper " + bootstrap.getClass( ).getSimpleName( ) + " since Provides.component=" + component.toString( ) + " is disabled." );
                  break;
                }
                if ( checkDepends( bootstrap ) ) {
                  r.add( bootstrap );
                  LOG.info( "-> Associated bootstrapper " + bootstrap.getClass( ).getSimpleName( ) + " with resource " + r.toString( ) + "." );
                  break;
                }
              }
            }
          }
        }
      }
    }
  }

  private static boolean checkDepends( Bootstrapper bootstrap ) {
    Depends depends = bootstrap.getClass( ).getAnnotation( Depends.class );
    if( depends == null ) return true;
    for ( Component c : depends.local( ) ) {
      if ( !c.isLocal( ) ) {
        LOG.info( "-X Skipping bootstrapper " + bootstrap.getClass( ).getSimpleName( ) + " since Depends.local=" + c.toString( ) + " is remote." );
        return false;
      }
    }
    for ( Component c : depends.remote( ) ) {
      if ( c.isLocal( ) ) {
        LOG.info( "-X Skipping bootstrapper " + bootstrap.getClass( ).getSimpleName( ) + " since Depends.remote=" + c.toString( ) + " is local." );
        return false;
      }
    }
    return true;
  }

}
