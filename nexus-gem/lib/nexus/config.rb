require 'rubygems/local_remote_options'
require 'net/http'
require 'base64'

module Nexus
  class Config

    class File
      def initialize( file, repo )
        if file.is_a?( String )
          @file = file

          if file && ::File.exists?( file )
            @all = YAML.load( ::File.read( file ) )
          end
        elsif file
          @file = file.instance_variable_get( '@file'.to_sym )
          @all = file.instance_variable_get( '@all'.to_sym )
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
        if value
          @data[ key ] = value
        else
          @data.delete( key )
        end
      end
      
      def store
        if @file
          dirname = ::File.dirname( @file )
          Dir.mkdir( dirname ) unless ::File.exists?( dirname )
          new = !::File.exists?( @file )

          ::File.open( @file, 'w') do |f|
            f.write @all.to_yaml
          end
          if new
            ::File.chmod( 0100600, @file ) rescue nil
          end
          true
        else
          false
        end
      end
    end

    def self.default_file
      ::File.join( Gem.user_home, '.gem', 'nexus' )
    end

    def initialize( repo = nil, config = nil, secrets = nil )
      config ||= self.class.default_file
      conf = File.new( config, nil )
      if secrets
        conf[ :secrets ] = secrets
        conf.store
      end
      @conf = File.new( conf, repo )
      @secr = File.new( secrets || conf[ :secrets ], repo )
    end

    def key?( key )
      @conf.key?( key ) || @secr.key?( key )
    end

    def []( key )
      if key == :authorization && @conf[ key ]
        copy_authorization
      end
      @conf[ key ] || @secr[ key ]
    end

    def copy_authorization
      @secr[ :authorization ] = @conf[ :authorization ]
      if @secr.store
        @conf[ :authorization ] = nil
        @conf.store
      end
    end
    private :copy_authorization

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
