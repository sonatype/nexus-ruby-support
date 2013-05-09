require 'rubygems'
require 'rubygems/format'
require 'maven/tools/minimal_project'
require 'json'
require 'nexus/bundler_dependencies'

module Nexus
  class Rubygems

    def create_quick( gemname, gemfile )
      spec = spec_get( gemfile )
      expected = spec.name + "-" + spec.version.to_s + ".gem"
      raise "mismatched filename: expected #{expected} but got #{gemname}" if gemname != expected
      Gem.deflate( Marshal.dump( spec ) ).bytes.to_a
    end

    def spec_get( gemfile )
      case gemfile
      when String
        Gem::Format.from_file_by_path( gemfile ).spec
      else
        Gem::Format.from_io( StringIO.new( read_binary( gemfile ) ) ).spec
      end
    end

    def to_pom( spec_source )
      spec = Marshal.load( Gem.inflate( read_binary( spec_source ) ) )
      proj = Maven::Tools::MinimalProject.new( spec )
      proj.to_xml
    end

    %W(name_preversions_map name_versions_map).each do |method|

      self.class_eval <<-EVAL
        def #{method}( source, modified )
          if @#{method}.nil? || @#{method}_modified != modified
            specs = load_specs( source )
            @#{method} = {}
            specs.select do |s|
              v = @#{method}[ s[0].to_s ] ||= []
              v << "\#{s[1]}-\#{s[2]}"
            end
            @#{method}_modified = modified
          end
          @#{method}
        end
      EVAL

    end

    def dependencies( specs, modified, 
                      prereleased_specs, prereleased_modified )
      if specs
        BundlerDependencies.new( name_versions_map( specs, modified ),
                                 name_preversions_map( prereleased_specs, prereleased_modified ) )
      else
        BundlerDependencies.new
      end
    end

    def list_versions( name, source, modified )
      versions = name_versions_map( source, modified )[ name.to_s ] || []
      versions = versions.select { |v| v =~ /(-|-ruby|-java|-jruby)$/ }.collect { |v| v.sub( /-.*$/, '' ) }
      versions.uniq!
      versions
    end

    def empty_specs
      dump_specs( [] )
    end

    def merge_specs( source, sources )
      result = if source
                 load_specs( source )
               else
                 []
               end
      sources.each do |s|
        result += load_specs( s )
      end
      dump_specs( result )
    end

    def add_spec( spec, source, type )
      # refill the map
      @name_versions_map = nil
      case type.downcase.to_sym
      when :latest
        do_add_spec( spec, source )
      when :release
        do_add_spec( spec, source ) unless spec.version.prerelease?
      when :prerelease
        do_add_spec( spec, source ) if spec.version.prerelease?
      end
    end

    def delete_spec( spec, source )
      # refill the map
      @name_versions_map = nil
      specs = load_specs( source )
      old_entry = [ spec.name, spec.version, spec.platform ]
      if specs.member? old_entry
        specs.delete old_entry
        dump_specs( specs )
      end
    end 

    private

    def do_add_spec( spec, source )
      specs = load_specs( source )
      new_entry = [ spec.name, spec.version, spec.platform ]
      unless specs.member? new_entry
        specs << new_entry
        dump_specs( specs )
      end
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
      io.close unless io.is_a? String
    end

    def load_specs( source )
      Marshal.load read_binary( source )
    end

    def dump_specs( specs )
      specs.uniq!
      specs.sort!
      Marshal.dump( compact_specs( specs ) ).bytes.to_a
    end

    def compact_specs( specs )
      names = {}
      versions = {}
      platforms = {}

      specs.map do |( name, version, platform )|
        names[ name ] = name unless names.include? name
        versions[ version ] = version unless versions.include? version
        platforms[ platform ] = platform unless platforms.include? platform

        [ names[ name ], versions[ version ], platforms[ platform ] ]
      end
    end
  end
end
Nexus::Rubygems
