require 'rubygems'
require 'bundler'
require 'bundler/monkey_patch'
require 'bundler/cli'
require 'stringio'
require 'thor/shell/basic'

module Nexus
  class Shell < Thor::Shell::Basic
  
    def stdout
      @stdout ||= StringIO.new
    end
    
    def stderr
      @stderr ||= StringIO.new
    end
    
  end

  class BundleRunner

    def exec( *args )

      shell = Shell.new

      Bundler::CLI.start( args, :shell => shell )

      shell.stdout.string

    rescue SystemExit => e
      raise err.string if e.exit_code != 0

      shell.stdout.string

    end

  end
end
# this makes it easy for a scripting container to 
# create an instance of this class
Nexus::BundleRunner
