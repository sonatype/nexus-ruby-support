require 'rubygems'
require 'json'
require 'fileutils'

module Nexus
  class BundlerDependencies

    attr_reader :array

    def initialize( name_versions_map = {}, name_preversions_map = {} )
      @versions = name_versions_map
      @preversions = name_preversions_map
      @array = []
    end
    
    def versions( gemname )
      ( @versions[ gemname ] || [] ) + ( @preversions[ gemname ] || [] )
    end

    def add( gemname, data )
      deps_map = load_dependencies( data )
      deps_map.each do |version, deps|
        add_to_array( gemname, 
                      version.sub( /-.*/, '' ), 
                      version.sub( /^[^-]+-/, '' ),
                      deps.entries )
      end
      ( versions( gemname ) - deps_map.keys ).collect { |v| v.sub( /-(ruby)?$/, '' ) }
    end

    def update( gemname, data, *specs )
      deps_map = load_dependencies( data ) 
      # flatten the specs to allow a single arrray arg as well
      specs.flatten.each do |file|
        spec = load_spec( gemname, file )
        dependencies = deps_from( spec )
        # TODO do something when version does not exists but spec.rz
        vv = versions( gemname )
        if vv.member?( spec.version.to_s + "-" + spec.platform.to_s )
          platform = spec.platform.to_s
        elsif spec.platform.respond_to? :os
          platform =  spec.platform.os.to_s
        else
          platform = spec.platform.to_s
        end
        deps_map[ spec.version.to_s + "-" + platform ] = dependencies
        add_to_array( gemname, spec.version.to_s, platform, dependencies )
      end
      deps_map.to_json
    end

    def dump
      Marshal.dump( @array ).bytes.to_a
    end
    
    private

    def add_to_array( gemname, number, platform, deps )
      @array << { :name => gemname,
          :number => number,
          :platform => platform,
          :dependencies => deps }
    end

    def read_binary( io )
      case io
      when String
        Gem.read_binary( io )
      else
        result = []
        while ( ( b = io.read ) != -1 ) do
          result << b
        end
        result.pack 'C*'
      end
    ensure
      io.close if io.respond_to? :close
    end

    def load_spec( gemname, file )
      spec = Marshal.load( Gem.inflate( read_binary( file ) ) )
      if spec.name != gemname
        raise 'name mismatch. given name: #{gemname} <> spec: #{spec.name}'
      end
      spec
    end

    def load_dependencies( data )
      if data
        JSON.parse( read_binary( data ) ) rescue Hash.new
      else
        Hash.new
      end
    end
    
    def deps_from( spec )
      spec.runtime_dependencies.collect do |d|
        # issue https://github.com/sonatype/nexus-ruby-support/issues/25
        name = case d.name
               when Array
                 d.name.first
               else
                 d.name
               end
        [ name, d.requirement.to_s ]
      end
    end
  end
end
