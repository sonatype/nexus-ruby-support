require 'nexus_indexer'

indexer = Gem::NexusIndexer.new @basedir

if @update then
  indexer.update_index
else
  indexer.generate_index
end
