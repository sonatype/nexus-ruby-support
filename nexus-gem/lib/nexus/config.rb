require 'rubygems/local_remote_options'
require 'net/http'
require 'base64'
require 'nexus/cipher'
require 'yaml'

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

    def initialize( repo = nil, config = nil, 
                    secrets = nil, pass = nil )
      config ||= self.class.default_file
      conf = File.new( config, nil )
      if secrets
        conf[ :secrets ] = secrets
        conf.store
      end
      secr = File.new( conf[ :secrets ], nil )# if conf[ :secrets ]
      token = conf[ :token ] || secr[ :token ]
      if pass && token
        @cipher = Cipher.new( pass, token )
      elsif pass
        @cipher = Cipher.new( pass )
        token = @cipher.token
      end
      @encrypted = token != nil

      if token
        secr[ :token ] = token
        token = nil if secr.store
        conf[ :token ] = token
        conf.store
      end

      @conf = File.new( conf, repo )
      @secr = File.new( secrets || conf[ :secrets ], repo )
    end

    def encrypted?
      @encrypted
    end
    def key?( key )
      @conf.key?( key ) || @secr.key?( key )
    end

    def []( key )
      if key == :authorization 
        move( :authorization, :iv ) if @conf[ key ]
        decrypt( @conf[ key ] || @secr[ key ] )
      else
        @conf[ key ] || @secr[ key ]
      end
    end
    
    def decrypt( auth )
      if @cipher && auth && self[ :iv ]
        @cipher.iv = self[ :iv ]
        @cipher.decrypt( auth )
      elsif @cipher && auth
        self[ :authorization ] = auth
        auth
      else
        auth
      end
    end
    private :decrypt

    def move( *keys )
      keys.each do |key|
        @secr[ key ] = @conf[ key ]
      end
      if @secr.store
        keys.each do |key|
          @conf[ key ] = nil
        end
        @conf.store
      end
    end
    private :move

    def encrypt( auth )
      if @cipher && auth
        result = @cipher.encrypt( auth )
        self[ :iv ] = @cipher.iv
        result
      else
        auth
      end
    end
    private :encrypt

    def []=( key, value )
      stored = false
      if key == :authorization
        value = encrypt( value )
        @secr[ key ] = value
        stored = @secr.store
      end
      if key == :iv
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
