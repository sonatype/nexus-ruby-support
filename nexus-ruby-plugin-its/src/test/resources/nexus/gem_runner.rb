require 'rubygems'
require 'rubygems/gem_runner'
require 'rubygems/exceptions'
require 'stringio'

module Nexus
  class GemRunner < Gem::GemRunner

    def load_plugins
      Gem.load_plugins
    end

    def exec( *args )

      out = StringIO.new
      err = StringIO.new
      Gem::DefaultUserInteraction.ui = Gem::StreamUI.new( STDIN, out, err )

      run args

      out.string

    rescue Gem::SystemExitException => e
      raise err.string if e.exit_code != 0

      out.string

    end

  end
end
# this makes it easy for a scripting container to 
# create an instance of this class
Nexus::GemRunner
