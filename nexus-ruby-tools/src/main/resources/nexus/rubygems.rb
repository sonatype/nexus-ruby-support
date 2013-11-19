require 'rubygems'
begin
  require 'rubygems/format'
rescue LoadError
  # newer versions of rubygems do not have that anymore
  # just to stay backward compatible for a while
end
require 'maven/tools/minimal_project'
require 'json'
require 'nexus/bundler_dependencies'
require 'nexus/indexer'

module Nexus
  class Rubygems

    def recreate_rubygems_index( directory )
      indexer = Nexus::Indexer.new( directory )
      indexer.generate_index
      indexer.remove_tmp_dir

      # delete obsolete files
      Dir[ File.join( directory, '*' ) ].each do |f|
        if !f.match( /.*specs.#{Gem.marshal_version}.gz/ ) && !File.directory?( f )
          FileUtils.rm_f( f )
        end
      end

      # NOTE that code gave all kinds of result but the expected
      #      could be jruby related or not.
      #      just leave the permissions as they are
      #
      # fix permissions 
      # mode = 16877 # File.new( directory ).stat.mode # does not work with jruby
      # ( [ directory ] + Dir[ File.join( directory, '**', '*') ] ).each do |f|
      #   begin
      #     if File.directory? f
      #       FileUtils.chmod( mode, f )
      #     end
      #   rescue
      #     # well - let it as it is
      #   end
      # end
      nil
    end

    def purge_broken_depencency_files( directory )
      Dir[ File.join( directory, 
                      'api', 'v1', 'dependencies', 
                      '*', '*' ) ].each do |file|
        begin
          JSON.parse( File.read( file ) )
        rescue
          # just in case the file is directory delete it as well
          FileUtils.rm_rf( file )
        end
      end
      nil
    end

    def purge_broken_gemspec_files( directory )
      Dir[ File.join( directory, 
                      'quick', "Marshal.#{Gem.marshal_version}",
                      '*', '*' ) ].each do |file|
        begin
          Marshal.load( Gem.inflate( Gem.read_binary( file ) ) )
        rescue
          # just in case the file is directory delete it as well
          FileUtils.rm_rf( file )
        end
      end
      nil
    end

    def create_quick( gemname, gemfile )
      spec = spec_get( gemfile )
      expected = spec.file_name
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

    def merge_specs( source, sources, lastest = false )
      result = if source
                 load_specs( source )
               else
                 []
               end
      sources.each do |s|
        result += load_specs( s )
      end
      result = regenerate_latest( result ) if lastest
      dump_specs( result )
    end

    def regenerate_latest( specs )
      specs.sort!
      specs.uniq!
      map = {}
      specs.each do |s|
        list = map[ s[ 0 ] ] ||= []
        list << s
      end
      result = []
      map.each do |name, list|
        list.sort!
        list.uniq!
        lastest_versions = {}
        list.each do |i|
          version = i[1]
          platform = i[2]
          lastest_versions[ platform ] = i
        end
        result += lastest_versions.collect { |k, v| v }
      end
      result
    end
    private :regenerate_latest

    def add_spec( spec, source, type )
      case type.downcase.to_sym
      when :latest
        do_add_spec( spec, source, true )
      when :release
        # refill the map
        @name_versions_map = nil
        do_add_spec( spec, source ) unless spec.version.prerelease?
      when :prerelease
        # refill the map
        @name_preversions_map = nil
        do_add_spec( spec, source ) if spec.version.prerelease?
      end
    end

    def delete_spec( spec, source, ref_source = nil )
      # refill the map
      @name_versions_map = nil
      @name_preversions_map = nil
      specs = load_specs( source )
      old_entry = [ spec.name, spec.version, spec.platform.to_s ]
      if specs.member? old_entry
        specs.delete old_entry
        if ref_source
          ref_specs = load_specs( ref_source )
          ref_specs.delete old_entry
          specs = regenerate_latest( ref_specs )
        end
        dump_specs( specs )
      end
    end

    private

    def ensure_latest( spec, ref_source )
      ref = load_specs( ref_source )
      map = {}
      ref.each do |s|
        if s[ 0 ] == spec.name  and ( s[1] != spec.version or s[2].to_s != spec.platform.to_s )
          a = map[ s[1] ] ||= []
          a << s
        end
      end
      k = map.keys.sort.last
      map[ k ] || []
    end

    def do_add_spec( spec, source, latest = false )
      specs = load_specs( source )
      new_entry = [ spec.name, spec.version, spec.platform.to_s ]
      unless specs.member?( new_entry )
        if latest
          new_specs = regenerate_latest( specs + [ new_entry ] )
          dump_specs( new_specs ) if new_specs != specs
        else
          specs << new_entry
          dump_specs( specs )
        end
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
      io.close if io.respond_to? :close
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
