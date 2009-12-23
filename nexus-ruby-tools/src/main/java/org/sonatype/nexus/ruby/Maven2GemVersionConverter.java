package org.sonatype.nexus.ruby;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

import org.codehaus.plexus.util.StringUtils;

public class Maven2GemVersionConverter
{
    /**
     * This is the pattern we match against. This is actually x.y.z... version format, that RubyGems support.
     */
    public static final Pattern gemVersionPattern = Pattern.compile( "\\d+(\\.\\d+)*(\\.[a-zA-Z])?" );

    /**
     * Creates valid GEM version out of Maven2 version. Gem versions are "stricter" than Maven versions: they are in
     * form of "x.y.z...". They have to start with integer, and be followed by a '.'. You can have as many like these
     * you want, but Maven version like "1.0-alpha-2" is invalid Gem version. Hence, some trickery has to be applied.
     * 
     * @param mavenVersion
     * @return
     */
    public String createGemVersion( String mavenVersion )
    {
        if ( gemVersionPattern.matcher( mavenVersion ).matches() )
        {
            return mavenVersion;
        }
        else
        {
            StringBuilder gemVersion = new StringBuilder();

            StringBuilder currentRegion = new StringBuilder();

            ChunkIterator chunkIterator = new ChunkIterator( mavenVersion );

            boolean artificialHardBreak = false;

            while ( chunkIterator.hasNext() )
            {
                String chunk = chunkIterator.next().toLowerCase();

                if ( ".".equals( chunk ) || "-".equals( chunk ) )
                {
                    continue;
                }
                else if ( !StringUtils.isNumeric( chunk ) )
                {
                    if ( !chunkIterator.hasNext() )
                    {
                        if ( currentRegion.length() > 0 )
                        {
                            currentRegion.append( "." );
                        }

                        if ( gemVersion.length() == 0 )
                        {
                            currentRegion.append( "0." );
                        }

                        currentRegion.append( chunk.substring( 0, 1 ) );

                        artificialHardBreak = true;
                    }
                    else if ( "alpha".equals( chunk ) )
                    {
                        if ( currentRegion.length() > 0 )
                        {
                            currentRegion.append( "." );
                        }

                        currentRegion.append( "0" );

                        artificialHardBreak = true;
                    }
                    else if ( "beta".equals( chunk ) )
                    {
                        if ( currentRegion.length() > 0 )
                        {
                            currentRegion.append( "." );
                        }

                        currentRegion.append( "1" );

                        artificialHardBreak = true;
                    }
                    else if ( chunk.contains( "pre" ) )
                    {
                        artificialHardBreak = true;
                    }
                    else
                    {
                        chunk = null;
                    }
                }
                else
                {
                    currentRegion.append( chunk );
                }

                if ( chunkIterator.isHardBreak() || artificialHardBreak )
                {
                    // add it to result
                    gemVersion.append( currentRegion.toString() );

                    gemVersion.append( "." );

                    currentRegion = new StringBuilder();

                    artificialHardBreak = false;
                }
            }

            return gemVersion.toString().substring( 0, gemVersion.length() - 1 );
        }
    }

    // ==

    private static class ChunkIterator
        implements Iterator<String>
    {
        private final String mavenVersion;

        private final int mavenVersionLength;

        private int lastMarker = 0;

        private Set<Character> hardBreakers;

        private boolean hardBreak;

        public ChunkIterator( String mavenVersion )
        {
            this.mavenVersion = mavenVersion.trim();

            this.mavenVersionLength = this.mavenVersion.length();

            this.hardBreakers = new HashSet<Character>();

            // default ones
            this.hardBreakers.add( '.' );
            this.hardBreakers.add( '-' );
        }

        public boolean hasNext()
        {
            return lastMarker < mavenVersionLength
                && StringUtils.isNotBlank( getChunk( mavenVersion, mavenVersionLength, lastMarker ) );
        }

        public String next()
        {
            String chunk = getChunk( mavenVersion, mavenVersionLength, lastMarker );

            lastMarker += chunk.length();

            return chunk;
        }

        public void remove()
        {
            // unsupported
        }

        public boolean isHardBreak()
        {
            return hardBreak;
        }

        // ==

        private final String getChunk( String s, int slength, int marker )
        {
            StringBuilder chunk = new StringBuilder();
            char c = s.charAt( marker );
            chunk.append( c );
            marker++;
            if ( !hardBreakers.contains( c ) )
            {
                if ( Character.isDigit( c ) )
                {
                    while ( marker < slength )
                    {
                        c = s.charAt( marker );
                        if ( !Character.isDigit( c ) )
                            break;
                        chunk.append( c );
                        marker++;
                    }
                }
                else
                {
                    while ( marker < slength )
                    {
                        c = s.charAt( marker );
                        if ( Character.isDigit( c ) || hardBreakers.contains( c ) )
                            break;
                        chunk.append( c );
                        marker++;
                    }
                }
            }

            this.hardBreak = marker >= slength || hardBreakers.contains( c );

            return chunk.toString();
        }
    }
}
