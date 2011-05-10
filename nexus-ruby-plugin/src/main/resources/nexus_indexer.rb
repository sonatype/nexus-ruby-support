#!/usr/bin/env ruby

require 'rubygems'
require 'rubygems/indexer'

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

class Gem::NexusIndexer < Gem::Indexer

  def initialize(directory, options = {})
    # do not understand why this is needed but does not work otherwise with jruby
    #Dir.tmpdir(File.join(directory, "tmp"))
    super(directory, options)
    @build_legacy = false
  end

  def gem_file_list
    Dir.glob(File.join(@dest_directory, "gems", "**/*.gem"))
  end

  def terminate_interaction *args
  end
end

