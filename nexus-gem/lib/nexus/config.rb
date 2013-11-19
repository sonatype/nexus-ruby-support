require 'rubygems/local_remote_options'
require 'net/http'
require 'base64'

module Nexus
  class Config

    class File
      def initialize( file, repo )
        @file = file

        if file && ::File.exists?( file )
          @all = YAML.load( ::File.read( file ) )
        end
        @all ||= {}

        @data = ( @all[ repo ] ||= {} ) if repo
        @data ||= @all
      end

      def key?( key )
        @data.key? key
      end

      def []( key )
        @data[ key ]
      end

      def []=( key, value )
        @data[ key ] = value
      end
      
      def store
        if @file
          dirname = ::File.dirname( @file )
          Dir.mkdir( dirname ) unless ::File.exists?( dirname )
          
          ::File.open( @file, 'w') do |f|
            f.write @all.to_yaml
          end
          true
        else
          false
        end
      end
    end

    def initialize( repo = nil, config = nil, secrets = nil )
      config ||= ::File.join( Gem.user_home, '.gem', 'nexus' )
      @conf = File.new( config, repo )
      @secr = File.new( secrets || @conf[ :secrets ], repo )

      if secrets
        @conf[ :secrets ] = secrets
        @conf.store
      end
    end

    def key?( key )
      @conf.key?( key ) || @secr.key?( key )
    end

    def []( key )
      @conf[ key ] || @secr[ key ]
    end

    def []=( key, value )
      stored = false
      if key == :authorization
        @secr[ key ] = value
        stored = @secr.store
      end

      unless stored
        @conf[ key ] = value
        @conf.store
      end
    end

  end
end
