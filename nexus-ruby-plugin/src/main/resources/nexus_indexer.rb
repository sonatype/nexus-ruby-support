#!/usr/bin/env ruby

require 'rubygems'
require 'rubygems/indexer'

# hack to allow to set a tmpdir in spec or so - jruby has problems to move
# away files from a system tmp dir (at least on ubuntu)
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

class Gem::NexusIndexer < Gem::Indexer

  def initialize(directory, options = {})
    super(directory, options)
    @build_legacy = false
  end

  def gem_file_list
    Dir.glob(File.join(@dest_directory, "gems", "**/*.gem"))
  end

  def terminate_interaction *args
  end
end

