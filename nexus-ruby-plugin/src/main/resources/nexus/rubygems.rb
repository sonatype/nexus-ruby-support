if RUBY_VERSION =~ /^1.8/
  require 'rubygems'
  require 'rubygems/format'
end
module Nexus
  class Rubygems

    def initialize(basedir)
      @quick = File.join(basedir, 
                         'quick',  
                         "Marshal.#{Gem.marshal_version}")
    end

    def create_quick(gemfile)
      spec = Gem::Format.from_file_by_path(gemfile).spec
      Gem.deflate( Marshal.dump(spec) ).bytes.to_a
    end

  end
end
Nexus::Rubygems
