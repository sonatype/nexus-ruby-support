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
    @store = File.open(store)
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
    Dir.tmpdir(File.join(directory, "tmp"))    
    super(directory, options)
    @build_legacy = false
  end
  
  def collect_specs(yamls = gemspec_file_list)
    index = Gem::SourceIndex.new
    store = Gem::GemspecStore.new(File.join(@dest_directory, "gemspec.store"))
    progress = ui.progress_reporter yamls.size, "Loading yamls from #{store.file}", "loaded all yamls"
    Gem.time 'loaded' do
      entry = store.next
      while(entry) do
        if entry[0].string.size == 0 then
          alert_warning "Skipping zero-length yaml: #{entry[1]}"
	  next
        end

	begin
          spec = Gem::Specification.from_yaml(entry[0].string)
	  spec.loaded_from = entry[1]

	  abbreviate spec
	  sanitize spec

	  index.add_spec spec, spec.original_name

	  progress.updated spec.original_name

	  rescue SignalException => e
	    alert_error "Received signal, existing"
	    raise
	  rescue Exception => e
	    alert_error "Unable to process #{entry[1]}\n#{e.message} (#{e.class})\n\t#{e.backtrace.join "\n\t"}"

	end
        entry = store.next
      end
      progress.done
    end
    index
  end

  def gemspec_file_list
    []#Dir.glob(File.join(@dest_directory, "gems", "*.gem")) 
  end

  def gem_file_list
    gemspec_file_list
  end

end

