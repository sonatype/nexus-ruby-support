require 'rubygems'
require 'rubygems/format'

module Nexus
  class Rubygems

    def create_quick( gemfile )
      Gem.deflate( Marshal.dump( spec_get( gemfile ) ) ).bytes.to_a
    end

    def spec_get( gemfile )
      case gemfile
      when String
        Gem::Format.from_file_by_path( gemfile ).spec
      else
        Gem::Format.from_io( StringIO.new( read_binary( gemfile ) ) ).spec
      end
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
