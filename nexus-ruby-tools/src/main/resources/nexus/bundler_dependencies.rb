require 'rubygems'
require 'json'
require 'fileutils'

module Nexus
  class BundlerDependencies

    attr_reader :array

    def initialize( name_versions_map, cache_dir )
      @versions = name_versions_map
      @cache_dir = cache_dir
      @array = []
    end
    
    def add_deps_for( gemname )
      deps = load_deps( gemname )
      deps.each do |version, deps|
        add( gemname, 
             version.sub( /-.*/, '' ), 
             version.sub( /^[^-]+-/, '' ),
             deps.entries )
      end
      ( (@versions[ gemname ] || [] ) - deps.keys ).collect { |v| v.sub( /-(ruby)?$/, '' ) }
    end
    
    def dump
      Marshal.dump( @array ).bytes.to_a
    end
    
    def update_cache( gemname, *specs )
      deps = load_deps( gemname ) 
      # flatten the specs to allow a single arrray arg as well
      specs.flatten.each do |file|
        spec = load_spec( gemname, file )
        d = deps_from( spec )
        vv = @versions[ gemname ]
        if vv.member?( spec.version.to_s + "-" + spec.platform.to_s )
          pl = spec.platform.to_s
        else
          pl =  spec.platform.os.to_s
        end
        deps[ spec.version.to_s + "-" + pl ] = d
        add( gemname, spec.version.to_s, pl, d )
      end
      save_deps( gemname, deps )
      deps.to_json
    end

    private

    def add( gemname, number, platform, deps )
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

    def save_deps( gemname, deps )
      dir = File.join( @cache_dir, gemname[0] )
      FileUtils.mkdir_p( dir )
      File.open( File.join( dir, gemname ), 'w') do |f|
        f.print deps.to_json
      end
    end

    def load_deps( gemname )
      file = File.join( @cache_dir, gemname[0], gemname )
      if File.exists?( file )
        JSON.load( File.read( file ) )
      else
        Hash.new
      end
    end
    
    def deps_from( spec )
      spec.runtime_dependencies.collect do |d|
        [ d.name, d.requirement.to_s ]
      end
    end
  end
end
