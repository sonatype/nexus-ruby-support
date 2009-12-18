package org.sonatype.nexus.ruby.gem.yaml;

import java.util.Map;

import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * A helper for snakeYaml.
 * 
 * @author cstamas
 */
public class MappingConstructor
    extends Constructor
{
    public MappingConstructor( Map<String, Class<?>> tag2class )
    {
        this( tag2class, Object.class );
    }

    public MappingConstructor( Map<String, Class<?>> tag2class, Class<? extends Object> theRoot )
    {
        super( theRoot );

        for ( Map.Entry<String, Class<?>> entry : tag2class.entrySet() )
        {
            addTypeDescription( new TypeDescription( entry.getValue(), entry.getKey() ) );
        }
    }
}
