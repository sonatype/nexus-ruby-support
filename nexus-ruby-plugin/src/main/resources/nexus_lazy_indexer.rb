#!/usr/bin/env ruby

require 'rubygems'
require 'rubygems/indexer'
require 'yaml'

# do not understand why this is needed but does not work otherwise with jruby
class Dir
  class <<self
    alias_method :tmpdir_old, :tmpdir
    def tmpdir(dir = nil)
      @_d_i_r_ ||= dir
      if @_d_i_r_
        unless dir
          dir = @_d_i_r_
          @_d_i_r_ = nil
          dir
        end
      else
        tmpdir_old
      end
    end
  end
end

class Gem::GemspecStore

  attr_reader :file

  def initialize(store)
    @file = File.expand_path(store)
    # create an empty file if missing
    File.open(@file, 'w') {} unless File.exists? @file
    @store = File.open(@file)
  end

  def next
    spec_io = StringIO.new
    begin
      line = @store.readline
      while(!(line =~ /^#+/)) do
        spec_io << line unless line =~ /\#$/
        line = @store.readline
      end
      [spec_io, line.sub(/^#+/, '').sub(/\s+$/,'')]
    rescue EOFError
      @store.close
      nil
    end
  end
end

class Gem::NexusLazyIndexer < Gem::Indexer

  def initialize(directory, options = {})
    # do not understand why this is needed but does not work otherwise with jruby
    # Dir.tmpdir(File.join(directory, "tmp"))
    super(directory, options)
    @build_legacy = false
  end

  def collect_specs(gems_and_specs = gemspec_file_list)
    index = Gem::SourceIndex.new
    progress = ui.progress_reporter gems_and_specs.size, "Loading gems and specs from #{@dest_directory}", "loaded all gems and specs"
    Gem.time 'loaded' do
      gems_and_specs.each do |file|
        if File.size(file.to_s) == 0 then
          alert_warning "Skipping zero-length gem/spec: #{file}"
          next
        end

        begin
          spec = begin
              Gem::Specification.from_yaml(File.read(file))
            rescue
              Gem::Format.from_file_by_path(file).spec

#              unless gemfile =~ /\/#{Regexp.escape spec.original_name}.*\.gem\z/i then
#                expected_name = spec.full_name
#                expected_name << " (#{spec.original_name})" if spec.original_name != spec.full_name
#                alert_warning "Skipping misnamed gem: #{gemfile} should be named #{expected_name}"
#                next
#              end
            end
          spec.loaded_from = file

          abbreviate spec
          sanitize spec

          index.add_spec spec, spec.original_name

          progress.updated spec.original_name

        rescue SignalException => e
          alert_error "Received signal, existing"
          raise
        rescue Exception => e
          alert_error "Unable to process #{file}\n#{e.message} (#{e.class})\n\t#{e.backtrace.join "\n\t"}"
        end
      end

      progress.done
    end

    index
  end

  def gemspec_file_list
    Dir.glob(File.join(@dest_directory, "gems", "*.gem"))  + Dir.glob(File.join(@dest_directory, "gems", "*.spec"))
  end

  def gem_file_list
    gemspec_file_list
  end

  def terminate_interaction *args
  end
end

