if RUBY_VERSION =~ /^1.8/
  require 'rubygems'
  require 'rubygems/format'
end
module Nexus
  class Rubygems

    def initialize( basedir )
      @quick = File.join( basedir, 
                          'quick',  
                          "Marshal.#{Gem.marshal_version}" )
    end

    def create_quick( gemfile )
      Gem.deflate( Marshal.dump( spec_get( gemfile ) ) ).bytes.to_a
    end

    def spec_get( gemfile )
      Gem::Format.from_file_by_path( gemfile ).spec
    end

    def empty_specs
      dump_specs( [] )
    end

    def add_spec( spec, source, type )
p type
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
      specs.delete [ spec.name, spec.version, spec.platform ]
      dump_specs( specs )
    end 

    private

    def do_add_spec( spec, source )
      specs = load_specs( source )
      specs << [ spec.name, spec.version, spec.platform ]
      dump_specs( specs )
    end 

    def load_specs( source )
      specs = Marshal.load Gem.read_binary( source )
      specs.uniq!
      specs.sort!
      specs
    end

    def dump_specs( specs )
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
