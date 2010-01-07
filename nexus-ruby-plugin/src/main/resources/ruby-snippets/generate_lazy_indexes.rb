require 'nexus_indexer'

indexer = Gem::NexusIndexer.new @basedir
indexer.generate_index
