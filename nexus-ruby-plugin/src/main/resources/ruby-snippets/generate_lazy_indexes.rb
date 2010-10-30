require 'nexus_lazy_indexer'

indexer = Gem::NexusLazyIndexer.new @basedir

if @update then
  indexer.update_index
else
  indexer.generate_index
end
