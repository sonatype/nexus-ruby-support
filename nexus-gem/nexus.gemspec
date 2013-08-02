# -*- mode: ruby -*-
# -*- encoding: utf-8 -*-

Gem::Specification.new do |s|
  s.name = 'nexus'
  s.version = "0.2.6"

  s.authors = ["Nick Quaranto", 'Christian Meier']
  s.email = ['nick@quaran.to', 'm.kristian@web.de']

  s.description = 'Adds a command to RubyGems for uploading gems to a nexus server.'

  s.executables = ['nbundle']
  s.files = ["MIT-LICENSE", "Rakefile"]
  s.files += Dir['lib/**/*.rb']
  s.files += Dir['test/**/*.rb']

  s.homepage = 'https://github.com/sonatype/nexus-ruby-support/tree/master/nexus-gem'
  s.post_install_message = %q{
========================================================================

           Thanks for installing Nexus gem! You can now run:

    gem nexus          publish your gems onto Nexus server

    nbundle            a bundler fork with mirror support. 
                       just add a mirror with:

    bundle config mirror.http://rubygems.org http://localhost:8081/nexus/content/repositories/rubygems.org

                       and use 'nbundle' instead of 'bundle'

========================================================================

}
  s.require_paths = ["lib"]
  s.summary = %q{Commands to interact with nexus server}

  s.add_development_dependency('rake', '0.9.2.2')
  s.add_development_dependency('shoulda', "~> 3.1.1")
  # to use a version which works
  s.add_development_dependency('activesupport', "< 4.0.0")
  s.add_development_dependency('webmock', "~> 1.8.8")
  s.add_development_dependency('rr', ">= 0")
end
