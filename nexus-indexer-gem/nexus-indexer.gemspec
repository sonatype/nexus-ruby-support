spec = Gem::Specification.new do |s|
  s.name = 'nexus-indexer'
  s.version = '0.0.1'
  s.summary = "Sonatype Nexus Gem Indexer."
  s.description = %{Sonatype Nexus modded Gem::Indexer.}
  s.files = Dir['lib/**/*.rb'] + Dir['test/**/*.rb']
  s.require_path = 'lib'
  s.has_rdoc = false
  s.extra_rdoc_files = Dir['[A-Z]*']
  s.rdoc_options << '--title' <<  'NexusIndexer -- Nexus Gem Indexer'
  s.author = "Juven Xu"
  s.email = "juvenxu@sonatype.com"
  s.homepage = "http://nexus.sonatype.org"
  s.add_dependency("rubygems-update", "~> 1.3.5")
end