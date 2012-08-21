if RUBY_VERSION =~ /^1.8/
  require 'rubygems'
  require 'rubygems/format'
end
module Nexus
  class Check

    def check(gemfile, gemspec)
      spec_from_gem = Gem::Format.from_file_by_path(gemfile).spec
      spec_from_gemspec = Marshal.load( Gem.inflate( Gem.read_binary( gemspec) ) )
      spec_from_gem == spec_from_gemspec
    end

  end
end
Nexus::Check.new
