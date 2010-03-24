package org.sonatype.nexus.ruby.gem.yaml;

import java.util.Map;

import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

/**
 * A helper for snakeYaml.
 * 
 * @author cstamas
 */
public class MappingRepresenter
    extends Representer
{
    public MappingRepresenter( final Map<String, Class<?>> tag2class )
    {
        super();

        this.nullRepresenter = new RepresentNull();

        for ( Map.Entry<String, Class<?>> entry : tag2class.entrySet() )
        {
            addClassTag( entry.getValue(), entry.getKey() );
        }
    }

    private class RepresentNull
        implements Represent
    {
        public Node representData( Object data )
        {
            return representScalar( Tag.NULL, "" );
        }
    }
}
